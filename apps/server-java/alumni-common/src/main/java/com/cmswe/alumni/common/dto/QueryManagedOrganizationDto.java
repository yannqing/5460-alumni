package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "QueryManagedOrganizationDto", description = "查询可管理组织列表请求")
public class QueryManagedOrganizationDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 组织类型（0-校友会 1-校促会 2-商户）
     */
    @Schema(description = "组织类型", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer type;

    /**
     * 是否只返回角色绑定范围内的组织
     */
    @Schema(description = "是否只返回角色绑定范围内的组织", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean roleScopedOnly = false;

    /**
     * 名称搜索关键字
     */
    @Schema(description = "名称搜索关键字", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;
}
