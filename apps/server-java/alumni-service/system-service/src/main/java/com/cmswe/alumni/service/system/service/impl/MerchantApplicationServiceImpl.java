package com.cmswe.alumni.service.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.system.MerchantApplicationService;
import com.cmswe.alumni.common.entity.MerchantApplication;
import com.cmswe.alumni.service.system.mapper.SystemMerchantApplicationMapper;
import org.springframework.stereotype.Service;

/**
 * 商户申请 Service 实现类
 *
 * @author CNI Alumni System
 */
@Service
public class MerchantApplicationServiceImpl extends ServiceImpl<SystemMerchantApplicationMapper, MerchantApplication> implements MerchantApplicationService {

}
