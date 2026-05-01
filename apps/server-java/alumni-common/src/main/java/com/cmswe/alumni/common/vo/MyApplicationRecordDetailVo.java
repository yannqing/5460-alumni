package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 「我的申请」聚合详情返回
 */
@Data
@Schema(description = "我的申请记录详情")
public class MyApplicationRecordDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务类型，见 MyApplicationRecordType")
    private String recordType;

    @Schema(description = "该业务主键 ID（字符串，避免 Long 精度问题）")
    private String recordId;

    @Schema(description = "各业务原始状态码")
    private Integer applicationStatus;

    @Schema(description = "状态文案")
    private String applicationStatusText;

    @Schema(description = "归一化状态：PENDING / APPROVED / REJECTED / CANCELLED / UNKNOWN")
    private String statusGroup;

    @Schema(description = "是否允许编辑")
    private Boolean canEdit;

    @Schema(description = "是否允许撤销")
    private Boolean canCancel;

    @Schema(description = "是否允许发布（校友会创建申请审核通过后可以完善信息并发布）")
    private Boolean canPublish;

    @Schema(description = "业务详情对象（按 recordType 返回对应详情结构）")
    private Object detail;

    /**
     * 商户入驻 / 门店申请：结构化图片列表（便于前端展示；与 detail 中字段同源）
     */
    @Schema(description = "申请资料图片（仅商户入驻、门店申请；含 Logo、营业执照、背景图、门店图等）")
    private List<MaterialImageItemVo> materialImages;
}

