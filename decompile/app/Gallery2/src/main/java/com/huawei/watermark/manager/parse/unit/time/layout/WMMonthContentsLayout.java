package com.huawei.watermark.manager.parse.unit.time.layout;

import android.content.Context;
import android.view.View;
import com.android.gallery3d.R;
import com.huawei.watermark.controller.broadcast.SystemTimeChangeBroadcastRec;
import com.huawei.watermark.controller.callback.WMTimeChangedCallBack;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMRelativeLayout;
import com.huawei.watermark.manager.parse.WaterMark;
import com.huawei.watermark.manager.parse.unit.time.view.WMWeekDayTitleTextView;
import com.huawei.watermark.wmutil.WMResourceUtil;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMMonthContentsLayout extends WMRelativeLayout implements WMTimeChangedCallBack {
    public static final int MONTH_NOW_INDEX = 999;
    private WMWeekDayTitleTextView[] mDayTitles = new WMWeekDayTitleTextView[7];
    private int mMonthValue = 999;
    private int mNowDayOfWeekIndex = -1;
    private int mNowMonthIndex = -1;
    private WMWeekContentsLayout mNowWeekContentsLayout;
    private int mNowWeekIndex = -1;
    SystemTimeChangeBroadcastRec mSystemTimeChangeBroadcastRec = new SystemTimeChangeBroadcastRec(this);
    private WMWeekContentsLayout[] mWeekContentsLayout = new WMWeekContentsLayout[6];
    private String[] mWeekDayIdsInWeek = null;
    private String[] mWeekIdsInMonth = null;
    private String mWeekNowIdInMonth = null;
    private int mYearValue = 999;
    private View monthContentsLayoutRootView;

    public WMMonthContentsLayout(XmlPullParser parser) {
        super(parser);
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        if (context != null) {
            initBaseData(context);
            getChildrenElements();
            consLogicValue(context);
            setDataToChildren();
            super.initBaseLogicData(context, delegate);
        }
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        context.getApplicationContext().registerReceiver(this.mSystemTimeChangeBroadcastRec, this.mSystemTimeChangeBroadcastRec.getFilter());
        View rootLayout = super.toView(context, wm, parentLayoutMode, ori);
        this.monthContentsLayoutRootView = rootLayout;
        return rootLayout;
    }

    public void addElement(WMElement element) {
        super.addElement(element);
    }

    public void setYearData(int year) {
        this.mYearValue = year;
    }

    public void setMonthData(int month) {
        this.mMonthValue = month;
    }

    private void consLogicValue(Context context) {
        consYearValue();
        consMonthValue();
    }

    private void initBaseData(Context context) {
        if (context != null) {
            this.mWeekDayIdsInWeek = context.getResources().getStringArray(WMResourceUtil.getArrayId(context, "water_mark_weektitle"));
            this.mWeekIdsInMonth = context.getResources().getStringArray(WMResourceUtil.getArrayId(context, "water_mark_weekid"));
            this.mWeekNowIdInMonth = context.getResources().getString(R.string.other_calendarwatermark_nowweek_id);
            Calendar calendar = Calendar.getInstance();
            this.mNowMonthIndex = calendar.get(2);
            this.mNowWeekIndex = calendar.get(4);
            this.mNowDayOfWeekIndex = calendar.get(7);
        }
    }

    private void consYearValue() {
        if (this.mYearValue == 999) {
            this.mYearValue = Calendar.getInstance().get(1);
        }
    }

    private void consMonthValue() {
        if (this.mMonthValue == 999) {
            this.mMonthValue = Calendar.getInstance().get(2);
        }
    }

    private void getChildrenElements() {
        int i;
        this.mNowWeekContentsLayout = (WMWeekContentsLayout) getElementById(this.mWeekNowIdInMonth);
        for (i = 0; i < this.mDayTitles.length; i++) {
            this.mDayTitles[i] = (WMWeekDayTitleTextView) getElementById(this.mWeekDayIdsInWeek[i]);
        }
        for (i = 0; i < this.mWeekContentsLayout.length; i++) {
            this.mWeekContentsLayout[i] = (WMWeekContentsLayout) getElementById(this.mWeekIdsInMonth[i]);
        }
    }

    private void setDataToChildren() {
        setDataToChild(this.mNowWeekContentsLayout, this.mYearValue, this.mMonthValue, 999);
        updateWeekTitle();
        for (int i = 0; i < this.mWeekContentsLayout.length; i++) {
            setDataToChild(this.mWeekContentsLayout[i], this.mYearValue, this.mMonthValue, i + 1);
        }
    }

    private void updateWeekTitle() {
        int offset = Calendar.getInstance().getFirstDayOfWeek();
        int i = 0;
        while (i < this.mDayTitles.length && this.mDayTitles[i] != null) {
            this.mDayTitles[i].setDayOfWeek(i + offset);
            i++;
        }
    }

    private void setDataToChild(WMWeekContentsLayout wmWeekContentsLayout, int year, int month, int week) {
        if (wmWeekContentsLayout != null) {
            wmWeekContentsLayout.setYearData(year);
            wmWeekContentsLayout.setMonthData(month);
            wmWeekContentsLayout.setWeekData(week);
        }
    }

    private void dataRefresh() {
        if (dataChanged()) {
            clearTimeData();
            consYearValue();
            consMonthValue();
            setDataToChildren();
            refreshData();
            refreshView();
        }
    }

    private void clearTimeData() {
        this.mYearValue = 999;
        this.mMonthValue = 999;
    }

    private boolean dataChanged() {
        boolean res;
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(2);
        int week = calendar.get(4);
        int weekday = calendar.get(7);
        if (this.mNowMonthIndex == month && this.mNowWeekIndex == week && this.mNowDayOfWeekIndex == weekday) {
            res = false;
        } else {
            res = true;
        }
        this.mNowMonthIndex = month;
        this.mNowWeekIndex = week;
        this.mNowDayOfWeekIndex = weekday;
        return res;
    }

    public void refreshData() {
        if (this.mNowWeekContentsLayout != null) {
            this.mNowWeekContentsLayout.refreshData();
        }
        for (WMWeekContentsLayout layout : this.mWeekContentsLayout) {
            if (layout != null) {
                layout.refreshData();
            }
        }
    }

    public void refreshView() {
        int i = 0;
        if (this.mNowWeekContentsLayout != null) {
            this.mNowWeekContentsLayout.refreshView();
        }
        for (WMWeekContentsLayout layout : this.mWeekContentsLayout) {
            if (layout != null) {
                layout.refreshView();
            }
        }
        WMWeekDayTitleTextView[] wMWeekDayTitleTextViewArr = this.mDayTitles;
        int length = wMWeekDayTitleTextViewArr.length;
        while (i < length) {
            WMWeekDayTitleTextView view = wMWeekDayTitleTextViewArr[i];
            if (view != null) {
                view.refreshView();
            }
            i++;
        }
    }

    public void pause() {
        if (this.monthContentsLayoutRootView != null) {
            try {
                this.monthContentsLayoutRootView.getContext().getApplicationContext().unregisterReceiver(this.mSystemTimeChangeBroadcastRec);
            } catch (IllegalArgumentException e) {
                WMLog.d("WMMonthContentsLayout", "receiver already be unregister");
            }
        }
        this.monthContentsLayoutRootView = null;
        super.pause();
    }

    public void miniteChanged() {
        dataRefresh();
    }
}
