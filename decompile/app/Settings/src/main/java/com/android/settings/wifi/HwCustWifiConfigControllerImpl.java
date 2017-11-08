package com.android.settings.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settingslib.wifi.AccessPoint;
import java.util.Arrays;

public class HwCustWifiConfigControllerImpl extends HwCustWifiConfigController implements OnItemSelectedListener {
    private static final int HISI_WAPI = 0;
    private static final int INVALID_WAPI = -1;
    private static final String KEYSTORE_SPACE = "keystore://";
    private static final int QUALCOMM_WAPI = 1;
    private static final String TAG = "HwCustWifiConfigControllerImpl";
    private static final int[] WAPI_PSK_TYPE_VALUES = new int[]{0, 1};
    private static final String WAPI_TYPE = "wapi_type";
    private static final int unspecifiedCertIndex = 0;
    private String isOnlyShowDialog = null;
    private String[] isOnlyShowDialogEntries = null;
    private TextView mPasswordView;
    private Spinner mWapiAsCert;
    private int mWapiCertIndex;
    private Spinner mWapiPskType;
    private int mWapiType;
    private Spinner mWapiUserCert;

    public HwCustWifiConfigControllerImpl(WifiConfigController wifiConfigController) {
        super(wifiConfigController);
    }

    boolean checkPasswordForWapiPsk(TextView passwordView) {
        if (passwordView == null) {
            return false;
        }
        if (passwordView.length() < 8) {
            return true;
        }
        return (this.mWapiPskType == null || this.mWapiPskType.getSelectedItemPosition() != 1 || passwordView.length() % 2 == 0) ? false : true;
    }

    boolean checkPasswordForWapiCert() {
        if ((this.mWapiAsCert == null || this.mWapiAsCert.getSelectedItemPosition() != 0) && (this.mWapiUserCert == null || this.mWapiUserCert.getSelectedItemPosition() != 0)) {
            return false;
        }
        return true;
    }

    public boolean isPasswordInvalid(int mAccessPointSecurity, TextView mPasswordView) {
        if (mPasswordView == null || ((mAccessPointSecurity != 1 || mPasswordView.length() >= 5) && ((mAccessPointSecurity != 4 || !checkPasswordForWapiPsk(mPasswordView)) && (mAccessPointSecurity != 5 || !checkPasswordForWapiCert())))) {
            return false;
        }
        return true;
    }

    private void makeConfig(WifiConfiguration config, String password, int wapiType) {
        if (password.matches("[0-9A-Fa-f]{64}")) {
            if (wapiType == 0) {
                config.preSharedKey = password;
            } else if (1 == wapiType) {
                config.wapiPskQualcomm = password;
            }
        } else if (wapiType == 0) {
            config.preSharedKey = '\"' + password + '\"';
        } else if (1 == wapiType) {
            config.wapiPskQualcomm = '\"' + password + '\"';
        }
    }

