package com.smartcrm.opportunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.smartcrm.opportunity.repository")
public class OpportunityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpportunityServiceApplication.class, args);
    }
}
