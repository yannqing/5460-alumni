package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniPlaceApplication;
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
import java.time.LocalDateTime;

/**
 * 校友企业/场所申请列表 VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniPlaceApplicationVo", description = "校友企业/场所申请信息返回VO")
public class AlumniPlaceApplicationVo implements Serializable {

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicationId;

    /**
     * 申请人ID
     */
    @Schema(description = "申请人ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @Schema(description = "申请人姓名")
    private String applicantName;

    /**
     * 申请人联系电话
     */
    @Schema(description = "申请人联系电话")
    private String applicantPhone;

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
     * 申请状态：0-待审核 1-审核通过 2-审核拒绝 3-已撤销
     */
    @Schema(description = "申请状态：0-待审核 1-审核通过 2-审核拒绝 3-已撤销")
    private Integer applicationStatus;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewUserId;

    /**
     * 审核人姓名
     */
    @Schema(description = "审核人姓名")
    private String reviewUserName;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    /**
     * 审核备注
     */
    @Schema(description = "审核备注")
    private String reviewRemark;

    /**
     * 审核通过后创建的场所/企业ID
     */
    @Schema(description = "审核通过后创建的场所/企业ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long placeId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转换为VO
     */
    public static AlumniPlaceApplicationVo objToVo(AlumniPlaceApplication application) {
        if (application == null) {
            return null;
        }
        AlumniPlaceApplicationVo vo = new AlumniPlaceApplicationVo();
        BeanUtils.copyProperties(application, vo);
        return vo;
    }
}
