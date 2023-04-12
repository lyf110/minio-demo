package com.minio.controller;

import com.minio.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lyf
 * @description:
 * @version: v1.0
 * @since 2022-05-12 14:07
 */
@RestController
@Slf4j
@RequestMapping(value = "/video")
@CrossOrigin
public class VideoController {

    @Autowired
    private MinioService minioService;

    /**
     * 支持分段读取视频流
     *
     * @param request    请求对象
     * @param response   响应对象
     * @param bucketName 视频所在桶的位置
     * @param objectName 视频的文件名
     */
    @GetMapping(value = "/play/{bucketName}/{objectName}")
    public void videoPlay(HttpServletRequest request, HttpServletResponse response,
                                  @PathVariable(value = "bucketName") String bucketName,
                                  @PathVariable(value = "objectName") String objectName) {
        minioService.videoPlay(request, response, bucketName, objectName);
    }

    @RequestMapping(value = "/home/{bucketName}/{objectName}")
    public ModelAndView videoHome( @PathVariable(value = "bucketName") String bucketName,
                                   @PathVariable(value = "objectName") String objectName) {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("bucketName", bucketName);
        modelAndView.addObject("objectName", objectName);
        modelAndView.setViewName("video");
        return modelAndView;
    }
}
