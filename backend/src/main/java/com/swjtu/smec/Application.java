package com.swjtu.smec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧医养管理系统 (SMECMS) - 启动类
 *
 * @author HD2026SMECMS01 Team
 * @since 2026-07-17
 */
@SpringBootApplication
@MapperScan("com.swjtu.smec.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
