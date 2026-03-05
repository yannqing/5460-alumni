package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 组织架构模板VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "组织架构模板VO")
public class OrganizeArchiTemplateVo implements Serializable {

    /**
     * 模板ID (转为String避免前端精度丢失)
     */
    @Schema(description = "模板ID")
    @JsonProperty("templateId")
    private String templateId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    @JsonProperty("templateName")
    private String templateName;

    /**
     * 模板唯一代码
     */
    @Schema(description = "模板唯一代码")
    @JsonProperty("templateCode")
    private String templateCode;

    /**
     * 适用组织类型（0校友会，1校处会，2商户）
     */
    @Schema(description = "适用组织类型（0校友会，1校处会，2商户）")
    @JsonProperty("organizeType")
    private Integer organizeType;

    /**
     * 模板内容（树形结构）
     */
    @Schema(description = "模板内容（树形结构）")
    @JsonProperty("templateContent")
    private List<TemplateNode> templateContent;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述")
    @JsonProperty("description")
    private String description;

    /**
     * 是否默认模板：0-否，1-是
     */
    @Schema(description = "是否默认模板：0-否，1-是")
    @JsonProperty("isDefault")
    private Integer isDefault;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板节点（树形结构）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "模板节点")
    public static class TemplateNode implements Serializable {

        /**
         * 节点ID (转为String避免前端精度丢失)
         */
        @Schema(description = "节点ID")
        @JsonProperty("nodeId")
        private String nodeId;

        /**
         * 父节点ID (转为String避免前端精度丢失)
         */
        @Schema(description = "父节点ID")
        @JsonProperty("pid")
        private String pid;

        /**
         * 角色名称
         */
        @Schema(description = "角色名称")
        @JsonProperty("roleOrName")
        private String roleOrName;

        /**
         * 角色代码
         */
        @Schema(description = "角色代码")
        @JsonProperty("roleOrCode")
        private String roleOrCode;

        /**
         * 角色含义
         */
        @Schema(description = "角色含义")
        @JsonProperty("remark")
        private String remark;

        /**
         * 子节点列表
         */
        @Schema(description = "子节点列表")
        @JsonProperty("children")
        private List<TemplateNode> children;

        @Serial
        private static final long serialVersionUID = 1L;
    }
}
