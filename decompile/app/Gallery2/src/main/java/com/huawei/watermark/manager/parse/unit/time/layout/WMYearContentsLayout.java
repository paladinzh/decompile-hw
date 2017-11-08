package com.huawei.watermark.manager.parse.unit.time.layout;

import android.content.Context;
import android.view.View;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMRelativeLayout;
import com.huawei.watermark.manager.parse.WaterMark;
import com.huawei.watermark.wmutil.WMResourceUtil;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMYearContentsLayout extends WMRelativeLayout {
    public static final int YEAR_NOW_INDEX = 999;
    private WMMonthContentsLayout[] mMonthContentsLayout = new WMMonthContentsLayout[12];
    private String[] mMonthIdsInYear = null;
    private String mMonthNowIdInYear = null;
    private WMMonthContentsLayout mNowMonthContentsLayout;
    private int mYearValue = 999;

    public WMYearContentsLayout(XmlPullParser parser) {
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
        return super.toView(context, wm, parentLayoutMode, ori);
    }

    public void addElement(WMElement element) {
        super.addElement(element);
    }

    public void setYearData(int year) {
        this.mYearValue = year;
    }

    private void consLogicValue(Context context) {
        initBaseData(context);
        consYearValue();
    }

    private void initBaseData(Context context) {
        if (context != null) {
            this.mMonthIdsInYear = context.getResources().getStringArray(WMResourceUtil.getArrayId(context, "water_mark_monthid"));
            this.mMonthNowIdInYear = context.getResources().getString(R.string.other_calendarwatermark_nowmonth_id);
        }
    }

    private void consYearValue() {
        if (this.mYearValue == 999) {
            this.mYearValue = Calendar.getInstance().get(1);
        }
    }

    private void getChildrenElements() {
        this.mNowMonthContentsLayout = (WMMonthContentsLayout) getElementById(this.mMonthNowIdInYear);
        for (int i = 0; i < this.mMonthContentsLayout.length; i++) {
            this.mMonthContentsLayout[i] = (WMMonthContentsLayout) getElementById(this.mMonthIdsInYear[i]);
        }
    }

    private void setDataToChildren() {
        setDataToChild(this.mNowMonthContentsLayout, this.mYearValue, 999);
        for (int i = 0; i < this.mMonthContentsLayout.length; i++) {
            setDataToChild(this.mMonthContentsLayout[i], this.mYearValue, i);
        }
    }

    private void setDataToChild(WMMonthContentsLayout wmMonthContentsLayout, int year, int month) {
        if (wmMonthContentsLayout != null) {
            wmMonthContentsLayout.setYearData(year);
            wmMonthContentsLayout.setMonthData(month);
        }
    }
}
