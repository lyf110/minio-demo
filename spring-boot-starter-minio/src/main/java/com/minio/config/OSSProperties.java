package com.minio.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lyf
 * @version 1.0
 * @classname OssConfig
 * @description
 * @since 2023/3/16 13:19
 */
@ConfigurationProperties(value = "oss.minio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSSProperties {
    /**
     * 对象存储服务的URL
     */
    private String endpoint;

    /**
     * Access key就像用户ID，可以唯一标识你的账户。
     */
    private String accessKey;

    /**
     * Secret key是你账户的密码。
     */
    private String secretKey;

    /**
     * bucketName是你设置的桶的名称
     */
    private String bucketName;
}
