package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的收藏-商户列表项
 */
@Data
@Schema(description = "我的收藏-商户列表项")
public class MerchantFavoriteItemVo {

    @Schema(description = "收藏记录ID（字符串雪花ID）")
    private String favoriteId;

    @Schema(description = "目标类型：1-商户")
    private Integer targetType;

    @Schema(description = "商户ID（字符串雪花ID）")
    private String merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户logo")
    private String logo;

    @Schema(description = "商户类型：1-校友商铺 2-普通商铺")
    private Integer merchantType;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "经营类目")
    private String businessCategory;

    @Schema(description = "商户状态：0-禁用 1-启用 2-已注销")
    private Integer status;

    @Schema(description = "收藏更新时间")
    private LocalDateTime favoritedTime;
}
