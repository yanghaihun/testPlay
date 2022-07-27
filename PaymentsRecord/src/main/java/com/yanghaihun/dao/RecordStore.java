package com.yanghaihun.dao;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: 杨海珲
 * Data:   2022/7/27 11:13 下午
 */
public class RecordStore {
    private static final ConcurrentHashMap<String,Long> concurrentHashMap = new ConcurrentHashMap<>(1024);

    public static ConcurrentHashMap<String, Long> getConcurrentHashMap() {
        return concurrentHashMap;
    }
}
