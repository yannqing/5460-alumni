package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 活动参与者VO（C 端展示用，已根据用户隐私设置脱敏）
 *
 * <p>不暴露真实姓名 user_name 与手机号 user_phone，仅返回昵称、头像、性别（受隐私控制）与报名通过的"日期"。</p>
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ActivityParticipantVo", description = "活动参与者展示VO（隐私过滤后）")
public class ActivityParticipantVo implements Serializable {

    /**
     * 用户ID（wxId，String 化避免精度丢失，用于跳转个人主页）
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 用户昵称（受隐私控制，不可见时返回"匿名校友"）
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 用户头像（始终返回，无图时由前端兜底默认头像）
     */
    @Schema(description = "用户头像")
    private String avatarUrl;

    /**
     * 性别：0-未知 1-男 2-女（受隐私控制，不可见时返回 null）
     */
    @Schema(description = "性别")
    private Integer gender;

    /**
     * 报名通过日期（脱敏到天）
     */
    @Schema(description = "报名通过日期")
    private LocalDate joinDate;

    @Serial
    private static final long serialVersionUID = 1L;
}
