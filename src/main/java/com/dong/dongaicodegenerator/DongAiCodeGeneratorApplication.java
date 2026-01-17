package com.dong.dongaicodegenerator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.dong.dongaicodegenerator.mapper")
public class DongAiCodeGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DongAiCodeGeneratorApplication.class, args);
    }

}
