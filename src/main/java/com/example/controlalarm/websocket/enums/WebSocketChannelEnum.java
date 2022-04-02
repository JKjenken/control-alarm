package com.example.controlalarm.websocket.enums;


import org.springframework.util.StringUtils;

/**
 * WebSocket Channel枚举类
 *
 * @author linjiankai
 */
public enum WebSocketChannelEnum {
    //船舶预警的订阅地址
    SHIP_ALARM("SHIP_ALARM", "船舶预警", "/topic/shipAlarm");

    WebSocketChannelEnum(String code, String description, String subscribeUrl) {
        this.code = code;
        this.description = description;
        this.subscribeUrl = subscribeUrl;
    }

    /**
     * 唯一CODE
     */
    private String code;
    /**
     * 描述
     */
    private String description;
    /**
     * WebSocket客户端订阅的URL
     */
    private String subscribeUrl;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getSubscribeUrl() {
        return subscribeUrl;
    }

    /**
     * 通过CODE查找枚举类
     */
    public static WebSocketChannelEnum fromCode(String code){
        if(StringUtils.hasText(code)){
            for(WebSocketChannelEnum channelEnum : values()){
                if(channelEnum.code.equals(code)){
                    return channelEnum;
                }
            }
        }

        return null;
    }

}
