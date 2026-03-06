package com.cmswe.alumni.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cmswe.alumni.api.user.AuthService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.WechatApiService;
import com.cmswe.alumni.api.user.InvitationService;
import com.cmswe.alumni.common.dto.GetPhoneNumberRequest;
import com.cmswe.alumni.common.dto.ConfirmInvitationDto;
import com.cmswe.alumni.common.dto.WxInitRequest;
import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.entity.RoleUser;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.entity.InvitationRecord;
import com.cmswe.alumni.common.entity.PointsChange;
import com.cmswe.alumni.common.utils.JwtUtils;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.WechatMiniUtil;
import com.cmswe.alumni.common.vo.GetPhoneNumberResponse;
import com.cmswe.alumni.common.vo.RoleListVo;
import com.cmswe.alumni.common.vo.WxInitResponse;
import com.cmswe.alumni.service.user.mapper.AlumniEducationMapper;
import com.cmswe.alumni.service.user.mapper.RoleUserMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import com.cmswe.alumni.service.user.mapper.InvitationRecordMapper;
import com.cmswe.alumni.service.user.mapper.PointsChangeMapper;
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
    private AlumniEducationMapper alumniEducationMapper;

    @Resource
    private InvitationRecordMapper invitationRecordMapper;

    @Resource
    private PointsChangeMapper pointsChangeMapper;

    @Resource
    private WechatMiniUtil wechatMiniUtil;

    @Resource
    private RoleService roleService;

    @Resource
    private InvitationService invitationService;

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

        //3. 获取微信用户标识信息
        String unionId = (String) sessionInfo.get("unionid");
        String openid = (String) sessionInfo.get("openid");

        // 未绑定开放平台时没有unionid，此时使用openid作为唯一标识
        if (unionId == null || unionId.trim().isEmpty()) {
            log.warn("微信返回结果中不包含unionid，小程序可能未绑定到开放平台，将使用openid作为唯一标识");
        }

        log.info("获取到微信用户信息: unionId={}, openid={}", unionId, openid);

        // ===== 以下为后续步骤，需要你自己实现 =====

        //4. 判断用户是否存在数据库
        // 优先通过unionid查询（如果有），否则通过openid查询
        WxUser loginUser = wxUserMapper.selectOne(new LambdaQueryWrapper<WxUser>().eq(WxUser::getOpenid, openid));

        // 使用 openid 是否为空判断是否第一次登录：为空=第一次，不为空=非第一次
        boolean isFirstLogin = (loginUser == null);
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
            //4.2 非首次登录 - 更新登录时间和IP
            wxUserMapper.update(
                    new LambdaUpdateWrapper<WxUser>()
                            .set(WxUser::getLastLoginTime, LocalDateTime.now())
                            .set(WxUser::getLastLoginIp, getRealClientIp(request))
                            .eq(WxUser::getWxId, loginUser.getWxId())
            );
        }

        //5. 检查校友卡申领状态

        //6. 生成JWT Token
        String token = jwtUtils.token(JSON.toJSONString(loginUser), null);

        //7. 获取用户角色信息（这里只获取了用户的系统角色，没有获取到组织相关角色）
        List<RoleListVo> roleList = roleService.getRoleListVoByWxId(loginUser.getWxId());

        //8. 检查用户基本信息是否完善
        boolean isProfileComplete = checkUserProfileComplete(loginUser.getWxId());

        //9. 构建响应，仅首次登录时返回被邀请人和邀请人wxid
        WxInitResponse.WxInitResponseBuilder responseBuilder = WxInitResponse.builder()
                .token(token)
                .roles(roleList)
                .certificationFlag(loginUser.getCertificationFlag())
                .isProfileComplete(isProfileComplete);
        if (isFirstLogin) {
            Long inviterWxIdLong = parseInviterWxId(inviterWxUuid);
            if (inviterWxIdLong != null) {
                ConfirmInvitationDto confirmInvitationDto = ConfirmInvitationDto.builder()
                        .inviterWxId(String.valueOf(inviterWxIdLong))
                        .inviteeWxId(String.valueOf(loginUser.getWxId()))
                        .build();
                try {
                    invitationService.confirmInvitation(confirmInvitationDto);
                    log.info("调用邀请确认接口成功：inviterWxId={}, inviteeWxId={}", inviterWxIdLong, loginUser.getWxId());
                } catch (Exception e) {
                    log.error("调用邀请确认接口失败：inviterWxId={}, inviteeWxId={}, error={}", inviterWxIdLong, loginUser.getWxId(), e.getMessage());
                }
            }
        }

        log.info("用户登录: openid={}, token={}, roles={}, certificationFlag={}, isProfileComplete={}, isFirstLogin={}",
                openid, token, roleList, loginUser.getCertificationFlag(), isProfileComplete, isFirstLogin);

        return responseBuilder.build();
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
     * 将邀请人UUID字符串解析为Long类型的wxId
     * @param inviterWxUuid 邀请人wxId字符串（来自扫码scene）
     * @return 解析成功返回wxId，失败返回null
     */
    private Long parseInviterWxId(String inviterWxUuid) {
        if (inviterWxUuid == null || inviterWxUuid.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(inviterWxUuid.trim());
        } catch (NumberFormatException e) {
            log.warn("邀请人wxId解析失败: inviterWxUuid={}", inviterWxUuid);
            return null;
        }
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

    /**
     * 检查用户基本信息是否完善
     * 需要满足以下条件：
     * 1. 真实姓名不为空
     * 2. 手机号不为空
     * 3. 性别不为空且不为0（未知）
     * 4. 至少有一条学习经历
     *
     * @param wxId 用户ID
     * @return true-信息完善，false-信息未完善
     */
    private boolean checkUserProfileComplete(Long wxId) {
        try {
            // 1. 查询用户基本信息
            WxUserInfo userInfo = wxUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<WxUserInfo>()
                            .eq(WxUserInfo::getWxId, wxId)
            );

            // 如果用户信息不存在，返回false
            if (userInfo == null) {
                log.warn("用户信息不存在，wxId={}", wxId);
                return false;
            }

            // 2. 检查真实姓名
            if (userInfo.getName() == null || userInfo.getName().trim().isEmpty()) {
                log.debug("用户真实姓名未填写，wxId={}", wxId);
                return false;
            }

            // 3. 检查手机号
            if (userInfo.getPhone() == null || userInfo.getPhone().trim().isEmpty()) {
                log.debug("用户手机号未填写，wxId={}", wxId);
                return false;
            }

            // 4. 检查性别（0-未知，1-男，2-女）
            if (userInfo.getGender() == null || userInfo.getGender() == 0) {
                log.debug("用户性别未填写，wxId={}", wxId);
                return false;
            }

            // 5. 检查是否至少有一条学习经历
            Long educationCount = alumniEducationMapper.selectCount(
                    new LambdaQueryWrapper<com.cmswe.alumni.common.entity.AlumniEducation>()
                            .eq(com.cmswe.alumni.common.entity.AlumniEducation::getWxId, wxId)
            );

            if (educationCount == null || educationCount == 0) {
                log.debug("用户学习经历为空，wxId={}", wxId);
                return false;
            }

            // 所有条件都满足，信息完善
            log.debug("用户基本信息完善，wxId={}", wxId);
            return true;

        } catch (Exception e) {
            log.error("检查用户信息完善状态失败，wxId={}, error={}", wxId, e.getMessage(), e);
            // 发生异常时返回false，防止用户因系统错误而无法正常使用
            return false;
        }
    }

    /**
     * 获取微信用户手机号
     *
     * @param request 获取手机号请求参数
     * @return 用户手机号信息
     */
    @Override
    public GetPhoneNumberResponse getPhoneNumber(GetPhoneNumberRequest request) {
        // 1. 参数校验
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            log.error("getPhoneNumber 参数错误: code为空");
            throw new BusinessException(400, "参数错误：code不能为空");
        }

        log.info("开始获取微信用户手机号, code={}", code);

        try {
            // 2. 调用微信API获取手机号
            Map<String, Object> phoneInfo = wechatMiniUtil.getUserPhoneNumber(code);
            log.info("微信返回手机号信息: {}", phoneInfo);

            // 3. 提取手机号信息
            String phoneNumber = (String) phoneInfo.get("phoneNumber");
            String purePhoneNumber = (String) phoneInfo.get("purePhoneNumber");
            String countryCode = (String) phoneInfo.get("countryCode");

            // 4. 构建响应
            GetPhoneNumberResponse response = GetPhoneNumberResponse.builder()
                    .phoneNumber(phoneNumber)
                    .purePhoneNumber(purePhoneNumber)
                    .countryCode(countryCode)
                    .build();

            log.info("获取手机号成功: phoneNumber={}, purePhoneNumber={}, countryCode={}",
                    phoneNumber, purePhoneNumber, countryCode);

            return response;

        } catch (BusinessException e) {
            log.error("获取手机号失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取手机号异常", e);
            throw new BusinessException(500, "获取手机号失败: " + e.getMessage());
        }
    }

    @Override
    public WxInitResponse testLogin(Long wxId) throws JsonProcessingException {
        // 1. 参数校验
        if (wxId == null) {
            log.error("testLogin 参数错误: wxId为空");
            throw new BusinessException(400, "参数错误：wxId不能为空");
        }

        log.info("开始测试登录 - 用户ID: {}", wxId);

        // 2. 查询用户信息
        WxUser loginUser = wxUserMapper.selectById(wxId);
        if (loginUser == null) {
            log.error("测试登录失败 - 用户不存在: wxId={}", wxId);
            throw new BusinessException(404, "用户不存在");
        }

        // 3. 生成JWT Token（与正式登录保持一致）
        String token = jwtUtils.token(JSON.toJSONString(loginUser), null);

        log.info("为用户生成测试Token - 用户ID: {}, OpenID: {}", loginUser.getWxId(), loginUser.getOpenid());

        // 4. 获取用户角色信息
        List<RoleListVo> roleList = roleService.getRoleListVoByWxId(loginUser.getWxId());

        log.info("查询到用户角色 - 用户ID: {}, 角色数量: {}", loginUser.getWxId(), roleList.size());

        // 5. 检查用户基本信息是否完善
        boolean isProfileComplete = checkUserProfileComplete(loginUser.getWxId());

        // 6. 返回用户信息 + Token + 角色信息 + 信息完善标识
        WxInitResponse response = WxInitResponse.builder()
                .token(token)
                .roles(roleList)
                .certificationFlag(loginUser.getCertificationFlag())
                .isProfileComplete(isProfileComplete)
                .build();

        log.info("测试登录成功 - 用户ID: {}, 认证标识: {}, 信息完善: {}",
                loginUser.getWxId(), loginUser.getCertificationFlag(), isProfileComplete);

        return response;
    }

    /**
     * 用户注册接口（完善用户基本信息和教育经历）
     *
     * @param wxId 微信用户ID（从token解析）
     * @param name 真实姓名
     * @param schoolId 学校ID
     * @param gender 性别
     * @param phone 手机号
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(Long wxId, String name, Long schoolId, Integer gender, String phone) {
        log.info("开始注册用户 - wxId: {}, name: {}, schoolId: {}, gender: {}, phone: {}",
                wxId, name, schoolId, gender, maskPhone(phone));

        try {
            // 1. 更新 wx_user_info 表
            WxUserInfo userInfo = wxUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<WxUserInfo>()
                            .eq(WxUserInfo::getWxId, wxId)
            );

            if (userInfo == null) {
                log.error("用户信息不存在，无法注册 - wxId: {}", wxId);
                throw new BusinessException(404, "用户信息不存在");
            }

            // 更新用户基本信息
            userInfo.setName(name);
            userInfo.setGender(gender);
            userInfo.setPhone(phone);
            int updateCount = wxUserInfoMapper.updateById(userInfo);

            if (updateCount == 0) {
                log.error("更新用户信息失败 - wxId: {}", wxId);
                throw new BusinessException(500, "更新用户信息失败");
            }

            log.info("用户信息更新成功 - wxId: {}", wxId);

            // 2. 检查教育经历表中是否已有该学校的记录
            com.cmswe.alumni.common.entity.AlumniEducation existingEducation = alumniEducationMapper.selectOne(
                    new LambdaQueryWrapper<com.cmswe.alumni.common.entity.AlumniEducation>()
                            .eq(com.cmswe.alumni.common.entity.AlumniEducation::getWxId, wxId)
                            .eq(com.cmswe.alumni.common.entity.AlumniEducation::getSchoolId, schoolId)
            );

            // 3. 如果不存在该学校的教育经历，则新增
            if (existingEducation == null) {
                com.cmswe.alumni.common.entity.AlumniEducation newEducation = new com.cmswe.alumni.common.entity.AlumniEducation();
                newEducation.setWxId(wxId);
                newEducation.setSchoolId(schoolId);
                newEducation.setType(1); // 1-主要经历
                newEducation.setCertificationStatus(0); // 0-未认证

                int insertCount = alumniEducationMapper.insert(newEducation);

                if (insertCount == 0) {
                    log.error("新增教育经历失败 - wxId: {}, schoolId: {}", wxId, schoolId);
                    throw new BusinessException(500, "新增教育经历失败");
                }

                log.info("新增教育经历成功 - wxId: {}, schoolId: {}", wxId, schoolId);
            } else {
                log.info("该学校的教育经历已存在，无需新增 - wxId: {}, schoolId: {}", wxId, schoolId);
            }

            log.info("用户注册完成 - wxId: {}", wxId);

            // ====== 邀请功能相关逻辑 START ======
            // 注册之后检查该用户是否是被邀请人
            InvitationRecord invitationRecord = invitationRecordMapper.selectOne(
                    new LambdaQueryWrapper<InvitationRecord>()
                            .eq(InvitationRecord::getInviteeWxId, wxId)
                            .eq(InvitationRecord::getIsRegister, 0) // 只处理未注册的邀请记录
            );

            if (invitationRecord != null) {
                // 1. 更新邀请记录表的 is_register 字段为1
                invitationRecord.setIsRegister(1);
                invitationRecordMapper.updateById(invitationRecord);
                log.info("更新邀请记录成功，被邀请人已注册 - inviteeWxId: {}", wxId);

                Long inviterWxId = invitationRecord.getInviterWxId();
                // 2. 给邀请人的 wx_user_info 表中的 integral 积分 +1
                WxUserInfo inviterUserInfo = wxUserInfoMapper.selectOne(
                        new LambdaQueryWrapper<WxUserInfo>()
                                .eq(WxUserInfo::getWxId, inviterWxId)
                );

                if (inviterUserInfo != null) {
                    Integer originalPoints = inviterUserInfo.getIntegral() != null ? inviterUserInfo.getIntegral() : 0;
                    inviterUserInfo.setIntegral(originalPoints + 1);
                    wxUserInfoMapper.updateById(inviterUserInfo);
                    log.info("邀请人积分增加成功 - inviterWxId: {}, originalPoints: {}, afterPoints: {}", inviterWxId, originalPoints, inviterUserInfo.getIntegral());

                    // 3. 在 points_change 表记录邀请人积分的变化
                    PointsChange pointsChange = PointsChange.builder()
                            .wxId(inviterWxId)
                            .type(0) // 0-邀请
                            .originalPoints(originalPoints)
                            .afterPoints(inviterUserInfo.getIntegral())
                            .createTime(LocalDateTime.now())
                            .build();
                    pointsChangeMapper.insert(pointsChange);
                    log.info("邀请人积分变化记录成功 - inviterWxId: {}, type: {}, originalPoints: {}, afterPoints: {}",
                            inviterWxId, pointsChange.getType(), originalPoints, inviterUserInfo.getIntegral());
                } else {
                    log.warn("未找到邀请人的用户信息，无法增加积分 - inviterWxId: {}", inviterWxId);
                }
            } else {
                log.info("用户不是被邀请人或邀请记录已处理 - wxId: {}", wxId);
            }
            // ====== 邀请功能相关逻辑 END ======
            return true;

        } catch (BusinessException e) {
            log.error("用户注册失败 - wxId: {}, error: {}", wxId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户注册异常 - wxId: {}, error: {}", wxId, e.getMessage(), e);
            throw new BusinessException(500, "用户注册失败: " + e.getMessage());
        }
    }
}
