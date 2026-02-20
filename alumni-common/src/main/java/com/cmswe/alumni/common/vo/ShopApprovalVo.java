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
import java.time.LocalDateTime;

/**
 * 店铺审批记录返回 VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ShopApprovalVo", description = "店铺审批记录查询返回 VO")
public class ShopApprovalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺ID")
    private String shopId;

    @Schema(description = "所属商户ID")
    private String merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺类型：1-总店 2-分店")
    private Integer shopType;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "店铺电话")
    private String phone;

    @Schema(description = "营业时间")
    private String businessHours;

    @Schema(description = "店铺图片URL数组")
    private String shopImages;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "状态：0-停业 1-营业中 2-装修中")
    private Integer status;

    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    @Schema(description = "审核原因")
    private String reviewReason;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "校友会推荐：0-否 1-是")
    private Integer isRecommended;

    @Schema(description = "创建人ID")
    private String createdBy;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 对象转VO
     *
     * @param shop 店铺实体
     * @return ShopApprovalVo
     */
    public static ShopApprovalVo objToVo(Shop shop) {
        if (shop == null) {
            return null;
        }
        ShopApprovalVo vo = new ShopApprovalVo();
        BeanUtils.copyProperties(shop, vo);
        // ID 转换为 String 避免精度丢失
        vo.setShopId(String.valueOf(shop.getShopId()));
        vo.setMerchantId(String.valueOf(shop.getMerchantId()));
        if (shop.getCreatedBy() != null) {
            vo.setCreatedBy(String.valueOf(shop.getCreatedBy()));
        }
        if (shop.getReviewerId() != null) {
            vo.setReviewerId(String.valueOf(shop.getReviewerId()));
        }
        return vo;
    }
}
