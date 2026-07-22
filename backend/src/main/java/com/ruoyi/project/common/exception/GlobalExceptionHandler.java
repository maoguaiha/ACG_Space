package com.ruoyi.project.common.exception;

import com.ruoyi.project.common.api.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理各类异常，转换为标准的 Result 响应。
 * 面试亮点：统一的异常处理体系 + 错误码枚举 + 自定义业务异常。
 * </p>
 *
 * @author ACG_Space Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常（最高优先级） ====================

    /**
     * 业务异常 — 所有业务逻辑统一抛 BizException
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getDisplayMessage());
        return Result.error(e.getCode(), e.getDisplayMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * @Validated 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("参数校验失败: {}", errorMessage);
        return Result.error(BizErrorCode.BAD_REQUEST.getCode(), errorMessage);
    }

    /**
     * 绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String errorMessage = e.getAllErrors().get(0).getDefaultMessage();
        log.warn("参数绑定失败: {}", errorMessage);
        return Result.error(BizErrorCode.BAD_REQUEST.getCode(), errorMessage);
    }

    // ==================== 认证/授权异常 ====================

    /**
     * Spring Security 认证异常（未登录）
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(BizErrorCode.UNAUTHORIZED.getCode(), BizErrorCode.UNAUTHORIZED.getMessage());
    }

    /**
     * Spring Security 权限不足
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(BizErrorCode.FORBIDDEN.getCode(), BizErrorCode.FORBIDDEN.getMessage());
    }

    // ==================== JWT 异常 ====================

    /**
     * JWT Token 过期
     */
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleExpiredJwtException(ExpiredJwtException e) {
        log.warn("Token已过期: {}", e.getMessage());
        return Result.error(BizErrorCode.TOKEN_EXPIRED.getCode(), BizErrorCode.TOKEN_EXPIRED.getMessage());
    }

    /**
     * JWT Token 无效
     */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleJwtException(JwtException e) {
        log.warn("Token无效: {}", e.getMessage());
        return Result.error(BizErrorCode.TOKEN_INVALID.getCode(), BizErrorCode.TOKEN_INVALID.getMessage());
    }

    // ==================== 通用异常 ====================

    /**
     * IllegalArgument — 参数不合法
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(BizErrorCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(BizErrorCode.INTERNAL_ERROR);
    }

    /**
     * 兜底异常处理 — 所有未特殊处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统发生未知异常: ", e);
        return Result.error(BizErrorCode.INTERNAL_ERROR.getCode(), BizErrorCode.INTERNAL_ERROR.getMessage());
    }
}
