package com.android.settings.wifi.ap;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.ListSpinner;
import com.android.settings.deviceinfo.DeviceNameSettings;
import com.android.settings.wifi.WifiApDialog;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.huawei.android.net.wifi.WifiManagerCommonEx;
import com.huawei.cust.HwCustUtils;
import java.nio.charset.Charset;
import java.util.Arrays;

public class WifiApDialogActivity extends SettingsDrawerActivity implements OnClickListener, TextWatcher, OnItemSelectedListener {
    protected static final boolean HWFLOW;
    private static final int[] MAX_CONNECTIONS_TITLES = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
    private static final double[] WIFI_AP_BAND_CONFIG_2G_ONLY_TITLES = new double[]{2.4d};
    private int OPEN_INDEX = 0;
    private int WPA2_INDEX = 1;
    private WifiConfiguration config = null;
    private CheckBox mAdvancedBox;
    private int mBandIndex = this.OPEN_INDEX;
    private Spinner mBroadcastChannelSpinner;
    private String[] mBroadcastChannelSpinnerEntries;
    private String[] mBroadcastChannelSpinnerValues;
    private Button mCancelBtn;
    private Spinner mChannel;
    private Context mContext;
    private HwCustWifiApDialogActivity mCustWifiApDialogActivity;
    private TextView mHint;
    private Spinner mMaxConnections;
    private EditText mPassword;
    private Button mSaveBtn;
    private Spinner mSecurity;
    private int mSecurityTypeIndex = this.OPEN_INDEX;
    private int mSelectedChannelPosition;
    private int mSelectedMaxSCBPosition;
    private TextView mSsid;
    private View mSsidHolder;
    WifiConfiguration mWifiConfig;
    WifiManager mWifiManager;
    private CheckBox showPassword;

