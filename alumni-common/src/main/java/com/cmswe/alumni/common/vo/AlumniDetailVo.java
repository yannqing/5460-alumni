package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校友详情VO（继承用户详情VO，添加关注和校友状态字段）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "校友详情vo", description = "校友信息返回VO（包含关注状态和校友认证状态）")
public class AlumniDetailVo extends UserDetailVo implements Serializable {

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

    /**
     * 是否是校友（是否通过校友认证）
     */
    @Schema(description = "是否是校友（是否通过校友认证）")
    private Boolean isAlumni;

    /**
     * 是否是好友（双向关注）
     */
    @Schema(description = "是否是好友（双向关注）")
    private Boolean isFriend;

    @Serial
    private static final long serialVersionUID = 1L;
}
