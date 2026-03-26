package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 「我的申请」聚合列表项（摘要）
 */
@Data
@Schema(description = "我的申请记录列表项")
public class MyApplicationRecordListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务类型，见 MyApplicationRecordType")
    private String recordType;

    @Schema(description = "该业务主键 ID（字符串，避免 Long 精度问题）")
    private String recordId;

    @Schema(description = "列表主标题")
    private String title;

    @Schema(description = "副标题/补充说明")
    private String subtitle;

    @Schema(description = "校友会 logo：创建申请取 alumni_association_application.logo；其余取 alumni_association.logo")
    private String associationLogo;

    @Schema(description = "校促会 logo/头像，仅「校友会申请加入校促会」有值，对应 local_platform.avatar")
    private String platformLogo;

    @Schema(description = "校友会 ID（有则返回）")
    private String alumniAssociationId;

    @Schema(description = "校促会 ID（加入校促会申请时有值）")
    private String platformId;

    @Schema(description = "各业务原始状态码")
    private Integer applicationStatus;

    @Schema(description = "状态文案")
    private String applicationStatusText;

    @Schema(description = "归一化状态：PENDING / APPROVED / REJECTED / CANCELLED / UNKNOWN")
    private String statusGroup;

    @Schema(description = "申请/提交时间")
    private LocalDateTime applyTime;

    @Schema(description = "是否允许编辑（当前实现：仅加入校友会待审核为 true）")
    private Boolean canEdit;

    @Schema(description = "是否允许撤销（当前实现：仅加入校友会待审核为 true）")
    private Boolean canCancel;
}
