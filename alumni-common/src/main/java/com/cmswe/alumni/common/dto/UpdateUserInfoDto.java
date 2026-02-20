package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 更新用户信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "更新用户信息请求")
public class UpdateUserInfoDto implements Serializable {

    @Schema(description = "用户昵称", example = "张三")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String name;

    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    @Schema(description = "用户背景图", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    private String bgImg;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "微信号", example = "wechat123")
    @Size(max = 50, message = "微信号长度不能超过50个字符")
    private String wxNum;

    @Schema(description = "QQ号", example = "123456789")
    @Size(max = 20, message = "QQ号长度不能超过20个字符")
    private String qqNum;

    @Schema(description = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "籍贯", example = "广东省")
    @Size(max = 100, message = "籍贯长度不能超过100个字符")
    private String originProvince;

    @Schema(description = "当前所在洲", example = "亚洲")
    @Size(max = 50, message = "所在洲长度不能超过50个字符")
    private String curContinent;

    @Schema(description = "当前所在国", example = "中国")
    @Size(max = 50, message = "所在国长度不能超过50个字符")
    private String curCountry;

    @Schema(description = "当前所在省市", example = "广东省")
    @Size(max = 50, message = "所在省市长度不能超过50个字符")
    private String curProvince;

    @Schema(description = "当前所在市区", example = "深圳市")
    @Size(max = 50, message = "所在市区长度不能超过50个字符")
    private String curCity;

    @Schema(description = "当前所在村/区", example = "南山区")
    @Size(max = 50, message = "所在村/区长度不能超过50个字符")
    private String curCounty;

    @Schema(description = "所在地详细地址", example = "科技园南区")
    @Size(max = 200, message = "详细地址长度不能超过200个字符")
    private String address;

    @Schema(description = "所在地纬度", example = "22.5431")
    private BigDecimal latitude;

    @Schema(description = "所在地经度", example = "114.0579")
    private BigDecimal longitude;

    @Schema(description = "星座", example = "1")
    private Integer constellation;

    @Schema(description = "个人签名", example = "这是我的个性签名")
    @Size(max = 200, message = "签名长度不能超过200个字符")
    private String signature;

    @Schema(description = "个人简介", example = "这是关于我的详细描述")
    @Size(max = 1000, message = "描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "个人特长", example = "篮球、编程、摄影")
    @Size(max = 200, message = "个人特长长度不能超过200个字符")
    private String personalSpecialty;

    @Schema(description = "婚姻状况：0-未知，1-未婚，2-已婚，3-离异，4-丧偶", example = "1")
    private Integer maritalStatus;

    @Schema(description = "性别：0-未知，1-男，2-女", example = "1")
    private Integer gender;

    @Schema(description = "证件类型：0-身份证，1-护照", example = "0")
    private Integer identifyType;

    @Schema(description = "证件号", example = "440123199001011234")
    @Size(max = 50, message = "证件号长度不能超过50个字符")
    private String identifyCode;

    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate birthDate;

    @Schema(description = "教育经历列表")
    private List<AlumniEducationDto> alumniEducationList;

    @Schema(description = "工作经历列表")
    private List<WxUserWorkDto> workExperienceList;

    /**
     * DTO 转换为实体对象
     */
    public static WxUserInfo dtoToObj(UpdateUserInfoDto updateUserInfoDto) {
        if (updateUserInfoDto == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        WxUserInfo wxUserInfo = new WxUserInfo();
        BeanUtils.copyProperties(updateUserInfoDto, wxUserInfo);

        return wxUserInfo;
    }
}