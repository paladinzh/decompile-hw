package com.android.huawei.coverscreen;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$layout;
import com.huawei.keyguard.cover.CoverScreen;
import fyusion.vislib.BuildConfig;

public class HwCustCoverScreenImpl extends HwCustCoverScreen {
    private static final String KEY_COVER_MODE_CHECKED = "cover_mode_checked";
    private static final String RESOURCE_SUFFIX = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    private static final int SMART_COVER_RADIOBUTTON_CHECKED = 1;
    private static String TAG = "HwCustCoverScreenImpl";
    private Context mContext;
    private RelativeLayout mGratingHomeLayout = null;
    private RelativeLayout mMainLayout = null;

    public HwCustCoverScreenImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void initLockScreen(RelativeLayout mainLayout, RelativeLayout originHomeLayout) {
        if (mainLayout == null || originHomeLayout == null) {
            Log.d(TAG, "mainLayout is null or  originHomeLayout is null in the initLockScreen");
            return;
        }
        this.mMainLayout = mainLayout;
        if (this.mMainLayout.getContext() != null) {
            int defaultLayoutId = R$layout.cover_home;
            int layoutID = CoverResourceUtils.getResIdentifier(mainLayout.getContext(), "cover_home", "layout", "com.android.systemui", defaultLayoutId);
            if (layoutID != defaultLayoutId) {
                LayoutInflater inflater = LayoutInflater.from(this.mMainLayout.getContext());
                if (inflater != null) {
                    this.mGratingHomeLayout = (RelativeLayout) inflater.inflate(layoutID, null);
                    if (this.mGratingHomeLayout == null) {
                        return;
                    }
                }
                LayoutParams originHomeLayoutParams = (LayoutParams) originHomeLayout.getLayoutParams();
                if (originHomeLayoutParams == null) {
                    Log.d(TAG, "originHomeLayoutParams is null in the initLockScreen");
                    return;
                }
                int topMargin = originHomeLayoutParams.topMargin;
                int width = originHomeLayoutParams.width;
                int height = originHomeLayoutParams.height;
                int leftMargin = originHomeLayoutParams.leftMargin;
                Log.d(TAG, "removeView oldHomeView,addView newHome");
                originHomeLayout.removeAllViews();
                this.mMainLayout.removeView(originHomeLayout);
                mainLayout.addView(this.mGratingHomeLayout);
                LayoutParams gratingHomeLayoutLayoutParams = (LayoutParams) this.mGratingHomeLayout.getLayoutParams();
                if (gratingHomeLayoutLayoutParams == null) {
                    Log.d(TAG, "gratingHomeLayoutLayoutParams is null in the initLockScreen");
                    return;
                }
                reCalculateLayoutParams(mainLayout, topMargin, width, height, leftMargin, gratingHomeLayoutLayoutParams);
                Log.d(TAG, "set the new layout params for the new Home layout");
                this.mGratingHomeLayout.setLayoutParams(gratingHomeLayoutLayoutParams);
            }
        }
    }

    private void reCalculateLayoutParams(RelativeLayout mainLayout, int topMargin, int width, int height, int leftMargin, LayoutParams gratingHomeLayoutLayoutParams) {
        gratingHomeLayoutLayoutParams.topMargin = topMargin;
        gratingHomeLayoutLayoutParams.width = width;
        gratingHomeLayoutLayoutParams.height = height;
        gratingHomeLayoutLayoutParams.leftMargin = leftMargin;
    }

    public int getTopMarginWithStatusbar(int topMargin) {
        String str = RESOURCE_SUFFIX;
        if (str.equals("_1047x1312")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.top_margin_with_statusbar_1047x1312);
        }
        if (str.equals("_1020x744")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.top_margin_with_statusbar_1020x744);
        }
        if (str.equals("_1041x1041")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.top_margin_with_statusbar_1041x1041);
        }
        if (str.equals("_1068x732")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.top_margin_with_statusbar_1068x732);
        }
        if (str.equals("_1080x1920")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.top_margin_with_statusbar_1080x1920);
        }
        if (str.equals("_1440x2560")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.top_margin_with_statusbar_1440x2560);
        }
        return topMargin;
    }

    public int getLeftMarginWithStatusbar(int leftMargin) {
        String str = RESOURCE_SUFFIX;
        if (str.equals("_1047x1312")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.left_margin_with_statusbar_1047x1312);
        }
        if (str.equals("_1020x744")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.left_margin_with_statusbar_1020x744);
        }
        if (str.equals("_1041x1041")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.left_margin_with_statusbar_1041x1041);
        }
        if (str.equals("_1068x732")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.left_margin_with_statusbar_1068x732);
        }
        if (str.equals("_1080x1920")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.left_margin_with_statusbar_1080x1920);
        }
        if (str.equals("_1440x2560")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.left_margin_with_statusbar_1440x2560);
        }
        return leftMargin;
    }

    public int getRightMarginWithStatusbar(int rightMargin) {
        String str = RESOURCE_SUFFIX;
        if (str.equals("_1047x1312")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.right_margin_with_statusbar_1047x1312);
        }
        if (str.equals("_1020x744")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.right_margin_with_statusbar_1020x744);
        }
        if (str.equals("_1041x1041")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.right_margin_with_statusbar_1041x1041);
        }
        if (str.equals("_1068x732")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.right_margin_with_statusbar_1068x732);
        }
        if (str.equals("_1080x1920")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.right_margin_with_statusbar_1080x1920);
        }
        if (str.equals("_1440x2560")) {
            return this.mContext.getResources().getDimensionPixelSize(R$dimen.right_margin_with_statusbar_1440x2560);
        }
        return rightMargin;
    }

    public void setCoverViewBackground(CoverScreen coverScreen) {
        if (RESOURCE_SUFFIX.equals("_1068x732")) {
            coverScreen.setBackgroundColor(-16777216);
        }
    }
}
