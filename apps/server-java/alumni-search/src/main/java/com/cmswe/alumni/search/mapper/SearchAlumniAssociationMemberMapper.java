package com.cmswe.alumni.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 搜索模块专用的校友会成员关系 Mapper 接口（重命名以避免 Bean 冲突）
 *
 * @author CNI Alumni System
 */
@Mapper
public interface SearchAlumniAssociationMemberMapper extends BaseMapper<AlumniAssociationMember> {
}
