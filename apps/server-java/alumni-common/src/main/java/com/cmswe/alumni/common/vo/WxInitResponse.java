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

    @Schema(description = "认证标识（0-未认证，1-校友总会认证，2-校促会认证，3-校友会认证）")
    private Integer certificationFlag;

    @Schema(description = "用户基本信息是否完善：true-完善，false-未完善")
    private Boolean isProfileComplete;

    @Schema(description = "被邀请人wxid（仅首次登录时返回）")
    private Long inviteeWxId;

    @Schema(description = "邀请人wxid（仅首次登录且有邀请人时返回）")
    private Long inviterWxId;
}
