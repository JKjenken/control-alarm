package com.example.controlalarm.websocket.entity;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


/**
 * 用户信息表
 *
 * @author linjiankai
 */
@Data
public class User {
    /**
     * 用户唯一标识id[身份证号]
     */
    @NotEmpty(message = "用户id不能为空")
    private String userId;
    /**
     * 用户名
     */
    @NotEmpty(message = "用户名不能为空")
    private String userName;
    /**
     * 机构代码
     */
    @NotEmpty(message = "机构代码不能为空")
    private String orgCode;
}
