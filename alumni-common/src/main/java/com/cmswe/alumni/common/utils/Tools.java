package com.cmswe.alumni.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class Tools {
    public static boolean contains(String str,String ...args){
        for (String arg : args) {
            if(str.equals(arg)){
                return true;
            }
        }
        return false;
    }

    public static String generateOrderNumber() {
        // 获取当前时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = sdf.format(new Date());

        // 生成一个随机数
        Random random = new Random();
        int randomNumber = random.nextInt(999999);

        // 生成一个UUID的一部分
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 组合生成订单号
        return timestamp + String.format("%06d", randomNumber) + uuidPart;
    }
}