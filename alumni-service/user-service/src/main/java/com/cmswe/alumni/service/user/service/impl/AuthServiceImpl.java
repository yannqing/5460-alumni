package com.cmswe.alumni.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cmswe.alumni.api.user.AuthService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.WechatApiService;
import com.cmswe.alumni.common.dto.WxInitRequest;
import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.entity.RoleUser;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.WechatMiniUtil;
import com.cmswe.alumni.common.vo.RoleListVo;
import com.cmswe.alumni.common.vo.WxInitResponse;
import com.cmswe.alumni.service.user.mapper.RoleUserMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private RoleUserMapper roleUserMapper;

    @Resource
    private WxUserInfoMapper wxUserInfoMapper;

    @Resource
    private WechatMiniUtil wechatMiniUtil;

    @Resource
    private RoleService roleService;

    /**
     * 微信小程序静默登录
     *
     * @param wxInitRequest 微信登录请求参数
     * @return 用户信息和Token
     */
    @Transactional(rollbackFor = Exception.class)
    public WxInitResponse wxInit(WxInitRequest wxInitRequest, HttpServletRequest request) throws JsonProcessingException {
        String code = wxInitRequest.getCode();
        String inviterWxUuid = wxInitRequest.getInviterWxUuid();

        //1. 校验参数
        if (code == null || code.trim().isEmpty()) {
            log.error("wxInit 参数错误: code为空");
            throw new BusinessException(400, "参数错误：code不能为空");
        }

        log.info("开始微信静默登录, code={}, inviterWxUuid={}", code, inviterWxUuid);

        //2. 调用微信API获取session信息（code2session）
        Map<String, Object> sessionInfo = wechatMiniUtil.code2session(code);
        log.info("微信code2session返回结果: {}", sessionInfo);

        //3. 验证unionid是否存在（防止非法访问）
        if (!sessionInfo.containsKey("unionid") || sessionInfo.get("unionid") == null) {
            log.error("微信返回结果中不包含unionid，可能小程序未绑定到开放平台");
            throw new BusinessException(401, "非法访问：请确保小程序已绑定到微信开放平台");
        }

        String unionId = (String) sessionInfo.get("unionid");
        String openid = (String) sessionInfo.get("openid");
        log.info("获取到微信用户信息: unionId={}, openid={}", unionId, openid);

        // ===== 以下为后续步骤，需要你自己实现 =====

        //4. 判断用户是否存在数据库（通过unionid查询）
        WxUser loginUser = wxUserMapper.selectOne(new LambdaQueryWrapper<WxUser>().eq(WxUser::getUnionId, unionId));
        if (loginUser == null) {
            //4.1 首次登录 - 创建新用户
            loginUser = new WxUser();
            loginUser.setOpenid(openid);
            loginUser.setUnionId(unionId);
//            wxUser.setLatitude();
//            wxUser.setLongitude();
            loginUser.setLastLoginTime(LocalDateTime.now());
            loginUser.setLastLoginIp(getRealClientIp(request));
            wxUserMapper.insert(loginUser);

            //4.2 初始化用户信息
            initData(loginUser.getWxId());
        } else {
            //4.2 非首次登录
            wxUserMapper.update(
                    new LambdaUpdateWrapper<WxUser>()
                            .set(WxUser::getLastLoginTime, LocalDateTime.now())
                            .set(WxUser::getLastLoginIp, getRealClientIp(request))
            );
        }

        //5. 检查校友卡申领状态

        //6. 生成JWT Token
        String token = jwtUtils.token(JSON.toJSONString(loginUser), null);

        //7. 获取用户角色信息（这里只获取了用户的系统角色，没有获取到组织相关角色）
        List<RoleListVo> roleList = roleService.getRoleListVoByWxId(loginUser.getWxId());

        //8. 返回用户信息 + Token + 角色信息
        log.info("用户登录: openid={}, token={}, roles={}, isAlumni={}", openid, token, roleList, loginUser.getIsAlumni());

        return WxInitResponse.builder()
                .token(token)
                .roles(roleList)
                .isAlumni(loginUser.getIsAlumni())
                .build();
    }

    /**
     * 用户首次登录的初始化信息处理 TODO 后续用 Kafka 做解耦处理
     * @param wxId 用户id
     */
    private void initData(Long wxId) {
        //1. 创建角色 TODO 这里不能写死，后面优化
        Role systemUser = roleService.getRoleByCodeInner("SYSTEM_USER");
        RoleUser roleUser = new RoleUser();
        roleUser.setRoleId(systemUser.getRoleId());
        roleUser.setWxId(wxId);
        roleUserMapper.insert(roleUser);

        //2. 初始化个人基本信息
        WxUserInfo wxUserInfo = new WxUserInfo();
        wxUserInfo.setWxId(wxId);
        wxUserInfoMapper.insert(wxUserInfo);

        // TODO 处理邀请关系
        // - 查找邀请人
        // - 创建邀请关系记录
    }

    /**
     * 获取真实 ip TODO 后续换位置（多个地方使用）
     * @param request
     * @return
     */
    private String getRealClientIp(HttpServletRequest request) {
        // 优先从 X-Forwarded-For 获取
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 最后才使用 getRemoteAddr
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For 可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * openid脱敏
     */
    private String maskOpenid(String openid) {
        if (openid == null || openid.length() < 10) {
            return openid;
        }
        return openid.substring(0, 6) + "****" + openid.substring(openid.length() - 4);
    }
}
