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
 * 搜索建议词表（运营配置，优化搜索体验）
 *
 * @author CNI Alumni System
 * @TableName search_suggestion
 */
@TableName(value = "search_suggestion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestion implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 建议词（用户输入时的提示词）
     */
    @TableField(value = "keyword")
    private String keyword;

    /**
     * 搜索类型（ALUMNI/ASSOCIATION/MERCHANT/GLOBAL-全局）
     */
    @TableField(value = "search_type")
    private String searchType;

    /**
     * 权重（影响排序，值越大越靠前，范围0-1000）
     */
    @TableField(value = "weight")
    private Integer weight;

    /**
     * 来源（MANUAL-人工配置 AUTO-自动挖掘）
     */
    @TableField(value = "source")
    private String source;

    /**
     * 状态（0-禁用 1-启用）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建人（运营人员ID或姓名）
     */
    @TableField(value = "create_by")
    private String createBy;

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
