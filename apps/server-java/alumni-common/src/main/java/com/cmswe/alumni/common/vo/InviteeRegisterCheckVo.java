package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 被邀请人是否注册检查响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "被邀请人是否注册检查响应")
public class InviteeRegisterCheckVo {

    @Schema(description = "是否为被邀请用户（邀请记录表中存在该wxid作为被邀请人）")
    private Boolean isInvitee;

    @Schema(description = "是否已注册（nickname、name 已填写且邀请记录已更新为已注册）")
    private Boolean isRegistered;
}
