package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 审核待办统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核待办统计")
public class AuditStatisticsVo implements Serializable {

    @Schema(description = "待办数量统计映射，key为功能代码或权限代码")
    private Map<String, Integer> todoCounts;

    private static final long serialVersionUID = 1L;
}
