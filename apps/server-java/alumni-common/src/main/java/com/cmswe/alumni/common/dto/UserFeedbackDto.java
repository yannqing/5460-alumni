package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户反馈DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户反馈请求")
public class UserFeedbackDto implements Serializable {

    @Schema(description = "反馈类型：1-数据问题，2-功能建议，3-Bug反馈，4-使用问题，5-其他", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "反馈类型不能为空")
    private Integer feedbackType;

    @Schema(description = "反馈标题", example = "数据库缺少XX大学", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈标题不能为空")
    @Size(max = 200, message = "反馈标题长度不能超过200个字符")
    private String feedbackTitle;

    @Schema(description = "反馈内容", example = "在注册填写教育经历时，学校列表中没有找到XX大学，希望能够添加该学校到数据库中", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 2000, message = "反馈内容长度不能超过2000个字符")
    private String feedbackContent;

    @Schema(description = "联系方式", example = "13800138000")
    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    private String contactInfo;

    @Schema(description = "附件ID数组（JSON格式）", example = "[123456,789012]")
    @Size(max = 500, message = "附件ID数组长度不能超过500个字符")
    private String attachmentIds;

    @Serial
    private static final long serialVersionUID = 1L;
}
