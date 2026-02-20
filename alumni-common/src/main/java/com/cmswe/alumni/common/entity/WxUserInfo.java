package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户基本信息表
 * @TableName wx_user_info
 */
@TableName(value = "wx_user_info")
@Data
public class WxUserInfo implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 用户昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 真实姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 用户头像 URL
     */
    @TableField(value = "avatar_url")
    private String avatarUrl;

    /**
     * 用户背景图
     */
    @TableField(value = "bg_img")
    private String bgImg;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 微信号
     */
    @TableField(value = "wx_num")
    private String wxNum;

    /**
     * QQ号
     */
    @TableField(value = "qq_num")
    private String qqNum;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 籍贯
     */
    @TableField(value = "origin_province")
    private String originProvince;

    /**
     * 当前所在洲
     */
    @TableField(value = "cur_continent")
    private String curContinent;

    /**
     * 当前所在国
     */
    @TableField(value = "cur_country")
    private String curCountry;

    /**
     * 当前所在省市
     */
    @TableField(value = "cur_province")
    private String curProvince;

    /**
     * 当前所在市区
     */
    @TableField(value = "cur_city")
    private String curCity;

    /**
     * 当前所在村/区
     */
    @TableField(value = "cur_county")
    private String curCounty;

    /**
     * 所在地详细地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * 所在地纬度
     */
    @TableField(value = "latitude")
    private BigDecimal latitude;

    /**
     * 所在地经度
     */
    @TableField(value = "longitude")
    private BigDecimal longitude;

    /**
     * 星座
     */
    @TableField(value = "constellation")
    private Integer constellation;

    /**
     * 个性签名
     */
    @TableField(value = "signature")
    private String signature;

    /**
     * 个人简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 个人特长
     */
    @TableField(value = "personal_specialty")
    private String personalSpecialty;

    /**
     * 婚姻状况：0-未知，1-未婚，2-已婚，3-离异，4-丧偶
     */
    @TableField(value = "marital_status")
    private Integer maritalStatus;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 证件类型：0-身份证，1-护照
     */
    @TableField(value = "identify_type")
    private Integer identifyType;

    /**
     * 证件号
     */
    @TableField(value = "identify_code")
    private String identifyCode;

    /**
     * 出生日期
     */
    @TableField(value = "birth_date")
    private LocalDate birthDate;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
