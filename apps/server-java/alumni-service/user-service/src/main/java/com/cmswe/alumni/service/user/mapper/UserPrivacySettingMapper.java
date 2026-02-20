package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserPrivacySetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户隐私设置表 Mapper 接口
 * @author system
 */
@Mapper
public interface UserPrivacySettingMapper extends BaseMapper<UserPrivacySetting> {

}