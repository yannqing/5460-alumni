package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券核销请求DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "VerifyCouponDto", description = "优惠券核销请求DTO")
public class VerifyCouponDto implements Serializable {

    /**
     * 核销码
     */
    @NotBlank(message = "核销码不能为空")
    @Schema(description = "核销码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;

    /**
     * 订单金额（可选）
     */
    @Schema(description = "订单金额（可选）")
    private BigDecimal orderAmount;

    /**
     * 核销店铺ID
     */
    @NotNull(message = "核销店铺ID不能为空")
    @Schema(description = "核销店铺ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long shopId;

    /**
     * 核销方式：1-扫码 2-输入卡号
     */
    @Schema(description = "核销方式：1-扫码 2-输入卡号")
    private Integer verificationMethod;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息")
    private String deviceInfo;

    @Serial
    private static final long serialVersionUID = 1L;
}
