package com.cmswe.alumni.common.dto;

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
     * 仅查询指定类型；为空或空列表表示三种全部包含。
     * 取值见 {@link com.cmswe.alumni.common.constants.MyApplicationRecordType}
     */
    @Schema(
            description = "申请类型过滤，可多选。为空表示全部。取值：ALUMNI_ASSOCIATION_CREATE / ALUMNI_ASSOCIATION_JOIN / ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> recordTypes;

    /**
     * 归一化状态筛选：待审核 / 已通过 / 已拒绝 / 已撤销；不传表示不按状态过滤。
     * 说明：校友会加入校促会申请无「已撤销」状态，筛选 CANCELLED 时该类型不会出现记录。
     */
    @Schema(
            description = "状态分组：PENDING(待审核) / APPROVED(已通过) / REJECTED(已拒绝) / CANCELLED(已撤销)；不传表示全部",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELLED"}
    )
    private String statusGroup;
}
