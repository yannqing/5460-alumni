package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 申请激活校友总会请求 DTO
 */
@Data
@Schema(description = "申请激活校友总会请求 DTO")
public class ApplyActivateHeadquartersRequest implements Serializable {

    /**
     * 校友总会 ID
     */
    @Schema(description = "校友总会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校友总会 ID 不能为空")
    private Long headquartersId;

    /**
     * 所属母校 ID
     */
    @Schema(description = "所属母校 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属母校 ID 不能为空")
    private Long schoolId;

    /**
     * 创建码/邀请码
     */
    @Schema(description = "创建码/邀请码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "创建码不能为空")
    private Integer createCode;

    /**
     * logo
     */
    @Schema(description = "logo")
    private String logo;

    /**
     * 校友总会描述
     */
    @Schema(description = "校友总会描述")
    private String description;

    /**
     * 联系信息 (JSON 格式)
     */
    @Schema(description = "联系信息")
    private String contactInfo;

    /**
     * 办公地址
     */
    @Schema(description = "办公地址")
    private String address;

    /**
     * 官方网站
     */
    @Schema(description = "官方网站")
    private String website;

    /**
     * 微信公众号
     */
    @Schema(description = "微信公众号")
    private String wechatPublicAccount;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String email;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 成立日期
     */
    @Schema(description = "成立日期")
    private java.time.LocalDate establishedDate;

    /**
     * 级别：1-校级 2-省级 3-国家级 4-国际级
     */
    @Schema(description = "级别：1-校级 2-省级 3-国家级 4-国际级")
    private Integer level;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createdUserId;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Long updatedUserId;

    private static final long serialVersionUID = 1L;
}
