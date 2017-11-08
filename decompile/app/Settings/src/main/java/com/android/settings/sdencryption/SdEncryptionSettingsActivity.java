package com.android.settings.sdencryption;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.view.MenuItem;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.ItemUseStat;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class SdEncryptionSettingsActivity extends SettingsDrawerActivity {
    private static String ACTION_HWSDCRYPTD_STATE = "com.huawei.android.HWSDCRYPTD_STATE";
    private FragmentManager mFragmentManager;
    private Handler mHandler = new MyHandler();
    private boolean mIsPrimaryUser = false;
    private CryptionReceiver mReceiver = new CryptionReceiver();
    private String mSimpleState = "";
    private String mState = "no_card";
    private StorageEventListener mStorageListener = new StorageEventListener() {
        public void onStorageStateChanged(String path, String oldState, String newState) {
            SdLog.i("SdEncryptionSettingsActivity", "SD old state = " + oldState + ", new state = " + newState);
            if ("bad_removal".equals(newState) || "removed".equals(newState)) {
                SdEncryptionSettingsActivity.this.finishActivity();
            }
        }
    };
    private StorageManager mStorageManager;

    private class CryptionReceiver extends BroadcastReceiver {
        private CryptionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                SdLog.i("SdEncryptionSettingsActivity", "Receive action = " + action);
                if (SdEncryptionSettingsActivity.ACTION_HWSDCRYPTD_STATE.equals(action)) {
                    SdEncryptionSettingsActivity.this.handleCrypingMsg(intent);
                }
            }
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            boolean z;
            if (SdEncryptionSettingsActivity.this.mFragmentManager == null) {
                SdEncryptionSettingsActivity.this.mFragmentManager = SdEncryptionSettingsActivity.this.getFragmentManager();
            }
            SdEncryptionProgress fragment = (SdEncryptionProgress) SdEncryptionSettingsActivity.this.mFragmentManager.findFragmentByTag("SdEncryptionProgress");
            String str = "SdEncryptionSettingsActivity";
            StringBuilder append = new StringBuilder().append("fragment == null ?");
            if (fragment == null) {
                z = true;
            } else {
                z = false;
            }
            SdLog.d(str, append.append(z).toString());
            Bundle bundle = new Bundle();
            switch (msg.what) {
                case 906:
                    bundle.putBoolean("code", false);
                    bundle.putString("message", (String) msg.obj);
                    SdEncryptionSettingsActivity.this.switchToFragment("SdEncryptionProgress", bundle);
                    break;
                case 907:
                    bundle.putBoolean("code", true);
                    bundle.putString("message", (String) msg.obj);
                    SdEncryptionSettingsActivity.this.switchToFragment("SdEncryptionProgress", bundle);
                    break;
                default:
                    SdEncryptionSettingsActivity.this.finishActivity();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFragmentManager = getFragmentManager();
        registerReceiver();
    }

    private void initView() {
        if (!"enable".equals(this.mState) || SdEncryptionUtils.checkIfDecryptSdDisallowed(this) == null) {
            setContentView(2130969082);
            Intent intent = getIntent();
            if (intent != null) {
                switch (intent.getIntExtra("ENCRYPTION_ENABLE", 0)) {
                    case -106:
                        if ("half_encrypted".equals(this.mState)) {
                            this.mState = "half_decrypted";
                            break;
                        }
                        break;
                    case -105:
                        if ("half_decrypted".equals(this.mState)) {
                            this.mState = "half_encrypted";
                            break;
                        }
                        break;
                }
            }
            this.mSimpleState = SdEncryptionUtils.getSimpleCryptState(this.mState);
            initActionBar();
            if (!handleCrypingMsg(intent)) {
                String str = this.mState;
                if (str.equals("disable") || str.equals("half_encrypted") || str.equals("enable") || str.equals("half_decrypted")) {
                    switchToFragment("SdEncryptionIntroduction");
                } else {
                    if (!str.equals("encrypting")) {
                        if (str.equals("decrypting")) {
                        }
                    }
                    switchToFragment("SdEncryptionProgress");
                }
                return;
            }
            return;
        }
        finishActivity();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void onResume() {
        super.onResume();
        if (SdEncryptionUtils.isFeatureAvailable()) {
            this.mState = initState();
            if ("no_card".equals(this.mState)) {
                finish();
                return;
            }
            this.mIsPrimaryUser = UserManager.get(this).isPrimaryUser();
            if (this.mIsPrimaryUser) {
                initView();
                return;
            }
            Intent intent = new Intent();
            intent.setClass(this, SdEncryptionDialog.class);
            intent.setAction("com.android.settings.sdencryption.SHOWDIALOG");
            startActivityForResult(intent, 1);
            return;
        }
        finish();
    }

    protected void onPause() {
        checkAndNotifyState(2);
        super.onPause();
    }

    private String initState() {
        return SdEncryptionUtils.getSdCryptionState((Context) this);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        String str = this.mSimpleState;
        if (str.equals("Encrypt")) {
            actionBar.setTitle(2131628778);
        } else if (str.equals("Decrypt")) {
            actionBar.setTitle(2131628780);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                checkAndNotifyState(2);
                finishActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed() {
        checkAndNotifyState(2);
        super.onBackPressed();
        finishActivity();
    }

    private void checkAndNotifyState(int stateCode) {
        if (this.mFragmentManager == null) {
            this.mFragmentManager = getFragmentManager();
        }
        SdEncryptionProgress fragment = (SdEncryptionProgress) this.mFragmentManager.findFragmentByTag("SdEncryptionProgress");
        if (fragment != null && fragment.isRunning()) {
            notifyState(stateCode);
        }
    }

    private void notifyState(int stateCode) {
        String str = this.mSimpleState;
        if (str.equals("Encrypt")) {
            SdEncryptionUtils.sendStateBroadcast(this, stateCode, true);
        } else if (str.equals("Decrypt")) {
            SdEncryptionUtils.sendStateBroadcast(this, stateCode, false);
        }
    }

    public void showProgress() {
        if (!runKeyguardConfirmation(55)) {
            switchToFragment("SdEncryptionProgress");
        }
    }

    private void switchToFragment(String fragmentTag) {
        if (!isFinishing()) {
            if (this.mFragmentManager == null) {
                this.mFragmentManager = getFragmentManager();
            }
            SdLog.i("SdEncryptionSettingsActivity", "Switch to fragment " + fragmentTag);
            FragmentTransaction fragmentTransaction = this.mFragmentManager.beginTransaction();
            Fragment oldFragment = this.mFragmentManager.findFragmentById(2131887128);
            if (oldFragment != null) {
                fragmentTransaction.remove(oldFragment);
            }
            Bundle bundle = new Bundle();
            boolean enable = true;
            String str = this.mSimpleState;
            if (str.equals("Encrypt")) {
                bundle.putString("State", "Encrypt");
                enable = true;
            } else if (str.equals("Decrypt")) {
                bundle.putString("State", "Decrypt");
                enable = false;
            }
            Fragment fragment = null;
            if (fragmentTag.equals("SdEncryptionIntroduction")) {
                fragment = new SdEncryptionIntroduction();
            } else if (fragmentTag.equals("SdEncryptionProgress")) {
                notifyState(1);
                if (enable && !"encrypting".equals(this.mState)) {
                    ItemUseStat.getInstance().handleClick(this, 7, "encrypt_sdcard");
                    SdEncryptionUtils.startCryption(true, this);
                }
                if (!(enable || "decrypting".equals(this.mState))) {
                    ItemUseStat.getInstance().handleClick(this, 7, "dectypt_sdcard");
                    SdEncryptionUtils.startCryption(false, this);
                }
                fragment = new SdEncryptionProgress();
            }
            if (fragment == null) {
                finishActivity();
            } else {
                fragment.setArguments(bundle);
                fragmentTransaction.add(2131887128, fragment, fragmentTag);
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }

    private void switchToFragment(String fragmentTag, Bundle bundle) {
        if (!isFinishing()) {
            if (this.mFragmentManager == null) {
                this.mFragmentManager = getFragmentManager();
            }
            SdLog.i("SdEncryptionSettingsActivity", "Switch to fragment " + fragmentTag);
            FragmentTransaction fragmentTransaction = this.mFragmentManager.beginTransaction();
            Fragment oldFragment = this.mFragmentManager.findFragmentById(2131887128);
            if (oldFragment != null) {
                fragmentTransaction.remove(oldFragment);
            }
            Fragment fragment = null;
            if (fragmentTag.equals("SdEncryptionIntroduction")) {
                fragment = new SdEncryptionIntroduction();
            } else if (fragmentTag.equals("SdEncryptionProgress")) {
                fragment = new SdEncryptionProgress();
            }
            if (fragment == null) {
                finishActivity();
            } else {
                fragment.setArguments(bundle);
                fragmentTransaction.add(2131887128, fragment, fragmentTag);
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }

    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(this).launchConfirmationActivity(request, getActionBar().getTitle());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            finish();
            return;
        }
        if (requestCode == 55 && resultCode == -1) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    SdEncryptionSettingsActivity.this.switchToFragment("SdEncryptionProgress");
                }
            }, 100);
        }
    }

    private void finishActivity() {
        if (this.mFragmentManager == null) {
            this.mFragmentManager = getFragmentManager();
        }
        FragmentTransaction fragmentTransaction = this.mFragmentManager.beginTransaction();
        Fragment oldFragment = this.mFragmentManager.findFragmentById(2131887128);
        if (oldFragment != null) {
            fragmentTransaction.remove(oldFragment).commitNowAllowingStateLoss();
        }
        finish();
    }

    protected void onDestroy() {
        unRegisterReceiver();
        super.onDestroy();
    }

    private void registerReceiver() {
        this.mStorageManager = (StorageManager) getSystemService("storage");
        this.mStorageManager.registerListener(this.mStorageListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HWSDCRYPTD_STATE);
        registerReceiver(this.mReceiver, filter);
    }

    private void unRegisterReceiver() {
        if (this.mStorageManager != null) {
            this.mStorageManager.unregisterListener(this.mStorageListener);
            unregisterReceiver(this.mReceiver);
            this.mStorageManager = null;
        }
    }

    private boolean handleCrypingMsg(Intent intent) {
        if (intent == null) {
            return false;
        }
        int code = intent.getIntExtra("code", -10);
        String msg = intent.getStringExtra("message");
        SdLog.i("SdEncryptionSettingsActivity", "Receive code = " + code + ", msg = " + msg);
        if (code != 906) {
            return code == 907;
        } else {
            Message message = this.mHandler.obtainMessage();
            message.what = 906;
            message.obj = msg;
            message.sendToTarget();
            return true;
        }
    }
}
