package com.example.controlalarm.websocket.mq;

import com.example.controlalarm.websocket.entity.RedisWebsocketMsg;
import com.example.controlalarm.websocket.enums.WebSocketChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;

/**
 * Redis中的WebSocket消息的处理者
 *
 * @author linjiankai
 */
@Slf4j
@Component
public class MessageReceiver {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimpUserRegistry userRegistry;

    /**
     * 处理WebSocket 单对单发送消息
     */
    public void receiveMessage(RedisWebsocketMsg redisWebsocketMsg) {
        log.info(MessageFormat.format("Received Message: {0}", redisWebsocketMsg));
        //1. 取出用户名并判断是否连接到当前应用节点的WebSocket
        SimpUser simpUser = userRegistry.getUser(redisWebsocketMsg.getReceiver());

        if (simpUser != null && StringUtils.hasText(simpUser.getName())) {
            //2. 获取WebSocket客户端的订阅地址
            WebSocketChannelEnum channelEnum = WebSocketChannelEnum.fromCode(redisWebsocketMsg.getChannelCode());

            if (channelEnum != null) {
                //3. 给WebSocket客户端发送消息
                messagingTemplate.convertAndSendToUser(redisWebsocketMsg.getReceiver(), channelEnum.getSubscribeUrl(), redisWebsocketMsg.getContent());
            }
        }
    }

    /**
     * 处理WebSocket 分组发送消息
     */
    public void receiveGroupMessage(RedisWebsocketMsg redisWebsocketMsg) {
        log.info(MessageFormat.format("Received Message: {0}", redisWebsocketMsg));
        //1. 获取WebSocket客户端的订阅地址
        WebSocketChannelEnum channelEnum = WebSocketChannelEnum.fromCode(redisWebsocketMsg.getChannelCode());
        //2. 获取分组标识
        String groupId = redisWebsocketMsg.getGroupId();
        if (channelEnum != null) {
            //3. 给WebSocket客户端发送消息
            messagingTemplate.convertAndSend(channelEnum.getSubscribeUrl() + "/" + groupId, redisWebsocketMsg.getContent());
        }
    }

    /**
     * 处理WebSocket 向所有客户端发送消息
     */
    public void receiveAllMessage(RedisWebsocketMsg redisWebsocketMsg) {
        log.info(MessageFormat.format("Received Message: {0}", redisWebsocketMsg));
        //1. 获取WebSocket客户端的订阅地址
        WebSocketChannelEnum channelEnum = WebSocketChannelEnum.fromCode(redisWebsocketMsg.getChannelCode());
        if (channelEnum != null) {
            //2. 给WebSocket客户端发送消息
            messagingTemplate.convertAndSend(channelEnum.getSubscribeUrl(), redisWebsocketMsg.getContent());
        }
    }
}
