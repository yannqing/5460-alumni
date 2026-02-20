package com.cmswe.alumni.common.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "查询角色列表的返回内容")
public class RoleListVo {
    /**
     * 角色ID（雪花算法）
     */
    @Schema(description = "角色 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private String roleId;

    /**
     * 角色名
     */
    @Schema(description = "角色名")
    private String roleName;

    /**
     * 角色唯一代码
     */
    @Schema(description = "角色唯一代码")
    private String roleCode;

    /**
     * 角色含义
     */
    @Schema(description = "角色含义")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 用户所属组织信息
     */
    @Schema(description = "用户所属组织信息")
    private UserOrganizationVo organization;

    /**
     * 角色对应的权限列表（树形结构，包含父级菜单权限）
     */
    @Schema(description = "角色权限列表（树形结构）")
    private java.util.List<PermissionsVo> permissions;

    public static RoleListVo objToVo(Role role) {
        if (role == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        RoleListVo roleListVo = new RoleListVo();
        BeanUtils.copyProperties(role, roleListVo);
        return roleListVo;
    }
}
