package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 活动报名请求 DTO
 *
 * <p>报名用户的姓名 / 手机号由服务端从 wx_user_info 自动取（真实姓名+绑定手机号），不再让用户手动填写。</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "活动报名请求DTO")
public class ApplyActivityRegistrationDto implements Serializable {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long activityId;

    /**
     * 备注（可选，如同行人数等）
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注信息")
    private String remark;

    @Serial
    private static final long serialVersionUID = 1L;
}
