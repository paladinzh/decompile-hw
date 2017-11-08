package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.text.BidiFormatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.server.input.InputManagerService;

final class AppErrorDialog extends BaseErrorDialog implements OnClickListener {
    static int ALREADY_SHOWING = -3;
    static int BACKGROUND_USER = -2;
    static final int CANCEL = 7;
    static int CANT_SHOW = -1;
    static final long DISMISS_TIMEOUT = 300000;
    static final int FORCE_QUIT = 1;
    static final int FORCE_QUIT_AND_REPORT = 2;
    static final int MUTE = 5;
    static final int RESTART = 3;
    static final int TIMEOUT = 6;
    private final boolean mForeground;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int result = msg.what;
            synchronized (AppErrorDialog.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (AppErrorDialog.this.mProc != null && AppErrorDialog.this.mProc.crashDialog == AppErrorDialog.this) {
                        AppErrorDialog.this.mProc.crashDialog = null;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            AppErrorDialog.this.mResult.set(result);
            removeMessages(6);
            AppErrorDialog.this.dismiss();
        }
    };
    private CharSequence mName;
    private final ProcessRecord mProc;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                AppErrorDialog.this.cancel();
            }
        }
    };
    private final boolean mRepeating;
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    static class Data {
        ProcessRecord proc;
        boolean repeating;
        AppErrorResult result;
        TaskRecord task;

        Data() {
        }
    }

    public AppErrorDialog(Context context, ActivityManagerService service, Data data) {
        boolean z;
        int i;
        LayoutParams attrs;
        super(context);
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = data.proc;
        this.mResult = data.result;
        this.mRepeating = data.repeating;
        if (data.task != null) {
            z = true;
        } else {
            z = false;
        }
        this.mForeground = z;
        BidiFormatter bidi = BidiFormatter.getInstance();
        if (this.mProc.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            this.mName = applicationLabel;
            if (applicationLabel != null) {
                if (this.mRepeating) {
                    i = 17040260;
                } else {
                    i = 17040258;
                }
                setTitle(res.getString(i, new Object[]{bidi.unicodeWrap(this.mName.toString()), bidi.unicodeWrap(this.mProc.info.processName)}));
                setCancelable(true);
                setCancelMessage(this.mHandler.obtainMessage(7));
                attrs = getWindow().getAttributes();
                attrs.setTitle("Application Error: " + this.mProc.info.processName);
                attrs.privateFlags |= InputManagerService.BTN_MOUSE;
                getWindow().setAttributes(attrs);
                if (this.mProc.persistent) {
                    getWindow().setType(2010);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), DISMISS_TIMEOUT);
            }
        }
        this.mName = this.mProc.processName;
        if (this.mRepeating) {
            i = 17040261;
        } else {
            i = 17040259;
        }
        setTitle(res.getString(i, new Object[]{bidi.unicodeWrap(this.mName.toString())}));
        setCancelable(true);
        setCancelMessage(this.mHandler.obtainMessage(7));
        attrs = getWindow().getAttributes();
        attrs.setTitle("Application Error: " + this.mProc.info.processName);
        attrs.privateFlags |= InputManagerService.BTN_MOUSE;
        getWindow().setAttributes(attrs);
        if (this.mProc.persistent) {
            getWindow().setType(2010);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), DISMISS_TIMEOUT);
    }

    protected void onCreate(Bundle savedInstanceState) {
        int i;
        int i2 = 8;
        super.onCreate(savedInstanceState);
        FrameLayout frame = (FrameLayout) findViewById(16908331);
        Context context = getContext();
        LayoutInflater.from(context).inflate(17367092, frame, true);
        boolean z = !this.mRepeating ? this.mForeground : false;
        boolean hasReceiver = this.mProc.errorReportReceiver != null;
        TextView restart = (TextView) findViewById(16909106);
        restart.setOnClickListener(this);
        if (z) {
            i = 0;
        } else {
            i = 8;
        }
        restart.setVisibility(i);
        TextView report = (TextView) findViewById(16909105);
        report.setOnClickListener(this);
        if (hasReceiver) {
            i = 0;
        } else {
            i = 8;
        }
        report.setVisibility(i);
        TextView close = (TextView) findViewById(16909103);
        if (z) {
            i = 8;
        } else {
            i = 0;
        }
        close.setVisibility(i);
        close.setOnClickListener(this);
        boolean showMute = (ActivityManagerService.IS_USER_BUILD || Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) == 0) ? false : true;
        TextView mute = (TextView) findViewById(16909108);
        mute.setOnClickListener(this);
        if (showMute) {
            i2 = 0;
        }
        mute.setVisibility(i2);
        findViewById(16909091).setVisibility(0);
    }

    public void onStart() {
        super.onStart();
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    protected void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.mReceiver);
    }

    public void dismiss() {
        if (!this.mResult.mHasResult) {
            this.mResult.set(1);
        }
        super.dismiss();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16909103:
                this.mHandler.obtainMessage(1).sendToTarget();
                return;
            case 16909105:
                this.mHandler.obtainMessage(2).sendToTarget();
                return;
            case 16909106:
                this.mHandler.obtainMessage(3).sendToTarget();
                return;
            case 16909108:
                this.mHandler.obtainMessage(5).sendToTarget();
                return;
            default:
                return;
        }
    }
}
