package com.cmswe.alumni.common.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.cmswe.alumni.common.dto.MiniProgramLinkDto;
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

    /**
     * 联系人姓名
     */
    @Schema(description = "联系人姓名")
    private String contactName;

    /**
     * 联系人职务
     */
    @Schema(description = "联系人职务")
    private String contactPosition;

    /**
     * 联系人电话
     */
    @Schema(description = "联系人电话")
    private String contactPhone;

    /**
     * 联系人wxid（是否入住平台时使用，字符串避免前端雪花ID精度丢失）
     */
    @Schema(description = "联系人wxid")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wxId;

    /**
     * 小程序链接列表
     */
    @Schema(description = "小程序链接列表")
    private List<MiniProgramLinkDto> miniProgramLinks;

    /**
     * 主页展示成员列表（is_show=1的成员，仅包含id、wxId、username、roleName）
     */
    @Schema(description = "主页展示成员列表")
    private List<LocalPlatformShowMemberVo> showMembers;

    /**
     * 校促会重大事记（JSON格式）
     */
    @Schema(description = "校促会重大事记")
    private Object importantEvents;

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