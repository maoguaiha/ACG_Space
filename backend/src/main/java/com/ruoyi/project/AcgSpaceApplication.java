package com.ruoyi.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.ruoyi.project.mapper", "com.ruoyi.project.agent.mapper"})
public class AcgSpaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcgSpaceApplication.class, args);
        System.out.println("* ACG Space Backend Started Successfully *");
    }
}