    public boolean getConfig(int mAccessPointSecurity, WifiConfiguration config, View mView) {
        this.mWapiType = Global.getInt(this.mWifiConfigController.mConfigUi.getContext().getContentResolver(), WAPI_TYPE, INVALID_WAPI);
        switch (mAccessPointSecurity) {
            case 4:
                if (this.mWapiType != INVALID_WAPI) {
                    if (this.mWapiType == 0) {
                        config.allowedKeyManagement.set(6);
                        config.wapiPskTypeBcm = WAPI_PSK_TYPE_VALUES[this.mWapiPskType.getSelectedItemPosition()];
                    } else if (this.mWapiType == 1) {
                        config.allowedKeyManagement.set(8);
                        config.wapiPskTypeQualcomm = WAPI_PSK_TYPE_VALUES[this.mWapiPskType.getSelectedItemPosition()];
                    }
                    this.mPasswordView = (TextView) mView.findViewById(2131887420);
                    if (this.mPasswordView.length() != 0) {
                        makeConfig(config, this.mPasswordView.getText().toString(), this.mWapiType);
                        break;
                    }
                }
                return false;
                break;
            case 5:
                if (this.mWapiType != INVALID_WAPI) {
                    String str;
                    if (this.mWapiType != 0) {
                        if (this.mWapiType == 1) {
                            config.allowedKeyManagement.set(9);
                            if (this.mWapiAsCert.getSelectedItemPosition() == 0) {
                                str = "";
                            } else {
                                str = AccessPoint.convertToQuotedString("keystore://WAPIAS_" + ((String) this.mWapiAsCert.getSelectedItem()));
                            }
                            config.wapiAsCertQualcomm = str;
                            if (this.mWapiUserCert.getSelectedItemPosition() == 0) {
                                str = "";
                            } else {
                                str = AccessPoint.convertToQuotedString("keystore://WAPIUSR_" + ((String) this.mWapiUserCert.getSelectedItem()));
                            }
                            config.wapiUserCertQualcomm = str;
                            break;
                        }
                    }
                    config.allowedKeyManagement.set(7);
                    this.mWapiCertIndex = 1;
                    config.wapiCertIndexBcm = this.mWapiCertIndex;
                    if (this.mWapiAsCert.getSelectedItemPosition() == 0) {
                        str = "";
                    } else {
                        str = AccessPoint.convertToQuotedString("keystore://WAPIAS_" + ((String) this.mWapiAsCert.getSelectedItem()));
                    }
                    config.wapiAsCertBcm = str;
                    if (this.mWapiUserCert.getSelectedItemPosition() == 0) {
                        str = "";
                    } else {
                        str = AccessPoint.convertToQuotedString("keystore://WAPIUSR_" + ((String) this.mWapiUserCert.getSelectedItem()));
                    }
                    config.wapiUserCertBcm = str;
                    break;
                }
                return false;
                break;
            default:
                return false;
        }
        return true;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!(parent == this.mWapiPskType || parent == this.mWapiUserCert)) {
            if (parent != this.mWapiAsCert) {
                return;
            }
        }
        this.mWifiConfigController.showIpConfigFields();
        this.mWifiConfigController.enableSubmitIfAppropriate();
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void showWapiSecurityFields(int mAccessPointSecurity, View mView) {
        WifiConfiguration config;
        this.mWapiType = Global.getInt(this.mWifiConfigController.mConfigUi.getContext().getContentResolver(), WAPI_TYPE, INVALID_WAPI);
        AccessPoint mAccessPoint = this.mWifiConfigController.getAccessPoint();
        if (mAccessPointSecurity != 4) {
            mView.findViewById(2131887507).setVisibility(8);
        } else {
            mView.findViewById(2131887507).setVisibility(0);
            this.mWapiPskType = (Spinner) mView.findViewById(2131887508);
            this.mWapiPskType.setOnItemSelectedListener(this);
            if (!(mAccessPoint == null || mAccessPoint.getNetworkId() == INVALID_WAPI)) {
                config = mAccessPoint.getConfig();
                if (this.mWapiType == 0) {
                    this.mWapiPskType.setSelection(config.wapiPskTypeBcm);
                } else if (this.mWapiType == 1) {
                    this.mWapiPskType.setSelection(config.wapiPskTypeQualcomm);
                }
            }
        }
        if (mAccessPointSecurity != 5) {
            mView.findViewById(2131887512).setVisibility(8);
            return;
        }
        mView.findViewById(2131887487).setVisibility(8);
        mView.findViewById(2131887512).setVisibility(0);
        this.mWapiAsCert = (Spinner) mView.findViewById(2131887513);
        this.mWapiUserCert = (Spinner) mView.findViewById(2131887514);
        this.mWapiUserCert.setOnItemSelectedListener(this);
        this.mWapiAsCert.setOnItemSelectedListener(this);
        loadCertificates(this.mWapiAsCert, "WAPIAS_");
        loadCertificates(this.mWapiUserCert, "WAPIUSR_");
        if (mAccessPoint != null && mAccessPoint.getNetworkId() != INVALID_WAPI) {
            config = mAccessPoint.getConfig();
            if (this.mWapiType == 0) {
                this.mWapiCertIndex = config.wapiCertIndexBcm;
                this.mWifiConfigController.setCertificate(this.mWapiAsCert, "WAPIAS_", config.wapiAsCertBcm, KEYSTORE_SPACE);
                this.mWifiConfigController.setCertificate(this.mWapiUserCert, "WAPIUSR_", config.wapiUserCertBcm, KEYSTORE_SPACE);
            } else if (this.mWapiType == 1) {
                this.mWifiConfigController.setCertificate(this.mWapiAsCert, "WAPIAS_", config.wapiAsCertQualcomm, KEYSTORE_SPACE);
                this.mWifiConfigController.setCertificate(this.mWapiUserCert, "WAPIUSR_", config.wapiUserCertQualcomm, KEYSTORE_SPACE);
            }
        }
    }

