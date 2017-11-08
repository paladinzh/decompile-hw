package com.android.settings;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.cust.HwCustUtils;

public class MasterClearHwBase extends OptionsMenuFragment {
    protected OnClickListener eraseInternalClickListener = new OnClickListener() {
        public void onClick(View v) {
            MasterClearHwBase.this.mInternalStorage.toggle();
            MasterClearHwBase.this.mInternalStorage.sendAccessibilityEvent(1);
        }
    };
    protected View mContentView;
    private HwCustMasterClearHwBase mCustMasterClearHwBase;
    protected CheckBox mInternalStorage;
    protected View mInternalStorageContainer;
    protected boolean mIsBackup = false;

    protected int getMetricsCategory() {
        return 100000;
    }

    protected Bitmap transformDrawableToBitmap(Drawable drawable) {
        Config config;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    protected boolean isFilterAccountType(String type) {
        if ("com.android.huawei.sim".equalsIgnoreCase(type) || "com.android.huawei.secondsim".equalsIgnoreCase(type)) {
            return true;
        }
        return "com.android.huawei.phone".equalsIgnoreCase(type);
    }

    protected void initViews() {
        this.mCustMasterClearHwBase = (HwCustMasterClearHwBase) HwCustUtils.createObj(HwCustMasterClearHwBase.class, new Object[]{getActivity()});
        this.mInternalStorageContainer = this.mContentView.findViewById(2131886783);
        this.mInternalStorage = (CheckBox) this.mContentView.findViewById(2131886784);
        if (LockPatternUtils.isDeviceEncryptionEnabled()) {
            this.mInternalStorage.setChecked(true);
        }
        this.mInternalStorageContainer.setOnClickListener(this.eraseInternalClickListener);
        this.mContentView.findViewById(2131886786).setVisibility(8);
    }

    protected void addExtrasExt(Preference preference) {
        preference.getExtras().putBoolean("key_has_backup", false);
        if (this.mInternalStorage != null && this.mInternalStorage.isChecked()) {
            preference.getExtras().putBoolean("erase_internal", true);
        }
    }

    protected void setIcon(TextView child, Drawable icon) {
        child.setHeight((int) (48.0f * getResources().getDisplayMetrics().density));
        if (icon != null) {
            Bitmap oldbmp = transformDrawableToBitmap(icon);
            Drawable drawable = null;
            if (oldbmp != null) {
                int iconSize = getResources().getDimensionPixelSize(2131558765);
                drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(oldbmp, iconSize, iconSize, false));
            }
            if (drawable != null) {
                icon = drawable;
            }
            child.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        }
    }

    protected boolean isFinal(Bundle savedInstanceState) {
        boolean isFinal = false;
        if (savedInstanceState != null) {
            isFinal = savedInstanceState.getBoolean("key_has_backup", false);
        }
        this.mIsBackup = isFinal;
        MLog.i(MasterClearHwBase.class.getCanonicalName(), ".mIsBackup:" + this.mIsBackup);
        return isFinal;
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }
}
