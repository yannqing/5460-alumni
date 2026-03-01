package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 审核校友总会请求 DTO
 */
@Data
@Schema(description = "审核校友总会请求 DTO")
public class AuditHeadquartersRequest implements Serializable {

    /**
     * 校友总会 ID
     */
    @Schema(description = "校友总会 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校友总会 ID 不能为空")
    private Long headquartersId;

    /**
     * 审核状态：1-通过 2-驳回
     */
    @Schema(description = "审核状态：1-通过 2-驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核状态不能为空")
    private Integer approvalStatus;

    /**
     * 校友总会名称 (仅通过时可选更新)
     */
    @Schema(description = "校友总会名称")
    private String headquartersName;

    /**
     * 校友总会描述
     */
    @Schema(description = "校友总会描述")
    private String description;

    /**
     * 联系信息 (JSON)
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
    private LocalDate establishedDate;

    /**
     * 级别：1-校级 2-省级 3-国家级 4-国际级
     */
    @Schema(description = "级别：1-校级 2-省级 3-国家级 4-国际级")
    private Integer level;

    private static final long serialVersionUID = 1L;
}
