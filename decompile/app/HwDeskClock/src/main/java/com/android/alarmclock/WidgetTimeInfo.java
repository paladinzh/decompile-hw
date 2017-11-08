package com.android.alarmclock;

import android.appwidget.AppWidgetHostView;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.R;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.ClockReporter;
import com.android.util.FormatTime;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class WidgetTimeInfo extends RelativeLayout {
    private static Uri mUri = System.getUriFor("launcher_background_color");
    private int blackcolor;
    private TextView hourView;
    private int layout_height_landscreen;
    private int layout_height_portrait;
    private Calendar mCalendar;
    private TextView mDate;
    private Drawable mDrawable;
    private Drawable mDrawableBack;
    private Drawable mDrawableBackground;
    private final ContentObserver mFormatChangeObserver;
    private Handler mHandler;
    private ImageView mImage;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mIsLauncherEditMode;
    private TextView mLeftAmPm;
    private int mMyLanguageAdapter;
    private final BroadcastReceiver mReceiver;
    private TextView mRightAmPm;
    private LinearLayout mTimeLayout;
    private TextView minuteView;
    private ImageView separatorView;
    private TextView separatorViewTV;
    private int shadcolor;

    public WidgetTimeInfo(Context context) {
        this(context, null);
    }

    public WidgetTimeInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCalendar = Calendar.getInstance();
        this.mMyLanguageAdapter = 0;
        this.mIsLauncherEditMode = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 200) {
                    int color;
                    int colorType = System.getInt(WidgetTimeInfo.this.getContext().getContentResolver(), "launcher_background_color", 2);
                    Drawable tempDrawable = WidgetTimeInfo.this.mDrawable;
                    if (WidgetTimeInfo.this.mIsLauncherEditMode || WidgetUtils.getLaucherState(WidgetTimeInfo.this.getContext()) == 0) {
                        color = -1;
                        tempDrawable = WidgetTimeInfo.this.mDrawable;
                        WidgetTimeInfo.this.mDate.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.mLeftAmPm.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.mRightAmPm.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.hourView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.minuteView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                    } else if (colorType == 1) {
                        color = WidgetTimeInfo.this.blackcolor;
                        tempDrawable = WidgetTimeInfo.this.mDrawableBack;
                        WidgetTimeInfo.this.mDate.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.mLeftAmPm.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.mRightAmPm.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.hourView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.minuteView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                    } else if (colorType == 2) {
                        color = -1;
                        tempDrawable = WidgetTimeInfo.this.mDrawable;
                        WidgetTimeInfo.this.mDate.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.mLeftAmPm.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.mRightAmPm.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.hourView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        WidgetTimeInfo.this.minuteView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                    } else {
                        color = -1;
                        tempDrawable = WidgetTimeInfo.this.mDrawableBackground;
                        WidgetTimeInfo.this.mDate.setShadowLayer(4.0f, 0.0f, 2.0f, WidgetTimeInfo.this.shadcolor);
                        WidgetTimeInfo.this.mLeftAmPm.setShadowLayer(4.0f, 0.0f, 2.0f, WidgetTimeInfo.this.shadcolor);
                        WidgetTimeInfo.this.mRightAmPm.setShadowLayer(4.0f, 0.0f, 2.0f, WidgetTimeInfo.this.shadcolor);
                        WidgetTimeInfo.this.hourView.setShadowLayer(4.0f, 0.0f, 2.0f, WidgetTimeInfo.this.shadcolor);
                        WidgetTimeInfo.this.minuteView.setShadowLayer(4.0f, 0.0f, 2.0f, WidgetTimeInfo.this.shadcolor);
                    }
                    Log.dRelease("WidgetTimeInfo", "SettingDB change, change to blackground, checkLightWallPaper colorType = " + colorType + " And will set color = " + color);
                    WidgetTimeInfo.this.mLeftAmPm.setTextColor(color);
                    WidgetTimeInfo.this.mRightAmPm.setTextColor(color);
                    WidgetTimeInfo.this.mDate.setTextColor(color);
                    WidgetTimeInfo.this.hourView.setTextColor(color);
                    WidgetTimeInfo.this.minuteView.setTextColor(color);
                    WidgetTimeInfo.this.separatorViewTV.setTextColor(color);
                    WidgetTimeInfo.this.separatorView.setImageDrawable(tempDrawable);
                }
            }
        };
        this.mFormatChangeObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                WidgetTimeInfo.this.updateTime();
            }

            public void onChange(boolean selfChange, Uri uri) {
                WidgetTimeInfo.this.updateTime();
                if (WidgetTimeInfo.mUri != null && WidgetTimeInfo.mUri.equals(uri)) {
                    if (WidgetTimeInfo.this.mHandler.hasMessages(200)) {
                        WidgetTimeInfo.this.mHandler.removeMessages(200);
                    }
                    WidgetTimeInfo.this.mHandler.sendEmptyMessage(200);
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if ("android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                        String timeZone = intent.getStringExtra("time-zone");
                        if (timeZone != null) {
                            WidgetTimeInfo.this.createTime(timeZone);
                        } else {
                            return;
                        }
                    }
                    WidgetTimeInfo.this.updateTime();
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if ("com.huawei.android.launcher.mode_change_action".equals(intent.getAction())) {
                        WidgetTimeInfo.this.refreshWidgetColor(intent);
                        if (WidgetTimeInfo.this.mHandler.hasMessages(200)) {
                            WidgetTimeInfo.this.mHandler.removeMessages(200);
                        }
                        WidgetTimeInfo.this.mHandler.sendEmptyMessage(200);
                    }
                }
            }
        };
        this.mDrawable = getResources().getDrawable(R.drawable.dualclock_widget_time_colon);
        this.blackcolor = getResources().getColor(R.color.widget_black_color);
        this.shadcolor = getResources().getColor(R.color.digital_widget_separator_tv_shadow);
        this.mDrawableBack = getResources().getDrawable(R.drawable.dualclock_widget_time_colon_light);
        this.mDrawableBackground = getResources().getDrawable(R.drawable.dualclock_widget_time_colon_shadow);
        this.mMyLanguageAdapter = getResources().getDimensionPixelSize(R.dimen.digit_clock_widget_my_language);
        this.layout_height_landscreen = getResources().getDimensionPixelSize(R.dimen.widget_digital_top_height_land);
        this.layout_height_portrait = getResources().getDimensionPixelSize(R.dimen.widget_digital_top_height);
    }

    private void refreshWidgetColor(Intent intent) {
        String type = intent.getStringExtra("launcher_mode_type");
        Log.dRelease("WidgetTimeInfo", "refreshWidgetColor launcher mode = " + type);
        int state = 0;
        if ("normal_mode".equals(type)) {
            this.mIsLauncherEditMode = false;
            state = 1;
        } else if ("edit_mode".equals(type)) {
            this.mIsLauncherEditMode = true;
            state = 0;
        }
        WidgetUtils.saveLaucherState(getContext(), state);
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            this.mCalendar = Calendar.getInstance();
        }
    }

    public void updateTime() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        FormatTime time = new FormatTime(this.mContext, this.mCalendar);
        DateFormat.is24HourFormat(this.mContext);
        refreshTimeDisplay(time);
        this.mLeftAmPm.setText(time.getTimeString(2));
        this.mRightAmPm.setText(time.getTimeString(3));
        this.mDate.setText(time.getTimeString(9));
    }

    public void refreshTimeDisplay(FormatTime time) {
        int number = this.mTimeLayout.getChildCount();
        for (int i = 0; i < number; i++) {
            this.mTimeLayout.getChildAt(i).setVisibility(8);
        }
        this.hourView.setText(time.getTimeString(4));
        this.hourView.setVisibility(0);
        this.minuteView.setText(time.getTimeString(5));
        this.minuteView.setVisibility(0);
        this.separatorView = (ImageView) this.mTimeLayout.findViewById(R.id.digital_separator);
        this.separatorViewTV = (TextView) this.mTimeLayout.findViewById(R.id.digital_separator_tv);
        if (":".equals(time.getTimeString(6))) {
            this.separatorView.setVisibility(0);
            this.separatorViewTV.setVisibility(8);
            return;
        }
        this.separatorViewTV.setText(time.getTimeString(6));
        this.separatorViewTV.setVisibility(0);
        this.separatorView.setVisibility(8);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDate = (TextView) findViewById(R.id.date);
        this.mLeftAmPm = (TextView) findViewById(R.id.left_amPm);
        this.mRightAmPm = (TextView) findViewById(R.id.right_amPm);
        this.mTimeLayout = (LinearLayout) findViewById(R.id.time_layout);
        this.hourView = (TextView) this.mTimeLayout.findViewById(R.id.digital_hour_time);
        this.minuteView = (TextView) this.mTimeLayout.findViewById(R.id.digital_minute_time);
        this.mImage = (ImageView) this.mTimeLayout.findViewById(R.id.digital_separator);
        LayoutParams params;
        if (Utils.isLandScreen(getContext())) {
            params = this.mTimeLayout.getLayoutParams();
            params.height = this.layout_height_landscreen;
            this.mTimeLayout.setLayoutParams(params);
            this.mDate.setTextSize(1, 12.0f);
            this.mLeftAmPm.setTextSize(1, 10.0f);
            this.mRightAmPm.setTextSize(1, 10.0f);
            this.hourView.setTextSize(1, 48.0f);
            this.minuteView.setTextSize(1, 48.0f);
            this.mImage.setScaleX(0.8f);
            this.mImage.setScaleY(0.8f);
        } else {
            params = this.mTimeLayout.getLayoutParams();
            params.height = this.layout_height_portrait;
            this.mTimeLayout.setLayoutParams(params);
            this.mDate.setTextSize(1, 15.0f);
            this.mLeftAmPm.setTextSize(1, 11.0f);
            this.mRightAmPm.setTextSize(1, 11.0f);
            this.hourView.setTextSize(1, 58.0f);
            this.minuteView.setTextSize(1, 58.0f);
            this.mImage.setScaleX(1.0f);
            this.mImage.setScaleY(1.0f);
        }
        if (Locale.getDefault().getLanguage().equals("my")) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mDate.getLayoutParams();
            layoutParams.topMargin += this.mMyLanguageAdapter;
            this.mDate.setLayoutParams(layoutParams);
        }
        if (this.mHandler.hasMessages(200)) {
            this.mHandler.removeMessages(200);
        }
        this.mHandler.sendEmptyMessage(200);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        getContext().registerReceiver(this.mIntentReceiver, filter, null, getHandler());
    }

    private void registerLauncherReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.android.launcher.mode_change_action");
        getContext().registerReceiver(this.mReceiver, filter, "com.huawei.android.launcher.permission.EDIT_MODE", getHandler());
    }

    private void registerObserver() {
        getContext().getContentResolver().registerContentObserver(System.CONTENT_URI, true, this.mFormatChangeObserver);
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(this.mIntentReceiver);
    }

    private void unregisterLauncherReceiver() {
        getContext().unregisterReceiver(this.mReceiver);
    }

    private void unregisterObserver() {
        getContext().getContentResolver().unregisterContentObserver(this.mFormatChangeObserver);
    }

    public void onAttachedToWindow() {
        if (getParent() instanceof AppWidgetHostView) {
            AppWidgetHostView appWidgetHostView = (AppWidgetHostView) getParent();
            if (appWidgetHostView != null) {
                appWidgetHostView.setPadding(0, 0, 0, 0);
            }
        } else if (getParent() instanceof View) {
            View view = (View) getParent();
            if (view != null) {
                view.setPadding(0, 0, 0, 0);
            }
        }
        super.onAttachedToWindow();
        registerReceiver();
        registerLauncherReceiver();
        registerObserver();
        updateTime();
        initOnClick();
        ClockReporter.reportEventMessage(this.mContext, 58, "");
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
        unregisterObserver();
        unregisterLauncherReceiver();
    }

    private void initOnClick() {
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ClockReporter.reportEventMessage(WidgetTimeInfo.this.getContext(), 61, "");
                Intent i = new Intent();
                i.setComponent(new ComponentName(HwCustCoverAdapter.APP_PACKEGE, AlarmsMainActivity.class.getName()));
                i.putExtra("isClockWidget", true);
                i.setAction("android.intent.action.MAIN");
                i.addCategory("android.intent.category.LAUNCHER");
                i.addFlags(805339136);
                WidgetTimeInfo.this.mContext.startActivity(i);
            }
        });
    }
}
