package com.cmswe.alumni.search.converter;

import com.cmswe.alumni.common.entity.AlumniInfo;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.SearchType;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import com.cmswe.alumni.search.document.AlumniDocument;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 校友数据转换器
 *
 * @author CNI Alumni System
 */
public class AlumniConverter {

    /**
     * 实体转换为 ES Document
     */
    public static AlumniDocument toDocument(AlumniInfo alumniInfo, WxUser wxUser, WxUserInfo wxUserInfo) {
        AlumniDocument.AlumniDocumentBuilder builder = AlumniDocument.builder()
                .alumniId(alumniInfo.getAlumniId())
                .userId(alumniInfo.getUserId())
                .realName(alumniInfo.getRealName())
                .certificationStatus(alumniInfo.getCertificationStatus() != null ?
                        alumniInfo.getCertificationStatus().toString() : null)
                .certified(Integer.valueOf(1).equals(alumniInfo.getCertificationStatus()));

        // 从微信用户信息补充
        if (wxUser != null) {
            builder.wxId(wxUser.getWxId() != null ? wxUser.getWxId().toString() : null)
                    .lastLoginTime(wxUser.getLastLoginTime())
                    .createTime(wxUser.getCreatedTime())
                    .updateTime(wxUser.getUpdatedTime());
        }

        if (wxUserInfo != null) {
            builder.nickname(wxUserInfo.getNickname())
                    .avatar(wxUserInfo.getAvatarUrl())
                    .province(wxUserInfo.getCurProvince())
                    .city(wxUserInfo.getCurCity());

            // 设置地理位置（如果有）
            if (wxUserInfo.getLatitude() != null && wxUserInfo.getLongitude() != null) {
                builder.location(new GeoPoint(
                        wxUserInfo.getLatitude().doubleValue(),
                        wxUserInfo.getLongitude().doubleValue()
                ));
            }
        }

        // 隐私设置（需要从 UserPrivacySetting 获取，这里默认为 true）
        builder.searchable(true)
                .privacyLevel("PUBLIC");

        return builder.build();
    }

    /**
     * SearchHit 转换为 SearchResultItem
     */
    public static SearchResultItem toSearchResultItem(SearchHit<AlumniDocument> hit) {
        AlumniDocument doc = hit.getContent();

        // 构建高亮文本
        String highlightText = buildHighlightText(hit);

        // 构建额外信息
        Map<String, Object> extra = new HashMap<>();
        extra.put("schoolName", doc.getSchoolName());
        extra.put("graduationYear", doc.getGraduationYear());
        extra.put("major", doc.getMajor());
        extra.put("company", doc.getCompany());
        extra.put("position", doc.getPosition());
        extra.put("province", doc.getProvince());
        extra.put("city", doc.getCity());
        extra.put("certified", doc.getCertified());
        extra.put("tags", doc.getTags());

        // 计算距离（如果有地理位置排序）
        Double distance = null;
        if (hit.getSortValues() != null && !hit.getSortValues().isEmpty()) {
            Object distanceValue = hit.getSortValues().get(0);
            if (distanceValue instanceof Number) {
                distance = ((Number) distanceValue).doubleValue();
            }
        }

        return SearchResultItem.builder()
                .type(SearchType.ALUMNI)
                .id(doc.getAlumniId().toString())
                .title(doc.getRealName() != null ? doc.getRealName() : doc.getNickname())
                .subtitle(buildSubtitle(doc))
                .avatar(doc.getAvatar())
                .highlightText(highlightText)
                .distance(distance)
                .score(hit.getScore())
                .extra(extra)
                .createTime(doc.getCreateTime() != null ? doc.getCreateTime().toString() : null)
                .build();
    }

