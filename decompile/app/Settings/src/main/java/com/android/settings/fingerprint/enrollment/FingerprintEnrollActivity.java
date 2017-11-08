package com.android.settings.fingerprint.enrollment;

import android.app.StatusBarManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.SettingsActivity;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.android.settings.navigation.NaviUtils;
import com.huawei.cust.HwCustUtils;

public class FingerprintEnrollActivity extends SettingsActivity {
    private HwCustFingerprintEnrollActivity mHwCustFingerprintEnrollActivity;
    private boolean mIsToFinish = true;
    private boolean mNeedRealFinishResult;
    private boolean mSetOwnerFingerprint;
    private StatusBarManager mStatusBarManager;

    public void setNeedRealFinishResult(boolean need) {
        this.mNeedRealFinishResult = need;
    }

    public Intent getIntent() {
        boolean z = true;
        Intent intent = super.getIntent();
        Bundle bundle = intent.getBundleExtra("fp_fragment_bundle");
        String fragmentClass = intent.getStringExtra(":settings:show_fragment");
        if (1 != intent.getIntExtra("hidden_space_main_user_type", -1)) {
            z = false;
        }
        this.mSetOwnerFingerprint = z;
        if (fragmentClass != null) {
            return intent;
        }
        Log.d("FingerprintEnrollActivity", "frgmentClass is null, i.e., started from action");
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", FingerprintEnrollFragment.class.getName());
        newIntent.putExtra(":settings:show_fragment_args", bundle);
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (FingerprintEnrollFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mSetOwnerFingerprint) {
            setTitle(2131628725);
        } else {
            setTitle(2131627636);
        }
        this.mHwCustFingerprintEnrollActivity = (HwCustFingerprintEnrollActivity) HwCustUtils.createObj(HwCustFingerprintEnrollActivity.class, new Object[]{this});
    }

    public void onResume() {
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            if (this.mStatusBarManager == null) {
                this.mStatusBarManager = (StatusBarManager) getSystemService("statusbar");
            }
            this.mStatusBarManager.disable(16777216 | 2097152);
        }
        super.onResume();
    }

    public void onPause() {
        if (!this.mIsToFinish) {
            this.mIsToFinish = true;
        } else if (!this.mNeedRealFinishResult) {
            setResult(101);
            FingerprintUtils.delayFinishActivity(this);
        }
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            this.mStatusBarManager.disable(0);
        }
        super.onPause();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("FingerprintEnrollActivity", "onAttachedToWindow");
        if (this.mHwCustFingerprintEnrollActivity.isFrontFingerPrint()) {
            this.mHwCustFingerprintEnrollActivity.hideVirtualKey();
        }
    }

    public void onDetachedFromWindow() {
        Log.d("FingerprintEnrollActivity", "onDetachedFromWindow");
        super.onDetachedFromWindow();
        if (this.mHwCustFingerprintEnrollActivity.isFrontFingerPrint()) {
            this.mHwCustFingerprintEnrollActivity.recoverVirtualKey();
        }
    }
}
