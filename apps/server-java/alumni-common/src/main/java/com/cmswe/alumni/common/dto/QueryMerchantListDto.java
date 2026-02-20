package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商户列表查询DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商户列表查询请求DTO")
public class QueryMerchantListDto extends PageRequest implements Serializable {

    /**
     * 商户名称
     */
    @Schema(description = "商户名称", example = "张三的店铺", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "商户名称长度不能超过200个字符")
    private String merchantName;

    /**
     * 商户类型：1-校友商铺 2-普通商铺
     */
    @Schema(description = "商户类型：1-校友商铺 2-普通商铺", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer merchantType;

    /**
     * 会员等级：1-基础版 2-标准版 3-专业版 4-旗舰版
     */
    @Schema(description = "会员等级：1-基础版 2-标准版 3-专业版 4-旗舰版", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer memberTier;

    /**
     * 经营类目（餐饮/酒店/零售/服务等）
     */
    @Schema(description = "经营类目", example = "餐饮", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String businessCategory;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "13800138000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String contactPhone;

    /**
     * 法人姓名
     */
    @Schema(description = "法人姓名", example = "张三", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String legalPerson;

    /**
     * 是否校友认证：0-否 1-是
     */
    @Schema(description = "是否校友认证：0-否 1-是", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer isAlumniCertified;

    /**
     * 关联校友会ID
     */
    @Schema(description = "关联校友会ID", example = "1234567890", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long alumniAssociationId;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段: 根据评分排序（ratingScore）；根据创建时间排序（createTime）；根据门店数量排序（shopCount）")
    private String sortField;

    @Serial
    private static final long serialVersionUID = 1L;
}
