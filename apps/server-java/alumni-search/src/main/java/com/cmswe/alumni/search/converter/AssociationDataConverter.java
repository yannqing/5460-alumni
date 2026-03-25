package com.cmswe.alumni.search.converter;

import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.search.document.AssociationDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 校友会数据转换器
 * Entity → ES Document
 *
 * @author CNI Alumni System
 * @since 2026-03-25
 */
@Slf4j
@Component
public class AssociationDataConverter {

    /**
     * 将 AlumniAssociation Entity 转换为 AssociationDocument
     */
    public AssociationDocument toDocument(AlumniAssociation entity) {
        if (entity == null) {
            return null;
        }

        try {
            log.info("[AssociationDataConverter] 转换校友会数据 - AssociationId: {}, AssociationName: {}, Logo: {}",
                entity.getAlumniAssociationId(), entity.getAssociationName(), entity.getLogo());

            return AssociationDocument.builder()
                    .associationId(entity.getAlumniAssociationId())
                    .associationName(entity.getAssociationName())
                    .schoolId(entity.getSchoolId())
                    .platformId(entity.getPlatformId())
                    .presidentName(entity.getChargeName()) // 会长姓名
                    .contactInfo(entity.getContactInfo())
                    // TODO: location 需要从 location 字符串解析经纬度
                    .location(null)
                    // TODO: province/city 需要从 location 字符串解析
                    .province(null)
                    .city(null)
                    .memberCount(entity.getMemberCount())
                    .status(entity.getStatus() != null ? entity.getStatus().toString() : "1")
                    .introduction(entity.getAssociationProfile()) // 校友会简介
                    .coverImage(entity.getLogo()) // 封面图/Logo
                    .schoolName(null) // TODO: 需要关联查询 school 表
                    .platformName(null) // TODO: 需要关联查询 platform 表
                    .createTime(entity.getCreateTime())
                    .updateTime(entity.getUpdateTime())
                    .build();
        } catch (Exception e) {
            log.error("[AssociationDataConverter] 转换失败 - AssociationId: {}", entity.getAlumniAssociationId(), e);
            return null;
        }
    }
}
