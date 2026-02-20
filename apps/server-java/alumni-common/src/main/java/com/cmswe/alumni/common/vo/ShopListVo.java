package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 店铺列表VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ShopListVo", description = "店铺列表信息返回VO")
public class ShopListVo implements Serializable {

    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    private String shopId;

    /**
     * 所属商户ID
     */
    @Schema(description = "所属商户ID")
    private String merchantId;

    /**
     * 店铺名称
     */
    @Schema(description = "店铺名称")
    private String shopName;

    /**
     * 店铺类型：1-总店 2-分店
     */
    @Schema(description = "店铺类型：1-总店 2-分店")
    private Integer shopType;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 区县
     */
    @Schema(description = "区县")
    private String district;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @Schema(description = "经度")
    private BigDecimal longitude;

    /**
     * 店铺电话
     */
    @Schema(description = "店铺电话")
    private String phone;

    /**
     * 营业时间
     */
    @Schema(description = "营业时间")
    private String businessHours;

    /**
     * 店铺图片URL数组（第一张图片作为封面）
     */
    @Schema(description = "店铺图片URL数组")
    private String shopImages;

    /**
     * 状态：0-停业 1-营业中 2-装修中
     */
    @Schema(description = "状态：0-停业 1-营业中 2-装修中")
    private Integer status;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    /**
     * 店铺评分（0-5分）
     */
    @Schema(description = "店铺评分（0-5分）")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @Schema(description = "评价数量")
    private Integer ratingCount;

    @Serial
    private static final long serialVersionUID = 1L;

    public static ShopListVo objToVo(Shop shop) {
        if (shop == null) {
            return null;
        }
        ShopListVo vo = new ShopListVo();
        BeanUtils.copyProperties(shop, vo);

        // 将 Long 转换为 String，避免前端精度丢失
        vo.setShopId(String.valueOf(shop.getShopId()));
        vo.setMerchantId(String.valueOf(shop.getMerchantId()));

        return vo;
    }
}
