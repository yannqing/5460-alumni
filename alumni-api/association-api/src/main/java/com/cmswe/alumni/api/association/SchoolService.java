package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.QuerySchoolListDto;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.common.vo.SchoolDetailVo;
import com.cmswe.alumni.common.vo.PageVo;

public interface SchoolService  extends IService<School> {

    /**
     * 分页查询学校列表
     * @param schoolListDto 学校列表查询DTO
     * @return 分页查询结果
     */
    PageVo<SchoolListVo> selectByPage(QuerySchoolListDto schoolListDto);

    /**
     * 根据id查询母校信息
     * @param id 查询id
     * @return 返回结果
     */
    SchoolDetailVo getSchoolDetailVoById(Long id);
}