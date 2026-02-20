package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AlumniAssociationListVo", description = "校友会信息列表返回VO")
public class AlumniAssociationListVo implements Serializable {

    /**
     * 校友会ID
     */
    @Schema(description = "校友会ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long alumniAssociationId;

    /**
     * 校友会名称
     */
    @Schema(description = "校友会名称")
    private String associationName;

    /**
     * 所属母校信息
     */
    @Schema(description = "所属母校信息")
    private SchoolListVo school;

    /**
     * 所属校处会ID
     */
    @Schema(description = "所属校处会ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long platformId;

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
     * 校友会logo
     */
    @Schema(description = "校友会logo")
    private String logo;

    /**
     * 成员身份：0-会员单位 1-理事单位
     */
    @Schema(description = "成员身份：0-会员单位 1-理事单位")
    private Integer role;

    /**
     * 当前登录用户是否已加入该校友会：true-已加入 false-未加入 null-未登录
     */
    @Schema(description = "当前登录用户是否已加入该校友会：true-已加入 false-未加入 null-未登录")
    private Boolean isMember;

    /**
     * 当前登录用户是否已关注该校友会：true-已关注 false-未关注 null-未登录
     */
    @Schema(description = "当前登录用户是否已关注该校友会：true-已关注 false-未关注 null-未登录")
    private Boolean isFollowed;

    @Serial
    private static final long serialVersionUID = 1L;

    public static AlumniAssociationListVo objToVo(AlumniAssociation alumniAssociation) {
        if (alumniAssociation == null) {
            return null;
        }
        AlumniAssociationListVo alumniAssociationListVo = new AlumniAssociationListVo();
        BeanUtils.copyProperties(alumniAssociation, alumniAssociationListVo);
        return alumniAssociationListVo;
    }
}