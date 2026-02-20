package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.OrganizeArchiRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yanqing
 * @description 针对表【organize_archi_role(组织架构角色表)】的数据库操作Mapper
 * @createDate 2026-01-13
 * @Entity com.cmswe.alumni.common.entity.OrganizeArchiRole
 */
@Mapper
public interface OrganizeArchiRoleMapper extends BaseMapper<OrganizeArchiRole> {

}
