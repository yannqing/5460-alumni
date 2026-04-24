package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商户入驻申请 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商户入驻申请请求DTO")
public class ApplyMerchantDto implements Serializable {

    /**
     * 商户名称
     */
    @NotBlank(message = "商户名称不能为空")
    @Size(max = 100, message = "商户名称长度不能超过100个字符")
    @Schema(description = "商户名称", example = "张三餐饮店", requiredMode = Schema.RequiredMode.REQUIRED)
    private String merchantName;

    /**
     * 商户类型：1-校友商铺 2-普通商铺
     */
    @Schema(description = "商户类型：1-校友商铺 2-普通商铺", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer merchantType;

    /**
     * 营业执照URL
     */
    @Schema(description = "营业执照URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String businessLicense;

    /**
     * 统一社会信用代码
     */
    @NotBlank(message = "统一社会信用代码不能为空")
    @Size(max = 18, message = "统一社会信用代码长度不能超过18个字符")
    @Schema(description = "统一社会信用代码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unifiedSocialCreditCode;

    /**
     * 法人姓名
     */
    @NotBlank(message = "法人姓名不能为空")
    @Size(max = 50, message = "法人姓名长度不能超过50个字符")
    @Schema(description = "法人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String legalPerson;

    /**
     * 法人身份证号
     */
    @Schema(description = "法人身份证号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String legalPersonId;

    /**
     * 法人个人联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "法人个人联系电话", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    /**
     * 联系邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "联系邮箱", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String contactEmail;

    /**
     * 经营范围
     */
    @Schema(description = "经营范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String businessScope;

    /**
     * 经营类目（餐饮/酒店/零售/服务等）
     */
    @Schema(description = "经营类目", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String businessCategory;

    /**
     * 一级经营类目ID
     */
    @Schema(description = "一级经营类目ID（level=1）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long businessCategoryId;

    /**
     * 二级服务ID
     */
    @Schema(description = "二级服务ID（level=2，且 parentId=businessCategoryId）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long businessServiceId;

    /**
     * 商家logo
     */
    @Size(max = 1024, message = "商家logo长度不能超过1024个字符")
    @Schema(description = "商家logo", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String logo;

    /**
     * 商家背景图（JSON字符串）
     */
    @Schema(description = "商家背景图（JSON字符串）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String backgroundImage;

    /**
     * 关联校友会ID（校友商铺时必填）
     */
    @Schema(description = "关联校友会ID（校友商铺时必填）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long alumniAssociationId;

    @Serial
    private static final long serialVersionUID = 1L;
}
