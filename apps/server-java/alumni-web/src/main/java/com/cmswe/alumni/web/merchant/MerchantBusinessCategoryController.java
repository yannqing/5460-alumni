package com.cmswe.alumni.web.merchant;

import com.cmswe.alumni.api.system.MerchantBusinessCategoryService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.MerchantBusinessCategoryVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商户经营类目 Controller
 */
@Tag(name = "商户经营类目", description = "商户经营类目及范围相关接口")
@Slf4j
@RestController
@RequestMapping("/merchant/category")
public class MerchantBusinessCategoryController {

    @Resource
    private MerchantBusinessCategoryService merchantBusinessCategoryService;

    /**
     * 获取所有经营类目及范围（树形结构）
     *
     * @return 树形结构列表
     */
    @GetMapping("/tree")
    @Operation(summary = "获取所有经营类目及范围（树形结构）")
    public BaseResponse<List<MerchantBusinessCategoryVo>> listTree() {
        log.info("查询所有商户经营类目树");
        List<MerchantBusinessCategoryVo> tree = merchantBusinessCategoryService.listAllAsTree();
        return ResultUtils.success(Code.SUCCESS, tree, "查询成功");
    }

    /**
     * 按一级类目查询二级服务列表
     *
     * @param parentId 一级类目ID
     * @return 二级服务列表
     */
    @GetMapping("/services")
    @Operation(summary = "按一级类目ID查询二级服务列表")
    public BaseResponse<List<MerchantBusinessCategoryVo>> listServicesByParentId(@RequestParam("parentId") Long parentId) {
        log.info("按一级类目查询二级服务列表 - parentId: {}", parentId);
        List<MerchantBusinessCategoryVo> services = merchantBusinessCategoryService.listServicesByParentId(parentId);
        return ResultUtils.success(Code.SUCCESS, services, "查询成功");
    }
}
