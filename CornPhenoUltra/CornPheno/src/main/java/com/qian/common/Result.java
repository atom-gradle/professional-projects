package com.qian.common;

import lombok.Getter;

/**
 * @version 1.0.0
 * @author Qian
 * @param <T> data type
 */

@Getter
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result() {}

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> success(int code, T data) {
        return new Result<>(code, "操作成功", data);
    }

    public static <T> Result<T> success(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> clientError(T data) {
        return new Result<>(400, "参数错误", data);
    }

    public static <T> Result<T> clientError(int code, T data) {
        return new Result<>(code, "参数错误", data);
    }

    public static <T> Result<T> clientError(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> serverError(T data) {
        return new Result<>(500, "操作失败", data);
    }


}
