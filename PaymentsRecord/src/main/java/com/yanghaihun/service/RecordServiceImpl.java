package com.yanghaihun.service;

import com.yanghaihun.dao.RecordStore;
import com.yanghaihun.entity.Record;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: 杨海珲
 * Data:   2022/7/28 4:01 上午
 */
public class RecordServiceImpl {

    // 匹配数据格式的正则表达式
    private static final String LINE_FORMAT_PATTERN = "[A-Z]{3}\\x20[-]?\\d{1,10}$";
    // 参考格式
    private static final String LINE_FORMAT_SAMPLE_TEXT = "USD 1000";
    // 打印记录间隔时间
    private static final Integer PRINT_RECORD_INTERVAL_TIME_MILLISECONDS = 60000;
    // 退出程序关键字
    private static final String EXIT_TEXT = "exit";
    // 从文件读取的方式类型
    private static final int LOAD_DATA_FROM_FILE_TYPE = 1;
    // 从控制台读取方式的类型
    private static final int LOAD_DATA_FROM_FILE_CONSOLE_TYPE = 2;
    // 选择的类型
    private static int mSelectedLoadType = 0;
    // Scanner
    private static final Scanner scanner = new Scanner(System.in);

    /**
     *  开始加载
     */
    public void startToLoadData() {
        System.out.println("=== Welcome to payment record system! ===");
        System.out.println("请选择您录入数据的方式");
        System.out.println("1.从文件中读取");
        System.out.println("2.从控制台输入");
        System.out.println("请输入选择的数字(输入其他表示退出):");
        String str = scanner.nextLine();
        if (str.equals(String.valueOf(LOAD_DATA_FROM_FILE_TYPE))) {
            mSelectedLoadType = LOAD_DATA_FROM_FILE_TYPE;
        } else if (str.equals(String.valueOf(LOAD_DATA_FROM_FILE_CONSOLE_TYPE))) {
            mSelectedLoadType = LOAD_DATA_FROM_FILE_CONSOLE_TYPE;
        } else {
            exitSystem();
        }
        startIntervalPrintRecord();
        continueToLoadData();
    }

    /**
     * 退出程序
     */
    private void exitSystem() {
        System.out.println("Goodbye and have a nice day :)");
        System.exit(1);
    }

    /**
     * 继续读取数据
     */
    public void continueToLoadData() {
        if(mSelectedLoadType == LOAD_DATA_FROM_FILE_TYPE) {
            LoadFromFile();
        } else {
            LoadFromConsole();
        }
    }

    /**
     * 通过从本地加载文件读取数据
     */
    private void LoadFromFile() {
        System.out.println("请输入文件路径");
        // 绝对路径/Users/haihunyang/java_code/PaymentsRecord/src/main/resources/data.txt macos
        // 相对路径 src/main/resources/data.txt macos or
        // 相对路径 windows 用 \\
        String fileName = scanner.nextLine();

        // 一行一行读
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str;
            while (null != (str = bufferedReader.readLine())) {
                Record record = parseOneLineData(str);
                if (record == null) {
                    continueToLoadData();
                    break;
                } else {
                    store(record);
                }
            }
            // 继续从控制台读取数据
            mSelectedLoadType = 2;
            continueToLoadData();
        } catch (IOException e) {
            System.out.println("从文件读取数据失败！（文件名:" + fileName + "）,Goodbye and have a nice day :)");
            System.exit(1);
        }
    }

    /**
     * 从控制台输入输入货币与金额（格式为USD 1000）
     */
    private void LoadFromConsole() {
        System.out.println("请输入货币与金额，参考格式(" + LINE_FORMAT_SAMPLE_TEXT + "),输入" + EXIT_TEXT + "表示退出");
        while (true) {
            String str = scanner.nextLine();
            if (str.toLowerCase().equals(EXIT_TEXT)) {
                exitSystem();
            }
            //解析
            Record record = parseOneLineData(str);
            if (record == null) {
                continueToLoadData();
                break;
            } else {
                store(record);
            }
        }
    }

    /**
     * 解析一行数据
     * @param line 一行数据
     */
    public Record parseOneLineData(String line) {
        Pattern r = Pattern.compile(LINE_FORMAT_PATTERN);
        Matcher m = r.matcher(line);
        if (!m.matches()) {
            System.out.println("货币名称输入格式错误！(" + line + "), 请重试");
            return null;
        }
        //数据匹配则解析数据
        String[] pair = line.split(" ");
        return new Record(pair[0], Long.parseLong(pair[1]));
    }

    /**
     * 储存数据
     * @param record
     */
    public String store(Record record) {
        // 存储的一个线程安全的hashmap
        ConcurrentHashMap<String, Long> concurrentHashMap = RecordStore.getConcurrentHashMap();

        synchronized (this) {
            // 如果当前货币存在
            if (concurrentHashMap.containsKey(record.getmRecordName())) {
                // 线程安全点 先get 再做操作 再set
                Long oldMoney = concurrentHashMap.get(record.getmRecordName());
                Long newMoney = oldMoney + record.getmRecordMoney();
                // 新金额小于0
                if (newMoney < 0) {
                    String msg = "计算后金额小于0，保存失败";
                    System.out.println("计算后金额小于0，保存失败");
                    return msg;
                } else {
                    concurrentHashMap.put(record.getmRecordName(), newMoney);
                    return "保存成功";
                }
                // 如果当前货币不存在
            } else {
                // 金额小于0
                if (record.getmRecordMoney() <= 0) {
//                System.out.println("金额小于等于0，保存失败");
                    return "金额小于等于0，保存失败";
                } else {
                    concurrentHashMap.put(record.getmRecordName(),record.getmRecordMoney());
                    return "保存成功";
                }
            }
        }
    }

    /**
     * 定时打印记录任务，60秒执行一次
     */
    private void startIntervalPrintRecord() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String s = printRecords();
                System.out.println(s);
            }
        }, 10000, PRINT_RECORD_INTERVAL_TIME_MILLISECONDS);
    }

    /**
     *  打印结果
     */
    public String printRecords() {
        ConcurrentHashMap<String, Long> concurrentHashMap = RecordStore.getConcurrentHashMap();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n=====").append(new Date()).append("统计结果").append("=====");

        for (Map.Entry<String, Long> longEntry : concurrentHashMap.entrySet()) {
            String key;
            key = (String) ((Map.Entry) longEntry).getKey();
            Long value = (Long) ((Map.Entry) longEntry).getValue();

            if (value == 0) {
                continue;
            }

            stringBuilder.append("\n").append(key).append(" ").append(value);
        }

        stringBuilder.append("\n============================================\n");
        return stringBuilder.toString();
    }

    /**
     * 打印某项结果
     * @param recordName 某类货币
     */
    public String printRecords(String recordName) {
        ConcurrentHashMap<String, Long> concurrentHashMap = RecordStore.getConcurrentHashMap();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n=====").append(new Date()).append("统计结果").append("=====");

        if (concurrentHashMap.containsKey(recordName)) {
            stringBuilder.append("\n").append(recordName).append(" ").append(concurrentHashMap.get(recordName));
        }

        stringBuilder.append("\n============================================\n");
        return stringBuilder.toString();
    }

}
