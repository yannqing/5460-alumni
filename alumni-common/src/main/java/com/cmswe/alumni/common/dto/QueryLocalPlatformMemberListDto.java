package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询校处会成员列表请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "查询校处会成员列表请求DTO")
public class QueryLocalPlatformMemberListDto extends PageRequest implements Serializable {

    /**
     * 校处会ID
     */
    @Schema(description = "校处会ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校处会ID不能为空")
    private Long localPlatformId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String nickname;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer gender;

    /**
     * 当前所在省市
     */
    @Schema(description = "当前所在省市", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curProvince;

    /**
     * 当前所在市区
     */
    @Schema(description = "当前所在市区", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curCity;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段: 根据加入时间排序（joinTime）")
    private String sortField;

    @Serial
    private static final long serialVersionUID = 1L;
}
