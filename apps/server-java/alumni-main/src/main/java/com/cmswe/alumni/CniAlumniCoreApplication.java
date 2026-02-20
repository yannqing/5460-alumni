package com.cmswe.alumni;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.cmswe.alumni")
@MapperScan({
        "com.cmswe.alumni.service.*.mapper",  // 业务服务 Mapper
        "com.cmswe.alumni.search.mapper"      // 搜索服务 Mapper
})
@EnableScheduling
public class CniAlumniCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(CniAlumniCoreApplication.class, args);
    }

}
