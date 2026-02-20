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
import java.util.List;

/**
 * 商户详情响应 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "MerchantDetailVo", description = "商户详情响应VO")
public class MerchantDetailVo implements Serializable {

    /**
     * 商户ID
     */
    @Schema(description = "商户ID")
    private String merchantId;

    /**
     * 关联用户ID
     */
    @Schema(description = "关联用户ID")
    private String userId;

    /**
     * 商户名称
     */
    @Schema(description = "商户名称")
    private String merchantName;

    /**
     * 商户类型：1-校友商铺 2-普通商铺
     */
    @Schema(description = "商户类型")
    private Integer merchantType;

    /**
     * 营业执照URL
     */
    @Schema(description = "营业执照URL")
    private String businessLicense;

    /**
     * 统一社会信用代码
     */
    @Schema(description = "统一社会信用代码")
    private String unifiedSocialCreditCode;

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
     * 经营类目
     */
    @Schema(description = "经营类目")
    private String businessCategory;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @Schema(description = "审核状态")
    private Integer reviewStatus;

    /**
     * 审核原因（驳回时填写）
     */
    @Schema(description = "审核原因")
    private String reviewReason;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    /**
     * 会员等级：1-基础版 2-标准版 3-专业版 4-旗舰版
     */
    @Schema(description = "会员等级")
    private Integer memberTier;

    /**
     * 会员等级到期时间
     */
    @Schema(description = "会员等级到期时间")
    private LocalDateTime tierExpireTime;

    /**
     * 状态：0-禁用 1-启用 2-已注销
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 门店数量
     */
    @Schema(description = "门店数量")
    private Integer shopCount;

    /**
     * 门店列表
     */
    @Schema(description = "门店列表")
    private List<ShopListVo> shops;

    /**
     * 商户评分（0-5分）
     */
    @Schema(description = "商户评分")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @Schema(description = "评价数量")
    private Integer ratingCount;

    /**
     * 是否校友认证：0-否 1-是
     */
    @Schema(description = "是否校友认证")
    private Integer isAlumniCertified;

    /**
     * 关联校友会信息
     */
    @Schema(description = "关联校友会信息")
    private AlumniAssociationListVo alumniAssociation;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;

    public static MerchantDetailVo objToVo(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        MerchantDetailVo vo = new MerchantDetailVo();
        BeanUtils.copyProperties(merchant, vo);

        // 将 Long 转换为 String，避免前端精度丢失
        vo.setMerchantId(String.valueOf(merchant.getMerchantId()));
        vo.setUserId(String.valueOf(merchant.getUserId()));

        // 注意：alumniAssociation 需要在 Service 层单独设置

        return vo;
    }
}
