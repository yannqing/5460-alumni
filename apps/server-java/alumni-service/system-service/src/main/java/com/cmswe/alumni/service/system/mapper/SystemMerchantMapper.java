package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户 Mapper 接口（系统服务使用）
 *
 * @author CNI Alumni System
 */
@Mapper
public interface SystemMerchantMapper extends BaseMapper<Merchant> {

}
