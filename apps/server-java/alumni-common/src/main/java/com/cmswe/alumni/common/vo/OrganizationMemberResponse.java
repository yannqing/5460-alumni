package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.beans.BeanUtils;

import java.io.Serial;

/**
 * 组织成员列表响应VO（包含架构角色信息）
 * 继承自 UserListResponse，额外包含组织架构角色信息
 * 用于校友会成员列表和校处会成员列表的返回
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "OrganizationMemberResponse", description = "组织成员列表响应类型（包含架构角色）")
public class OrganizationMemberResponse extends UserListResponse {

    /**
     * 用户架构角色
     */
    @Schema(description = "用户架构角色（在组织中的角色）")
    private OrganizeArchiRoleVo organizeArchiRole;

    /**
     * 成员表中的用户名（来自组织架构成员表）
     */
    @Schema(description = "成员表中的用户名（来自组织架构成员表）")
    private String username;

    /**
     * 成员表中的角色名称（来自组织架构成员表）
     */
    @Schema(description = "成员表中的角色名称（来自组织架构成员表）")
    private String roleName;

    /**
     * 当前登录用户是否关注了该成员：true-已关注 false-未关注 null-未登录
     */
    @Schema(description = "当前登录用户是否关注了该成员：true-已关注 false-未关注 null-未登录")
    private Boolean isFollowed;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 将 WxUserInfo 转换为 OrganizationMemberResponse
     *
     * @param wxUserInfo 用户信息实体
     * @return 响应VO
     */
    public static OrganizationMemberResponse objToVo(WxUserInfo wxUserInfo) {
        if (wxUserInfo == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        OrganizationMemberResponse response = new OrganizationMemberResponse();
        BeanUtils.copyProperties(wxUserInfo, response);
        return response;
    }
}
