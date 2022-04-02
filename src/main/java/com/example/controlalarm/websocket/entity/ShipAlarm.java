package com.example.controlalarm.websocket.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 船舶报警消息实体类
 *
 * @author linjiankai
 */
@Data
public class ShipAlarm implements Serializable {
    /**
     * 用户id(推送给个人时需要的receiveId)"
     */
    private String userId;
    /**
     * 机构代码(推送给分组时需要的groupId)
     */
    private String orgCode;
    /**
     * 订阅推送方式[个人 0，分组 1，全部 2]
     */
    @NotNull(message = "订阅推送方式不能为空")
    private Integer subscribeType;

    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 管控类型
     */
    private String controlType;
    /**
     * 对象类型
     */
    private String objectType;
    /**
     * 船舶名称
     */
    private String shipName;
    /**
     * mmsi
     */
    private String mmsi;
    /**
     * 管控规则ID
     */
    private String ruleId;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 备注
     */
    private String remark;

}
