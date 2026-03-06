package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新校友会成员信息请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAlumniAssociationMemberDto implements Serializable {

    /**
     * 成员主键ID
     */
    @Schema(description = "成员主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成员ID不能为空")
    private Long id;

    /**
     * 成员用户名
     */
    @Schema(description = "成员用户名")
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

    /**
     * 是否展示在主页（0-否，1-是）
     */
    @Schema(description = "是否展示在主页（0-否，1-是）")
    private Integer isShowOnHome;

    @Serial
    private static final long serialVersionUID = 1L;
}
