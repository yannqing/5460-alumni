package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.HomePageArticleApply;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 首页文章审核记录列表 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "HomePageArticleApplyVo", description = "首页文章审核记录列表返回VO")
public class HomePageArticleApplyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "审核记录id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long homeArticleApplyId;

    @Schema(description = "首页文章信息")
    private HomePageArticleVo articleInfo;

    @Schema(description = "审核状态 0-审核中，1-审核通过，2-审核拒绝")
    private Integer applyStatus;

    @Schema(description = "审批人id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appliedWxId;

    @Schema(description = "审批人信息")
    private WxUserInfoVo appliedUserInfo;

    @Schema(description = "审批人名称")
    private String appliedName;

    @Schema(description = "审批意见")
    private String appliedDescription;

    @Schema(description = "审核完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static HomePageArticleApplyVo objToVo(HomePageArticleApply apply) {
        if (apply == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        HomePageArticleApplyVo vo = new HomePageArticleApplyVo();
        BeanUtils.copyProperties(apply, vo);
        return vo;
    }
}
