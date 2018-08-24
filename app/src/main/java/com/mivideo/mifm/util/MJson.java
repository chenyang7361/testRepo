package com.mivideo.mifm.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by aaron on 2016/10/27.
 * 主要用于处理解析Gson字符串的解析
 */
public class MJson {

    private static MJson instance = null;
    private static final Object objs = new Object();
    private Gson gson = null;


    private MJson() {
        gson = new Gson();
    }

    /**
     * MJson初始化操作
     * @return
     */
    public static MJson getInstance() {
        if (instance == null) {
            synchronized (objs) {
                if (instance == null) {
                    instance = new MJson();
                }
            }
        }

        return instance;
    }

    /**
     * 将对象转化为JSON字符串
     * @param objs
     * @return
     */
    public String toGson(Object objs) {
        String result = "";

        try {
            result = gson.toJson(objs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 将JSON字符串转换为对象
     * @param json
     * @param classes
     * @param <T>
     * @return
     */
    public <T> T fromJson(String json, Class<T> classes) {

        return gson.fromJson(json, classes);
    }


    /**
     * 将JSON字符串转换为列表对象
     * @param json
     * @param typeOfT
     * @param <T>
     * @return
     */
    public <T> T fromJson(String json, Type typeOfT) {

        return gson.fromJson(json, typeOfT);
    }

}
