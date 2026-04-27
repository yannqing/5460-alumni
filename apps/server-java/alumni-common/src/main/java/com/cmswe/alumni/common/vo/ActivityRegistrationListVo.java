package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动报名列表VO（管理员视角，含完整联系方式，不脱敏）
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ActivityRegistrationListVo", description = "活动报名列表信息返回VO")
public class ActivityRegistrationListVo implements Serializable {

    /**
     * 报名记录ID
     */
    @Schema(description = "报名记录ID")
    private String registrationId;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private String activityId;

    /**
     * 报名用户ID（wxId）
     */
    @Schema(description = "报名用户ID")
    private String userId;

    /**
     * 报名时填写的姓名（真名）
     */
    @Schema(description = "报名时填写的姓名")
    private String userName;

    /**
     * 报名时填写的联系电话
     */
    @Schema(description = "报名时填写的联系电话")
    private String userPhone;

    /**
     * 用户头像（来自 wx_user_info）
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户昵称（来自 wx_user_info）
     */
    @Schema(description = "用户昵称")
    private String userNickname;

    /**
     * 报名时间
     */
    @Schema(description = "报名时间")
    private LocalDateTime registrationTime;

    /**
     * 报名状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消
     */
    @Schema(description = "报名状态：0-待审核 1-审核通过 2-审核拒绝 3-已取消")
    private Integer registrationStatus;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    /**
     * 审核理由
     */
    @Schema(description = "审核理由")
    private String auditReason;

    /**
     * 用户备注
     */
    @Schema(description = "用户备注")
    private String remark;

    @Serial
    private static final long serialVersionUID = 1L;
}
