package com.cmswe.alumni.common.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cmswe.alumni.common.entity.LocalPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LocalPlatformVo", description = "校处会详情返回VO")
public class LocalPlatformDetailVo implements Serializable {

    /**
     * 校处会ID
     */
    @Schema(description = "校处会ID")
    private String platformId;

    /**
     * 校处会名称
     */
    @Schema(description = "校处会名称")
    private String platformName;

    /**
     * 校处会头像
     */
    @Schema(description = "校处会头像")
    private String avatar;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市")
    private String city;

    /**
     * 背景图片
     */
    @Schema(description = "背景图片")
    private String bgImg;

    /**
     * 管辖范围
     */
    @Schema(description = "管辖范围")
    private String scope;

    /**
     * 联系信息
     */
    @Schema(description = "联系信息")
    private String contactInfo;

    /**
     * 简介
     */
    @Schema(description = "简介")
    private String description;

    /**
     * 会员数量
     */
    @Schema(description = "会员数量")
    private Integer memberCount;

    /**
     * 关联的校友会数量
     */
    @Schema(description = "关联的校友会数量")
    private Integer associationCount;

    /**
     * 会长信息
     */
    @Schema(description = "会长信息")
    private WxUserInfoVo presidentInfo;

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
     * 文章列表
     */
    @Schema(description = "文章列表（已发布的文章）")
    private List<HomePageArticleVo> articleList;

    @Serial
    private static final long serialVersionUID = 1L;

    public static LocalPlatformDetailVo objToVo(LocalPlatform localPlatform) {
        if (localPlatform == null) {
            return null;
        }
        LocalPlatformDetailVo localPlatformDetailVo = new LocalPlatformDetailVo();
        BeanUtils.copyProperties(localPlatform, localPlatformDetailVo);
        return localPlatformDetailVo;
    }


}