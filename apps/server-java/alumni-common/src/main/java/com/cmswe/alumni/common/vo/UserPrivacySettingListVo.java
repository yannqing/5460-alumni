package com.cmswe.alumni.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.cmswe.alumni.common.entity.UserPrivacySetting;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrivacySettingListVo implements Serializable {
    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private String userPrivacySettingId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String wxId;

    /**
     * 字段名称
     */
    @Schema(description = "字段名称")
    private String fieldName;

    /**
     * 字段代码
     */
    @Schema(description = "字段代码")
    private String fieldCode;

    /**
     * 可见性: 0 不可见；1 可见
     */
    @Schema(description = "可见性: 0 不可见；1 可见")
    private Integer visibility;

    /**
     * 用户隐私的类型（1用户个人信息，2用户的企业信息，3用户的校友场所信息，4用户的校友会信息）
     */
    @Schema(description = "用户隐私的类型（1用户个人信息，2用户的企业信息，3用户的校友场所信息，4用户的校友会信息）")
    private Integer type;

    /**
     * 是否可被搜索：0-否，1-是
     */
    @Schema(description = "是否可被搜索：0-否，1-是")
    private Integer searchable;

    @Serial
    private static final long serialVersionUID = 1L;

    public static UserPrivacySettingListVo objToVo(UserPrivacySetting userPrivacySetting) {
        if (userPrivacySetting == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        UserPrivacySettingListVo vo = new UserPrivacySettingListVo();
        BeanUtils.copyProperties(userPrivacySetting, vo);
        return vo;
    }
}
