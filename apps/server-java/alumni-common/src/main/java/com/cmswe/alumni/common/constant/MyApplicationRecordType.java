package com.cmswe.alumni.common.constant;

import java.util.Set;

/**
 * 「我的申请」聚合列表中的业务类型（与前端约定字符串）。
 */
public final class MyApplicationRecordType {

    private MyApplicationRecordType() {}

    /** 创建校友会申请（申请人对应表字段 zh_wx_id，驻会代表） */
    public static final String ALUMNI_ASSOCIATION_CREATE = "ALUMNI_ASSOCIATION_CREATE";

    /** 加入校友会申请（普通用户） */
    public static final String ALUMNI_ASSOCIATION_JOIN = "ALUMNI_ASSOCIATION_JOIN";

    /** 校友会申请加入校促会（当前用户为 applicant_wx_id） */
    public static final String ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM = "ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM";

    /** 商户入驻申请（当前用户为 merchant.user_id） */
    public static final String MERCHANT_APPLICATION = "MERCHANT_APPLICATION";

    /** 商家加入校友会申请 */
    public static final String MERCHANT_ASSOCIATION_JOIN = "MERCHANT_ASSOCIATION_JOIN";

    /** 门店申请（当前用户为 shop.created_by） */
    public static final String SHOP_APPLICATION = "SHOP_APPLICATION";

    public static final Set<String> ALL = Set.of(
            ALUMNI_ASSOCIATION_CREATE,
            ALUMNI_ASSOCIATION_JOIN,
            ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM,
            MERCHANT_APPLICATION,
            MERCHANT_ASSOCIATION_JOIN,
            SHOP_APPLICATION
    );

    public static boolean isValid(String type) {
        return type != null && ALL.contains(type.trim());
    }
}
