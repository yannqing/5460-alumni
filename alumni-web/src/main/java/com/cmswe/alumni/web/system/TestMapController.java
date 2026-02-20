package com.cmswe.alumni.web.system;

import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.TencentMapGeocodeResponse;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.utils.TencentMapUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 腾讯地图API测试Controller
 */
@Tag(name = "地图测试")
@RestController
@RequestMapping("/test/map")
public class TestMapController {

    @Resource
    private TencentMapUtils tencentMapUtils;

    /**
     * 测试逆地址解析
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址信息
     */
    @GetMapping("/geocode")
    @Operation(summary = "根据经纬度获取地址信息")
    public BaseResponse<TencentMapGeocodeResponse> testGeocode(
            @Parameter(description = "纬度", example = "39.908823")
            @RequestParam Double latitude,
            @Parameter(description = "经度", example = "116.397470")
            @RequestParam Double longitude
    ) {
        TencentMapGeocodeResponse response = tencentMapUtils.reverseGeocode(latitude, longitude);
        return ResultUtils.success(Code.SUCCESS, response, "查询成功");
    }

    /**
     * 测试获取格式化地址
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址字符串
     */
    @GetMapping("/address")
    @Operation(summary = "获取格式化地址")
    public BaseResponse<String> testGetAddress(
            @Parameter(description = "纬度", example = "39.908823")
            @RequestParam Double latitude,
            @Parameter(description = "经度", example = "116.397470")
            @RequestParam Double longitude
    ) {
        String address = tencentMapUtils.getFormattedAddress(latitude, longitude);
        return ResultUtils.success(Code.SUCCESS, address, "查询成功");
    }

    /**
     * 测试获取省市区
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 省市区信息
     */
    @GetMapping("/location")
    @Operation(summary = "获取省市区信息")
    public BaseResponse<String[]> testGetLocation(
            @Parameter(description = "纬度", example = "39.908823")
            @RequestParam Double latitude,
            @Parameter(description = "经度", example = "116.397470")
            @RequestParam Double longitude
    ) {
        String[] location = tencentMapUtils.getProvinceCityDistrict(latitude, longitude);
        return ResultUtils.success(Code.SUCCESS, location, "查询成功");
    }
}
