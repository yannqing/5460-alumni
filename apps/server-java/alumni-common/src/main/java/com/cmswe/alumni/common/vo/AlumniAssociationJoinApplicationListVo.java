package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 校友会加入申请列表响应VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "校友会加入申请列表响应")
public class AlumniAssociationJoinApplicationListVo implements Serializable {

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    private String applicationId;

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID")
    private String alumniAssociationId;

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    private String alumniAssociationName;

    /**
     * 申请人类型：1-用户 2-商户
     */
    @Schema(description = "申请人类型：1-用户 2-商户")
    private Integer applicantType;

    /**
     * 申请人ID
     */
    @Schema(description = "申请人ID")
    private String targetId;

    /**
     * 申请人信息
     */
    @Schema(description = "申请人信息")
    private UserListResponse applicantInfo;

    /**
     * 申请理由
     */
    @Schema(description = "申请理由")
    private String applicationReason;

    /**
     * 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销
     */
    @Schema(description = "申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销")
    private Integer applicationStatus;

    /**
     * 申请状态文本
     */
    @Schema(description = "申请状态文本")
    private String applicationStatusText;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private String reviewerId;

    /**
     * 审核人信息
     */
    @Schema(description = "审核人信息")
    private UserListResponse reviewerInfo;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String reviewComment;

    /**
     * 申请时间
     */
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /**
     * 附件文件列表
     */
    @Schema(description = "附件文件列表")
    private List<FilesVo> attachmentFiles;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 获取申请状态文本
     */
    public static String getApplicationStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已撤销";
            default -> "未知";
        };
    }
}
