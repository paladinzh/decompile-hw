package com.android.deskclock.alarmclock;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.LockAlarmFullActivity.ControlAlarm;
import com.android.deskclock.widgetlayout.DeskClockFontTypeTextView;
import com.android.util.Log;
import java.util.Calendar;

public class HwCustDavPixelCoverAlarmImpl extends HwCustDavPixelCoverAlarm {
    private static final String DAVINCE = "DAVINCE";
    private static final float DISTANCE = 350.0f;
    private static final String EDISON = "EDISON";
    private static final String PIXEL_FONTS = "/cust/preinstalled/public/fonts/pixelcover.ttf";
    private static final String TAG = "LockAlarmFullActivity";
    private ImageView digitalImgAmPm;
    private DeskClockFontTypeTextView digitalLeftAmPm;
    private TextView digitalTime;
    private float down = 0.0f;
    private Context mContext = null;
    private CoverView mCoverScreen;
    private float move = 0.0f;
    private ImageView rightImg;
    private ImageView rightTransImg;
    private ImageView rightTransImgMiddle;

    public boolean isPixelCoverEnable() {
        String boardName = SystemProperties.get("ro.board.boardname", "NULL");
        if (boardName.contains(DAVINCE) || boardName.contains(EDISON)) {
            return true;
        }
        return false;
    }

    public CoverView dynamicDisplayCoverAlarmLayout(Context context) {
        this.mContext = context;
        this.mCoverScreen = (CoverView) LayoutInflater.from(this.mContext).inflate(R.layout.pixel_cover_alarm_full_port, null);
        this.digitalLeftAmPm = (DeskClockFontTypeTextView) this.mCoverScreen.findViewById(R.id.digital_left_ampm);
        this.digitalLeftAmPm.setVisibility(8);
        this.digitalTime = (TextView) this.mCoverScreen.findViewById(R.id.digital_full_time);
        this.digitalImgAmPm = (ImageView) this.mCoverScreen.findViewById(R.id.digital_img_ampm);
        this.rightImg = (ImageView) this.mCoverScreen.findViewById(R.id.cover_arrow_right);
        this.rightTransImg = (ImageView) this.mCoverScreen.findViewById(R.id.cover_arrow_right_transfer);
        this.rightTransImgMiddle = (ImageView) this.mCoverScreen.findViewById(R.id.cover_arrow_right_transfer_middle);
        int digitalTimeSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.cover_time_port_tvsize_dav);
        try {
            Typeface pixelFonts = Typeface.createFromFile(PIXEL_FONTS);
            if (pixelFonts != null) {
                this.digitalTime.setTypeface(pixelFonts);
                this.digitalTime.setTextSize(0, (float) digitalTimeSize);
                this.digitalTime.setTextColor(-1);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "dynamicDisplayCoverAlarmLayout  Exception = " + e);
        }
        if (DateFormat.is24HourFormat(this.mContext)) {
            this.digitalImgAmPm.setVisibility(8);
            int digitalTimeMarginLeft = this.mContext.getResources().getDimensionPixelSize(R.dimen.digital_full_time_marginLeft_24h);
            int digitalTimeMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.digital_full_time_marginTop);
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            layoutParams.setMargins(digitalTimeMarginLeft, digitalTimeMarginTop, 0, 0);
            this.digitalTime.setLayoutParams(layoutParams);
        } else {
            this.digitalImgAmPm.setVisibility(0);
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(10);
            if (calendar.get(9) == 0) {
                if (hour == 10 || hour == 11 || hour == 0) {
                    this.digitalImgAmPm.setBackgroundResource(R.drawable.pixel_cover_am_label_margin);
                } else {
                    this.digitalImgAmPm.setBackgroundResource(R.drawable.pixel_cover_am_label);
                }
            } else if (hour == 10 || hour == 11 || hour == 0) {
                this.digitalImgAmPm.setBackgroundResource(R.drawable.pixel_cover_pm_label_margin);
            } else {
                this.digitalImgAmPm.setBackgroundResource(R.drawable.pixel_cover_pm_label);
            }
        }
        dynamicDisplayCoverAlarmIcon(ObjectAnimator.ofFloat(this.rightTransImg, "alpha", new float[]{0.0f}).setDuration(750), 0);
        dynamicDisplayCoverAlarmIcon(ObjectAnimator.ofFloat(this.rightTransImgMiddle, "alpha", new float[]{0.0f}).setDuration(750), 250);
        dynamicDisplayCoverAlarmIcon(ObjectAnimator.ofFloat(this.rightImg, "alpha", new float[]{0.0f}).setDuration(750), 500);
        return this.mCoverScreen;
    }

    public void dynamicDisplayCoverAlarmIcon(ObjectAnimator obj, int time_delay) {
        obj.setRepeatCount(-1);
        obj.setRepeatMode(2);
        obj.setStartDelay((long) time_delay);
        obj.start();
    }

    public void slideCloseAlarmClock(RelativeLayout mCoverSnoozeLayout, final ControlAlarm mControlAlarm, Boolean mkilled, final Alarm mAlarm) {
        mCoverSnoozeLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 0:
                        HwCustDavPixelCoverAlarmImpl.this.down = event.getX();
                        break;
                    case 2:
                        HwCustDavPixelCoverAlarmImpl.this.move = event.getX();
                        if (Math.abs(HwCustDavPixelCoverAlarmImpl.this.move - HwCustDavPixelCoverAlarmImpl.this.down) > HwCustDavPixelCoverAlarmImpl.DISTANCE) {
                            mControlAlarm.dismiss(false, mAlarm);
                            ((LockAlarmFullActivity) HwCustDavPixelCoverAlarmImpl.this.mContext).finish();
                            break;
                        }
                        break;
                }
                return true;
            }
        });
    }
}
