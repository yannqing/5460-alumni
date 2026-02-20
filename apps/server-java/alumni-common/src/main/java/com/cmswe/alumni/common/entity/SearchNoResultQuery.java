package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索无结果查询表（搜索召回优化依据）
 *
 * @author CNI Alumni System
 * @TableName search_no_result_query
 */
@TableName(value = "search_no_result_query")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchNoResultQuery implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 搜索关键词（无结果的查询词）
     */
    @TableField(value = "keyword")
    private String keyword;

    /**
     * 搜索类型（ALUMNI/ASSOCIATION/MERCHANT）
     */
    @TableField(value = "search_type")
    private String searchType;

    /**
     * 查询次数（相同关键词累加计数）
     */
    @TableField(value = "query_count")
    private Long queryCount;

    /**
     * 最后查询时间
     */
    @TableField(value = "last_query_time")
    private LocalDateTime lastQueryTime;

    /**
     * 处理状态（PENDING-待处理 RESOLVED-已解决 IGNORED-已忽略）
     */
    @TableField(value = "status")
    private String status;

    /**
     * 解决方案（如：新增同义词、建议词、数据补充）
     */
    @TableField(value = "solution")
    private String solution;

    /**
     * 处理人（运营人员）
     */
    @TableField(value = "handler")
    private String handler;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
