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
 * 商户审批记录查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商户审批记录查询请求参数")
public class QueryMerchantApprovalDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商户名称", example = "校友咖啡")
    private String merchantName;

    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败", example = "0")
    private Integer reviewStatus;

    @Schema(description = "商户类型：1-校友商铺 2-普通商铺", example = "1")
    private Integer merchantType;
}
