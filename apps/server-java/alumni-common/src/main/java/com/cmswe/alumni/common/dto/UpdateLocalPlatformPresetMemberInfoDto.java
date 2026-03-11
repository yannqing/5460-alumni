package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新校促会预设成员信息请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLocalPlatformPresetMemberInfoDto implements Serializable {

    /**
     * 成员 ID
     */
    @Schema(description = "成员 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员 ID不能为空")
    private Long memberId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String username;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String roleName;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String contactInformation;

    /**
     * 社会职务
     */
    @Schema(description = "社会职务", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String socialDuties;

    /**
     * 是否在主页展示
     */
    @Schema(description = "是否在主页展示(0否,1是)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer isShow;

    /**
     * 排序权重, 数值越小越靠前
     */
    @Schema(description = "排序权重, 数值越小越靠前", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer sort;

    @Serial
    private static final long serialVersionUID = 1L;
}