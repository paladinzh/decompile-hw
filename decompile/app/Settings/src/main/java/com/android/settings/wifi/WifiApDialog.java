package com.android.settings.wifi;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.android.settings.Utf8ByteLengthFilter;
import java.nio.charset.Charset;

public class WifiApDialog extends WifiApDialogHwBase implements OnClickListener, TextWatcher, OnItemSelectedListener {
    private int mBandIndex = 0;
    private Context mContext;
    private final DialogInterface.OnClickListener mListener;
    WifiManager mWifiManager;

    public WifiApDialog(Context context, DialogInterface.OnClickListener listener, WifiConfiguration wifiConfig) {
        super(context);
        this.mListener = listener;
        this.mWifiConfig = wifiConfig;
        if (wifiConfig != null) {
            this.mSecurityTypeIndex = getSecurityTypeIndex(wifiConfig);
        }
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mContext = context;
    }

    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(4)) {
            return 1;
        }
        return 0;
    }

    public WifiConfiguration getConfig() {
        this.config = new WifiConfiguration();
        this.config.SSID = this.mSsid.getText().toString();
        this.config.apBand = this.mBandIndex;
        switch (this.mSecurityTypeIndex) {
            case 0:
                this.config.allowedKeyManagement.set(0);
                return this.config;
            case 1:
                this.config.allowedKeyManagement.set(4);
                this.config.allowedAuthAlgorithms.set(0);
                if (this.mPassword.length() != 0) {
                    this.config.preSharedKey = this.mPassword.getText().toString();
                }
                return this.config;
            default:
                return null;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> channelAdapter;
        this.mView = getLayoutInflater().inflate(2130969263, null);
        Spinner mSecurity = (Spinner) this.mView.findViewById(2131887460);
        final Spinner mChannel = (Spinner) this.mView.findViewById(2131887467);
        setView(this.mView);
        setInverseBackgroundForced(true);
        Context context = getContext();
        setTitle(2131625060);
        this.mView.findViewById(2131886800).setVisibility(0);
        this.mSsid = (EditText) this.mView.findViewById(2131887459);
        this.mPassword = (EditText) this.mView.findViewById(2131887420);
        String countryCode = this.mWifiManager.getCountryCode();
        if (!this.mWifiManager.isDualBandSupported() || countryCode == null) {
            Log.i("WifiApDialog", (!this.mWifiManager.isDualBandSupported() ? "Device do not support 5GHz " : "") + (countryCode == null ? " NO country code" : "") + " forbid 5GHz");
            channelAdapter = ArrayAdapter.createFromResource(this.mContext, 2131361850, 17367048);
            this.mWifiConfig.apBand = 0;
        } else {
            channelAdapter = ArrayAdapter.createFromResource(this.mContext, 2131361849, 17367048);
        }
        channelAdapter.setDropDownViewResource(17367049);
        setButton(-1, context.getString(2131625011), this.mListener);
        setButton(-2, context.getString(2131625013), this.mListener);
        if (this.mWifiConfig != null) {
            this.mSsid.setText(this.mWifiConfig.SSID);
            if (this.mWifiConfig.SSID != null) {
                this.mSsid.setSelection(this.mWifiConfig.SSID.length());
            }
            if (this.mWifiConfig.apBand == 0) {
                this.mBandIndex = 0;
            } else {
                this.mBandIndex = 1;
            }
            mSecurity.setSelection(this.mSecurityTypeIndex);
            if (this.mSecurityTypeIndex == 1) {
                this.mPassword.setText(this.mWifiConfig.preSharedKey);
            }
        }
        mChannel.setAdapter(channelAdapter);
        mChannel.setOnItemSelectedListener(new OnItemSelectedListener() {
            boolean mInit = true;

            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (this.mInit) {
                    this.mInit = false;
                    mChannel.setSelection(WifiApDialog.this.mBandIndex);
                    return;
                }
                WifiApDialog.this.mBandIndex = position;
                WifiApDialog.this.mWifiConfig.apBand = WifiApDialog.this.mBandIndex;
                Log.i("WifiApDialog", "config on channelIndex : " + WifiApDialog.this.mBandIndex + " Band: " + WifiApDialog.this.mWifiConfig.apBand);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.mSsid.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(32)});
        this.mSsid.addTextChangedListener(this);
        this.mPassword.addTextChangedListener(this);
        this.showPassword = (CheckBox) this.mView.findViewById(2131886368);
        this.showPassword.setOnClickListener(this);
        mSecurity.setOnItemSelectedListener(this);
        super.onCreate(savedInstanceState);
        showSecurityFields();
        validate();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        int i;
        super.onRestoreInstanceState(savedInstanceState);
        EditText editText = this.mPassword;
        if (((CheckBox) this.mView.findViewById(2131886368)).isChecked()) {
            i = 144;
        } else {
            i = 128;
        }
        editText.setInputType(i | 1);
    }

    private void validate() {
        String mSsidString = this.mSsid.getText().toString();
        if ((this.mSsid == null || this.mSsid.length() != 0) && ((this.mSecurityTypeIndex != 1 || this.mPassword.length() >= 8) && (this.mSsid == null || Charset.forName("UTF-8").encode(mSsidString).limit() <= 32))) {
            getButton(-1).setEnabled(true);
        } else {
            getButton(-1).setEnabled(false);
        }
    }

    public void onClick(View view) {
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
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        this.mSecurityTypeIndex = position;
        showSecurityFields();
        validate();
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void showSecurityFields() {
        if (this.mSecurityTypeIndex == 0) {
            this.mView.findViewById(2131886320).setVisibility(8);
        } else {
            this.mView.findViewById(2131886320).setVisibility(0);
        }
    }
}
