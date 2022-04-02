package com.example.controlalarm.websocket.config;

/**
 * @description: RedisDbIndex reiddb枚举类 <br>
 * @date: 2020/02/27 15:30 <br>
 * @author: winfo-lixin <br>
 * @version: 1.0 <br>
 */
public enum RedisDbIndex {

    INDEX_0("DB0", 0),
    INDEX_1("DB1", 1),
    INDEX_2("DB2", 2),
    INDEX_3("DB3", 3),
    INDEX_4("DB4", 4),
    INDEX_5("DB5", 5),
    INDEX_6("DB6", 6),
    INDEX_7("DB7", 7),
    INDEX_8("DB8", 8),
    INDEX_9("DB9", 9),
    INDEX_10("DB10", 10),
    INDEX_11("DB11", 11),
    INDEX_12("DB12", 12),
    INDEX_13("DB13", 13),
    INDEX_14("DB14", 14),
    INDEX_15("DB15", 15);


    String key;
    int value;

    RedisDbIndex(String key, int value) {
        this.key = key;
        this.value = value;
    }
}
