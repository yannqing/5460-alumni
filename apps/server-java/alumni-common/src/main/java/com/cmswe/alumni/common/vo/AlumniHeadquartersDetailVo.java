package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniHeadquartersDetailVo", description = "校友总会详情返回VO")
public class AlumniHeadquartersDetailVo implements Serializable {

    /**
     * 校友总会ID
     */
    @Schema(description = "校友总会ID")
    private String headquartersId;

    /**
     * 校友总会名称
     */
    @Schema(description = "校友总会名称")
    private String headquartersName;

    /**
     * 所属母校信息
     */
    @Schema(description = "所属母校信息")
    private SchoolListVo schoolInfo;

    /**
     * 校友总会描述
     */
    @Schema(description = "校友总会描述")
    private String description;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息")
    private String contactInfo;

    /**
     * 办公地址
     */
    @Schema(description = "办公地址")
    private String address;

    /**
     * 官方网站
     */
    @Schema(description = "官方网站")
    private String website;

    /**
     * 微信公众号
     */
    @Schema(description = "微信公众号")
    private String wechatPublicAccount;

    /**
     * logo
     */
    @Schema(description = "logo")
    private String logo;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String email;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 成立日期
     */
    @Schema(description = "成立日期")
    private LocalDate establishedDate;

    /**
     * 下属校友会总数
     */
    @Schema(description = "下属校友会总数")
    private Integer memberCount;

    /**
     * 活跃状态：0-不活跃 1-活跃
     */
    @Schema(description = "活跃状态：0-不活跃 1-活跃")
    private Integer activeStatus;

    /**
     * 审核状态：0-待审核 1-已通过 2-已驳回
     */
    @Schema(description = "审核状态：0-待审核 1-已通过 2-已驳回")
    private Integer approvalStatus;

    /**
     * 级别：1-校级 2-省级 3-国家级 4-国际级
     */
    @Schema(description = "级别：1-校级 2-省级 3-国家级 4-国际级")
    private Integer level;

    /**
     * 创建码
     */
    @Schema(description = "创建码")
    private Integer createCode;

    /**
     * 创建人信息
     */
    @Schema(description = "创建人信息")
    private UserDetailVo createdUser;

    /**
     * 更新人信息
     */
    @Schema(description = "更新人信息")
    private UserDetailVo updatedUser;

    @Serial
    private static final long serialVersionUID = 1L;

    public static AlumniHeadquartersDetailVo objToVo(AlumniHeadquarters alumniHeadquarters) {
        if (alumniHeadquarters == null) {
            return null;
        }
        AlumniHeadquartersDetailVo vo = new AlumniHeadquartersDetailVo();
        BeanUtils.copyProperties(alumniHeadquarters, vo);
        vo.setHeadquartersId(String.valueOf(alumniHeadquarters.getHeadquartersId()));
        return vo;
    }
}
