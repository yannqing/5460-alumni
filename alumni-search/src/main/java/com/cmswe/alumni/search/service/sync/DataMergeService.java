package com.cmswe.alumni.search.service.sync;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.common.entity.AlumniEducation;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.model.DataChangeEvent;
import com.cmswe.alumni.search.converter.AlumniConverter;
import com.cmswe.alumni.search.document.AlumniDocument;
import com.cmswe.alumni.service.user.mapper.AlumniEducationMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据合并服务（共享服务）
 *
 * <p>功能：
 * <ul>
 *   <li>从数据变更事件中提取 wxId</li>
 *   <li>查询关联表数据（wx_users + wx_user_info + alumni_education）</li>
 *   <li>合并为完整的 AlumniDocument</li>
 * </ul>
 *
 * <p>设计理念：
 * 多个消费者都需要相同的数据合并逻辑，所以抽取为共享服务，符合 DRY 原则
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Slf4j
@Service
public class DataMergeService {

    @Resource
    private WxUserMapper wxUserMapper;

    @Resource
    private WxUserInfoMapper wxUserInfoMapper;

    @Resource
    private AlumniEducationMapper alumniEducationMapper;

    /**
     * 从事件中提取 wxId
     *
     * @param event 数据变更事件
     * @return wxId，如果无法提取则返回 null
     */
    public Long extractWxId(DataChangeEvent event) {
        if (event == null) {
            return null;
        }

        String table = event.getTable();

        // wx_users 表：主键就是 wx_id
        if ("wx_users".equals(table)) {
            Object wxId = event.getEventType() == DataChangeEvent.EventType.DELETE
                    ? event.getBeforeValue("wx_id")
                    : event.getAfterValue("wx_id");
            return wxId != null ? Long.parseLong(wxId.toString()) : null;
        }

        // wx_user_info 和 alumni_education 表：有 wx_id 外键
        if ("wx_user_info".equals(table) || "alumni_education".equals(table)) {
            Object wxId = event.getEventType() == DataChangeEvent.EventType.DELETE
                    ? event.getBeforeValue("wx_id")
                    : event.getAfterValue("wx_id");
            return wxId != null ? Long.parseLong(wxId.toString()) : null;
        }

        return null;
    }

    /**
     * 合并多表数据为 AlumniDocument
     *
     * @param wxId 微信用户ID
     * @return AlumniDocument，如果数据不存在则返回 null
     */
    public AlumniDocument mergeToDocument(Long wxId) {
        if (wxId == null) {
            log.warn("[DataMergeService] wxId为空，无法合并数据");
            return null;
        }

        try {
            // 查询三张表的数据
            WxUser wxUser = wxUserMapper.selectById(wxId);
            if (wxUser == null) {
                log.warn("[DataMergeService] 用户不存在 - wxId: {}", wxId);
                return null;
            }

            // 注意：wx_user_info 表的主键是 id，不是 wx_id，所以要用 findByWxId
            WxUserInfo wxUserInfo = wxUserInfoMapper.findByWxId(wxId);
            List<AlumniEducation> educationList = alumniEducationMapper.selectList(
                    new LambdaQueryWrapper<AlumniEducation>()
                            .eq(AlumniEducation::getWxId, wxId)
            );

            // 转换为 AlumniDocument
            AlumniDocument document = AlumniConverter.toDocumentFromSync(wxUser, wxUserInfo, educationList);

            log.debug("[DataMergeService] 数据合并完成 - wxId: {}", wxId);
            return document;

        } catch (Exception e) {
            log.error("[DataMergeService] 数据合并失败 - wxId: {}", wxId, e);
            return null;
        }
    }

    /**
     * 验证事件数据完整性
     *
     * @param event 数据变更事件
     * @return 是否验证通过
     */
    public boolean validateEvent(DataChangeEvent event) {
        // 验证事件ID
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            log.warn("[DataMergeService] 事件ID为空");
            return false;
        }

        // 验证表名
        if (event.getTable() == null || event.getTable().isEmpty()) {
            log.warn("[DataMergeService] 表名为空 - EventId: {}", event.getEventId());
            return false;
        }

        // 验证事件类型
        if (event.getEventType() == null) {
            log.warn("[DataMergeService] 事件类型为空 - EventId: {}", event.getEventId());
            return false;
        }

        // 验证数据：INSERT/UPDATE需要afterData，DELETE需要beforeData
        if (event.getEventType() == DataChangeEvent.EventType.INSERT ||
                event.getEventType() == DataChangeEvent.EventType.UPDATE) {
            if (event.getAfterData() == null || event.getAfterData().isEmpty()) {
                log.warn("[DataMergeService] INSERT/UPDATE事件缺少afterData - EventId: {}", event.getEventId());
                return false;
            }
        }

        if (event.getEventType() == DataChangeEvent.EventType.DELETE) {
            if (event.getBeforeData() == null || event.getBeforeData().isEmpty()) {
                log.warn("[DataMergeService] DELETE事件缺少beforeData - EventId: {}", event.getEventId());
                return false;
            }
        }

        return true;
    }
}
