package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改商户申请记录 DTO
 */
@Data
@Schema(description = "修改商户申请记录请求DTO")
public class UpdateMerchantApplicationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long applicationId;

    /**
     * 商户名称
     */
    @NotBlank(message = "商户名称不能为空")
    @Size(max = 255, message = "商户名称长度不能超过255个字符")
    @Schema(description = "商户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String merchantName;

    /**
     * 法人姓名
     */
    @Size(max = 100, message = "法人姓名长度不能超过100个字符")
    @Schema(description = "法人姓名")
    private String legalPerson;

    /**
     * 法人电话号
     */
    @Size(max = 20, message = "法人电话号长度不能超过20个字符")
    @Schema(description = "法人电话号")
    private String phone;

    /**
     * 统一社会信用代码
     */
    @Size(max = 50, message = "统一社会信用代码长度不能超过50个字符")
    @Schema(description = "统一社会信用代码")
    private String unifiedSocialCreditCode;

    /**
     * 所在城市
     */
    @Size(max = 100, message = "所在城市长度不能超过100个字符")
    @Schema(description = "所在城市")
    private String city;
}
