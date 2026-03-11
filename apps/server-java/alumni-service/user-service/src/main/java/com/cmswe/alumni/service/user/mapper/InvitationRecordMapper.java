package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.InvitationRecord;
import org.apache.ibatis.annotations.Mapper;

import com.cmswe.alumni.common.dto.InviterCountDto;
import java.util.List;

@Mapper
public interface InvitationRecordMapper extends BaseMapper<InvitationRecord> {

    List<InviterCountDto> selectInviteCountByInviter();
}