package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限VO
 *
 * @author CMSWE
 * @since 2025-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限信息")
public class PermissionsVo {

    /**
     * 权限ID
     */
    @Schema(description = "权限ID")
    private Long perId;

    /**
     * 权限UUID
     */
    @Schema(description = "权限UUID")
    private String perUuid;

    /**
     * 该权限的父ID
     */
    @Schema(description = "父权限ID，0表示顶级菜单")
    private Long pid;

    /**
     * 名称
     */
    @Schema(description = "权限名称")
    private String name;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码")
    private String code;

    /**
     * 类型：0代表菜单，1权限
     */
    @Schema(description = "类型：0-菜单，1-权限")
    private Integer type;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 排序字段
     */
    @Schema(description = "排序顺序")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 子权限列表
     */
    @Schema(description = "子权限列表")
    private List<PermissionsVo> children;

    /**
     * 实体转VO
     *
     * @param permission 权限实体
     * @return PermissionsVo
     */
    public static PermissionsVo objToVo(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionsVo vo = new PermissionsVo();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }
}
