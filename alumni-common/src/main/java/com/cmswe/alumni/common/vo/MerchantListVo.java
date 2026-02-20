package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Merchant;
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
 * 商户列表返回VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "MerchantListVo", description = "商户列表信息返回VO")
public class MerchantListVo implements Serializable {

    /**
     * 商户ID
     */
    @Schema(description = "商户ID")
    private String merchantId;

    /**
     * 关联用户ID
     */
    @Schema(description = "关联用户ID（商户所有者的 wx_id）")
    private String userId;

    /**
     * 商户名称
     */
    @Schema(description = "商户名称")
    private String merchantName;

    /**
     * 商户类型：1-校友商铺 2-普通商铺
     */
    @Schema(description = "商户类型：1-校友商铺 2-普通商铺")
    private Integer merchantType;

    /**
     * 营业执照URL
     */
    @Schema(description = "营业执照URL")
    private String businessLicense;

    /**
     * 法人姓名
     */
    @Schema(description = "法人姓名")
    private String legalPerson;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String contactEmail;

    /**
     * 经营范围
     */
    @Schema(description = "经营范围")
    private String businessScope;

    /**
     * 经营类目（餐饮/酒店/零售/服务等）
     */
    @Schema(description = "经营类目")
    private String businessCategory;

    /**
     * 会员等级：1-基础版 2-标准版 3-专业版 4-旗舰版
     */
    @Schema(description = "会员等级：1-基础版 2-标准版 3-专业版 4-旗舰版")
    private Integer memberTier;

    /**
     * 会员等级到期时间
     */
    @Schema(description = "会员等级到期时间")
    private LocalDateTime tierExpireTime;

    /**
     * 累计缴费金额
     */
    @Schema(description = "累计缴费金额")
    private BigDecimal totalPaidAmount;

    /**
     * 门店数量
     */
    @Schema(description = "门店数量")
    private Integer shopCount;

    /**
     * 累计发放优惠券数量
     */
    @Schema(description = "累计发放优惠券数量")
    private Long totalCouponIssued;

    /**
     * 累计核销优惠券数量
     */
    @Schema(description = "累计核销优惠券数量")
    private Long totalCouponVerified;

    /**
     * 商户评分（0-5分）
     */
    @Schema(description = "商户评分（0-5分）")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @Schema(description = "评价数量")
    private Integer ratingCount;

    /**
     * 是否校友认证：0-否 1-是
     */
    @Schema(description = "是否校友认证：0-否 1-是")
    private Integer isAlumniCertified;

    /**
     * 关联校友会ID
     */
    @Schema(description = "关联校友会ID")
    private String alumniAssociationId;

    /**
     * 认证通过时间
     */
    @Schema(description = "认证通过时间")
    private LocalDateTime certifiedTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转VO
     *
     * @param merchant 商户实体
     * @return MerchantListVo
     */
    public static MerchantListVo objToVo(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        MerchantListVo merchantListVo = new MerchantListVo();
        BeanUtils.copyProperties(merchant, merchantListVo);
        return merchantListVo;
    }
}
