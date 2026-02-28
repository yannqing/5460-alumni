package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询管理的组织列表请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QueryManagedOrganizationsDto", description = "查询管理的组织列表请求参数")
public class QueryManagedOrganizationsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "组织类型不能为空")
    @Schema(description = "组织类型（0-校友会，1-校促会）", example = "0", required = true)
    private Integer organizationType;
}
