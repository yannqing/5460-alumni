package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "MerchantBusinessCategoryVo", description = "商户经营类目及范围返回VO")
public class MerchantBusinessCategoryVo implements Serializable {
    @Schema(description = "主键id（雪花id）")
    private String id;

    @Schema(description = "父级id（0表示一级类目）")
    private String parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "层级：1-经营类目 2-经营范围")
    private Integer level;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "子类目列表")
    private List<MerchantBusinessCategoryVo> children;
}
