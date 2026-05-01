package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "QueryAlumniListNoPrivacyDto", description = "查询校友列表请求（不走ES，不按隐私设置过滤）")
public class QueryAlumniListNoPrivacyDto extends PageRequest implements Serializable {

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String nickname;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phone;

    @Schema(description = "微信号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String wxNum;

    @Schema(description = "QQ号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String qqNum;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String email;

    @Schema(description = "当前所在洲", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curContinent;

    @Schema(description = "当前所在国", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curCountry;

    @Schema(description = "当前所在省", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curProvince;

    @Schema(description = "当前所在市", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String curCity;

    @Schema(description = "星座", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer constellation;

    @Schema(description = "个性签名", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String signature;

    @Schema(description = "性别：0-未知，1-男，2-女", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer gender;

    @Schema(description = "证件号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String identifyCode;

    @Schema(description = "出生日期", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate birthDate;

    @Schema(description = "我的关注：0-全部，1-仅我关注的", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer myFollow;

    @Serial
    private static final long serialVersionUID = 1L;
}
