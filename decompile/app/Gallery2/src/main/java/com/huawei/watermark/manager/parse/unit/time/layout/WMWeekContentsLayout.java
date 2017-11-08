package com.huawei.watermark.manager.parse.unit.time.layout;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMImage;
import com.huawei.watermark.manager.parse.WMRelativeLayout;
import com.huawei.watermark.manager.parse.WaterMark;
import com.huawei.watermark.manager.parse.unit.time.view.WMWeekDayValueTextView;
import com.huawei.watermark.wmutil.WMResourceUtil;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMWeekContentsLayout extends WMRelativeLayout {
    public static final int WEEK_NOW_INDEX = 999;
    private int mMonthValue = 999;
    private WMImage mSelectedIcon;
    private String mSelectedIconId = null;
    private int mSelectedIconPos;
    private WMWeekDayValueTextView[] mWMWeekDayValueTextView = new WMWeekDayValueTextView[7];
    private String[] mWeekDayIdsInWeek = null;
    private int mWeekValue = 999;
    private int mYearValue = 999;

    public WMWeekContentsLayout(XmlPullParser parser) {
        super(parser);
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        if (context != null) {
            consLogicValue(context);
            getChildrenElements();
            setDataToChildren();
            super.initBaseLogicData(context, delegate);
        }
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        View res = super.toView(context, wm, parentLayoutMode, ori);
        refreshSelectedIconView();
        return res;
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

    public void setWeekData(int week) {
        this.mWeekValue = week;
    }

    private void consLogicValue(Context context) {
        initBaseData(context);
        consYearValue();
        consMonthValue();
        consWeekValue();
    }

    private void initBaseData(Context context) {
        if (context != null) {
            this.mWeekDayIdsInWeek = context.getResources().getStringArray(WMResourceUtil.getArrayId(context, "water_mark_weekdayid"));
            this.mSelectedIconId = context.getResources().getString(R.string.other_calendarwatermark_weekcontentsselectedicon_id);
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

    private void consWeekValue() {
        if (this.mWeekValue == 999) {
            this.mWeekValue = Calendar.getInstance().get(4);
        }
    }

    private void getChildrenElements() {
        for (int i = 0; i < this.mWMWeekDayValueTextView.length; i++) {
            this.mWMWeekDayValueTextView[i] = (WMWeekDayValueTextView) getElementById(this.mWeekDayIdsInWeek[i]);
        }
        this.mSelectedIcon = (WMImage) getElementById(this.mSelectedIconId);
    }

    private void setDataToChildren() {
        int offset = Calendar.getInstance().getFirstDayOfWeek() - 1;
        for (int i = 0; i < this.mWMWeekDayValueTextView.length; i++) {
            setDataToChild(this.mWMWeekDayValueTextView[i], this.mYearValue, this.mMonthValue, this.mWeekValue, ((i + offset) % 7) + 1);
        }
        setDataTOSelectedIcon();
    }

    private void setDataToChild(WMWeekDayValueTextView wmWeekDayValueTextView, int year, int month, int week, int weekday) {
        if (wmWeekDayValueTextView != null) {
            wmWeekDayValueTextView.setYearData(year);
            wmWeekDayValueTextView.setMonthData(month);
            wmWeekDayValueTextView.setWeekData(week);
            wmWeekDayValueTextView.setWeekDayData(weekday);
        }
    }

    public void refreshData() {
        consYearValue();
        consMonthValue();
        consWeekValue();
        setDataToChildren();
        for (WMWeekDayValueTextView view : this.mWMWeekDayValueTextView) {
            if (view != null) {
                view.refreshData();
            }
        }
    }

    public void refreshView() {
        for (WMWeekDayValueTextView view : this.mWMWeekDayValueTextView) {
            if (view != null) {
                view.refreshView();
            }
        }
        refreshSelectedIconView();
    }

    private void setDataTOSelectedIcon() {
        if (this.mSelectedIcon != null) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(1);
            int month = calendar.get(2);
            int week = calendar.get(4);
            int weekday = calendar.get(7);
            int offset = calendar.getFirstDayOfWeek() - 1;
            int i = 0;
            while (i < this.mWMWeekDayValueTextView.length) {
                if (year == this.mYearValue && month == this.mMonthValue && week == this.mWeekValue && weekday == ((i + offset) % 7) + 1) {
                    this.mSelectedIconPos = i;
                    return;
                }
                i++;
            }
        }
    }

    private void refreshSelectedIconView() {
        this.mSelectedIcon.getView().setLayoutParams((LayoutParams) this.mWMWeekDayValueTextView[this.mSelectedIconPos].getView().getLayoutParams());
    }
}
