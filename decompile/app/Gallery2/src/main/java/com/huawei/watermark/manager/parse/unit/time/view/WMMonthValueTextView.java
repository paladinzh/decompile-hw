package com.huawei.watermark.manager.parse.unit.time.view;

import android.content.Context;
import android.widget.TextView;
import com.huawei.watermark.controller.broadcast.SystemTimeChangeBroadcastRec;
import com.huawei.watermark.controller.callback.WMTimeChangedCallBack;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMText;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUtil;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMMonthValueTextView extends WMText implements WMTimeChangedCallBack {
    private String[] mMonthValues = null;
    private int mNowMonthIndex = -1;
    SystemTimeChangeBroadcastRec mSystemTimeChangeBroadcastRec;
    private TextView mTextView;
    private String type;

    public WMMonthValueTextView(XmlPullParser parser) {
        super(parser);
        this.type = getStringByAttributeName(parser, "type");
        this.mSystemTimeChangeBroadcastRec = new SystemTimeChangeBroadcastRec(this);
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        consMonthValue();
        tv.setText(getText());
        tv.getContext().getApplicationContext().registerReceiver(this.mSystemTimeChangeBroadcastRec, this.mSystemTimeChangeBroadcastRec.getFilter());
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
        if (context != null) {
            this.mMonthValues = context.getResources().getStringArray(WMResourceUtil.getArrayId(context, "water_mark_monthvalue_typeabbreviation"));
            this.mNowMonthIndex = Calendar.getInstance().get(2);
        }
    }

    private void consMonthValue() {
        if (!WMCollectionUtil.isEmptyCollection(this.mMonthValues)) {
            int month = Calendar.getInstance().get(2);
            this.mNowMonthIndex = month;
            if ("abbreviation".equalsIgnoreCase(this.type) && month >= 0 && month <= this.mMonthValues.length - 1) {
                this.text = this.mMonthValues[month];
            }
        }
    }

    public void resume() {
        super.resume();
    }

    public void pause() {
        if (this.mTextView != null) {
            try {
                this.mTextView.getContext().getApplicationContext().unregisterReceiver(this.mSystemTimeChangeBroadcastRec);
            } catch (IllegalArgumentException e) {
                WMLog.d("WMMonthContentsLayout", "receiver already be unregister");
            }
        }
        this.mTextView = null;
        super.pause();
    }

    private void dataRefresh() {
        if (dataChanged()) {
            consMonthValue();
            this.mTextView.setText(getText());
        }
    }

    private boolean dataChanged() {
        boolean res;
        int month = Calendar.getInstance().get(2);
        if (this.mNowMonthIndex != month) {
            res = true;
        } else {
            res = false;
        }
        this.mNowMonthIndex = month;
        return res;
    }

    public void miniteChanged() {
        dataRefresh();
    }
}
