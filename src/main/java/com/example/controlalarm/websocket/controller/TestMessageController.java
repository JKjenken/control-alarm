package com.example.controlalarm.websocket.controller;

import com.example.controlalarm.websocket.common.Constants;
import com.example.controlalarm.websocket.common.SpringContextUtils;
import com.example.controlalarm.websocket.dto.RedisUserIdentify;
import com.example.controlalarm.websocket.entity.HelloMessage;
import com.example.controlalarm.websocket.entity.RedisWebsocketMsg;
import com.example.controlalarm.websocket.enums.ExpireEnum;
import com.example.controlalarm.websocket.enums.WebSocketChannelEnum;
import com.example.controlalarm.websocket.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;

/**
 * 测试{@link SimpMessagingTemplate}类的基本用法
 *
 * @author linjiankai
 */
@RestController
@RequestMapping("/wsTemplate")
@Slf4j
public class TestMessageController {
    /**
     * 设置订阅频道 channel
     */
    @Value("${spring.redis.message.ship-alarm-user}")
    private String shipAlarm;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimpUserRegistry userRegistry;


    /**
     * 给指定用户发送WebSocket消息（测试用户单对单的发送，暂时没有涉及该处业务的需求）
     */
    @PostMapping("/sendToUser")
    @ResponseBody
    public String chat(HttpServletRequest request) {
        //直接通过接口获取消息接收者以及当前用户信息
        String receiver = request.getParameter("receiver");
        String userId = request.getParameter("userId");
        String orgCode = request.getParameter("orgCode");
        //消息内容
        String msg = request.getParameter("msg");
        HelloMessage resultData = new HelloMessage(MessageFormat.format("{0} say: {1}", userId, msg));
        // FIXME 该处可以采用session从前端获取用户信息【依据系统的登录校验方式不同，可以切换为其他方式获取用户信息，例如JWT或者token】
//        HttpSession session = SpringContextUtils.getSession();
//        User loginUser = (User) session.getAttribute(Constants.SESSION_USER);
//        this.sendToUser(loginUser.getUserName(), receiver, loginUser.getOrgCode(), WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl(), GsonJsonUtil.toJson(resultData));
        this.sendToUser(userId, receiver, orgCode, WebSocketChannelEnum.SHIP_ALARM.getSubscribeUrl(), resultData);

        return "ok";
    }

    /**
     * 给指定用户发送消息，并处理接收者不在线的情况
     *
     * @param sender      消息发送者
     * @param receiver    消息接收者
     * @param orgCode     分组标识（机构代码）
     * @param destination 目的地
     * @param payload     消息正文
     */
    private void sendToUser(String sender, String receiver, String orgCode, String destination, Object payload) {
        RedisUserIdentify identify = new RedisUserIdentify(receiver,orgCode);
        SimpUser simpUser = userRegistry.getUser(receiver);
        //如果接收者存在，则发送消息
        if (simpUser != null && StringUtils.hasText(simpUser.getName())) {
            messagingTemplate.convertAndSendToUser(receiver, destination, payload);
        }
        //如果接收者在线但是没有获取到对应的接收者，则说明接收者连接了集群的其他节点，需要通知接收者连接的那个节点发送消息
        else if (RedisService.isSetMember(Constants.REDIS_WEBSOCKET_USER_SET, identify)) {
            RedisWebsocketMsg<String> redisWebsocketMsg = new RedisWebsocketMsg<>(WebSocketChannelEnum.SHIP_ALARM.getCode(), payload.toString());
            redisWebsocketMsg.setReceiver(receiver);
            RedisService.convertAndSend(shipAlarm, redisWebsocketMsg);
        }
        //否则将消息存储到redis，等用户上线后主动拉取未读消息
        else {
            //存储消息的Redis列表名
            String listKey = Constants.REDIS_UNREAD_MSG_PREFIX + orgCode + ":" + receiver + ":" + destination;
            log.info(MessageFormat.format("消息接收者{0}还未建立WebSocket连接，{1}发送的消息【{2}】将被存储到Redis的【{3}】列表中", receiver, sender, payload, listKey));
            //存储消息到Redis中
            RedisService.addToListRight(listKey, ExpireEnum.UNREAD_MSG, payload);
        }
    }
}
