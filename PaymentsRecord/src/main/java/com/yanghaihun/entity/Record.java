package com.yanghaihun.entity;

/**
 * Author: 杨海珲
 * Data:   2022/7/27 10:21 下午
 */
public class Record {
    private String mRecordName;
    private Long mRecordMoney;

    public Record(String recordName,Long recordMoney) {
        this.mRecordName = recordName;
        this.mRecordMoney = recordMoney;
    }

    public String getmRecordName() {
        return mRecordName;
    }

    public void setmRecordName(String mRecordName) {
        this.mRecordName = mRecordName;
    }

    public Long getmRecordMoney() {
        return mRecordMoney;
    }

    public void setmRecordMoney(Long mRecordMoney) {
        this.mRecordMoney = mRecordMoney;
    }
}
