package com.cmswe.alumni.common.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知统计 DTO
 *
 * @author CMSWE
 * @since 2025-12-05
 */
@Data
public class NotificationStatistic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 数量
     */
    private Integer count;
}
