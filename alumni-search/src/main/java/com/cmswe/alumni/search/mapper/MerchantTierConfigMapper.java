package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.MerchantTierConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户会员等级配置 Mapper 接口
 *
 * @author CNI Alumni System
 * @since 2025-12-17
 */
@Mapper
public interface MerchantTierConfigMapper extends BaseMapper<MerchantTierConfig> {

    /**
     * 查询所有启用的会员等级配置（按排序）
     *
     * @return 会员等级配置列表
     */
    List<MerchantTierConfig> selectAllEnabled();

    /**
     * 根据等级查询配置
     *
     * @param tierLevel 等级
     * @return 会员等级配置
     */
    MerchantTierConfig selectByTierLevel(@Param("tierLevel") Integer tierLevel);
}
