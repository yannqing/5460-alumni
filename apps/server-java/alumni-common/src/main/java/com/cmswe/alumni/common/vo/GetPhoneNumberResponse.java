package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取微信用户手机号响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "获取微信用户手机号响应")
public class GetPhoneNumberResponse {

    @Schema(description = "用户绑定的手机号（国外手机号会有区号）")
    private String phoneNumber;

    @Schema(description = "没有区号的手机号")
    private String purePhoneNumber;

    @Schema(description = "区号")
    private String countryCode;
}
