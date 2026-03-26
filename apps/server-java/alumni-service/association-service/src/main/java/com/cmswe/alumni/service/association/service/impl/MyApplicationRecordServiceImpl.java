package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.association.AlumniAssociationApplicationService;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplyService;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplicationService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.association.MyApplicationRecordService;
import com.cmswe.alumni.api.association.SchoolService;
import com.cmswe.alumni.common.constants.MyApplicationRecordType;
import com.cmswe.alumni.common.dto.QueryMyApplicationRecordListDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationApplication;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApplication;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationListVo;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.common.vo.MyApplicationRecordListVo;
import com.cmswe.alumni.common.vo.PageVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 我的申请聚合列表
 */
@Slf4j
@Service
public class MyApplicationRecordServiceImpl implements MyApplicationRecordService {

    @Resource
    private AlumniAssociationApplicationService alumniAssociationApplicationService;

    @Resource
    private AlumniAssociationJoinApplicationService alumniAssociationJoinApplicationService;

    @Resource
    private AlumniAssociationJoinApplyService alumniAssociationJoinApplyService;

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private LocalPlatformService localPlatformService;

    @Override
    public PageVo<MyApplicationRecordListVo> queryMyApplicationRecordPage(Long wxId, QueryMyApplicationRecordListDto dto) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户未登录");
        }
        if (dto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "查询参数不能为空");
        }
        validateRecordTypes(dto.getRecordTypes());

        List<MergedRow> merged = new ArrayList<>();

        if (includeType(dto.getRecordTypes(), MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE)) {
            merged.addAll(loadCreateAssociationRows(wxId, dto.getStatusGroup()));
        }
        if (includeType(dto.getRecordTypes(), MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN)) {
            merged.addAll(loadJoinAssociationRows(wxId, dto.getStatusGroup()));
        }
        if (includeType(dto.getRecordTypes(), MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM)) {
            merged.addAll(loadJoinPlatformRows(wxId, dto.getStatusGroup()));
        }

        merged.sort(Comparator.comparing(MergedRow::sortTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        long total = merged.size();
        int from = Math.max(0, (dto.getCurrent() - 1) * dto.getPageSize());
        int to = Math.min(from + dto.getPageSize(), merged.size());
        List<MergedRow> pageRows = from >= merged.size() ? List.of() : merged.subList(from, to);

        List<MyApplicationRecordListVo> records = pageRows.stream()
                .map(this::toVo)
                .collect(Collectors.toList());

        return new PageVo<>(records, total, (long) dto.getCurrent(), (long) dto.getPageSize());
    }

    private void validateRecordTypes(List<String> recordTypes) {
        if (recordTypes == null || recordTypes.isEmpty()) {
            return;
        }
        for (String t : recordTypes) {
            if (StringUtils.isBlank(t) || !MyApplicationRecordType.isValid(t.trim())) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "非法的 recordTypes 取值: " + t);
            }
        }
    }

    private static boolean includeType(List<String> recordTypes, String type) {
        if (recordTypes == null || recordTypes.isEmpty()) {
            return true;
        }
        return recordTypes.stream().anyMatch(t -> type.equalsIgnoreCase(t.trim()));
    }

    private List<MergedRow> loadCreateAssociationRows(Long wxId, String statusGroup) {
        LambdaQueryWrapper<AlumniAssociationApplication> w = new LambdaQueryWrapper<>();
        // 创建校友会：业务上申请人即驻会代表，对应表字段 zh_wx_id
        w.eq(AlumniAssociationApplication::getZhWxId, wxId);
        applyFourStateFilter(w, statusGroup, AlumniAssociationApplication::getApplicationStatus);
        w.orderByDesc(AlumniAssociationApplication::getApplyTime);
        List<AlumniAssociationApplication> list = alumniAssociationApplicationService.list(w);
        List<MergedRow> rows = new ArrayList<>();
        for (AlumniAssociationApplication e : list) {
            LocalDateTime t = e.getApplyTime() != null ? e.getApplyTime() : e.getCreateTime();
            rows.add(new MergedRow(MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE, e.getApplicationId(), t, e));
        }
        return rows;
    }

    private List<MergedRow> loadJoinAssociationRows(Long wxId, String statusGroup) {
        LambdaQueryWrapper<AlumniAssociationJoinApplication> w = new LambdaQueryWrapper<>();
        w.eq(AlumniAssociationJoinApplication::getTargetId, wxId)
                .eq(AlumniAssociationJoinApplication::getApplicantType, 1);
        applyFourStateFilter(w, statusGroup, AlumniAssociationJoinApplication::getApplicationStatus);
        w.orderByDesc(AlumniAssociationJoinApplication::getApplyTime);
        List<AlumniAssociationJoinApplication> list = alumniAssociationJoinApplicationService.list(w);
        List<MergedRow> rows = new ArrayList<>();
        for (AlumniAssociationJoinApplication e : list) {
            LocalDateTime t = e.getApplyTime() != null ? e.getApplyTime() : e.getCreateTime();
            rows.add(new MergedRow(MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN, e.getApplicationId(), t, e));
        }
        return rows;
    }

    private List<MergedRow> loadJoinPlatformRows(Long wxId, String statusGroup) {
        if (isCancelledOnlyGroup(statusGroup)) {
            return List.of();
        }
        LambdaQueryWrapper<AlumniAssociationJoinApply> w = new LambdaQueryWrapper<>();
        w.eq(AlumniAssociationJoinApply::getApplicantWxId, wxId);
        applyThreeStateFilter(w, statusGroup, AlumniAssociationJoinApply::getStatus);
        w.orderByDesc(AlumniAssociationJoinApply::getCreateTime);
        List<AlumniAssociationJoinApply> list = alumniAssociationJoinApplyService.list(w);
        List<MergedRow> rows = new ArrayList<>();
        for (AlumniAssociationJoinApply e : list) {
            LocalDateTime t = e.getCreateTime() != null ? e.getCreateTime() : e.getUpdateTime();
            rows.add(new MergedRow(MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM, e.getId(), t, e));
        }
        return rows;
    }

    private static boolean isCancelledOnlyGroup(String statusGroup) {
        return "CANCELLED".equalsIgnoreCase(StringUtils.trimToEmpty(statusGroup));
    }

    private <T> void applyFourStateFilter(LambdaQueryWrapper<T> w, String statusGroup,
                                        com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> statusGetter) {
        if (StringUtils.isBlank(statusGroup)) {
            return;
        }
        switch (statusGroup.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> w.eq(statusGetter, 0);
            case "APPROVED" -> w.eq(statusGetter, 1);
            case "REJECTED" -> w.eq(statusGetter, 2);
            case "CANCELLED" -> w.eq(statusGetter, 3);
            default -> throw new BusinessException(ErrorType.ARGS_ERROR, "非法的 statusGroup: " + statusGroup);
        }
    }

    private <T> void applyThreeStateFilter(LambdaQueryWrapper<T> w, String statusGroup,
                                         com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> statusGetter) {
        if (StringUtils.isBlank(statusGroup)) {
            return;
        }
        switch (statusGroup.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> w.eq(statusGetter, 0);
            case "APPROVED" -> w.eq(statusGetter, 1);
            case "REJECTED" -> w.eq(statusGetter, 2);
            default -> throw new BusinessException(ErrorType.ARGS_ERROR, "非法的 statusGroup: " + statusGroup);
        }
    }

    private MyApplicationRecordListVo toVo(MergedRow row) {
        return switch (row.recordType) {
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE -> toCreateVo((AlumniAssociationApplication) row.payload);
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN -> toJoinVo((AlumniAssociationJoinApplication) row.payload);
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM -> toJoinPlatformVo((AlumniAssociationJoinApply) row.payload);
            default -> throw new BusinessException(ErrorType.SYSTEM_ERROR, "未知记录类型");
        };
    }

    private MyApplicationRecordListVo toCreateVo(AlumniAssociationApplication e) {
        MyApplicationRecordListVo vo = new MyApplicationRecordListVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE);
        vo.setRecordId(String.valueOf(e.getApplicationId()));
        vo.setTitle(StringUtils.defaultIfBlank(e.getAssociationName(), "创建校友会申请"));
        vo.setSubtitle(buildSchoolSubtitle(e.getSchoolId()));
        vo.setAlumniAssociationId(e.getCreatedAssociationId() != null ? String.valueOf(e.getCreatedAssociationId()) : null);
        vo.setPlatformId(e.getPlatformId() != null ? String.valueOf(e.getPlatformId()) : null);
        vo.setApplicationStatus(e.getApplicationStatus());
        vo.setApplicationStatusText(fourStateText(e.getApplicationStatus()));
        vo.setStatusGroup(fourStateGroup(e.getApplicationStatus()));
        vo.setApplyTime(e.getApplyTime());
        vo.setCanEdit(false);
        vo.setCanCancel(false);
        return vo;
    }

    private MyApplicationRecordListVo toJoinVo(AlumniAssociationJoinApplication e) {
        MyApplicationRecordListVo vo = new MyApplicationRecordListVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN);
        vo.setRecordId(String.valueOf(e.getApplicationId()));
        AlumniAssociation ass = alumniAssociationService.getById(e.getAlumniAssociationId());
        vo.setTitle(ass != null ? StringUtils.defaultIfBlank(ass.getAssociationName(), "加入校友会申请") : "加入校友会申请");
        vo.setSubtitle(ass != null ? ass.getLocation() : null);
        vo.setAlumniAssociationId(String.valueOf(e.getAlumniAssociationId()));
        vo.setPlatformId(null);
        vo.setApplicationStatus(e.getApplicationStatus());
        vo.setApplicationStatusText(AlumniAssociationJoinApplicationListVo.getApplicationStatusText(e.getApplicationStatus()));
        vo.setStatusGroup(fourStateGroup(e.getApplicationStatus()));
        vo.setApplyTime(e.getApplyTime());
        boolean pending = e.getApplicationStatus() != null && e.getApplicationStatus() == 0;
        vo.setCanEdit(pending);
        vo.setCanCancel(pending);
        return vo;
    }

    private MyApplicationRecordListVo toJoinPlatformVo(AlumniAssociationJoinApply e) {
        MyApplicationRecordListVo vo = new MyApplicationRecordListVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM);
        vo.setRecordId(String.valueOf(e.getId()));
        AlumniAssociation ass = alumniAssociationService.getById(e.getAlumniAssociationId());
        String assName = ass != null ? ass.getAssociationName() : null;
        vo.setTitle(StringUtils.defaultIfBlank(assName, "校友会加入校促会申请"));
        String platformName = null;
        if (e.getPlatformId() != null) {
            try {
                LocalPlatformDetailVo p = localPlatformService.getLocalPlatformById(e.getPlatformId());
                platformName = p != null ? p.getPlatformName() : null;
            } catch (Exception ex) {
                log.debug("加载校促会名称失败 platformId={}", e.getPlatformId(), ex);
            }
        }
        vo.setSubtitle(platformName != null ? "校促会：" + platformName : null);
        vo.setAlumniAssociationId(String.valueOf(e.getAlumniAssociationId()));
        vo.setPlatformId(e.getPlatformId() != null ? String.valueOf(e.getPlatformId()) : null);
        vo.setApplicationStatus(e.getStatus());
        vo.setApplicationStatusText(threeStateText(e.getStatus()));
        vo.setStatusGroup(threeStateGroup(e.getStatus()));
        vo.setApplyTime(e.getCreateTime());
        vo.setCanEdit(false);
        vo.setCanCancel(false);
        return vo;
    }

    private String buildSchoolSubtitle(Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        School school = schoolService.getById(schoolId);
        return school != null ? school.getSchoolName() : null;
    }

    private static String fourStateText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已撤销";
            default -> "未知";
        };
    }

    private static String threeStateText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }

    private static String fourStateGroup(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return switch (status) {
            case 0 -> "PENDING";
            case 1 -> "APPROVED";
            case 2 -> "REJECTED";
            case 3 -> "CANCELLED";
            default -> "UNKNOWN";
        };
    }

    private static String threeStateGroup(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return switch (status) {
            case 0 -> "PENDING";
            case 1 -> "APPROVED";
            case 2 -> "REJECTED";
            default -> "UNKNOWN";
        };
    }

    private record MergedRow(String recordType, Long recordId, LocalDateTime sortTime, Object payload) {}
}
