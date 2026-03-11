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
 * 校促会主页展示成员VO（is_show=1的成员）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "校促会主页展示成员")
public class LocalPlatformShowMemberVo implements Serializable {

    @Schema(description = "成员ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "校友ID")
    private String wxId;

    @Schema(description = "用户名字")
    private String username;

    @Schema(description = "角色名称")
    private String roleName;

    @Serial
    private static final long serialVersionUID = 1L;
}
