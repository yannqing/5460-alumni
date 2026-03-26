package com.cmswe.alumni.api.association;

import com.cmswe.alumni.common.dto.QueryMyApplicationRecordListDto;
import com.cmswe.alumni.common.vo.MyApplicationRecordListVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 当前用户「我的申请」聚合列表（多类型、状态筛选）
 */
public interface MyApplicationRecordService {

    /**
     * 分页查询当前用户的申请记录摘要列表
     *
     * @param wxId 当前用户 wxId
     * @param dto  分页与筛选条件
     */
    PageVo<MyApplicationRecordListVo> queryMyApplicationRecordPage(Long wxId, QueryMyApplicationRecordListDto dto);
}
