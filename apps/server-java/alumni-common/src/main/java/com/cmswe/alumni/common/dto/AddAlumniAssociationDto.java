package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddAlumniAssociationDto implements Serializable {

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    @NotNull(message = "名称不能为空")
    private String associationName;

    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校ID")
    @NotNull(message = "母校id不能为空")
    private Long schoolId;

    /**
     * 所属校处会ID
     */
    @Schema(description = "所属校处会ID")
    @NotNull(message = "校处会id不能为空")
    private Long platformId;

    /**
     * 会长用户ID
     */
    @Schema(description = "会长用户ID")
    @NotNull(message = "会长id不能为空")
    private Long presidentUserId;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息（json 格式）")
    private String contactInfo;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点")
    private String location;

    @Serial
    private static final long serialVersionUID = 1L;

    public static AlumniAssociation dtoToObject(AddAlumniAssociationDto addAlumniAssociationDto) {
        if (addAlumniAssociationDto == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        AlumniAssociation alumniAssociation = new AlumniAssociation();
        BeanUtils.copyProperties(addAlumniAssociationDto, alumniAssociation);

        return alumniAssociation;
    }
}
