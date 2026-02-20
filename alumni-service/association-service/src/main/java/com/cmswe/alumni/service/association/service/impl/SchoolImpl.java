package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.AlumniHeadquartersService;
import com.cmswe.alumni.common.dto.QueryAlumniAssociationListDto;
import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import com.cmswe.alumni.common.vo.*;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMapper;
import com.cmswe.alumni.api.association.SchoolService;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.dto.QuerySchoolListDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class SchoolImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {

    @Resource
    private SchoolMapper schoolMapper;

    @Resource
    private AlumniHeadquartersService alumniHeadquartersService;

    @Resource
    private AlumniAssociationMapper alumniAssociationMapper;

    /**
     *
     * @param schoolListDto 学校列表查询DTO
     * @return
     */
    @Override
    public PageVo<SchoolListVo> selectByPage(QuerySchoolListDto schoolListDto){
        //1.参数校验
        if (schoolListDto == null) {
            throw new BusinessException("参数为空");
        }

        //2.获取参数
        String schoolName = schoolListDto.getSchoolName();
        String location = schoolListDto.getLocation();
        String description = schoolListDto.getDescription();
        String previousName = schoolListDto.getPreviousName();
        String mergedInstitutions = schoolListDto.getMergedInstitutions();
        String level = schoolListDto.getLevel();
        String province = schoolListDto.getProvince();
        String city = schoolListDto.getCity();
        int current = schoolListDto.getCurrent();
        int pageSize = schoolListDto.getPageSize();
        String sortField = schoolListDto.getSortField();
        String sortOrder = schoolListDto.getSortOrder();

        if (sortField == null ){
            sortField = "createTime";
        }

        //3.构造查询条件
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.isNotBlank(schoolName), School::getSchoolName, schoolName)
                .or()
                .like(StringUtils.isNotBlank(previousName), School::getPreviousName, previousName)
                .or()
                .like(StringUtils.isNotBlank(mergedInstitutions), School::getMergedInstitutions, mergedInstitutions)
                .like(StringUtils.isNotBlank(location), School::getLocation, location)
                .like(StringUtils.isNotBlank(level), School::getLevel, level)
                .like(StringUtils.isNotBlank(province), School::getProvince, province)
                .like(StringUtils.isNotBlank(city), School::getCity, city)
                .like(StringUtils.isNotBlank(description), School::getDescription, description)
                .orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), School.getSchoolSortMethod(sortField));

        //4.执行分页查询
        Page<School> schoolPage = this.page(new Page<>(current, pageSize), queryWrapper);
//        List<SchoolListVo> list = schoolPage.getRecords().stream().map(SchoolListVo::objToVo).toList();
        List<SchoolListVo> list = schoolPage.getRecords().stream().map(school -> {
            SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
            schoolListVo.setSchoolId(String.valueOf(school.getSchoolId()));
            return schoolListVo;
        }).toList();

        log.info("分页查询学校列表");

        Page<SchoolListVo> resultPage = new Page<SchoolListVo>(current, pageSize, schoolPage.getTotal()).setRecords(list);
        return PageVo.of(resultPage);
    }


    /**
     * 根据 id 查询 SchoolDetailVo
     *
     * @param id 查询id
     * @return SchoolDetailVo
     */
    @Override
    public SchoolDetailVo getSchoolDetailVoById(Long id) {
        // 1.校验id
        if (id == null) {
            throw new BusinessException("参数不能为空，请重试");
        }

        // 2.查询数据库
        School school = schoolMapper.selectById(id);

        // 3.返回校验值
        if (school == null) {
            throw new BusinessException("数据不存在，请重试");
        }

        // 4. 转 vo
        SchoolDetailVo schoolDetailVo = SchoolDetailVo.objToVo(school);
        schoolDetailVo.setSchoolId(String.valueOf(school.getSchoolId()));

        // 5. 查询母校下面所属校友会
        List<AlumniAssociation> alumniAssociations = alumniAssociationMapper.selectList(
                new LambdaQueryWrapper<AlumniAssociation>()
                        .eq(AlumniAssociation::getSchoolId, school.getSchoolId())
                        .last("limit 10")
        );
        List<AlumniAssociationListVo> alumniAssociationListVos = alumniAssociations.stream()
                .map(AlumniAssociationListVo::objToVo)
                .toList();
        schoolDetailVo.setAlumniAssociationListVos(alumniAssociationListVos);

        // 6. 查询校友总会信息
        Long headquartersId = school.getHeadquartersId();
        if (headquartersId != null) {
            AlumniHeadquarters alumniHeadquarters = alumniHeadquartersService.getById(headquartersId);
            if (alumniHeadquarters != null) {
                AlumniHeadquartersListVo alumniHeadquartersListVo = AlumniHeadquartersListVo.objToVo(alumniHeadquarters);
                alumniHeadquartersListVo.setHeadquartersId(String.valueOf(headquartersId));
                schoolDetailVo.setAlumniHeadquarters(alumniHeadquartersListVo);
            }
        }

        log.info("根据id查询母校信息 id:{}", id);

        return schoolDetailVo;
    }



}
