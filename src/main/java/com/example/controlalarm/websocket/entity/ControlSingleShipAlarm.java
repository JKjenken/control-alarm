package com.example.controlalarm.websocket.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class ControlSingleShipAlarm implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 管控id
     */
    private String controlId;

    /**
     * 规则表Id
     */
    private String ruleId;

    /**
     * 中文船名
     */
    private String shipNameCn;

    /**
     * 英文船名
     */
    private String shipNameEn;

    /**
     * mmsi
     */
    private String mmsi;

    /**
     * 规则类型id
     */
    private String ruleTypeId;

    /**
     * 规则类型名称
     */
    private String ruleTypeName;

    /**
     * 管控类型
     */
    private String typeId;

    /**
     * 管控类行名称
     */
    private String typeName;

    /**
     * 状态
     */
    private String status;

    /**
     * 处理结果
     */
    private String results;

    /**
     * 告警时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date alarmDate;

    /**
     * 告警机构
     */
    private String orgCode;

    /**
     * 告警机构名称
     */
    private String orgName;

    /**
     * 管控添加人
     */
    private String addUid;

    /**
     * 管控人名称
     */
    private String addBy;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 描述
     */
    private String description;

    /**
     * 船舶类型
     */
    private String shipType;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系电话
     */
    private String contactNum;

    /**
     * 处理人
     */
    private String handler;


}
