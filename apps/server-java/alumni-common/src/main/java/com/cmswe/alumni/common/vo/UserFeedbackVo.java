package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.UserFeedback;
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
 * 用户反馈VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户反馈信息")
public class UserFeedbackVo implements Serializable {

    /**
     * 反馈ID
     */
    @Schema(description = "反馈ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feedbackId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wxId;

    /**
     * 反馈类型：1-数据问题，2-功能建议，3-Bug反馈，4-使用问题，5-其他
     */
    @Schema(description = "反馈类型：1-数据问题，2-功能建议，3-Bug反馈，4-使用问题，5-其他")
    private Integer feedbackType;

    /**
     * 反馈标题
     */
    @Schema(description = "反馈标题")
    private String feedbackTitle;

    /**
     * 反馈内容
     */
    @Schema(description = "反馈内容")
    private String feedbackContent;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    private String contactInfo;

    /**
     * 附件ID数组（JSON格式）
     */
    @Schema(description = "附件ID数组（JSON格式）")
    private String attachmentIds;

    /**
     * 反馈状态：0-待处理，1-处理中，2-已处理，3-已关闭
     */
    @Schema(description = "反馈状态：0-待处理，1-处理中，2-已处理，3-已关闭")
    private Integer feedbackStatus;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long handlerId;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    /**
     * 处理意见
     */
    @Schema(description = "处理意见")
    private String handleComment;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;

    public static UserFeedbackVo objToVo(UserFeedback userFeedback) {
        if (userFeedback == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }

        UserFeedbackVo vo = new UserFeedbackVo();
        BeanUtils.copyProperties(userFeedback, vo);
        return vo;
    }
}
