package com.cmswe.alumni.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.entity.RoleUser;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.service.user.mapper.RoleMapper;
import com.cmswe.alumni.service.user.mapper.RoleUserMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SecurityUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleUserMapper roleUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        //对于微信小程序，这里的username实际上是手机号或openId
//        log.info("loading user by code: {}", username);
//
//        WxUser wxUser = null;
//
//        //通过 openid 来查询
//        wxUser = wxUserMapper.findByOpenid(username);
//
//        log.debug("login user: {}", wxUser);
//        if (wxUser == null) {
//            throw new UsernameNotFoundException("用户不存在");
//        }
//        //获取用户的角色
//        List<RoleUser> roleUsers = roleUserMapper.selectList(new QueryWrapper<RoleUser>().eq("wx_id", wxUser.getWxId()));
//        List<Role> roles = roleMapper.selectBatchIds(roleUsers.stream().map(RoleUser::getRoleId).collect(Collectors.toList()));
//        SecurityUser securityUser = new SecurityUser(wxUser);
//        securityUser.setRole(roles);
//
//        return securityUser;

        return null;
    }
}
