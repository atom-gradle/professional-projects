package com.qian.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
// 这里改为了 implements WebMvcConfigurer，保留 Spring Boot 自动配置
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * 配置 OpenAPI 3
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("表型系统后端API一览")
                        .version("1.0")
                        .description("表型系统后端的RESTful API文档"));
    }

    /**
     * 设置静态资源映射
     * 如果使用 implements WebMvcConfigurer，Knife4j 4.x 通常会自动配置资源。
     * 但为了保险起见，保留 doc.html 的映射。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 knife4j 的文档页面
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        // 映射 webjars (css/js 等资源)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 添加 CORS 跨域配置
     * 允许前端访问后端 API，支持 Authorization Header
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有路径都支持跨域
                .allowedOriginPatterns(allowedOrigins) // 允许指定域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Cache-Control", "X-CSRF-Token") // 允许的请求头
                .allowCredentials(true) // 是否允许携带 cookie
                .maxAge(3600); // 预检请求缓存时间（秒）
    }

}