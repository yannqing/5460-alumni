package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cmswe.alumni.api.association.SchoolService;
import com.cmswe.alumni.api.user.InvitationService;
import com.cmswe.alumni.common.dto.ConfirmInvitationDto;
import com.cmswe.alumni.common.dto.InviterCountDto;
import com.cmswe.alumni.common.entity.AlumniEducation;
import com.cmswe.alumni.common.entity.AlumniInfo;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.vo.InvitationRankItemVo;
import com.cmswe.alumni.common.vo.InvitationRankVo;
import com.cmswe.alumni.common.vo.InvitationRecordItemVo;
import com.cmswe.alumni.common.vo.InviteeRegisterCheckVo;
import com.cmswe.alumni.common.entity.InvitationRecord;
import com.cmswe.alumni.common.entity.PointsChange;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.user.mapper.AlumniEducationMapper;
import com.cmswe.alumni.service.user.mapper.AlumniInfoMapper;
import com.cmswe.alumni.service.user.mapper.InvitationRecordMapper;
import com.cmswe.alumni.service.user.mapper.PointsChangeMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 邀请服务实现
 */
@Slf4j
@Service
public class InvitationServiceImpl implements InvitationService {

    /** 积分变化类型：0-邀请 */
    private static final int POINTS_TYPE_INVITE = 0;

    @Resource
    private InvitationRecordMapper invitationRecordMapper;

    @Resource
    private PointsChangeMapper pointsChangeMapper;

    @Resource
    private WxUserInfoMapper wxUserInfoMapper;

    @Resource
    private AlumniInfoMapper alumniInfoMapper;

    @Resource
    private AlumniEducationMapper alumniEducationMapper;

