package com.cmswe.alumni.search.service.enrich;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.association.SchoolService;
import com.cmswe.alumni.common.entity.AlumniEducation;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.vo.AlumniEducationListVo;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.service.user.mapper.AlumniEducationMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户数据增强服务
 * 用于补充 ES 查询结果中缺失的数据库详细信息（如学校信息、完整教育经历）
 *
 * @author CNI Alumni System
 * @since 2026-03-25
 */
@Slf4j
@Service
public class UserDataEnrichService {

    @Resource
    private AlumniEducationMapper alumniEducationMapper;

    @Resource
    private SchoolService schoolService;

    /**
     * 批量增强用户数据：补充学校信息和完整教育经历
     *
     * @param responses ES 查询结果列表
     */
    public void enrichUserListResponses(List<UserListResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();

        // 1. 提取所有 wxId
        List<Long> wxIds = responses.stream()
                .map(UserListResponse::getWxId)
                .filter(Objects::nonNull)
                .map(Long::parseLong)
                .distinct()
                .collect(Collectors.toList());

        if (wxIds.isEmpty()) {
            log.warn("[数据增强] 没有有效的 wxId，跳过增强");
            return;
        }

        log.debug("[数据增强] 开始批量查询 - 用户数: {}", wxIds.size());

        // 2. 批量查询所有用户的主要教育经历（type=1）
        List<AlumniEducation> primaryEducations = alumniEducationMapper.selectList(
                new LambdaQueryWrapper<AlumniEducation>()
                        .in(AlumniEducation::getWxId, wxIds)
                        .eq(AlumniEducation::getType, 1) // type=1 表示主要经历
        );

        log.info("[数据增强] 查询到 {} 条主要教育经历", primaryEducations.size());

        // 调试：打印前3条教育经历
        if (!primaryEducations.isEmpty()) {
            primaryEducations.stream().limit(3).forEach(edu ->
                log.debug("[数据增强] 教育经历示例 - wxId: {}, schoolId: {}, graduationYear: {}, major: {}, type: {}",
                    edu.getWxId(), edu.getSchoolId(), edu.getGraduationYear(), edu.getMajor(), edu.getType())
            );
        }

        // 3. 批量查询学校信息
        Map<Long, SchoolListVo> schoolMap = batchQuerySchools(primaryEducations);

        // 4. 构建 wxId -> 教育经历 的映射
        Map<Long, AlumniEducationListVo> educationMap = primaryEducations.stream()
                .collect(Collectors.toMap(
                        AlumniEducation::getWxId,
                        education -> {
                            AlumniEducationListVo vo = AlumniEducationListVo.objToVo(education);
                            // 设置学校信息
                            if (education.getSchoolId() != null) {
                                vo.setSchoolInfo(schoolMap.get(education.getSchoolId()));
                            }
                            return vo;
                        },
                        (v1, v2) -> v1)); // 如果有多个主要经历，保留第一个

        // 5. 将教育经历和学校信息填充到 responses 中
        int enrichedCount = 0;
        for (UserListResponse response : responses) {
            Long wxId = response.getWxId() != null ? Long.parseLong(response.getWxId()) : null;
            if (wxId != null && educationMap.containsKey(wxId)) {
                AlumniEducationListVo education = educationMap.get(wxId);
                response.setPrimaryEducation(education);
                enrichedCount++;

                // 调试：打印填充后的数据
                if (enrichedCount <= 2) {
                    log.info("[数据增强] 填充数据 - wxId: {}, schoolId: {}, schoolName: {}, graduationYear: {}",
                        wxId,
                        education.getSchoolInfo() != null ? education.getSchoolInfo().getSchoolId() : "null",
                        education.getSchoolInfo() != null ? education.getSchoolInfo().getSchoolName() : "null",
                        education.getGraduationYear());
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("[数据增强] 完成 - 用户数: {}, 增强数: {}, 实际填充: {}, 耗时: {}ms",
                responses.size(), educationMap.size(), enrichedCount, (endTime - startTime));
    }

    /**
     * 批量查询学校信息
     *
     * @param educations 教育经历列表
     * @return schoolId -> SchoolListVo 的映射
     */
    private Map<Long, SchoolListVo> batchQuerySchools(List<AlumniEducation> educations) {
        if (educations == null || educations.isEmpty()) {
            return Collections.emptyMap();
        }

        // 提取所有学校ID
        List<Long> schoolIds = educations.stream()
                .map(AlumniEducation::getSchoolId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (schoolIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("[数据增强] 批量查询学校信息 - 学校数: {}", schoolIds.size());
        log.debug("[数据增强] 学校ID列表: {}", schoolIds);

        // 批量查询学校
        List<School> schools = schoolService.listByIds(schoolIds);
        log.info("[数据增强] 查询到 {} 所学校", schools.size());

        // 调试：打印前3所学校
        if (!schools.isEmpty()) {
            schools.stream().limit(3).forEach(school ->
                log.debug("[数据增强] 学校示例 - schoolId: {}, schoolName: {}, province: {}, city: {}",
                    school.getSchoolId(), school.getSchoolName(), school.getProvince(), school.getCity())
            );
        }

        // 转换为 Map
        return schools.stream()
                .map(school -> {
                    SchoolListVo vo = SchoolListVo.objToVo(school);
                    vo.setSchoolId(String.valueOf(school.getSchoolId()));
                    return vo;
                })
                .collect(Collectors.toMap(
                        vo -> Long.valueOf(vo.getSchoolId()),
                        Function.identity(),
                        (v1, v2) -> v1));
    }
}
