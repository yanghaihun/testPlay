package com.yanghaihun.test;

import com.yanghaihun.dao.RecordStore;
import com.yanghaihun.entity.Record;
import com.yanghaihun.service.RecordServiceImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: 杨海珲
 * Data:   2022/7/28 1:31 上午
 */
public class JunitTest {

    @Test
    public void test() throws InterruptedException {
        RecordServiceImpl recordService = new RecordServiceImpl();
        String fileName = Objects.requireNonNull(Record.class.getClassLoader().getResource("data.txt")).getPath();

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str;
            while (null != (str = bufferedReader.readLine())) {
                Record record = recordService.parseOneLineData(str);
                if (record == null) {
                    recordService.continueToLoadData();
                    break;
                } else {
                    recordService.store(record);
                }
            }

        } catch (IOException e) {
            System.out.println("从文件读取数据失败！（文件名:" + fileName + "）,Goodbye and have a nice day :)");
            System.exit(1);
        }

        recordService.store(new Record("USD", (long) 50));
        recordService.store(new Record("USD", (long) -50));
        recordService.store(new Record("UDI", (long) 50));
        boolean usd = RecordStore.getConcurrentHashMap().get("USD") == 50;
        boolean udi = RecordStore.getConcurrentHashMap().get("UDI") == 350;
        Assert.assertTrue(usd);
        Assert.assertTrue(udi);
        ConcurrentHashMap<String, Long> concurrentHashMap = RecordStore.getConcurrentHashMap();

        // 线程安全测试
        Runnable a = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    recordService.store(new Record("NBA", (long) 30));
                }
            }
        };

        Runnable b = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    recordService.store(new Record("NBA", (long) 30));
                }

            }
        };

        Thread a1 = new Thread(a);
        Thread b1 = new Thread(b);
        a1.start();
        b1.start();

        a1.join();
        b1.join();

        boolean nba = RecordStore.getConcurrentHashMap().get("NBA") == 6000;
        Assert.assertTrue(nba);
    }

}
