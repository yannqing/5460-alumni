package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import com.cmswe.alumni.common.vo.AlumniHeadquartersDetailVo;

/**
 * 校友总会服务接口
 */
public interface AlumniHeadquartersService extends IService<AlumniHeadquarters> {

    /**
     * 根据id获取校友总会详情
     * @param id 校友总会id
     * @return 返回结果
     */
    AlumniHeadquartersDetailVo getAlumniHeadquartersDetailById(Long id);
}
