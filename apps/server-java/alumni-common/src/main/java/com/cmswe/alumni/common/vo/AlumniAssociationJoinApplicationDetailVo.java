package com.cmswe.alumni.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 校友会加入申请详情响应VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "校友会加入申请详情响应")
public class AlumniAssociationJoinApplicationDetailVo implements Serializable {

    /**
     * 申请ID
     */
    @Schema(description = "申请ID")
    private String applicationId;

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID")
    private String alumniAssociationId;

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    private String alumniAssociationName;

    // 校友会详细信息
    /**
     * 所属母校ID
     */
    @Schema(description = "所属母校ID")
    private Long associationSchoolId;

    /**
     * 所属校处会ID
     */
    @Schema(description = "所属校处会ID")
    private Long platformId;

    /**
     * 认证标识（0-未认证，1-校友总会，2-校促会，3-校友总会）
     */
    @Schema(description = "认证标识（0-未认证，1-校友总会，2-校促会，3-校友总会）")
    private Integer certificationFlag;

    /**
     * 成员身份（0-会员单位 1-理事单位）
     */
    @Schema(description = "成员身份（0-会员单位 1-理事单位）")
    private Integer role;

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
     * 当月可发布到首页的文章数量（配额）
     */
    @Schema(description = "当月可发布到首页的文章数量（配额）")
    private Integer monthlyHomepageArticleQuota;

    /**
     * logo
     */
    @Schema(description = "logo")
    private String logo;

    /**
     * 校友会简介
     */
    @Schema(description = "校友会简介")
    private String associationProfile;

    /**
     * 主要负责人微信用户ID
     */
    @Schema(description = "主要负责人微信用户ID")
    private Long chargeWxId;

    /**
     * 主要负责人姓名
     */
    @Schema(description = "主要负责人姓名")
    private String chargeName;

    /**
     * 主要负责人架构角色
     */
    @Schema(description = "主要负责人架构角色")
    private String chargeRole;

    /**
     * 主要负责人社会职务
     */
    @Schema(description = "主要负责人社会职务")
    private String chargeSocialAffiliation;

    /**
     * 驻会代表微信用户ID
     */
    @Schema(description = "驻会代表微信用户ID")
    private Long zhWxId;

    /**
     * 驻会代表姓名
     */
    @Schema(description = "驻会代表姓名")
    private String zhName;

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
     * 背景图（json 数组）
     */
    @Schema(description = "背景图（json 数组）")
    private String bgImg;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

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

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String name;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String identifyCode;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 申请理由
     */
    @Schema(description = "申请理由")
    private String applicationReason;

    /**
     * 附件文件列表
     */
    @Schema(description = "附件文件列表")
    private List<FilesVo> attachmentFiles;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private String schoolId;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolName;

    /**
     * 入学年份
     */
    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    /**
     * 毕业年份
     */
    @Schema(description = "毕业年份")
    private Integer graduationYear;

    /**
     * 院系
     */
    @Schema(description = "院系")
    private String department;

    /**
     * 专业
     */
    @Schema(description = "专业")
    private String major;

    /**
     * 班级
     */
    @Schema(description = "班级")
    private String className;

    /**
     * 学历层次
     */
    @Schema(description = "学历层次")
    private String educationLevel;

    /**
     * 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销
     */
    @Schema(description = "申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销")
    private Integer applicationStatus;

    /**
     * 申请状态文本
     */
    @Schema(description = "申请状态文本")
    private String applicationStatusText;

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
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 获取申请状态文本
     */
    public static String getApplicationStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已撤销";
            default -> "未知";
        };
    }
}