    static {
        boolean z = true;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z = Log.isLoggable("WifiApDialog", 4);
            } else {
                z = false;
            }
        }
        HWFLOW = z;
    }

    public WifiConfiguration getConfig() {
        this.config = new WifiConfiguration();
        this.config.SSID = this.mSsid.getText().toString();
        this.config.apBand = this.mBandIndex;
        this.config.apChannel = getChannel();
        if (this.mCustWifiApDialogActivity != null) {
            this.mCustWifiApDialogActivity.custConfig(this.config);
        }
        if (this.mSecurityTypeIndex == this.OPEN_INDEX) {
            this.config.allowedKeyManagement.set(0);
            return this.config;
        } else if (this.mSecurityTypeIndex != this.WPA2_INDEX) {
            return null;
        } else {
            this.config.allowedKeyManagement.set(4);
            this.config.allowedAuthAlgorithms.set(0);
            if (this.mPassword.length() != 0) {
                this.config.preSharedKey = this.mPassword.getText().toString();
            }
            return this.config;
        }
    }

    private void initHint() {
        this.mHint = (TextView) findViewById(2131886417);
        String hint = String.format(getString(2131628875, new Object[]{Integer.valueOf(8)}), new Object[0]);
        if (this.mHint != null) {
            this.mHint.setText(hint);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> channelAdapter;
        super.onCreate(savedInstanceState);
        setContentView(2130969263);
        initHint();
        this.mAdvancedBox = (CheckBox) findViewById(2131887463);
        if (this.mAdvancedBox != null) {
            this.mAdvancedBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.getId() != 2131887463) {
                        return;
                    }
                    if (isChecked) {
                        WifiApDialogActivity.this.findViewById(2131887465).setVisibility(0);
                        WifiApDialogActivity.this.findViewById(2131887464).setVisibility(8);
                        return;
                    }
                    WifiApDialogActivity.this.findViewById(2131887465).setVisibility(8);
                }
            });
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        this.mCustWifiApDialogActivity = (HwCustWifiApDialogActivity) HwCustUtils.createObj(HwCustWifiApDialogActivity.class, new Object[]{this});
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mContext = this;
        this.mWifiConfig = (WifiConfiguration) getIntent().getParcelableExtra("wifi_config");
        this.mChannel = (Spinner) findViewById(2131887467);
        setTitle(2131625060);
        this.mSecurity = (Spinner) findViewById(2131887460);
        findViewById(2131886800).setVisibility(0);
        this.mSsidHolder = findViewById(2131887459);
        this.mSsidHolder.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.DEVICE_NAME_SETTINGS");
                intent.putExtra("device_name", WifiApDialogActivity.this.mSsid.getText());
                WifiApDialogActivity.this.startActivityForResult(intent, 1000);
            }
        });
        ((TextView) this.mSsidHolder.findViewById(2131886914)).setText(2131628525);
        ((TextView) this.mSsidHolder.findViewById(16908310)).setText(2131628601);
        LayoutInflater.from(this).inflate(2130968998, (ViewGroup) this.mSsidHolder.findViewById(16908312));
        this.mSsid = (TextView) this.mSsidHolder.findViewById(16908304);
        this.mBroadcastChannelSpinnerEntries = getResources().getStringArray(2131361936);
        this.mBroadcastChannelSpinnerValues = getResources().getStringArray(2131361937);
        this.mPassword = (EditText) findViewById(2131887420);
        String countryCode = this.mWifiManager.getCountryCode();
        boolean z = false;
        boolean z2 = false;
        if (this.mCustWifiApDialogActivity != null) {
            z = this.mCustWifiApDialogActivity.isSetWifiApBand2G();
            if (HWFLOW) {
                Log.i("WifiApDialog", "isSetWifiApBand2G :" + z);
            }
            z2 = this.mCustWifiApDialogActivity.isSetWifiApBand5G(countryCode);
            if (HWFLOW) {
                Log.i("WifiApDialog", "isSetWifiApBand5G :" + z2);
            }
        }
        if (this.mWifiManager.isDualBandSupported() && countryCode != null) {
            if (z) {
            }
            channelAdapter = new ArrayAdapter(this.mContext, 17367048, buildWifiApBandConfigFullTitles(this.mContext));
            channelAdapter.setDropDownViewResource(17367049);
            this.mSaveBtn = (Button) findViewById(2131887474);
            this.mSaveBtn.setOnClickListener(this);
            this.mCancelBtn = (Button) findViewById(2131887473);
            this.mCancelBtn.setOnClickListener(this);
            if (this.mWifiConfig != null) {
                this.mSecurityTypeIndex = WifiApDialog.getSecurityTypeIndex(this.mWifiConfig);
                this.mSsid.setText(this.mWifiConfig.SSID);
                if (this.mWifiConfig.apBand == 0) {
                    this.mBandIndex = 0;
                } else {
                    this.mBandIndex = 1;
                }
                this.mSecurity.setSelection(this.mSecurityTypeIndex);
                if (this.mSecurityTypeIndex == this.WPA2_INDEX) {
                    this.mPassword.setText(this.mWifiConfig.preSharedKey);
                }
            }
            this.mChannel.setAdapter(channelAdapter);
            if (this.mWifiConfig != null && this.mWifiConfig.apBand >= 0 && this.mWifiConfig.apBand < channelAdapter.getCount()) {
                this.mChannel.setSelection(this.mWifiConfig.apBand);
            }
            this.mChannel.setOnItemSelectedListener(new OnItemSelectedListener() {
                boolean mInit = true;

                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    WifiApDialogActivity.this.buildBroadcastChannelSpinner();
                    if (this.mInit) {
                        this.mInit = false;
                        WifiApDialogActivity.this.mChannel.setSelection(WifiApDialogActivity.this.mBandIndex);
                        return;
                    }
                    WifiApDialogActivity.this.mBandIndex = position;
                    if (WifiApDialogActivity.this.mWifiConfig != null) {
                        WifiApDialogActivity.this.mWifiConfig.apBand = WifiApDialogActivity.this.mBandIndex;
                        Log.i("WifiApDialog", "config on channelIndex : " + WifiApDialogActivity.this.mBandIndex + " Band: " + WifiApDialogActivity.this.mWifiConfig.apBand);
                    }
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            this.mPassword.addTextChangedListener(this);
            this.showPassword = (CheckBox) findViewById(2131886368);
            if (1 == System.getInt(getContentResolver(), "wifiap_show_password", 0)) {
                this.showPassword.setChecked(true);
            }
            this.showPassword.setOnClickListener(this);
            this.mSecurity.setOnItemSelectedListener(this);
            showSecurityFields();
            showChannel();
            showMaxConnections();
            validate();
            if (this.mCustWifiApDialogActivity == null) {
                this.mCustWifiApDialogActivity.onCustCreate();
            }
        }
        if (!z2) {
            Log.i("WifiApDialog", (!this.mWifiManager.isDualBandSupported() ? "Device do not support 5GHz " : "") + (countryCode == null ? " NO country code" : "") + " forbid 5GHz");
            channelAdapter = new ArrayAdapter(this.mContext, 17367048, buildWifiApBandConfig2GOnlyTitles(this.mContext));
            if (this.mWifiConfig != null) {
                this.mWifiConfig.apBand = 0;
            }
            channelAdapter.setDropDownViewResource(17367049);
            this.mSaveBtn = (Button) findViewById(2131887474);
            this.mSaveBtn.setOnClickListener(this);
            this.mCancelBtn = (Button) findViewById(2131887473);
            this.mCancelBtn.setOnClickListener(this);
            if (this.mWifiConfig != null) {
                this.mSecurityTypeIndex = WifiApDialog.getSecurityTypeIndex(this.mWifiConfig);
                this.mSsid.setText(this.mWifiConfig.SSID);
                if (this.mWifiConfig.apBand == 0) {
                    this.mBandIndex = 1;
                } else {
                    this.mBandIndex = 0;
                }
                this.mSecurity.setSelection(this.mSecurityTypeIndex);
                if (this.mSecurityTypeIndex == this.WPA2_INDEX) {
                    this.mPassword.setText(this.mWifiConfig.preSharedKey);
                }
            }
            this.mChannel.setAdapter(channelAdapter);
            this.mChannel.setSelection(this.mWifiConfig.apBand);
            this.mChannel.setOnItemSelectedListener(/* anonymous class already generated */);
            this.mPassword.addTextChangedListener(this);
            this.showPassword = (CheckBox) findViewById(2131886368);
            if (1 == System.getInt(getContentResolver(), "wifiap_show_password", 0)) {
                this.showPassword.setChecked(true);
            }
            this.showPassword.setOnClickListener(this);
            this.mSecurity.setOnItemSelectedListener(this);
            showSecurityFields();
            showChannel();
            showMaxConnections();
            validate();
            if (this.mCustWifiApDialogActivity == null) {
                this.mCustWifiApDialogActivity.onCustCreate();
            }
        }
        channelAdapter = new ArrayAdapter(this.mContext, 17367048, buildWifiApBandConfigFullTitles(this.mContext));
        channelAdapter.setDropDownViewResource(17367049);
        this.mSaveBtn = (Button) findViewById(2131887474);
        this.mSaveBtn.setOnClickListener(this);
        this.mCancelBtn = (Button) findViewById(2131887473);
        this.mCancelBtn.setOnClickListener(this);
        if (this.mWifiConfig != null) {
            this.mSecurityTypeIndex = WifiApDialog.getSecurityTypeIndex(this.mWifiConfig);
            this.mSsid.setText(this.mWifiConfig.SSID);
            if (this.mWifiConfig.apBand == 0) {
                this.mBandIndex = 0;
            } else {
                this.mBandIndex = 1;
            }
            this.mSecurity.setSelection(this.mSecurityTypeIndex);
            if (this.mSecurityTypeIndex == this.WPA2_INDEX) {
                this.mPassword.setText(this.mWifiConfig.preSharedKey);
            }
        }
        this.mChannel.setAdapter(channelAdapter);
        this.mChannel.setSelection(this.mWifiConfig.apBand);
        this.mChannel.setOnItemSelectedListener(/* anonymous class already generated */);
        this.mPassword.addTextChangedListener(this);
        this.showPassword = (CheckBox) findViewById(2131886368);
        if (1 == System.getInt(getContentResolver(), "wifiap_show_password", 0)) {
            this.showPassword.setChecked(true);
        }
        this.showPassword.setOnClickListener(this);
        this.mSecurity.setOnItemSelectedListener(this);
        showSecurityFields();
        showChannel();
        showMaxConnections();
        validate();
        if (this.mCustWifiApDialogActivity == null) {
            this.mCustWifiApDialogActivity.onCustCreate();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000 && resultCode == -1) {
            this.mWifiConfig.SSID = DeviceNameSettings.getDeviceName(this);
            this.mSsid.setText(this.mWifiConfig.SSID);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        int i;
        super.onRestoreInstanceState(savedInstanceState);
        EditText editText = this.mPassword;
        if (((CheckBox) findViewById(2131886368)).isChecked()) {
            i = 144;
        } else {
            i = 128;
        }
        editText.setInputType(i | 1);
    }

    public void onStart() {
        int i;
        EditText editText = this.mPassword;
        if (this.showPassword.isChecked()) {
            i = 144;
        } else {
            i = 128;
        }
        editText.setInputType(i | 1);
        super.onStart();
    }

    public void onStop() {
        super.onStop();
        if (this.config != null) {
            changeConfig(this.config);
        } else {
            changeConfig(this.mWifiConfig);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeConfig(WifiConfiguration mwc) {
        if (mwc != null) {
            if (this.mSsid != null) {
                this.mSsid.setText(this.mSsid.getText().toString());
            }
            if (this.mSecurity != null) {
                this.mSecurity.setSelection(this.mSecurityTypeIndex);
            }
        }
        this.showPassword.setChecked(false);
        if (this.mPassword != null) {
            this.mPassword.setInputType(129);
        }
    }

    private void validate() {
        String mSsidString = "";
        if (this.mSsid != null) {
            mSsidString = this.mSsid.getText().toString();
        }
        if ((this.mSsid == null || this.mSsid.length() != 0) && ((this.mSecurityTypeIndex != this.WPA2_INDEX || this.mPassword.length() >= 8) && (this.mSsid == null || Charset.forName("UTF-8").encode(mSsidString).limit() <= 32))) {
            this.mSaveBtn.setEnabled(true);
        } else {
            this.mSaveBtn.setEnabled(false);
        }
    }

    public void onClick(View view) {
        if (this.mCustWifiApDialogActivity != null) {
            this.mCustWifiApDialogActivity.onCustClick(view);
        }
        if (view == this.showPassword) {
            int i;
            int index = this.mPassword.getSelectionStart();
            EditText editText = this.mPassword;
            if (((CheckBox) view).isChecked()) {
                i = 144;
            } else {
                i = 128;
            }
            editText.setInputType(i | 1);
            if (index >= 0) {
                this.mPassword.setSelection(index);
            }
        } else if (view == this.mSaveBtn) {
            Intent intent = new Intent();
            intent.putExtra("wifi_ap_channel", getChannel());
            intent.putExtra("wifi_ap_max_connections", getMaxConnections());
            intent.putExtra("wifi_config", getConfig());
            setResult(-1, intent);
            finish();
        } else if (view == this.mCancelBtn) {
            setResult(0);
            finish();
        }
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (this.mSecurity == parent) {
            this.mSecurityTypeIndex = position;
            showSecurityFields();
            validate();
            showSecurityMsg();
        } else if (this.mBroadcastChannelSpinner == parent) {
            this.mSelectedChannelPosition = position;
        } else if (parent == this.mMaxConnections) {
            this.mSelectedMaxSCBPosition = position;
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void showSecurityFields() {
        if (this.mSecurityTypeIndex == this.OPEN_INDEX) {
            findViewById(2131886320).setVisibility(8);
            findViewById(2131887462).setVisibility(0);
            return;
        }
        findViewById(2131886320).setVisibility(0);
        findViewById(2131887462).setVisibility(8);
    }

    public void showSecurityMsg() {
        if (this.mSecurityTypeIndex == this.OPEN_INDEX) {
            Context context = this;
            if (1 == System.getInt(getContentResolver(), "select_open_configure_wi_fi_hotspot", 0)) {
                Toast.makeText(this, 2131628358, 0).show();
            }
        }
    }

    public int getChannel() {
        if (WifiApClientUtils.getInstance(this).isSupportChannel()) {
            return getEntryValueByPosition(this.mSelectedChannelPosition, 0);
        }
        return Secure.getInt(getContentResolver(), "wifi_ap_channel", 0);
    }

    private void showChannel() {
        findViewById(2131887468).setVisibility(WifiApClientUtils.getInstance(this).isSupportChannel() ? 0 : 8);
        buildBroadcastChannelSpinner();
    }

    private void buildBroadcastChannelSpinner() {
        if (this.mBroadcastChannelSpinner == null) {
            this.mBroadcastChannelSpinner = (Spinner) findViewById(2131887469);
        }
        if (this.mBroadcastChannelSpinner instanceof ListSpinner) {
            ((ListSpinner) this.mBroadcastChannelSpinner).dismissDialog();
        }
        resetBroadcastChannelSpinnerAdapter();
        int apChannel = Secure.getInt(getContentResolver(), "wifi_ap_channel", 0);
        if (apChannel == 0 && this.mWifiConfig != null && this.mWifiConfig.apChannel > 0) {
            apChannel = this.mWifiConfig.apChannel;
        }
        this.mSelectedChannelPosition = getEntryPositionByValue(String.valueOf(apChannel), 0);
        this.mBroadcastChannelSpinner.setOnItemSelectedListener(null);
        if (this.mSelectedChannelPosition >= this.mBroadcastChannelSpinner.getCount()) {
            this.mSelectedChannelPosition = 0;
        }
        this.mBroadcastChannelSpinner.setSelection(this.mSelectedChannelPosition);
        this.mBroadcastChannelSpinner.setOnItemSelectedListener(this);
    }

    private void showMaxConnections() {
        findViewById(2131887470).setVisibility(0);
        this.mMaxConnections = (Spinner) findViewById(2131887471);
        this.mMaxConnections.setAdapter(new ArrayAdapter(this.mContext, 17367048, buildMaxConnectionsEntries(this.mContext)));
        setMaxConnectionsSelection();
        this.mMaxConnections.setOnItemSelectedListener(this);
    }

    private void setMaxConnectionsSelection() {
        this.mSelectedMaxSCBPosition = getMaxSCBPositionByValue(String.valueOf(Secure.getInt(getContentResolver(), "wifi_ap_maxscb", 8)));
        this.mMaxConnections.setSelection(this.mSelectedMaxSCBPosition);
    }

    private int getMaxSCBPositionByValue(String value) {
        String[] aStrMaxConnections = getResources().getStringArray(2131361914);
        if (!(value == null || aStrMaxConnections == null)) {
            for (int i = aStrMaxConnections.length - 1; i >= 0; i--) {
                if (aStrMaxConnections[i].equals(value)) {
                    return i;
                }
            }
        }
        return 8;
    }

    public int getMaxConnections() {
        if (this.mSelectedMaxSCBPosition < 0) {
            return 8;
        }
        String[] aStrMaxConnections = getResources().getStringArray(2131361914);
        if (aStrMaxConnections == null || this.mSelectedMaxSCBPosition >= aStrMaxConnections.length) {
            return 8;
        }
        int result = 8;
        try {
            result = Integer.parseInt(aStrMaxConnections[this.mSelectedMaxSCBPosition]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    private int getEntryPositionByValue(String entryValue, int defaultPosition) {
        if (!(entryValue == null || this.mBroadcastChannelSpinnerValues == null)) {
            for (int i = this.mBroadcastChannelSpinnerValues.length - 1; i >= 0; i--) {
                if (this.mBroadcastChannelSpinnerValues[i].equals(entryValue)) {
                    return i;
                }
            }
        }
        return defaultPosition;
    }

    public int getEntryValueByPosition(int position, int defaultValue) {
        int result = defaultValue;
        if (this.mBroadcastChannelSpinnerValues == null || position < 0 || position >= this.mBroadcastChannelSpinnerValues.length) {
            return defaultValue;
        }
        try {
            result = Integer.parseInt(this.mBroadcastChannelSpinnerValues[position]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getSecurityTypeIndex() {
        return this.mSecurityTypeIndex;
    }

    public WifiConfiguration getWifiConfig() {
        return this.mWifiConfig;
    }

    public void setWap2IndexValue(int value) {
        this.WPA2_INDEX = value;
    }

    public void setOpenIndexValue(int value) {
        this.OPEN_INDEX = value;
    }

    public int getWap2IndexValue() {
        return this.WPA2_INDEX;
    }

    public int getOpenIndexValue() {
        return this.OPEN_INDEX;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mSecurity instanceof ListSpinner) {
            ((ListSpinner) this.mSecurity).dismissDialog();
        }
        if (this.mBroadcastChannelSpinner instanceof ListSpinner) {
            ((ListSpinner) this.mBroadcastChannelSpinner).dismissDialog();
        }
        if (this.mMaxConnections instanceof ListSpinner) {
            ((ListSpinner) this.mMaxConnections).dismissDialog();
        }
        if (this.mChannel instanceof ListSpinner) {
            ((ListSpinner) this.mChannel).dismissDialog();
        }
    }

    private void resetBroadcastChannelSpinnerAdapter() {
        this.mSelectedChannelPosition = 0;
        if (this.mChannel.getSelectedItemPosition() == 1) {
            String[] apChannelEntries = getResources().getStringArray(2131361936);
            int[] channels = WifiManagerCommonEx.getChannelListFor5G();
            if (channels == null || channels.length == 0) {
                int apChannel = Secure.getInt(getContentResolver(), "wifi_ap_channel", 0);
                if (apChannel == 0 && this.mWifiConfig != null && this.mWifiConfig.apChannel > 0) {
                    apChannel = this.mWifiConfig.apChannel;
                }
                if (this.mWifiConfig == null || this.mWifiConfig.apBand != 1 || apChannel <= 0) {
                    this.mBroadcastChannelSpinnerEntries = new String[1];
                    this.mBroadcastChannelSpinnerValues = new String[1];
                    this.mBroadcastChannelSpinnerEntries[0] = apChannelEntries[0];
                    this.mBroadcastChannelSpinnerValues[0] = String.valueOf(0);
                } else {
                    this.mBroadcastChannelSpinnerEntries = new String[2];
                    this.mBroadcastChannelSpinnerValues = new String[2];
                    this.mBroadcastChannelSpinnerEntries[0] = apChannelEntries[0];
                    this.mBroadcastChannelSpinnerValues[0] = String.valueOf(0);
                    this.mBroadcastChannelSpinnerEntries[1] = String.valueOf(apChannel);
                    this.mBroadcastChannelSpinnerValues[1] = String.valueOf(apChannel);
                }
            } else {
                Arrays.sort(channels);
                this.mBroadcastChannelSpinnerEntries = new String[(channels.length + 1)];
                this.mBroadcastChannelSpinnerValues = new String[(channels.length + 1)];
                this.mBroadcastChannelSpinnerEntries[0] = apChannelEntries[0];
                this.mBroadcastChannelSpinnerValues[0] = String.valueOf(0);
                for (int i = 0; i < channels.length; i++) {
                    this.mBroadcastChannelSpinnerEntries[i + 1] = String.valueOf(channels[i]);
                    this.mBroadcastChannelSpinnerValues[i + 1] = String.valueOf(channels[i]);
                }
            }
        } else {
            this.mBroadcastChannelSpinnerEntries = getResources().getStringArray(2131361936);
            this.mBroadcastChannelSpinnerValues = getResources().getStringArray(2131361937);
        }
        convertToLocalNumber(this.mBroadcastChannelSpinnerEntries);
        this.mBroadcastChannelSpinner.setAdapter(new ArrayAdapter(this.mContext, 17367048, this.mBroadcastChannelSpinnerEntries));
    }

    private CharSequence[] buildWifiApBandConfigFullTitles(Context context) {
        CharSequence[] tiles = new String[2];
        tiles[0] = String.format(getResources().getString(2131628417, new Object[]{Double.valueOf(2.4d)}), new Object[0]);
        tiles[1] = String.format(getResources().getString(2131628418, new Object[]{Integer.valueOf(5)}), new Object[0]);
        return tiles;
    }

    private CharSequence[] buildWifiApBandConfig2GOnlyTitles(Context context) {
        CharSequence[] tiles = new String[1];
        tiles[0] = String.format(getResources().getString(2131628417, new Object[]{Double.valueOf(WIFI_AP_BAND_CONFIG_2G_ONLY_TITLES[0])}), new Object[0]);
        return tiles;
    }

    private CharSequence[] buildMaxConnectionsEntries(Context context) {
        tiles = new String[8];
        tiles[0] = String.format(getResources().getString(2131628409, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[0])}), new Object[0]);
        tiles[1] = String.format(getResources().getString(2131628410, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[1])}), new Object[0]);
        tiles[2] = String.format(getResources().getString(2131628411, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[2])}), new Object[0]);
        tiles[3] = String.format(getResources().getString(2131628412, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[3])}), new Object[0]);
        tiles[4] = String.format(getResources().getString(2131628413, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[4])}), new Object[0]);
        tiles[5] = String.format(getResources().getString(2131628414, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[5])}), new Object[0]);
        tiles[6] = String.format(getResources().getString(2131628415, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[6])}), new Object[0]);
        tiles[7] = String.format(getResources().getString(2131628416, new Object[]{Integer.valueOf(MAX_CONNECTIONS_TITLES[7])}), new Object[0]);
        return tiles;
    }

    private void convertToLocalNumber(String[] entries) {
        if (entries != null && entries.length != 0) {
            for (int i = 0; i < entries.length; i++) {
                try {
                    entries[i] = this.mContext.getString(2131628408, new Object[]{Integer.valueOf(entries[i])});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
