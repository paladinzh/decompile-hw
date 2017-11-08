package com.android.deskclock.alarmclock;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.deskclock.R;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.HwLog;
import com.huawei.cust.HwCustUtils;

public class CoverRelativeLayout extends RelativeLayout {
    public CoverRelativeLayout(Context context) {
        this(context, null);
    }

    public CoverRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Drawable coverDrawable = CoverCfg.getCoverWallpaper();
            if (coverDrawable != null) {
                setColorFilter(coverDrawable);
                setBackground(coverDrawable);
                return;
            }
            init();
        } catch (Exception e) {
            HwLog.w("CoverRelativeLayout", "framework resource not found");
            setBackgroundColor(-16777216);
        }
    }

    private void setColorFilter(Drawable coverDrawable) {
        HwLog.i("CoverRelativeLayout", "framework resource not found");
        coverDrawable.setColorFilter(getResources().getColor(R.color.deskclock_cover_background), Mode.DARKEN);
    }

    private void init() {
        Drawable mDrawable = getResources().getDrawable(33751074);
        HwCustCoverAdapter mCover = (HwCustCoverAdapter) HwCustUtils.createObj(HwCustCoverAdapter.class, new Object[0]);
        HwCustDavPixelCoverAlarm mPixelCoverAlarm = (HwCustDavPixelCoverAlarm) HwCustUtils.createObj(HwCustDavPixelCoverAlarm.class, new Object[0]);
        boolean isPixelCoverEnable = mPixelCoverAlarm != null ? mPixelCoverAlarm.isPixelCoverEnable() : false;
        int colorID = R.color.deskclock_cover_background;
        if (mCover != null && (mCover.isAdapterCoverEnable() || isPixelCoverEnable)) {
            mDrawable = mCover.getCoverBackground(getContext(), 33751074);
            colorID = mCover.getCoverBGColor(getContext(), R.color.deskclock_cover_background);
        }
        mDrawable.setColorFilter(getResources().getColor(colorID), Mode.DARKEN);
        setBackgroundDrawable(mDrawable);
    }
}
