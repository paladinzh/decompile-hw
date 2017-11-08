package com.huawei.watermark.manager.parse.unit.time.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMText;
import com.huawei.watermark.wmutil.WMUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMWeekDayTitleTextView extends WMText {
    private static int[] weekTitles = new int[]{R.string.other_calendarwatermark_sunday_abbreviate, R.string.other_calendarwatermark_monday_abbreviate, R.string.other_calendarwatermark_tuesday_abbreviate, R.string.other_calendarwatermark_wednesday_abbreviate, R.string.other_calendarwatermark_thursday_abbreviate, R.string.other_calendarwatermark_friday_abbreviate, R.string.other_calendarwatermark_saturday_abbreviate};
    private int mDayOfWeek = 1;
    private TextView mTextView;

    public WMWeekDayTitleTextView(XmlPullParser parser) {
        super(parser);
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        refreshView();
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.mDayOfWeek = dayOfWeek;
    }

    public void refreshView() {
        if (this.mTextView != null) {
            this.mTextView.setText(this.mTextView.getResources().getString(weekTitles[(this.mDayOfWeek - 1) % weekTitles.length]));
        }
    }

    public View getView() {
        return this.mTextView;
    }
}
