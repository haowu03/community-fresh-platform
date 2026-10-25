package com.fresh.common.exception;

/**
 * 通用业务异常：库存不足、状态非法、参数错误等业务校验失败时抛出
 */
public class BusinessException extends BaseException {

    public BusinessException(String msg) {
        super(msg);
    }
}
