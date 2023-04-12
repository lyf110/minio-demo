package com.minio.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minio.core.MinioTemplate;
import com.minio.entity.MinioObject;
import com.minio.entity.OssFile;
import com.minio.entity.Result;
import com.minio.entity.StatusCode;
import com.minio.service.MinioService;
import com.minio.util.FileTypeUtil;
import com.minio.util.Md5Util;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author lyf
 * @version 1.0
 * @classname MinioServiceImpl
 * @description
 * @since 2023/4/12 15:46
 */
@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

    /**
     * 存储视频的元数据列表
     */
    private static final String OBJECT_INFO_LIST = "com:minio:media:objectList";

    /**
     * 已上传文件的md5列表
     */
    private static final String MD5_KEY = "com:minio:file:md5List";

    @Autowired
    private MinioTemplate minioTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource(name = "jsonRedisTemplate")
    private RedisTemplate<String, Serializable> redisTemplate;

    /**
     * 文件上传前的检查，这是为了实现秒传接口
     *
     * @param md5 文件的md5
     * @return 文件是否上传过的元数据
     */
    @Override
    public Map<String, Object> uploadCheck(String md5) {
        Map<String, Object> resultMap = new HashMap<>();
        if (ObjectUtils.isEmpty(md5)) {
            resultMap.put("status", StatusCode.PARAM_ERROR.getCode());
            return resultMap;
        }
        // 先从Redis中查询
        String url = (String) redisTemplate.boundHashOps(MD5_KEY).get(md5);

        // 文件不存在
        if (ObjectUtils.isEmpty(url)) {
            resultMap.put("status", StatusCode.NOT_FOUND.getCode());
            return resultMap;
        }

        resultMap.put("status", StatusCode.SUCCESS.getCode());
        resultMap.put("url", url);
        // 文件已经存在了
        return resultMap;
    }

    /**
     * 文件上传的核心功能
     *
     * @param req 请求
     * @return 上传结果的元数据
     */
    @Override
    public Map<String, Object> upload(HttpServletRequest req) {
        Map<String, Object> map = new HashMap<>();

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;

        // 获得文件分片数据
        MultipartFile file = multipartRequest.getFile("data");

        // 上传过程中出现异常，状态码设置为50000
        if (file == null) {
            map.put("status", StatusCode.FAILURE.getCode());
            return map;
        }
        // 分片第几片
        int index = Integer.parseInt(multipartRequest.getParameter("index"));
        // 总片数
        int total = Integer.parseInt(multipartRequest.getParameter("total"));
        // 获取文件名
        String fileName = multipartRequest.getParameter("name");

        String md5 = multipartRequest.getParameter("md5");

        // 创建文件桶
        minioTemplate.makeBucket(md5);
        String objectName = String.valueOf(index);

        log.info("index: {}, total:{}, fileName:{}, md5:{}, objectName:{}", index, total, fileName, md5, objectName);

        // 当不是最后一片时，上传返回的状态码为20001
        if (index < total) {
            try {
                // 上传文件
                OssFile ossFile = minioTemplate.putChunkObject(file.getInputStream(), md5, objectName);
                log.info("{} upload success {}", objectName, ossFile);

                // 设置上传分片的状态
                map.put("status", StatusCode.ALONE_CHUNK_UPLOAD_SUCCESS.getCode());
                return map;
            } catch (Exception e) {
                e.printStackTrace();
                map.put("status", StatusCode.FAILURE.getCode());
                return map;
            }
        } else {
            // 为最后一片时状态码为20002
            try {
                // 上传文件
                minioTemplate.putChunkObject(file.getInputStream(), md5, objectName);

                // 设置上传分片的状态
                map.put("status", StatusCode.ALL_CHUNK_UPLOAD_SUCCESS.getCode());
                return map;
            } catch (Exception e) {
                e.printStackTrace();
                map.put("status", StatusCode.FAILURE.getCode());
                return map;
            }
        }
    }

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
    @Override
    public Map<String, Object> merge(Integer shardCount, String fileName, String md5, String fileType, Long fileSize) {
        Map<String, Object> retMap = new HashMap<>();

        try {
            // 查询片数据
            List<String> objectNameList = minioTemplate.listObjectNames(md5);
            if (shardCount != objectNameList.size()) {
                // 失败
                retMap.put("status", StatusCode.FAILURE.getCode());
            } else {
                // 开始合并请求
                String targetBucketName = minioTemplate.getDefaultBucketName();
                String filenameExtension = StringUtils.getFilenameExtension(fileName);
                String fileNameWithoutExtension = UUID.randomUUID().toString();
                String objectName = fileNameWithoutExtension + "." + filenameExtension;
                minioTemplate.composeObject(md5, targetBucketName, objectName);

                log.info("桶：{} 中的分片文件，已经在桶：{},文件 {} 合并成功", md5, targetBucketName, objectName);

                // 合并成功之后删除对应的临时桶
                minioTemplate.removeBucket(md5, true);
                log.info("删除桶 {} 成功", md5);

                // 计算文件的md5
                String fileMd5 = null;
                try (InputStream inputStream = minioTemplate.getObject(targetBucketName, objectName)) {
                    fileMd5 = Md5Util.calculateMd5(inputStream);
                } catch (IOException e) {
                    log.error("", e);
                }

                // 计算文件真实的类型
                String type = null;
                List<String> typeList = new ArrayList<>();
                try (InputStream inputStreamCopy = minioTemplate.getObject(targetBucketName, objectName)) {
                    typeList.addAll(FileTypeUtil.getFileRealTypeList(inputStreamCopy, fileName, fileSize));
                } catch (IOException e) {
                    log.error("", e);
                }

                // 并和前台的md5进行对比
                if (!ObjectUtils.isEmpty(fileMd5) && !ObjectUtils.isEmpty(typeList) && fileMd5.equalsIgnoreCase(md5) && typeList.contains(fileType.toLowerCase(Locale.ENGLISH))) {
                    // 表示是同一个文件, 且文件后缀名没有被修改过
                    String url = minioTemplate.getPresignedObjectUrl(targetBucketName, objectName);

                    // 存入redis中
                    redisTemplate.boundHashOps(MD5_KEY).put(fileMd5, url);

                    // 成功
                    retMap.put("status", StatusCode.SUCCESS.getCode());
                } else {
                    log.info("非法的文件信息: 分片数量:{}, 文件名称:{}, 文件fileMd5:{}, 文件真实类型:{}, 文件大小:{}",
                            shardCount, fileName, fileMd5, typeList, fileSize);
                    log.info("非法的文件信息: 分片数量:{}, 文件名称:{}, 文件md5:{}, 文件类型:{}, 文件大小:{}",
                            shardCount, fileName, md5, fileType, fileSize);

                    // 并需要删除对象
                    minioTemplate.deleteObject(targetBucketName, objectName);
                    retMap.put("status", StatusCode.FAILURE.getCode());
                }
            }
        } catch (Exception e) {
            log.error("", e);
            // 失败
            retMap.put("status", StatusCode.FAILURE.getCode());
        }
        return retMap;
    }

    /**
     * 视频播放的核心功能
     *
     * @param request    request
     * @param response   response
     * @param bucketName 视频文件所在的桶
     * @param objectName 视频文件名
     */
    @Override
    public void videoPlay(HttpServletRequest request, HttpServletResponse response, String bucketName, String objectName) {
        // 设置响应报头
        // 需要查询redis
        String key = bucketName + ":" + objectName;
        Object obj = redisTemplate.boundHashOps(OBJECT_INFO_LIST).get(key);

        // 用于记录视频文件的元数据
        // 这里使用Redis的缓存作为优化
        MinioObject minioObject;
        if (obj == null) {
            StatObjectResponse objectInfo = null;
            try {
                objectInfo = minioTemplate.getObjectInfo(bucketName, objectName);
            } catch (Exception e) {
                log.error("{}中{}不存在: {}", bucketName, objectName, e.getMessage());
                response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                try {
                    response.getWriter().write(objectMapper.writeValueAsString(Result.error(StatusCode.NOT_FOUND)));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }
            // 判断是否是视频，是否为mp4格式
            String filenameExtension = StringUtils.getFilenameExtension(objectName);
            if (ObjectUtils.isEmpty(filenameExtension) ||
                    !"mp4".equalsIgnoreCase(filenameExtension.toLowerCase(Locale.ENGLISH))) {
                throw new IllegalArgumentException("不支持的媒体类型, 文件名: " + objectName);
            }

            minioObject = new MinioObject();
            BeanUtils.copyProperties(objectInfo, minioObject);


            redisTemplate.boundHashOps(OBJECT_INFO_LIST).put(key, minioObject);
        } else {
            minioObject = (MinioObject) obj;
        }


        // 获取文件的长度
        long fileSize = minioObject.getSize();
        // Accept-Ranges: bytes
        response.setHeader("Accept-Ranges", "bytes");
        //pos开始读取位置;  last最后读取位置
        long startPos = 0;
        long endPos = fileSize - 1;
        String rangeHeader = request.getHeader("Range");
        if (!ObjectUtils.isEmpty(rangeHeader) && rangeHeader.startsWith("bytes=")) {

            try {
                // 情景一：RANGE: bytes=2000070- 情景二：RANGE: bytes=2000070-2000970
                String numRang = request.getHeader("Range").replaceAll("bytes=", "");
                if (numRang.startsWith("-")) {
                    endPos = fileSize - 1;
                    startPos = endPos - Long.parseLong(new String(numRang.getBytes(StandardCharsets.UTF_8), 1,
                            numRang.length() - 1)) + 1;
                } else if (numRang.endsWith("-")) {
                    endPos = fileSize - 1;
                    startPos = Long.parseLong(new String(numRang.getBytes(StandardCharsets.UTF_8), 0,
                            numRang.length() - 1));
                } else {
                    String[] strRange = numRang.split("-");
                    if (strRange.length == 2) {
                        startPos = Long.parseLong(strRange[0].trim());
                        endPos = Long.parseLong(strRange[1].trim());
                    } else {
                        startPos = Long.parseLong(numRang.replaceAll("-", "").trim());
                    }
                }

                if (startPos < 0 || endPos < 0 || endPos >= fileSize || startPos > endPos) {
                    // SC 要求的范围不满足
                    response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                }

                // 断点续传 状态码206
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            } catch (NumberFormatException e) {
                log.error(request.getHeader("Range") + " is not Number!");
                startPos = 0;
            }
        }

        // 总共需要读取的字节
        long rangLength = endPos - startPos + 1;
        response.setHeader("Content-Range", String.format("bytes %d-%d/%d", startPos, endPos, fileSize));
        response.addHeader("Content-Length", String.valueOf(rangLength));
        //response.setHeader("Connection", "keep-alive");
        response.addHeader("Content-Type", "video/mp4");

        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
             BufferedInputStream bis = new BufferedInputStream(
                     minioTemplate.getObject(bucketName, objectName, startPos, rangLength))) {
            IOUtils.copy(bis, bos);
        } catch (
                IOException e) {
            if (e instanceof ClientAbortException) {
                // ignore 这里就不要打日志，这里的异常原因是用户在拖拽视频进度造成的
            } else {
                log.error(e.getMessage());
            }
        }
    }
}
