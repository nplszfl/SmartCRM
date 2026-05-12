package com.smartcrm.lead;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.smartcrm.lead.repository")
public class LeadServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeadServiceApplication.class, args);
    }
}
