package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 首页轮播图表
 * @TableName home_page_banner
 */
@TableName(value = "home_page_banner")
@Data
public class HomePageBanner implements Serializable {
    /**
     * 轮播图ID（雪花ID）
     */
    @TableId(value = "banner_id", type = IdType.ASSIGN_ID)
    private Long bannerId;

    /**
     * 轮播图标题
     */
    @TableField(value = "banner_title")
    private String bannerTitle;

    /**
     * 轮播图图片文件ID
     */
    @TableField(value = "banner_image")
    private Long bannerImage;

    /**
     * 跳转类型：1-无跳转，2-内部路径，3-第三方链接，4-文章详情
     */
    @TableField(value = "banner_type")
    private Integer bannerType;

    /**
     * 跳转链接地址
     */
    @TableField(value = "link_url")
    private String linkUrl;

    /**
     * 关联业务ID（如文章ID）
     */
    @TableField(value = "related_id")
    private Long relatedId;

    /**
     * 关联业务类型（ARTICLE-文章，ACTIVITY-活动等）
     */
    @TableField(value = "related_type")
    private String relatedType;

    /**
     * 排序顺序，数值越小越靠前
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField(value = "banner_status")
    private Integer bannerStatus;

    /**
     * 生效开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 浏览次数
     */
    @TableField(value = "view_count")
    private Long viewCount;

    /**
     * 点击次数
     */
    @TableField(value = "click_count")
    private Long clickCount;

    /**
     * 描述信息
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建人ID
     */
    @TableField(value = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
