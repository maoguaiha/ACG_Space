package com.ruoyi.project.common.api;

import com.ruoyi.project.common.exception.BizErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一 REST API 响应封装体
 * <p>
 * 面试亮点：统一的响应格式 {code, msg, data}，支持业务错误码枚举。
 * </p>
 *
 * @author ACG_Space Team
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;

    private int code;
    private String msg;
    private T data;

    // ==================== 成功响应 ====================

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

    // ==================== 错误响应 ====================

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

    /**
     * 使用 BizErrorCode 枚举构建错误响应
     */
    public static <T> Result<T> error(BizErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMessage());
        return result;
    }

    /**
     * 使用 BizErrorCode 枚举 + 自定义消息构建错误响应
     */
    public static <T> Result<T> error(BizErrorCode errorCode, String customMsg) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMsg(customMsg);
        return result;
    }
}
