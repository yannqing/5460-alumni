package com.cmswe.alumni.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 腾讯地图逆地址解析响应
 */
@Data
public class TencentMapGeocodeResponse {

    /**
     * 状态码，0为成功
     */
    private Integer status;

    /**
     * 状态说明
     */
    private String message;

    /**
     * 结果数据
     */
    private Result result;

    @Data
    public static class Result {
        /**
         * 地址信息
         */
        private String address;

        /**
         * 格式化地址
         */
        @JsonProperty("formatted_addresses")
        private FormattedAddresses formattedAddresses;

        /**
         * 地址部件
         */
        @JsonProperty("address_component")
        private AddressComponent addressComponent;

        /**
         * POI信息（附近地标）
         */
        @JsonProperty("ad_info")
        private AdInfo adInfo;
    }

    @Data
    public static class FormattedAddresses {
        /**
         * 推荐地址
         */
        private String recommend;

        /**
         * 粗略地址
         */
        private String rough;
    }

    @Data
    public static class AddressComponent {
        /**
         * 国家
         */
        private String nation;

        /**
         * 省
         */
        private String province;

        /**
         * 市
         */
        private String city;

        /**
         * 区
         */
        private String district;

        /**
         * 街道
         */
        private String street;

        /**
         * 门牌号
         */
        @JsonProperty("street_number")
        private String streetNumber;
    }

    @Data
    public static class AdInfo {
        /**
         * 行政区划代码
         */
        private String adcode;

        /**
         * 国家
         */
        private String nation;

        /**
         * 省
         */
        private String province;

        /**
         * 市
         */
        private String city;

        /**
         * 区
         */
        private String district;

        /**
         * 行政区划中心点
         */
        private Location location;
    }

    @Data
    public static class Location {
        /**
         * 纬度
         */
        private Double lat;

        /**
         * 经度
         */
        private Double lng;
    }
}
