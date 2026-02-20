package com.cmswe.alumni.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学校列表展示DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "学校列表查询请求DTO")
public class QuerySchoolListDto extends PageRequest implements Serializable {

    /**
     * 学校名称
     */
    @Schema(description = "学校名称", example = "北京大学",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "学校名称长度不能超过200个字符")
    private String schoolName;

    /**
     * 学校地址
     */
    @Schema(description = "学校地址", example = "北京市海淀区颐和园路5号",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 200, message = "学校地址长度不能超过200个字符")
    private String location;

    /**
     * 学校描述
     */
    @Schema(description = "学校描述", example = "学校描述xxxx",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    /**
     * 合并院校
     */
    @Schema(description = "合并院校", example = "xxx",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String mergedInstitutions;

    /**
     * 办学层次
     */
    @Schema(description = "办学层次", example = "xxx",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String level;

    /**
     * 所在省
     */
    @Schema(description = "所在省", example = "xxx",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String province;

    /**
     * 所在市
     */
    @Schema(description = "所在市", example = "xxx",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String city;

    /**
     * 曾用名(合并院校)
     */
    @Schema(description = "曾用名(合并院校)", example = "北京大学测曾用名为京师大学堂,是xxxx",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String previousName;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段:根据时间排序（createTime）")
    private String sortField;

    @Serial
    private static final long serialVersionUID = 1L;

}