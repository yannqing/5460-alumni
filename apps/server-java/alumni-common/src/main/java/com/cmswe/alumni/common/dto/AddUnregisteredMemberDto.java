package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 添加未注册成员到校友会请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUnregisteredMemberDto implements Serializable {

    /**
     * 校友会 ID
     */
    @Schema(description = "校友会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校友会 ID不能为空")
    private Long alumniAssociationId;

    /**
     * 用户名字
     */
    @Schema(description = "用户名字", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户名字不能为空")
    private String username;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 用户的联系电话
     */
    @Schema(description = "用户的联系电话")
    private String userPhone;

    /**
     * 用户的社会职务
     */
    @Schema(description = "用户的社会职务")
    private String userAffiliation;

    @Serial
    private static final long serialVersionUID = 1L;
}
