package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.system.UserFeedbackService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.UserFeedbackDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.UserFeedbackVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户反馈控制器
 */
@Slf4j
@Tag(name = "用户反馈")
@RestController
@RequestMapping("/feedback")
public class UserFeedbackController {

    @Resource
    private UserFeedbackService userFeedbackService;

    /**
     * 提交用户反馈
     *
     * @param securityUser 当前登录用户
     * @param feedbackDto  反馈信息
     * @return 反馈结果
     */
    @PostMapping("/submit")
    @Operation(summary = "提交用户反馈")
    public BaseResponse<UserFeedbackVo> submitFeedback(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UserFeedbackDto feedbackDto) {

        log.info("[UserFeedbackController] 用户提交反馈 - 用户ID: {}, 反馈类型: {}",
                securityUser.getWxUser().getWxId(), feedbackDto.getFeedbackType());

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        UserFeedbackVo result = userFeedbackService.submitFeedback(wxId, feedbackDto);

        return ResultUtils.success(Code.SUCCESS, result, "反馈提交成功，感谢您的反馈！");
    }
}
