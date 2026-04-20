package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户加入校友会申请记录返回 VO
 */
@Data
@Schema(name = "MerchantAssociationJoinApplyVo", description = "商户加入校友会申请记录返回 VO")
public class MerchantAssociationJoinApplyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID")
    private String id;

    @Schema(description = "商户ID")
    private String merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户logo")
    private String logo;

    @Schema(description = "商户类型：1-校友商铺 2-普通商铺")
    private Integer merchantType;

    @Schema(description = "营业执照图片 URL")
    private String businessLicense;

    @Schema(description = "统一社会信用代码")
    private String unifiedSocialCreditCode;

    @Schema(description = "法人姓名")
    private String legalPerson;

    @Schema(description = "商户经营范围")
    private String businessScope;

    @Schema(description = "商户背景图")
    private String backgroundImage;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请人电话")
    private String applicantPhone;

    @Schema(description = "审核状态（0-待审核, 1-已通过, 2-已拒绝, 3-已撤销）")
    private Integer status;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "关联校友会信息（申请加入的）")
    private AlumniAssociationListVo alumniAssociation;

    @Schema(description = "已加入的校友会列表")
    private java.util.List<AlumniAssociationListVo> joinedAssociations;
}
