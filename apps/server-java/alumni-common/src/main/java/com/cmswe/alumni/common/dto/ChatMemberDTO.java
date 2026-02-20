package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Schema(name = "ChatMemberDTO", description = "群成员数据传输对象")
public class ChatMemberDTO {
    @Schema(description = "群号")
    private String chatGroupId;
    @Schema(description = "用户Id（插入单条使用）")
    private String userId;
    @Schema(description = "用户Id列表（插入多条使用）")
    private List<String> userIds;

}
