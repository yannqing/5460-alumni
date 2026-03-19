package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniAssociationApplication;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 校友会创建申请详情Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniAssociationApplicationDetailVo", description = "校友会创建申请详情返回VO")
public class AlumniAssociationApplicationDetailVo implements Serializable {

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    private String applicationId;

    /**
     * 申请创建的校友会名称
     */
    @Schema(description = "申请创建的校友会名称")
    private String associationName;

    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校ID")
    private String schoolId;

    /**
     * 母校信息
     */
    @Schema(description = "母校信息")
    private SchoolListVo schoolInfo;

    /**
     * 所属校处会ID
     */
    @Schema(description = "所属校处会ID")
    private String platformId;

    /**
     * 负责人微信用户ID
     */
    @Schema(description = "负责人微信用户ID")
    private String chargeWxId;

    /**
     * 负责人姓名
     */
    @Schema(description = "负责人姓名")
    private String chargeName;

    /**
     * 背景图（json 数组）
     */
    @Schema(description = "背景图（json 数组）")
    private String bgImg;

    /**
     * 负责人架构角色
     */
    @Schema(description = "负责人架构角色")
    private String chargeRole;

    /**
     * 联系信息（会长联系方式）
     */
    @Schema(description = "联系信息（会长联系方式）")
    private String contactInfo;

    /**
     * 主要负责人社会职务
     */
    @Schema(description = "主要负责人社会职务")
    private String msocialAffiliation;

    /**
     * 驻会代表姓名
     */
    @Schema(description = "驻会代表姓名")
    private String zhName;

    /**
     * 驻会代表架构角色
     */
    @Schema(description = "驻会代表架构角色")
    private String zhRole;

    /**
     * 驻会代表联系电话
     */
    @Schema(description = "驻会代表联系电话")
    private String zhPhone;

    /**
     * 驻会代表社会职务
     */
    @Schema(description = "驻会代表社会职务")
    private String zhSocialAffiliation;

    /**
     * 常驻地点
     */
    @Schema(description = "常驻地点")
    private String location;

    /**
     * 覆盖区域
     */
    @Schema(description = "覆盖区域")
    private String coverageArea;

    /**
     * 校友会logo
     */
    @Schema(description = "校友会logo")
    private String logo;

    /**
     * 申请理由
     */
    @Schema(description = "申请理由")
    private String applicationReason;

    /**
     * 校友会简介
     */
    @Schema(description = "校友会简介")
    private String associationProfile;

    /**
     * 初始成员列表（JSON格式）
     * 格式：[{"wxId": 123, "name": "张三", "roleId": 1}, {"wxId": 456, "name": "李四", "roleId": 2}]
     */
    @Schema(description = "初始成员列表（JSON格式）")
    private String initialMembers;

    /**
     * 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销
     */
    @Schema(description = "申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销")
    private Integer applicationStatus;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private String reviewerId;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String reviewComment;

    /**
     * 申请时间
     */
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /**
     * 申请材料附件ID数组（JSON格式）
     */
    @Schema(description = "申请材料附件ID数组（JSON格式）")
    private String attachmentIds;

    /**
     * 申请材料附件详情列表
     */
    @Schema(description = "申请材料附件详情列表")
    private List<FilesVo> attachments;

    /**
     * 创建的校友会ID（审核通过后自动创建校友会时填写）
     */
    @Schema(description = "创建的校友会ID")
    private String createdAssociationId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转VO
     */
    public static AlumniAssociationApplicationDetailVo objToVo(AlumniAssociationApplication application) {
        if (application == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "申请对象不能为空");
        }
        AlumniAssociationApplicationDetailVo vo = new AlumniAssociationApplicationDetailVo();
        BeanUtils.copyProperties(application, vo);

        // 处理 Long 类型转 String，避免前端精度丢失
        vo.setApplicationId(String.valueOf(application.getApplicationId()));
        vo.setSchoolId(application.getSchoolId() != null ? String.valueOf(application.getSchoolId()) : null);
        vo.setPlatformId(application.getPlatformId() != null ? String.valueOf(application.getPlatformId()) : null);
        vo.setChargeWxId(application.getChargeWxId() != null ? String.valueOf(application.getChargeWxId()) : null);
        vo.setReviewerId(application.getReviewerId() != null ? String.valueOf(application.getReviewerId()) : null);
        vo.setCreatedAssociationId(application.getCreatedAssociationId() != null ? String.valueOf(application.getCreatedAssociationId()) : null);

        return vo;
    }
}
