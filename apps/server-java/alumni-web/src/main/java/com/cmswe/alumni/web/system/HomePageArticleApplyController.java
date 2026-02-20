package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.system.HomePageArticleApplyService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApproveArticleDto;
import com.cmswe.alumni.common.dto.QueryArticleApplyListDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.HomePageArticleApplyVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 首页公众号文章审核 Controller
 */
@Slf4j
@Tag(name = "首页文章审核管理")
@RestController
@RequestMapping("/home-page-article-apply")
public class HomePageArticleApplyController {

    @Resource
    private HomePageArticleApplyService homePageArticleApplyService;

    /**
     * 审核文章
     * @param securityUser 当前登录用户
     * @param approveDto 审核请求参数
     * @return 审核结果
     */
    @PostMapping("/approve")
    @Operation(summary = "审核文章")
    public BaseResponse<Boolean> approveArticle(@AuthenticationPrincipal SecurityUser securityUser,
                                                 @RequestBody @Valid ApproveArticleDto approveDto) {
        // 从当前登录用户中获取审批人信息
        Long approverWxId = securityUser.getWxUser().getWxId();
        // 审批人名称暂时使用openid，后续可以通过wxId查询AlumniInfo获取真实姓名
        String approverName = securityUser.getWxUser().getOpenid();

        boolean result = homePageArticleApplyService.approveArticle(approveDto, approverWxId, approverName);
        String message = approveDto.getApplyStatus() == 1 ? "审核通过" : "审核拒绝";
        return ResultUtils.success(Code.SUCCESS, result, message);
    }

    /**
     * 获取审核记录列表（分页）
     * 支持根据状态筛选：
     * - applyStatus = 0：查询待审核记录
     * - applyStatus = 1：查询审核通过记录
     * - applyStatus = 2：查询审核拒绝记录
     * - applyStatus = null：查询所有状态的记录
     *
     * @param queryDto 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "获取审核记录列表（分页）")
    public BaseResponse<PageVo<HomePageArticleApplyVo>> getApplyList(
            @RequestBody QueryArticleApplyListDto queryDto) {
        PageVo<HomePageArticleApplyVo> pageVo = homePageArticleApplyService.getApplyList(queryDto);
        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }
}
