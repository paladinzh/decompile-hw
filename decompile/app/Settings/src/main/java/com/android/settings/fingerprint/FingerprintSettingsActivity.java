package com.android.settings.fingerprint;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import com.android.settings.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.fingerprint.utils.FingerprintUtils;

public class FingerprintSettingsActivity extends SettingsActivity {
    private boolean mIsFinishDelay = false;
    private boolean mIsToFinish = true;
    private long mLastPauseTime = -1;
    private int mUserId;

    public Intent getIntent() {
        Intent intent = super.getIntent();
        Bundle bundle = intent.getBundleExtra("fp_fragment_bundle");
        String fragmentClass = intent.getStringExtra(":settings:show_fragment");
        this.mUserId = intent.getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        if (fragmentClass != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", FingerprintSettingsFragment.class.getName());
        newIntent.putExtra(":settings:show_fragment_args", bundle);
        newIntent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (FingerprintSettingsFragment.class.getName().equals(fragmentName) || ChooseLockGenericFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        this.mIsToFinish = false;
        super.startPreferencePanel(fragmentClass, args, titleRes, titleText, resultTo, resultRequestCode);
    }

    protected void setIsToFinish(boolean isToFinish) {
        this.mIsToFinish = isToFinish;
    }

    public void onResume() {
        if (this.mLastPauseTime != -1) {
            Log.i("FingerprintSettings", "gap = " + (System.currentTimeMillis() - this.mLastPauseTime));
            if (System.currentTimeMillis() - this.mLastPauseTime > 120000) {
                finish();
            }
            this.mLastPauseTime = -1;
        }
        super.onResume();
    }

    public void onPause() {
        if (this.mIsFinishDelay) {
            this.mLastPauseTime = System.currentTimeMillis();
            this.mIsFinishDelay = false;
            this.mIsToFinish = false;
        }
        if (this.mIsToFinish) {
            FingerprintUtils.delayFinishActivity(this);
        } else {
            this.mIsToFinish = true;
        }
        super.onPause();
    }

    public static Preference getFingerprintPreferenceForUser(Context context, final int userId) {
        Preference fingerprintPreference = new Preference(context);
        fingerprintPreference.setKey("fp_function_category");
        fingerprintPreference.setTitle(2131627854);
        final String clazz = "com.android.settings.fingerprint.FingerprintSettingsActivity";
        fingerprintPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Context context = preference.getContext();
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", clazz);
                intent.putExtra("android.intent.extra.USER_ID", userId);
                context.startActivity(intent);
                return true;
            }
        });
        return fingerprintPreference;
    }

    protected void setIsToFinishDelay(boolean isToFinishDelay) {
        this.mIsFinishDelay = isToFinishDelay;
    }
}
