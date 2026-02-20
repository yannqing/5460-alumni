package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校友教育经历DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "校友教育经历请求")
public class AlumniEducationDto implements Serializable {

    @Schema(description = "教育经历ID（更新时需要，新增时不需要）", example = "1234567890")
    private Long alumniEducationId;

    @Schema(description = "学校ID", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    @Schema(description = "入学年份", example = "2015")
    @Min(value = 1900, message = "入学年份不能早于1900年")
    @Max(value = 2100, message = "入学年份不能晚于2100年")
    private Integer enrollmentYear;

    @Schema(description = "毕业年份", example = "2019")
    @Min(value = 1900, message = "毕业年份不能早于1900年")
    @Max(value = 2100, message = "毕业年份不能晚于2100年")
    private Integer graduationYear;

    @Schema(description = "院系", example = "计算机学院")
    @Size(max = 200, message = "院系名称长度不能超过200个字符")
    private String department;

    @Schema(description = "专业", example = "软件工程")
    @Size(max = 200, message = "专业名称长度不能超过200个字符")
    private String major;

    @Schema(description = "班级", example = "软件1班")
    @Size(max = 100, message = "班级名称长度不能超过100个字符")
    private String className;

    @Schema(description = "学历层次", example = "本科")
    @Size(max = 50, message = "学历层次长度不能超过50个字符")
    private String educationLevel;

    @Schema(description = "学位", example = "学士")
    @Size(max = 50, message = "学位长度不能超过50个字符")
    private String degree;

    @Schema(description = "认证状态", example = "0")
    private Integer certificationStatus;

    @Schema(description = "类型（1 主要经历 0 次要经历）", example = "1")
    private Integer type;

    @Serial
    private static final long serialVersionUID = 1L;
}
