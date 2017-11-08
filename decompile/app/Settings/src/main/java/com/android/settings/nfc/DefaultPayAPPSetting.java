package com.android.settings.nfc;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.PreferenceCategory;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.nfc.NfcPayPreference.NfcPayChangeListener;
import com.android.settings.nfc.PaymentBackend.Callback;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import com.android.settings.wifi.HwCustWifiConfigController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPayAPPSetting extends SettingsPreferenceFragment implements Callback, NfcPayChangeListener {
    private static final boolean NFC_PAYMENT_OPENAPP = "true".equals(SystemProperties.get("ro.config.nfc_ce_transevt", "false"));
    private static final boolean mNfcMsimce = SystemProperties.getBoolean("ro.config.hw_nfc_msimce", false);
    private static final Map<String, Boolean> trustInfoMap = new HashMap();
    private Context mContext;
    private IntentFilter mIntentFilter;
    private String[] mMsg;
    private NfcAdapter mNfcAdapter;
    private String[] mNfcPayEntries;
    private PreferenceCategory mNfcPayOtherListPreference;
    private PreferenceCategory mNfcPaySecurityListPreference;
    private PaymentBackend mPaymentBackend;
    private List<NfcPayPreference> mPreferList;
    private ProgressDialog mProgressDialog = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.huawei.android.nfc.SWITCH_CE_STATE".equals(action)) {
                DefaultPayAPPSetting.this.handleNfcSwitchResult(intent.getIntExtra("com.huawei.android.nfc.CE_SELECTED_STATE", -1));
            }
            if (DefaultPayAPPSetting.NFC_PAYMENT_OPENAPP && "org.simalliance.openmobileapi.service.ACTION_CHECK_X509_RESULT".equals(action)) {
                DefaultPayAPPSetting.this.updateSecurityListPreference(intent);
            }
        }
    };
    private TelephonyManager mTelephonyManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230829);
        this.mContext = getActivity();
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        this.mPaymentBackend = new PaymentBackend(this.mContext);
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        this.mNfcPayEntries = new String[2];
        this.mMsg = new String[this.mNfcPayEntries.length];
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("com.huawei.android.nfc.SWITCH_CE_STATE");
        if (NFC_PAYMENT_OPENAPP) {
            this.mIntentFilter.addAction("org.simalliance.openmobileapi.service.ACTION_CHECK_X509_RESULT");
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(2130968879, contentRoot, false);
        ((ImageView) emptyView.findViewById(2131886560)).setImageResource(2130838390);
        ((TextView) emptyView.findViewById(2131886561)).setText(2131628493);
        contentRoot.addView(emptyView);
        setEmptyView(emptyView);
        this.mNfcPaySecurityListPreference = (PreferenceCategory) findPreference("using_security_chip_apps");
        this.mNfcPayOtherListPreference = (PreferenceCategory) findPreference("other_payment_apps");
        this.mPreferList = new ArrayList();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null && mNfcMsimce) {
            this.mNfcPayEntries[0] = savedInstanceState.getString("card1", "");
            this.mNfcPayEntries[1] = savedInstanceState.getString("card2", "");
        }
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        if (this.mContext != null) {
            this.mPaymentBackend.onResume();
            this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
            refresh();
        }
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mContext != null) {
            this.mPaymentBackend.onPause();
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        if (NFC_PAYMENT_OPENAPP) {
            trustInfoMap.clear();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("card1", this.mNfcPayEntries[0]);
        outState.putString("card2", this.mNfcPayEntries[1]);
    }

    public void onPaymentAppsChanged() {
        refresh();
    }

    public void onNfcPayChanged(NfcPayPreference nfcPayPref) {
        Log.d("AndroidBeam", "DefaultPayAPPSetting onNfcPayChanged");
        if (nfcPayPref != null) {
            updatePreference(nfcPayPref);
            PaymentAppInfo appInfo = nfcPayPref.getNfcPayment();
            if (!(appInfo == null || appInfo.isOnHost || !mNfcMsimce)) {
                showDialog(1);
            }
        }
    }

    private void refresh() {
        List<PaymentAppInfo> appInfos = this.mPaymentBackend.getPaymentAppInfos();
        this.mPreferList.clear();
        this.mNfcPayOtherListPreference.removeAll();
        this.mNfcPaySecurityListPreference.removeAll();
        if (appInfos == null || appInfos.size() <= 0) {
            removePreference("using_security_chip_apps");
            removePreference("other_payment_apps");
        } else {
            int index = 0;
            int indexOther = 0;
            for (PaymentAppInfo appInfo : appInfos) {
                if (appInfo != null) {
                    NfcPayPreference pref = new NfcPayPreference(appInfo, this.mPaymentBackend, this.mContext);
                    pref.setTitle(appInfo.label);
                    pref.setLayoutResource(2130968881);
                    Log.d("AndroidBeam", "DefaultPayAPPSetting refresh found " + appInfo.label);
                    if (appInfo.isOnHost) {
                        int indexOther2 = indexOther + 1;
                        pref.setOrder(indexOther);
                        this.mNfcPayOtherListPreference.addPreference(pref);
                        indexOther = indexOther2;
                    } else {
                        pref.setWidgetLayoutResource(2130968998);
                        int index2 = index + 1;
                        pref.setOrder(index);
                        this.mNfcPaySecurityListPreference.addPreference(pref);
                        if (NFC_PAYMENT_OPENAPP) {
                            checkCertificatesFromUICC(appInfo);
                            index = index2;
                        } else {
                            index = index2;
                        }
                    }
                    pref.setListener(this);
                    this.mPreferList.add(pref);
                }
            }
            if (index == 0) {
                removePreference("using_security_chip_apps");
            }
            if (indexOther == 0) {
                removePreference("other_payment_apps");
            }
        }
        updatePayEntries();
    }

    private void updatePreference(NfcPayPreference selPref) {
        if (selPref != null) {
            for (NfcPayPreference pref : this.mPreferList) {
                if (!pref.equals(selPref)) {
                    pref.clearDefault(pref.getNfcPayment());
                    pref.refresh();
                }
            }
        }
    }

    private void updatePayEntries() {
        this.mMsg[0] = this.mTelephonyManager.getNetworkOperatorName(0);
        this.mMsg[1] = this.mTelephonyManager.getNetworkOperatorName(1);
        if (getActivity() != null && isAdded()) {
            if (TextUtils.isEmpty(this.mMsg[0])) {
                this.mNfcPayEntries[0] = getResources().getString(2131627386);
            } else {
                this.mNfcPayEntries[0] = getResources().getString(2131627386) + ":" + this.mMsg[0];
            }
            if (TextUtils.isEmpty(this.mMsg[1])) {
                this.mNfcPayEntries[1] = getResources().getString(2131627387);
            } else {
                this.mNfcPayEntries[1] = getResources().getString(2131627387) + ":" + this.mMsg[1];
            }
            if (SystemProperties.getBoolean("ro.config.nfc_hasese", false)) {
                this.mNfcPayEntries[1] = getResources().getString(2131628498);
            }
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        if (dialogId != 1) {
            return super.onCreateDialog(dialogId);
        }
        int i = this.mNfcAdapter.getSelectedCardEmulation();
        int index = 0;
        if (i == 1) {
            index = 0;
        } else if (i == 2) {
            index = 1;
        }
        return new Builder(this.mContext).setTitle(2131628489).setSingleChoiceItems(this.mNfcPayEntries, index, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int current = DefaultPayAPPSetting.this.mNfcAdapter.getSelectedCardEmulation();
                int value2set = 0;
                Log.d("AndroidBeam", "in Dialog Select = " + which + " current = " + current);
                if (which == 0) {
                    value2set = 1;
                } else if (which == 1) {
                    value2set = 2;
                }
                if (value2set != current) {
                    DefaultPayAPPSetting.this.mNfcAdapter.selectCardEmulation(value2set);
                    DefaultPayAPPSetting.this.createDialog(2);
                    DefaultPayAPPSetting.this.mProgressDialog.show();
                }
                dialog.dismiss();
            }
        }).setNegativeButton(2131624572, null).create();
    }

    private void createDialog(int dialogId) {
        switch (dialogId) {
            case 2:
                this.mProgressDialog = new ProgressDialog(this.mContext);
                this.mProgressDialog.setMessage(getString(2131628483));
                this.mProgressDialog.setCancelable(false);
                return;
            default:
                Log.d("AndroidBeam", "received unknow event, just return");
                return;
        }
    }

    private void handleNfcSwitchResult(int Recult) {
        if (mNfcMsimce) {
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            if (Recult == -1) {
                Toast.makeText(this.mContext, 2131628479, 1).show();
            }
        }
    }

    protected int getMetricsCategory() {
        return 69;
    }

    public void checkCertificatesFromUICC(PaymentAppInfo appInfo) {
        if (appInfo.componentName != null) {
            String pkg = appInfo.componentName.getPackageName();
            if (pkg != null && !trustInfoMap.containsKey(pkg)) {
                Intent intent = new Intent();
                intent.setAction("org.simalliance.openmobileapi.service.ACTION_CHECK_X509");
                intent.setPackage("org.simalliance.openmobileapi.service");
                intent.putExtra("org.simalliance.openmobileapi.service.EXTRA_SE_NAME", HwCustWifiConfigController.EAP_METHOD_SIM);
                intent.putExtra("org.simalliance.openmobileapi.service.EXTRA_PKG", appInfo.componentName.getPackageName());
                this.mContext.sendBroadcast(intent, "org.simalliance.openmobileapi.service.permission.CHECK_X509");
                trustInfoMap.put(pkg, Boolean.valueOf(false));
            }
        }
    }

    private void updateSecurityListPreference(Intent intent) {
        String pkgName = intent.getStringExtra("org.simalliance.openmobileapi.service.extra.EXTRA_PKG");
        boolean istrusted = intent.getBooleanExtra("org.simalliance.openmobileapi.service.extra.EXTRA_RESULT", false);
        if (pkgName != null) {
            trustInfoMap.put(pkgName, Boolean.valueOf(istrusted));
            for (NfcPayPreference pref : this.mPreferList) {
                if (pref.getNfcPayment().componentName != null && pkgName.equals(pref.getNfcPayment().componentName.getPackageName())) {
                    pref.setTrustSummary(istrusted);
                }
            }
        }
    }
}
