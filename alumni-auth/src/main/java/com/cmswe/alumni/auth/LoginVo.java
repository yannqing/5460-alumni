package com.cmswe.alumni.auth;

import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.entity.WxUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录成功后返回的封装信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {

    private String userId;
    private String nickname;
    private String avatar;
    private String token;
    private List<Role> roles;

    public LoginVo(WxUser user, String token, List<Role> roles) {
        this.userId = user.getOpenid();
        this.token = token;
        this.roles = roles;
    }

}
