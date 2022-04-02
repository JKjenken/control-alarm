package com.example.controlalarm.websocket.config;

import com.example.controlalarm.websocket.interceptor.AuthHandshakeInterceptor;
import com.example.controlalarm.websocket.interceptor.MyChannelInterceptor;
import com.example.controlalarm.websocket.interceptor.MyHandshakeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 *
 * @author linjiankai
 */
@Configuration
@EnableWebSocket
//注解开启使用STOMP协议来传输基于代理(message broker)的消息,这时控制器支持使用@MessageMapping,
//就像使用@RequestMapping一样
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private AuthHandshakeInterceptor authHandshakeInterceptor;

    @Autowired
    private MyHandshakeHandler myHandshakeHandler;

    @Autowired
    private MyChannelInterceptor myChannelInterceptor;

    //注册STOMP协议的节点(endpoint),并映射指定的url
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        // 注册一个Stomp 协议的endpoint,并指定 SockJS协议，前端通过这个端点进行连接
        stompEndpointRegistry.addEndpoint("/topic-websocket")
                .addInterceptors(authHandshakeInterceptor)
                .setHandshakeHandler(myHandshakeHandler)
                //解决跨域问题
                //.setAllowedOrigins("*")
                // SpringBoot2.4.0以上 采用[allowedOriginPatterns]代替[allowedOrigins]
                .setAllowedOriginPatterns("*")
                .withSockJS();


//        stompEndpointRegistry.addEndpoint("endpointWisely")
//                .setHandshakeHandler(new DefaultHandshakeHandler() {
//                    @Override
//                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//                        //将客户端标识封装为Principal对象，从而让服务端能通过getName()方法找到指定客户端
//                        Object o = attributes.get("name");
//                        return new FastPrincipal(o.toString());
//                    }
//                })
//                //添加socket拦截器，用于从请求中获取客户端标识参数
//                .addInterceptors(new HandleShakeInterceptors())
//                //解决跨域问题
//                .setAllowedOrigins("*")
//                .withSockJS();
    }

    // 配置消息代理(message broker)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //点对点应配置一个/user消息代理，广播式应配置一个/topic消息代理;服务端广播消息的路径前缀，客户端需要相应订阅/topic/yyy这个地址的消息
        registry.enableSimpleBroker("/topic");
        //客户端向服务器推送时的默认前缀即为 /app
        registry.setApplicationDestinationPrefixes("/app");
        //点对点使用的订阅前缀（客户端订阅路径上会体现出来），不设置的话，默认也是/user/
        registry.setUserDestinationPrefix("/user/");

//         //使用额外的消息中间件配置
//            registry.enableStompBrokerRelay("/topic", "/queue")
//            .setRelayHost("rabbit.someotherserver")
//            .setRelayPort(62623)
//            .setClientLogin("marcopolo")
//            .setClientPasscode("letmein01");
//            registry.setApplicationDestinationPrefixes("/app", "/foo");
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(myChannelInterceptor);
    }

}
