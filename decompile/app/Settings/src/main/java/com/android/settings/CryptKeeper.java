package com.android.settings;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IMountService;
import android.os.storage.IMountService.Stub;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v7.appcompat.R$id;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView.OnPatternListener;
import java.util.List;

public class CryptKeeper extends Activity implements OnEditorActionListener, OnKeyListener, OnTouchListener, TextWatcher {
    private AudioManager mAudioManager;
    protected OnPatternListener mChooseNewLockPatternListener = new OnPatternListener() {
        public void onPatternStart() {
            CryptKeeper.this.mLockPatternView.removeCallbacks(CryptKeeper.this.mClearPatternRunnable);
        }

        public void onPatternCleared() {
        }

        public void onPatternDetected(List<Cell> pattern) {
            CryptKeeper.this.mLockPatternView.setEnabled(false);
            if (pattern.size() >= 4) {
                new DecryptTask().execute(new String[]{LockPatternUtils.patternToString(pattern)});
                return;
            }
            CryptKeeper.this.fakeUnlockAttempt(CryptKeeper.this.mLockPatternView);
        }

        public void onPatternCellAdded(List<Cell> list) {
        }
    };
    private final Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            CryptKeeper.this.mLockPatternView.clearPattern();
        }
    };
    private boolean mCooldown = false;
    private boolean mCorrupt;
    private boolean mEncryptionGoneBad;
    private final Runnable mFakeUnlockAttemptRunnable = new Runnable() {
        public void run() {
            CryptKeeper.this.handleBadAttempt(Integer.valueOf(1));
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    CryptKeeper.this.updateProgress();
                    return;
                case 2:
                    CryptKeeper.this.notifyUser();
                    return;
                default:
                    Log.w("CryptKeeper", "mHandler unknown id");
                    return;
            }
        }
    };
    private LockPatternView mLockPatternView;
    private int mNotificationCountdown = 0;
    private EditText mPasswordEntry;
    private int mReleaseWakeLockCountdown = 0;
    private StatusBarManager mStatusBar;
    private int mStatusString = 2131626263;
    private boolean mValidationComplete;
    private boolean mValidationRequested;
    WakeLock mWakeLock;

    private class DecryptTask extends AsyncTask<String, Void, Integer> {
        private DecryptTask() {
        }

        private void hide(int id) {
            View view = CryptKeeper.this.findViewById(id);
            if (view != null) {
                view.setVisibility(8);
            }
        }

        protected void onPreExecute() {
            super.onPreExecute();
            CryptKeeper.this.beginAttempt();
        }

        protected Integer doInBackground(String... params) {
            IMountService service = CryptKeeper.this.getMountService();
            if (service != null) {
                return Integer.valueOf(service.decryptStorage(params[0]));
            }
            try {
                return Integer.valueOf(-1);
            } catch (Exception e) {
                Log.e("CryptKeeper", "Error while decrypting...", e);
                return Integer.valueOf(-1);
            }
        }

        protected void onPostExecute(Integer failedAttempts) {
            if (failedAttempts.intValue() == 0) {
                if (CryptKeeper.this.mLockPatternView != null) {
                    CryptKeeper.this.mLockPatternView.removeCallbacks(CryptKeeper.this.mClearPatternRunnable);
                    CryptKeeper.this.mLockPatternView.postDelayed(CryptKeeper.this.mClearPatternRunnable, 500);
                }
                ((TextView) CryptKeeper.this.findViewById(2131886287)).setText(2131626270);
                hide(2131886424);
                hide(2131886435);
                hide(2131886374);
                hide(2131886434);
                hide(2131886423);
            } else if (failedAttempts.intValue() == 30) {
                Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
                intent.addFlags(268435456);
                CryptKeeper.this.sendBroadcast(intent);
            } else if (failedAttempts.intValue() == -1) {
                CryptKeeper.this.setContentView(2130968706);
                CryptKeeper.this.showFactoryReset(true);
            } else {
                CryptKeeper.this.handleBadAttempt(failedAttempts);
            }
        }
    }

    private static class NonConfigurationInstanceState {
        final WakeLock wakelock;

        NonConfigurationInstanceState(WakeLock _wakelock) {
            this.wakelock = _wakelock;
        }
    }

    private class ValidationTask extends AsyncTask<Void, Void, Boolean> {
        int state;

        private ValidationTask() {
        }

        protected Boolean doInBackground(Void... params) {
            boolean z = false;
            IMountService service = CryptKeeper.this.getMountService();
            if (service == null) {
                try {
                    return Boolean.valueOf(true);
                } catch (RemoteException e) {
                    Log.w("CryptKeeper", "Unable to get encryption state properly");
                    return Boolean.valueOf(true);
                }
            }
            this.state = service.getEncryptionState();
            if (this.state == 1) {
                Log.w("CryptKeeper", "Unexpectedly in CryptKeeper even though there is no encryption.");
                return Boolean.valueOf(true);
            }
            if (this.state == 0) {
                z = true;
            }
            return Boolean.valueOf(z);
        }

        protected void onPostExecute(Boolean result) {
            boolean z = true;
            CryptKeeper.this.mValidationComplete = true;
            if (Boolean.FALSE.equals(result)) {
                CryptKeeper.this.mEncryptionGoneBad = true;
                CryptKeeper cryptKeeper = CryptKeeper.this;
                if (this.state != -4) {
                    z = false;
                }
                cryptKeeper.mCorrupt = z;
            }
            CryptKeeper.this.setupUi();
        }
    }

    private void beginAttempt() {
        ((TextView) findViewById(2131886287)).setText(2131626269);
    }

    private void handleBadAttempt(Integer failedAttempts) {
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 1500);
        }
        if (failedAttempts.intValue() % 10 == 0) {
            this.mCooldown = true;
            cooldown();
            return;
        }
        TextView status = (TextView) findViewById(2131886287);
        if (30 - failedAttempts.intValue() < 10) {
            status.setText(TextUtils.expandTemplate(getText(2131624708), new CharSequence[]{Integer.toString(remainingAttempts)}));
        } else {
            int passwordType = 0;
            try {
                IMountService service = getMountService();
                if (service != null) {
                    passwordType = service.getPasswordType();
                }
            } catch (Exception e) {
                Log.e("CryptKeeper", "Error calling mount service " + e);
            }
            if (passwordType == 3) {
                status.setText(2131626268);
            } else if (passwordType == 2) {
                status.setText(2131626266);
            } else {
                status.setText(2131626267);
            }
        }
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
            this.mLockPatternView.setEnabled(true);
        }
        if (this.mPasswordEntry != null) {
            this.mPasswordEntry.setEnabled(true);
            ((InputMethodManager) getSystemService("input_method")).showSoftInput(this.mPasswordEntry, 0);
            setBackFunctionality(true);
        }
    }

    private boolean isDebugView() {
        return getIntent().hasExtra("com.android.settings.CryptKeeper.DEBUG_FORCE_VIEW");
    }

    private boolean isDebugView(String viewType) {
        return viewType.equals(getIntent().getStringExtra("com.android.settings.CryptKeeper.DEBUG_FORCE_VIEW"));
    }

    private void notifyUser() {
        if (this.mNotificationCountdown > 0) {
            this.mNotificationCountdown--;
        } else if (this.mAudioManager != null) {
            try {
                this.mAudioManager.playSoundEffect(5, 100);
            } catch (Exception e) {
                Log.w("CryptKeeper", "notifyUser: Exception while playing sound: " + e);
            }
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 5000);
        if (!this.mWakeLock.isHeld()) {
            return;
        }
        if (this.mReleaseWakeLockCountdown > 0) {
            this.mReleaseWakeLockCountdown--;
        } else {
            this.mWakeLock.release();
        }
    }

    public void onBackPressed() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String state = SystemProperties.get("vold.decrypt");
        if (isDebugView() || !("".equals(state) || "trigger_restart_framework".equals(state))) {
            try {
                if (getResources().getBoolean(2131492901)) {
                    setRequestedOrientation(-1);
                }
            } catch (NotFoundException e) {
                Log.w("CryptKeeper", "onCreate NotFoundException:", e);
            }
            this.mStatusBar = (StatusBarManager) getSystemService("statusbar");
            this.mStatusBar.disable(52887552);
            if (savedInstanceState != null) {
                this.mCooldown = savedInstanceState.getBoolean("cooldown");
            }
            setAirplaneModeIfNecessary();
            this.mAudioManager = (AudioManager) getSystemService("audio");
            NonConfigurationInstanceState lastInstance = getLastNonConfigurationInstance();
            if (lastInstance instanceof NonConfigurationInstanceState) {
                this.mWakeLock = lastInstance.wakelock;
            }
            return;
        }
        disableCryptKeeperComponent(this);
        finish();
    }

    public void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
        setupUi();
    }

    private void setupUi() {
        if (this.mEncryptionGoneBad || isDebugView("error")) {
            setContentView(2130968706);
            showFactoryReset(this.mCorrupt);
            return;
        }
        if (!"".equals(SystemProperties.get("vold.encrypt_progress")) || isDebugView("progress")) {
            setContentView(2130968706);
            encryptionProgressInit();
        } else if (this.mValidationComplete || isDebugView("password")) {
            new AsyncTask<Void, Void, Void>() {
                String owner_info;
                int passwordType = 0;
                boolean password_visible;
                boolean pattern_visible;

                public Void doInBackground(Void... v) {
                    boolean z = false;
                    try {
                        IMountService service = CryptKeeper.this.getMountService();
                        if (service != null) {
                            boolean z2;
                            this.passwordType = service.getPasswordType();
                            this.owner_info = service.getField("OwnerInfo");
                            if ("0".equals(service.getField("PatternVisible"))) {
                                z2 = false;
                            } else {
                                z2 = true;
                            }
                            this.pattern_visible = z2;
                            if (!"0".equals(service.getField("PasswordVisible"))) {
                                z = true;
                            }
                            this.password_visible = z;
                        }
                    } catch (Exception e) {
                        Log.e("CryptKeeper", "Error calling mount service " + e);
                    }
                    return null;
                }

                public void onPostExecute(Void v) {
                    int i;
                    boolean z = true;
                    ContentResolver contentResolver = CryptKeeper.this.getContentResolver();
                    String str = "show_password";
                    if (this.password_visible) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    System.putInt(contentResolver, str, i);
                    if (this.passwordType == 3) {
                        CryptKeeper.this.setContentView(2130968704);
                        CryptKeeper.this.mStatusString = 2131626264;
                    } else if (this.passwordType == 2) {
                        CryptKeeper.this.setContentView(2130968702);
                        CryptKeeper.this.setBackFunctionality(false);
                        CryptKeeper.this.mStatusString = 2131626265;
                    } else {
                        CryptKeeper.this.setContentView(2130968700);
                        CryptKeeper.this.mStatusString = 2131626263;
                    }
                    ((TextView) CryptKeeper.this.findViewById(2131886287)).setText(CryptKeeper.this.mStatusString);
                    TextView ownerInfo = (TextView) CryptKeeper.this.findViewById(2131886434);
                    if (TextUtils.isEmpty(this.owner_info)) {
                        ownerInfo.setVisibility(8);
                    } else {
                        ownerInfo.setText(this.owner_info);
                        ownerInfo.setSelected(true);
                    }
                    CryptKeeper.this.passwordEntryInit();
                    CryptKeeper.this.findViewById(16908290).setSystemUiVisibility(4194304);
                    if (CryptKeeper.this.mLockPatternView != null) {
                        LockPatternView -get2 = CryptKeeper.this.mLockPatternView;
                        if (this.pattern_visible) {
                            z = false;
                        }
                        -get2.setInStealthMode(z);
                    }
                    if (CryptKeeper.this.mCooldown) {
                        CryptKeeper.this.setBackFunctionality(false);
                        CryptKeeper.this.cooldown();
                    }
                }
            }.execute(new Void[0]);
        } else if (!this.mValidationRequested) {
            new ValidationTask().execute((Void[]) null);
            this.mValidationRequested = true;
        }
    }

    public void onStop() {
        super.onStop();
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
    }

    public Object onRetainNonConfigurationInstance() {
        NonConfigurationInstanceState state = new NonConfigurationInstanceState(this.mWakeLock);
        this.mWakeLock = null;
        return state;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    private void encryptionProgressInit() {
        Log.d("CryptKeeper", "Encryption progress screen initializing.");
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(26, "CryptKeeper");
            this.mWakeLock.acquire();
        }
        ((ProgressBar) findViewById(2131886426)).setIndeterminate(true);
        setBackFunctionality(false);
        updateProgress();
    }

    private void showFactoryReset(boolean corrupt) {
        findViewById(2131886427).setVisibility(8);
        Button button = (Button) findViewById(2131886428);
        button.setVisibility(0);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
                CryptKeeper.this.sendBroadcast(intent);
            }
        });
        if (corrupt) {
            ((TextView) findViewById(R$id.title)).setText(2131624712);
            ((TextView) findViewById(2131886287)).setText(2131624713);
        } else {
            ((TextView) findViewById(R$id.title)).setText(2131624710);
            ((TextView) findViewById(2131886287)).setText(2131624711);
        }
        View view = findViewById(2131886429);
        if (view != null) {
            view.setVisibility(0);
        }
    }

    private void updateProgress() {
        String state = SystemProperties.get("vold.encrypt_progress");
        if ("error_partially_encrypted".equals(state)) {
            showFactoryReset(false);
            return;
        }
        CharSequence status = getText(2131624705);
        int percent = 0;
        try {
            percent = isDebugView() ? 50 : Integer.parseInt(state);
        } catch (Exception e) {
            Log.w("CryptKeeper", "Error parsing progress: " + e.toString());
        }
        String progress = Integer.toString(percent);
        try {
            int time = Integer.parseInt(SystemProperties.get("vold.encrypt_time_remaining"));
            if (time >= 0) {
                progress = DateUtils.formatElapsedTime((long) (((time + 9) / 10) * 10));
                status = getText(2131624706);
            }
        } catch (Exception e2) {
            Log.i("CryptKeeper", "updateProgress Exception:", e2);
        }
        TextView tv = (TextView) findViewById(2131886287);
        if (tv != null) {
            tv.setText(TextUtils.expandTemplate(status, new CharSequence[]{progress}));
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private void cooldown() {
        if (this.mPasswordEntry != null) {
            this.mPasswordEntry.setEnabled(false);
        }
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setEnabled(false);
        }
        ((TextView) findViewById(2131886287)).setText(2131624707);
    }

    private final void setBackFunctionality(boolean isEnabled) {
        if (isEnabled) {
            this.mStatusBar.disable(52887552);
        } else {
            this.mStatusBar.disable(57081856);
        }
    }

    private void fakeUnlockAttempt(View postingView) {
        beginAttempt();
        postingView.postDelayed(this.mFakeUnlockAttemptRunnable, 1000);
    }

    private void passwordEntryInit() {
        this.mPasswordEntry = (EditText) findViewById(2131886424);
        if (this.mPasswordEntry != null) {
            this.mPasswordEntry.setOnEditorActionListener(this);
            this.mPasswordEntry.requestFocus();
            this.mPasswordEntry.setOnKeyListener(this);
            this.mPasswordEntry.setOnTouchListener(this);
            this.mPasswordEntry.addTextChangedListener(this);
        }
        this.mLockPatternView = (LockPatternView) findViewById(2131886374);
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setOnPatternListener(this.mChooseNewLockPatternListener);
        }
        if (!getTelephonyManager().isVoiceCapable()) {
            View emergencyCall = findViewById(2131886423);
            if (emergencyCall != null) {
                emergencyCall.setVisibility(8);
            }
        }
        View imeSwitcher = findViewById(2131886435);
        final InputMethodManager imm = (InputMethodManager) getSystemService("input_method");
        if (imeSwitcher != null && hasMultipleEnabledIMEsOrSubtypes(imm, false)) {
            imeSwitcher.setVisibility(0);
            imeSwitcher.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    imm.showInputMethodPicker(false);
                }
            });
        }
        if (this.mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService("power");
            if (pm != null) {
                this.mWakeLock = pm.newWakeLock(26, "CryptKeeper");
                this.mWakeLock.acquire();
                this.mReleaseWakeLockCountdown = 96;
            }
        }
        if (this.mLockPatternView == null && !this.mCooldown) {
            getWindow().setSoftInputMode(5);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    imm.showSoftInputUnchecked(0, null);
                }
            }, 0);
        }
        updateEmergencyCallButtonState();
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 120000);
        getWindow().addFlags(4718592);
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager imm, boolean shouldIncludeAuxiliarySubtypes) {
        boolean z = true;
        int filteredImisCount = 0;
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            if (filteredImisCount > 1) {
                return true;
            }
            List<InputMethodSubtype> subtypes = imm.getEnabledInputMethodSubtypeList(imi, true);
            if (subtypes.isEmpty()) {
                filteredImisCount++;
            } else {
                int auxCount = 0;
                for (InputMethodSubtype subtype : subtypes) {
                    if (subtype.isAuxiliary()) {
                        auxCount++;
                    }
                }
                if (subtypes.size() - auxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
                    filteredImisCount++;
                }
            }
        }
        if (filteredImisCount <= 1 && imm.getEnabledInputMethodSubtypeList(null, false).size() <= 1) {
            z = false;
        }
        return z;
    }

    private IMountService getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return Stub.asInterface(service);
        }
        return null;
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != 0 && actionId != 6) {
            return false;
        }
        String password = v.getText().toString();
        if (TextUtils.isEmpty(password)) {
            return true;
        }
        v.setText(null);
        this.mPasswordEntry.setEnabled(false);
        setBackFunctionality(false);
        if (password.length() >= 4) {
            new DecryptTask().execute(new String[]{password});
        } else {
            fakeUnlockAttempt(this.mPasswordEntry);
        }
        return true;
    }

    private final void setAirplaneModeIfNecessary() {
        if (!(getTelephonyManager().getLteOnCdmaMode() == 1)) {
            Log.d("CryptKeeper", "Going into airplane mode.");
            Global.putInt(getContentResolver(), "airplane_mode_on", 1);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", true);
            sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void updateEmergencyCallButtonState() {
        Button emergencyCall = (Button) findViewById(2131886423);
        if (emergencyCall != null) {
            if (isEmergencyCallCapable()) {
                int textId;
                emergencyCall.setVisibility(0);
                emergencyCall.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        CryptKeeper.this.takeEmergencyCallAction();
                    }
                });
                if (getTelecomManager().isInCall()) {
                    textId = 2131626357;
                } else {
                    textId = 2131626356;
                }
                emergencyCall.setCompoundDrawablesWithIntrinsicBounds(2130837661, 0, 0, 0);
                emergencyCall.setText(textId);
                return;
            }
            emergencyCall.setVisibility(8);
        }
    }

    private boolean isEmergencyCallCapable() {
        return getResources().getBoolean(17956956);
    }

    private void takeEmergencyCallAction() {
        TelecomManager telecomManager = getTelecomManager();
        if (telecomManager.isInCall()) {
            telecomManager.showInCallScreen(false);
        } else {
            launchEmergencyDialer();
        }
    }

    private void launchEmergencyDialer() {
        Intent intent = new Intent("com.android.phone.EmergencyDialer.DIAL");
        intent.setFlags(276824064);
        setBackFunctionality(true);
        startActivity(intent);
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) getSystemService("phone");
    }

    private TelecomManager getTelecomManager() {
        return (TelecomManager) getSystemService("telecom");
    }

    private void delayAudioNotification() {
        this.mNotificationCountdown = 20;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        delayAudioNotification();
        return false;
    }

    public boolean onTouch(View v, MotionEvent event) {
        delayAudioNotification();
        return false;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        delayAudioNotification();
    }

    public void afterTextChanged(Editable s) {
    }

    private static void disableCryptKeeperComponent(Context context) {
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(context, CryptKeeper.class);
        Log.d("CryptKeeper", "Disabling component " + name);
        pm.setComponentEnabledSetting(name, 2, 1);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("mCooldown", this.mCooldown);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mCooldown = savedInstanceState.getBoolean("mCooldown");
    }
}
