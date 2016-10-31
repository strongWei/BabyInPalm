package com.hongsi.babyinpalm.dll.showRecord;

import java.util.List;

/**
 * Created by Administrator on 2016/10/29.
 */

public class OneDayRecordsData {
    private int month;
    private int day;
    private List<RecordData> recordDataList;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public List<RecordData> getRecordDataList() {
        return recordDataList;
    }

    public void setRecordDataList(List<RecordData> recordDataList) {
        this.recordDataList = recordDataList;
    }
}
