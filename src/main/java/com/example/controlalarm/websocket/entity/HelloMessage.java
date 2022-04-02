package com.example.controlalarm.websocket.entity;

import lombok.Data;

/**
 * 测试用户单对单的发送
 *
 * @author linjiankai
 */
@Data
public class HelloMessage {
    private String content;

    public HelloMessage(String content) {
        this.content = content;
    }
}
