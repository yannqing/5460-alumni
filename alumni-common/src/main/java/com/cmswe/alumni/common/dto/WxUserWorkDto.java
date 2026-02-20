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
import java.time.LocalDate;

/**
 * 用户工作经历DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户工作经历请求")
public class WxUserWorkDto implements Serializable {

    @Schema(description = "工作经历ID（更新时需要，新增时不需要）", example = "1234567890")
    private Long userWorkId;

    @Schema(description = "公司名称", example = "腾讯科技", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公司名称不能为空")
    @Size(max = 100, message = "公司名称长度不能超过100个字符")
    private String companyName;

    @Schema(description = "职位/角色名称", example = "高级软件工程师", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "职位名称不能为空")
    @Size(max = 100, message = "职位名称长度不能超过100个字符")
    private String position;

    @Schema(description = "所属行业", example = "互联网")
    @Size(max = 50, message = "行业名称长度不能超过50个字符")
    private String industry;

    @Schema(description = "入职日期", example = "2020-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入职日期不能为空")
    private LocalDate startDate;

    @Schema(description = "离职日期（NULL表示至今）", example = "2023-12-31")
    private LocalDate endDate;

    @Schema(description = "是否当前在职：0-否，1-是", example = "1")
    private Integer isCurrent;

    @Schema(description = "工作内容/项目成就详情", example = "负责核心业务系统的开发与维护")
    @Size(max = 2000, message = "工作描述长度不能超过2000个字符")
    private String workDescription;

    @Serial
    private static final long serialVersionUID = 1L;
}
