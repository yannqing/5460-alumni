package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.OrganizeArchiRole;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 组织架构角色响应VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "组织架构角色响应")
public class OrganizeArchiRoleVo implements Serializable {

    /**
     * 架构角色ID
     */
    @Schema(description = "架构角色ID")
    private String roleOrId;

    /**
     * 父角色ID
     */
    @Schema(description = "父角色ID")
    private String pid;

    /**
     * 组织类型（0-校友会，1-校处会，2-商户）
     */
    @Schema(description = "组织类型（0-校友会，1-校处会，2-商户）")
    private Integer organizeType;

    /**
     * 组织ID
     */
    @Schema(description = "组织ID")
    private String organizeId;

    /**
     * 角色名
     */
    @Schema(description = "角色名")
    private String roleOrName;

    /**
     * 角色唯一代码
     */
    @Schema(description = "角色唯一代码")
    private String roleOrCode;

    /**
     * 角色含义
     */
    @Schema(description = "角色含义")
    private String remark;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 子角色列表
     */
    @Schema(description = "子角色列表")
    private List<OrganizeArchiRoleVo> children;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 实体转VO
     */
    public static OrganizeArchiRoleVo objToVo(OrganizeArchiRole organizeArchiRole) {
        if (organizeArchiRole == null) {
            return null;
        }

        OrganizeArchiRoleVo vo = new OrganizeArchiRoleVo();
        BeanUtils.copyProperties(organizeArchiRole, vo);
        // 处理JS精度问题，Long转String
        vo.setRoleOrId(String.valueOf(organizeArchiRole.getRoleOrId()));
        vo.setOrganizeId(String.valueOf(organizeArchiRole.getOrganizeId()));
        vo.setPid(organizeArchiRole.getPid() != null ? String.valueOf(organizeArchiRole.getPid()) : null);
        // 初始化children为空列表
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
