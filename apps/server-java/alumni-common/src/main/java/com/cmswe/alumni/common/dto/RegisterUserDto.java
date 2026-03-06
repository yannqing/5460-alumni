package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "RegisterUserDto", description = "用户注册信息DTO")
public class RegisterUserDto implements Serializable {

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", required = true)
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID", required = true)
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女", required = true)
    @NotNull(message = "性别不能为空")
    private Integer gender;

    /**
     * 手机号
     */
    @Schema(description = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Serial
    private static final long serialVersionUID = 1L;
}
