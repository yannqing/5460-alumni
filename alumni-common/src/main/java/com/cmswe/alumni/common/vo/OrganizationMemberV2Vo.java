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
import java.time.LocalDateTime;

/**
 * 组织成员VO V2版本（基于username）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "OrganizationMemberV2Vo", description = "组织成员响应类型V2（基于username）")
public class OrganizationMemberV2Vo implements Serializable {

    /**
     * 成员表 ID (alumni_association_member 或 local_platform_member 的 id)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "成员主键ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 用户id（可能为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "用户id（可能为空）")
    private Long wxId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String name;

    /**
     * 用户头像 URL
     */
    @Schema(description = "用户头像")
    private String avatarUrl;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别")
    private Integer gender;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 加入时间
     */
    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
