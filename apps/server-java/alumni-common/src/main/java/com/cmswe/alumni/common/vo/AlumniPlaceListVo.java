package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniPlace;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 校友企业/场所列表VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniPlaceListVo", description = "校友企业/场所列表信息返回VO")
public class AlumniPlaceListVo implements Serializable {

    /**
     * 场所/企业ID
     */
    @Schema(description = "场所/企业ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long placeId;

    /**
     * 场所/企业名称
     */
    @Schema(description = "场所/企业名称")
    private String placeName;

    /**
     * 类型：1-企业 2-场所
     */
    @Schema(description = "类型：1-企业 2-场所")
    private Integer placeType;

    /**
     * 所属校友ID
     */
    @Schema(description = "所属校友ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long alumniId;

    /**
     * 所属校友会ID
     */
    @Schema(description = "所属校友会ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long alumniAssociationId;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 区县
     */
    @Schema(description = "区县")
    private String district;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @Schema(description = "经度")
    private BigDecimal longitude;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String contactEmail;

    /**
     * 营业/办公时间
     */
    @Schema(description = "营业/办公时间")
    private String businessHours;

    /**
     * 图片URL数组（JSON格式）
     */
    @Schema(description = "图片URL数组（JSON格式）")
    private String images;

    /**
     * logo URL
     */
    @Schema(description = "logo URL")
    private String logo;

    /**
     * 简介
     */
    @Schema(description = "简介")
    private String description;

    /**
     * 成立时间
     */
    @Schema(description = "成立时间")
    private LocalDate establishedTime;

    /**
     * 状态：0-停业/关闭 1-正常运营 2-筹建中
     */
    @Schema(description = "状态：0-停业/关闭 1-正常运营 2-筹建中")
    private Integer status;

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核失败
     */
    @Schema(description = "审核状态：0-待审核 1-审核通过 2-审核失败")
    private Integer reviewStatus;

    /**
     * 校友会推荐：0-否 1-是
     */
    @Schema(description = "校友会推荐：0-否 1-是")
    private Integer isRecommended;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long viewCount;

    /**
     * 点击次数
     */
    @Schema(description = "点击次数")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long clickCount;

    /**
     * 评分（0-5分）
     */
    @Schema(description = "评分（0-5分）")
    private BigDecimal ratingScore;

    /**
     * 评价数量
     */
    @Schema(description = "评价数量")
    private Integer ratingCount;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转换为VO
     */
    public static AlumniPlaceListVo objToVo(AlumniPlace alumniPlace) {
        if (alumniPlace == null) {
            return null;
        }
        AlumniPlaceListVo vo = new AlumniPlaceListVo();
        BeanUtils.copyProperties(alumniPlace, vo);
        return vo;
    }
}
