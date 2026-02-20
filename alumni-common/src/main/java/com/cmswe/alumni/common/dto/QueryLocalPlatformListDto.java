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
 * 校处会列表查询DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "校处会列表查询请求DTO")
public class QueryLocalPlatformListDto extends PageRequest implements Serializable {

    /**
     * 校处会名称
     */
    @Schema(description = "校处会名称", example = "北京校处会", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "校处会名称长度不能超过200个字符")
    private String platformName;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市", example = "北京", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String city;

    /**
     * 管辖范围
     */
    @Schema(description = "管辖范围", example = "北京市朝阳区", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String scope;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段: 根据创建时间排序（createTime）")
    private String sortField;

    @Serial
    private static final long serialVersionUID = 1L;
}
