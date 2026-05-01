package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Page<UserListNoPrivacyResponse>", description = "用户列表响应（不走隐私过滤）")
public class UserListNoPrivacyResponse implements Serializable {

    @Schema(description = "用户id")
    private String wxId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @Schema(description = "当前所在洲")
    private String curContinent;

    @Schema(description = "当前所在国")
    private String curCountry;

    @Schema(description = "当前所在省")
    private String curProvince;

    @Schema(description = "当前所在市")
    private String curCity;

    @Schema(description = "主要教育经历")
    private AlumniEducationListVo primaryEducation;

    @Schema(description = "认证标识（0-未认证，1-校友总会认证，2-校促会认证，3-校友会认证）")
    private Integer certificationFlag;

    @Schema(description = "是否已关注")
    private Boolean isFollowed;

    @Schema(description = "关注状态（1-正常关注，2-特别关注，3-免打扰）")
    private Integer followStatus;

    @Serial
    private static final long serialVersionUID = 1L;
}
