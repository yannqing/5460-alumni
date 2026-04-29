package com.cmswe.alumni.common.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 商户信息VO
 */
@Data
public class MerchantInfoVo implements Serializable {
    private String merchantName;
    private String legalPerson;
    private String unifiedSocialCreditCode;
    private String phone;
    private String city;

    private static final long serialVersionUID = 1L;
}
