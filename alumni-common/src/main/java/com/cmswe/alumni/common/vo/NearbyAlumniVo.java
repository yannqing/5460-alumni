package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 附近校友VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "NearbyAlumniVo", description = "附近校友信息返回VO")
public class NearbyAlumniVo implements Serializable {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wxId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String name;

    /**
     * 用户头像 URL
     */
    @Schema(description = "用户头像")
    private String avatarUrl;

    /**
     * 当前所在洲
     */
    @Schema(description = "当前所在洲")
    private String curContinent;

    /**
     * 当前所在国
     */
    @Schema(description = "当前所在国")
    private String curCountry;

    /**
     * 当前所在省市
     */
    @Schema(description = "当前所在省市")
    private String curProvince;

    /**
     * 当前所在市区
     */
    @Schema(description = "当前所在市区")
    private String curCity;

    /**
     * 星座
     */
    @Schema(description = "星座")
    private Integer constellation;

    /**
     * 个性签名
     */
    @Schema(description = "个性签名")
    private String signature;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    /**
     * 用户标签列表
     */
    @Schema(description = "用户标签列表")
    private List<TagVo> tagList;

    /**
     * 距离（公里）
     */
    @Schema(description = "距离（公里）")
    private BigDecimal distance;

    /**
     * 关注状态：null-未关注，1-正常关注，2-特别关注，3-免打扰
     */
    @Schema(description = "关注状态：null-未关注，1-正常关注，2-特别关注，3-免打扰")
    private Integer followStatus;

    /**
     * 是否已关注
     */
    @Schema(description = "是否已关注")
    private Boolean isFollowed;

    @Serial
    private static final long serialVersionUID = 1L;
}
