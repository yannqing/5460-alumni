package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.WxUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户基本信息VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "WxUserInfoVo", description = "用户基本信息VO")
public class WxUserInfoVo implements Serializable {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String wxId;

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
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转VO
     *
     * @param wxUserInfo 用户实体
     * @return WxUserInfoVo
     */
    public static WxUserInfoVo objToVo(WxUserInfo wxUserInfo) {
        if (wxUserInfo == null) {
            return null;
        }
        WxUserInfoVo wxUserInfoVo = new WxUserInfoVo();
        BeanUtils.copyProperties(wxUserInfo, wxUserInfoVo);
        return wxUserInfoVo;
    }
}
