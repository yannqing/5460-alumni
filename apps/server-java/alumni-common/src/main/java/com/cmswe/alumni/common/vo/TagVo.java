package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.SysTag;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * 标签VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TagVo", description = "标签信息返回VO")
public class TagVo implements Serializable {

    /**
     * 标签ID
     */
    @Schema(description = "标签ID")
    private Long tagId;

    /**
     * 标签代码
     */
    @Schema(description = "标签代码")
    private String code;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称")
    private String name;

    /**
     * 标签背景色
     */
    @Schema(description = "标签背景色")
    private String bgColor;

    /**
     * 标签文字色
     */
    @Schema(description = "标签文字色")
    private String textColor;

    /**
     * 标签分类: 1-通用, 2-用户画像, 3-商户类型, 4-行业领域
     */
    @Schema(description = "标签分类: 1-通用, 2-用户画像, 3-商户类型, 4-行业领域")
    private Integer category;

    /**
     * 标签图标URL
     */
    @Schema(description = "标签图标URL")
    private String iconUrl;

    /**
     * 显示排序
     */
    @Schema(description = "显示排序")
    private Integer sortOrder;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 实体转换为VO
     */
    public static TagVo objToVo(SysTag sysTag) {
        if (sysTag == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        TagVo vo = new TagVo();
        BeanUtils.copyProperties(sysTag, vo);
        return vo;
    }
}
