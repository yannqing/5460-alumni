package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校促会成员列表响应VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "校促会成员列表响应")
public class LocalPlatformMemberListVo implements Serializable {

    @Schema(description = "成员ID（local_platform_member 表主键，用于 addToStructure/removeFromStructure）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;

    @Schema(description = "用户ID（字符串形式，避免前端长整型精度丢失）")
    private String wxId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "组织架构角色ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleOrId;

    @Schema(description = "组织架构名称（关联 role_or_id 的 role_or_name）")
    private String roleOrName;

    @Schema(description = "联系方式")
    private String contactInformation;

    @Schema(description = "社会职务")
    private String socialDuties;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Serial
    private static final long serialVersionUID = 1L;
}
