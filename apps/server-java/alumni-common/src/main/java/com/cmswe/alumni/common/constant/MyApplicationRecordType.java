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

    public static final Set<String> ALL = Set.of(
            ALUMNI_ASSOCIATION_CREATE,
            ALUMNI_ASSOCIATION_JOIN,
            ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM
    );

    public static boolean isValid(String type) {
        return type != null && ALL.contains(type.trim());
    }
}
