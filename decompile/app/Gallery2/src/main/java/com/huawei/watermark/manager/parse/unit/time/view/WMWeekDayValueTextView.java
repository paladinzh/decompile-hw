package com.huawei.watermark.manager.parse.unit.time.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMText;
import com.huawei.watermark.wmutil.WMUtil;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMWeekDayValueTextView extends WMText {
    private int mDayValue;
    private int mMonthValue;
    private TextView mTextView;
    private int mWeekDayValue;
    private int mWeekValue;
    private int mYearValue;

    public WMWeekDayValueTextView(XmlPullParser parser) {
        super(parser);
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
        consLogicValue();
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        tv.setText(String.valueOf(this.mDayValue));
    }

    private void consLogicValue() {
        consRealDayInMonthValue();
    }

    private void consRealDayInMonthValue() {
        Calendar cal = Calendar.getInstance();
        cal.set(1, this.mYearValue);
        cal.set(2, this.mMonthValue);
        cal.set(4, this.mWeekValue);
        cal.set(7, this.mWeekDayValue);
        this.mDayValue = cal.get(5);
    }

    public void setYearData(int year) {
        this.mYearValue = year;
    }

    public void setMonthData(int month) {
        this.mMonthValue = month;
    }

    public void setWeekData(int week) {
        this.mWeekValue = week;
    }

    public void setWeekDayData(int weekday) {
        this.mWeekDayValue = weekday;
    }

    public void refreshData() {
        consRealDayInMonthValue();
    }

    public void refreshView() {
        this.mTextView.setText(String.valueOf(this.mDayValue));
    }

    public View getView() {
        return this.mTextView;
    }
}
