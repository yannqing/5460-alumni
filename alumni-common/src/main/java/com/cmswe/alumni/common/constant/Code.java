package com.cmswe.alumni.common.constant;

public class Code {

    public static final Integer SUCCESS = 200;              //一般的成功操作
    public static final Integer FAILURE = 500;              //一般的失败操作

    public static final Integer LOGIN_SUCCESS = 20001;      //登录成功
    public static final Integer LOGIN_FAILURE = 20000;      //登录失败
    public static final Integer LOGOUT_SUCCESS = 20010;     //退出成功

    public static final Integer TOKEN_EXPIRE = 10000;       //token过期
    public static final Integer TOKEN_AUTHENTICATE_FAILURE = 10001;       //token认证失败
    public static final Integer TOKEN_ERROR = 10002;       //token错误（无效或为空）

    public static final Integer SIGNATURE_VERIFY_FAILURE = 10003;       //签名验证失败

}
