package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员查询审批记录列表请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AdminQueryApplyListDto", description = "管理员查询审批记录列表请求参数")
public class AdminQueryApplyListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Long current;

    @Schema(description = "每页大小", example = "10")
    private Long size;

    @Schema(description = "审核状态 0-审核中，1-审核通过，2-审核拒绝", example = "0")
    private Integer applyStatus;

    @Schema(description = "文章ID", example = "123456789")
    private Long homeArticleId;

    @Schema(description = "审批人ID", example = "123456789")
    private Long appliedWxId;

    @Schema(description = "审批人名称（模糊搜索）", example = "张三")
    private String appliedName;

    @Schema(description = "开始时间（创建时间）", example = "2024-01-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间（创建时间）", example = "2024-12-31T23:59:59")
    private LocalDateTime endTime;
}
