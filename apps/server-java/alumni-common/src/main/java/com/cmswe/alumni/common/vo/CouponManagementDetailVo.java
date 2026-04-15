package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端优惠券详情返回 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CouponManagementDetailVo", description = "管理端优惠券详情（包含可使用店铺）")
public class CouponManagementDetailVo implements Serializable {

    @Schema(description = "优惠券详情")
    private CouponVo coupon;

    @Schema(description = "可使用店铺列表")
    private List<ShopListVo> availableShops;

    @Serial
    private static final long serialVersionUID = 1L;
}
