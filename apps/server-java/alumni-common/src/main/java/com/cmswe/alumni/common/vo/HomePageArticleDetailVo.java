package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.HomePageArticle;
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
 * 首页文章详情 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "HomePageArticleDetailVo", description = "首页文章详情返回VO")
public class HomePageArticleDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "首页文章id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long homeArticleId;

    @Schema(description = "文章标题")
    private String articleTitle;

    @Schema(description = "封面图文件id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long coverImg;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "文章类型（1-公众号，2-内部路径，3-第三方链接）")
    private Integer articleType;

    @Schema(description = "文章链接")
    private String articleLink;

    @Schema(description = "文章内容文件id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleFile;

    @Schema(description = "其他信息")
    private String metaData;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer articleStatus;

    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核拒绝")
    private Integer applyStatus;

    @Schema(description = "发布者id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long publishWxId;

    @Schema(description = "发布者名称")
    private String publishUsername;

    @Schema(description = "发布者类型（ASSOCIATION-校友会，LOCAL_PLATFORM-校促会）")
    private String publishType;

    @Schema(description = "发布者头像")
    private String publisherAvatar;

    @Schema(description = "实际发布用户ID（通过token解析获取的操作者ID）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long actualPublisherWxId;

    @Schema(description = "是否展示在首页（0-不展示，1-展示）")
    private Integer showOnHomepage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "子文章列表")
    private java.util.List<HomePageArticleVo> children;

    public static HomePageArticleDetailVo objToVo(HomePageArticle article) {
        if (article == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        HomePageArticleDetailVo vo = new HomePageArticleDetailVo();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }
}
