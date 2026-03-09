package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.LocalPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LocalPlatformAdminVo", description = "校处会管理端详情返回VO")
public class LocalPlatformAdminVo implements Serializable {

    @Schema(description = "校处会ID")
    private String platformId;

    @Schema(description = "校处会名称")
    private String platformName;

    @Schema(description = "校处会头像")
    private String avatar;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "管辖范围")
    private String scope;

    @Schema(description = "联系信息")
    private String contactInfo;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "会员数量")
    private Integer memberCount;

    @Schema(description = "当月可发布到首页的文章数量（配额）")
    private Integer monthlyHomepageArticleQuota;

    @Schema(description = "背景图片")
    private String bgImg;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "负责人姓名")
    private String principalName;

    @Schema(description = "负责人职务")
    private String principalPosition;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "校促会重大事记（JSON格式）")
    private String importantEvents;

    @Schema(description = "小程序链接列表（JSON数组格式）")
    private String miniProgramLinks;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    public static LocalPlatformAdminVo objToVo(LocalPlatform localPlatform) {
        if (localPlatform == null) {
            return null;
        }
        LocalPlatformAdminVo vo = new LocalPlatformAdminVo();
        BeanUtils.copyProperties(localPlatform, vo);
        vo.setPlatformId(String.valueOf(localPlatform.getPlatformId()));
        return vo;
    }
}
