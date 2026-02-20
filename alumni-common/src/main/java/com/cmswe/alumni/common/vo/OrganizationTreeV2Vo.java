package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 组织架构树VO V2版本（基于username）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "OrganizationTreeV2Vo", description = "组织架构树响应类型V2（基于username）")
public class OrganizationTreeV2Vo implements Serializable {

    /**
     * 架构角色id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "架构角色id")
    private Long roleOrId;

    /**
     * 父id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "父id")
    private Long pid;

    /**
     * 角色名
     */
    @Schema(description = "角色名")
    private String roleOrName;

    /**
     * 角色唯一代码
     */
    @Schema(description = "角色唯一代码")
    private String roleOrCode;

    /**
     * 角色含义
     */
    @Schema(description = "角色含义")
    private String remark;

    /**
     * 成员列表
     */
    @Schema(description = "该角色下的成员列表")
    private List<OrganizationMemberV2Vo> members;

    /**
     * 子节点列表
     */
    @Schema(description = "子节点列表")
    private List<OrganizationTreeV2Vo> children;

    @Serial
    private static final long serialVersionUID = 1L;
}
