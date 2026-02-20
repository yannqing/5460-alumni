package com.cmswe.alumni.api.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.AdminQueryApplyListDto;
import com.cmswe.alumni.common.dto.ApproveArticleDto;
import com.cmswe.alumni.common.dto.QueryArticleApplyListDto;
import com.cmswe.alumni.common.entity.HomePageArticleApply;
import com.cmswe.alumni.common.vo.HomePageArticleApplyVo;
import com.cmswe.alumni.common.vo.PageVo;

/**
 * 首页公众号文章审核 API Service 接口
 */
public interface HomePageArticleApplyService extends IService<HomePageArticleApply> {

    /**
     * 审核文章
     * @param approveDto 审核请求参数
     * @param approverWxId 审批人ID
     * @param approverName 审批人名称
     * @return 是否审核成功
     */
    boolean approveArticle(ApproveArticleDto approveDto, Long approverWxId, String approverName);

    /**
     * 获取审核记录列表（分页）
     * 根据 applyStatus 参数查询不同状态的记录
     * 如果 applyStatus 为 null，则查询所有状态的记录
     *
     * @param queryDto 查询参数（包含状态筛选）
     * @return 分页结果
     */
    PageVo<HomePageArticleApplyVo> getApplyList(QueryArticleApplyListDto queryDto);

}
