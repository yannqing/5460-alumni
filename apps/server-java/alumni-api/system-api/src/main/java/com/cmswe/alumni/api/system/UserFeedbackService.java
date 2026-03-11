package com.cmswe.alumni.api.system;

import com.cmswe.alumni.common.dto.UserFeedbackDto;
import com.cmswe.alumni.common.vo.UserFeedbackVo;

/**
 * 用户反馈服务接口
 */
public interface UserFeedbackService {

    /**
     * 提交用户反馈
     *
     * @param wxId        用户ID
     * @param feedbackDto 反馈信息
     * @return 反馈详情
     */
    UserFeedbackVo submitFeedback(Long wxId, UserFeedbackDto feedbackDto);
}
