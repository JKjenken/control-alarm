package com.example.controlalarm.websocket.service;


import com.example.controlalarm.websocket.common.Constants;
import com.example.controlalarm.websocket.dto.RedisUserIdentify;
import com.example.controlalarm.websocket.entity.ControlSingleShipAlarm;
import com.example.controlalarm.websocket.entity.RedisWebsocketMsg;
import com.example.controlalarm.websocket.entity.ShipAlarm;
import com.example.controlalarm.websocket.enums.ExpireEnum;
import com.example.controlalarm.websocket.enums.WebSocketChannelEnum;
import com.example.controlalarm.websocket.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linjiankai
 */
@Slf4j
@Service
public class WebsocketService {
    //设置订阅频道 channel
    @Value("${spring.redis.message.ship-alarm-user}")
    private String alarmUser;
    @Value("${spring.redis.message.ship-alarm-group}")
    private String alarmGroup;
    @Value("${spring.redis.message.ship-alarm-all}")
    private String alarmAll;
    @Autowired
    private SimpUserRegistry userRegistry;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //广播消息
    public void testBroadcast(ShipAlarm alarm) {
        if (alarm.getSubscribeType() == 2) {
            //推送给所有用户
            handleAllOffline(alarm);
        } else if (alarm.getSubscribeType() == 1) {
            //推送给分组用户
            handleGroupOffline(alarm);
        } else {
            //推送给个人用户
            handleSimpleOffline(alarm);
        }
    }

    //广播消息
    public void broadcast(ControlSingleShipAlarm alarm) {
        //推送给分组用户
        handleOffline(alarm);
    }
    private void handleOffline(ControlSingleShipAlarm alarm) {
        //分组标识
        String orgCode = alarm.getOrgCode();

        //向当前服务器上订阅当前分组的所有客户端发送消息
        //messagingTemplate.convertAndSend(WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl() + "/" + orgCode, alarm);

        //向集群内的其他服务器上订阅当前分组的所有客户端发送消息
        RedisWebsocketMsg<ControlSingleShipAlarm> redisWebsocketMsg = new RedisWebsocketMsg<>(WebSocketChannelEnum.SHIP_ALARM.getCode(), alarm);
        redisWebsocketMsg.setGroupId(orgCode);
        RedisService.convertAndSend(alarmGroup, redisWebsocketMsg);

        //获取所有未上线的客户端用户 并将消息存储到redis，等用户上线后主动拉取未读消息

        //TODO 1 连接业务系统查询该分组下的所有用户
        List<String> userList = new ArrayList<>();
        Set<String> allList = userList.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        //2 在redis中获取所有服务器上在线的当前分组的客户端用户信息
        Set<String> clientList = RedisService.rangeSet(Constants.REDIS_WEBSOCKET_USER_SET).stream()
                .map(item -> JsonUtils.fromJson(item.toString(),RedisUserIdentify.class)).filter(identify -> identify.getGroupId().equals(orgCode))
                .map(RedisUserIdentify::getUserId).collect(Collectors.toSet());
        //3 取差集 得到未上线的用户集合
        allList.removeAll(clientList);
        for (String receiver : allList) {
            //4 存储消息的Redis列表名
            String listKey = Constants.REDIS_UNREAD_MSG_PREFIX + orgCode + ":" + receiver + ":" + WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl();
//            log.info(MessageFormat.format("消息接收者{0}还未建立WebSocket连接，服务器发送的消息【{1}】将被存储到Redis的【{2}】列表中", receiver, alarm, listKey));
            //5 存储消息到Redis中
            RedisService.addToListRight(listKey, ExpireEnum.UNREAD_MSG, alarm);
        }
        log.info(MessageFormat.format("消息接收者们 还未建立WebSocket连接，服务器发送的消息【{0}】将被存储到Redis的列表中，已经存储完成", alarm));
    }


    private void handleSimpleOffline(ShipAlarm alarm) {
        RedisUserIdentify identify = new RedisUserIdentify(alarm.getUserId(), alarm.getOrgCode());
        String receiver = identify.getUserId();
        String orgCode = alarm.getOrgCode();
        SimpUser simpUser = userRegistry.getUser(identify.getUserId());
        //如果接收者存在，则发送消息
        if (simpUser != null && StringUtils.hasText(simpUser.getName())) {
            messagingTemplate.convertAndSendToUser(identify.getUserId(), WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl(), JsonUtils.toJson(alarm));
        }
        //如果接收者在线，则说明接收者连接了集群的其他节点，需要通知接收者连接的那个节点发送消息
        else if (RedisService.isSetMember(Constants.REDIS_WEBSOCKET_USER_SET, identify)) {
            RedisWebsocketMsg<String> redisWebsocketMsg = new RedisWebsocketMsg<>(WebSocketChannelEnum.SHIP_ALARM.getCode(), JsonUtils.toJson(alarm));
            redisWebsocketMsg.setReceiver(receiver);
            RedisService.convertAndSend(alarmUser, redisWebsocketMsg);
        }
        //否则将消息存储到redis，等用户上线后主动拉取未读消息
        else {
            //存储消息的Redis列表名
            String listKey = Constants.REDIS_UNREAD_MSG_PREFIX + orgCode + ":" + receiver + ":" + WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl();
//            log.info(MessageFormat.format("消息接收者{0}还未建立WebSocket连接，服务器发送的消息【{1}】将被存储到Redis的【{2}】列表中", receiver, alarm, listKey));
            //存储消息到Redis中
            RedisService.addToListRight(listKey, ExpireEnum.UNREAD_MSG, alarm);
        }
        log.info(MessageFormat.format("消息接收者们{0}还未建立WebSocket连接，服务器发送的消息【{1}】将被存储到Redis的列表中，已经存储完成", receiver, alarm));
    }

