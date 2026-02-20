package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.HomePageBanner;
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
import java.time.LocalDateTime;

/**
 * 轮播图列表 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "BannerVo", description = "轮播图列表返回 VO")
public class BannerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "轮播图ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bannerId;

    @Schema(description = "轮播图标题")
    private String bannerTitle;

    @Schema(description = "轮播图图片信息")
    private FilesVo bannerImage;

    @Schema(description = "跳转类型：1-无跳转，2-内部路径，3-第三方链接，4-文章详情")
    private Integer bannerType;

    @Schema(description = "跳转链接地址")
    private String linkUrl;

    @Schema(description = "关联业务ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long relatedId;

    @Schema(description = "关联业务类型")
    private String relatedType;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer bannerStatus;

    @Schema(description = "生效开始时间")
    private LocalDateTime startTime;

    @Schema(description = "生效结束时间")
    private LocalDateTime endTime;

    @Schema(description = "浏览次数")
    private Long viewCount;

    @Schema(description = "点击次数")
    private Long clickCount;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static BannerVo objToVo(HomePageBanner banner) {
        if (banner == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        BannerVo vo = new BannerVo();
        BeanUtils.copyProperties(banner, vo, "bannerImage");
        return vo;
    }
}
