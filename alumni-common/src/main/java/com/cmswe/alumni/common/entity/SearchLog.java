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
 * 搜索日志表（监控、性能分析、问题排查）
 *
 * @author CNI Alumni System
 * @TableName search_log
 */
@TableName(value = "search_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchLog implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 链路追踪ID（全链路日志追踪）
     */
    @TableField(value = "trace_id")
    private String traceId;

    /**
     * 用户ID（未登录为NULL）
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 搜索关键词
     */
    @TableField(value = "keyword")
    private String keyword;

    /**
     * 搜索类型（ALUMNI/ASSOCIATION/MERCHANT）
     */
    @TableField(value = "search_type")
    private String searchType;

    /**
     * 过滤条件（JSON格式存储）
     */
    @TableField(value = "filters")
    private String filters;

    /**
     * 页码
     */
    @TableField(value = "page_num")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @TableField(value = "page_size")
    private Integer pageSize;

    /**
     * 搜索结果总数
     */
    @TableField(value = "result_count")
    private Long resultCount;

    /**
     * 总搜索耗时（毫秒）
     */
    @TableField(value = "search_time_ms")
    private Integer searchTimeMs;

    /**
     * ES查询耗时（毫秒）
     */
    @TableField(value = "es_time_ms")
    private Integer esTimeMs;

    /**
     * 缓存命中情况（0-未命中 1-L1本地缓存 2-L2Redis缓存）
     */
    @TableField(value = "cache_hit")
    private Integer cacheHit;

    /**
     * 是否成功（0-失败 1-成功）
     */
    @TableField(value = "is_success")
    private Integer isSuccess;

    /**
     * 错误信息（失败时记录）
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    private String ipAddress;

    /**
     * User-Agent信息
     */
    @TableField(value = "user_agent")
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
