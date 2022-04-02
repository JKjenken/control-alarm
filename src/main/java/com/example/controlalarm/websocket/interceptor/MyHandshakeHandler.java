package com.example.controlalarm.websocket.interceptor;

import com.example.controlalarm.websocket.common.Constants;
import com.example.controlalarm.websocket.common.SpringContextUtils;
import com.example.controlalarm.websocket.dto.RedisUserIdentify;
import com.example.controlalarm.websocket.entity.User;
import com.example.controlalarm.websocket.service.RedisService;
import com.example.controlalarm.websocket.utils.JsonUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Map;

/**
 * 自定义{@link DefaultHandshakeHandler}，实现“生成自定义的{@link Principal}”
 *
 * @author linjiankai
 */
@Slf4j
@Component
public class MyHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        //将客户端标识封装为Principal对象，从而让服务端能通过getName()方法找到指定客户端
        //FIXME linjiankai 该处采用session获取用户信息【依据系统的登录校验方式不同，可以切换为其他方式获取用户信息，例如JWT或者token】
        HttpSession session = SpringContextUtils.getSession();
        User loginUser = (User) session.getAttribute(Constants.SESSION_USER);

        if(loginUser != null){
            log.debug(MessageFormat.format("WebSocket连接开始创建Principal，用户：{0}", loginUser.getUserName()));
            //1. 将用户标识存到Redis中
            RedisUserIdentify identify = new RedisUserIdentify(loginUser.getUserId(),loginUser.getOrgCode());
            RedisService.addToSet(Constants.REDIS_WEBSOCKET_USER_SET, JsonUtils.toJson(identify));

            //2.     返回自定义的Principal
            return new MyPrincipal(loginUser.getUserId(),loginUser.getOrgCode());
        }else{
//            log.error("未登录系统，禁止连接WebSocket");
            return null;
        }
    }

}
