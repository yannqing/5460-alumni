package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新校处会信息DTO
 */
@Data
@Schema(name = "UpdateLocalPlatformDto", description = "更新校处会信息请求参数")
public class UpdateLocalPlatformDto implements Serializable {

    @Schema(description = "校处会ID")
    @NotNull(message = "校处会ID不能为空")
    private Long platformId;

    @Schema(description = "校处会名称")
    private String platformName;

    @Schema(description = "校处会头像")
    private String avatar;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "管辖范围")
    private String scope;

    @Schema(description = "联系信息")
    private String contactInfo;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "会员数量")
    private Integer memberCount;

    @Schema(description = "当月可发布到首页的文章数量（配额）")
    private Integer monthlyHomepageArticleQuota;

    @Schema(description = "背景图片")
    private String bgImg;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人职务")
    private String contactPosition;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "联系人wxid")
    private Long wxId;

    @Schema(description = "校促会重大事记（JSON格式）")
    private String importantEvents;

    @Schema(description = "小程序链接列表（JSON数组格式）")
    private String miniProgramLinks;

    private static final long serialVersionUID = 1L;
}