    private void handleGroupOffline(ShipAlarm alarm) {
        //分组标识
        String orgCode = alarm.getOrgCode();

        //向当前服务器上订阅当前分组的所有客户端发送消息
        //messagingTemplate.convertAndSend(WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl() + "/" + orgCode, alarm);

        //向集群内的其他服务器上订阅当前分组的所有客户端发送消息
        RedisWebsocketMsg<ShipAlarm> redisWebsocketMsg = new RedisWebsocketMsg<>(WebSocketChannelEnum.SHIP_ALARM.getCode(), alarm);
        redisWebsocketMsg.setGroupId(orgCode);
        RedisService.convertAndSend(alarmGroup, redisWebsocketMsg);

        //获取所有未上线的客户端用户 并将消息存储到redis，等用户上线后主动拉取未读消息

        //TODO 1 连接业务系统查询该分组下的所有用户
        List<String> userList = new ArrayList<>();
        Set<String> allList = userList.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        //2 在redis中获取所有服务器上在线的当前分组的客户端用户信息
        Set<String> clientList = RedisService.rangeSet(Constants.REDIS_WEBSOCKET_USER_SET).stream()
                .map(item -> JsonUtils.fromJson(item.toString(),RedisUserIdentify.class)).filter(identify -> identify.getGroupId().equals(orgCode))
                .map(RedisUserIdentify::getUserId).collect(Collectors.toSet());
        //3 取差集 得到未上线的用户集合
        allList.removeAll(clientList);
        for (String receiver : allList) {
            //4 存储消息的Redis列表名
            String listKey = Constants.REDIS_UNREAD_MSG_PREFIX + orgCode + ":" + receiver + ":" + WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl();
//            log.info(MessageFormat.format("消息接收者{0}还未建立WebSocket连接，服务器发送的消息【{1}】将被存储到Redis的【{2}】列表中", receiver, alarm, listKey));
            //存储消息到Redis中
            RedisService.addToListRight(listKey, ExpireEnum.UNREAD_MSG, alarm);
        }
        log.info(MessageFormat.format("消息接收者们 还未建立WebSocket连接，服务器发送的消息【{0}】将被存储到Redis的列表中，已经存储完成", alarm));
    }

    private void handleAllOffline(ShipAlarm alarm) {
        //分组标识
        String orgCode = alarm.getOrgCode();

        //向当前服务器上订阅的所有客户端发送消息
        //messagingTemplate.convertAndSend(WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl(), alarm);


        //向集群内的其他服务器上订阅的所有客户端发送消息
        RedisWebsocketMsg<ShipAlarm> redisWebsocketMsg = new RedisWebsocketMsg<>(WebSocketChannelEnum.SHIP_ALARM.getCode(), alarm);
        RedisService.convertAndSend(alarmAll, redisWebsocketMsg);

        //获取所有未上线的客户端用户 并将消息存储到redis，等用户上线后主动拉取未读消息

        //TODO 1 连接业务系统查询该分组下的所有用户
        List<String> userList = new ArrayList<>();
        Set<String> allList = userList.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        //2 在redis中获取所有服务器上在线的所有客户端用户信息
        Set<String> clientList = RedisService.rangeSet(Constants.REDIS_WEBSOCKET_USER_SET).stream()
                .map(item -> JsonUtils.fromJson(item.toString(),RedisUserIdentify.class))
                .map(RedisUserIdentify::getUserId).collect(Collectors.toSet());
        //3 取差集 得到未上线的用户集合
        allList.removeAll(clientList);

        for (String receiver : allList) {
            //4 存储消息的Redis列表名
            String listKey = Constants.REDIS_UNREAD_MSG_PREFIX + orgCode + ":" + receiver + ":" + WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl();
//            log.info(MessageFormat.format("消息接收者{0}还未建立WebSocket连接，服务器发送的消息【{1}】将被存储到Redis的【{2}】列表中", receiver, alarm, listKey));
            //存储消息到Redis中
            RedisService.addToListRight(listKey, ExpireEnum.UNREAD_MSG, alarm);
        }
        log.info(MessageFormat.format("消息接收者们还未建立WebSocket连接，服务器发送的消息【{0}】将被存储到Redis的列表中，已经存储完成", alarm));
    }

}
