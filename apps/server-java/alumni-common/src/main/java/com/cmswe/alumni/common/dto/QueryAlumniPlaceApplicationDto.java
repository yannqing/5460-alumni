package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校友企业/场所申请列表查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "校友企业/场所申请列表查询请求参数")
public class QueryAlumniPlaceApplicationDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "场所/企业名称", example = "某某科技有限公司")
    private String placeName;

    @Schema(description = "申请状态：0-待审核 1-审核通过 2-审核拒绝 3-已撤销", example = "0")
    private Integer applicationStatus;

    @Schema(description = "类型：1-企业 2-场所", example = "1")
    private Integer placeType;

    @Schema(description = "所属校友会ID", example = "123456")
    private Long alumniAssociationId;

    @Schema(description = "申请人姓名", example = "张三")
    private String applicantName;
}
