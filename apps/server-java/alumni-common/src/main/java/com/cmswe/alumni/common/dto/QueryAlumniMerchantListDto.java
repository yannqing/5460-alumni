package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校友商户列表查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "校友商户列表查询请求")
public class QueryAlumniMerchantListDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商户名称（模糊查询）")
    @Size(max = 200, message = "商户名称长度不能超过200个字符")
    private String merchantName;

    @Schema(description = "校友会ID（可选）")
    private Long alumniAssociationId;
}
