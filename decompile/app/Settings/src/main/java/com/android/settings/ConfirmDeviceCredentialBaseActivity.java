package com.android.settings;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.view.MenuItem;

public abstract class ConfirmDeviceCredentialBaseActivity extends SettingsActivity {
    private boolean mDark = false;
    private boolean mEnterAnimationPending;
    private boolean mFirstTimeVisible = true;
    private boolean mIsKeyguardLocked = false;
    private boolean mRestoring;

    protected void onCreate(Bundle savedState) {
        boolean isKeyguardLocked;
        super.onCreate(savedState);
        if (savedState == null) {
            isKeyguardLocked = ((KeyguardManager) getSystemService(KeyguardManager.class)).isKeyguardLocked();
        } else {
            isKeyguardLocked = savedState.getBoolean("STATE_IS_KEYGUARD_LOCKED", false);
        }
        this.mIsKeyguardLocked = isKeyguardLocked;
        if (this.mIsKeyguardLocked && getIntent().getBooleanExtra("com.android.settings.ConfirmCredentials.showWhenLocked", false)) {
            getWindow().addFlags(524288);
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        if (savedState != null) {
            isKeyguardLocked = true;
        } else {
            isKeyguardLocked = false;
        }
        this.mRestoring = isKeyguardLocked;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("STATE_IS_KEYGUARD_LOCKED", this.mIsKeyguardLocked);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    public void onResume() {
        super.onResume();
        if (!isChangingConfigurations() && !this.mRestoring && this.mDark && this.mFirstTimeVisible) {
            this.mFirstTimeVisible = false;
            prepareEnterAnimation();
            this.mEnterAnimationPending = true;
        }
    }

    private ConfirmDeviceCredentialBaseFragment getFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(2131887151);
        if (fragment == null || !(fragment instanceof ConfirmDeviceCredentialBaseFragment)) {
            return null;
        }
        return (ConfirmDeviceCredentialBaseFragment) fragment;
    }

    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (this.mEnterAnimationPending) {
            startEnterAnimation();
            this.mEnterAnimationPending = false;
        }
    }

    public void prepareEnterAnimation() {
        if (getFragment() != null) {
            getFragment().prepareEnterAnimation();
        }
    }

    public void startEnterAnimation() {
        if (getFragment() != null) {
            getFragment().startEnterAnimation();
        }
    }
}
