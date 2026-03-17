package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.user.UserPrivacyBatchInitService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.BatchInitResultVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户隐私设置批量初始化控制器
 * <p>
 * 用于批量初始化所有用户的隐私设置（针对老用户或初始化失败的用户）
 *
 * @author CMSWE
 * @since 2026-03-17
 */
@Slf4j
@Tag(name = "用户隐私设置批量初始化")
@RestController
@RequestMapping("/system/privacy")
public class UserPrivacyInitController {

    @Resource
    private UserPrivacyBatchInitService userPrivacyBatchInitService;

    /**
     * 批量初始化所有用户的隐私设置
     * <p>
     * 该接口会查询所有未初始化隐私设置的用户，并为他们初始化隐私设置
     * <p>
     * 注意：该接口可能执行时间较长，建议在业务低峰期调用
     *
     * @return 批量初始化结果
     */
    @PostMapping("/batch-init")
    @Operation(summary = "批量初始化所有用户的隐私设置")
    public BaseResponse<BatchInitResultVo> batchInitUserPrivacy() {
        log.info("开始批量初始化用户隐私设置");

        try {
            BatchInitResultVo result = userPrivacyBatchInitService.batchInitAllUsers();

            log.info("批量初始化用户隐私设置完成 - 总用户数: {}, 需要初始化: {}, 成功: {}, 失败: {}",
                    result.getTotalUsers(),
                    result.getNeedInitUsers(),
                    result.getSuccessCount(),
                    result.getFailedCount());

            return ResultUtils.success(Code.SUCCESS, result, "批量初始化完成");

        } catch (Exception e) {
            log.error("批量初始化用户隐私设置失败", e);
            return ResultUtils.failure(Code.FAILURE, null, "批量初始化失败: " + e.getMessage());
        }
    }
}
