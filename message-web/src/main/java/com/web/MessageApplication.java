package com.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author 17131
 * @Date 2024/1/7
 * @Description:
 */
@SpringBootApplication
public class MessageApplication {
    public static void main(String[] args) {

        /**
         * 如果你需要启动Apollo动态配置
         * 1、启动apollo
         * 2、将application.properties配置文件的 austin.apollo.enabled 改为true
         * 3、下方的property替换真实的ip和port
         */
        System.setProperty("apollo.config-service", "http://austin-apollo-config:8080");

        SpringApplication.run(MessageApplication.class, args);
    }
}
