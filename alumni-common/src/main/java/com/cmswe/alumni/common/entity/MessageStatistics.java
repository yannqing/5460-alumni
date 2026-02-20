package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消息统计实体类
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@TableName("message_statistics")
public class MessageStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 消息类别：P2P, GROUP, SYSTEM, ORGANIZATION, BUSINESS
     */
    private String messageCategory;

    /**
     * 总消息数
     */
    private Long totalCount;

    /**
     * 成功消息数
     */
    private Long successCount;

    /**
     * 失败消息数
     */
    private Long failedCount;

    /**
     * 平均处理时间（毫秒）
     */
    private Integer avgProcessTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
