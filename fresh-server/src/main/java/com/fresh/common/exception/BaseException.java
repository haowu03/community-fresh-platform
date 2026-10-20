package com.fresh.common.exception;

/**
 * 基础业务异常，所有自定义业务异常的父类
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }
}
