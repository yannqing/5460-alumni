package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 新增首页文章请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CreateHomePageArticleDto", description = "新增首页文章请求参数")
public class CreateHomePageArticleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "文章标题不能为空")
    @Schema(description = "文章标题", example = "校友会最新动态")
    private String articleTitle;

    @Schema(description = "封面图文件 id", example = "123456")
    private Long coverImg;

    @Schema(description = "描述", example = "这是一篇关于校友会的文章")
    private String description;

    @NotNull(message = "文章类型不能为空")
    @Schema(description = "文章类型（1-公众号，2-内部路径，3-第三方链接）", example = "1")
    private Integer articleType;

    @Schema(description = "文章链接", example = "https://mp.weixin.qq.com/xxx")
    private String articleLink;

    @Schema(description = "文章内容文件 id", example = "654321")
    private Long articleFile;

    @Schema(description = "其他信息", example = "{}")
    private String metaData;

    @Schema(description = "发布者 id", example = "1001")
    private Long publishWxId;

    @Schema(description = "发布者名称", example = "张三")
    private String publishUsername;

    @Schema(description = "发布者类型枚举（ASSOCIATION-校友会，LOCAL_PLATFORM-校促会）", example = "ASSOCIATION")
    private String publishType;

    @Schema(description = "是否展示在首页（0-不展示，1-展示）", example = "0")
    private Integer showOnHomepage;

    @Schema(description = "子文章列表（可选，子文章会继承父文章的发布者信息）")
    private List<CreateChildArticleDto> childArticles;
}
