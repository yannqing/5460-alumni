package com.cmswe.alumni.web.AlumniAssociataion;

import com.cmswe.alumni.api.association.AlumniAssociationApplicationService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ApplyCreateAlumniAssociationDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 校友会创建申请控制器
 */
@Tag(name = "校友会创建申请")
@RestController
@RequestMapping("/AlumniAssociationApplication")
public class AlumniAssociationApplicationController {

    @Resource
    private AlumniAssociationApplicationService alumniAssociationApplicationService;

    /**
     * 申请创建校友会
     *
     * @param securityUser 当前登录用户
     * @param applyDto     申请信息
     * @return 申请结果
     */
    @PostMapping("/apply")
    @Operation(summary = "申请创建校友会")
    public BaseResponse<Boolean> applyToCreateAssociation(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApplyCreateAlumniAssociationDto applyDto) {

        // 从当前登录用户中获取用户ID
        Long wxId = securityUser.getWxUser().getWxId();

        boolean result = alumniAssociationApplicationService.applyToCreateAssociation(wxId, applyDto);

        if (result) {
            return ResultUtils.success(Code.SUCCESS, true, "校友会创建申请提交成功，请等待审核");
        } else {
            return ResultUtils.failure(Code.FAILURE, false, "校友会创建申请提交失败");
        }
    }

    //
}
