package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章表
 * @TableName article
 */
@TableName(value = "article")
@Data
public class HomePageArticle implements Serializable {
    /**
     * 文章 id
     */
    @TableId(value = "article_id", type = IdType.ASSIGN_ID)
    private Long homeArticleId;

    /**
     * 文章父 id
     */
    @TableField(value = "pid")
    private Long pid;

    /**
     * 文章标题
     */
    @TableField(value = "article_title")
    private String articleTitle;

    /**
     * 封面图文件 id
     */
    @TableField(value = "cover_img")
    private Long coverImg;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 文章类型（1-公众号，2-内部路径，3-第三方链接）
     */
    @TableField(value = "article_type")
    private Integer articleType;

    /**
     * 文章链接
     */
    @TableField(value = "article_link")
    private String articleLink;

    /**
     * 文章内容文件 id
     */
    @TableField(value = "article_file")
    private Long articleFile;

    /**
     * 其他信息
     */
    @TableField(value = "meta_data")
    private String metaData;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField(value = "article_status")
    private Integer articleStatus;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核拒绝
     */
    @TableField(value = "apply_status")
    private Integer applyStatus;

    /**
     * 审核人 id
     */
    @TableField(value = "reviewer_wx_id")
    private Long reviewerWxId;

    /**
     * 审核人名称
     */
    @TableField(value = "reviewer_name")
    private String reviewerName;

    /**
     * 审核意见
     */
    @TableField(value = "review_opinion")
    private String reviewOpinion;

    /**
     * 审核完成时间
     */
    @TableField(value = "reviewed_time")
    private LocalDateTime reviewedTime;

    /**
     * 发布者 id
     */
    @TableField(value = "publish_wx_id")
    private Long publishWxId;

    /**
     * 发布者名称
     */
    @TableField(value = "publish_username")
    private String publishUsername;

    /**
     * 发布者类型枚举（alumni，association）
     */
    @TableField(value = "publish_type")
    private String publishType;

    /**
     * 发布者头像
     */
    @TableField(value = "publisher_avatar")
    private String publisherAvatar;

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
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
