package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.AlumniEducationService;
import com.cmswe.alumni.common.entity.AlumniEducation;
import com.cmswe.alumni.service.user.mapper.AlumniEducationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 校友教育经历服务实现类
 */
@Slf4j
@Service
public class AlumniEducationServiceImpl extends ServiceImpl<AlumniEducationMapper, AlumniEducation>
        implements AlumniEducationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateByWxIdAndSchoolId(AlumniEducation education) {
        // 查询是否已存在该用户在该学校的教育经历
        LambdaQueryWrapper<AlumniEducation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlumniEducation::getWxId, education.getWxId())
                .eq(AlumniEducation::getSchoolId, education.getSchoolId());

        AlumniEducation existingEducation = this.getOne(queryWrapper);

        if (existingEducation != null) {
            // 更新已有记录
            education.setAlumniEducationId(existingEducation.getAlumniEducationId());
            boolean result = this.updateById(education);
            if (result) {
                log.info("更新教育经历成功 - wxId: {}, schoolId: {}", education.getWxId(), education.getSchoolId());
            }
            return result;
        } else {
            // 新增记录
            boolean result = this.save(education);
            if (result) {
                log.info("新增教育经历成功 - wxId: {}, schoolId: {}", education.getWxId(), education.getSchoolId());
            }
            return result;
        }
    }
}
