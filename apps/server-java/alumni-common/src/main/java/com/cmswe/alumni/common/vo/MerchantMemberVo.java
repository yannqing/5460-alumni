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
 * 商户成员VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "MerchantMemberVo", description = "商户成员响应类型")
public class MerchantMemberVo implements Serializable {

    /**
     * 成员ID
     */
    @Schema(description = "成员ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户ID（JSON 输出为字符串，避免前端 Number 精度丢失导致编辑/删除时查不到成员）
     */
    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 商户ID
     */
    @Schema(description = "商户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long merchantId;

    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shopId;

    /**
     * 店铺名称
     */
    @Schema(description = "店铺名称")
    private String shopName;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleOrId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 职务
     */
    @Schema(description = "职务")
    private String position;

    /**
     * 加入时间
     */
    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

    /**
     * 状态：0-退出 1-正常
     */
    @Schema(description = "状态")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;
}
