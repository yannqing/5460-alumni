package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邀请记录列表项 VO（我的邀请列表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邀请记录列表项")
public class InvitationRecordItemVo {

    @Schema(description = "邀请记录ID")
    private Long id;

    @Schema(description = "被邀请人wxid")
    private Long inviteeWxId;

    @Schema(description = "被邀请人昵称")
    private String inviteeNickname;

    @Schema(description = "被邀请人姓名")
    private String inviteeName;

    @Schema(description = "是否认证(0未认证,1已认证)")
    private Integer isVerified;

    @Schema(description = "是否注册(0未注册,1已注册)")
    private Integer isRegister;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
