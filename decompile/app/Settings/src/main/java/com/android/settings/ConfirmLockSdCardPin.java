package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import com.android.settings.search.Index;

public class ConfirmLockSdCardPin extends SettingsActivity {
    protected BroadcastReceiver mSdcardReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.HWSDLOCK_LOCKED_SD_REMOVED".equals(intent.getAction())) {
                ConfirmLockSdCardPin.this.finish();
            }
        }
    };
    private StorageEventListener mStorageListener = new StorageEventListener() {
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Index.getInstance(ConfirmLockSdCardPin.this).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
            if ("bad_removal".equals(newState)) {
                ConfirmLockSdCardPin.this.finish();
            }
        }
    };
    private StorageManager mStorageManager;

    public static class ConfirmLockSdCardPinFragment extends ConfirmPasswordFragmentBase {
        protected boolean isAlphaMode() {
            return true;
        }

        protected boolean needRestore() {
            return true;
        }

        protected String getTimeKeeperName() {
            return "sdcard_lock_" + SdCardLockUtils.getCurrentSDCardId(getActivity());
        }

        protected boolean doCheckPassword(String password) {
            onPasswordChecked(SdCardLockUtils.unlockSDCard(getActivity(), password), null);
            return true;
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", ConfirmLockSdCardPinFragment.class.getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ConfirmLockSdCardPinFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(2131628098);
        this.mStorageManager = (StorageManager) getSystemService("storage");
        this.mStorageManager.registerListener(this.mStorageListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HWSDLOCK_LOCKED_SD_REMOVED");
        registerReceiver(this.mSdcardReceiver, intentFilter);
    }

    protected void onStop() {
        super.onStop();
        SettingsExtUtils.checkHideSoftInput(this);
    }

    public void onDestroy() {
        if (!(this.mStorageManager == null || this.mStorageListener == null)) {
            this.mStorageManager.unregisterListener(this.mStorageListener);
        }
        unregisterReceiver(this.mSdcardReceiver);
        super.onDestroy();
    }
}
