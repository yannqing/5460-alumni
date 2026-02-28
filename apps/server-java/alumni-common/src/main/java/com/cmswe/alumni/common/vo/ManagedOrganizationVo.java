package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理的组织 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ManagedOrganizationVo", description = "管理的组织返回VO")
public class ManagedOrganizationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID（校友会ID或校促会ID）")
    @JsonSerialize(using = ToStringSerializer.class)
    private String organizationId;

    @Schema(description = "组织名称")
    private String organizationName;

    @Schema(description = "组织头像/Logo")
    private String avatar;

    @Schema(description = "会员数量")
    private Integer memberCount;

    @Schema(description = "当月可发布到首页的文章数量（配额）")
    private Integer monthlyHomepageArticleQuota;
}
