package com.example.controlalarm.websocket.service;

import com.example.controlalarm.websocket.common.SpringContextUtils;
import com.example.controlalarm.websocket.enums.ExpireEnum;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author linjiankai
 */
public class RedisService {

    /**
     * 获取RedisTemplate
     */
    private static RedisTemplate<String, Object> template() {
        return (RedisTemplate<String, Object>) SpringContextUtils.getBeanByName("redisTemplate");
    }


    /**
     * 向Redis中存储键值对
     * @author linjiankai
     * @param key KEY
     * @param value VALUE
     */
    public static void set(String key, Object value) {
        ValueOperations<String, Object> valueOperation = template().opsForValue();
        valueOperation.set(key, value);
    }

    /**
     * 向Redis中存储键值对，并设置过期时间
     * @author linjiankai
     * @param key KEY
     * @param value VALUE
     * @param time 过期时间
     * @param timeUnit 时间单位
     */
    public static void setWithExpire(String key, Object value, long time, TimeUnit timeUnit) {
        BoundValueOperations<String, Object> boundValueOperations = template().boundValueOps(key);
        boundValueOperations.set(value);
        boundValueOperations.expire(time,timeUnit);
    }

    /**
     * 从Redis中获取键值对
     * @author linjiankai
     * @param key KEY
     * @return K
     */
    public static <K> K get(String key) {
        ValueOperations<String, Object> valueOperation = template().opsForValue();

        return (K) valueOperation.get(key);
    }

    /**
     * 删除Redis中的某个KEY
     * @author linjiankai
     * @param key KEY
     * @return boolean
     */
    public static boolean delete(String key) {
        return template().delete(key);
    }

    /**
     * 将数据添加到Redis的list中（从左边添加）
     * @author linjiankai
     * @param listKey LIST的key
     * @param expireEnum 有效期的枚举类
     * @param values 待添加的数据
     */
    public static void addToListLeft(String listKey, ExpireEnum expireEnum, Object... values) {
        //绑定操作
        BoundListOperations<String, Object> boundValueOperations = template().boundListOps(listKey);
        //插入数据
        boundValueOperations.leftPushAll(values);
        //设置过期时间
        boundValueOperations.expire(expireEnum.getTime(),expireEnum.getTimeUnit());
    }

    /**
     * 将数据添加到Redis的list中（从右边添加）
     * @author linjiankai
     * @param listKey LIST的key
     * @param expireEnum 有效期的枚举类
     * @param values 待添加的数据
     */
    public static void addToListRight(String listKey, ExpireEnum expireEnum, Object... values) {
        //绑定操作
        BoundListOperations<String, Object> boundValueOperations = template().boundListOps(listKey);
        //插入数据
        boundValueOperations.rightPushAll(values);
        //设置过期时间
        boundValueOperations.expire(expireEnum.getTime(),expireEnum.getTimeUnit());
    }

    /**
     * 根据起始结束序号遍历Redis中的list
     * @author linjiankai
     * @param listKey LIST的key
     * @param start 起始序号
     * @param end 结束序号
     */
    public static List<Object> rangeList(String listKey, long start, long end) {
        //绑定操作
        BoundListOperations<String, Object> boundValueOperations = template().boundListOps(listKey);
        //查询数据
        return boundValueOperations.range(start, end);
    }

    /**
     * 获取Redis中的Set集合
     * @author linjiankai
     * @param setKey Set的key
     * @return
     */
    public static Set<Object> rangeSet(String setKey) {
        SetOperations<String, Object> opsForSet = template().opsForSet();
        //查询数据
        return opsForSet.members(setKey);
    }

    /**
     * 将数据添加Redis的set中
     * @author linjiankai
     * @param setKey SET的key
     * @param values 待添加的数据
     */
    public static void addToSet(String setKey, Object... values) {
        SetOperations<String, Object> opsForSet = template().opsForSet();
        opsForSet.add(setKey, values);
    }

    /**
     * 判断指定数据是否在Redis的set中
     * @author linjiankai
     * @param setKey SET的key
     * @param value 待判断的数据
     * @return java.lang.Boolean
     */
    public static Boolean isSetMember(String setKey, Object value) {
        SetOperations<String, Object> opsForSet = template().opsForSet();

        return opsForSet.isMember(setKey, value);
    }

    /**
     * 从Redis的set中移除数据
     * @author linjiankai
     * @param setKey SET的key
     * @param values 待移除的数据
     */
    public static void removeFromSet(String setKey, Object... values) {
        SetOperations<String, Object> opsForSet = template().opsForSet();
        opsForSet.remove(setKey, values);
    }

    /**
     * 使用Redis的消息队列
     * @author linjiankai
     * @param channel topic name
     * @param message 消息内容
     */
    public static void convertAndSend(String channel, Object message) {
        template().convertAndSend(channel, message);
    }
}
