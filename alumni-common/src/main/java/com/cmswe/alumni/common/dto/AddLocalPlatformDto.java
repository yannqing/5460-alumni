package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.entity.LocalPlatform;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddLocalPlatformDto implements Serializable {

    /**
     * 校处会名称
     */
    @Schema(description = "校处会名称")
    @NotBlank(message = "名称不能为空")
    private String platformName;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市")
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 管理员用户ID
     */
    @Schema(description = "管理员用户ID")
    @NotBlank(message = "管理员id不能为空")
    private Long wxId;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息")
    private String contactInfo;

    @Serial
    private static final long serialVersionUID = 1L;

    public static LocalPlatform dtoToObj(AddLocalPlatformDto addLocalPlatformDto) {
        if (addLocalPlatformDto == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        LocalPlatform localPlatform = new LocalPlatform();
        BeanUtils.copyProperties(addLocalPlatformDto, localPlatform);
        return localPlatform;
    }
}
