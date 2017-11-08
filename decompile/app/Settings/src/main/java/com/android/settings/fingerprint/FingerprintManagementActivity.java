package com.android.settings.fingerprint;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.SettingsActivity;

public class FingerprintManagementActivity extends SettingsActivity {
    private boolean mIsToFinish = true;

    public void setmIsToFinish(boolean mIsToFinish) {
        this.mIsToFinish = mIsToFinish;
    }

    public Intent getIntent() {
        Intent intent = super.getIntent();
        Bundle bundle = intent.getBundleExtra("fp_fragment_bundle");
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Log.d("FingerprintManagement", "frgmentClass is null, i.e., started from action");
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", FingerprintManageFragment.class.getName());
        newIntent.putExtra(":settings:show_fragment_args", bundle);
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (FingerprintManageFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    public void onPause() {
        if (this.mIsToFinish) {
            setResult(101);
            finish();
        } else {
            this.mIsToFinish = true;
        }
        super.onPause();
    }
}
