package com.android.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.DataConnectionRealTimeInfo;
import android.telephony.MSimTelephonyManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseCallState;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settings.deviceinfo.HwCustStatusImpl;
import com.huawei.android.telephony.PhoneStateListenerEx;
import com.huawei.cust.HwCustUtils;
import java.io.IOException;
import java.util.List;

public class RadioInfo extends Activity {
    private static final boolean ISMULTISIMENABLED = Utils.isMultiSimEnabled();
    private static final String[] mCellInfoRefreshRateLabels = new String[]{"Disabled", "Immediate", "Min 5s", "Min 10s", "Min 60s"};
    private static final int[] mCellInfoRefreshRates = new int[]{HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID, 0, 5000, 10000, 60000};
    private static final String[] mPreferredNetworkLabels = new String[]{"WCDMA preferred", "GSM only", "WCDMA only", "GSM auto (PRL)", "CDMA auto (PRL)", "CDMA only", "EvDo only", "Global auto (PRL)", "LTE/CDMA auto (PRL)", "LTE/UMTS auto (PRL)", "LTE/CDMA/UMTS auto (PRL)", "LTE only", "LTE/WCDMA", "TD-SCDMA only", "TD-SCDMA/WCDMA", "LTE/TD-SCDMA", "TD-SCDMA/GSM", "TD-SCDMA/UMTS", "LTE/TD-SCDMA/WCDMA", "LTE/TD-SCDMA/UMTS", "TD-SCDMA/CDMA/UMTS", "Global/TD-SCDMA", "Unknown"};
    private TextView callState;
    private TextView dBm;
    private TextView dataNetwork;
    private TextView dnsCheckState;
    private Button dnsCheckToggleButton;
    private TextView gprsState;
    private TextView gsmState;
    private TextView mCellInfo;
    private int mCellInfoRefreshRateIndex;
    private List<CellInfo> mCellInfoResult = null;
    private CellLocation mCellLocationResult = null;
    private TextView mCfi;
    private boolean mCfiValue = false;
    private HwCustRadioInfo mCustRadioInfo;
    private TextView mDcRtInfoTv;
    private TextView mDeviceId;
    OnClickListener mDnsCheckButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.phone.disableDnsCheck(!RadioInfo.this.phone.isDnsCheckDisabled());
            RadioInfo.this.updateDnsCheckState();
        }
    };
    private OnMenuItemClickListener mGetPdpList = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            RadioInfo.this.phone.getDataCallList(null);
            return true;
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case 1000:
                    ar = msg.obj;
                    if (ar.exception != null || ar.result == null) {
                        RadioInfo.this.updatePreferredNetworkType(RadioInfo.mPreferredNetworkLabels.length - 1);
                        return;
                    } else {
                        RadioInfo.this.updatePreferredNetworkType(((int[]) ar.result)[0]);
                        return;
                    }
                case 1001:
                    if (((AsyncResult) msg.obj).exception != null) {
                        RadioInfo.this.log("Set preferred network type failed.");
                        return;
                    }
                    return;
                case 1005:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        RadioInfo.this.smsc.setText("refresh error");
                        return;
                    } else {
                        RadioInfo.this.smsc.setText((String) ar.result);
                        return;
                    }
                case 1006:
                    RadioInfo.this.updateSmscButton.setEnabled(true);
                    if (((AsyncResult) msg.obj).exception != null) {
                        RadioInfo.this.smsc.setText("update error");
                        return;
                    }
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    private TextView mHttpClientTest;
    private String mHttpClientTestResult;
    private ImsManager mImsManager = null;
    private TextView mLocation;
    private TextView mMwi;
    private boolean mMwiValue = false;
    private List<NeighboringCellInfo> mNeighboringCellResult = null;
    private TextView mNeighboringCids;
    OnClickListener mOemInfoButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            try {
                RadioInfo.this.startActivity(new Intent("com.android.settings.OEM_RADIO_INFO"));
            } catch (ActivityNotFoundException ex) {
                RadioInfo.this.log("OEM-specific Info/Settings Activity Not Found : " + ex);
            }
        }
    };
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onDataConnectionStateChanged(int state) {
            RadioInfo.this.updateDataState();
            RadioInfo.this.updateNetworkType();
        }

        public void onDataActivity(int direction) {
            RadioInfo.this.updateDataStats2();
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            RadioInfo.this.updateNetworkType();
            RadioInfo.this.updatePhoneState(state);
        }

        public void onPreciseCallStateChanged(PreciseCallState preciseState) {
            RadioInfo.this.updateNetworkType();
        }

        public void onCellLocationChanged(CellLocation location) {
            RadioInfo.this.updateLocation(location);
        }

        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            RadioInfo.this.mMwiValue = mwi;
            RadioInfo.this.updateMessageWaiting();
        }

        public void onCallForwardingIndicatorChanged(boolean cfi) {
            RadioInfo.this.mCfiValue = cfi;
            RadioInfo.this.updateCallRedirect();
        }

        public void onCellInfoChanged(List<CellInfo> arrayCi) {
            RadioInfo.this.log("onCellInfoChanged: arrayCi=" + arrayCi);
            RadioInfo.this.mCellInfoResult = arrayCi;
            RadioInfo.this.updateCellInfo(RadioInfo.this.mCellInfoResult);
        }

        public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) {
            RadioInfo.this.log("onDataConnectionRealTimeInfoChanged: dcRtInfo=" + dcRtInfo);
            RadioInfo.this.updateDcRtInfoTv(dcRtInfo);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            RadioInfo.this.log("onSignalStrengthChanged: SignalStrength=" + signalStrength);
            RadioInfo.this.updateSignalStrength(signalStrength);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            RadioInfo.this.log("onServiceStateChanged: ServiceState=" + serviceState);
            RadioInfo.this.updateServiceState(serviceState);
            RadioInfo.this.updateRadioPowerState();
            RadioInfo.this.updateNetworkType();
        }
    };
    OnClickListener mPingButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.updatePingState();
        }
    };
    private String mPingHostnameResultV4;
    private String mPingHostnameResultV6;
    private TextView mPingHostnameV4;
    private TextView mPingHostnameV6;
    OnItemSelectedListener mPreferredNetworkHandler = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int pos, long id) {
            if (RadioInfo.this.mPreferredNetworkTypeResult != pos && pos >= 0 && pos <= RadioInfo.mPreferredNetworkLabels.length - 2) {
                RadioInfo.this.mPreferredNetworkTypeResult = pos;
                RadioInfo.this.phone.setPreferredNetworkType(RadioInfo.this.mPreferredNetworkTypeResult, RadioInfo.this.mHandler.obtainMessage(1001));
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    };
    private int mPreferredNetworkTypeResult;
    OnCheckedChangeListener mRadioPowerOnChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RadioInfo.this.log("toggle radio power: currently " + (RadioInfo.this.isRadioOn() ? "on" : "off"));
            RadioInfo.this.phone.setRadioPower(isChecked);
        }
    };
    OnClickListener mRefreshSmscButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.refreshSmsc();
        }
    };
    private TelephonyManager mTelephonyManager;
    private OnMenuItemClickListener mToggleData = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            switch (RadioInfo.this.mTelephonyManager.getDataState()) {
                case 0:
                    RadioInfo.this.phone.setDataEnabled(true);
                    break;
                case 2:
                    RadioInfo.this.phone.setDataEnabled(false);
                    break;
            }
            return true;
        }
    };
    OnClickListener mUpdateSmscButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.updateSmscButton.setEnabled(false);
            RadioInfo.this.phone.setSmscAddress(RadioInfo.this.smsc.getText().toString(), RadioInfo.this.mHandler.obtainMessage(1006));
        }
    };
    private OnMenuItemClickListener mViewADNCallback = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setClassName("com.android.phone", "com.android.phone.SimContacts");
            RadioInfo.this.startActivity(intent);
            return true;
        }
    };
    private OnMenuItemClickListener mViewFDNCallback = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent("android.intent.action.VIEW");
            if (VERSION.SDK_INT > 21) {
                intent.setClassName("com.android.phone", "com.android.phone.settings.fdn.FdnList");
            } else {
                intent.setClassName("com.android.phone", "com.android.phone.FdnList");
            }
            RadioInfo.this.startActivity(intent);
            return true;
        }
    };
    private OnMenuItemClickListener mViewSDNCallback = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("content://icc/sdn"));
            intent.setClassName("com.android.phone", "com.android.phone.ADNList");
            RadioInfo.this.startActivity(intent);
            return true;
        }
    };
    private TextView number;
    private Button oemInfoButton;
    private TextView operatorName;
    private Phone phone = null;
    private Button pingTestButton;
    private Spinner preferredNetworkType;
    private Switch radioPowerOnSwitch;
    private TextView received;
    private Button refreshSmscButton;
    private TextView roamingState;
    private TextView sent;
    private EditText smsc;
    private int subscription = 0;
    private Button updateSmscButton;
    private TextView voiceNetwork;

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
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.RadioInfo.httpClientTest():void");
    }

    private void log(String s) {
        Log.d("RadioInfo", s);
    }

    private void updatePreferredNetworkType(int type) {
        if (type >= mPreferredNetworkLabels.length || type < 0) {
            log("EVENT_QUERY_PREFERRED_TYPE_DONE: unknown type=" + type);
            type = mPreferredNetworkLabels.length - 1;
        }
        this.mPreferredNetworkTypeResult = type;
        this.preferredNetworkType.setSelection(this.mPreferredNetworkTypeResult, true);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(2130969043);
        log("Started onCreate");
        try {
            this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
            this.mCustRadioInfo = (HwCustRadioInfo) HwCustUtils.createObj(HwCustRadioInfo.class, new Object[]{this});
            if (ISMULTISIMENABLED) {
                this.subscription = getIntent().getIntExtra("SUBSCRIPTION_ID", 0);
                this.phone = PhoneFactory.getPhone(this.subscription);
                createPhoneListener(this.subscription);
            } else {
                this.phone = PhoneFactory.getDefaultPhone();
            }
            this.mImsManager = ImsManager.getInstance(getApplicationContext(), SubscriptionManager.getDefaultVoicePhoneId());
            this.mDeviceId = (TextView) findViewById(2131887027);
            this.number = (TextView) findViewById(2131886804);
            this.callState = (TextView) findViewById(2131887034);
            this.operatorName = (TextView) findViewById(2131887028);
            this.roamingState = (TextView) findViewById(2131887035);
            this.gsmState = (TextView) findViewById(2131887030);
            this.gprsState = (TextView) findViewById(2131887031);
            this.voiceNetwork = (TextView) findViewById(2131887032);
            this.dataNetwork = (TextView) findViewById(2131887033);
            this.dBm = (TextView) findViewById(2131887029);
            this.mMwi = (TextView) findViewById(2131887046);
            this.mCfi = (TextView) findViewById(2131887048);
            this.mLocation = (TextView) findViewById(2131887064);
            this.mNeighboringCids = (TextView) findViewById(2131887065);
            this.mCellInfo = (TextView) findViewById(2131887066);
            this.mCellInfo.setTypeface(Typeface.MONOSPACE);
            this.mDcRtInfoTv = (TextView) findViewById(2131887044);
            this.sent = (TextView) findViewById(2131887050);
            this.received = (TextView) findViewById(2131887052);
            this.smsc = (EditText) findViewById(2131887060);
            this.dnsCheckState = (TextView) findViewById(2131887062);
            this.mPingHostnameV4 = (TextView) findViewById(2131887040);
            this.mPingHostnameV6 = (TextView) findViewById(2131887041);
            this.mHttpClientTest = (TextView) findViewById(2131887043);
            this.preferredNetworkType = (Spinner) findViewById(2131887036);
            ArrayAdapter<String> adapter = new ArrayAdapter(this, 17367048, mPreferredNetworkLabels);
            adapter.setDropDownViewResource(17367049);
            this.preferredNetworkType.setAdapter(adapter);
            this.radioPowerOnSwitch = (Switch) findViewById(2131887055);
            this.pingTestButton = (Button) findViewById(2131887037);
            this.pingTestButton.setOnClickListener(this.mPingButtonHandler);
            this.updateSmscButton = (Button) findViewById(2131887058);
            this.updateSmscButton.setOnClickListener(this.mUpdateSmscButtonHandler);
            this.refreshSmscButton = (Button) findViewById(2131887059);
            this.refreshSmscButton.setOnClickListener(this.mRefreshSmscButtonHandler);
            this.dnsCheckToggleButton = (Button) findViewById(2131887061);
            this.dnsCheckToggleButton.setOnClickListener(this.mDnsCheckButtonHandler);
            this.oemInfoButton = (Button) findViewById(2131887063);
            this.oemInfoButton.setOnClickListener(this.mOemInfoButtonHandler);
            if (getPackageManager().queryIntentActivities(new Intent("com.android.settings.OEM_RADIO_INFO"), 0).size() == 0) {
                this.oemInfoButton.setEnabled(false);
            }
            this.mCellInfoRefreshRateIndex = 0;
            this.mPreferredNetworkTypeResult = mPreferredNetworkLabels.length - 1;
            this.phone.getPreferredNetworkType(this.mHandler.obtainMessage(1000));
            restoreFromBundle(icicle);
        } catch (RuntimeException e) {
            finish();
            Log.d("RadioInfo", "RadioInfo Catch RuntimeException!");
        }
    }

    protected void onResume() {
        super.onResume();
        log("Started onResume");
        updateMessageWaiting();
        updateCallRedirect();
        updateDataState();
        updateDataStats2();
        updateRadioPowerState();
        updateProperties();
        updateDnsCheckState();
        if (this.mCustRadioInfo != null) {
            this.mCustRadioInfo.hideUselessMenu(this);
        }
        updateNetworkType();
        updateNeighboringCids(this.mNeighboringCellResult);
        updateLocation(this.mCellLocationResult);
        updateCellInfo(this.mCellInfoResult);
        this.mPingHostnameV4.setText(this.mPingHostnameResultV4);
        this.mPingHostnameV6.setText(this.mPingHostnameResultV6);
        this.mHttpClientTest.setText(this.mHttpClientTestResult);
        this.preferredNetworkType.setSelection(this.mPreferredNetworkTypeResult, true);
        this.preferredNetworkType.setOnItemSelectedListener(this.mPreferredNetworkHandler);
        this.radioPowerOnSwitch.setOnCheckedChangeListener(this.mRadioPowerOnChangeListener);
        this.mTelephonyManager.listen(this.mPhoneStateListener, 9725);
        this.smsc.clearFocus();
    }

    protected void onPause() {
        super.onPause();
        log("onPause: unregister phone & data intents");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        this.phone.setCellInfoListRate(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID);
    }

    private void restoreFromBundle(Bundle b) {
        if (b != null) {
            this.mPingHostnameResultV4 = b.getString("mPingHostnameResultV4", "");
            this.mPingHostnameResultV6 = b.getString("mPingHostnameResultV6", "");
            this.mHttpClientTestResult = b.getString("mHttpClientTestResult", "");
            this.mPingHostnameV4.setText(this.mPingHostnameResultV4);
            this.mPingHostnameV6.setText(this.mPingHostnameResultV6);
            this.mHttpClientTest.setText(this.mHttpClientTestResult);
            this.mPreferredNetworkTypeResult = b.getInt("mPreferredNetworkTypeResult", mPreferredNetworkLabels.length - 1);
            this.mCellInfoRefreshRateIndex = b.getInt("mCellInfoRefreshRateIndex", 0);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("mPingHostnameResultV4", this.mPingHostnameResultV4);
        outState.putString("mPingHostnameResultV6", this.mPingHostnameResultV6);
        outState.putString("mHttpClientTestResult", this.mHttpClientTestResult);
        outState.putInt("mPreferredNetworkTypeResult", this.mPreferredNetworkTypeResult);
        outState.putInt("mCellInfoRefreshRateIndex", this.mCellInfoRefreshRateIndex);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 0, 2131624378).setOnMenuItemClickListener(this.mViewADNCallback);
        menu.add(1, 2, 0, 2131624379).setOnMenuItemClickListener(this.mViewFDNCallback);
        menu.add(1, 3, 0, 2131624380).setOnMenuItemClickListener(this.mViewSDNCallback);
        menu.add(1, 4, 0, 2131624381).setOnMenuItemClickListener(this.mGetPdpList);
        menu.add(1, 5, 0, 2131624375).setOnMenuItemClickListener(this.mToggleData);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(5);
        boolean visible = true;
        switch (this.mTelephonyManager.getDataState()) {
            case 0:
                item.setTitle(2131624374);
                break;
            case 2:
            case 3:
                item.setTitle(2131624375);
                break;
            default:
                visible = false;
                break;
        }
        item.setVisible(visible);
        return true;
    }

    private void updateDnsCheckState() {
        this.dnsCheckState.setText(this.phone.isDnsCheckDisabled() ? "0.0.0.0 allowed" : "0.0.0.0 not allowed");
    }

    private final void updateSignalStrength(SignalStrength signalStrength) {
        Resources r = getResources();
        int signalDbm = signalStrength.getDbm();
        if (ISMULTISIMENABLED) {
            signalDbm = SettingsExtUtils.getMultiSimSignalDbm(signalStrength, this);
        }
        if (-1 == signalDbm) {
            signalDbm = 0;
        }
        int signalAsu = signalStrength.getAsuLevel();
        if (ISMULTISIMENABLED) {
            signalAsu = SettingsExtUtils.getMultiSimSignalAsu(signalStrength, this);
        }
        if (-1 == signalAsu) {
            signalAsu = 0;
        }
        this.dBm.setText(String.valueOf(signalDbm) + " " + r.getString(2131624398) + "   " + String.valueOf(signalAsu) + " " + r.getString(2131624399));
    }

    private final void updateLocation(CellLocation location) {
        Resources r = getResources();
        if (location instanceof GsmCellLocation) {
            GsmCellLocation loc = (GsmCellLocation) location;
            int lac = loc.getLac();
            int cid = loc.getCid();
            this.mLocation.setText(r.getString(2131624400) + " = " + (lac == -1 ? HwCustStatusImpl.SUMMARY_UNKNOWN : Integer.toHexString(lac)) + "   " + r.getString(2131624401) + " = " + (cid == -1 ? HwCustStatusImpl.SUMMARY_UNKNOWN : Integer.toHexString(cid)));
        } else if (location instanceof CdmaCellLocation) {
            String str;
            CdmaCellLocation loc2 = (CdmaCellLocation) location;
            int bid = loc2.getBaseStationId();
            int sid = loc2.getSystemId();
            int nid = loc2.getNetworkId();
            int lat = loc2.getBaseStationLatitude();
            int lon = loc2.getBaseStationLongitude();
            TextView textView = this.mLocation;
            StringBuilder append = new StringBuilder().append("BID = ").append(bid == -1 ? HwCustStatusImpl.SUMMARY_UNKNOWN : Integer.toHexString(bid)).append("   ").append("SID = ").append(sid == -1 ? HwCustStatusImpl.SUMMARY_UNKNOWN : Integer.toHexString(sid)).append("   ").append("NID = ");
            if (nid == -1) {
                str = HwCustStatusImpl.SUMMARY_UNKNOWN;
            } else {
                str = Integer.toHexString(nid);
            }
            append = append.append(str).append("\n").append("LAT = ");
            if (lat == -1) {
                str = HwCustStatusImpl.SUMMARY_UNKNOWN;
            } else {
                str = Integer.toHexString(lat);
            }
            append = append.append(str).append("   ").append("LONG = ");
            if (lon == -1) {
                str = HwCustStatusImpl.SUMMARY_UNKNOWN;
            } else {
                str = Integer.toHexString(lon);
            }
            textView.setText(append.append(str).toString());
        } else {
            this.mLocation.setText(HwCustStatusImpl.SUMMARY_UNKNOWN);
        }
    }

    private final void updateNeighboringCids(List<NeighboringCellInfo> cids) {
        StringBuilder sb = new StringBuilder();
        if (cids == null) {
            sb.append(HwCustStatusImpl.SUMMARY_UNKNOWN);
        } else if (cids.isEmpty()) {
            sb.append("no neighboring cells");
        } else {
            for (NeighboringCellInfo cell : cids) {
                sb.append(cell.toString()).append(" ");
            }
        }
        this.mNeighboringCids.setText(sb.toString());
    }

    private final String getCellInfoDisplayString(int i) {
        return i != HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID ? Integer.toString(i) : "";
    }

    private final String buildCdmaInfoString(CellInfoCdma ci) {
        CellIdentityCdma cidCdma = ci.getCellIdentity();
        CellSignalStrengthCdma ssCdma = ci.getCellSignalStrength();
        String str = "%-3.3s %-5.5s %-5.5s %-5.5s %-6.6s %-6.6s %-6.6s %-6.6s %-5.5s";
        Object[] objArr = new Object[9];
        objArr[0] = ci.isRegistered() ? "S  " : "   ";
        objArr[1] = getCellInfoDisplayString(cidCdma.getSystemId());
        objArr[2] = getCellInfoDisplayString(cidCdma.getNetworkId());
        objArr[3] = getCellInfoDisplayString(cidCdma.getBasestationId());
        objArr[4] = getCellInfoDisplayString(ssCdma.getCdmaDbm());
        objArr[5] = getCellInfoDisplayString(ssCdma.getCdmaEcio());
        objArr[6] = getCellInfoDisplayString(ssCdma.getEvdoDbm());
        objArr[7] = getCellInfoDisplayString(ssCdma.getEvdoEcio());
        objArr[8] = getCellInfoDisplayString(ssCdma.getEvdoSnr());
        return String.format(str, objArr);
    }

    private final String buildGsmInfoString(CellInfoGsm ci) {
        CellIdentityGsm cidGsm = ci.getCellIdentity();
        CellSignalStrengthGsm ssGsm = ci.getCellSignalStrength();
        String str = "%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-4.4s %-4.4s\n";
        Object[] objArr = new Object[8];
        objArr[0] = ci.isRegistered() ? "S  " : "   ";
        objArr[1] = getCellInfoDisplayString(cidGsm.getMcc());
        objArr[2] = getCellInfoDisplayString(cidGsm.getMnc());
        objArr[3] = getCellInfoDisplayString(cidGsm.getLac());
        objArr[4] = getCellInfoDisplayString(cidGsm.getCid());
        objArr[5] = getCellInfoDisplayString(cidGsm.getArfcn());
        objArr[6] = getCellInfoDisplayString(cidGsm.getBsic());
        objArr[7] = getCellInfoDisplayString(ssGsm.getDbm());
        return String.format(str, objArr);
    }

    private final String buildLteInfoString(CellInfoLte ci) {
        CellIdentityLte cidLte = ci.getCellIdentity();
        CellSignalStrengthLte ssLte = ci.getCellSignalStrength();
        String str = "%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-3.3s %-6.6s %-4.4s %-4.4s %-2.2s\n";
        Object[] objArr = new Object[10];
        objArr[0] = ci.isRegistered() ? "S  " : "   ";
        objArr[1] = getCellInfoDisplayString(cidLte.getMcc());
        objArr[2] = getCellInfoDisplayString(cidLte.getMnc());
        objArr[3] = getCellInfoDisplayString(cidLte.getTac());
        objArr[4] = getCellInfoDisplayString(cidLte.getCi());
        objArr[5] = getCellInfoDisplayString(cidLte.getPci());
        objArr[6] = getCellInfoDisplayString(cidLte.getEarfcn());
        objArr[7] = getCellInfoDisplayString(ssLte.getDbm());
        objArr[8] = getCellInfoDisplayString(ssLte.getRsrq());
        objArr[9] = getCellInfoDisplayString(ssLte.getTimingAdvance());
        return String.format(str, objArr);
    }

    private final String buildWcdmaInfoString(CellInfoWcdma ci) {
        CellIdentityWcdma cidWcdma = ci.getCellIdentity();
        CellSignalStrengthWcdma ssWcdma = ci.getCellSignalStrength();
        String str = "%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-3.3s %-4.4s\n";
        Object[] objArr = new Object[8];
        objArr[0] = ci.isRegistered() ? "S  " : "   ";
        objArr[1] = getCellInfoDisplayString(cidWcdma.getMcc());
        objArr[2] = getCellInfoDisplayString(cidWcdma.getMnc());
        objArr[3] = getCellInfoDisplayString(cidWcdma.getLac());
        objArr[4] = getCellInfoDisplayString(cidWcdma.getCid());
        objArr[5] = getCellInfoDisplayString(cidWcdma.getUarfcn());
        objArr[6] = getCellInfoDisplayString(cidWcdma.getPsc());
        objArr[7] = getCellInfoDisplayString(ssWcdma.getDbm());
        return String.format(str, objArr);
    }

    private final String buildCellInfoString(List<CellInfo> arrayCi) {
        String value = new String();
        StringBuilder cdmaCells = new StringBuilder();
        StringBuilder gsmCells = new StringBuilder();
        StringBuilder lteCells = new StringBuilder();
        StringBuilder wcdmaCells = new StringBuilder();
        if (arrayCi != null) {
            for (CellInfo ci : arrayCi) {
                if (ci instanceof CellInfoLte) {
                    lteCells.append(buildLteInfoString((CellInfoLte) ci));
                } else if (ci instanceof CellInfoWcdma) {
                    wcdmaCells.append(buildWcdmaInfoString((CellInfoWcdma) ci));
                } else if (ci instanceof CellInfoGsm) {
                    gsmCells.append(buildGsmInfoString((CellInfoGsm) ci));
                } else if (ci instanceof CellInfoCdma) {
                    cdmaCells.append(buildCdmaInfoString((CellInfoCdma) ci));
                }
            }
            if (lteCells.length() != 0) {
                value = (value + String.format("LTE\n%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-3.3s %-6.6s %-4.4s %-4.4s %-2.2s\n", new Object[]{"SRV", "MCC", "MNC", "TAC", "CID", "PCI", "EARFCN", "RSRP", "RSRQ", "TA"})) + lteCells.toString();
            }
            if (wcdmaCells.length() != 0) {
                value = (value + String.format("WCDMA\n%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-3.3s %-4.4s\n", new Object[]{"SRV", "MCC", "MNC", "LAC", "CID", "UARFCN", "PSC", "RSCP"})) + wcdmaCells.toString();
            }
            if (gsmCells.length() != 0) {
                value = (value + String.format("GSM\n%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-4.4s %-4.4s\n", new Object[]{"SRV", "MCC", "MNC", "LAC", "CID", "ARFCN", "BSIC", "RSSI"})) + gsmCells.toString();
            }
            if (cdmaCells.length() != 0) {
                value = (value + String.format("CDMA/EVDO\n%-3.3s %-5.5s %-5.5s %-5.5s %-6.6s %-6.6s %-6.6s %-6.6s %-5.5s\n", new Object[]{"SRV", "SID", "NID", "BSID", "C-RSSI", "C-ECIO", "E-RSSI", "E-ECIO", "E-SNR"})) + cdmaCells.toString();
            }
        } else {
            value = HwCustStatusImpl.SUMMARY_UNKNOWN;
        }
        return value.toString();
    }

    private final void updateCellInfo(List<CellInfo> arrayCi) {
        this.mCellInfo.setText(buildCellInfoString(arrayCi));
    }

    private final void updateDcRtInfoTv(DataConnectionRealTimeInfo dcRtInfo) {
        this.mDcRtInfoTv.setText(dcRtInfo.toString());
    }

    private final void updateMessageWaiting() {
        this.mMwi.setText(String.valueOf(this.mMwiValue));
    }

    private final void updateCallRedirect() {
        this.mCfi.setText(String.valueOf(this.mCfiValue));
    }

    private final void updateServiceState(ServiceState serviceState) {
        int state = serviceState.getState();
        Resources r = getResources();
        String display = r.getString(2131624395);
        switch (state) {
            case 0:
                display = r.getString(2131624382);
                break;
            case 1:
            case 2:
                display = r.getString(2131624384);
                break;
            case 3:
                display = r.getString(2131624385);
                break;
        }
        this.gsmState.setText(display);
        if (serviceState.getRoaming()) {
            this.roamingState.setText(2131624386);
        } else {
            this.roamingState.setText(2131624387);
        }
        this.operatorName.setText(serviceState.getOperatorAlphaLong());
    }

    private final void updatePhoneState(int state) {
        Resources r = getResources();
        String display = r.getString(2131624395);
        switch (state) {
            case 0:
                display = r.getString(2131624388);
                break;
            case 1:
                display = r.getString(2131624389);
                break;
            case 2:
                display = r.getString(2131624390);
                break;
        }
        this.callState.setText(display);
    }

    private final void updateDataState() {
        int state = this.mTelephonyManager.getDataState();
        Resources r = getResources();
        String display = r.getString(2131624395);
        switch (state) {
            case 0:
                display = r.getString(2131624391);
                break;
            case 1:
                display = r.getString(2131624392);
                break;
            case 2:
                display = r.getString(2131624393);
                break;
            case 3:
                display = r.getString(2131624394);
                break;
        }
        if (ISMULTISIMENABLED && MSimTelephonyManager.getDefault().getPreferredDataSubscription() != this.subscription) {
            display = r.getString(2131624391);
        }
        this.gprsState.setText(display);
    }

    private final void updateNetworkType() {
        if (this.phone != null) {
            this.dataNetwork.setText(ServiceState.rilRadioTechnologyToString(this.phone.getServiceState().getRilDataRadioTechnology()));
            this.voiceNetwork.setText(ServiceState.rilRadioTechnologyToString(this.phone.getServiceState().getRilVoiceRadioTechnology()));
        }
    }

    private final void updateProperties() {
        Resources r = getResources();
        String s = this.phone.getDeviceId();
        if (s == null) {
            s = r.getString(2131624395);
        }
        this.mDeviceId.setText(s);
        s = this.phone.getLine1Number();
        if (s == null) {
            s = r.getString(2131624395);
        }
        this.number.setText(s);
    }

    private final void updateDataStats2() {
        Resources r = getResources();
        long txPackets = TrafficStats.getMobileTxPackets();
        long rxPackets = TrafficStats.getMobileRxPackets();
        long txBytes = TrafficStats.getMobileTxBytes();
        long rxBytes = TrafficStats.getMobileRxBytes();
        String packets = r.getString(2131624396);
        String bytes = r.getString(2131624397);
        this.sent.setText(txPackets + " " + packets + ", " + txBytes + " " + bytes);
        this.received.setText(rxPackets + " " + packets + ", " + rxBytes + " " + bytes);
    }

    private final void pingHostname() {
        try {
            if (Runtime.getRuntime().exec("ping -c 1 www.google.com").waitFor() == 0) {
                this.mPingHostnameResultV4 = "Pass";
            } else {
                this.mPingHostnameResultV4 = String.format("Fail(%d)", new Object[]{Integer.valueOf(Runtime.getRuntime().exec("ping -c 1 www.google.com").waitFor())});
            }
        } catch (IOException e) {
            try {
                this.mPingHostnameResultV4 = "Fail: IOException";
            } catch (InterruptedException e2) {
                String str = "Fail: InterruptedException";
                this.mPingHostnameResultV6 = str;
                this.mPingHostnameResultV4 = str;
                return;
            }
        }
        try {
            if (Runtime.getRuntime().exec("ping6 -c 1 www.google.com").waitFor() == 0) {
                this.mPingHostnameResultV6 = "Pass";
                return;
            }
            this.mPingHostnameResultV6 = String.format("Fail(%d)", new Object[]{Integer.valueOf(Runtime.getRuntime().exec("ping6 -c 1 www.google.com").waitFor())});
        } catch (IOException e3) {
            this.mPingHostnameResultV6 = "Fail: IOException";
        }
    }

    private void refreshSmsc() {
        this.phone.getSmscAddress(this.mHandler.obtainMessage(1005));
    }

    private final void updatePingState() {
        this.mPingHostnameResultV4 = getResources().getString(2131624395);
        this.mPingHostnameResultV6 = getResources().getString(2131624395);
        this.mHttpClientTestResult = getResources().getString(2131624395);
        this.mPingHostnameV4.setText(this.mPingHostnameResultV4);
        this.mPingHostnameV6.setText(this.mPingHostnameResultV6);
        this.mHttpClientTest.setText(this.mHttpClientTestResult);
        final Runnable updatePingResults = new Runnable() {
            public void run() {
                RadioInfo.this.mPingHostnameV4.setText(RadioInfo.this.mPingHostnameResultV4);
                RadioInfo.this.mPingHostnameV6.setText(RadioInfo.this.mPingHostnameResultV6);
                RadioInfo.this.mHttpClientTest.setText(RadioInfo.this.mHttpClientTestResult);
            }
        };
        new Thread() {
            public void run() {
                RadioInfo.this.pingHostname();
                RadioInfo.this.mHandler.post(updatePingResults);
            }
        }.start();
        new Thread() {
            public void run() {
                RadioInfo.this.httpClientTest();
                RadioInfo.this.mHandler.post(updatePingResults);
            }
        }.start();
    }

    private boolean isRadioOn() {
        return this.phone.getServiceState().getState() != 3;
    }

    private void updateRadioPowerState() {
        this.radioPowerOnSwitch.setOnCheckedChangeListener(null);
        this.radioPowerOnSwitch.setChecked(isRadioOn());
        this.radioPowerOnSwitch.setOnCheckedChangeListener(this.mRadioPowerOnChangeListener);
    }

    private void createPhoneListener(int subscription) {
        this.mPhoneStateListener = new PhoneStateListenerEx(subscription) {
            public void onDataConnectionStateChanged(int state) {
                RadioInfo.this.updateDataState();
                RadioInfo.this.updateNetworkType();
            }

            public void onDataActivity(int direction) {
                RadioInfo.this.updateDataStats2();
            }

            public void onCellLocationChanged(CellLocation location) {
                RadioInfo.this.updateLocation(location);
            }

            public void onMessageWaitingIndicatorChanged(boolean mwi) {
                RadioInfo.this.mMwiValue = mwi;
                RadioInfo.this.updateMessageWaiting();
            }

            public void onCallForwardingIndicatorChanged(boolean cfi) {
                RadioInfo.this.mCfiValue = cfi;
                RadioInfo.this.updateCallRedirect();
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                RadioInfo.this.updateSignalStrength(signalStrength);
            }

            public void onDataConnectionStateChanged(int state, int networkType) {
                RadioInfo.this.updateNetworkType();
            }

            public void onCellInfoChanged(List<CellInfo> arrayCi) {
                RadioInfo.this.log("onCellInfoChanged: arrayCi=" + arrayCi);
                RadioInfo.this.updateCellInfo(arrayCi);
            }
        };
    }
}
