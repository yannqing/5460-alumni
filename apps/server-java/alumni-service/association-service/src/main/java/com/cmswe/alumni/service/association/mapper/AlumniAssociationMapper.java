package com.cmswe.alumni.service.association.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yanqing
 * @description 针对表【AlumniAssociation】的数据库操作Mapper
 * @createDate 2025-09-08 10:43:14
 * @Entity com.cmswe.alumni.common.entity.School
 */
@Mapper
public interface AlumniAssociationMapper extends BaseMapper<AlumniAssociation> {

    /**
     * 更新校友会成员数量
     *
     * @param alumniAssociationId 校友会ID
     * @param delta              变化值（+1 或 -1）
     * @return 更新行数
     */
    int updateMemberCount(@org.apache.ibatis.annotations.Param("alumniAssociationId") Long alumniAssociationId, 
                         @org.apache.ibatis.annotations.Param("delta") Integer delta);
}




