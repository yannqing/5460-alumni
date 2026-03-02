package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniHeadquartersService;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.dto.ApplyActivateHeadquartersRequest;
import com.cmswe.alumni.common.dto.AuditHeadquartersRequest;
import com.cmswe.alumni.common.dto.QueryAlumniHeadquartersListDto;
import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import com.cmswe.alumni.common.entity.School;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniHeadquartersDetailVo;
import com.cmswe.alumni.common.vo.AlumniHeadquartersListVo;
import com.cmswe.alumni.common.vo.InactiveAlumniHeadquartersVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.service.association.mapper.AlumniHeadquartersMapper;
import com.cmswe.alumni.service.association.mapper.SchoolMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 校友总会服务实现类
 */
@Slf4j
@Service
public class AlumniHeadquartersServiceImpl extends ServiceImpl<AlumniHeadquartersMapper, AlumniHeadquarters>
        implements AlumniHeadquartersService {

    @Resource
    private SchoolMapper schoolMapper;

    @Override
    public PageVo<AlumniHeadquartersListVo> selectByPage(QueryAlumniHeadquartersListDto infoDTO) {
        if (infoDTO == null) {
            throw new BusinessException("参数为空");
        }

        String headquartersName = infoDTO.getHeadquartersName();
        Long schoolId = infoDTO.getSchoolId();
        String address = infoDTO.getAddress();
        Integer activeStatus = infoDTO.getActiveStatus();
        Integer level = infoDTO.getLevel();
        Integer createCode = infoDTO.getCreateCode();
        int current = infoDTO.getCurrent();
        int pageSize = infoDTO.getPageSize();
        String sortField = infoDTO.getSortField();
        String sortOrder = infoDTO.getSortOrder();

        LambdaQueryWrapper<AlumniHeadquarters> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.isNotBlank(headquartersName), AlumniHeadquarters::getHeadquartersName,
                        headquartersName)
                .eq(schoolId != null, AlumniHeadquarters::getSchoolId, schoolId)
                .like(StringUtils.isNotBlank(address), AlumniHeadquarters::getAddress, address)
                .eq(activeStatus != null, AlumniHeadquarters::getActiveStatus, activeStatus)
                .eq(level != null, AlumniHeadquarters::getLevel, level)
                .eq(createCode != null, AlumniHeadquarters::getCreateCode, createCode)
                .eq(AlumniHeadquarters::getActiveStatus, 1); // 仅查询活跃状态

        // 排序
        queryWrapper
                .orderBy(StringUtils.isNotBlank(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                        AlumniHeadquarters.getSortMethod(sortField))
                .orderByDesc(AlumniHeadquarters::getHeadquartersId);

        Page<AlumniHeadquarters> page = this.page(new Page<>(current, pageSize), queryWrapper);
        List<AlumniHeadquartersListVo> list = page.getRecords().stream().map(alumniHeadquarters -> {
            AlumniHeadquartersListVo vo = AlumniHeadquartersListVo.objToVo(alumniHeadquarters);
            vo.setHeadquartersId(String.valueOf(alumniHeadquarters.getHeadquartersId()));
            return vo;
        }).toList();

        log.info("分页查询校友总会列表");

        Page<AlumniHeadquartersListVo> resultPage = new Page<>(current, pageSize, page.getTotal());
        resultPage.setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public PageVo<AlumniHeadquartersListVo> selectPendingByPage(QueryAlumniHeadquartersListDto infoDTO) {
        if (infoDTO == null) {
            throw new BusinessException("参数为空");
        }

        String headquartersName = infoDTO.getHeadquartersName();
        Long schoolId = infoDTO.getSchoolId();
        Integer approvalStatus = infoDTO.getApprovalStatus();
        int current = infoDTO.getCurrent();
        int pageSize = infoDTO.getPageSize();
        String sortField = infoDTO.getSortField();
        String sortOrder = infoDTO.getSortOrder();

        LambdaQueryWrapper<AlumniHeadquarters> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.isNotBlank(headquartersName), AlumniHeadquarters::getHeadquartersName,
                        headquartersName)
                .eq(schoolId != null, AlumniHeadquarters::getSchoolId, schoolId)
                .eq(approvalStatus != null, AlumniHeadquarters::getApprovalStatus, approvalStatus)
                .ne(AlumniHeadquarters::getActiveStatus, 0);

        // 排序
        queryWrapper
                .orderBy(StringUtils.isNotBlank(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                        AlumniHeadquarters.getSortMethod(sortField))
                .orderByDesc(AlumniHeadquarters::getCreatedTime);

        Page<AlumniHeadquarters> page = this.page(new Page<>(current, pageSize), queryWrapper);
        List<AlumniHeadquartersListVo> list = page.getRecords().stream().map(alumniHeadquarters -> {
            AlumniHeadquartersListVo vo = AlumniHeadquartersListVo.objToVo(alumniHeadquarters);
            vo.setHeadquartersId(String.valueOf(alumniHeadquarters.getHeadquartersId()));
            return vo;
        }).toList();

        log.info("分页查询待审核校友总会列表");

        Page<AlumniHeadquartersListVo> resultPage = new Page<>(current, pageSize, page.getTotal());
        resultPage.setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public boolean applyActivateHeadquarters(ApplyActivateHeadquartersRequest request, Long userId) {
        if (request == null || request.getHeadquartersId() == null
                || request.getCreateCode() == null) {
            throw new BusinessException("申请失败，参数不能为空");
        }

        Long headquartersId = request.getHeadquartersId();
        Long schoolId = request.getSchoolId();
        Integer createCode = request.getCreateCode();

        // 1. 查询校友总会信息
        AlumniHeadquarters alumniHeadquarters = this.getById(headquartersId);
        if (alumniHeadquarters == null) {
            throw new BusinessException("申请失败，校友总会信息不存在");
        }

        // 验证传入的母校 ID 与数据是否匹配（如果传了 schoolId 才校验）
        if (schoolId != null) {
            if (alumniHeadquarters.getSchoolId() != null && !alumniHeadquarters.getSchoolId().equals(schoolId)) {
                throw new BusinessException("申请失败，母校信息不匹配");
            }
            alumniHeadquarters.setSchoolId(schoolId);
        }

        // 2. 验证创建码是否匹配
        if (!createCode.equals(alumniHeadquarters.getCreateCode())) {
            throw new BusinessException("激活失败，创建码不匹配");
        }

        // 3. 更新其他描述信息（非必填）
        if (StringUtils.isNotBlank(request.getLogo())) {
            alumniHeadquarters.setLogo(request.getLogo());
        }
        if (StringUtils.isNotBlank(request.getDescription())) {
            alumniHeadquarters.setDescription(request.getDescription());
        }
        if (StringUtils.isNotBlank(request.getContactInfo())) {
            alumniHeadquarters.setContactInfo(request.getContactInfo());
        }
        if (StringUtils.isNotBlank(request.getAddress())) {
            alumniHeadquarters.setAddress(request.getAddress());
        }
        if (StringUtils.isNotBlank(request.getWebsite())) {
            alumniHeadquarters.setWebsite(request.getWebsite());
        }
        if (StringUtils.isNotBlank(request.getWechatPublicAccount())) {
            alumniHeadquarters.setWechatPublicAccount(request.getWechatPublicAccount());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            alumniHeadquarters.setEmail(request.getEmail());
        }
        if (StringUtils.isNotBlank(request.getPhone())) {
            alumniHeadquarters.setPhone(request.getPhone());
        }
        if (request.getEstablishedDate() != null) {
            alumniHeadquarters.setEstablishedDate(request.getEstablishedDate());
        }
        if (request.getLevel() != null) {
            alumniHeadquarters.setLevel(request.getLevel());
        }

        // 设置创建人和更新人
        Long requestCreatedUserId = request.getCreatedUserId();
        Long requestUpdatedUserId = request.getUpdatedUserId();

        if (alumniHeadquarters.getCreatedUserId() == null) {
            alumniHeadquarters.setCreatedUserId(requestCreatedUserId != null ? requestCreatedUserId : userId);
        }
        alumniHeadquarters.setUpdatedUserId(requestUpdatedUserId != null ? requestUpdatedUserId : userId);

        // 4. 更新激活状态
        alumniHeadquarters.setActiveStatus(1);
        alumniHeadquarters.setApprovalStatus(0);
        boolean result = this.updateById(alumniHeadquarters);

        if (!result) {
            throw new BusinessException("激活失败，系统内部错误");
        }

        log.info("校友总会激活成功 ID: {}, 名称: {}, 操作人: {}", headquartersId, alumniHeadquarters.getHeadquartersName(), userId);
        return true;
    }

    @Override
    public PageVo<InactiveAlumniHeadquartersVo> selectInactiveByPage(QueryAlumniHeadquartersListDto pageRequest) {
        if (pageRequest == null) {
            throw new BusinessException("参数为空");
        }

        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();
        String headquartersName = pageRequest.getHeadquartersName();

        LambdaQueryWrapper<AlumniHeadquarters> queryWrapper = new LambdaQueryWrapper<>();
        // 仅查询活跃状态为 0 (不活跃)
        queryWrapper
                .like(StringUtils.isNotBlank(headquartersName), AlumniHeadquarters::getHeadquartersName,
                        headquartersName)
                .eq(AlumniHeadquarters::getActiveStatus, 0)
                .orderByDesc(AlumniHeadquarters::getCreatedTime);

        Page<AlumniHeadquarters> page = this.page(new Page<>(current, pageSize), queryWrapper);
        List<InactiveAlumniHeadquartersVo> list = page.getRecords().stream()
                .map(record -> {
                    InactiveAlumniHeadquartersVo vo = InactiveAlumniHeadquartersVo.objToVo(record);
                    if (record.getSchoolId() != null) {
                        School school = schoolMapper.selectById(record.getSchoolId());
                        if (school != null) {
                            vo.setLogo(school.getLogo());
                        }
                    }
                    return vo;
                })
                .toList();

        Page<InactiveAlumniHeadquartersVo> resultPage = new Page<>(current, pageSize, page.getTotal());
        resultPage.setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public boolean auditHeadquarters(AuditHeadquartersRequest request, Long userId) {
        if (request == null || request.getHeadquartersId() == null || request.getApprovalStatus() == null) {
            throw new BusinessException("审核失败，请求参数为空或缺少必填项");
        }

        Long headquartersId = request.getHeadquartersId();
        Integer approvalStatus = request.getApprovalStatus();

        AlumniHeadquarters alumniHeadquarters = this.getById(headquartersId);
        if (alumniHeadquarters == null) {
            throw new BusinessException("审核失败，校友总会记录不存在");
        }

        // 仅处理未审核或仍在处理的记录
        if (alumniHeadquarters.getApprovalStatus() != null && alumniHeadquarters.getApprovalStatus() == 1) {
            throw new BusinessException("审核失败，该记录已通过审核，无法重复操作");
        }

        // 处理不同审核状态
        boolean result;
        if (approvalStatus == 1) {
            // 如果审核通过（状态为1），选择性更新基本信息
            alumniHeadquarters.setApprovalStatus(approvalStatus);
            alumniHeadquarters.setUpdatedUserId(userId);

            if (StringUtils.isNotBlank(request.getHeadquartersName())) {
                alumniHeadquarters.setHeadquartersName(request.getHeadquartersName());
            }
            if (StringUtils.isNotBlank(request.getDescription())) {
                alumniHeadquarters.setDescription(request.getDescription());
            }
            if (StringUtils.isNotBlank(request.getContactInfo())) {
                alumniHeadquarters.setContactInfo(request.getContactInfo());
            }
            if (StringUtils.isNotBlank(request.getAddress())) {
                alumniHeadquarters.setAddress(request.getAddress());
            }
            if (StringUtils.isNotBlank(request.getWebsite())) {
                alumniHeadquarters.setWebsite(request.getWebsite());
            }
            if (StringUtils.isNotBlank(request.getWechatPublicAccount())) {
                alumniHeadquarters.setWechatPublicAccount(request.getWechatPublicAccount());
            }
            if (StringUtils.isNotBlank(request.getEmail())) {
                alumniHeadquarters.setEmail(request.getEmail());
            }
            if (StringUtils.isNotBlank(request.getPhone())) {
                alumniHeadquarters.setPhone(request.getPhone());
            }
            if (request.getEstablishedDate() != null) {
                alumniHeadquarters.setEstablishedDate(request.getEstablishedDate());
            }
            if (request.getLevel() != null) {
                alumniHeadquarters.setLevel(request.getLevel());
            }

            // 审核通过后，激活状态也一同改为活跃
            alumniHeadquarters.setActiveStatus(1);

            result = this.updateById(alumniHeadquarters);
        } else if (approvalStatus == 2) {
            // 如果审核驳回（状态为2），清空相关信息并保留最基础数据
            LambdaUpdateWrapper<AlumniHeadquarters> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AlumniHeadquarters::getHeadquartersId, headquartersId)
                    .set(AlumniHeadquarters::getApprovalStatus, 2)
                    .set(AlumniHeadquarters::getLogo, null)
                    .set(AlumniHeadquarters::getDescription, null)
                    .set(AlumniHeadquarters::getContactInfo, null)
                    .set(AlumniHeadquarters::getAddress, null)
                    .set(AlumniHeadquarters::getWebsite, null)
                    .set(AlumniHeadquarters::getWechatPublicAccount, null)
                    .set(AlumniHeadquarters::getEmail, null)
                    .set(AlumniHeadquarters::getPhone, null)
                    .set(AlumniHeadquarters::getEstablishedDate, null)
                    .set(AlumniHeadquarters::getMemberCount, 0)
                    .set(AlumniHeadquarters::getActiveStatus, 0)
                    .set(AlumniHeadquarters::getLevel, 1)
                    .set(AlumniHeadquarters::getCreatedUserId, null)
                    .set(AlumniHeadquarters::getUpdatedUserId, null);

            result = this.update(updateWrapper);
        } else {
            throw new BusinessException("未知的审核状态");
        }

        if (!result) {
            throw new BusinessException("审核失败，系统内部错误");
        }

        log.info("校友总会审核完毕 ID: {}, 审核状态: {}, 操作人: {}", headquartersId, approvalStatus, userId);
        return true;
    }

    @Override
    public AlumniHeadquartersDetailVo getAlumniHeadquartersDetailById(Long id) {
        // 1. 校验id
        if (id == null) {
            throw new BusinessException("参数不能为空，请重试");
        }

        // 2. 查询数据库
        AlumniHeadquarters alumniHeadquarters = this.getById(id);

        // 3. 返回校验值
        if (alumniHeadquarters == null
                || (alumniHeadquarters.getActiveStatus() != null && alumniHeadquarters.getActiveStatus() == 0)) {
            throw new BusinessException("数据不存在或已禁用，请重试");
        }

        // 4. 转换为VO
        AlumniHeadquartersDetailVo detailVo = AlumniHeadquartersDetailVo.objToVo(alumniHeadquarters);

        // 5. 构建母校信息
        Long schoolId = alumniHeadquarters.getSchoolId();
        if (schoolId != null) {
            School school = schoolMapper.selectById(schoolId);
            if (school != null) {
                SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                schoolListVo.setSchoolId(String.valueOf(schoolId));
                detailVo.setSchoolInfo(schoolListVo);
            }
        }

        // 6. 构建创建人和更新人信息（暂时不设置，需要用户服务模块）
        // Long createdUserId = alumniHeadquarters.getCreatedUserId();
        // if (createdUserId != null) {
        // detailVo.setCreatedUser(userService.getUserDetailVoById(createdUserId));
        // }
        // Long updatedUserId = alumniHeadquarters.getUpdatedUserId();
        // if (updatedUserId != null) {
        // detailVo.setUpdatedUser(userService.getUserDetailVoById(updatedUserId));
        // }

        log.info("根据id查询校友总会信息 id: {}", id);

        return detailVo;
    }

    @Override
    public AlumniHeadquartersDetailVo getApplyDetailByAdmin(Long headquartersId) {
        // 1. 校验参数
        if (headquartersId == null) {
            throw new BusinessException("参数不能为空，请重试");
        }

        // 2. 查询数据库（不限活跃/审核状态）
        AlumniHeadquarters alumniHeadquarters = this.getById(headquartersId);
        if (alumniHeadquarters == null) {
            throw new BusinessException("校友总会信息不存在");
        }

        // 3. 转换为 VO
        AlumniHeadquartersDetailVo detailVo = AlumniHeadquartersDetailVo.objToVo(alumniHeadquarters);

        // 4. 构建母校信息
        Long schoolId = alumniHeadquarters.getSchoolId();
        if (schoolId != null) {
            School school = schoolMapper.selectById(schoolId);
            if (school != null) {
                SchoolListVo schoolListVo = SchoolListVo.objToVo(school);
                schoolListVo.setSchoolId(String.valueOf(schoolId));
                detailVo.setSchoolInfo(schoolListVo);
            }
        }

        log.info("管理员查询校友总会申请详情 headquartersId: {}", headquartersId);
        return detailVo;
    }
}
