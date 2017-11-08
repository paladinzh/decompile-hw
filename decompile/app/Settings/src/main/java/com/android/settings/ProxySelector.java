package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Proxy;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.settings.SettingsPreferenceFragment.SettingsDialogFragment;

public class ProxySelector extends InstrumentedFragment implements DialogCreatable {
    Button mClearButton;
    OnClickListener mClearHandler = new OnClickListener() {
        public void onClick(View v) {
            ProxySelector.this.mHostnameField.setText("");
            ProxySelector.this.mPortField.setText("");
            ProxySelector.this.mExclusionListField.setText("");
        }
    };
    Button mDefaultButton;
    OnClickListener mDefaultHandler = new OnClickListener() {
        public void onClick(View v) {
            ProxySelector.this.populateFields();
        }
    };
    private SettingsDialogFragment mDialogFragment;
    EditText mExclusionListField;
    EditText mHostnameField;
    Button mOKButton;
    OnClickListener mOKHandler = new OnClickListener() {
        public void onClick(View v) {
            if (ProxySelector.this.saveToDb()) {
                ProxySelector.this.getActivity().onBackPressed();
            }
        }
    };
    OnFocusChangeListener mOnFocusChangeHandler = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                Selection.selectAll((Spannable) ((TextView) v).getText());
            }
        }
    };
    EditText mPortField;
    private View mView;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getActivity().finish();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mView = inflater.inflate(2130969042, container, false);
        initView(this.mView);
        populateFields();
        return this.mView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean userSetGlobalProxy = ((DevicePolicyManager) getActivity().getSystemService("device_policy")).getGlobalProxyAdmin() == null;
        this.mHostnameField.setEnabled(userSetGlobalProxy);
        this.mPortField.setEnabled(userSetGlobalProxy);
        this.mExclusionListField.setEnabled(userSetGlobalProxy);
        this.mOKButton.setEnabled(userSetGlobalProxy);
        this.mClearButton.setEnabled(userSetGlobalProxy);
        this.mDefaultButton.setEnabled(userSetGlobalProxy);
    }

    public Dialog onCreateDialog(int id) {
        if (id != 0) {
            return null;
        }
        int msgId = validate(this.mHostnameField.getText().toString().trim(), this.mPortField.getText().toString().trim(), this.mExclusionListField.getText().toString().trim());
        if (msgId <= 0) {
            msgId = 2131627268;
        }
        return new Builder(getActivity()).setTitle(2131624494).setPositiveButton(2131624495, null).setMessage(getActivity().getString(msgId)).create();
    }

    private void showDialog(int dialogId) {
        if (this.mDialogFragment != null) {
            Log.e("ProxySelector", "Old dialog fragment not null!");
        }
        this.mDialogFragment = new SettingsDialogFragment(this, dialogId);
        this.mDialogFragment.show(getActivity().getFragmentManager(), Integer.toString(dialogId));
    }

    private void initView(View view) {
        this.mHostnameField = (EditText) view.findViewById(2131887021);
        this.mHostnameField.setOnFocusChangeListener(this.mOnFocusChangeHandler);
        this.mPortField = (EditText) view.findViewById(2131887022);
        this.mPortField.setOnClickListener(this.mOKHandler);
        this.mPortField.setOnFocusChangeListener(this.mOnFocusChangeHandler);
        this.mExclusionListField = (EditText) view.findViewById(2131887023);
        this.mExclusionListField.setOnFocusChangeListener(this.mOnFocusChangeHandler);
        this.mOKButton = (Button) view.findViewById(2131887024);
        this.mOKButton.setOnClickListener(this.mOKHandler);
        this.mClearButton = (Button) view.findViewById(2131887025);
        this.mClearButton.setOnClickListener(this.mClearHandler);
        this.mDefaultButton = (Button) view.findViewById(2131887026);
        this.mDefaultButton.setOnClickListener(this.mDefaultHandler);
    }

    void populateFields() {
        Activity activity = getActivity();
        String hostname = "";
        int port = -1;
        String exclList = "";
        ProxyInfo proxy = ((ConnectivityManager) getActivity().getSystemService("connectivity")).getGlobalProxy();
        if (proxy != null) {
            hostname = proxy.getHost();
            port = proxy.getPort();
            exclList = proxy.getExclusionListAsString();
        }
        if (hostname == null) {
            hostname = "";
        }
        this.mHostnameField.setText(hostname);
        this.mPortField.setText(port == -1 ? "" : Integer.toString(port));
        this.mExclusionListField.setText(exclList);
        Intent intent = activity.getIntent();
        if (intent == null) {
            Log.e("ProxySelector", "populateFields()-->intent is null!");
            return;
        }
        String buttonLabel = intent.getStringExtra("button-label");
        if (!TextUtils.isEmpty(buttonLabel)) {
            this.mOKButton.setText(buttonLabel);
        }
        String title = intent.getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            activity.setTitle(title);
        }
    }

    public static int validate(String hostname, String port, String exclList) {
        switch (Proxy.validate(hostname, port, exclList)) {
            case 0:
                return 0;
            case 1:
                return 2131624499;
            case 2:
                return 2131624496;
            case 3:
                return 2131624498;
            case 4:
                return 2131624500;
            case 5:
                return 2131624497;
            default:
                Log.e("ProxySelector", "Unknown proxy settings error");
                return -1;
        }
    }

    boolean saveToDb() {
        String hostname = this.mHostnameField.getText().toString().trim();
        String portStr = this.mPortField.getText().toString().trim();
        String exclList = this.mExclusionListField.getText().toString().trim();
        int port = 0;
        if (validate(hostname, portStr, exclList) != 0) {
            showDialog(0);
            return false;
        }
        if (portStr.length() > 0) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        ((ConnectivityManager) getActivity().getSystemService("connectivity")).setGlobalProxy(new ProxyInfo(hostname, port, exclList));
        return true;
    }

    protected int getMetricsCategory() {
        return 82;
    }
}
