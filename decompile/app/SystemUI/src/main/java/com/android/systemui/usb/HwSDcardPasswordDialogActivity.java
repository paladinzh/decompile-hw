package com.android.systemui.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.huawei.timekeeper.TimeKeeper;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;
import fyusion.vislib.BuildConfig;

public class HwSDcardPasswordDialogActivity extends AlertActivity implements OnClickListener, View.OnClickListener {
    private boolean mCanDismiss = false;
    private CountDownTimer mCountdownTimer;
    private BroadcastReceiver mDissMissReceiver = null;
    private EditText mEditText;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (HwSDcardPasswordDialogActivity.this.mTimeKeeper.getRemainingChance() <= 0) {
                        HwSDcardPasswordDialogActivity.this.setPositiveButtonClickable(false, true);
                        HwSDcardPasswordDialogActivity.this.mRetryButton.setText(HwSDcardPasswordDialogActivity.this.getTimeStr());
                        HwSDcardPasswordDialogActivity.this.mEditText.setFocusable(false);
                        break;
                    }
                    break;
                case 1:
                    HwSDcardPasswordDialogActivity.this.mEditText.selectAll();
                    HwSDcardPasswordDialogActivity.this.mEditText.setError(HwSDcardPasswordDialogActivity.this.getRemainStr());
                    break;
                case 2:
                    HwSDcardPasswordDialogActivity.this.mEditText.selectAll();
                    break;
                case 3:
                    HwSDcardPasswordDialogActivity.this.mTitleView.setText(HwSDcardPasswordDialogActivity.this.mInputPassword);
                    HwSDcardPasswordDialogActivity.this.setPositiveButtonClickable(true, false);
                    HwSDcardPasswordDialogActivity.this.mEditText.setFocusable(true);
                    HwSDcardPasswordDialogActivity.this.mEditText.setFocusableInTouchMode(true);
                    HwSDcardPasswordDialogActivity.this.mEditText.requestFocus();
                    HwSDcardPasswordDialogActivity.this.mEditText.requestFocusFromTouch();
                    break;
                case 4:
                    HwSDcardPasswordDialogActivity.this.mCanDismiss = true;
                    HwSDcardPasswordDialogActivity.this.dismiss();
                    break;
                case 5:
                    HwSDcardPasswordDialogActivity.this.setPositiveButtonClickable(true, false);
                    break;
                case 6:
                    HwSDcardPasswordDialogActivity.this.setPositiveButtonClickable(false, false);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private String mInputPassword;
    private Button mRetryButton;
    private CheckBox mShowPassword;
    private TimeKeeper mTimeKeeper;
    private TextView mTitleView;
    private String mWhich;
    private String mWrongPassword;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        this.mWhich = getIntent().getStringExtra("which");
        AlertParams ap = this.mAlertParams;
        LayoutInflater inflater = ap.mInflater;
        View view;
        if ("bad_removal".equals(this.mWhich)) {
            view = inflater.inflate(R.layout.hw_sdcard_password_dialog_bad_remove, null, false);
            ((Button) view.findViewById(R.id.know_button)).setOnClickListener(this);
            ap.mView = view;
            setupAlert();
        } else if ("checking".equals(this.mWhich)) {
            view = inflater.inflate(R.layout.hw_sdcard_password_dialog_check, null, false);
            this.mEditText = (EditText) view.findViewById(R.id.password_edit);
            this.mTitleView = (TextView) view.findViewById(R.id.title_text);
            this.mTitleView.setText(this.mInputPassword);
            ap.mPositiveButtonText = getString(R.string.lockscreen_done);
            ap.mNegativeButtonText = getString(17039360);
            ap.mNegativeButtonListener = this;
            ap.mPositiveButtonListener = this;
            ap.mView = view;
            this.mEditText.setFilters(new InputFilter[]{new LengthFilter(16)});
            this.mEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable arg0) {
                }

                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                public void onTextChanged(CharSequence c, int arg1, int arg2, int arg3) {
                    if (HwSDcardPasswordDialogActivity.this.mTimeKeeper.getRemainingChance() > 0 && HwSDcardPasswordDialogActivity.this.mRetryButton.getVisibility() == 4) {
                        if (c.length() > 0) {
                            HwSDcardPasswordDialogActivity.this.mHandler.sendEmptyMessage(5);
                        } else {
                            HwSDcardPasswordDialogActivity.this.mHandler.sendEmptyMessage(6);
                        }
                    }
                }
            });
            this.mShowPassword = (CheckBox) view.findViewById(R.id.show_password);
            this.mShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int start = HwSDcardPasswordDialogActivity.this.mEditText.getSelectionStart();
                    int end = HwSDcardPasswordDialogActivity.this.mEditText.getSelectionEnd();
                    if (isChecked) {
                        HwSDcardPasswordDialogActivity.this.mEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        HwSDcardPasswordDialogActivity.this.mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    HwSDcardPasswordDialogActivity.this.mEditText.setSelection(start, end);
                }
            });
            this.mRetryButton = (Button) view.findViewById(R.id.retry_button);
            setupAlert();
            String name = "sdcard_lock_" + HwSdCardLockUtils.getHwSdLockManager(this).getSDCardId();
            this.mTimeKeeper = TimeKeeper.getInstance(this, name, 0, 1);
            this.mTimeKeeper.restore();
            TimeTickInfo t = this.mTimeKeeper.getTimeTickInfo();
            HwLog.i("HwSDcardPasswordDialogActivity", "time keeper=" + name + ", remain=" + this.mTimeKeeper.getRemainingChance() + ", wait=" + t.getHour() + "h:" + t.getMinute() + "m:" + t.getSecond() + "s");
            this.mTimeKeeper.registerObserver(new TimeObserver() {
                public void onTimeTick(TimeTickInfo info) {
                    HwSDcardPasswordDialogActivity.this.mHandler.sendEmptyMessage(0);
                }

                public void onTimeFinish() {
                    HwSDcardPasswordDialogActivity.this.mHandler.sendEmptyMessage(3);
                }
            });
            setPositiveButtonClickable(false, false);
        }
        setupBroadcast();
        getWindow().setCloseOnTouchOutside(false);
        getWindow().setType(2003);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mDissMissReceiver != null) {
            unregisterReceiver(this.mDissMissReceiver);
            this.mDissMissReceiver = null;
        }
    }

    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case -2:
                this.mCanDismiss = true;
                break;
            case -1:
                final String word = this.mEditText.getText().toString();
                if (word.length() > 0) {
                    SystemUIThread.runAsync(new SimpleAsyncTask() {
                        TimeKeeper timeKeeper = HwSDcardPasswordDialogActivity.this.mTimeKeeper;

                        public boolean runInThread() {
                            if (HwSDcardPasswordDialogActivity.this.allowpassword(word)) {
                                HwSDcardPasswordDialogActivity.this.mCanDismiss = true;
                                HwSDcardPasswordDialogActivity.this.mHandler.sendEmptyMessage(4);
                                if (this.timeKeeper != null) {
                                    this.timeKeeper.resetErrorCount(HwSDcardPasswordDialogActivity.this);
                                }
                            } else {
                                try {
                                    if (this.timeKeeper != null) {
                                        this.timeKeeper.addErrorCount();
                                    }
                                } catch (IllegalStateException e) {
                                    HwLog.e("HwSDcardPasswordDialogActivity", "mTimeKeeper.addErrorCount() error !");
                                }
                                HwSDcardPasswordDialogActivity.this.mHandler.sendEmptyMessage(1);
                            }
                            HwSDcardPasswordDialogActivity.this.mHandler.sendEmptyMessage(2);
                            return true;
                        }
                    });
                    break;
                } else {
                    this.mHandler.sendEmptyMessage(3);
                    return;
                }
        }
    }

    public void dismiss() {
        if (this.mCanDismiss) {
            if (this.mCountdownTimer != null) {
                this.mCountdownTimer.cancel();
                this.mCountdownTimer = null;
            }
            if (this.mTimeKeeper != null) {
                this.mTimeKeeper.unregisterAll();
                this.mTimeKeeper = null;
            }
            super.dismiss();
        }
    }

    private void setupBroadcast() {
        if (this.mDissMissReceiver != null) {
            HwLog.i("HwSDcardPasswordDialogActivity", "setupBroadcast:mDissMissReceiver != null");
            return;
        }
        this.mDissMissReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d("HwSDcardPasswordDialogActivity", "onReceive:" + intent + ", " + HwSDcardPasswordDialogActivity.this.mWhich);
                if (intent == null || intent.getAction() == null) {
                    HwLog.e("HwSDcardPasswordDialogActivity", "mDissMissReceiver intent == null || intent.getAction() == null ");
                    return;
                }
                if (intent.getAction().equals("android.intent.action.HWSDLOCK_LOCKED_SD_REMOVED")) {
                    HwSDcardPasswordDialogActivity.this.mCanDismiss = true;
                    HwSDcardPasswordDialogActivity.this.dismiss();
                } else if (intent.getAction().equals("com.android.systemui.action.SD_MOUNTED") && HwSDcardPasswordDialogActivity.this.mWhich.equals("bad_removal")) {
                    HwSDcardPasswordDialogActivity.this.mCanDismiss = true;
                    HwSDcardPasswordDialogActivity.this.dismiss();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.HWSDLOCK_LOCKED_SD_REMOVED");
        registerReceiver(this.mDissMissReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("com.android.systemui.action.SD_MOUNTED");
        registerReceiver(this.mDissMissReceiver, filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
    }

    private void init() {
        this.mInputPassword = getString(R.string.sdcard_input_password);
        this.mWrongPassword = getString(R.string.sdcard_input_wrong_password);
    }

    private boolean allowpassword(String word) {
        return HwSdCardLockUtils.unlockSDCard(this, word);
    }

    public void onClick(View arg0) {
        this.mCanDismiss = true;
        dismiss();
    }

    private void setPositiveButtonClickable(boolean able, boolean time) {
        int i;
        int i2 = 4;
        AlertController at = this.mAlert;
        at.getButton(-1).setEnabled(able);
        at.getButton(-1).setClickable(able);
        at.getButton(-1).setTextColor(getResources().getColor(able ? R.color.sdcard_password_enter_can_button : R.color.sdcard_password_enter_cannot_button));
        Button button = this.mRetryButton;
        if (time) {
            i = 0;
        } else {
            i = 4;
        }
        button.setVisibility(i);
        EditText editText = this.mEditText;
        if (!time) {
            i2 = 0;
        }
        editText.setVisibility(i2);
    }

    public String getTimeStr() {
        TimeTickInfo ti = this.mTimeKeeper.getTimeTickInfo();
        int i;
        if (ti.getHour() > 0) {
            if (ti.getMinute() > 30) {
                i = 1;
            } else {
                i = 0;
            }
            int hour = i + ti.getHour();
            return getResources().getQuantityString(R.plurals.lockscreen_try_after_hours, hour, new Object[]{Integer.valueOf(hour)});
        } else if (ti.getMinute() > 0) {
            if (ti.getSecond() > 30) {
                i = 1;
            } else {
                i = 0;
            }
            int minute = i + ti.getMinute();
            return getResources().getQuantityString(R.plurals.lockscreen_try_after_minutes, minute, new Object[]{Integer.valueOf(minute)});
        } else if (ti.getSecond() <= 0) {
            return BuildConfig.FLAVOR;
        } else {
            return getResources().getQuantityString(R.plurals.lockscreen_try_after_seconds, ti.getSecond(), new Object[]{Integer.valueOf(ti.getSecond())});
        }
    }

    public String getRemainStr() {
        try {
            int remain = this.mTimeKeeper.getRemainingChance();
            if (remain > 0) {
                return getResources().getQuantityString(R.plurals.lockscreen_warn_wrong_password, remain, new Object[]{Integer.valueOf(remain)});
            }
        } catch (NullPointerException e) {
            HwLog.e("HwSDcardPasswordDialogActivity", "NullPointerException");
        }
        return null;
    }
}
