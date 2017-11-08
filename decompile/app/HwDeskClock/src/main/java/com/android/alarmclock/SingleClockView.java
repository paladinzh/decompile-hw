package com.android.alarmclock;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RemoteViews.RemoteView;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.ClockReporter;

@RemoteView
public class SingleClockView extends WorldAnalogClock {
    public SingleClockView(Context context) {
        this(context, null);
    }

    public SingleClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initOnClick();
    }

    private void initOnClick() {
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ClockReporter.reportEventMessage(SingleClockView.this.getContext(), 62, "");
                Intent i = new Intent();
                i.setComponent(new ComponentName(HwCustCoverAdapter.APP_PACKEGE, AlarmsMainActivity.class.getName()));
                i.putExtra("isClockWidget", true);
                i.setAction("android.intent.action.MAIN");
                i.addCategory("android.intent.category.LAUNCHER");
                i.addFlags(805339136);
                SingleClockView.this.mContext.startActivity(i);
            }
        });
    }
}
