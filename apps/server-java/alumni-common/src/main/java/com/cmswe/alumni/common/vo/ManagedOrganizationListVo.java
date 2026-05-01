package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 可管理的组织列表VO
 *
 * @author CNI Alumni System
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "可管理的组织列表")
public class ManagedOrganizationListVo implements Serializable {

    /**
     * 组织ID
     */
    @Schema(description = "组织ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 组织类型：0-校友会 1-校促会 2-商户 3-校友总会
     */
    @Schema(description = "组织类型：0-校友会 1-校促会 2-商户 3-校友总会")
    private Integer type;

    /**
     * 组织logo/头像
     */
    @Schema(description = "组织logo/头像")
    private String logo;

    /**
     * 组织名称
     */
    @Schema(description = "组织名称")
    private String name;

    /**
     * 地点
     */
    @Schema(description = "地点")
    private String location;

    /**
     * 名称拼音首字母（用于排序）
     */
    @Schema(description = "名称拼音首字母")
    private String pinyinInitial;

    /**
     * 名称完整拼音（用于显示）
     */
    @Schema(description = "名称完整拼音")
    private String pinyin;

    @Serial
    private static final long serialVersionUID = 1L;
}
