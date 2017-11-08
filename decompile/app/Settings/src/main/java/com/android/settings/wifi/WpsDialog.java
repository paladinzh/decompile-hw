package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WpsCallback;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class WpsDialog extends AlertDialog {
    private Context mContext;
    DialogState mDialogState = DialogState.WPS_INIT;
    private final IntentFilter mFilter;
    private Handler mHandler = new Handler();
    private String mMsgString = "";
    private ProgressBar mProgressBar;
    private BroadcastReceiver mReceiver;
    private TextView mTextView;
    private ProgressBar mTimeoutBar;
    private Timer mTimer;
    private View mView;
    private WifiManager mWifiManager;
    private WpsCallback mWpsListener;
    private int mWpsSetup;

    private enum DialogState {
        WPS_INIT,
        WPS_START,
        WPS_COMPLETE,
        CONNECTED,
        WPS_FAILED
    }

    public WpsDialog(Context context, int wpsSetup) {
        super(context);
        this.mContext = context;
        this.mWpsSetup = wpsSetup;
        if (this.mWpsSetup == 0) {
            setTitle(2131627557);
        } else if (1 == this.mWpsSetup) {
            setTitle(2131627338);
        }
        this.mWpsListener = new WpsCallback() {
            public void onStarted(String pin) {
                if (pin != null) {
                    WpsDialog.this.updateDialog(DialogState.WPS_START, String.format(WpsDialog.this.mContext.getString(2131624956), new Object[]{pin}));
                    return;
                }
                WpsDialog.this.updateDialog(DialogState.WPS_START, WpsDialog.this.mContext.getString(2131624955));
            }

            public void onSucceeded() {
                WpsDialog.this.updateDialog(DialogState.WPS_COMPLETE, WpsDialog.this.mContext.getString(2131624957));
            }

            public void onFailed(int reason) {
                String msg;
                switch (reason) {
                    case 1:
                        msg = WpsDialog.this.mContext.getString(2131624959);
                        break;
                    case 3:
                        msg = WpsDialog.this.mContext.getString(2131624964);
                        break;
                    case 4:
                        msg = WpsDialog.this.mContext.getString(2131624961);
                        break;
                    case 5:
                        msg = WpsDialog.this.mContext.getString(2131624962);
                        break;
                    default:
                        msg = WpsDialog.this.mContext.getString(2131624960);
                        break;
                }
                WpsDialog.this.updateDialog(DialogState.WPS_FAILED, msg);
            }
        };
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WpsDialog.this.handleEvent(context, intent);
            }
        };
        setCanceledOnTouchOutside(false);
    }

    public Bundle onSaveInstanceState() {
        Bundle bundle = super.onSaveInstanceState();
        bundle.putString("android:dialogState", this.mDialogState.toString());
        bundle.putString("android:dialogMsg", this.mMsgString.toString());
        return bundle;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            super.onRestoreInstanceState(savedInstanceState);
            DialogState dialogState = this.mDialogState;
            updateDialog(DialogState.valueOf(savedInstanceState.getString("android:dialogState")), savedInstanceState.getString("android:dialogMsg"));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.mView = getLayoutInflater().inflate(2130969287, null);
        this.mTextView = (TextView) this.mView.findViewById(2131887598);
        this.mTextView.setText(2131624954);
        this.mTimeoutBar = (ProgressBar) this.mView.findViewById(2131887599);
        this.mTimeoutBar.setMax(120);
        this.mTimeoutBar.setProgress(0);
        this.mProgressBar = (ProgressBar) this.mView.findViewById(2131887600);
        this.mProgressBar.setVisibility(8);
        setButton(-1, this.mContext.getString(2131625657), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WpsDialog.this.dismiss();
            }
        });
        this.mWifiManager = (WifiManager) this.mContext.getApplicationContext().getSystemService("wifi");
        setView(this.mView);
        if (savedInstanceState == null) {
            WpsInfo wpsConfig = new WpsInfo();
            wpsConfig.setup = this.mWpsSetup;
            this.mWifiManager.startWps(wpsConfig, this.mWpsListener);
        }
        super.onCreate(savedInstanceState);
    }

    protected void onStart() {
        this.mTimer = new Timer(false);
        this.mTimer.schedule(new TimerTask() {
            public void run() {
                WpsDialog.this.mHandler.post(new Runnable() {
                    public void run() {
                        WpsDialog.this.mTimeoutBar.incrementProgressBy(1);
                        if (WpsDialog.this.mTimeoutBar.getProgress() == WpsDialog.this.mTimeoutBar.getMax()) {
                            WpsDialog.this.mWifiManager.cancelWps(null);
                            WpsDialog.this.updateDialog(DialogState.WPS_FAILED, WpsDialog.this.mContext.getString(2131624960));
                        }
                    }
                });
            }
        }, 1000, 1000);
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
    }

    protected void onStop() {
        if (this.mDialogState != DialogState.WPS_COMPLETE) {
            this.mWifiManager.cancelWps(null);
        }
        if (this.mReceiver != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
        }
    }

    private void updateDialog(final DialogState state, final String msg) {
        if (this.mDialogState.ordinal() < state.ordinal()) {
            this.mDialogState = state;
            this.mMsgString = msg;
            this.mHandler.post(new Runnable() {
                private static final /* synthetic */ int[] -com-android-settings-wifi-WpsDialog$DialogStateSwitchesValues = null;

                private static /* synthetic */ int[] -getcom-android-settings-wifi-WpsDialog$DialogStateSwitchesValues() {
                    if (-com-android-settings-wifi-WpsDialog$DialogStateSwitchesValues != null) {
                        return -com-android-settings-wifi-WpsDialog$DialogStateSwitchesValues;
                    }
                    int[] iArr = new int[DialogState.values().length];
                    try {
                        iArr[DialogState.CONNECTED.ordinal()] = 1;
                    } catch (NoSuchFieldError e) {
                    }
                    try {
                        iArr[DialogState.WPS_COMPLETE.ordinal()] = 2;
                    } catch (NoSuchFieldError e2) {
                    }
                    try {
                        iArr[DialogState.WPS_FAILED.ordinal()] = 3;
                    } catch (NoSuchFieldError e3) {
                    }
                    try {
                        iArr[DialogState.WPS_INIT.ordinal()] = 4;
                    } catch (NoSuchFieldError e4) {
                    }
                    try {
                        iArr[DialogState.WPS_START.ordinal()] = 5;
                    } catch (NoSuchFieldError e5) {
                    }
                    -com-android-settings-wifi-WpsDialog$DialogStateSwitchesValues = iArr;
                    return iArr;
                }

                public void run() {
                    switch (AnonymousClass4.-getcom-android-settings-wifi-WpsDialog$DialogStateSwitchesValues()[state.ordinal()]) {
                        case 1:
                        case 3:
                            WpsDialog.this.getButton(-1).setText(WpsDialog.this.mContext.getString(2131625656));
                            WpsDialog.this.mTimeoutBar.setVisibility(8);
                            WpsDialog.this.mProgressBar.setVisibility(8);
                            if (WpsDialog.this.mReceiver != null) {
                                WpsDialog.this.mContext.unregisterReceiver(WpsDialog.this.mReceiver);
                                WpsDialog.this.mReceiver = null;
                                break;
                            }
                            break;
                        case 2:
                            WpsDialog.this.mTimeoutBar.setVisibility(8);
                            WpsDialog.this.mProgressBar.setVisibility(0);
                            break;
                    }
                    WpsDialog.this.mTextView.setText(msg);
                }
            });
        }
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.net.wifi.STATE_CHANGE".equals(action)) {
            if (((NetworkInfo) intent.getParcelableExtra("networkInfo")).getDetailedState() == DetailedState.CONNECTED && this.mDialogState == DialogState.WPS_COMPLETE && this.mWifiManager.getConnectionInfo() != null) {
                updateDialog(DialogState.CONNECTED, String.format(this.mContext.getString(2131624958), new Object[]{wifiInfo.getSSID()}));
            }
        } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action) && this.mWifiManager != null && !this.mWifiManager.isWifiEnabled()) {
            updateDialog(DialogState.WPS_FAILED, this.mContext.getString(2131624960));
        }
    }
}
