package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商户基本信息更新 DTO（部分字段可选，非 null 字段才会更新）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商户基本信息更新请求（仅传需要修改的字段）")
public class UpdateMerchantDto implements Serializable {

    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    @Size(max = 100, message = "商户名称长度不能超过100个字符")
    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户类型：1-校友商铺 2-普通商铺")
    private Integer merchantType;

    @Schema(description = "营业执照URL")
    private String businessLicense;

    @Size(max = 18, message = "统一社会信用代码长度不能超过18个字符")
    @Schema(description = "统一社会信用代码")
    private String unifiedSocialCreditCode;

    @Size(max = 50, message = "法人姓名长度不能超过50个字符")
    @Schema(description = "法人姓名")
    private String legalPerson;

    @Size(max = 18, message = "法人身份证号长度不能超过18个字符")
    @Schema(description = "法人身份证号")
    private String legalPersonId;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "联系电话")
    private String contactPhone;

    @Size(max = 20, message = "法人个人联系电话长度不能超过20个字符")
    @Schema(description = "法人个人联系电话")
    private String phone;

    @Schema(description = "联系邮箱（非空时须为合法邮箱格式，由服务端校验）")
    private String contactEmail;

    @Schema(description = "经营范围")
    private String businessScope;

    @Schema(description = "经营类目（餐饮/酒店/零售/服务等）")
    private String businessCategory;

    @Size(max = 1024, message = "商家logo长度不能超过1024个字符")
    @Schema(description = "商家logo URL")
    private String logo;

    @Schema(description = "商家背景图（JSON字符串）")
    private String backgroundImage;

    @Schema(description = "商户详情图片（JSON数组）")
    private String detailImages;

    @Schema(description = "关联校友会ID（校友商铺必填）")
    private Long alumniAssociationId;

    @Serial
    private static final long serialVersionUID = 1L;
}
