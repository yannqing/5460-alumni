package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 初始成员 DTO（用于申请创建校友会时填写初始成员）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "初始成员信息")
public class InitialMemberDto implements Serializable {

    /**
     * 成员微信用户 ID
     */
    @Schema(description = "成员微信用户 ID")
    private Long wxId;

    /**
     * 成员姓名
     */
    @Schema(description = "成员姓名")
    private String name;

    /**
     * 成员架构角色
     */
    @Schema(description = "成员架构角色")
    private String role;

    @Serial
    private static final long serialVersionUID = 1L;
}
