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

/**
 * 校友会创建申请列表Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniAssociationApplicationListVo", description = "校友会创建申请列表返回VO")
public class AlumniAssociationApplicationListVo implements Serializable {

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
     * 负责人架构角色
     */
    @Schema(description = "负责人架构角色")
    private String chargeRole;

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
     * 创建的校友会ID（审核通过后自动创建校友会时填写）
     */
    @Schema(description = "创建的校友会ID")
    private String createdAssociationId;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象转VO
     */
    public static AlumniAssociationApplicationListVo objToVo(AlumniAssociationApplication application) {
        if (application == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "申请对象不能为空");
        }
        AlumniAssociationApplicationListVo vo = new AlumniAssociationApplicationListVo();
        BeanUtils.copyProperties(application, vo);

        // 处理 Long 类型转 String，避免前端精度丢失
        vo.setApplicationId(String.valueOf(application.getApplicationId()));
        vo.setPlatformId(application.getPlatformId() != null ? String.valueOf(application.getPlatformId()) : null);
        vo.setChargeWxId(application.getChargeWxId() != null ? String.valueOf(application.getChargeWxId()) : null);
        vo.setReviewerId(application.getReviewerId() != null ? String.valueOf(application.getReviewerId()) : null);
        vo.setCreatedAssociationId(application.getCreatedAssociationId() != null ? String.valueOf(application.getCreatedAssociationId()) : null);

        return vo;
    }
}