    @Resource
    private SchoolService schoolService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmInvitation(ConfirmInvitationDto dto) {
        Long inviterWxId = dto.getInviterWxId();
        Long inviteeWxId = dto.getInviteeWxId();

        // 1. 防止自己邀请自己
        if (inviterWxId.equals(inviteeWxId)) {
            throw new BusinessException(400, "邀请人和被邀请人不能相同");
        }

        // 2. 防止重复确认：同一条邀请记录只处理一次
        long exists = invitationRecordMapper.selectCount(
                new LambdaQueryWrapper<InvitationRecord>()
                        .eq(InvitationRecord::getInviterWxId, inviterWxId)
                        .eq(InvitationRecord::getInviteeWxId, inviteeWxId));
        if (exists > 0) {
            log.info("邀请已确认过，跳过重复处理 inviterWxId={}, inviteeWxId={}", inviterWxId, inviteeWxId);
            return true;
        }

        // 3. 获取被邀请人是否认证（alumni_info.certification_status: 1=已认证）
        int isVerified = 0;
        AlumniInfo alumniInfo = alumniInfoMapper.findByWxIdOrUserId(inviteeWxId);
        if (alumniInfo != null && alumniInfo.getCertificationStatus() != null && alumniInfo.getCertificationStatus() == 1) {
            isVerified = 1;
        }

        // 4. 写入邀请记录表
        InvitationRecord record = new InvitationRecord();
        record.setInviterWxId(inviterWxId);
        record.setInviteeWxId(inviteeWxId);
        record.setIsVerified(isVerified);
        invitationRecordMapper.insert(record);

        // 5. 查询邀请人当前积分
        WxUserInfo inviterInfo = wxUserInfoMapper.findByWxId(inviterWxId);
        int originalPoints = (inviterInfo != null && inviterInfo.getIntegral() != null) ? inviterInfo.getIntegral() : 0;
        int afterPoints = originalPoints + 1;

        // 6. 更新邀请人积分（wx_user_info.integral + 1）
        LambdaUpdateWrapper<WxUserInfo> updateWrapper = new LambdaUpdateWrapper<WxUserInfo>()
                .setSql("integral = COALESCE(integral, 0) + 1")
                .eq(WxUserInfo::getWxId, inviterWxId);
        int updated = wxUserInfoMapper.update(null, updateWrapper);
        if (updated == 0) {
            // wx_user_info 可能不存在该用户记录，需要先插入
            WxUserInfo newInfo = new WxUserInfo();
            newInfo.setWxId(inviterWxId);
            newInfo.setIntegral(1);
            wxUserInfoMapper.insert(newInfo);
            originalPoints = 0;
            afterPoints = 1;
        }

        // 7. 记录积分变化
        PointsChange pointsChange = new PointsChange();
        pointsChange.setWxId(inviterWxId);
        pointsChange.setType(POINTS_TYPE_INVITE);
        pointsChange.setOriginalPoints(originalPoints);
        pointsChange.setAfterPoints(afterPoints);
        pointsChangeMapper.insert(pointsChange);

        log.info("确认邀请成功 inviterWxId={}, inviteeWxId={}, isVerified={}, originalPoints={}, afterPoints={}",
                inviterWxId, inviteeWxId, isVerified, originalPoints, afterPoints);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InviteeRegisterCheckVo checkInviteeRegistration(Long wxId) {
        if (wxId == null) {
            return InviteeRegisterCheckVo.builder().isInvitee(false).isRegistered(false).build();
        }
        // 1. 邀请记录表中是否存在该 wxid 作为被邀请人
        List<InvitationRecord> records = invitationRecordMapper.selectList(
                new LambdaQueryWrapper<InvitationRecord>().eq(InvitationRecord::getInviteeWxId, wxId));
        if (records == null || records.isEmpty()) {
            return InviteeRegisterCheckVo.builder().isInvitee(false).isRegistered(false).build();
        }
        // 2. 用 wxid 查 wx_user_info，看 nickname、name 是否已填写
        WxUserInfo userInfo = wxUserInfoMapper.findByWxId(wxId);
        boolean nicknameFilled = userInfo != null && StringUtils.hasText(userInfo.getNickname());
        boolean nameFilled = userInfo != null && StringUtils.hasText(userInfo.getName());
        boolean isRegistered = nicknameFilled && nameFilled;
        // 3. 若已填写，将对应邀请记录的 is_register 更新为 1
        if (isRegistered) {
            LambdaUpdateWrapper<InvitationRecord> updateWrapper = new LambdaUpdateWrapper<InvitationRecord>()
                    .set(InvitationRecord::getIsRegister, 1)
                    .eq(InvitationRecord::getInviteeWxId, wxId);
            invitationRecordMapper.update(null, updateWrapper);
            log.info("被邀请人已注册，更新邀请记录 is_register=1, inviteeWxId={}", wxId);
        }
        return InviteeRegisterCheckVo.builder()
                .isInvitee(true)
                .isRegistered(isRegistered)
                .build();
    }

    @Override
    public List<InvitationRecordItemVo> getMyInvitationList(Long inviterWxId) {
        if (inviterWxId == null) {
            return new ArrayList<>();
        }
        List<InvitationRecord> records = invitationRecordMapper.selectList(
                new LambdaQueryWrapper<InvitationRecord>()
                        .eq(InvitationRecord::getInviterWxId, inviterWxId)
                        .orderByDesc(InvitationRecord::getCreateTime));
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<InvitationRecordItemVo> result = new ArrayList<>(records.size());
        for (InvitationRecord r : records) {
            WxUserInfo inviteeInfo = wxUserInfoMapper.findByWxId(r.getInviteeWxId());
            InvitationRecordItemVo vo = InvitationRecordItemVo.builder()
                    .id(r.getId())
                    .inviteeWxId(r.getInviteeWxId())
                    .inviteeNickname(inviteeInfo != null ? inviteeInfo.getNickname() : null)
                    .inviteeName(inviteeInfo != null ? inviteeInfo.getName() : null)
                    .isVerified(r.getIsVerified() != null ? r.getIsVerified() : 0)
                    .isRegister(r.getIsRegister() != null ? r.getIsRegister() : 0)
                    .createTime(r.getCreateTime())
                    .build();
            result.add(vo);
        }
        return result;
    }

    @Override
    public InvitationRankVo getInvitationRank(Long myWxId) {
        List<InviterCountDto> countList = invitationRecordMapper.selectInviteCountByInviter();
        if (countList == null || countList.isEmpty()) {
            return InvitationRankVo.builder()
                    .myInviteCount(myWxId != null ? 0 : null)
                    .myRank(null)
                    .rankList(new ArrayList<>())
                    .build();
        }
        Integer myInviteCount = null;
        Integer myRank = null;
        List<InvitationRankItemVo> rankList = new ArrayList<>(countList.size());
        for (int i = 0; i < countList.size(); i++) {
            InviterCountDto d = countList.get(i);
            int rank = i + 1;
            Long wxId = d.getInviterWxId();
            int inviteCount = d.getInviteCount() != null ? d.getInviteCount().intValue() : 0;
            if (myWxId != null && myWxId.equals(wxId)) {
                myInviteCount = inviteCount;
                myRank = rank;
            }
            WxUserInfo userInfo = wxUserInfoMapper.findByWxId(wxId);
            String avatar = userInfo != null ? userInfo.getAvatarUrl() : null;
            String name = userInfo != null ? userInfo.getName() : null;
            if (name == null || name.isEmpty()) {
                name = userInfo != null ? userInfo.getNickname() : null;
            }
            String school = getSchoolNameByWxId(wxId);
            rankList.add(InvitationRankItemVo.builder()
                    .wxId(wxId)
                    .rank(rank)
                    .inviteCount(inviteCount)
                    .avatar(avatar)
                    .name(name)
                    .school(school)
                    .build());
        }
        if (myWxId != null && myInviteCount == null) {
            myInviteCount = 0;
        }
        return InvitationRankVo.builder()
                .myInviteCount(myInviteCount)
                .myRank(myRank)
                .rankList(rankList)
                .build();
    }

    private String getSchoolNameByWxId(Long wxId) {
        AlumniEducation education = alumniEducationMapper.selectOne(
                new LambdaQueryWrapper<AlumniEducation>()
                        .eq(AlumniEducation::getWxId, wxId)
                        .orderByDesc(AlumniEducation::getType)
                        .last("LIMIT 1"));
        if (education == null || education.getSchoolId() == null) {
            return null;
        }
        School school = schoolService.getById(education.getSchoolId());
        return school != null ? school.getSchoolName() : null;
    }
}
