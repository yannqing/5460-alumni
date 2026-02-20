package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Page<UserListResponse>", description = "用户列表响应数据格式")
public class UserListResponse implements Serializable {

    /**
     * 用户id
     */
    @Schema(description = "用户id")
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
    @Schema(description = "用户id")
    private Integer gender;

    /**
     * 用户标签列表
     */
    @Schema(description = "用户标签列表")
    private List<TagVo> tagList;

    /**
     * 是否在线
     */
    @Schema(description = "是否在线")
    private Boolean isOnline;

    /**
     * 主要教育经历
     */
    @Schema(description = "主要教育经历")
    private AlumniEducationListVo primaryEducation;

    /**
     * 是否是校友（是否通过校友认证）
     */
    @Schema(description = "是否是校友（是否通过校友认证）")
    private Boolean isAlumni;

    @Serial
    private static final long serialVersionUID = 1L;

    public static UserListResponse ObjToVo(WxUserInfo wxUserInfo) {
        if (wxUserInfo == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        UserListResponse userListResponse = new UserListResponse();
        BeanUtils.copyProperties(wxUserInfo, userListResponse);
        return userListResponse;
    }
}
