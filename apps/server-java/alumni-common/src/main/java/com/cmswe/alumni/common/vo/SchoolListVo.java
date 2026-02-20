package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SchoolListVo", description = "学校列表返回VO")
public class SchoolListVo implements Serializable {

    /**
     * 母校ID
     */
    @Schema(description = "母校ID")
    private String schoolId;

    /**
     * 学校logo
     */
    @Schema(description = "学校logoURL")
    private String logo;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolName;

    /**
     * 所在省
     */
    @Schema(description = "所在省")
    private String province;

    /**
     * 所在市
     */
    @Schema(description = "所在市")
    private String city;

    /**
     * 办学层次
     */
    @Schema(description = "办学层次")
    private String level;

    /**
     * 建校日期
     */
    @Schema(description = "建校日期")
    private LocalDate foundingDate;

    /**
     * 官方认证状态（0-未认证，1-已认证）
     */
    @Schema(description = "官方认证状态（0-未认证，1-已认证）")
    private Integer officialCertification;

    @Serial
    private static final long serialVersionUID = 1L;

    public static SchoolListVo objToVo(School school) {
        if (school == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        SchoolListVo schoolListVo = new SchoolListVo();
        BeanUtils.copyProperties(school, schoolListVo);
        return schoolListVo;
    }
}
