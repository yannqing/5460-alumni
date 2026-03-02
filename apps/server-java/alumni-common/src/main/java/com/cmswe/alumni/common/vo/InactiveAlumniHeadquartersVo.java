package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未激活校友总会列表 VO
 */
@Data
@Schema(name = "InactiveAlumniHeadquartersVo", description = "未激活校友总会列表 VO")
public class InactiveAlumniHeadquartersVo implements Serializable {

    @Schema(description = "校友总会 ID")
    private String headquartersId;

    @Schema(description = "校友总会 名称")
    private String headquartersName;

    @Schema(description = "校友总会 Logo")
    private String logo;

    @Serial
    private static final long serialVersionUID = 1L;

    public static InactiveAlumniHeadquartersVo objToVo(AlumniHeadquarters alumniHeadquarters) {
        if (alumniHeadquarters == null) {
            return null;
        }
        InactiveAlumniHeadquartersVo vo = new InactiveAlumniHeadquartersVo();
        BeanUtils.copyProperties(alumniHeadquarters, vo);
        vo.setHeadquartersId(String.valueOf(alumniHeadquarters.getHeadquartersId()));
        return vo;
    }
}
