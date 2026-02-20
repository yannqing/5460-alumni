package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.WxUserWork;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户工作经历VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户工作经历信息")
public class WxUserWorkVo implements Serializable {

    /**
     * 主键ID
     */
    @Schema(description = "工作经历ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userWorkId;

    /**
     * 公司名称
     */
    @Schema(description = "公司名称")
    private String companyName;

    /**
     * 职位/角色名称
     */
    @Schema(description = "职位/角色名称")
    private String position;

    /**
     * 所属行业
     */
    @Schema(description = "所属行业")
    private String industry;

    /**
     * 入职日期
     */
    @Schema(description = "入职日期")
    private LocalDate startDate;

    /**
     * 离职日期（NULL表示至今）
     */
    @Schema(description = "离职日期")
    private LocalDate endDate;

    /**
     * 是否当前在职：0-否，1-是
     */
    @Schema(description = "是否当前在职：0-否，1-是")
    private Integer isCurrent;

    /**
     * 工作内容/项目成就详情
     */
    @Schema(description = "工作内容/项目成就详情")
    private String workDescription;

    @Serial
    private static final long serialVersionUID = 1L;

    public static WxUserWorkVo objToVo(WxUserWork wxUserWork) {
        if (wxUserWork == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        WxUserWorkVo vo = new WxUserWorkVo();
        BeanUtils.copyProperties(wxUserWork, vo);
        return vo;
    }
}
