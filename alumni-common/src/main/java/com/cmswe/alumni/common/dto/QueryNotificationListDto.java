package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询通知列表DTO
 *
 * @author CMSWE
 * @since 2025-01-07
 */
@Data
@Schema(description = "查询通知列表请求")
public class QueryNotificationListDto {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "阅读状态（0-未读，1-已读，不传则查询全部）", example = "0")
    private Integer readStatus;

    @Schema(description = "消息类型（不传则查询全部）", example = "SYSTEM_NOTICE")
    private String messageType;
}
