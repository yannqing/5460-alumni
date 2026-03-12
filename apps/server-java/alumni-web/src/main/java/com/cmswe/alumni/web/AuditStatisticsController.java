package com.cmswe.alumni.web;

import com.cmswe.alumni.api.association.AuditStatisticsService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AuditStatisticsVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审核统计控制器
 */
@Tag(name = "审核统计")
@RestController
@RequestMapping("/audit/statistics")
public class AuditStatisticsController {

    @Resource
    private AuditStatisticsService auditStatisticsService;

    /**
     * 获取审核待办数量统计
     *
     * @param securityUser 当前登录用户
     * @return 待办统计结果
     */
    @GetMapping("/todoCount")
    @Operation(summary = "获取当前用户的审核待办数量统计")
    public BaseResponse<AuditStatisticsVo> getAuditTodoCount(
            @AuthenticationPrincipal SecurityUser securityUser) {
        
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.TOKEN_ERROR, null, "用户未登录");
        }

        Long wxId = securityUser.getWxUser().getWxId();
        AuditStatisticsVo statistics = auditStatisticsService.getAuditTodoStatistics(wxId);

        return ResultUtils.success(Code.SUCCESS, statistics, "查询成功");
    }
}
