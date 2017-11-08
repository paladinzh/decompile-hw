package com.android.settings.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settingslib.wifi.AccessPoint;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class WifiStatusTest extends Activity {
    private TextView mBSSID;
    private TextView mHiddenSSID;
    private TextView mHttpClientTest;
    private String mHttpClientTestResult;
    private TextView mIPAddr;
    private TextView mLinkSpeed;
    private TextView mMACAddr;
    private TextView mNetworkId;
    private TextView mNetworkState;
    OnClickListener mPingButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            WifiStatusTest.this.updatePingState();
        }
    };
    private TextView mPingHostname;
    private String mPingHostnameResult;
    private TextView mRSSI;
    private TextView mSSID;
    private TextView mScanList;
    private TextView mSupplicantState;
    private WifiManager mWifiManager;
    private TextView mWifiState;
    private IntentFilter mWifiStateFilter;
    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                WifiStatusTest.this.handleWifiStateChanged(intent.getIntExtra("wifi_state", 4));
            } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                WifiStatusTest.this.handleNetworkStateChanged((NetworkInfo) intent.getParcelableExtra("networkInfo"));
            } else if (intent.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
                WifiStatusTest.this.handleScanResultsAvailable();
            } else if (!intent.getAction().equals("android.net.wifi.supplicant.CONNECTION_CHANGE")) {
                if (intent.getAction().equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                    WifiStatusTest.this.handleSupplicantStateChanged((SupplicantState) intent.getParcelableExtra("newState"), intent.hasExtra("supplicantError"), intent.getIntExtra("supplicantError", 0));
                } else if (intent.getAction().equals("android.net.wifi.RSSI_CHANGED")) {
                    WifiStatusTest.this.handleSignalChanged(intent.getIntExtra("newRssi", 0));
                } else if (!intent.getAction().equals("android.net.wifi.NETWORK_IDS_CHANGED")) {
                    Log.e("WifiStatusTest", "Received an unknown Wifi Intent");
                }
            }
        }
    };
    private Button pingTestButton;
    private Button updateButton;
    OnClickListener updateButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            WifiInfo wifiInfo = WifiStatusTest.this.mWifiManager.getConnectionInfo();
            WifiStatusTest.this.setWifiStateText(WifiStatusTest.this.mWifiManager.getWifiState());
            WifiStatusTest.this.mBSSID.setText(wifiInfo.getBSSID());
            WifiStatusTest.this.mHiddenSSID.setText(String.valueOf(wifiInfo.getHiddenSSID()));
            int ipAddr = wifiInfo.getIpAddress();
            StringBuffer ipBuf = new StringBuffer();
            ipAddr >>>= 8;
            ipAddr >>>= 8;
            ipBuf.append(ipAddr & 255).append('.').append(ipAddr & 255).append('.').append(ipAddr & 255).append('.').append((ipAddr >>> 8) & 255);
            WifiStatusTest.this.mIPAddr.setText(ipBuf);
            WifiStatusTest.this.mLinkSpeed.setText(String.valueOf(wifiInfo.getLinkSpeed()) + " Mbps");
            WifiStatusTest.this.mMACAddr.setText(wifiInfo.getMacAddress());
            WifiStatusTest.this.mNetworkId.setText(String.valueOf(wifiInfo.getNetworkId()));
            WifiStatusTest.this.mRSSI.setText(String.valueOf(wifiInfo.getRssi()));
            WifiStatusTest.this.mSSID.setText(wifiInfo.getSSID());
            WifiStatusTest.this.setSupplicantStateText(wifiInfo.getSupplicantState());
        }
    };

    private void httpClientTest() {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r6 = this;
        r3 = 0;
        r2 = new java.net.URL;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r4 = "https://www.google.com";	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r2.<init>(r4);	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r4 = r2.openConnection();	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r0 = r4;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r0 = (java.net.HttpURLConnection) r0;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r3 = r0;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r4 = r3.getResponseCode();	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r5 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        if (r4 != r5) goto L_0x0024;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
    L_0x0019:
        r4 = "Pass";	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r6.mHttpClientTestResult = r4;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
    L_0x001e:
        if (r3 == 0) goto L_0x0023;
    L_0x0020:
        r3.disconnect();
    L_0x0023:
        return;
    L_0x0024:
        r4 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r4.<init>();	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r5 = "Fail: Code: ";	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r4 = r4.append(r5);	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r5 = r3.getResponseMessage();	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r4 = r4.append(r5);	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r4 = r4.toString();	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r6.mHttpClientTestResult = r4;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        goto L_0x001e;
    L_0x003f:
        r1 = move-exception;
        r4 = "Fail: IOException";	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        r6.mHttpClientTestResult = r4;	 Catch:{ IOException -> 0x003f, all -> 0x004b }
        if (r3 == 0) goto L_0x0023;
    L_0x0047:
        r3.disconnect();
        goto L_0x0023;
    L_0x004b:
        r4 = move-exception;
        if (r3 == 0) goto L_0x0051;
    L_0x004e:
        r3.disconnect();
    L_0x0051:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.wifi.WifiStatusTest.httpClientTest():void");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getApplicationContext().getSystemService("wifi");
        this.mWifiStateFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mWifiStateFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mWifiStateFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mWifiStateFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mWifiStateFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mWifiStateFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(this.mWifiStateReceiver, this.mWifiStateFilter);
        setContentView(2130969285);
        this.updateButton = (Button) findViewById(2131887581);
        this.updateButton.setOnClickListener(this.updateButtonHandler);
        this.mWifiState = (TextView) findViewById(2131887582);
        this.mNetworkState = (TextView) findViewById(2131887583);
        this.mSupplicantState = (TextView) findViewById(2131887584);
        this.mRSSI = (TextView) findViewById(2131887585);
        this.mBSSID = (TextView) findViewById(2131887586);
        this.mSSID = (TextView) findViewById(2131887459);
        this.mHiddenSSID = (TextView) findViewById(2131887587);
        this.mIPAddr = (TextView) findViewById(2131887588);
        this.mMACAddr = (TextView) findViewById(2131887590);
        this.mNetworkId = (TextView) findViewById(2131887591);
        this.mLinkSpeed = (TextView) findViewById(2131887592);
        this.mScanList = (TextView) findViewById(2131887593);
        this.mPingHostname = (TextView) findViewById(2131887596);
        this.mHttpClientTest = (TextView) findViewById(2131887043);
        this.pingTestButton = (Button) findViewById(2131887037);
        this.pingTestButton.setOnClickListener(this.mPingButtonHandler);
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(this.mWifiStateReceiver, this.mWifiStateFilter);
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.mWifiStateReceiver);
    }

    private void setSupplicantStateText(SupplicantState supplicantState) {
        if (SupplicantState.FOUR_WAY_HANDSHAKE.equals(supplicantState)) {
            this.mSupplicantState.setText("FOUR WAY HANDSHAKE");
        } else if (SupplicantState.ASSOCIATED.equals(supplicantState)) {
            this.mSupplicantState.setText("ASSOCIATED");
        } else if (SupplicantState.ASSOCIATING.equals(supplicantState)) {
            this.mSupplicantState.setText("ASSOCIATING");
        } else if (SupplicantState.COMPLETED.equals(supplicantState)) {
            this.mSupplicantState.setText("COMPLETED");
        } else if (SupplicantState.DISCONNECTED.equals(supplicantState)) {
            this.mSupplicantState.setText("DISCONNECTED");
        } else if (SupplicantState.DORMANT.equals(supplicantState)) {
            this.mSupplicantState.setText("DORMANT");
        } else if (SupplicantState.GROUP_HANDSHAKE.equals(supplicantState)) {
            this.mSupplicantState.setText("GROUP HANDSHAKE");
        } else if (SupplicantState.INACTIVE.equals(supplicantState)) {
            this.mSupplicantState.setText("INACTIVE");
        } else if (SupplicantState.INVALID.equals(supplicantState)) {
            this.mSupplicantState.setText("INVALID");
        } else if (SupplicantState.SCANNING.equals(supplicantState)) {
            this.mSupplicantState.setText("SCANNING");
        } else if (SupplicantState.UNINITIALIZED.equals(supplicantState)) {
            this.mSupplicantState.setText("UNINITIALIZED");
        } else {
            this.mSupplicantState.setText("BAD");
            Log.e("WifiStatusTest", "supplicant state is bad");
        }
    }

    private void setWifiStateText(int wifiState) {
        String wifiStateString;
        switch (wifiState) {
            case 0:
                wifiStateString = getString(2131625086);
                break;
            case 1:
                wifiStateString = getString(2131625087);
                break;
            case 2:
                wifiStateString = getString(2131625088);
                break;
            case 3:
                wifiStateString = getString(2131625089);
                break;
            case 4:
                wifiStateString = getString(2131625090);
                break;
            default:
                wifiStateString = "BAD";
                Log.e("WifiStatusTest", "wifi state is bad");
                break;
        }
        this.mWifiState.setText(wifiStateString);
    }

    private void handleSignalChanged(int rssi) {
        this.mRSSI.setText(String.valueOf(rssi));
    }

    private void handleWifiStateChanged(int wifiState) {
        setWifiStateText(wifiState);
    }

    private void handleScanResultsAvailable() {
        List<ScanResult> list = this.mWifiManager.getScanResults();
        StringBuffer scanList = new StringBuffer();
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                ScanResult scanResult = (ScanResult) list.get(i);
                if (!(scanResult == null || TextUtils.isEmpty(scanResult.SSID))) {
                    scanList.append(scanResult.SSID + " ");
                }
            }
        }
        this.mScanList.setText(scanList);
    }

    private void handleSupplicantStateChanged(SupplicantState state, boolean hasError, int error) {
        if (hasError) {
            this.mSupplicantState.setText("ERROR AUTHENTICATING");
        } else {
            setSupplicantStateText(state);
        }
    }

    private void handleNetworkStateChanged(NetworkInfo networkInfo) {
        if (this.mWifiManager.isWifiEnabled()) {
            WifiInfo info = this.mWifiManager.getConnectionInfo();
            this.mNetworkState.setText(AccessPoint.getSummary(this, info.getSSID(), networkInfo.getDetailedState(), info.getNetworkId() == -1, null));
        }
    }

    private final void updatePingState() {
        final Handler handler = new Handler();
        this.mPingHostnameResult = getResources().getString(2131624395);
        this.mHttpClientTestResult = getResources().getString(2131624395);
        this.mPingHostname.setText(this.mPingHostnameResult);
        this.mHttpClientTest.setText(this.mHttpClientTestResult);
        final Runnable updatePingResults = new Runnable() {
            public void run() {
                WifiStatusTest.this.mPingHostname.setText(WifiStatusTest.this.mPingHostnameResult);
                WifiStatusTest.this.mHttpClientTest.setText(WifiStatusTest.this.mHttpClientTestResult);
            }
        };
        new Thread() {
            public void run() {
                WifiStatusTest.this.pingHostname();
                handler.post(updatePingResults);
            }
        }.start();
        new Thread() {
            public void run() {
                WifiStatusTest.this.httpClientTest();
                handler.post(updatePingResults);
            }
        }.start();
    }

    private final void pingHostname() {
        try {
            if (Runtime.getRuntime().exec("ping -c 1 -w 100 www.google.com").waitFor() == 0) {
                this.mPingHostnameResult = "Pass";
            } else {
                this.mPingHostnameResult = "Fail: Host unreachable";
            }
        } catch (UnknownHostException e) {
            this.mPingHostnameResult = "Fail: Unknown Host";
        } catch (IOException e2) {
            this.mPingHostnameResult = "Fail: IOException";
        } catch (InterruptedException e3) {
            this.mPingHostnameResult = "Fail: InterruptedException";
        }
    }
}
