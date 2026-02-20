package com.cmswe.alumni.common.constant;

public interface BaseTypeInterface<T> {
    // 获取对应代码
    T getCode();

    // 获取信息描述
    String getMessage();
}
