package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.system.BannerService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.CreateBannerDto;
import com.cmswe.alumni.common.dto.QueryBannerListDto;
import com.cmswe.alumni.common.dto.UpdateBannerDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.BannerVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图管理 Controller
 * 用于后台管理轮播图的 CRUD 操作
 */
@Slf4j
@Tag(name = "轮播图管理")
@RestController
@RequestMapping("/banner-management")
public class BannerManagementController {

    @Resource
    private BannerService bannerService;

    /**
     * 分页查询轮播图列表
     * @param queryDto 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询轮播图列表")
    public BaseResponse<PageVo<BannerVo>> getBannerPage(@RequestBody QueryBannerListDto queryDto) {
        PageVo<BannerVo> bannerPage = bannerService.getBannerPage(queryDto);
        return ResultUtils.success(Code.SUCCESS, bannerPage, "分页查询成功");
    }

    /**
     * 根据ID查询轮播图详情
     * @param id 轮播图ID
     * @return 轮播图详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询轮播图详情")
    public BaseResponse<BannerVo> getBannerById(@PathVariable Long id) {
        BannerVo banner = bannerService.getBannerById(id);
        return ResultUtils.success(Code.SUCCESS, banner, "查询成功");
    }

    /**
     * 新增轮播图
     * @param createDto 新增轮播图参数
     * @return 轮播图ID
     */
    @PostMapping("/create")
    @Operation(summary = "新增轮播图")
    public BaseResponse<String> createBanner(@Valid @RequestBody CreateBannerDto createDto) {
        Long bannerId = bannerService.createBanner(createDto);
        return ResultUtils.success(Code.SUCCESS, String.valueOf(bannerId), "新增成功");
    }

    /**
     * 更新轮播图
     * @param updateDto 更新轮播图参数
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新轮播图")
    public BaseResponse<Boolean> updateBanner(@Valid @RequestBody UpdateBannerDto updateDto) {
        Boolean result = bannerService.updateBanner(updateDto);
        return ResultUtils.success(Code.SUCCESS, result, "更新成功");
    }

    /**
     * 删除轮播图
     * @param id 轮播图ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除轮播图")
    public BaseResponse<Boolean> deleteBanner(@PathVariable Long id) {
        Boolean result = bannerService.deleteBanner(id);
        return ResultUtils.success(Code.SUCCESS, result, "删除成功");
    }
}
