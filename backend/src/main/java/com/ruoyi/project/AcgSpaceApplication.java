package com.ruoyi.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ruoyi.project.mapper")
public class AcgSpaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcgSpaceApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  ACG Space 后端启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
