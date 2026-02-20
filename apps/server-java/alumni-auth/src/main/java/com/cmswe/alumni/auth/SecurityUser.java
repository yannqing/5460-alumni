package com.cmswe.alumni.auth;

import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.entity.WxUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    private List<SimpleGrantedAuthority> simpleGrantedAuthorities;

    @Getter
    @Setter
    private List<Role> role;

    @Getter
    private final WxUser wxUser;

    public SecurityUser(WxUser user) {
        this.wxUser = user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return simpleGrantedAuthorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        // 对于微信小程序，优先返回openid，其次是手机号
        if (wxUser.getOpenid() != null && !wxUser.getOpenid().isEmpty()) {
            return wxUser.getOpenid();
        }
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 根据新的User表结构，检查enabled字段（1-可用，0-禁用）
        return wxUser.getIsEnabled() != null && wxUser.getIsEnabled() == 1;
    }
}
