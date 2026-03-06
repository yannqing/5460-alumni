package com.cmswe.alumni.service.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈表 Mapper
 * @author yanqing
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {

}
