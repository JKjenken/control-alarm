package com.example.controlalarm.api;

/**
 * 封装API的错误码
 * @author linjiankai
 */
public interface IErrorCode {
    /**
     * 错误码
     */
    long getCode();
    /**
     * 错误信息
     */
    String getMessage();
}
