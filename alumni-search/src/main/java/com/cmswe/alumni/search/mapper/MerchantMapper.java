package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-17
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    /**
     * 根据用户ID查询商户列表
     *
     * @param userId 用户ID
     * @return 商户列表
     */
    List<Merchant> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据会员等级查询商户列表
     *
     * @param memberTier 会员等级
     * @param limit      查询数量
     * @return 商户列表
     */
    List<Merchant> selectByMemberTier(@Param("memberTier") Integer memberTier,
                                      @Param("limit") Integer limit);

    /**
     * 查询待审核的商户列表
     *
     * @param merchantType 商户类型（可选）
     * @param limit        查询数量
     * @return 商户列表
     */
    List<Merchant> selectPendingReview(@Param("merchantType") Integer merchantType,
                                       @Param("limit") Integer limit);

    /**
     * 统计商户数量（按状态）
     *
     * @param status 状态
     * @return 数量
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 更新商户统计数据
     *
     * @param merchantId 商户ID
     * @return 更新行数
     */
    int updateStatistics(@Param("merchantId") Long merchantId);
}
