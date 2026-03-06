package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 海报模板列表项 VO（仅 id、url）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "海报模板项")
public class PosterTemplateItemVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模版URL")
    private String url;
}
