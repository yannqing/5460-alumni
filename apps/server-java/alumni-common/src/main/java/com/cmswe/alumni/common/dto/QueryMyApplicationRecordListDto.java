package com.cmswe.alumni.common.dto;

import com.cmswe.alumni.common.constant.MyApplicationRecordType;
import com.cmswe.alumni.common.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 查询「我的申请」聚合列表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryMyApplicationRecordListDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 仅查询指定类型；为空或空列表表示三种全部。
     * 取值见 {@link MyApplicationRecordType}
     */
    @Schema(
            description = "申请类型过滤，可多选。为空表示全部。取值：ALUMNI_ASSOCIATION_CREATE / ALUMNI_ASSOCIATION_JOIN / ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> recordTypes;

    /**
     * 归一化状态：待审核 / 已通过 / 已拒绝 / 已撤销；不传表示不按状态过滤。
     * 校友会加入校促会无「已撤销」，筛选 CANCELLED 时该类型无数据。
     */
    @Schema(
            description = "状态分组：PENDING / APPROVED / REJECTED / CANCELLED；不传表示全部",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELLED"}
    )
    private String statusGroup;
}
