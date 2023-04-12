package com.minio.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author lyf
 * @description:
 * @version: v1.0
 * @since 2022-05-11 18:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinioObject {
    private String bucket;
    private String region;
    private String object;
    private String etag;
    private long size;
    private boolean deleteMarker;
    private Map<String, String> userMetadata;
}
