package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniHeadquartersService;
import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniHeadquartersDetailVo;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.service.association.mapper.AlumniHeadquartersMapper;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 校友总会服务实现类
 */
@Slf4j
@Service
public class AlumniHeadquartersServiceImpl extends ServiceImpl<AlumniHeadquartersMapper, AlumniHeadquarters>
        implements AlumniHeadquartersService {

    @Resource
    private SchoolMapper schoolMapper;

    @Override
    public AlumniHeadquartersDetailVo getAlumniHeadquartersDetailById(Long id) {
        // 1. 校验id
        if (id == null) {
            throw new BusinessException("参数不能为空，请重试");
        }

        // 2. 查询数据库
        AlumniHeadquarters alumniHeadquarters = this.getById(id);

        // 3. 返回校验值
        if (alumniHeadquarters == null) {
            throw new BusinessException("数据不存在，请重试");
        }

        // 4. 转换为VO
        AlumniHeadquartersDetailVo detailVo = AlumniHeadquartersDetailVo.objToVo(alumniHeadquarters);

        // 5. 构建母校信息
        Long schoolId = alumniHeadquarters.getSchoolId();
        if (schoolId != null) {
            School school = schoolMapper.selectById(schoolId);
            if (school != null) {
                SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                schoolListVo.setSchoolId(String.valueOf(schoolId));
                detailVo.setSchoolInfo(schoolListVo);
            }
        }

        // 6. 构建创建人和更新人信息（暂时不设置，需要用户服务模块）
        // Long createdUserId = alumniHeadquarters.getCreatedUserId();
        // if (createdUserId != null) {
        //     detailVo.setCreatedUser(userService.getUserDetailVoById(createdUserId));
        // }
        // Long updatedUserId = alumniHeadquarters.getUpdatedUserId();
        // if (updatedUserId != null) {
        //     detailVo.setUpdatedUser(userService.getUserDetailVoById(updatedUserId));
        // }

        log.info("根据id查询校友总会信息 id: {}", id);

        return detailVo;
    }
}
