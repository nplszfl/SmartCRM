package com.smartcrm.crm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * CRM Service Application.
 */
@SpringBootApplication(scanBasePackages = {"com.smartcrm.crm", "com.smartcrm.common"})
@MapperScan("com.smartcrm.crm.repository")
@EnableFeignClients(basePackages = "com.smartcrm.common.ai")
public class CrmServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmServiceApplication.class, args);
    }
}