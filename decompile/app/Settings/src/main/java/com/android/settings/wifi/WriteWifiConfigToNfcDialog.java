package com.android.settings.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.IOException;

class WriteWifiConfigToNfcDialog extends AlertDialog implements TextWatcher, OnClickListener, OnCheckedChangeListener {
    private static final String TAG = WriteWifiConfigToNfcDialog.class.getName().toString();
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    Activity activity;
    private boolean isNfcReaderMode;
    private Button mCancelButton;
    private Context mContext;
    private TextView mLabelView;
    private int mNetworkId;
    private Handler mOnTextChangedHandler = new Handler();
    private CheckBox mPasswordCheckBox;
    private TextView mPasswordView;
    private ProgressBar mProgressBar;
    private int mSecurity;
    private Button mSubmitButton;
    private View mView;
    private final WakeLock mWakeLock;
    private WifiManager mWifiManager;
    private String mWpsNfcConfigurationToken;
    NfcAdapter nfcAdapter;

    WriteWifiConfigToNfcDialog(Context context, int networkId, int security, WifiManager wifiManager) {
        super(context);
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "WriteWifiConfigToNfcDialog:wakeLock");
        this.mNetworkId = networkId;
        this.mSecurity = security;
        this.mWifiManager = wifiManager;
    }

    WriteWifiConfigToNfcDialog(Context context, Bundle savedState, WifiManager wifiManager) {
        super(context);
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "WriteWifiConfigToNfcDialog:wakeLock");
        this.mNetworkId = savedState.getInt("network_id");
        this.mSecurity = savedState.getInt("security");
        this.mWifiManager = wifiManager;
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mView = getLayoutInflater().inflate(2130969289, null);
        setView(this.mView);
        setInverseBackgroundForced(true);
        setTitle(2131628552);
        setCancelable(true);
        setButton(-3, this.mContext.getResources().getString(2131626683), (DialogInterface.OnClickListener) null);
        setButton(-2, this.mContext.getResources().getString(17039360), (DialogInterface.OnClickListener) null);
        this.mPasswordView = (TextView) this.mView.findViewById(2131887420);
        this.mLabelView = (TextView) this.mView.findViewById(2131887602);
        this.mPasswordView.addTextChangedListener(this);
        this.mPasswordCheckBox = (CheckBox) this.mView.findViewById(2131886368);
        this.mPasswordCheckBox.setOnCheckedChangeListener(this);
        this.mProgressBar = (ProgressBar) this.mView.findViewById(2131886426);
        super.onCreate(savedInstanceState);
        this.mSubmitButton = getButton(-3);
        this.mSubmitButton.setOnClickListener(this);
        this.mSubmitButton.setEnabled(false);
        this.mCancelButton = getButton(-2);
        this.isNfcReaderMode = false;
    }

    public void onClick(View v) {
        String passwordLength;
        this.mWakeLock.acquire();
        String password = this.mPasswordView.getText().toString();
        String wpsNfcConfigurationToken = this.mWifiManager.getWpsNfcConfigurationToken(this.mNetworkId);
        String passwordHex = byteArrayToHexString(password.getBytes());
        if (password.length() >= 16) {
            passwordLength = Integer.toString(password.length(), 16);
        } else {
            passwordLength = "0" + Character.forDigit(password.length(), 16);
        }
        if (wpsNfcConfigurationToken.contains(String.format("102700%s%s", new Object[]{passwordLength, passwordHex}).toUpperCase())) {
            this.mWpsNfcConfigurationToken = wpsNfcConfigurationToken;
            this.activity = getOwnerActivity();
            this.nfcAdapter = NfcAdapter.getDefaultAdapter(this.activity);
            this.nfcAdapter.enableReaderMode(this.activity, new ReaderCallback() {
                public void onTagDiscovered(Tag tag) {
                    WriteWifiConfigToNfcDialog.this.handleWriteNfcEvent(tag);
                }
            }, 31, null);
            this.mPasswordView.setVisibility(8);
            this.mPasswordCheckBox.setVisibility(8);
            this.mSubmitButton.setVisibility(8);
            ((InputMethodManager) getOwnerActivity().getSystemService("input_method")).hideSoftInputFromWindow(this.mPasswordView.getWindowToken(), 0);
            this.mLabelView.setText(2131626684);
            this.mView.findViewById(2131887511).setTextAlignment(4);
            this.mProgressBar.setVisibility(0);
            return;
        }
        this.mLabelView.setText(2131626685);
    }

    public void saveState(Bundle state) {
        state.putInt("network_id", this.mNetworkId);
        state.putInt("security", this.mSecurity);
    }

    private void handleWriteNfcEvent(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            setViewText(this.mLabelView, 2131626688);
            Log.e(TAG, "Tag does not support NDEF");
        } else if (ndef.isWritable()) {
            NdefRecord record = NdefRecord.createMime("application/vnd.wfa.wsc", hexStringToByteArray(this.mWpsNfcConfigurationToken));
            try {
                ndef.connect();
                ndef.writeNdefMessage(new NdefMessage(record, new NdefRecord[0]));
                getOwnerActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        WriteWifiConfigToNfcDialog.this.mProgressBar.setVisibility(8);
                    }
                });
                setViewText(this.mLabelView, 2131626686);
                setViewText(this.mCancelButton, 17040775);
            } catch (IOException e) {
                setViewText(this.mLabelView, 2131626687);
                Log.e(TAG, "Unable to write Wi-Fi config to NFC tag.", e);
                return;
            } catch (FormatException e2) {
                setViewText(this.mLabelView, 2131626687);
                Log.e(TAG, "Unable to write Wi-Fi config to NFC tag.", e2);
                return;
            }
        } else {
            setViewText(this.mLabelView, 2131626688);
            Log.e(TAG, "Tag is not writable");
        }
        this.isNfcReaderMode = true;
    }

    public void dismiss() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        if (this.isNfcReaderMode) {
            this.nfcAdapter.disableReaderMode(this.activity);
            Log.d(TAG, " /dismiss/nfcAdapter.disableReaderMode.");
        }
        super.dismiss();
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.mOnTextChangedHandler.post(new Runnable() {
            public void run() {
                WriteWifiConfigToNfcDialog.this.enableSubmitIfAppropriate();
            }
        });
    }

    private void enableSubmitIfAppropriate() {
        boolean z = true;
        if (this.mPasswordView == null) {
            this.mSubmitButton.setEnabled(false);
        } else if (this.mSecurity == 1) {
            r2 = this.mSubmitButton;
            if (this.mPasswordView.length() <= 0) {
                z = false;
            }
            r2.setEnabled(z);
        } else if (this.mSecurity == 2) {
            r2 = this.mSubmitButton;
            if (this.mPasswordView.length() < 8) {
                z = false;
            }
            r2.setEnabled(z);
        }
    }

    private void setViewText(final TextView view, final int resid) {
        getOwnerActivity().runOnUiThread(new Runnable() {
            public void run() {
                view.setText(resid);
            }
        });
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int i;
        int pos = this.mPasswordView.getSelectionEnd();
        TextView textView = this.mPasswordView;
        if (isChecked) {
            i = 144;
        } else {
            i = 128;
        }
        textView.setInputType(i | 1);
        if (pos >= 0) {
            ((EditText) this.mPasswordView).setSelection(pos);
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable s) {
    }
}
