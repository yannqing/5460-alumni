package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniAssociation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniAssociationDetailVo", description = "校友会信息列表返回VO")
public class AlumniAssociationDetailVo implements Serializable {


    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    private String associationName;

    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校信息")
    private SchoolListVo schoolInfo;

    /**
     * 所属校处会ID
     */
    @Schema(description = "所属校处会")
    private LocalPlatformDetailVo platform;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息")
    private String contactInfo;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点")
    private String location;

    /**
     * 会员数量
     */
    @Schema(description = "会员数量")
    private Integer memberCount;

    /**
     * logo
     */
    @Schema(description = "logo")
    private String logo;

    /**
     * 背景图（json 数组）
     */
    @Schema(description = "背景图（json 数组）")
    private String bgImg;

    /**
     * 当前用户加入状态：1-已加入（成员表中存在且状态正常） null-未加入
     */
    @Schema(description = "当前用户加入状态：1-已加入 null-未加入")
    private Integer applicationStatus;

    /**
     * 活动列表
     */
    @Schema(description = "活动列表")
    private List<ActivityListVo> activityList;

    /**
     * 企业列表
     */
    @Schema(description = "企业列表")
    private List<AlumniPlaceListVo> enterpriseList;


    @Serial
    private static final long serialVersionUID = 1L;

    public static AlumniAssociationDetailVo objToVo(AlumniAssociation alumniAssociation) {
        if (alumniAssociation == null) {
            return null;
        }
        AlumniAssociationDetailVo alumniAssociationListVo = new AlumniAssociationDetailVo();
        BeanUtils.copyProperties(alumniAssociation, alumniAssociationListVo);
        return alumniAssociationListVo;
    }
}