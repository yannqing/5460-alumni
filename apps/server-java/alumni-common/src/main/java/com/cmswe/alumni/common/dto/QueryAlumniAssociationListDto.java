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
 * 校友会列表查询DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "校友会列表查询请求DTO")
public class QueryAlumniAssociationListDto extends PageRequest implements Serializable {

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称", example = "清华大学北京校友会",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "校友会名称长度不能超过200个字符")
    private String associationName;

    /**
     * 校友会会长名称
     */
    @Schema(description = "校友会会长名称", example = "张三", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String presidentUsername;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息", example = "JSON格式", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String contactInfo;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点", example = "北京市朝阳区xxx", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String location;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段: 根据校友会数量排序（memberCount）；根据时间排序（createTime）")
    private String sortField;

    @Serial
    private static final long serialVersionUID = 1L;

}