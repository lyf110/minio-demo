package com.minio.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyf
 * @version 1.0
 * @classname MinioTemplate
 * @description OSSFile
 * @since 2023/3/16 13:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssFile {
    /**
     * OSS 存储时文件路径
     */
    private String ossFilePath;
    /**
     * 原始文件名
     */
    private String originalFileName;
}
