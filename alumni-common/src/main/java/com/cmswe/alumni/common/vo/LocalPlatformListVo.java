package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.LocalPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校处会列表返回VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LocalPlatformListVo", description = "校处会列表信息返回VO")
public class LocalPlatformListVo implements Serializable {

    /**
     * 校处会ID
     */
    @Schema(description = "校处会ID")
    private String platformId;

    /**
     * 校处会名称
     */
    @Schema(description = "校处会名称")
    private String platformName;

    /**
     * 校处会头像
     */
    @Schema(description = "校处会头像")
    private String avatar;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市")
    private String city;

    /**
     * 管辖范围
     */
    @Schema(description = "管辖范围")
    private String scope;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息")
    private String contactInfo;

    /**
     * 背景图片
     */
    @Schema(description = "背景图片")
    private String bgImg;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转VO
     *
     * @param localPlatform 校处会实体
     * @return LocalPlatformListVo
     */
    public static LocalPlatformListVo objToVo(LocalPlatform localPlatform) {
        if (localPlatform == null) {
            return null;
        }
        LocalPlatformListVo localPlatformListVo = new LocalPlatformListVo();
        BeanUtils.copyProperties(localPlatform, localPlatformListVo);
        return localPlatformListVo;
    }
}
