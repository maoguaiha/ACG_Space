package com.ruoyi.project.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 统一业务异常类，携带错误码枚举。Service层抛出此异常，
 * GlobalExceptionHandler 统一捕获并转换为标准 Result 响应。
 * </p>
 *
 * @author ACG_Space Team
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 业务错误码
     */
    private final BizErrorCode errorCode;

    /**
     * 可选的动态错误信息（会替换枚举中的默认message）
     */
    private final String detail;

    public BizException(BizErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public BizException(BizErrorCode errorCode, String detail) {
        super(detail != null ? detail : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public BizException(BizErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * 获取最终展示给用户的错误信息
     */
    public String getDisplayMessage() {
        return detail != null ? detail : errorCode.getMessage();
    }

    /**
     * 获取错误码数值
     */
    public int getCode() {
        return errorCode.getCode();
    }
}
