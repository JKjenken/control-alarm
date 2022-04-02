package com.example.controlalarm.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 用户信息传输类
 *
 * @author linjiankai
 */
@Data
public class UserDTO {
    /**
     * 用户id[唯一标识]
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;
    /**
     * 机构代码 [用于获取对应机构的订阅消息-存储分组标识]
     */
    @NotBlank(message = "机构代码不能为空")
    private String orgCode;
    /**
     * 订阅路径
     */
    @NotBlank(message = "订阅路径不能为空")
    private String destination;
}
