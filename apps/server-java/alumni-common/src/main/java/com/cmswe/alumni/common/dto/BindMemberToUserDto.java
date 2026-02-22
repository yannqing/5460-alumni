package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 绑定校友会组织架构成员与系统用户请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "绑定校友会组织架构成员与系统用户请求")
public class BindMemberToUserDto implements Serializable {

    @Schema(description = "校友会成员表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotNull(message = "成员表ID不能为空")
    private Long memberId;

    @Schema(description = "用户微信ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotNull(message = "用户微信ID不能为空")
    private Long wxId;

    @Serial
    private static final long serialVersionUID = 1L;
}
