package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息死信队列实体类
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@TableName("message_dead_letter")
public class MessageDeadLetter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始消息ID
     */
    private String messageId;

    /**
     * 消息类别：P2P, GROUP, SYSTEM, ORGANIZATION, BUSINESS
     */
    private String messageCategory;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 原始消息JSON（完整的UnifiedMessage）
     */
    private String originalMessage;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 失败时间
     */
    private LocalDateTime failureTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 错误堆栈信息
     */
    private String errorStack;

    /**
     * 处理状态：0-未处理, 1-处理中, 2-已处理, 3-已忽略
     */
    private Integer processStatus;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 处理结果
     */
    private String processResult;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
