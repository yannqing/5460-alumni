package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 小程序链接项DTO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "小程序链接项")
public class MiniProgramLinkDto implements Serializable {

    /**
     * 链接项ID
     */
    @Schema(description = "链接项ID")
    private Integer id;

    /**
     * 显示文本
     */
    @Schema(description = "显示文本")
    private String text;

    /**
     * 小程序跳转链接
     */
    @Schema(description = "小程序跳转链接（格式：#小程序://名称/路径）")
    private String url;

    @Serial
    private static final long serialVersionUID = 1L;
}
