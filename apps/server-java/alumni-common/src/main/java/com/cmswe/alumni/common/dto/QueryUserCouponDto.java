package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询用户优惠券列表请求DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QueryUserCouponDto", description = "查询用户优惠券列表请求DTO")
public class QueryUserCouponDto implements Serializable {

    /**
     * 状态：1-未使用 2-已使用 3-已过期 4-已作废（不传则查询全部）
     */
    @Schema(description = "状态：1-未使用 2-已使用 3-已过期 4-已作废")
    private Integer status;

    /**
     * 优惠券类型：1-折扣券 2-满减券 3-礼品券（不传则查询全部）
     */
    @Schema(description = "优惠券类型：1-折扣券 2-满减券 3-礼品券")
    private Integer couponType;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", defaultValue = "1")
    private int current = 1;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", defaultValue = "10")
    private int pageSize = 10;

    @Serial
    private static final long serialVersionUID = 1L;
}
