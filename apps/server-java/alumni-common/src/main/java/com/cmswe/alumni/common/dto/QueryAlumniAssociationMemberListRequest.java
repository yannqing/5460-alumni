package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryAlumniAssociationMemberListRequest extends PageRequest implements Serializable {

    /**
     * 校友会id
     */
    @Schema(description = "校友会id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校友会 id 不能为空")
    private Long alumniAssociationId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String nickname;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    /**
     * 手机号
     */
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phone;

    /**
     * 微信号
     */
    @Schema(description = "微信号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String wxNum;

    /**
     * QQ号
     */
    @Schema(description = "QQ号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String qqNum;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String email;

    /**
     * 当前所在洲
     */
    @Schema(description = "当前所在洲", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curContinent;

    /**
     * 当前所在国
     */
    @Schema(description = "当前所在国", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curCountry;

    /**
     * 当前所在省市
     */
    @Schema(description = "当前所在省市", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curProvince;

    /**
     * 当前所在市区
     */
    @Schema(description = "当前所在市区", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curCity;

    /**
     * 星座
     */
    @Schema(description = "星座", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer constellation;

    /**
     * 个性签名
     */
    @Schema(description = "个性签名", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String signature;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer gender;

    /**
     * 证件号
     */
    @Schema(description = "证件号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String identifyCode;

    /**
     * 出生日期
     */
    @Schema(description = "出生日期", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate birthDate;

    @Serial
    private static final long serialVersionUID = 1L;
}
