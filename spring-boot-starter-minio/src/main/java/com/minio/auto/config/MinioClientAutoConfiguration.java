package com.minio.auto.config;

import com.minio.config.OSSProperties;
import com.minio.core.MinioTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lyf
 * @version 1.0
 * @classname MinioClientAutoConfiguration
 * @description Minio自动配置类
 * @since 2023/3/16 13:05
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(OSSProperties.class)
public class MinioClientAutoConfiguration {
    /**
     * 初始化MinioTemplate，封装了一些MinIOClient的基本操作
     *
     * @return MinioTemplate
     */
    @ConditionalOnMissingBean(MinioTemplate.class)
    @Bean(name = "minioTemplate")
    public MinioTemplate minioTemplate() {
        return new MinioTemplate();
    }
}
