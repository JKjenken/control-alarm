package com.example.controlalarm.websocket.interceptor;

import com.example.controlalarm.websocket.common.Constants;
import com.example.controlalarm.websocket.dto.RedisUserIdentify;
import com.example.controlalarm.websocket.service.RedisService;
import com.example.controlalarm.websocket.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.Map;

/**
 * 自定义{@link ChannelInterceptor}，实现断开连接的处理
 *
 * @author linjiankai
 */
@Slf4j
@Component
public class MyChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Object raw = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
            if (raw instanceof Map) {
                Object loginName = ((Map) raw).get("userId");
                Object groupId = ((Map) raw).get("orgCode");
                if (loginName instanceof LinkedList && groupId instanceof LinkedList) {
                    RedisUserIdentify identify = new RedisUserIdentify(((LinkedList) loginName).get(0).toString(),((LinkedList) groupId).get(0).toString());
                    RedisService.addToSet(Constants.REDIS_WEBSOCKET_USER_SET, JsonUtils.toJson(identify));
                    // 设置当前访问的认证用户
                    accessor.setUser(new MyPrincipal(((LinkedList) loginName).get(0).toString(),((LinkedList) groupId).get(0).toString()));
                }
            }
        }
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        //用户已经断开连接
        if(StompCommand.DISCONNECT.equals(command)){
//            String user = "";
            MyPrincipal principal = (MyPrincipal)accessor.getUser();
            if(principal != null && StringUtils.hasText(principal.getName())){
                RedisUserIdentify identify = new RedisUserIdentify(principal.getName(),principal.getGroupId());
                //从Redis中移除用户
//                user = identify.getUserId();
                RedisService.removeFromSet(Constants.REDIS_WEBSOCKET_USER_SET, JsonUtils.toJson(identify));
            }
//            else{
//                user = accessor.getSessionId();
//            }
//            log.debug(MessageFormat.format("用户{0}的WebSocket连接已经断开", user));
        }
    }

}
