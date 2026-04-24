package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询商户加入校友会申请列表请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryMerchantAssociationJoinApplyDto extends PageRequest implements Serializable {

    @Schema(description = "校友会ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long alumniAssociationId;

    @Schema(description = "商户ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long merchantId;

    @Schema(description = "审核状态：0-待审核 1-已通过 2-已拒绝", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
