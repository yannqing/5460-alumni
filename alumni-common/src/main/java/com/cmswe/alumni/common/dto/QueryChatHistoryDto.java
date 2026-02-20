package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询聊天历史DTO
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Data
@Schema(description = "查询聊天历史请求")
public class QueryChatHistoryDto {

    @NotNull(message = "对方用户ID不能为空")
    @Schema(description = "对方用户ID", example = "123456")
    private Long otherUserId;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;
}
