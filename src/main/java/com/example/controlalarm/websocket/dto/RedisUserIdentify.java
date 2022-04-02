package com.example.controlalarm.websocket.dto;

import lombok.Data;

import java.io.Serializable;


/**
 * redis存储用户标识
 *
 * @author linjiankai
 */
@Data
public class RedisUserIdentify implements Serializable {
    private String userId;
    private String groupId;

    public RedisUserIdentify(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
}
