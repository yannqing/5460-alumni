package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量初始化结果 VO
 *
 * @author CMSWE
 * @since 2026-03-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchInitResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总用户数
     */
    @Schema(description = "总用户数")
    private Integer totalUsers;

    /**
     * 需要初始化的用户数
     */
    @Schema(description = "需要初始化的用户数")
    private Integer needInitUsers;

    /**
     * 成功初始化的用户数
     */
    @Schema(description = "成功初始化的用户数")
    private Integer successCount;

    /**
     * 初始化失败的用户数
     */
    @Schema(description = "初始化失败的用户数")
    private Integer failedCount;

    /**
     * 执行开始时间
     */
    @Schema(description = "执行开始时间")
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    @Schema(description = "执行结束时间")
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    @Schema(description = "执行耗时（毫秒）")
    private Long durationMs;

    /**
     * 失败的用户ID列表（用于排查问题）
     */
    @Schema(description = "失败的用户ID列表")
    private List<Long> failedUserIds;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;
}
