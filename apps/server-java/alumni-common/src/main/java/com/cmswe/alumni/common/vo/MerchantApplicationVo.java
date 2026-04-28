package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户申请记录精简 VO
 */
@Data
@Schema(name = "MerchantApplicationVo", description = "商户创建申请表精简返回 VO")
public class MerchantApplicationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID（雪花ID）")
    private String applicationId;

    @Schema(description = "申请者wxid（关联用户ID）")
    private String userId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "法人姓名")
    private String legalPerson;

    @Schema(description = "法人电话号")
    private String phone;

    @Schema(description = "统一社会信用代码")
    private String unifiedSocialCreditCode;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销 4-待发布")
    private Integer reviewStatus;

    @Schema(description = "审核原因")
    private String reviewReason;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer isDelete;
}
