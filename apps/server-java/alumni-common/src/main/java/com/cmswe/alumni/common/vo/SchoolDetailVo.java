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
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SchoolDetailVo", description = "学校详情返回VO")
public class SchoolDetailVo implements Serializable {

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
     * 学校编码
     */
    @Schema(description = "学校编码")
    private String schoolCode;

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
     * 合并院校（json 数组）
     */
    @Schema(description = "合并院校")
    private String mergedInstitutions;

    /**
     * 曾用名（json 数组）
     */
    @Schema(description = "曾用名")
    private String previousName;

    /**
     * 其他内容
     */
    @Schema(description = "其他内容")
    private String otherInfo;

    /**
     * 学校描述
     */
    @Schema(description = "学校描述")
    private String description;

    /**
     * 建校日期
     */
    @Schema(description = "建校日期")
    private LocalDate foundingDate;

    /**
     * 学校地址
     */
    @Schema(description = "学校地址")
    private String location;

    /**
     * 官方认证状态（0-未认证，1-已认证）
     */
    @Schema(description = "官方认证状态（0-未认证，1-已认证）")
    private Integer officialCertification;

    /**
     * 母校下的所有校友会列表
     */
    @Schema(description = "母校下的所有校友会列表")
    private List<AlumniAssociationListVo> alumniAssociationListVos;

    /**
     * 校友总会信息
     */
    @Schema(description = "校友总会信息")
    private AlumniHeadquartersListVo alumniHeadquarters;

    @Serial
    private static final long serialVersionUID = 1L;

    public static SchoolDetailVo objToVo(School school) {
        if (school == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        SchoolDetailVo schoolDetailVo = new SchoolDetailVo();
        BeanUtils.copyProperties(school, schoolDetailVo);
        return schoolDetailVo;
    }
}
