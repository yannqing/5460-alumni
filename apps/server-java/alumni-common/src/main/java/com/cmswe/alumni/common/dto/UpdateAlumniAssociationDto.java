package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新校友会信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UpdateAlumniAssociationDto", description = "更新校友会信息DTO")
public class UpdateAlumniAssociationDto implements Serializable {

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID")
    @NotNull(message = "校友会ID不能为空")
    private Long alumniAssociationId;

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    private String associationName;

    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校ID")
    private Long schoolId;

    /**
     * 所属校处会ID
     */
    @Schema(description = "所属校处会ID")
    private Long platformId;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息（json 格式）")
    private String contactInfo;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点")
    private String location;

    /**
     * 校友会logo
     */
    @Schema(description = "校友会logo")
    private String logo;

    /**
     * 背景图（json 数组）
     */
    @Schema(description = "背景图（json 数组）")
    private String bgImg;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
