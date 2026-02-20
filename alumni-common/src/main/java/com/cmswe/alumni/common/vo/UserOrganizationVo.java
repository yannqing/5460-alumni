package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户组织信息VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户组织信息")
public class UserOrganizationVo implements Serializable {

    /**
     * 组织类型（1-校处会，2-校友会，3-商户）
     */
    @Schema(description = "组织类型（1-校处会，2-校友会，3-商户）")
    private Integer type;

    /**
     * 组织 ID
     */
    @Schema(description = "组织 ID")
    private String organizeId;

    /**
     * 组织信息（根据 type 不同，可能是 LocalPlatformListVo、AlumniAssociationListVo 或 MerchantListVo）
     */
    @Schema(description = "组织信息")
    private Object organizationInfo;

    @Serial
    private static final long serialVersionUID = 1L;
}
