package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新增轮播图请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CreateBannerDto", description = "新增轮播图请求参数")
public class CreateBannerDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "轮播图标题不能为空")
    @Schema(description = "轮播图标题", example = "欢迎加入校友会")
    private String bannerTitle;

    @NotNull(message = "轮播图图片不能为空")
    @Schema(description = "轮播图图片文件ID", example = "123456")
    private Long bannerImage;

    @NotNull(message = "跳转类型不能为空")
    @Schema(description = "跳转类型：1-无跳转，2-内部路径，3-第三方链接，4-文章详情", example = "1")
    private Integer bannerType;

    @Schema(description = "跳转链接地址", example = "https://example.com")
    private String linkUrl;

    @Schema(description = "关联业务ID（如文章ID）", example = "789")
    private Long relatedId;

    @Schema(description = "关联业务类型（ARTICLE-文章，ACTIVITY-活动等）", example = "ARTICLE")
    private String relatedType;

    @Schema(description = "排序顺序，数值越小越靠前", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer bannerStatus;

    @Schema(description = "生效开始时间")
    private LocalDateTime startTime;

    @Schema(description = "生效结束时间")
    private LocalDateTime endTime;

    @Schema(description = "描述信息", example = "这是首页轮播图")
    private String description;
}
