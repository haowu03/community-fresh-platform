package com.fresh.handler;

import com.fresh.common.exception.BaseException;
import com.fresh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器：捕获业务异常与常见系统异常，统一返回 R 结构
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BaseException.class)
    public R<String> exceptionHandler(BaseException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return R.error(ex.getMessage());
    }

    /** 唯一约束冲突（如用户名重复、重复点赞、重复参团） */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.warn("数据约束冲突: {}", ex.getMessage());
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            return R.error("数据已存在，请勿重复提交");
        }
        return R.error("数据约束冲突");
    }

    /** 静态资源未找到（如浏览器自动请求 favicon.ico），属正常噪音，降级为 debug 日志 */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<String> exceptionHandler(NoResourceFoundException ex) {
        log.debug("静态资源不存在: {}", ex.getResourcePath());
        return R.error("资源不存在");
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public R<String> exceptionHandler(Exception ex) {
        log.error("系统异常", ex);
        return R.error("系统繁忙，请稍后再试");
    }
}