    private void loadCertificates(Spinner spinner, String prefix) {
        String[] certs;
        Context context = this.mWifiConfigController.mConfigUi.getContext();
        Object certs2 = null;
        String unspecifiedCert = context.getString(2131624990);
        try {
            certs2 = KeyStore.getInstance().list(prefix, 1010);
        } catch (Exception e) {
            Log.e(TAG, "loadCertificates catch KeyStore.getInstance().saw() exception");
        }
        if (certs2 == null || certs2.length == 0) {
            certs = new String[]{unspecifiedCert};
        } else {
            String[] array = new String[(certs2.length + 1)];
            array[0] = unspecifiedCert;
            System.arraycopy(certs2, 0, array, 1, certs2.length);
            certs = array;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(context, 17367048, certs);
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter(adapter);
    }

    public int getEapMethodDefault(Spinner spinner, Context context) {
        if (checkEapSimDefault(context)) {
            return setSpinerSelection(spinner, HwCustWifiConfigController.EAP_METHOD_SIM);
        }
        return 0;
    }

    public boolean checkEapSimDefault(Context context) {
        if ("true".equals(SystemProperties.get("ro.config.hw_eapsim", "false"))) {
            boolean isSetEapsimAlways;
            if (Systemex.getInt(context.getContentResolver(), "hw_eap_sim_always", 0) == 1) {
                isSetEapsimAlways = true;
            } else {
                isSetEapsimAlways = false;
            }
            if (isSetEapsimAlways) {
                return true;
            }
            String plmnsConfig = Systemex.getString(context.getContentResolver(), "plmn_eap_sim_default");
            if (plmnsConfig == null) {
                plmnsConfig = "45400,45402,45403,45404,45405,45406,45407,45410,45412,45413,45414,45415,45416,45417,45418,45419,45420,52501,52502,52503,52505,52507";
            }
            String[] plmns = plmnsConfig.split(",");
            String operatorNumeric = TelephonyManager.getDefault().getSimOperator();
            for (String plmn : plmns) {
                if (plmn != null && plmn.equals(operatorNumeric)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int setSpinerSelection(Spinner spinner, String name) {
        if (name != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = adapter.getCount() + INVALID_WAPI; i >= 0; i += INVALID_WAPI) {
                if (name.equals(adapter.getItem(i))) {
                    return i;
                }
            }
        }
        return 0;
    }

    public boolean isNotEditWifi(AccessPoint mSelectedAccessPoint, Context context) {
        String wifi_notdel_notedit = Systemex.getString(context.getContentResolver(), "wifi_notdel_notedit");
        if (wifi_notdel_notedit != null && wifi_notdel_notedit.contains(mSelectedAccessPoint.getSsidStr()) && mSelectedAccessPoint.getSecurity() == 3) {
            return true;
        }
        return false;
    }

    public void notAllowModifyWifi(View view, AccessPoint mAccessPoint) {
        String notModifyWifi = SystemProperties.get("ro.config.hw_not_modify_wifi");
        if (!(notModifyWifi == null || mAccessPoint == null || mAccessPoint.getSecurity() != 3)) {
            for (String ssid : notModifyWifi.split(",")) {
                if (ssid.equals(mAccessPoint.getSsidStr())) {
                    view.findViewById(2131887490).setEnabled(false);
                    view.findViewById(2131887501).setVisibility(8);
                    view.findViewById(2131887511).setVisibility(8);
                    view.findViewById(2131887461).setVisibility(8);
                }
            }
        }
    }

    public boolean isShowOnlyDialog(AccessPoint mAccessPoint, Context context) {
        this.isOnlyShowDialog = Systemex.getString(context.getContentResolver(), "hw_eap_show_dialog");
        if (!TextUtils.isEmpty(this.isOnlyShowDialog)) {
            this.isOnlyShowDialogEntries = this.isOnlyShowDialog.split(";");
        }
        if (this.isOnlyShowDialogEntries == null || mAccessPoint == null) {
            return false;
        }
        boolean isSecurityEap = mAccessPoint.getSecurity() == 3;
        if (!Arrays.asList(this.isOnlyShowDialogEntries).contains(mAccessPoint.getSsidStr())) {
            isSecurityEap = false;
        }
        return isSecurityEap;
    }

    public void customizeFields(View mView, TextView mPasswordView) {
        if (mView != null) {
            mView.findViewById(2131887511).setVisibility(8);
            if (mPasswordView != null) {
                mPasswordView.setText("");
            }
            ((CheckBox) mView.findViewById(2131887463)).setVisibility(8);
            mView.findViewById(2131887515).setVisibility(8);
        }
    }
}
