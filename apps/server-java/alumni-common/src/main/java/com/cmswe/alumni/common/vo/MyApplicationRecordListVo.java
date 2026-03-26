package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 「我的申请」聚合列表项（摘要，供列表页与跳转详情使用）
 */
@Data
@Schema(description = "我的申请记录列表项")
public class MyApplicationRecordListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务类型，见 MyApplicationRecordType 常量")
    private String recordType;

    @Schema(description = "该业务下的主键 ID（字符串，避免 Long 精度问题）")
    private String recordId;

    @Schema(description = "列表主标题")
    private String title;

    @Schema(description = "副标题/补充说明（如母校、校促会名称等）")
    private String subtitle;

    @Schema(description = "校友会 ID（有则返回，字符串）")
    private String alumniAssociationId;

    @Schema(description = "校促会 ID（加入校促会申请时有值，字符串）")
    private String platformId;

    @Schema(description = "各业务原始状态码")
    private Integer applicationStatus;

    @Schema(description = "状态文案")
    private String applicationStatusText;

    @Schema(description = "归一化状态分组：PENDING / APPROVED / REJECTED / CANCELLED")
    private String statusGroup;

    @Schema(description = "申请/提交时间（创建校友会、加入校友会用 applyTime；加入校促会用 createTime）")
    private LocalDateTime applyTime;

    @Schema(description = "是否允许编辑（当前仅加入校友会待审核为 true，其余视后端能力）")
    private Boolean canEdit;

    @Schema(description = "是否允许撤销（当前仅加入校友会待审核为 true，其余视后端能力）")
    private Boolean canCancel;
}
