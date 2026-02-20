package com.cmswe.alumni.web;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cmswe.alumni.api.search.AlumniPlaceService;
import com.cmswe.alumni.api.search.ShopService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.QueryNearbyDto;
import com.cmswe.alumni.common.dto.QueryNearbyShopDto;
import com.cmswe.alumni.common.dto.QueryShopDetailDto;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.NearbyAlumniVo;
import com.cmswe.alumni.common.vo.NearbyPlaceVo;
import com.cmswe.alumni.common.vo.NearbyShopVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopDetailVo;
import com.cmswe.alumni.api.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 附近权益控制器（暂时直接查询数据库，后续替换为 ES 查询）
 *
 * @author CNI Alumni System
 * @since 2025-12-22
 */
@Slf4j
@Tag(name = "附近权益")
@RestController
@RequestMapping("/NearbyBenefits")
public class NearbyBenefitsController {

    @Resource
    private ShopService shopService;

    @Autowired
    @Qualifier("alumniPlaceService")
    private AlumniPlaceService alumniPlaceService;

    @Resource
    private WxUserInfoService wxUserInfoService;

    @Resource
    private UserService userService;

    /**
     * 根据ID查询商铺详情
     *
     * @param queryDto 查询参数（包含商铺ID）
     * @return 商铺详情信息
     */
    @PostMapping("/shops/detail")
    @Operation(summary = "查询商铺详情", description = "根据商铺ID查询商铺的详细信息，包括基本信息、优惠券列表等")
    public BaseResponse<ShopDetailVo> getShopDetail(@Valid @RequestBody QueryShopDetailDto queryDto) {
        ShopDetailVo shopDetail = shopService.getShopDetail(queryDto.getShopId());
        return ResultUtils.success(Code.SUCCESS, shopDetail, "查询成功");
    }

    /**
     * 根据经纬度查询附近权益（通用接口）
     * 支持查询商铺、企业/场所、校友，根据 queryType 区分：
     * - queryType=1：查询附近商铺（必须有优惠券）
     * - queryType=2：查询附近校友企业/场所（正常运营即可）
     * - queryType=3：查询附近校友（包含关注状态）
     *
     * @param securityUser 当前登录用户（可选，用于查询关注状态）
     * @param queryDto     查询参数（包含经纬度、半径、查询类型等）
     * @return 附近权益分页列表（根据 queryType 返回不同类型的数据）
     */
    @PostMapping("/nearby")
    @Operation(summary = "查询附近权益（通用）", description = "根据经纬度查询附近的商铺、企业/场所或校友，queryType=1查询商铺，queryType=2查询企业/场所，queryType=3查询校友（包含关注状态）")
    public BaseResponse<?> getNearbyBenefits(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody QueryNearbyDto queryDto) {

        Integer queryType = queryDto.getQueryType();

        if (queryType == null) {
            return ResultUtils.failure("查询类型不能为空");
        }

        // 获取当前用户ID（可能为null，如果用户未登录）
        Long currentUserId = securityUser != null && securityUser.getWxUser() != null
                ? securityUser.getWxUser().getWxId()
                : null;

        // 如果用户已登录且提供了经纬度信息，更新用户的经纬度
        if (currentUserId != null && queryDto.getLatitude() != null && queryDto.getLongitude() != null) {
            try {
                userService.update(null, new LambdaUpdateWrapper<WxUser>()
                        .eq(WxUser::getWxId, currentUserId)
                        .set(WxUser::getLatitude, queryDto.getLatitude())
                        .set(WxUser::getLongitude, queryDto.getLongitude()));
                log.debug("更新用户经纬度成功 - 用户ID: {}, 经度: {}, 纬度: {}",
                        currentUserId, queryDto.getLongitude(), queryDto.getLatitude());
            } catch (Exception e) {
                // 更新失败不影响主流程，只记录日志
                log.error("更新用户经纬度失败 - 用户ID: {}, 经度: {}, 纬度: {}",
                        currentUserId, queryDto.getLongitude(), queryDto.getLatitude(), e);
            }
        }

        switch (queryType) {
            case 1:
                // 查询附近商铺（必须有优惠券）
                QueryNearbyShopDto shopQueryDto = new QueryNearbyShopDto();
                shopQueryDto.setLatitude(queryDto.getLatitude());
                shopQueryDto.setLongitude(queryDto.getLongitude());
                shopQueryDto.setRadius(queryDto.getRadius());
                shopQueryDto.setShopName(queryDto.getName());
                shopQueryDto.setIsRecommended(queryDto.getIsRecommended());
                shopQueryDto.setCurrent(queryDto.getCurrent());
                shopQueryDto.setPageSize(queryDto.getPageSize());

                PageVo<NearbyShopVo> shopPageVo = shopService.getNearbyShops(shopQueryDto);
                return ResultUtils.success(Code.SUCCESS, shopPageVo, "查询成功");

            case 2:
                // 查询附近校友企业/场所（正常运营即可）
                PageVo<NearbyPlaceVo> placePageVo = alumniPlaceService.getNearbyPlaces(queryDto);
                return ResultUtils.success(Code.SUCCESS, placePageVo, "查询成功");

            case 3:
                // 查询附近校友（包含关注状态）
                PageVo<NearbyAlumniVo> alumniPageVo = wxUserInfoService.getNearbyAlumni(queryDto, currentUserId);
                return ResultUtils.success(Code.SUCCESS, alumniPageVo, "查询成功");

            default:
                return ResultUtils.failure("不支持的查询类型，queryType只能为1、2或3");
        }
    }
}
