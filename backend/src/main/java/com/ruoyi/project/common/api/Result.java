package com.ruoyi.project.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一 REST API 响应封装体
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
