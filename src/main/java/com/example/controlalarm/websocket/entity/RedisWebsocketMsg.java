package com.example.controlalarm.websocket.entity;

import lombok.Data;

/**
 * Redis中存储WebSocket消息
 *
 * @author linjiankai
 */
@Data
public class RedisWebsocketMsg<T> {
    /**
     * 消息接收者的userId
     */
    private String receiver;
    /**
     * 消息接收者的groupId
     */
    private String groupId;
    /**
     * 消息对应的订阅频道的CODE，参考{@link com.example.controlalarm.websocket.enums.WebSocketChannelEnum}的code字段
     */
    private String channelCode;
    /**
     * 消息正文
     */
    private T content;
    public RedisWebsocketMsg() {

    }
    public RedisWebsocketMsg(String channelCode, T content) {
        this.channelCode = channelCode;
        this.content = content;
    }

}
