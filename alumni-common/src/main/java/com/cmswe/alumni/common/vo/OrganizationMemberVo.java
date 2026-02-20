package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织成员VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "OrganizationMemberVo", description = "组织成员响应类型")
public class OrganizationMemberVo implements Serializable {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
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
     * 加入时间
     */
    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

    @Serial
    private static final long serialVersionUID = 1L;
}