package com.cmswe.alumni.service.association.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.association.AlumniAssociationApplicationService;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplyService;
import com.cmswe.alumni.api.association.AlumniAssociationJoinApplicationService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.association.MyApplicationRecordService;
import com.cmswe.alumni.api.association.SchoolService;
import com.cmswe.alumni.api.search.ShopService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.common.constant.MyApplicationRecordType;
import com.cmswe.alumni.common.dto.CancelMyApplicationRecordDto;
import com.cmswe.alumni.common.dto.QueryMyApplicationRecordDetailDto;
import com.cmswe.alumni.common.dto.QueryMyApplicationRecordListDto;
import com.cmswe.alumni.common.dto.UpdateAlumniAssociationJoinApplicationDto;
import com.cmswe.alumni.common.dto.UpdateMyJoinPlatformApplicationDto;
import com.cmswe.alumni.common.dto.UpdateMyApplicationRecordDto;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationApplication;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApplication;
import com.cmswe.alumni.common.entity.Merchant;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.entity.Shop;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationListVo;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.AlumniAssociationApplicationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplicationDetailVo;
import com.cmswe.alumni.common.vo.AlumniAssociationJoinApplyDetailVo;
import com.cmswe.alumni.common.vo.LocalPlatformDetailVo;
import com.cmswe.alumni.common.vo.MaterialImageItemVo;
import com.cmswe.alumni.common.vo.MerchantDetailVo;
import com.cmswe.alumni.common.vo.MerchantListVo;
import com.cmswe.alumni.common.vo.MyApplicationRecordDetailVo;
import com.cmswe.alumni.common.vo.MyApplicationRecordListVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopDetailVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 我的申请：多类型合并、内存分页
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

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private MerchantService merchantService;

    @Resource
    private ShopService shopService;

    private List<Long> parseAssociationIds(String associationIdStr) {
        if (StringUtils.isBlank(associationIdStr)) {
            return new ArrayList<>();
        }
        String trimmed = associationIdStr.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                return JSON.parseArray(trimmed, Long.class);
            } catch (Exception e) {
                log.error("解析校友会ID数组失败: {}", trimmed, e);
            }
        }
        try {
            Long id = Long.parseLong(trimmed);
            return new ArrayList<>(Collections.singletonList(id));
        } catch (NumberFormatException e) {
            log.warn("校友会ID字段格式非数字且非数组: {}", trimmed);
        }
        return new ArrayList<>();
    }

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
        if (includeType(dto.getRecordTypes(), MyApplicationRecordType.MERCHANT_APPLICATION)) {
            merged.addAll(loadMerchantRows(wxId, dto.getStatusGroup()));
        }
        if (includeType(dto.getRecordTypes(), MyApplicationRecordType.SHOP_APPLICATION)) {
            merged.addAll(loadShopRows(wxId, dto.getStatusGroup()));
        }

        merged.sort(Comparator.comparing(MergedRow::sortTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        long total = merged.size();
        int from = Math.max(0, (dto.getCurrent() - 1) * dto.getPageSize());
        int to = Math.min(from + dto.getPageSize(), merged.size());
        List<MergedRow> pageRows = from >= merged.size() ? List.of() : merged.subList(from, to);

        List<MyApplicationRecordListVo> records = pageRows.stream().map(this::toVo).collect(Collectors.toList());
        return new PageVo<>(records, total, (long) dto.getCurrent(), (long) dto.getPageSize());
    }

    @Override
    public MyApplicationRecordDetailVo queryMyApplicationRecordDetail(Long wxId, QueryMyApplicationRecordDetailDto dto) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户未登录");
        }
        if (dto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "查询参数不能为空");
        }
        if (!MyApplicationRecordType.isValid(dto.getRecordType())) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "非法的 recordType: " + dto.getRecordType());
        }
        Long recordId = parseRecordId(dto.getRecordId());

        return switch (dto.getRecordType().trim()) {
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE -> buildCreateDetail(wxId, recordId);
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN -> buildJoinDetail(wxId, recordId);
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM -> buildJoinPlatformDetail(wxId, recordId);
            case MyApplicationRecordType.MERCHANT_APPLICATION -> buildMerchantDetail(wxId, recordId);
            case MyApplicationRecordType.SHOP_APPLICATION -> buildShopDetail(wxId, recordId);
            default -> throw new BusinessException(ErrorType.SYSTEM_ERROR, "未知记录类型");
        };
    }

    @Override
    public boolean updateMyApplicationRecord(Long wxId, UpdateMyApplicationRecordDto dto) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户未登录");
        }
        if (dto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "更新参数不能为空");
        }
        if (!MyApplicationRecordType.isValid(dto.getRecordType())) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "非法的 recordType: " + dto.getRecordType());
        }
        Long recordId = parseRecordId(dto.getRecordId());
        if (dto.getPayload() == null || dto.getPayload().isNull()) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "payload不能为空");
        }

        String type = dto.getRecordType().trim();
        if (MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN.equals(type)) {
            UpdateAlumniAssociationJoinApplicationDto updateDto =
                    objectMapper.convertValue(dto.getPayload(), UpdateAlumniAssociationJoinApplicationDto.class);
            updateDto.setApplicationId(recordId);
            return alumniAssociationJoinApplicationService.updateAndResubmitApplication(wxId, updateDto);
        }
        if (MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM.equals(type)) {
            UpdateMyJoinPlatformApplicationDto updateDto =
                    objectMapper.convertValue(dto.getPayload(), UpdateMyJoinPlatformApplicationDto.class);
            if (updateDto.getPlatformId() == null) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "platformId不能为空");
            }
            AlumniAssociationJoinApply apply = alumniAssociationJoinApplyService.getById(recordId);
            if (apply == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "申请记录不存在");
            }
            if (!wxId.equals(apply.getApplicantWxId())) {
                throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权修改该申请");
            }
            if (apply.getStatus() == null || apply.getStatus() != 0) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "只能编辑待审核的申请");
            }
            apply.setPlatformId(updateDto.getPlatformId());
            return alumniAssociationJoinApplyService.updateById(apply);
        }
        throw new BusinessException(ErrorType.ARGS_ERROR, "当前类型暂不支持编辑: " + type);
    }

    @Override
    public boolean cancelMyApplicationRecord(Long wxId, CancelMyApplicationRecordDto dto) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户未登录");
        }
        if (dto == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "撤销参数不能为空");
        }
        if (!MyApplicationRecordType.isValid(dto.getRecordType())) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "非法的 recordType: " + dto.getRecordType());
        }
        Long recordId = parseRecordId(dto.getRecordId());
        String type = dto.getRecordType().trim();

        if (MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE.equals(type)) {
            return alumniAssociationApplicationService.cancelPendingApplication(wxId, recordId);
        }
        if (MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN.equals(type)) {
            return alumniAssociationJoinApplicationService.cancelApplication(wxId, recordId);
        }
        if (MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM.equals(type)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "当前类型不支持撤销: " + type);
        }
        if (MyApplicationRecordType.MERCHANT_APPLICATION.equals(type)) {
            return merchantService.cancelPendingMerchantApplication(wxId, recordId);
        }
        if (MyApplicationRecordType.SHOP_APPLICATION.equals(type)) {
            return shopService.cancelPendingShopApplication(wxId, recordId);
        }
        throw new BusinessException(ErrorType.ARGS_ERROR, "当前类型暂不支持撤销: " + type);
    }

    private MyApplicationRecordDetailVo buildCreateDetail(Long wxId, Long recordId) {
        AlumniAssociationApplication application = alumniAssociationApplicationService.getById(recordId);
        if (application == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "申请记录不存在");
        }
        if (!wxId.equals(application.getZhWxId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权查看该申请详情");
        }

        AlumniAssociationApplicationDetailVo detail = alumniAssociationApplicationService.getApplicationDetailById(recordId);
        MyApplicationRecordDetailVo vo = new MyApplicationRecordDetailVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE);
        vo.setRecordId(String.valueOf(recordId));
        vo.setApplicationStatus(application.getApplicationStatus());
        vo.setApplicationStatusText(fourStateText(application.getApplicationStatus()));
        vo.setStatusGroup(fourStateGroup(application.getApplicationStatus()));
        boolean pending = application.getApplicationStatus() != null && application.getApplicationStatus() == 0;
        vo.setCanEdit(pending);
        vo.setCanCancel(pending);
        vo.setDetail(detail);
        return vo;
    }

    private MyApplicationRecordDetailVo buildJoinDetail(Long wxId, Long recordId) {
        AlumniAssociationJoinApplication application = alumniAssociationJoinApplicationService.getById(recordId);
        if (application == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "申请记录不存在");
        }
        if (!wxId.equals(application.getTargetId()) || application.getApplicantType() == null || application.getApplicantType() != 1) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权查看该申请详情");
        }

        AlumniAssociationJoinApplicationDetailVo detail = alumniAssociationJoinApplicationService.getApplicationDetailById(recordId);
        MyApplicationRecordDetailVo vo = new MyApplicationRecordDetailVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN);
        vo.setRecordId(String.valueOf(recordId));
        vo.setApplicationStatus(application.getApplicationStatus());
        vo.setApplicationStatusText(AlumniAssociationJoinApplicationListVo.getApplicationStatusText(application.getApplicationStatus()));
        vo.setStatusGroup(fourStateGroup(application.getApplicationStatus()));
        boolean pending = application.getApplicationStatus() != null && application.getApplicationStatus() == 0;
        vo.setCanEdit(pending);
        vo.setCanCancel(pending);
        vo.setDetail(detail);
        return vo;
    }

    private MyApplicationRecordDetailVo buildJoinPlatformDetail(Long wxId, Long recordId) {
        AlumniAssociationJoinApply application = alumniAssociationJoinApplyService.getById(recordId);
        if (application == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "申请记录不存在");
        }
        if (!wxId.equals(application.getApplicantWxId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权查看该申请详情");
        }

        AlumniAssociationJoinApplyDetailVo detail = alumniAssociationJoinApplyService.getApplyDetailWithAttachmentById(recordId);
        MyApplicationRecordDetailVo vo = new MyApplicationRecordDetailVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM);
        vo.setRecordId(String.valueOf(recordId));
        vo.setApplicationStatus(application.getStatus());
        vo.setApplicationStatusText(threeStateText(application.getStatus()));
        vo.setStatusGroup(threeStateGroup(application.getStatus()));
        boolean pending = application.getStatus() != null && application.getStatus() == 0;
        vo.setCanEdit(pending);
        vo.setCanCancel(false);
        vo.setDetail(detail);
        return vo;
    }

    private MyApplicationRecordDetailVo buildMerchantDetail(Long wxId, Long recordId) {
        Merchant merchant = merchantService.getById(recordId);
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在");
        }
        if (!wxId.equals(merchant.getUserId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权查看该申请详情");
        }
        MerchantDetailVo detail = MerchantDetailVo.objToVo(merchant);
        if (StringUtils.isNotBlank(merchant.getAlumniAssociationId())) {
            List<Long> associationIds = parseAssociationIds(merchant.getAlumniAssociationId());
            if (!associationIds.isEmpty()) {
                AlumniAssociation alumniAssociation = alumniAssociationService.getById(associationIds.get(0));
                if (alumniAssociation != null) {
                    detail.setAlumniAssociation(AlumniAssociationListVo.objToVo(alumniAssociation));
                }
            }
        }
        MyApplicationRecordDetailVo vo = new MyApplicationRecordDetailVo();
        vo.setRecordType(MyApplicationRecordType.MERCHANT_APPLICATION);
        vo.setRecordId(String.valueOf(recordId));
        vo.setApplicationStatus(merchant.getReviewStatus());
        vo.setApplicationStatusText(threeStateText(merchant.getReviewStatus()));
        vo.setStatusGroup(threeStateGroup(merchant.getReviewStatus()));
        boolean merchantPending = merchant.getReviewStatus() != null && merchant.getReviewStatus() == 0;
        vo.setCanEdit(merchantPending);
        vo.setCanCancel(merchantPending);
        vo.setDetail(detail);
        vo.setMaterialImages(collectMerchantMaterialImages(detail));
        return vo;
    }

    private MyApplicationRecordDetailVo buildShopDetail(Long wxId, Long recordId) {
        Shop shop = shopService.getById(recordId);
        if (shop == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "门店不存在");
        }
        if (!wxId.equals(shop.getCreatedBy())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权查看该申请详情");
        }
        ShopDetailVo detailVo;
        if (shop.getReviewStatus() != null && shop.getReviewStatus() == 1) {
            detailVo = shopService.getShopDetail(recordId);
        } else {
            detailVo = buildShopDetailVoFromEntity(shop);
        }
        MyApplicationRecordDetailVo vo = new MyApplicationRecordDetailVo();
        vo.setRecordType(MyApplicationRecordType.SHOP_APPLICATION);
        vo.setRecordId(String.valueOf(recordId));
        vo.setApplicationStatus(shop.getReviewStatus());
        vo.setApplicationStatusText(threeStateText(shop.getReviewStatus()));
        vo.setStatusGroup(threeStateGroup(shop.getReviewStatus()));
        boolean shopPending = shop.getReviewStatus() != null && shop.getReviewStatus() == 0;
        vo.setCanEdit(shopPending);
        vo.setCanCancel(shopPending);
        vo.setDetail(detailVo);
        vo.setMaterialImages(collectShopMaterialImages(detailVo));
        return vo;
    }

    private List<MaterialImageItemVo> collectMerchantMaterialImages(MerchantDetailVo d) {
        List<MaterialImageItemVo> list = new ArrayList<>();
        if (d == null) {
            return list;
        }
        addMaterialImageIfPresent(list, "LOGO", "商家 Logo", d.getLogo());
        addMaterialImageIfPresent(list, "LICENSE", "营业执照", d.getBusinessLicense());
        appendJsonImageArray(list, "BACKGROUND", "背景图", d.getBackgroundImage());
        return list;
    }

    private List<MaterialImageItemVo> collectShopMaterialImages(ShopDetailVo d) {
        List<MaterialImageItemVo> list = new ArrayList<>();
        if (d == null) {
            return list;
        }
        addMaterialImageIfPresent(list, "SHOP_LOGO", "门店 Logo", d.getLogo());
        appendJsonImageArray(list, "SHOP_IMAGE", "门店图片", d.getShopImages());
        return list;
    }

    private void addMaterialImageIfPresent(List<MaterialImageItemVo> list, String kind, String label, String url) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        MaterialImageItemVo vo = new MaterialImageItemVo();
        vo.setKind(kind);
        vo.setLabel(label);
        vo.setUrl(url.trim());
        list.add(vo);
    }

    /**
     * 解析 JSON 数组（字符串 URL 或 {url|fileUrl}）；解析失败则按单张 URL 处理（与小程序编辑页一致）。
     */
    private void appendJsonImageArray(List<MaterialImageItemVo> list, String kind, String labelPrefix, String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }
        String t = json.trim();
        try {
            JsonNode root = objectMapper.readTree(t);
            if (root.isArray()) {
                if (root.size() == 0) {
                    return;
                }
                int idx = 0;
                for (JsonNode n : root) {
                    String u = extractImageUrlFromJsonNode(n);
                    if (StringUtils.isNotBlank(u)) {
                        String label = root.size() > 1 ? labelPrefix + " " + (idx + 1) : labelPrefix;
                        addMaterialImageIfPresent(list, kind, label, u);
                    }
                    idx++;
                }
                return;
            }
        } catch (Exception ex) {
            log.debug("解析图片 JSON 失败，按单 URL 处理: {}", ex.getMessage());
        }
        addMaterialImageIfPresent(list, kind, labelPrefix, t);
    }

    private static String extractImageUrlFromJsonNode(JsonNode n) {
        if (n == null || n.isNull()) {
            return null;
        }
        if (n.isTextual()) {
            return n.asText();
        }
        if (n.isObject()) {
            if (n.has("url") && !n.get("url").isNull()) {
                return n.get("url").asText();
            }
            if (n.has("fileUrl") && !n.get("fileUrl").isNull()) {
                return n.get("fileUrl").asText();
            }
        }
        return null;
    }

    /**
     * 未审核通过时无法走 {@link ShopService#getShopDetail(Long)}，改为组装基础详情。
     */
    private ShopDetailVo buildShopDetailVoFromEntity(Shop shop) {
        ShopDetailVo vo = new ShopDetailVo();
        vo.setShopId(shop.getShopId() != null ? String.valueOf(shop.getShopId()) : null);
        vo.setShopName(shop.getShopName());
        vo.setShopType(shop.getShopType());
        vo.setProvince(shop.getProvince());
        vo.setCity(shop.getCity());
        vo.setDistrict(shop.getDistrict());
        vo.setAddress(shop.getAddress());
        vo.setLatitude(shop.getLatitude());
        vo.setLongitude(shop.getLongitude());
        vo.setPhone(shop.getPhone());
        vo.setBusinessHours(shop.getBusinessHours());
        vo.setShopImages(shop.getShopImages());
        vo.setLogo(shop.getLogo());
        vo.setDescription(shop.getDescription());
        vo.setStatus(shop.getStatus());
        vo.setReviewStatus(shop.getReviewStatus());
        vo.setReviewReason(shop.getReviewReason());
        vo.setReviewTime(shop.getReviewTime());
        vo.setIsRecommended(shop.getIsRecommended());
        vo.setCreateTime(shop.getCreateTime());
        vo.setUpdateTime(shop.getUpdateTime());
        if (shop.getMerchantId() != null) {
            Merchant m = merchantService.getById(shop.getMerchantId());
            if (m != null) {
                MerchantListVo mvo = MerchantListVo.objToVo(m);
                mvo.setMerchantId(String.valueOf(m.getMerchantId()));
                if (m.getUserId() != null) {
                    mvo.setUserId(String.valueOf(m.getUserId()));
                }
                if (m.getAlumniAssociationId() != null) {
                    mvo.setAlumniAssociationId(String.valueOf(m.getAlumniAssociationId()));
                }
                vo.setMerchant(mvo);
            }
        }
        return vo;
    }

    private static Long parseRecordId(String recordId) {
        if (StringUtils.isBlank(recordId)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "recordId不能为空");
        }
        try {
            return Long.valueOf(recordId.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "recordId格式错误: " + recordId);
        }
    }

    private void validateRecordTypes(List<String> recordTypes) {
        if (recordTypes == null || recordTypes.isEmpty()) {
            return;
        }
        for (String t : recordTypes) {
            if (StringUtils.isBlank(t) || !MyApplicationRecordType.isValid(t)) {
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

    /**
     * 创建校友会：申请人即驻会代表，对应 zh_wx_id
     */
    private List<MergedRow> loadCreateAssociationRows(Long wxId, String statusGroup) {
        LambdaQueryWrapper<AlumniAssociationApplication> w = new LambdaQueryWrapper<>();
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

    private List<MergedRow> loadMerchantRows(Long wxId, String statusGroup) {
        LambdaQueryWrapper<Merchant> w = new LambdaQueryWrapper<>();
        w.eq(Merchant::getUserId, wxId);
        applyThreeStateFilter(w, statusGroup, Merchant::getReviewStatus);
        w.orderByDesc(Merchant::getCreateTime);
        List<Merchant> list = merchantService.list(w);
        List<MergedRow> rows = new ArrayList<>();
        for (Merchant e : list) {
            LocalDateTime t = e.getCreateTime() != null ? e.getCreateTime() : e.getUpdateTime();
            rows.add(new MergedRow(MyApplicationRecordType.MERCHANT_APPLICATION, e.getMerchantId(), t, e));
        }
        return rows;
    }

    private List<MergedRow> loadShopRows(Long wxId, String statusGroup) {
        LambdaQueryWrapper<Shop> w = new LambdaQueryWrapper<>();
        w.eq(Shop::getCreatedBy, wxId);
        applyThreeStateFilter(w, statusGroup, Shop::getReviewStatus);
        w.orderByDesc(Shop::getCreateTime);
        List<Shop> list = shopService.list(w);
        List<MergedRow> rows = new ArrayList<>();
        for (Shop e : list) {
            LocalDateTime t = e.getCreateTime() != null ? e.getCreateTime() : e.getUpdateTime();
            rows.add(new MergedRow(MyApplicationRecordType.SHOP_APPLICATION, e.getShopId(), t, e));
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
            case "CANCELLED" -> w.eq(statusGetter, 3);
            default -> throw new BusinessException(ErrorType.ARGS_ERROR, "非法的 statusGroup: " + statusGroup);
        }
    }

    private MyApplicationRecordListVo toVo(MergedRow row) {
        return switch (row.recordType) {
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE -> toCreateVo((AlumniAssociationApplication) row.payload);
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN -> toJoinVo((AlumniAssociationJoinApplication) row.payload);
            case MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM -> toJoinPlatformVo((AlumniAssociationJoinApply) row.payload);
            case MyApplicationRecordType.MERCHANT_APPLICATION -> toMerchantVo((Merchant) row.payload);
            case MyApplicationRecordType.SHOP_APPLICATION -> toShopVo((Shop) row.payload);
            default -> throw new BusinessException(ErrorType.SYSTEM_ERROR, "未知记录类型");
        };
    }

    private MyApplicationRecordListVo toCreateVo(AlumniAssociationApplication e) {
        MyApplicationRecordListVo vo = new MyApplicationRecordListVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_CREATE);
        vo.setRecordId(String.valueOf(e.getApplicationId()));
        vo.setTitle(StringUtils.defaultIfBlank(e.getAssociationName(), "创建校友会申请"));
        vo.setSubtitle(buildSchoolSubtitle(e.getSchoolId()));
        vo.setAssociationLogo(e.getLogo());
        vo.setAlumniAssociationId(e.getCreatedAssociationId() != null ? String.valueOf(e.getCreatedAssociationId()) : null);
        vo.setPlatformId(e.getPlatformId() != null ? String.valueOf(e.getPlatformId()) : null);
        vo.setApplicationStatus(e.getApplicationStatus());
        vo.setApplicationStatusText(fourStateText(e.getApplicationStatus()));
        vo.setStatusGroup(fourStateGroup(e.getApplicationStatus()));
        vo.setApplyTime(e.getApplyTime());
        boolean pending = e.getApplicationStatus() != null && e.getApplicationStatus() == 0;
        vo.setCanEdit(pending);
        vo.setCanCancel(pending);
        return vo;
    }

    private MyApplicationRecordListVo toJoinVo(AlumniAssociationJoinApplication e) {
        MyApplicationRecordListVo vo = new MyApplicationRecordListVo();
        vo.setRecordType(MyApplicationRecordType.ALUMNI_ASSOCIATION_JOIN);
        vo.setRecordId(String.valueOf(e.getApplicationId()));
        AlumniAssociation ass = alumniAssociationService.getById(e.getAlumniAssociationId());
        vo.setTitle(ass != null ? StringUtils.defaultIfBlank(ass.getAssociationName(), "加入校友会申请") : "加入校友会申请");
        vo.setSubtitle(ass != null ? ass.getLocation() : null);
        vo.setAssociationLogo(ass != null ? ass.getLogo() : null);
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
        vo.setAssociationLogo(ass != null ? ass.getLogo() : null);
        String platformName = null;
        String platformAvatar = null;
        if (e.getPlatformId() != null) {
            try {
                LocalPlatformDetailVo p = localPlatformService.getLocalPlatformById(e.getPlatformId());
                if (p != null) {
                    platformName = p.getPlatformName();
                    platformAvatar = p.getAvatar();
                }
            } catch (Exception ex) {
                log.debug("加载校促会信息失败 platformId={}", e.getPlatformId(), ex);
            }
        }
        vo.setPlatformLogo(platformAvatar);
        vo.setSubtitle(platformName != null ? "校促会：" + platformName : null);
        vo.setAlumniAssociationId(String.valueOf(e.getAlumniAssociationId()));
        vo.setPlatformId(e.getPlatformId() != null ? String.valueOf(e.getPlatformId()) : null);
        vo.setApplicationStatus(e.getStatus());
        vo.setApplicationStatusText(threeStateText(e.getStatus()));
        vo.setStatusGroup(threeStateGroup(e.getStatus()));
        vo.setApplyTime(e.getCreateTime());
        boolean pending = e.getStatus() != null && e.getStatus() == 0;
        vo.setCanEdit(pending);
        vo.setCanCancel(false);
        return vo;
    }

    private MyApplicationRecordListVo toMerchantVo(Merchant e) {
        MyApplicationRecordListVo vo = new MyApplicationRecordListVo();
        vo.setRecordType(MyApplicationRecordType.MERCHANT_APPLICATION);
        vo.setRecordId(String.valueOf(e.getMerchantId()));
        vo.setTitle(StringUtils.defaultIfBlank(e.getMerchantName(), "商户入驻申请"));
        vo.setSubtitle(StringUtils.defaultIfBlank(e.getBusinessCategory(), e.getBusinessScope()));
        vo.setAssociationLogo(e.getLogo());
        vo.setPlatformLogo(null);
        vo.setAlumniAssociationId(null);
        vo.setPlatformId(null);
        vo.setApplicationStatus(e.getReviewStatus());
        vo.setApplicationStatusText(threeStateText(e.getReviewStatus()));
        vo.setStatusGroup(threeStateGroup(e.getReviewStatus()));
        vo.setApplyTime(e.getCreateTime());
        boolean merchantListPending = e.getReviewStatus() != null && e.getReviewStatus() == 0;
        vo.setCanEdit(merchantListPending);
        vo.setCanCancel(merchantListPending);
        return vo;
    }

    private MyApplicationRecordListVo toShopVo(Shop e) {
        MyApplicationRecordListVo vo = new MyApplicationRecordListVo();
        vo.setRecordType(MyApplicationRecordType.SHOP_APPLICATION);
        vo.setRecordId(String.valueOf(e.getShopId()));
        vo.setTitle(StringUtils.defaultIfBlank(e.getShopName(), "门店申请"));
        String subtitle = null;
        if (e.getMerchantId() != null) {
            Merchant m = merchantService.getById(e.getMerchantId());
            if (m != null && StringUtils.isNotBlank(m.getMerchantName())) {
                subtitle = "所属商户：" + m.getMerchantName();
            }
        }
        if (StringUtils.isBlank(subtitle)) {
            subtitle = buildShopAddressLine(e);
        }
        vo.setSubtitle(subtitle);
        vo.setAssociationLogo(e.getLogo());
        vo.setPlatformLogo(null);
        vo.setAlumniAssociationId(null);
        vo.setPlatformId(null);
        vo.setApplicationStatus(e.getReviewStatus());
        vo.setApplicationStatusText(threeStateText(e.getReviewStatus()));
        vo.setStatusGroup(threeStateGroup(e.getReviewStatus()));
        vo.setApplyTime(e.getCreateTime());
        boolean shopListPending = e.getReviewStatus() != null && e.getReviewStatus() == 0;
        vo.setCanEdit(shopListPending);
        vo.setCanCancel(shopListPending);
        return vo;
    }

    private static String buildShopAddressLine(Shop e) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(e.getProvince())) {
            sb.append(e.getProvince().trim());
        }
        if (StringUtils.isNotBlank(e.getCity())) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(e.getCity().trim());
        }
        if (StringUtils.isNotBlank(e.getDistrict())) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(e.getDistrict().trim());
        }
        if (StringUtils.isNotBlank(e.getAddress())) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(e.getAddress().trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
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
            case 3 -> "已撤销";
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
            case 3 -> "CANCELLED";
            default -> "UNKNOWN";
        };
    }

    private record MergedRow(String recordType, Long recordId, LocalDateTime sortTime, Object payload) {}
}
