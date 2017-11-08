package com.huawei.permissionmanager.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class CountDownDialog extends AlertDialog {
    private static String LOG_TAG = "CountDownDialog";
    private static final int UPDATE_LIMIT_TIME = 1;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String btnRefuseText;
                    if (CountDownDialog.this.mLimitedTime > 0) {
                        btnRefuseText = CountDownDialog.this.mContext.getString(R.string.forbidden_with_number);
                        Button button = CountDownDialog.this.mForbidButton;
                        Object[] objArr = new Object[1];
                        CountDownDialog countDownDialog = CountDownDialog.this;
                        int i = countDownDialog.mLimitedTime;
                        countDownDialog.mLimitedTime = i - 1;
                        objArr[0] = Integer.valueOf(i);
                        button.setText(String.format(btnRefuseText, objArr));
                        break;
                    }
                    HwLog.i(CountDownDialog.LOG_TAG, "Permission dialog time counter: time out.");
                    if (!CountDownDialog.this.mLostFoucs) {
                        if (CountDownDialog.this.mThread != null) {
                            CountDownDialog.this.mThread.stopThread(true);
                        }
                        if (!CountDownDialog.this.mCallingStatus) {
                            if (!CountDownDialog.this.mClickedButton) {
                                CountDownDialog.this.mClickedButton = true;
                                CountDownDialog.this.mCallBack.callBackAddRecord(2, false, CountDownDialog.this);
                                CountDownDialog.this.mCallBack.callBackRelease(2, true);
                                break;
                            }
                            return;
                        }
                    }
                    btnRefuseText = CountDownDialog.this.mContext.getString(R.string.forbidden_with_number);
                    CountDownDialog.this.mForbidButton.setText(String.format(btnRefuseText, new Object[]{Integer.valueOf(CountDownDialog.this.mTimerSecond)}));
                    CountDownDialog.this.mRepeat = true;
                    break;
                    break;
            }
            super.handleMessage(msg);
        }
    };
    protected Button mAllowButton = null;
    protected AppInfo mAppInfo = null;
    protected CallBackHelper mCallBack;
    private boolean mCallingStatus = false;
    public boolean mClickedButton = false;
    protected Context mContext;
    protected Button mForbidButton = null;
    protected int mLimitedTime = 15;
    private boolean mLostFoucs = false;
    public int mPermissionType;
    private boolean mRepeat = false;
    protected MyThread mThread;
    protected int mTimerSecond = 15;
    protected TextView tvWarningMessage;

    public class MyThread extends Thread {
        private boolean isWait = false;
        private boolean mTickerStopped = true;

        public void stopThread(boolean run) {
            this.mTickerStopped = !run;
        }

        public void run() {
            while (this.mTickerStopped) {
                try {
                    synchronized (this) {
                        while (this.isWait) {
                            wait();
                        }
                    }
                    Message message = new Message();
                    message.what = 1;
                    CountDownDialog.this.handler.sendMessage(message);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public CountDownDialog(Context context, int theme) {
        super(context, theme);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mThread = new MyThread();
        this.mLimitedTime = this.mTimerSecond;
        this.mThread.stopThread(false);
        this.mThread.start();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        HwLog.i(LOG_TAG, "onFocusChange called " + hasWindowFocus);
        if (hasWindowFocus && this.mThread != null && this.mThread.isAlive()) {
            this.mLostFoucs = false;
            if (this.mRepeat) {
                this.mLimitedTime = this.mTimerSecond;
                this.mThread.stopThread(false);
                this.mRepeat = false;
            }
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }

    public void setCountdownTime(int time) {
        this.mTimerSecond = time;
    }

    public void setCallingFlag() {
        this.mCallingStatus = true;
    }
}
