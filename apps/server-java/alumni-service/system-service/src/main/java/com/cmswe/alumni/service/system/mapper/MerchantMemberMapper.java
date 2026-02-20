package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.MerchantMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户成员关系表 Mapper
 * @author CNI Alumni System
 */
@Mapper
public interface MerchantMemberMapper extends BaseMapper<MerchantMember> {
}
