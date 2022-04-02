package com.example.controlalarm.websocket.interceptor;

import com.example.controlalarm.websocket.common.Constants;
import com.example.controlalarm.websocket.common.SpringContextUtils;
import com.example.controlalarm.websocket.dto.RedisUserIdentify;
import com.example.controlalarm.websocket.entity.User;
import com.example.controlalarm.websocket.service.RedisService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 自定义{@link HandshakeInterceptor}，实现“需要登录才允许连接WebSocket”
 *
 * @author linjiankai
 */
@Component
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) {
        HttpSession session = SpringContextUtils.getSession();
        User loginUser = (User) session.getAttribute(Constants.SESSION_USER);

        if (loginUser == null || !StringUtils.hasText(loginUser.getUserName())) {
//            log.error("未登录系统，禁止连接WebSocket");
            return false;
        }
        RedisUserIdentify identify = new RedisUserIdentify(loginUser.getUserId(), loginUser.getOrgCode());
        if (RedisService.isSetMember(Constants.REDIS_WEBSOCKET_USER_SET, identify)) {
//            log.error("同一个用户不准建立多个连接WebSocket");
            return false;
        } else {
//            log.debug(MessageFormat.format("用户{0}请求建立WebSocket连接", loginUser.getUserName()));
            return true;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }

}
