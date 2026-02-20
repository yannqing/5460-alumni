package com.cmswe.alumni.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Schema(name = "ChatMemberParam", description = "群组-成员查询参数对象")
public class ChatMemberParam {
    @Schema(description = "群号")
    private String chatGroupId;

    private int pageNo = 1;//第几页
    private int pageSize = 10;//每页多少个

}
