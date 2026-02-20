package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 根据校处会ID查询校友会列表DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "根据校处会ID查询校友会列表请求DTO")
public class QueryAlumniAssociationByPlatformDto extends PageRequest implements Serializable {

    /**
     * 校处会ID
     */
    @NotNull(message = "校处会ID不能为空")
    @Schema(description = "校处会ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long platformId;

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称", example = "清华大学北京校友会", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "校友会名称长度不能超过200个字符")
    private String associationName;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点", example = "北京市朝阳区", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String location;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段: 根据会员数量排序（memberCount）；根据时间排序（createTime）")
    private String sortField;

    @Serial
    private static final long serialVersionUID = 1L;
}
