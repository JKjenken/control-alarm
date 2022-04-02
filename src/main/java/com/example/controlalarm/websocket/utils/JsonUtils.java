package com.example.controlalarm.websocket.utils;

import com.google.gson.Gson;

/**
 * JSON相关公共方法（通过GSON实现）
 *
 * @author linjiankai
 */
public class JsonUtils {
    private static final Gson gson = new Gson();
    /**
     * 将对象转化为json字符串
     * @param source Java对象
     * @return java.lang.String
     */
    public static <K> String toJson(K source){
        return gson.toJson(source);
    }

    /**
     * 将json字符串还原为目标对象
     * @param source json字符串
     * @return K
     */
    public static <T> T fromJson(String source, Class<T> clazz){
        return gson.fromJson(source,clazz);
    }

}
