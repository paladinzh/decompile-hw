package com.android.deskclock.smartcover;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.CoverView;
import com.android.util.Utils;
import java.util.Locale;

public class HwCustCoverAdapterImpl extends HwCustCoverAdapter {
    private static final String RESOURCE_SUFFIX = SystemProperties.get("ro.config.small_cover_size", "");
    private static final String TAG = HwCustCoverAdapterImpl.class.getSimpleName();
    private PortBallFrameView mPortFrameView;

    public boolean isAdapterCoverEnable() {
        return !"".equals(RESOURCE_SUFFIX);
    }

    public Drawable getCoverBackground(Context context, int cover_full_lock_background) {
        return context.getResources().getDrawable(getResIdentifier(context, "cover_full_lock_background", HwCustCoverAdapter.TYPE_DRAWABLE, HwCustCoverAdapter.APP_PACKEGE, cover_full_lock_background));
    }

    public int getCoverBGColor(Context context, int deskclock_cover_background) {
        return getResIdentifier(context, "deskclock_cover_background", HwCustCoverAdapter.TYPE_COLOR, HwCustCoverAdapter.APP_PACKEGE, deskclock_cover_background);
    }

    public int getResIdentifier(Context context, String name, String defType, String defPackage, int defResIdentifier) {
        int identifier = context.getResources().getIdentifier(name + RESOURCE_SUFFIX, defType, defPackage);
        return identifier > 0 ? identifier : defResIdentifier;
    }

    public float getCoverCloseTextSize(Context context, int cover_close_textSize) {
        return (float) context.getResources().getDimensionPixelSize(getResIdentifier(context, "cover_close_textSize", HwCustCoverAdapter.TYPE_DIMEN, HwCustCoverAdapter.APP_PACKEGE, cover_close_textSize));
    }

    public boolean isLONPortCover() {
        return "_500x2560".equals(RESOURCE_SUFFIX);
    }

    public boolean isMTPortCover() {
        return "_1047x1312".equals(RESOURCE_SUFFIX);
    }

    public boolean isEvaPortCover() {
        return "_570x1251".equals(RESOURCE_SUFFIX);
    }

    public boolean isNeedBoldText() {
        String str = RESOURCE_SUFFIX;
        if (str.equals("_1047x1312")) {
            return true;
        }
        if (str.equals("_570x1251")) {
            return true;
        }
        if (str.equals("_1020x744")) {
            return true;
        }
        if (str.equals("_1041x1041")) {
            return true;
        }
        if (str.equals("_1068x732")) {
            return true;
        }
        if (str.equals("_1080x1920")) {
            return true;
        }
        return false;
    }

    public void initLONCover(Context context, CoverView mCoverScreen, Handler mHandler, OnClickListener mSnoozeListener) {
        int snoozeMinutes = Utils.getDefaultSharedPreferences(context).getInt("snooze_duration", 10);
        RelativeLayout snooze = (RelativeLayout) mCoverScreen.findViewById(R.id.snooze_layout);
        ((TextView) mCoverScreen.findViewById(R.id.snooze_pause_time)).setText(String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(snoozeMinutes)}));
        ((TextView) mCoverScreen.findViewById(R.id.snooze_pause_min)).setText(context.getString(R.string.tips_clock_snoozealarm_min));
        ((TextView) mCoverScreen.findViewById(R.id.snooze_pause_tip)).setText(context.getString(R.string.tips_clock_snoozealarm_tip));
        this.mPortFrameView = (PortBallFrameView) mCoverScreen.findViewById(R.id.port_close_layout);
        this.mPortFrameView.setMainHandler(mHandler);
        this.mPortFrameView.setCoverViewWidth(context.getResources().getDisplayMetrics().widthPixels);
        snooze.requestFocus();
        snooze.setOnClickListener(mSnoozeListener);
    }

    public void stopLONCoverAnim() {
        if (this.mPortFrameView != null) {
            this.mPortFrameView.stopTextViewAnimal();
        }
    }
}
