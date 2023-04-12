package com.minio.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author lyf
 * @version 1.0
 * @classname MinioService
 * @description 这里统一作为Minio文件业务处理类接口
 * @since 2023/4/12 15:37
 */
public interface MinioService {

    /**
     * 文件上传前的检查，这是为了实现秒传接口
     *
     * @param md5 文件的md5
     * @return 文件是否上传过的元数据
     */
    Map<String, Object> uploadCheck(String md5);

    /**
     * 文件上传的核心功能
     *
     * @param req 请求
     * @return 上传结果的元数据
     */
    Map<String, Object> upload(HttpServletRequest req);

    /**
     * 分片文件合并的核心方法
     *
     * @param shardCount 分片数
     * @param fileName   文件名
     * @param md5        文件的md5值
     * @param fileType   文件类型
     * @param fileSize   文件大小
     * @return 合并成功的元数据
     */
    Map<String, Object> merge(Integer shardCount, String fileName, String md5, String fileType,
                              Long fileSize);

    /**
     * 视频播放的核心功能
     *
     * @param request    request
     * @param response   response
     * @param bucketName 视频文件所在的桶
     * @param objectName 视频文件名
     */
    void videoPlay(HttpServletRequest request, HttpServletResponse response,
                   String bucketName,
                   String objectName);
}