    /**
     * 构建高亮文本
     */
    private static String buildHighlightText(SearchHit<AlumniDocument> hit) {
        if (hit.getHighlightFields() == null || hit.getHighlightFields().isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        hit.getHighlightFields().forEach((field, highlights) -> {
            if (!highlights.isEmpty()) {
                sb.append(highlights.get(0)).append("... ");
            }
        });

        return sb.length() > 0 ? sb.toString().trim() : null;
    }

    /**
     * 构建副标题
     */
    private static String buildSubtitle(AlumniDocument doc) {
        StringBuilder subtitle = new StringBuilder();

        if (doc.getSchoolName() != null) {
            subtitle.append(doc.getSchoolName());
        }

        if (doc.getGraduationYear() != null) {
            if (subtitle.length() > 0) subtitle.append(" · ");
            subtitle.append(doc.getGraduationYear()).append("届");
        }

        if (doc.getMajor() != null) {
            if (subtitle.length() > 0) subtitle.append(" · ");
            subtitle.append(doc.getMajor());
        }

        if (doc.getCompany() != null) {
            if (subtitle.length() > 0) subtitle.append(" | ");
            subtitle.append(doc.getCompany());
        }

        return subtitle.length() > 0 ? subtitle.toString() : null;
    }

    /**
     * 批量转换
     */
    public static List<SearchResultItem> toSearchResultItems(List<SearchHit<AlumniDocument>> hits) {
        return hits.stream()
                .map(AlumniConverter::toSearchResultItem)
                .collect(Collectors.toList());
    }

    /**
     * 从数据同步事件转换为 ES Document
     * 用于 Canal 数据同步场景
     *
     * @param wxUser 微信用户基本信息
     * @param wxUserInfo 微信用户详细信息
     * @param educationList 教育经历列表
     * @return ES文档对象
     */
    public static AlumniDocument toDocumentFromSync(WxUser wxUser, WxUserInfo wxUserInfo,
                                                     List<com.cmswe.alumni.common.entity.AlumniEducation> educationList) {
        AlumniDocument.AlumniDocumentBuilder builder = AlumniDocument.builder();

        // 从 wxUser 获取基本信息
        if (wxUser != null) {
            // 使用 wxId 作为 alumniId（ES 主键）
            builder.alumniId(wxUser.getWxId())
                    .wxId(wxUser.getWxId() != null ? wxUser.getWxId().toString() : null)
                    .userId(wxUser.getWxId())
                    .lastLoginTime(wxUser.getLastLoginTime())
                    .createTime(wxUser.getCreatedTime())
                    .updateTime(wxUser.getUpdatedTime());
        }

        // 从 wxUserInfo 获取详细信息
        if (wxUserInfo != null) {
            builder.nickname(wxUserInfo.getNickname())
                    .avatar(wxUserInfo.getAvatarUrl())
                    .phone(wxUserInfo.getPhone())
                    .email(wxUserInfo.getEmail())
                    .province(wxUserInfo.getCurProvince())
                    .city(wxUserInfo.getCurCity())
//                    .district(wxUserInfo.getCurDistrict())
//                    .company(wxUserInfo.getCompany())
//                    .position(wxUserInfo.getPosition())
//                    .industry(wxUserInfo.getIndustry())
                    .signature(wxUserInfo.getSignature());

            // 设置地理位置
            if (wxUserInfo.getLatitude() != null && wxUserInfo.getLongitude() != null) {
                builder.location(new GeoPoint(
                        wxUserInfo.getLatitude().doubleValue(),
                        wxUserInfo.getLongitude().doubleValue()
                ));
            }
        }

        // 从教育经历获取学校信息（取第一条认证通过的记录，或最新的记录）
        if (educationList != null && !educationList.isEmpty()) {
            com.cmswe.alumni.common.entity.AlumniEducation primaryEducation = educationList.stream()
                    .filter(edu -> Integer.valueOf(1).equals(edu.getCertificationStatus()))
                    .findFirst()
                    .orElse(educationList.get(0));

            builder.schoolId(primaryEducation.getSchoolId())
                    .graduationYear(primaryEducation.getGraduationYear())
                    .major(primaryEducation.getMajor());

            // 设置认证状态
            if (Integer.valueOf(1).equals(primaryEducation.getCertificationStatus())) {
                builder.certified(true)
                        .certificationStatus("VERIFIED");
            } else {
                builder.certified(false)
                        .certificationStatus("UNVERIFIED");
            }
        } else {
            builder.certified(false)
                    .certificationStatus("UNVERIFIED");
        }

        // 默认隐私设置
        builder.searchable(true)
                .privacyLevel("PUBLIC");

        return builder.build();
    }
}
