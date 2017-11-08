package com.android.settings.wifi.bridge;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.android.settings.MLog;
import com.android.settings.Utf8ByteLengthFilter;
import java.nio.charset.Charset;

public class WifiBridgeDialogActivity extends Activity implements OnClickListener, TextWatcher, OnItemSelectedListener {
    private Spinner mApBandChannel;
    private int mApChannelIndex = 0;
    private String mApHotSsid;
    private int mBandIndex = 0;
    private Button mCancelBtn;
    private EditText mPassword;
    private WifiConfiguration mReceivedWifiConfig;
    private Button mSaveBtn;
    private Spinner mSecurity;
    private CheckBox mShowPassword;
    private EditText mSsid;
    private WifiConfiguration mWifiConfig = null;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969268);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        setTitle(2131627243);
        this.mReceivedWifiConfig = (WifiConfiguration) getIntent().getParcelableExtra("wifi_bridge_config");
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        this.mSsid = (EditText) findViewById(2131887459);
        this.mSecurity = (Spinner) findViewById(2131887460);
        this.mPassword = (EditText) findViewById(2131887420);
        this.mShowPassword = (CheckBox) findViewById(2131886368);
        this.mShowPassword.setOnClickListener(this);
        this.mApBandChannel = (Spinner) findViewById(2131887467);
        this.mApBandChannel.setVisibility(8);
        this.mSaveBtn = (Button) findViewById(2131887479);
        this.mSaveBtn.setOnClickListener(this);
        this.mCancelBtn = (Button) findViewById(2131887478);
        this.mCancelBtn.setOnClickListener(this);
        this.mSecurity.setEnabled(false);
        if (this.mReceivedWifiConfig != null) {
            if (this.mReceivedWifiConfig.SSID != null) {
                this.mSsid.setText(this.mReceivedWifiConfig.SSID);
                this.mSsid.setSelection(this.mReceivedWifiConfig.SSID.length());
            } else {
                MLog.e("WifiBridgeDialog", "Get SSID from Intent faild.");
            }
            if (this.mReceivedWifiConfig.apBand == 0) {
                this.mBandIndex = 0;
            } else {
                this.mBandIndex = 1;
            }
            this.mApChannelIndex = this.mReceivedWifiConfig.apChannel;
            this.mPassword.setText(this.mReceivedWifiConfig.preSharedKey);
            if (this.mReceivedWifiConfig.apBand >= 0 && this.mReceivedWifiConfig.apBand <= 1) {
                this.mApBandChannel.setSelection(this.mReceivedWifiConfig.apBand);
            }
        } else {
            MLog.e("WifiBridgeDialog", "Get WifiConfigation from Intent faild.");
        }
        this.mApBandChannel.setOnItemSelectedListener(new OnItemSelectedListener() {
            boolean mInit = true;

            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (this.mInit) {
                    this.mInit = false;
                    WifiBridgeDialogActivity.this.mApBandChannel.setSelection(WifiBridgeDialogActivity.this.mBandIndex);
                } else {
                    WifiBridgeDialogActivity.this.mBandIndex = position;
                    if (WifiBridgeDialogActivity.this.mReceivedWifiConfig != null) {
                        WifiBridgeDialogActivity.this.mReceivedWifiConfig.apBand = WifiBridgeDialogActivity.this.mBandIndex;
                        MLog.i("WifiBridgeDialog", "config on channelIndex : " + WifiBridgeDialogActivity.this.mBandIndex + " Band: " + WifiBridgeDialogActivity.this.mReceivedWifiConfig.apBand);
                    }
                }
                if (WifiBridgeDialogActivity.this.mWifiInfo != null) {
                    WifiBridgeDialogActivity.this.mApHotSsid = WifiBridgeDialogActivity.this.mWifiInfo.getSSID();
                    WifiBridgeDialogActivity.this.mApChannelIndex = WifiBridgeDialogActivity.this.convertFrequencyToChannelNumber(WifiBridgeDialogActivity.this.mWifiInfo.getFrequency(), WifiBridgeDialogActivity.this.mBandIndex);
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.mSsid.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(30)});
        this.mSsid.addTextChangedListener(this);
        this.mPassword.addTextChangedListener(this);
        if (1 == System.getInt(getContentResolver(), "wifi_bridge_show_password", 0)) {
            this.mShowPassword.setChecked(true);
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
        if (this.mShowPassword.isChecked()) {
            i = 144;
        } else {
            i = 128;
        }
        editText.setInputType(i | 1);
        super.onStart();
    }

    public void onStop() {
        super.onStop();
        if (this.mWifiConfig != null) {
            changeConfig(this.mWifiConfig);
        } else {
            changeConfig(this.mReceivedWifiConfig);
        }
    }

    public void onClick(View view) {
        if (view == this.mShowPassword) {
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
            intent.putExtra("wifi_bridge_config", getConfig());
            setResult(-1, intent);
            finish();
        } else if (view == this.mCancelBtn) {
            setResult(0);
            finish();
        }
    }

    public WifiConfiguration getConfig() {
        this.mWifiConfig = new WifiConfiguration();
        this.mWifiConfig.SSID = this.mSsid.getText().toString();
        this.mWifiConfig.apBand = this.mBandIndex;
        this.mWifiConfig.apChannel = this.mApChannelIndex;
        this.mWifiConfig.allowedKeyManagement.set(4);
        this.mWifiConfig.allowedAuthAlgorithms.set(0);
        if (this.mPassword.length() != 0) {
            this.mWifiConfig.preSharedKey = this.mPassword.getText().toString();
        }
        return this.mWifiConfig;
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
    }

    private void validate() {
        String mSsidString = "";
        if (this.mSsid != null) {
            mSsidString = this.mSsid.getText().toString();
        }
        if ((this.mSsid == null || this.mSsid.length() != 0) && this.mPassword.length() >= 8 && ((this.mSsid == null || this.mApHotSsid == null || !this.mApHotSsid.replace("\"", "").equals(mSsidString)) && (this.mSsid == null || Charset.forName("UTF-8").encode(mSsidString).limit() <= 30))) {
            this.mSaveBtn.setEnabled(true);
        } else {
            this.mSaveBtn.setEnabled(false);
        }
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    private int convertFrequencyToChannelNumber(int frequency, int mBandIndex) {
        if (frequency < 2412 || frequency > 2484) {
            if (frequency >= 5170 && frequency <= 5825 && mBandIndex == 1) {
                return ((frequency - 5170) / 5) + 34;
            }
        } else if (mBandIndex == 0) {
            return ((frequency - 2412) / 5) + 1;
        }
        return 0;
    }

    private void changeConfig(WifiConfiguration mwc) {
        if (mwc != null) {
            if (this.mSsid != null) {
                this.mSsid.setText(this.mSsid.getText().toString());
                this.mSsid.setSelection(this.mSsid.getText().toString().length());
            }
            if (this.mPassword != null) {
                this.mPassword.setText(mwc.preSharedKey);
            }
        }
        this.mShowPassword.setChecked(false);
        if (this.mPassword != null) {
            this.mPassword.setInputType(129);
        }
    }
}
