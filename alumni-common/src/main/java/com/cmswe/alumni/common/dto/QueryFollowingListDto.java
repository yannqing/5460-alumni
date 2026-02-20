package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询我关注的列表DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "查询我关注的列表DTO")
public class QueryFollowingListDto extends PageRequest implements Serializable {

    @Schema(description = "关注目标类型：1-用户，2-校友会，3-母校，4-商户")
    private Integer targetType;

    @Schema(description = "关注状态：1-正常关注 2-特别关注 3-免打扰 4-已取消")
    private Integer followStatus;

    @Schema(description = "搜索关键词（目标名称）")
    private String keyword;

    @Serial
    private static final long serialVersionUID = 1L;
}
