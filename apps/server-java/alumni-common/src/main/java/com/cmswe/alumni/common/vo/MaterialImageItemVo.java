package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 申请资料中的单张图片（商户/门店：Logo、营业执照、背景图、门店图等）
 */
@Data
@Schema(description = "申请资料图片项")
public class MaterialImageItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "类型：LOGO / LICENSE / BACKGROUND / SHOP_LOGO / SHOP_IMAGE 等")
    private String kind;

    @Schema(description = "展示文案")
    private String label;

    /**
     * 与库表及现有 VO 一致：可为相对路径或完整 URL，由前端拼接文件域名
     */
    @Schema(description = "图片地址（相对路径或完整 URL）")
    private String url;
}
