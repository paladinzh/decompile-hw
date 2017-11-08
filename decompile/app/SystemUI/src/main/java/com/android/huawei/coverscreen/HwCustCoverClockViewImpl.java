package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Typeface;
import android.os.SystemProperties;
import android.text.TextPaint;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.huawei.keyguard.util.Typefaces;
import fyusion.vislib.BuildConfig;

public class HwCustCoverClockViewImpl extends HwCustCoverClockView {
    private static final String DUAL_CLOCK_ID = "cover_dual_clock_textsize_onlinefont";
    private static final float LON_TIME_CLOCK_SIZE = 48.0f;
    private static final String MUSIC_CLOCK_ID = "cover_music_clock_textsize_onlinefont";
    private static final String RESOURCE_SUFFIX = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    private static final String SINGLE_CLOCK_ID = "cover_single_clock_textsize_onlinefont";
    private CoverClockGalleryLayout mClockGalleryLayout;
    private View mContainer;
    private Context mContext;

    public HwCustCoverClockViewImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void addGalleryLayout(View mview) {
        this.mClockGalleryLayout = (CoverClockGalleryLayout) mview.findViewById(R$id.cover_clock_gallery_layout);
        if (this.mClockGalleryLayout != null) {
            this.mContainer = mview.findViewById(R$id.clock_view_container);
            this.mClockGalleryLayout.setContainer(this.mContainer);
        }
    }

    public void responseLongPress() {
        if (this.mClockGalleryLayout != null && !this.mClockGalleryLayout.isInShrinkMode()) {
            this.mClockGalleryLayout.switchMode(true);
        }
    }

    public boolean isInShrinkMode() {
        if (this.mClockGalleryLayout != null) {
            return this.mClockGalleryLayout.isInShrinkMode();
        }
        return false;
    }

    public void setSwitchPanel(View view) {
        if (this.mClockGalleryLayout != null) {
            this.mClockGalleryLayout.setSwitchPanel(view);
        }
    }

    public void setSwitchPanelBackground() {
        if (this.mClockGalleryLayout != null) {
            this.mClockGalleryLayout.setSwitchPanelBackground(-1);
        }
    }

    public void switchToNormalMode() {
        if (this.mClockGalleryLayout != null) {
            this.mClockGalleryLayout.enlargeDirectly();
            this.mClockGalleryLayout.setSwitchPanelBackground(-1);
        }
    }

    public int getStepResBright(int defaultResId) {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return R$drawable.cover_steps_for_big;
        }
        return defaultResId;
    }

    public int getStepResGray(int defaultResId) {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return R$drawable.cover_steps_for_big;
        }
        return defaultResId;
    }

    public void setDigitalTimeFont(TextView textView) {
        String str = RESOURCE_SUFFIX;
        if (str.equals("_401x1920") || str.equals("_500x2560") || str.equals("_540x2560")) {
            Typeface t = Typeface.create("sans-serif-condensed-light", 0);
            if (t != null) {
                textView.setTypeface(t);
            }
        } else if (str.equals("_747x1920") || str.equals("_1440x2560")) {
            Typeface t2 = Typefaces.get(this.mContext, "/system/fonts/Roboto-Thin.ttf");
            if (t2 != null) {
                textView.setTypeface(t2);
            }
        } else {
            if (!(str.equals("_1047x1312") || str.equals("_570x1251") || str.equals("_1068x732") || str.equals("_1080x1920") || str.equals("_1041x1041"))) {
                if (!str.equals("_1020x744")) {
                    return;
                }
            }
            Typeface t1 = Typefaces.get(this.mContext, "/system/fonts/AndroidClock.ttf");
            if (t1 != null) {
                textView.setTypeface(t1);
                TextPaint paint = textView.getPaint();
                if (paint != null) {
                    paint.setFakeBoldText(true);
                }
            }
        }
    }

    public void setDigitalTimeFontSize(Context context, TextView textView, int type) {
        int dimenId = 0;
        switch (type) {
            case 0:
                dimenId = CoverResourceUtils.getResIdentifier(context, SINGLE_CLOCK_ID, "dimen", "com.android.systemui", 0);
                break;
            case 1:
                dimenId = CoverResourceUtils.getResIdentifier(context, DUAL_CLOCK_ID, "dimen", "com.android.systemui", 0);
                break;
            case 2:
                dimenId = CoverResourceUtils.getResIdentifier(context, MUSIC_CLOCK_ID, "dimen", "com.android.systemui", 0);
                break;
        }
        if (dimenId != 0) {
            textView.setTextSize(0, context.getResources().getDimension(dimenId));
        }
    }
}
