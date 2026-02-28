package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增子文章请求 DTO
 * 子文章继承父文章的发布者信息，只需要提供文章本身的必要字段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CreateChildArticleDto", description = "新增子文章请求参数")
public class CreateChildArticleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "子文章标题不能为空")
    @Schema(description = "文章标题", example = "子文章标题示例")
    private String articleTitle;

    @Schema(description = "封面图文件 id", example = "123456")
    private Long coverImg;

    @Schema(description = "描述", example = "这是一篇子文章")
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
}
