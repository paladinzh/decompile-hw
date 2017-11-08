package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.keyguard.R$id;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IContentListener;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.util.Typefaces;
import com.huawei.keyguard.util.WindowCounter;
import java.util.Calendar;

public class ClockView extends AbsClockView implements IEventListener, IContentListener, Callback {
    private static WindowCounter mWndCounter = new WindowCounter(ClockView.class);
    private final Handler mHandler;
    boolean mIsTimeZoneChanged;
    private Runnable mUpdater;

    public boolean onReceive(Context context, Intent intent) {
        if (intent != null) {
            String recvAction = intent.getAction();
            if (recvAction != null) {
                this.mIsTimeZoneChanged = recvAction.equals("android.intent.action.TIMEZONE_CHANGED");
            }
        }
        this.mHandler.removeCallbacks(this.mUpdater);
        this.mHandler.post(this.mUpdater);
        return false;
    }

    public void onContentChange(boolean selfChange) {
        this.mHandler.removeCallbacks(this.mUpdater);
        this.mHandler.post(this.mUpdater);
    }

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHandler = GlobalContext.getUIHandler();
        this.mIsTimeZoneChanged = false;
        this.mUpdater = new Runnable() {
            public void run() {
                if (ClockView.this.mIsTimeZoneChanged && !ClockView.this.getFixedTimeZone()) {
                    ClockView.this.mCalendar = Calendar.getInstance();
                }
                ClockView.this.updateTime();
            }
        };
        setFactory(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTimeView = (TextView) findViewById(R$id.clock_text);
        this.mCalendar = Calendar.getInstance();
        this.mFactory.setHwDateFormat();
        this.mDateView = (TextView) findViewById(R$id.date);
        Typeface t1 = Typefaces.get(getContext(), "/system/fonts/Roboto-Thin.ttf");
        if (t1 != null) {
            this.mTimeView.setTypeface(t1);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWndCounter.onAttach();
        EventCenter eventCenter = EventCenter.getInst();
        eventCenter.listenContent(1, this);
        eventCenter.listen(1, this);
        updateTime();
        AppHandler.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWndCounter.onDetach();
        this.mHandler.removeCallbacks(this.mUpdater);
        EventCenter eventCenter = EventCenter.getInst();
        eventCenter.stopListen(this);
        eventCenter.stopListenContent(this);
        AppHandler.removeListener(this);
    }

    public void updateTime(Calendar c) {
        this.mCalendar = c;
        updateTime();
    }

    public void updateTime() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        this.mFactory.updateHwTimeStyle();
        this.mFactory.refreshDate();
    }

    public boolean handleMessage(Message arg) {
        if (arg.what == 14) {
            updateTime();
        }
        return false;
    }
}
