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
 * 更新子文章请求 DTO
 * 如果包含 homeArticleId 则更新现有子文章，否则新增子文章
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UpdateChildArticleDto", description = "更新/新增子文章请求参数")
public class UpdateChildArticleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "子文章ID（更新时必填，新增时不填）", example = "1234567890")
    private Long homeArticleId;

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
