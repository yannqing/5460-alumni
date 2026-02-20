package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.AlumniEducation;

/**
 * 校友教育经历服务接口
 */
public interface AlumniEducationService extends IService<AlumniEducation> {

    /**
     * 保存或更新教育经历（根据 wxId 和 schoolId 判断）
     *
     * @param education 教育经历信息
     * @return 是否成功
     */
    boolean saveOrUpdateByWxIdAndSchoolId(AlumniEducation education);
}
