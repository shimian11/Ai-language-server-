package com.ailang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ailang.service.**.mapper")
public class AiLangApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiLangApplication.class, args);
    }
}
