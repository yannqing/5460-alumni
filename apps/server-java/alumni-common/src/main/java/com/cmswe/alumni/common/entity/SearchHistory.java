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
 * 用户搜索历史表
 *
 * @author CNI Alumni System
 * @TableName search_history
 */
@TableName(value = "search_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联wx_users.wx_id）
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 搜索关键词
     */
    @TableField(value = "keyword")
    private String keyword;

    /**
     * 搜索类型（ALUMNI-校友 ASSOCIATION-校友会 MERCHANT-商户）
     */
    @TableField(value = "search_type")
    private String searchType;

    /**
     * 搜索结果数量
     */
    @TableField(value = "result_count")
    private Integer resultCount;

    /**
     * 搜索耗时（毫秒）
     */
    @TableField(value = "search_time_ms")
    private Integer searchTimeMs;

    /**
     * 是否来自搜索建议（0-否 1-是）
     */
    @TableField(value = "from_suggest")
    private Integer fromSuggest;

    /**
     * 设备类型（iOS/Android/Web/MiniProgram）
     */
    @TableField(value = "device_type")
    private String deviceType;

    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    private String ipAddress;

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
