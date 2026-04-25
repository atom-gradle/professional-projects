package com.qian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Qian
 * @version 3.0.0
 */
@SpringBootApplication
@MapperScan("com.qian.mapper")
@EnableAsync
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MainApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }

}
