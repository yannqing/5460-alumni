package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.dto.InviterCountDto;
import com.cmswe.alumni.common.entity.InvitationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 邀请记录表 Mapper
 */
@Mapper
public interface InvitationRecordMapper extends BaseMapper<InvitationRecord> {

    /**
     * 按邀请人统计邀请人数，按人数降序（用于排行榜）
     */
    @Select("SELECT inviter_wx_id AS inviter_wx_id, COUNT(*) AS invite_count FROM invitation_record GROUP BY inviter_wx_id ORDER BY invite_count DESC")
    List<InviterCountDto> selectInviteCountByInviter();
}
