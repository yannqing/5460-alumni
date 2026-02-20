package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 领取优惠券请求DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ClaimCouponDto", description = "领取优惠券请求DTO")
public class ClaimCouponDto implements Serializable {

    /**
     * 优惠券ID
     */
    @NotNull(message = "优惠券ID不能为空")
    @Schema(description = "优惠券ID", required = true)
    private Long couponId;

    /**
     * 领取渠道（APP/小程序/H5/活动页）
     */
    @Schema(description = "领取渠道")
    private String receiveChannel;

    /**
     * 领取来源（首页/店铺详情/活动）
     */
    @Schema(description = "领取来源")
    private String receiveSource;

    @Serial
    private static final long serialVersionUID = 1L;
}
