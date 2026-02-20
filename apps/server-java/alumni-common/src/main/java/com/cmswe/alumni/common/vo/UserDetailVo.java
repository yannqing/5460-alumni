
package com.cmswe.alumni.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "用户详情vo", description = "用户信息返回VO")
public class UserDetailVo implements Serializable {

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
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatarUrl;

    /**
     * 用户背景图
     */
    @Schema(description = "用户背景图")
    private String bgImg;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 微信号
     */
    @Schema(description = "微信号")
    private String wxNum;

    /**
     * QQ号
     */
    @Schema(description = "QQ号")
    private String qqNum;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 籍贯
     */
    @Schema(description = "籍贯")
    private String originProvince;

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
     * 当前所在村/区
     */
    @Schema(description = "当前所在村/区")
    private String curCounty;

    /**
     * 所在地详细地址
     */
    @Schema(description = "所在地详细地址")
    private String address;

    /**
     * 所在地纬度
     */
    @Schema(description = "所在地纬度")
    private BigDecimal latitude;

    /**
     * 所在地经度
     */
    @Schema(description = "所在地经度")
    private BigDecimal longitude;

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
     * 个人简介
     */
    @Schema(description = "个人简介")
    private String description;

    /**
     * 个人特长
     */
    @Schema(description = "个人特长")
    private String personalSpecialty;

    /**
     * 婚姻状况：0-未知，1-未婚，2-已婚，3-离异，4-丧偶
     */
    @Schema(description = "婚姻状况：0-未知，1-未婚，2-已婚，3-离异，4-丧偶")
    private Integer maritalStatus;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    /**
     * 证件类型：0-身份证，1-护照
     */
    @Schema(description = "证件类型：0-身份证，1-护照")
    private Integer identifyType;

    /**
     * 证件号
     */
    @Schema(description = "证件号")
    private String identifyCode;

    /**
     * 出生日期
     */
    @Schema(description = "出生日期")
    private LocalDate birthDate;

    /**
     * 用户的教育经历信息
     */
    @Schema(description = "用户的教育经历信息")
    private List<AlumniEducationListVo> alumniEducationList;

    /**
     * 用户的工作经历信息
     */
    @Schema(description = "用户的工作经历信息")
    private List<WxUserWorkVo> workExperienceList;

    /**
     * 用户标签列表
     */
    @Schema(description = "用户标签列表")
    private List<TagVo> tagList;

    @Serial
    private static final long serialVersionUID = 1L;


    public static UserDetailVo objToVo(WxUserInfo wxUser) {
        if (wxUser == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        UserDetailVo vo = new UserDetailVo();
        BeanUtils.copyProperties(wxUser, vo);
        return vo;
    }
}