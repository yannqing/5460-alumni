package com.cmswe.alumni.web;

import com.cmswe.alumni.api.user.UserFavoriteService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ToggleMerchantFavoriteDto;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.MerchantFavoriteItemVo;
import com.cmswe.alumni.common.vo.MerchantFavoriteToggleVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收藏控制器
 */
@Slf4j
@Tag(name = "用户收藏")
@RestController
@RequestMapping("/favorite")
public class UserFavoriteController {

    @Resource
    private UserFavoriteService userFavoriteService;

    /**
     * 切换商户收藏状态。
     * 第一次调用：收藏；第二次调用：取消收藏；第三次调用：再次收藏。
     */
    @PostMapping("/merchant/toggle")
    @Operation(summary = "切换商户收藏状态（收藏/取消收藏）")
    public BaseResponse<MerchantFavoriteToggleVo> toggleMerchantFavorite(
            @Valid @RequestBody ToggleMerchantFavoriteDto dto) {
        Long wxId = parseId(dto.getWxId(), "用户ID");
        Long merchantId = parseId(dto.getMerchantId(), "商户ID");
        MerchantFavoriteToggleVo result = userFavoriteService.toggleMerchantFavorite(wxId, merchantId);
        String msg = Boolean.TRUE.equals(result.getFavorited()) ? "收藏成功" : "取消收藏成功";
        return ResultUtils.success(Code.SUCCESS, result, msg);
    }

    @GetMapping("/merchant/list")
    @Operation(summary = "查询用户收藏的商户列表")
    public BaseResponse<PageVo<MerchantFavoriteItemVo>> listMerchantFavorites(
            @RequestParam String wxId,
            @RequestParam(required = false, defaultValue = "1") Long current,
            @RequestParam(required = false, defaultValue = "10") Long pageSize) {
        Long userId = parseId(wxId, "用户ID");
        PageVo<MerchantFavoriteItemVo> result = userFavoriteService.listMerchantFavorites(userId, current, pageSize);
        return ResultUtils.success(Code.SUCCESS, result, "查询成功");
    }

    private Long parseId(String id, String fieldName) {
        try {
            return Long.parseLong(id);
        } catch (Exception ex) {
            throw new BusinessException(fieldName + "格式错误，请传字符串形式雪花ID");
        }
    }
}
