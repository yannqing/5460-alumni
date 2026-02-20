package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.AlumniInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author yanqing
 * @description 针对表【alumni_info(校友信息表)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.AlumniInfo
 */
@Mapper
public interface AlumniInfoMapper extends BaseMapper<AlumniInfo> {

    /**
     * 根据用户ID查询校友信息
     * @param userId 用户ID
     * @return 校友信息
     */
    @Select("SELECT * FROM alumni_info WHERE user_id = #{userId} AND is_delete = 0")
    AlumniInfo findByUserId(@Param("userId") Long userId);
}
