package com.cmswe.alumni.common.utils;

import com.cmswe.alumni.common.dto.TencentMapGeocodeResponse;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

/**
 * 腾讯地图API工具类
 *
 * 主要功能：
 * 1. 逆地址解析（经纬度 -> 地址）
 * 2. 地址解析（地址 -> 经纬度）
 * 3. 地点搜索
 *
 * 文档：<a href="https://lbs.qq.com/service/webService/webServiceGuide/webServiceGcoder">...</a>
 */
@Slf4j
@Component
public class TencentMapUtils {

    @Value("${tencent.map.key}")
    private String key;

    @Value("${tencent.map.base-url}")
    private String baseUrl;

    @Resource
    private HttpClientUtil httpClientUtil;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 逆地址解析：根据经纬度获取地址信息
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址信息
     */
    public TencentMapGeocodeResponse reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        return reverseGeocode(latitude.doubleValue(), longitude.doubleValue());
    }

    /**
     * 逆地址解析：根据经纬度获取地址信息
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址信息
     */
    public TencentMapGeocodeResponse reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }

        try {
            // 构建请求URL
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/ws/geocoder/v1/")
                    .queryParam("location", latitude + "," + longitude)
                    .queryParam("key", key)
                    .queryParam("get_poi", 0)  // 是否返回周边POI列表：0不返回，1返回
                    .toUriString();

            log.info("[腾讯地图] 逆地址解析请求: latitude={}, longitude={}, url={}", latitude, longitude, url);

            // 发送HTTP请求
            String response = httpClientUtil.get(url, String.class);
            log.debug("[腾讯地图] 逆地址解析响应: {}", response);

            // 解析响应
            TencentMapGeocodeResponse result = objectMapper.readValue(response, TencentMapGeocodeResponse.class);

            // 检查响应状态
            if (result.getStatus() != 0) {
                log.error("[腾讯地图] 逆地址解析失败: status={}, message={}", result.getStatus(), result.getMessage());
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "地址解析失败：" + result.getMessage());
            }

            log.info("[腾讯地图] 逆地址解析成功: address={}", result.getResult().getAddress());
            return result;

        } catch (Exception e) {
            log.error("[腾讯地图] 逆地址解析异常", e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "地址解析失败：" + e.getMessage());
        }
    }

    /**
     * 获取格式化的详细地址
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 详细地址字符串
     */
    public String getFormattedAddress(Double latitude, Double longitude) {
        TencentMapGeocodeResponse response = reverseGeocode(latitude, longitude);
        if (response != null && response.getResult() != null) {
            return response.getResult().getAddress();
        }
        return null;
    }

    /**
     * 获取省市区信息
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 数组 [省, 市, 区]
     */
    public String[] getProvinceCityDistrict(Double latitude, Double longitude) {
        TencentMapGeocodeResponse response = reverseGeocode(latitude, longitude);
        if (response != null && response.getResult() != null) {
            TencentMapGeocodeResponse.AddressComponent component = response.getResult().getAddressComponent();
            if (component != null) {
                return new String[]{
                        component.getProvince(),
                        component.getCity(),
                        component.getDistrict()
                };
            }
        }
        return new String[]{"", "", ""};
    }

    /**
     * 获取省份
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 省份名称
     */
    public String getProvince(Double latitude, Double longitude) {
        TencentMapGeocodeResponse response = reverseGeocode(latitude, longitude);
        if (response != null && response.getResult() != null && response.getResult().getAddressComponent() != null) {
            return response.getResult().getAddressComponent().getProvince();
        }
        return "";
    }

    /**
     * 获取城市
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 城市名称
     */
    public String getCity(Double latitude, Double longitude) {
        TencentMapGeocodeResponse response = reverseGeocode(latitude, longitude);
        if (response != null && response.getResult() != null && response.getResult().getAddressComponent() != null) {
            return response.getResult().getAddressComponent().getCity();
        }
        return "";
    }

    /**
     * 获取区/县
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 区/县名称
     */
    public String getDistrict(Double latitude, Double longitude) {
        TencentMapGeocodeResponse response = reverseGeocode(latitude, longitude);
        if (response != null && response.getResult() != null && response.getResult().getAddressComponent() != null) {
            return response.getResult().getAddressComponent().getDistrict();
        }
        return "";
    }
}
