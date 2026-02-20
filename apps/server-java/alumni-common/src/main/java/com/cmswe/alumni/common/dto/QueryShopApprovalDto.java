package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 店铺审批记录查询 DTO
 *
 * @author CNI Alumni System
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "店铺审批记录查询请求参数")
public class QueryShopApprovalDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺名称", example = "总店")
    private String shopName;

    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败", example = "0")
    private Integer reviewStatus;

    @Schema(description = "商户ID", example = "123456789")
    private Long merchantId;

    @Schema(description = "店铺类型：1-总店 2-分店", example = "1")
    private Integer shopType;
}
