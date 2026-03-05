package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.user.OrganizeArchiTemplateService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.OrganizeArchiTemplateVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 组织架构模板控制器
 */
@Tag(name = "组织架构模板管理")
@RestController
@RequestMapping("/organizeArchiTemplate")
public class OrganizeArchiTemplateController {

    @Resource
    private OrganizeArchiTemplateService organizeArchiTemplateService;

    /**
     * 获取所有组织架构模板列表
     *
     * @param organizeType 组织类型（可选，0-校友会，1-校处会，2-商户）
     * @return 模板列表
     */
    @Operation(summary = "获取所有组织架构模板列表")
    @GetMapping("/list")
    public BaseResponse<List<OrganizeArchiTemplateVo>> getAllTemplates(
            @Parameter(description = "组织类型（可选，0-校友会，1-校处会，2-商户）")
            @RequestParam(value = "organizeType", required = false) Integer organizeType) {

        List<OrganizeArchiTemplateVo> templates = organizeArchiTemplateService.getAllTemplates(organizeType);

        return ResultUtils.success(Code.SUCCESS, templates, "查询成功");
    }
}
