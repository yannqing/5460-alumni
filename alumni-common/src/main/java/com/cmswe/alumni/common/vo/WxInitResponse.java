package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 微信小程序静默登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "微信小程序静默登录响应")
public class WxInitResponse {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户角色列表")
    private List<RoleListVo> roles;

    @Schema(description = "是否成为校友：0-否，1-是")
    private Integer isAlumni;
}
