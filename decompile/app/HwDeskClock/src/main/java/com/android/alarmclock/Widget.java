package com.android.alarmclock;

import android.content.ContentValues;
import android.database.Cursor;

public class Widget {
    private String mFirstIndex;
    private String mFirstTimeZone;
    private String mSecondIndex;
    private String mSecondTimeZone;
    private int mWidgetId;

    public Widget(Cursor c) {
        this.mWidgetId = c.getInt(c.getColumnIndex("widget_id"));
        this.mFirstTimeZone = c.getString(c.getColumnIndex("first_timezone"));
        this.mSecondTimeZone = c.getString(c.getColumnIndex("second_timezone"));
        this.mFirstIndex = c.getString(c.getColumnIndex("first_index"));
        this.mSecondIndex = c.getString(c.getColumnIndex("second_index"));
    }

    public String getmFirstIndex() {
        return this.mFirstIndex;
    }

    public String getmSecondIndex() {
        return this.mSecondIndex;
    }

    public Widget(int mWidgetId, String mFirstTimeZone, String mSecondTimeZone, String mFirstIndex, String mSecondIndex) {
        this.mWidgetId = mWidgetId;
        this.mFirstTimeZone = mFirstTimeZone;
        this.mSecondTimeZone = mSecondTimeZone;
        this.mFirstIndex = mFirstIndex;
        this.mSecondIndex = mSecondIndex;
    }

    public int queryWidgetID() {
        return this.mWidgetId;
    }

    public String getmFirstTimeZone() {
        return this.mFirstTimeZone;
    }

    public String getmSecondTimeZone() {
        return this.mSecondTimeZone;
    }

    public ContentValues createCheckContentValues() {
        ContentValues values = new ContentValues();
        if (!(getmFirstTimeZone() == null || getmFirstTimeZone().equals(""))) {
            values.put("first_timezone", getmFirstTimeZone());
            values.put("first_index", getmFirstIndex());
        }
        if (!(getmSecondTimeZone() == null || getmSecondTimeZone().equals(""))) {
            values.put("second_timezone", getmSecondTimeZone());
            values.put("second_index", getmSecondIndex());
        }
        return values;
    }
}
