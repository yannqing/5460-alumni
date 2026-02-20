package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 更新校友会加入申请 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAlumniAssociationJoinApplicationDto implements Serializable {

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    @NotNull(message = "真实姓名不能为空")
    private String name;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    @NotNull(message = "身份证号不能为空")
    private String identifyCode;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 申请理由
     */
    @Schema(description = "申请理由")
    private String applicationReason;

    /**
     * 附件ID数组
     */
    @Schema(description = "附件ID数组（用户上传的材料）")
    private List<Long> attachmentIds;

    /**
     * 学校ID（可选，如果填写则会保存教育经历信息）
     */
    @Schema(description = "学校ID（可选）")
    private Long schoolId;

    /**
     * 入学年份
     */
    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    /**
     * 毕业年份
     */
    @Schema(description = "毕业年份")
    private Integer graduationYear;

    /**
     * 院系
     */
    @Schema(description = "院系")
    private String department;

    /**
     * 专业
     */
    @Schema(description = "专业")
    private String major;

    /**
     * 班级
     */
    @Schema(description = "班级")
    private String className;

    /**
     * 学历层次
     */
    @Schema(description = "学历层次")
    private String educationLevel;

    @Serial
    private static final long serialVersionUID = 1L;
}
