package com.minio.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lyf
 * @version 1.0
 * @classname Result
 * @description
 * @since 2023/4/12 11:56
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private String message;
    private Integer code;
    private T data;


    /**
     * 成功 并不返回数据
     * @param <T>
     * @return
     */
    public static <T> Result<T> ok() {
        return new Result<>(StatusCode.SUCCESS.getMessage(), StatusCode.SUCCESS.getCode(), null);
    }

    /**
     * 成功 并返回数据
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(StatusCode.SUCCESS.getMessage(), StatusCode.SUCCESS.getCode(), data);
    }

    /**
     * 系统错误 不返回数据
     * @param <T>
     * @return
     */
    public static <T> Result<T> error() {
        return new Result<>(StatusCode.FAILURE.getMessage(), StatusCode.FAILURE.getCode(), null);
    }

    /**
     * 系统错误 并返回逻辑数据
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(T data) {
        return new Result<>(StatusCode.FAILURE.getMessage(), StatusCode.FAILURE.getCode(), data);
    }

    /**
     * 错误并返回指定错误信息和状态码以及逻辑数据
     * @param statusCode
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(StatusCode statusCode, T data) {
        return new Result<>(statusCode.getMessage(), statusCode.getCode(), data);
    }

    /**
     * 错误并返回指定错误信息和状态码 不返回数据
     * @param statusCode
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(StatusCode statusCode) {
        return new Result<>(statusCode.getMessage(), statusCode.getCode(), null);
    }

    /**
     * 自定义错误和状态返回
     * @param message
     * @param code
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> errorMessage(String message, Integer code, T data) {
        return new Result<>(message, code, data);
    }

    /**
     * 自定义错误信息 状态码固定
     * @param message
     * @param <T>
     * @return
     */
    public static <T> Result<T> errorMessage(String message) {
        return new Result<>(message, StatusCode.CUSTOM_FAILURE.getCode(), null);
    }
}
