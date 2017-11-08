package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.util.AsyncChannel;
import com.huawei.cust.HwCustUtils;

public class NetStatusBar extends LinearLayout {
    private boolean mIsRegister = false;
    private PhoneStateListener mPhoneStateListener;
    private BroadcastReceiver mReceiver;
    private int mSimActivity = 0;
    private ImageView mSimConnected;
    private FrameLayout mSimLayout;
    Handler mSimViewHandler = new Handler() {
        public void handleMessage(Message msg) {
            NetStatusBar.this.mSimConnected.setVisibility(0);
            NetStatusBar.this.mSimLayout.setVisibility(0);
            ((HwCustNetStatusBar) HwCustUtils.createObj(HwCustNetStatusBar.class, new Object[0])).hideSimIconLayout(NetStatusBar.this.mSimLayout);
            switch (msg.what) {
                case 0:
                    NetStatusBar.this.mSimConnected.setVisibility(8);
                    return;
                case 1:
                    NetStatusBar.this.mSimConnected.setImageResource(2130838694);
                    return;
                case 2:
                    NetStatusBar.this.mSimConnected.setImageResource(2130838698);
                    return;
                case 3:
                    NetStatusBar.this.mSimConnected.setImageResource(2130838696);
                    return;
                default:
                    return;
            }
        }
    };
    Handler mViewVisibilityHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    NetStatusBar.this.mSimLayout.setVisibility(0);
                    break;
                case 3:
                    NetStatusBar.this.mWifiLayout.setVisibility(0);
                    break;
                case 4:
                    NetStatusBar.this.mSimLayout.setVisibility(8);
                    break;
                case 5:
                    NetStatusBar.this.mWifiLayout.setVisibility(8);
                    break;
            }
            ((HwCustNetStatusBar) HwCustUtils.createObj(HwCustNetStatusBar.class, new Object[0])).hideSimIconLayout(NetStatusBar.this.mSimLayout);
        }
    };
    private int mWifiActivity = 0;
    private AsyncChannel mWifiChannel;
    private ImageView mWifiConnected;
    private FrameLayout mWifiLayout;
    Handler mWifiLengthHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    NetStatusBar.this.mWifisignal.setImageResource(2130838700);
                    return;
                case 1:
                    NetStatusBar.this.mWifisignal.setImageResource(2130838701);
                    return;
                case 2:
                    NetStatusBar.this.mWifisignal.setImageResource(2130838702);
                    return;
                case 3:
                    NetStatusBar.this.mWifisignal.setImageResource(2130838703);
                    return;
                case 4:
                    NetStatusBar.this.mWifisignal.setImageResource(2130838704);
                    return;
                default:
                    return;
            }
        }
    };
    private WifiManager mWifiManager;
    Handler mWifiViewHandler = new Handler() {
        public void handleMessage(Message msg) {
            NetStatusBar.this.mWifiConnected.setVisibility(0);
            if (SettingsExtUtils.isSimCardPresent()) {
                NetStatusBar.this.mSimLayout.setVisibility(0);
            } else {
                NetStatusBar.this.mSimLayout.setVisibility(8);
            }
            ((HwCustNetStatusBar) HwCustUtils.createObj(HwCustNetStatusBar.class, new Object[0])).hideSimIconLayout(NetStatusBar.this.mSimLayout);
            switch (msg.what) {
                case 0:
                    NetStatusBar.this.mWifiConnected.setVisibility(8);
                    return;
                case 1:
                    NetStatusBar.this.mWifiConnected.setImageResource(2130838693);
                    return;
                case 2:
                    NetStatusBar.this.mWifiConnected.setImageResource(2130838697);
                    return;
                case 3:
                    NetStatusBar.this.mWifiConnected.setImageResource(2130838695);
                    return;
                default:
                    return;
            }
        }
    };
    private ImageView mWifisignal;
    private Context netContext;

    class WifiHandler extends Handler {
        WifiHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (msg.arg1 != NetStatusBar.this.mWifiActivity) {
                        NetStatusBar.this.mWifiActivity = msg.arg1;
                        NetStatusBar.this.mWifiViewHandler.sendEmptyMessage(NetStatusBar.this.mWifiActivity);
                        return;
                    }
                    return;
                case 69632:
                    if (msg.arg1 == 0) {
                        NetStatusBar.this.mWifiChannel.sendMessage(Message.obtain(this, 69633));
                        return;
                    } else {
                        Log.e("NetStatusBar", "Failed to connect to wifi");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public NetStatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.netContext = context;
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onDataActivity(int direction) {
                if (direction != NetStatusBar.this.mSimActivity) {
                    NetStatusBar.this.mSimActivity = direction;
                    NetStatusBar.this.mSimViewHandler.sendEmptyMessage(NetStatusBar.this.mSimActivity);
                }
            }
        };
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 128);
        createWifiHandler();
    }

    private void createWifiHandler() {
        this.mWifiManager = (WifiManager) this.netContext.getSystemService("wifi");
        Handler handler = new WifiHandler();
        this.mWifiChannel = new AsyncChannel();
        Messenger wifiMessenger = this.mWifiManager.getWifiServiceMessenger();
        if (wifiMessenger != null) {
            this.mWifiChannel.connect(this.netContext, handler, wifiMessenger);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterBroadcast();
        ((TelephonyManager) this.netContext.getSystemService("phone")).listen(this.mPhoneStateListener, 0);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSimLayout = (FrameLayout) findViewById(2131887210);
        this.mWifiLayout = (FrameLayout) findViewById(2131887207);
        this.mSimConnected = (ImageView) findViewById(2131887211);
        this.mWifiConnected = (ImageView) findViewById(2131887208);
        this.mWifisignal = (ImageView) findViewById(2131887209);
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (info == null || !info.isConnected()) {
                            NetStatusBar.this.mViewVisibilityHandler.sendEmptyMessage(5);
                        } else {
                            NetStatusBar.this.mViewVisibilityHandler.sendEmptyMessage(3);
                        }
                    } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction())) {
                        NetStatusBar.this.setWifiLength(NetStatusBar.this.mWifiManager.getConnectionInfo().getRssi());
                    } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
                        if (tm != null) {
                            if (5 == tm.getSimState()) {
                                NetStatusBar.this.mViewVisibilityHandler.sendEmptyMessage(2);
                            } else {
                                NetStatusBar.this.mViewVisibilityHandler.sendEmptyMessage(4);
                            }
                        }
                    }
                }
            }
        };
        if (SettingsExtUtils.isSimCardPresent()) {
            this.mSimLayout.setVisibility(0);
            ((HwCustNetStatusBar) HwCustUtils.createObj(HwCustNetStatusBar.class, new Object[0])).hideSimIconLayout(this.mSimLayout);
        } else {
            this.mSimLayout.setVisibility(8);
        }
        registerBroadcast();
    }

    protected void setWifiLength(int mRssi) {
        if (mRssi <= 0 && mRssi >= -60) {
            this.mWifiLengthHandler.sendEmptyMessage(4);
        } else if (mRssi < -60 && mRssi >= -80) {
            this.mWifiLengthHandler.sendEmptyMessage(3);
        } else if (mRssi < -80 && mRssi >= -95) {
            this.mWifiLengthHandler.sendEmptyMessage(2);
        } else if (mRssi >= -95 || mRssi < -100) {
            this.mWifiLengthHandler.sendEmptyMessage(0);
        } else {
            this.mWifiLengthHandler.sendEmptyMessage(1);
        }
    }

    private void registerBroadcast() {
        if (!this.mIsRegister) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.wifi.STATE_CHANGE");
            filter.addAction("android.net.wifi.RSSI_CHANGED");
            this.netContext.registerReceiver(this.mReceiver, filter);
            this.mIsRegister = true;
        }
    }

    private void unregisterBroadcast() {
        if (this.mIsRegister) {
            this.netContext.unregisterReceiver(this.mReceiver);
            this.mIsRegister = false;
        }
    }
}
