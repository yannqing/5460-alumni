package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Merchant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户审批记录返回 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "MerchantApprovalVo", description = "商户审批记录查询返回 VO")
public class MerchantApprovalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商户ID")
    private String merchantId;

    @Schema(description = "关联用户ID")
    private String userId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户类型：1-校友商铺 2-普通商铺")
    private Integer merchantType;

    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    @Schema(description = "审核原因")
    private String reviewReason;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "法人姓名")
    private String legalPerson;

    /**
     * 对象转VO
     *
     * @param merchant 商户实体
     * @return MerchantApprovalVo
     */
    public static MerchantApprovalVo objToVo(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        MerchantApprovalVo vo = new MerchantApprovalVo();
        BeanUtils.copyProperties(merchant, vo);
        // ID 转换为 String 避免精度丢失
        vo.setMerchantId(String.valueOf(merchant.getMerchantId()));
        if (merchant.getUserId() != null) {
            vo.setUserId(String.valueOf(merchant.getUserId()));
        }
        if (merchant.getReviewerId() != null) {
            vo.setReviewerId(String.valueOf(merchant.getReviewerId()));
        }
        return vo;
    }
}
