package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniEducation;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlumniEducationListVo implements Serializable {

    /**
     * 学校信息
     */
    @Schema(description = "学校信息")
    private SchoolListVo schoolInfo;

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

    /**
     * 学位
     */
    @Schema(description = "学位（如：学士、硕士、博士等）")
    private String degree;

    /**
     * 认证状态
     */
    @Schema(description = "认证状态")
    private Integer certificationStatus;

    /**
     * 类型（1 主要经历 0 次要经历）
     */
    @Schema(description = "类型（1 主要经历 0 次要经历）")
    private Integer type;

    @Serial
    private static final long serialVersionUID = 1L;

    public static AlumniEducationListVo objToVo(AlumniEducation alumniEducation) {
        if (alumniEducation == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        AlumniEducationListVo vo = new AlumniEducationListVo();
        BeanUtils.copyProperties(alumniEducation, vo);
        return vo;
    }
}
