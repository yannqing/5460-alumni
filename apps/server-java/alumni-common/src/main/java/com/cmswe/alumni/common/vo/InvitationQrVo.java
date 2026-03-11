package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邀请二维码响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邀请二维码响应")
public class InvitationQrVo {

    @Schema(description = "邀请人wxid")
    private String wxId;

    @Schema(description = "小程序AppID（从配置读取）")
    private String appId;

    @Schema(description = "邀请二维码图片（Base64，可直接用于img标签src）")
    private String qrCodeBase64;
}
