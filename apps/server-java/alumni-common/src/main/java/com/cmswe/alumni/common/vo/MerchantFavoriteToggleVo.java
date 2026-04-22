package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商户收藏切换结果
 */
@Data
@Schema(description = "商户收藏切换结果")
public class MerchantFavoriteToggleVo {

    @Schema(description = "收藏记录ID（字符串形式雪花ID）")
    private String favoriteId;

    @Schema(description = "用户ID（字符串形式雪花ID）")
    private String wxId;

    @Schema(description = "商户ID（字符串形式雪花ID）")
    private String merchantId;

    @Schema(description = "目标类型：1-商户")
    private Integer targetType;

    @Schema(description = "当前是否已收藏：true-已收藏，false-已取消")
    private Boolean favorited;

    @Schema(description = "本次动作：favorite-收藏，unfavorite-取消收藏")
    private String action;
}
