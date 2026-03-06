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
 * 核心成员VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "核心成员信息")
public class CoreMemberVo implements Serializable {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wxId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String userPhone;

    /**
     * 社会职务
     */
    @Schema(description = "社会职务")
    private String userAffiliation;

    @Serial
    private static final long serialVersionUID = 1L;
}
