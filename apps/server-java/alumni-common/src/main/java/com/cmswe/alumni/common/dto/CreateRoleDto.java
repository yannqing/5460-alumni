package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建角色请求参数")
public class CreateRoleDto implements Serializable {
    /**
     * 角色名
     */
    @Schema(description = "角色名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String roleName;

    /**
     * 角色唯一代码
     */
    @Schema(description = "角色唯一代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String roleCode;

    /**
     * 角色含义
     */
    @Schema(description = "角色含义", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;

    @Serial
    private static final long serialVersionUID = 1L;

    public static Role dtoToObj(CreateRoleDto createRoleDto) {
        if (createRoleDto == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        Role role = new Role();
        BeanUtils.copyProperties(createRoleDto, role);
        return role;
    }
}
