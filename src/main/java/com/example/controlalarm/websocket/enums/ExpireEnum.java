package com.example.controlalarm.websocket.enums;

import java.util.concurrent.TimeUnit;

/**
 * 过期时间相关枚举
 *
 * @author linjiankai
 */
public enum ExpireEnum {
    //未读消息的有效期为1天
    UNREAD_MSG(1L, TimeUnit.DAYS)
    ;

    /**
     * 过期时间
     */
    private Long time;
    /**
     * 时间单位
     */
    private TimeUnit timeUnit;

    ExpireEnum(Long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public Long getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
