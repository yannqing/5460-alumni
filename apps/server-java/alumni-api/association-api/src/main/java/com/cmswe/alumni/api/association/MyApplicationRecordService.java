package com.cmswe.alumni.api.association;

import com.cmswe.alumni.common.dto.QueryMyApplicationRecordListDto;
import com.cmswe.alumni.common.dto.QueryMyApplicationRecordDetailDto;
import com.cmswe.alumni.common.dto.UpdateMyApplicationRecordDto;
import com.cmswe.alumni.common.vo.MyApplicationRecordDetailVo;
import com.cmswe.alumni.common.vo.MyApplicationRecordListVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 当前用户「我的申请」聚合列表
 */
public interface MyApplicationRecordService {

    PageVo<MyApplicationRecordListVo> queryMyApplicationRecordPage(Long wxId, QueryMyApplicationRecordListDto dto);

    MyApplicationRecordDetailVo queryMyApplicationRecordDetail(Long wxId, QueryMyApplicationRecordDetailDto dto);

    boolean updateMyApplicationRecord(Long wxId, UpdateMyApplicationRecordDto dto);
}
