package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.UserFeedbackService;
import com.cmswe.alumni.common.dto.UserFeedbackDto;
import com.cmswe.alumni.common.entity.UserFeedback;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.UserFeedbackVo;
import com.cmswe.alumni.service.system.mapper.UserFeedbackMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户反馈服务实现
 */
@Slf4j
@Service
public class UserFeedbackServiceImpl extends ServiceImpl<UserFeedbackMapper, UserFeedback>
        implements UserFeedbackService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFeedbackVo submitFeedback(Long wxId, UserFeedbackDto feedbackDto) {
        try {
            log.info("[UserFeedbackService] 用户提交反馈 - 用户ID: {}, 反馈类型: {}, 反馈标题: {}",
                    wxId, feedbackDto.getFeedbackType(), feedbackDto.getFeedbackTitle());

            // 1. 参数校验
            if (wxId == null) {
                throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
            }

            if (feedbackDto.getFeedbackType() == null || feedbackDto.getFeedbackType() < 1 || feedbackDto.getFeedbackType() > 5) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "反馈类型不合法");
            }

            // 2. 创建反馈记录
            UserFeedback userFeedback = new UserFeedback();
            BeanUtils.copyProperties(feedbackDto, userFeedback);
            userFeedback.setWxId(wxId);
            userFeedback.setFeedbackStatus(0); // 0-待处理
            userFeedback.setCreateTime(LocalDateTime.now());
            userFeedback.setUpdateTime(LocalDateTime.now());

            // 3. 保存到数据库
            boolean saved = this.save(userFeedback);
            if (!saved) {
                log.error("[UserFeedbackService] 反馈提交失败 - 用户ID: {}", wxId);
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "反馈提交失败");
            }

            log.info("[UserFeedbackService] 反馈提交成功 - 用户ID: {}, 反馈ID: {}", wxId, userFeedback.getFeedbackId());

            // 4. 返回反馈详情
            return UserFeedbackVo.objToVo(userFeedback);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[UserFeedbackService] 提交反馈异常 - 用户ID: {}, Error: {}", wxId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "提交反馈失败，请稍后重试");
        }
    }
}
