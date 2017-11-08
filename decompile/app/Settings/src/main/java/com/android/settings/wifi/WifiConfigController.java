package com.android.settings.wifi;

import android.content.Context;
import android.content.res.Resources;
import android.net.DhcpInfo;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.System;
import android.security.KeyStore;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.settings.LinkifyUtils;
import com.android.settings.ProxySelector;
import com.android.settings.Utils;
import com.android.settings.wifi.cmcc.WifiExt;
import com.android.settings.wifi.qrcode.QrcodeUtil;
import com.android.settingslib.wifi.AccessPoint;
import com.huawei.cust.HwCustUtils;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class WifiConfigController extends WifiConfigControllerHwBase implements TextWatcher, OnItemSelectedListener, OnCheckedChangeListener, OnEditorActionListener, OnKeyListener {
    private final AccessPoint mAccessPoint;
    private TextView mDns1View;
    private TextView mDns2View;
    private String mDoNotProvideEapUserCertString;
    private String mDoNotValidateEapServerString;
    private TextView mEapAnonymousView;
    private Spinner mEapCaCertSpinner;
    private TextView mEapDomainView;
    private TextView mEapIdentityView;
    private Spinner mEapMethodSpinner;
    private Spinner mEapUserCertSpinner;
    private TextView mGatewayView;
    private ProxyInfo mHttpProxy;
    private HwCustWifiConfigController mHwCustWifiConfigController;
    private TextView mIpAddressView;
    private IpAssignment mIpAssignment;
    private Spinner mIpSettingsSpinner;
    private String[] mLevels;
    private int mMode;
    private String mMultipleCertSetString;
    private TextView mNetworkPrefixLengthView;
    private ArrayAdapter<String> mPhase2Adapter;
    private final ArrayAdapter<String> mPhase2FullAdapter;
    private final ArrayAdapter<String> mPhase2PeapAdapter;
    private Spinner mPhase2Spinner;
    private TextView mProxyExclusionListView;
    private TextView mProxyHostView;
    private TextView mProxyPacView;
    private TextView mProxyPortView;
    private ProxySettings mProxySettings;
    private Spinner mProxySettingsSpinner;
    private QrcodeUtil mQrcodeUtil;
    private Spinner mSecuritySpinner;
    private CheckBox mSharedCheckBox;
    private TextView mSsidView;
    private StaticIpConfiguration mStaticIpConfiguration;
    private final Handler mTextViewChangedHandler;
    private String mUnspecifiedCertString;
    private String mUseSystemCertsString;
    private final View mView;
    private WifiManager mWifiManager;

    public WifiConfigController(WifiConfigUiBase parent, View view, AccessPoint accessPoint, int mode) {
        this(parent, view, accessPoint, mode, false);
    }

    public WifiConfigController(WifiConfigUiBase parent, View view, AccessPoint accessPoint, int mode, boolean isDialog) {
        int i;
        this.mIpAssignment = IpAssignment.UNASSIGNED;
        this.mProxySettings = ProxySettings.UNASSIGNED;
        this.mHttpProxy = null;
        this.mStaticIpConfiguration = null;
        this.mHwCustWifiConfigController = (HwCustWifiConfigController) HwCustUtils.createObj(HwCustWifiConfigController.class, new Object[]{this});
        this.mIsDialog = isDialog;
        this.mConfigUi = parent;
        this.mView = view;
        this.mAccessPoint = accessPoint;
        if (accessPoint == null) {
            i = 0;
        } else {
            i = accessPoint.getSecurity();
        }
        this.mAccessPointSecurity = i;
        this.mMode = mode;
        this.mTextViewChangedHandler = new Handler();
        this.mContext = this.mConfigUi.getContext();
        Resources res = this.mContext.getResources();
        this.mWifiExt = new WifiExt(this.mContext);
        this.mLevels = res.getStringArray(2131361856);
        this.mPhase2PeapAdapter = new ArrayAdapter(this.mContext, 17367048, res.getStringArray(2131361865));
        this.mPhase2PeapAdapter.setDropDownViewResource(17367049);
        this.mPhase2FullAdapter = new ArrayAdapter(this.mContext, 17367048, res.getStringArray(2131361866));
        this.mPhase2FullAdapter.setDropDownViewResource(17367049);
        this.mUnspecifiedCertString = this.mContext.getString(2131624990);
        this.mMultipleCertSetString = this.mContext.getString(2131624991);
        this.mUseSystemCertsString = this.mContext.getString(2131624992);
        this.mDoNotProvideEapUserCertString = this.mContext.getString(2131624993);
        this.mDoNotValidateEapServerString = this.mContext.getString(2131624994);
        this.mIpSettingsSpinner = (Spinner) this.mView.findViewById(2131887527);
        this.mIpSettingsSpinner.setOnItemSelectedListener(this);
        this.mProxySettingsSpinner = (Spinner) this.mView.findViewById(2131887517);
        this.mProxySettingsSpinner.setOnItemSelectedListener(this);
        this.mSharedCheckBox = (CheckBox) this.mView.findViewById(2131887543);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mAccessPoint == null) {
            this.mConfigUi.setTitle(2131624932);
            this.mSsidView = (TextView) this.mView.findViewById(2131887459);
            this.mSsidView.addTextChangedListener(this);
            this.mSecuritySpinner = (Spinner) this.mView.findViewById(2131887460);
            this.mSecuritySpinner.setOnItemSelectedListener(this);
            this.mView.findViewById(2131886800).setVisibility(0);
            showIpConfigFields();
            showProxyFields();
            this.mView.findViewById(2131887461).setVisibility(0);
            ((CheckBox) this.mView.findViewById(2131887463)).setOnCheckedChangeListener(this);
            this.mConfigUi.setSubmitButton(res.getString(2131625007));
        } else {
            WifiConfiguration config;
            this.mConfigUi.setTitle(this.mAccessPoint.getSsid());
            ViewGroup group = (ViewGroup) this.mView.findViewById(2131886843);
            boolean showAdvancedFields = false;
            if (this.mAccessPoint.isSaved() && this.mAccessPoint.getConfig() != null) {
                config = this.mAccessPoint.getConfig();
                if (config.getIpAssignment() == IpAssignment.STATIC) {
                    this.mIpSettingsSpinner.setSelection(1);
                    showAdvancedFields = true;
                    StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                    if (!(staticConfig == null || staticConfig.ipAddress == null)) {
                        addRow(group, 2131624972, staticConfig.ipAddress.getAddress().getHostAddress());
                    }
                } else {
                    this.mIpSettingsSpinner.setSelection(0);
                }
                this.mSharedCheckBox.setEnabled(config.shared);
                if (!config.shared) {
                    showAdvancedFields = true;
                }
                if (config.getProxySettings() == ProxySettings.STATIC) {
                    this.mProxySettingsSpinner.setSelection(1);
                    showAdvancedFields = true;
                } else {
                    if (config.getProxySettings() == ProxySettings.PAC) {
                        this.mProxySettingsSpinner.setSelection(2);
                        showAdvancedFields = true;
                    } else {
                        this.mProxySettingsSpinner.setSelection(0);
                    }
                }
                if (config.isPasspoint()) {
                    addRow(group, 2131624973, String.format(this.mContext.getString(2131624974), new Object[]{config.providerFriendlyName}));
                }
            }
            if (!((this.mAccessPoint.isSaved() || this.mAccessPoint.isActive()) && this.mMode == 0)) {
                showSecurityFields();
                showIpConfigFields();
                showProxyFields();
                this.mView.findViewById(2131887461).setVisibility(0);
                ((CheckBox) this.mView.findViewById(2131887463)).setOnCheckedChangeListener(this);
                if (showAdvancedFields) {
                    ((CheckBox) this.mView.findViewById(2131887463)).setChecked(true);
                    this.mView.findViewById(2131887515).setVisibility(0);
                }
            }
            String signalLevel;
            boolean withBottomDivider;
            if (this.mMode == 2) {
                signalLevel = getSignalString();
                if (signalLevel != null) {
                    addRow(group, 2131624968, signalLevel);
                }
                withBottomDivider = true;
                if (this.mAccessPointSecurity == 3) {
                    withBottomDivider = false;
                }
                addRow(group, 2131628357, this.mAccessPoint.getSecurityString(false), withBottomDivider);
                this.mConfigUi.setSubmitButton(res.getString(2131625011));
            } else if (this.mMode == 1) {
                signalLevel = getSignalString();
                if (signalLevel != null) {
                    addRow(group, 2131624968, signalLevel);
                }
                withBottomDivider = true;
                if (this.mAccessPointSecurity == 3) {
                    withBottomDivider = false;
                }
                addRow(group, 2131628357, this.mAccessPoint.getSecurityString(false), withBottomDivider);
                this.mConfigUi.setSubmitButton(res.getString(2131625007));
            } else {
                DetailedState state = this.mAccessPoint.getDetailedState();
                signalLevel = getSignalString();
                if (state == null && signalLevel != null) {
                    WifiConfiguration mConfig = this.mAccessPoint.getConfig();
                    if (this.mAccessPoint.isSaved() && mConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason() == 3) {
                        this.mConfigUi.setSubmitButton(res.getString(2131628055));
                    } else {
                        this.mConfigUi.setSubmitButton(res.getString(2131625007));
                    }
                } else if (state == null || !WifiExt.shouldSetDisconnectButton(this.mContext)) {
                    this.mView.findViewById(2131887526).setVisibility(8);
                } else {
                    this.mConfigUi.setSubmitButton(this.mContext.getString(2131627515));
                }
                if (state != null) {
                    boolean isEphemeral = this.mAccessPoint.isEphemeral();
                    config = this.mAccessPoint.getConfig();
                    String providerFriendlyName = null;
                    if (config != null && config.isPasspoint()) {
                        providerFriendlyName = config.providerFriendlyName;
                    }
                    addRow(group, 2131624969, AccessPoint.getSummary(this.mConfigUi.getContext(), state, isEphemeral, providerFriendlyName));
                }
                if (signalLevel != null) {
                    addRow(group, 2131624968, signalLevel);
                }
                WifiInfo info = fetchNewestWifiInfo();
                if (!(info == null || info.getLinkSpeed() == -1)) {
                    addRow(group, 2131624970, String.format(res.getString(2131624891), new Object[]{Integer.valueOf(info.getLinkSpeed())}));
                }
                if (!(info == null || info.getFrequency() == -1)) {
                    int frequency = info.getFrequency();
                    String band = null;
                    if (frequency >= 2400 && frequency < 2500) {
                        band = String.format(res.getString(2131624485, new Object[]{Double.valueOf(2.4d)}), new Object[0]);
                    } else if (frequency < 4900 || frequency >= 5900) {
                        Log.e("WifiConfigController", "Unexpected frequency " + frequency);
                    } else {
                        band = String.format(res.getString(2131624486, new Object[]{Integer.valueOf(5)}), new Object[0]);
                    }
                    if (band != null) {
                        addRow(group, 2131624971, band);
                    }
                }
                addRow(group, 2131628357, this.mAccessPoint.getSecurityString(false));
                if (Utils.isOwner(this.mContext) && (this.mAccessPoint.isSaved() || this.mAccessPoint.isActive())) {
                    if (this.mHwCustWifiConfigController != null && !this.mHwCustWifiConfigController.isNotEditWifi(this.mAccessPoint, this.mConfigUi.getContext())) {
                        this.mConfigUi.setForgetButton(res.getString(2131625009));
                    } else if (this.mHwCustWifiConfigController == null) {
                        this.mConfigUi.setForgetButton(res.getString(2131625009));
                    }
                }
                View qrcodeView = this.mView.findViewById(2131887483);
                WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
                String ssid = this.mAccessPoint.getSsidStr();
                if (this.mAccessPoint.isActive() && WifiExtUtils.isWifiConnected(this.mContext) && wifiInfo != null && ssid != null && wifiInfo.getSSID().equals("\"" + ssid + "\"")) {
                    if (!(this.mAccessPointSecurity == 0 || this.mAccessPointSecurity == 1)) {
                        if (this.mAccessPointSecurity == 2) {
                        }
                    }
                    qrcodeView.setVisibility(0);
                    this.mQrcodeUtil = new QrcodeUtil(this.mContext, ssid, this.mView, this.mWifiManager);
                    this.mQrcodeUtil.getQrcodeBitmap();
                }
            }
            this.mWifiExt.hideWifiConfigInfo(WifiExtUtils.buildHideList(this.mAccessPoint, this.mMode, this.mView));
            View wifiHiLinkView = this.mView.findViewById(2131887509);
            if (!(wifiHiLinkView == null || this.mMode == 0)) {
                TextView wifiHiLinkTextView = (TextView) wifiHiLinkView.findViewById(2131887510);
                LinkifyUtils.linkify(this.mContext, wifiHiLinkTextView, new StringBuilder().append(wifiHiLinkTextView.getText()), null);
                if (this.mAccessPoint.isHiLinkNetwork()) {
                    i = 0;
                } else {
                    i = 8;
                }
                wifiHiLinkView.setVisibility(i);
            }
        }
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (!UserManager.isSplitSystemUser()) {
            this.mSharedCheckBox.setVisibility(8);
        }
        this.mConfigUi.setCancelButton(res.getString(2131625013));
        if (this.mConfigUi.getSubmitButton() != null) {
            enableSubmitIfAppropriate();
        }
        setOnTouchListenerForDisabledButton();
    }

    protected void addRow(ViewGroup group, int name, String value) {
        addRow(group, name, value, true);
    }

    protected void recycleBitmap() {
        if (this.mQrcodeUtil != null) {
            this.mQrcodeUtil.forRecycle();
        }
    }

    protected void addRow(ViewGroup group, int name, String value, boolean withBottomDivider) {
        View row;
        if (this.mIsDialog) {
            row = this.mConfigUi.getLayoutInflater().inflate(2130969274, group, false);
        } else {
            row = this.mConfigUi.getLayoutInflater().inflate(2130969275, group, false);
        }
        ((TextView) row.findViewById(2131886300)).setText(name);
        ((TextView) row.findViewById(2131887547)).setText(value);
        group.addView(row);
        if (withBottomDivider) {
            group.addView(this.mConfigUi.getLayoutInflater().inflate(2130968932, null));
        }
    }

    private String getSignalString() {
        int level = this.mAccessPoint.getLevel();
        if (level <= -1 || level >= this.mLevels.length) {
            return level == 4 ? this.mLevels[0] : null;
        } else {
            return this.mLevels[level];
        }
    }

    void hideForgetButton() {
        Button forget = this.mConfigUi.getForgetButton();
        if (forget != null) {
            forget.setVisibility(8);
        }
    }

    void hideSubmitButton() {
        Button submit = this.mConfigUi.getSubmitButton();
        if (submit != null) {
            submit.setVisibility(8);
        }
    }

    void enableSubmitIfAppropriate() {
        Button submit = this.mConfigUi.getSubmitButton();
        if (submit != null) {
            submit.setEnabled(isSubmittable());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isSubmittable() {
        boolean enabled;
        boolean passwordInvalid = false;
        if (this.mPasswordView != null) {
            int length = this.mPasswordView.length();
            if (this.mAccessPointSecurity == 1) {
                if (!(length == 10 || length == 26)) {
                    if (length == 58) {
                    }
                    if (!(length == 5 || length == 13 || length == 29)) {
                        Log.d("WifiConfigController", "wep passwordInvalid");
                        passwordInvalid = true;
                    }
                }
            }
            if (this.mAccessPointSecurity == 2 && length < 8) {
                passwordInvalid = true;
            }
        }
        if (this.mAccessPointSecurity == 3 && this.mEapMethodSpinner != null && this.mEapMethodSpinner.getSelectedItemPosition() == 0) {
            if (this.mEapIdentityView == null || this.mEapIdentityView.getText() != null) {
                if (this.mPasswordView != null && this.mPasswordView.length() == 0) {
                }
            }
            passwordInvalid = true;
        }
        if (this.mHwCustWifiConfigController != null && this.mHwCustWifiConfigController.isPasswordInvalid(this.mAccessPointSecurity, this.mPasswordView)) {
            passwordInvalid = true;
        }
        if (this.mSsidView == null || this.mSsidView.length() != 0) {
            if ((this.mAccessPoint != null && this.mAccessPoint.isSaved()) || !passwordInvalid) {
                if (!ipAndProxyFieldsAreValid() || (passwordInvalid && (this.mPasswordView == null || this.mPasswordView.length() != 0))) {
                    enabled = false;
                } else {
                    enabled = true;
                }
                if (!(this.mEapCaCertSpinner == null || this.mView.findViewById(2131887493).getVisibility() == 8 || !((String) this.mEapCaCertSpinner.getSelectedItem()).equals(this.mUseSystemCertsString) || this.mEapDomainView == null || this.mView.findViewById(2131887496).getVisibility() == 8 || !TextUtils.isEmpty(this.mEapDomainView.getText().toString()))) {
                    enabled = false;
                }
                if (this.mEapUserCertSpinner == null && this.mView.findViewById(2131887499).getVisibility() != 8 && ((String) this.mEapUserCertSpinner.getSelectedItem()).equals(this.mUnspecifiedCertString)) {
                    return false;
                }
                return enabled;
            }
        }
        enabled = false;
        enabled = false;
        return this.mEapUserCertSpinner == null ? enabled : enabled;
    }

    void showWarningMessagesIfAppropriate() {
        this.mView.findViewById(2131887495).setVisibility(8);
        this.mView.findViewById(2131887498).setVisibility(8);
        if (this.mEapCaCertSpinner != null && this.mView.findViewById(2131887493).getVisibility() != 8) {
            String caCertSelection = (String) this.mEapCaCertSpinner.getSelectedItem();
            if (caCertSelection.equals(this.mDoNotValidateEapServerString)) {
                this.mView.findViewById(2131887495).setVisibility(0);
            }
            if (caCertSelection.equals(this.mUseSystemCertsString) && this.mEapDomainView != null && this.mView.findViewById(2131887496).getVisibility() != 8 && TextUtils.isEmpty(this.mEapDomainView.getText().toString())) {
                this.mView.findViewById(2131887498).setVisibility(8);
            }
        }
    }

    WifiConfiguration getConfig() {
        if (this.mMode == 0) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        if (this.mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(this.mSsidView.getText().toString());
            config.hiddenSSID = true;
        } else if (this.mAccessPoint.isSaved()) {
            config.networkId = this.mAccessPoint.getConfig().networkId;
            config.oriSsid = this.mAccessPoint.getOriSsid();
        } else {
            config.SSID = AccessPoint.convertToQuotedString(this.mAccessPoint.getSsidStr());
            config.oriSsid = this.mAccessPoint.getOriSsid();
        }
        config.shared = this.mSharedCheckBox.isChecked();
        String password;
        switch (this.mAccessPointSecurity) {
            case 0:
                config.allowedKeyManagement.set(0);
                break;
            case 1:
                config.allowedKeyManagement.set(0);
                config.allowedAuthAlgorithms.set(0);
                config.allowedAuthAlgorithms.set(1);
                if (this.mPasswordView.length() != 0) {
                    int length = this.mPasswordView.length();
                    password = this.mPasswordView.getText().toString();
                    if ((length != 10 && length != 26 && length != 58) || !password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = '\"' + password + '\"';
                        break;
                    }
                    config.wepKeys[0] = password;
                    break;
                }
                break;
            case 2:
                config.allowedKeyManagement.set(1);
                if (this.mPasswordView.length() != 0) {
                    password = this.mPasswordView.getText().toString();
                    if (!password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = '\"' + password + '\"';
                        break;
                    }
                    config.preSharedKey = password;
                    break;
                }
                break;
            case 3:
                config.allowedKeyManagement.set(2);
                config.allowedKeyManagement.set(3);
                config.enterpriseConfig = new WifiEnterpriseConfig();
                int eapMethod = this.mEapMethodSpinner.getSelectedItemPosition();
                if (!(this.mWifiExt == null || this.mAccessPoint == null)) {
                    eapMethod = this.mWifiExt.getEapMethodbySpinnerPos(eapMethod, this.mAccessPoint.getSsidStr(), this.mAccessPoint.getSecurity());
                }
                if (eapMethod >= 4 && isHideEapSimByCardOrNetwork(this.mConfigUi.getContext())) {
                    eapMethod++;
                }
                int phase2Method = this.mPhase2Spinner.getSelectedItemPosition();
                config.enterpriseConfig.setEapMethod(eapMethod);
                switch (eapMethod) {
                    case 0:
                        switch (phase2Method) {
                            case 0:
                                config.enterpriseConfig.setPhase2Method(0);
                                break;
                            case 1:
                                config.enterpriseConfig.setPhase2Method(3);
                                break;
                            case 2:
                                config.enterpriseConfig.setPhase2Method(4);
                                break;
                            default:
                                Log.e("WifiConfigController", "Unknown phase2 method" + phase2Method);
                                break;
                        }
                    default:
                        config.enterpriseConfig.setPhase2Method(phase2Method);
                        break;
                }
                String caCert = (String) this.mEapCaCertSpinner.getSelectedItem();
                config.enterpriseConfig.setCaCertificateAliases(null);
                config.enterpriseConfig.setCaPath(null);
                config.enterpriseConfig.setDomainSuffixMatch(this.mEapDomainView.getText().toString());
                if (!(caCert.equals(this.mUnspecifiedCertString) || caCert.equals(this.mDoNotValidateEapServerString))) {
                    if (caCert.equals(this.mUseSystemCertsString)) {
                        config.enterpriseConfig.setCaPath("/system/etc/security/cacerts");
                    } else if (!caCert.equals(this.mMultipleCertSetString)) {
                        config.enterpriseConfig.setCaCertificateAliases(new String[]{caCert});
                    } else if (this.mAccessPoint != null) {
                        if (!this.mAccessPoint.isSaved()) {
                            Log.e("WifiConfigController", "Multiple certs can only be set when editing saved network");
                        }
                        config.enterpriseConfig.setCaCertificateAliases(this.mAccessPoint.getConfig().enterpriseConfig.getCaCertificateAliases());
                    }
                }
                if (!(config.enterpriseConfig.getCaCertificateAliases() == null || config.enterpriseConfig.getCaPath() == null)) {
                    Log.e("WifiConfigController", "ca_cert (" + config.enterpriseConfig.getCaCertificateAliases() + ") and ca_path (" + config.enterpriseConfig.getCaPath() + ") should not both be non-null");
                }
                String clientCert = (String) this.mEapUserCertSpinner.getSelectedItem();
                if (clientCert.equals(this.mUnspecifiedCertString) || clientCert.equals(this.mDoNotProvideEapUserCertString)) {
                    clientCert = "";
                }
                config.enterpriseConfig.setClientCertificateAlias(clientCert);
                if (eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
                    config.enterpriseConfig.setIdentity("");
                    config.enterpriseConfig.setAnonymousIdentity("");
                } else if (eapMethod == 3) {
                    config.enterpriseConfig.setIdentity(this.mEapIdentityView.getText().toString());
                    config.enterpriseConfig.setAnonymousIdentity("");
                } else {
                    config.enterpriseConfig.setIdentity(this.mEapIdentityView.getText().toString());
                    config.enterpriseConfig.setAnonymousIdentity(this.mEapAnonymousView.getText().toString());
                }
                if (this.mPasswordView.isShown()) {
                    if (this.mPasswordView.length() > 0) {
                        config.enterpriseConfig.setPassword(this.mPasswordView.getText().toString());
                        break;
                    }
                }
                config.enterpriseConfig.setPassword(this.mPasswordView.getText().toString());
                break;
                break;
            default:
                if (!(this.mHwCustWifiConfigController == null || this.mHwCustWifiConfigController.getConfig(this.mAccessPointSecurity, config, this.mView)) || this.mHwCustWifiConfigController == null) {
                    return null;
                }
        }
        config.setIpConfiguration(new IpConfiguration(this.mIpAssignment, this.mProxySettings, this.mStaticIpConfiguration, this.mHttpProxy));
        return config;
    }

    private boolean ipAndProxyFieldsAreValid() {
        IpAssignment ipAssignment;
        if (this.mIpSettingsSpinner == null || this.mIpSettingsSpinner.getSelectedItemPosition() != 1) {
            ipAssignment = IpAssignment.DHCP;
        } else {
            ipAssignment = IpAssignment.STATIC;
        }
        this.mIpAssignment = ipAssignment;
        if (this.mIpAssignment == IpAssignment.STATIC) {
            this.mStaticIpConfiguration = new StaticIpConfiguration();
            if (validateIpConfigFields(this.mStaticIpConfiguration) != 0) {
                return false;
            }
        }
        int selectedPosition = this.mProxySettingsSpinner.getSelectedItemPosition();
        this.mProxySettings = ProxySettings.NONE;
        this.mHttpProxy = null;
        if (selectedPosition == 1 && this.mProxyHostView != null) {
            int result;
            this.mProxySettings = ProxySettings.STATIC;
            String host = this.mProxyHostView.getText().toString();
            String portStr = this.mProxyPortView.getText().toString();
            String exclusionList = this.mProxyExclusionListView.getText().toString();
            int i = 0;
            try {
                i = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = 2131624500;
            }
            if (result != 0) {
                return false;
            }
            this.mHttpProxy = new ProxyInfo(host, i, exclusionList);
        } else if (selectedPosition == 2 && this.mProxyPacView != null) {
            this.mProxySettings = ProxySettings.PAC;
            CharSequence uriSequence = this.mProxyPacView.getText();
            if (TextUtils.isEmpty(uriSequence)) {
                return false;
            }
            Uri uri = Uri.parse(uriSequence.toString());
            if (uri == null) {
                return false;
            }
            this.mHttpProxy = new ProxyInfo(uri);
        }
        return true;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int validateIpConfigFields(StaticIpConfiguration staticIpConfiguration) {
        if (this.mIpAddressView == null) {
            return 0;
        }
        String ipAddr = this.mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) {
            return 2131625029;
        }
        Inet4Address inetAddr = getIPv4Address(ipAddr);
        if (inetAddr == null || inetAddr.equals(Inet4Address.ANY)) {
            return 2131625029;
        }
        if (!this.mWifiExt.shouldDisplayNetmask()) {
            try {
                int networkPrefixLength = Integer.parseInt(this.mNetworkPrefixLengthView.getText().toString());
                if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                    return 2131625032;
                }
                staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
            } catch (NumberFormatException e) {
                return 2131625032;
            } catch (IllegalArgumentException e2) {
                return 2131625029;
            }
        } else if (validateNetMask(staticIpConfiguration, inetAddr) == -1) {
            return -1;
        }
        String gateway = this.mGatewayView.getText().toString();
        if (TextUtils.isEmpty(gateway)) {
            return 2131625030;
        }
        InetAddress gatewayAddr = getIPv4Address(gateway);
        if (gatewayAddr == null || gatewayAddr.isMulticastAddress()) {
            return 2131625030;
        }
        staticIpConfiguration.gateway = gatewayAddr;
        String dns = this.mDns1View.getText().toString();
        if (TextUtils.isEmpty(dns)) {
            return 2131625031;
        }
        InetAddress dnsAddr = getIPv4Address(dns);
        if (dnsAddr == null) {
            return 2131625031;
        }
        staticIpConfiguration.dnsServers.add(dnsAddr);
        if (this.mDns2View.length() > 0) {
            dnsAddr = getIPv4Address(this.mDns2View.getText().toString());
            if (dnsAddr == null) {
                return 2131625031;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }
        return 0;
    }

    private void showSecurityFields() {
        LinearLayout securitySpinnerContainer = (LinearLayout) this.mView.findViewById(2131887486);
        if (securitySpinnerContainer != null) {
            if (this.mAccessPointSecurity == 3) {
                securitySpinnerContainer.setShowDividers(0);
            } else {
                securitySpinnerContainer.setShowDividers(4);
            }
        }
        if (this.mAccessPointSecurity == 0) {
            this.mView.findViewById(2131887487).setVisibility(8);
            return;
        }
        this.mView.findViewById(2131887487).setVisibility(0);
        if (this.mPasswordView == null) {
            this.mPasswordView = (TextView) this.mView.findViewById(2131887420);
        }
        if (this.mPasswordView != null) {
            this.mPasswordView.addTextChangedListener(this);
            this.mPasswordView.setOnEditorActionListener(this);
            this.mPasswordView.setOnKeyListener(this);
            WifiExtUtils.setPasswordView(this.mPasswordView, this.mView);
            if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
                this.mPasswordView.setHint(2131624989);
            }
        }
        if (this.mHwCustWifiConfigController != null) {
            this.mHwCustWifiConfigController.showWapiSecurityFields(this.mAccessPointSecurity, this.mView);
        }
        if (this.mAccessPointSecurity != 3) {
            this.mView.findViewById(2131887488).setVisibility(8);
            setVisibility(2131887511, 0);
            return;
        }
        this.mView.findViewById(2131887488).setVisibility(0);
        if (this.mEapMethodSpinner == null) {
            this.mEapMethodSpinner = (Spinner) this.mView.findViewById(2131887490);
            this.mEapMethodSpinner.setOnItemSelectedListener(this);
            if (Utils.isWifiOnly(this.mContext) || !this.mContext.getResources().getBoolean(17957033) || isHideEapSimByCardOrNetwork(this.mConfigUi.getContext())) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this.mContext, 17367048, !isHideEapSimByCardOrNetwork(this.mConfigUi.getContext()) ? this.mContext.getResources().getStringArray(2131361848) : this.mContext.getResources().getStringArray(2131361847));
                arrayAdapter.setDropDownViewResource(17367049);
                int formerSelectedItemPosition = this.mEapMethodSpinner.getSelectedItemPosition();
                this.mEapMethodSpinner.setAdapter(arrayAdapter);
                if (formerSelectedItemPosition < arrayAdapter.getCount()) {
                    this.mEapMethodSpinner.setSelection(formerSelectedItemPosition);
                }
            } else if (this.mWifiExt != null) {
                this.mWifiExt.applyCmccEapMethod(this.mEapMethodSpinner, this.mAccessPoint);
            }
            this.mPhase2Spinner = (Spinner) this.mView.findViewById(2131887492);
            this.mEapCaCertSpinner = (Spinner) this.mView.findViewById(2131887494);
            this.mEapCaCertSpinner.setOnItemSelectedListener(this);
            this.mEapDomainView = (TextView) this.mView.findViewById(2131887497);
            this.mEapDomainView.addTextChangedListener(this);
            this.mEapUserCertSpinner = (Spinner) this.mView.findViewById(2131887500);
            this.mEapUserCertSpinner.setOnItemSelectedListener(this);
            this.mEapIdentityView = (TextView) this.mView.findViewById(2131887503);
            this.mEapAnonymousView = (TextView) this.mView.findViewById(2131887506);
            loadCertificates(this.mEapCaCertSpinner, "CACERT_", this.mDoNotValidateEapServerString, false, true);
            loadCertificates(this.mEapUserCertSpinner, "USRPKEY_", this.mDoNotProvideEapUserCertString, false, false);
            if (this.mAccessPoint == null || !this.mAccessPoint.isSaved()) {
                int defaultEapMethod = this.mEapMethodSpinner.getSelectedItemPosition();
                if (-1 == defaultEapMethod) {
                    defaultEapMethod = 0;
                }
                if (this.mHwCustWifiConfigController != null) {
                    defaultEapMethod = this.mHwCustWifiConfigController.getEapMethodDefault(this.mEapMethodSpinner, this.mConfigUi.getContext());
                }
                this.mEapMethodSpinner.setSelection(defaultEapMethod);
                showEapFieldsByMethod(defaultEapMethod);
            } else {
                WifiEnterpriseConfig enterpriseConfig = this.mAccessPoint.getConfig().enterpriseConfig;
                int eapMethod = enterpriseConfig.getEapMethod();
                int phase2Method = enterpriseConfig.getPhase2Method();
                eapMethod = this.mWifiExt.getCustomizeEapMethod(eapMethod, this.mAccessPoint.getSsidStr(), this.mAccessPoint.getSecurity());
                if (eapMethod >= 4 && isHideEapSimByCardOrNetwork(this.mConfigUi.getContext())) {
                    eapMethod--;
                }
                this.mEapMethodSpinner.setSelection(eapMethod);
                showEapFieldsByMethod(eapMethod);
                switch (eapMethod) {
                    case 0:
                        switch (phase2Method) {
                            case 0:
                                this.mPhase2Spinner.setSelection(0);
                                break;
                            case 3:
                                this.mPhase2Spinner.setSelection(1);
                                break;
                            case 4:
                                this.mPhase2Spinner.setSelection(2);
                                break;
                            default:
                                Log.e("WifiConfigController", "Invalid phase 2 method " + phase2Method);
                                break;
                        }
                    default:
                        this.mPhase2Spinner.setSelection(phase2Method);
                        break;
                }
                if (TextUtils.isEmpty(enterpriseConfig.getCaPath())) {
                    String[] caCerts = enterpriseConfig.getCaCertificateAliases();
                    if (caCerts == null) {
                        setSelection(this.mEapCaCertSpinner, this.mDoNotValidateEapServerString);
                    } else if (caCerts.length == 1) {
                        setSelection(this.mEapCaCertSpinner, caCerts[0]);
                    } else {
                        loadCertificates(this.mEapCaCertSpinner, "CACERT_", this.mDoNotValidateEapServerString, true, true);
                        setSelection(this.mEapCaCertSpinner, this.mMultipleCertSetString);
                    }
                } else {
                    setSelection(this.mEapCaCertSpinner, this.mUseSystemCertsString);
                }
                this.mEapDomainView.setText(enterpriseConfig.getDomainSuffixMatch());
                String userCert = enterpriseConfig.getClientCertificateAlias();
                if (TextUtils.isEmpty(userCert)) {
                    setSelection(this.mEapUserCertSpinner, this.mDoNotProvideEapUserCertString);
                } else {
                    setSelection(this.mEapUserCertSpinner, userCert);
                }
                this.mEapIdentityView.setText(enterpriseConfig.getIdentity());
                this.mEapAnonymousView.setText(enterpriseConfig.getAnonymousIdentity());
            }
        } else {
            showEapFieldsByMethod(this.mEapMethodSpinner.getSelectedItemPosition());
            if (this.mHwCustWifiConfigController != null) {
                this.mHwCustWifiConfigController.notAllowModifyWifi(this.mView, this.mAccessPoint);
            }
        }
    }

    private boolean isHideEapSimByCardOrNetwork(Context context) {
        String residentPlmn = SystemProperties.get("ril.operator.numeric", "");
        String mccNum = System.getString(context.getContentResolver(), "hw_hide_eap_sim_method");
        if (TextUtils.isEmpty(mccNum)) {
            mccNum = "262";
        }
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            String mcc_mnc0 = MSimTelephonyManager.getDefault().getSimOperator(0);
            String mcc_mnc1 = MSimTelephonyManager.getDefault().getSimOperator(1);
            String networkMccMnc0 = MSimTelephonyManager.getDefault().getNetworkOperator(0);
            String networkMccMnc1 = MSimTelephonyManager.getDefault().getNetworkOperator(1);
            boolean isMccMatched = !isMccMatched(mccNum, mcc_mnc0, networkMccMnc0, residentPlmn) ? isMccMatched(mccNum, mcc_mnc1, networkMccMnc1, residentPlmn) : true;
            Log.d("WifiConfigController", "mcc_mnc0 = " + mcc_mnc0 + " mcc_mnc1 = " + mcc_mnc1 + " networkMccMnc0 = " + networkMccMnc0 + " networkMccMnc1 = " + networkMccMnc1 + " residentPlmn = " + residentPlmn + " mccNum = " + mccNum);
            return isMccMatched;
        }
        String mcc_mnc = TelephonyManager.getDefault().getSimOperator();
        String networkMccMnc = TelephonyManager.getDefault().getNetworkOperator();
        isMccMatched = isMccMatched(mccNum, mcc_mnc, networkMccMnc, residentPlmn);
        Log.d("WifiConfigController", "mcc_mnc = " + mcc_mnc + " networkMccMnc = " + networkMccMnc + " mccNum = " + mccNum + " residentPlmn = " + residentPlmn);
        return isMccMatched;
    }

    private boolean isMccMatched(String mccNums, String mccmnc, String networkMccMnc, String residentPlmn) {
        for (String mcc : mccNums.split(",")) {
            if ((!TextUtils.isEmpty(mccmnc) && mccmnc.startsWith(mcc)) || ((!TextUtils.isEmpty(networkMccMnc) && networkMccMnc.startsWith(mcc)) || (!TextUtils.isEmpty(residentPlmn) && residentPlmn.startsWith(mcc)))) {
                return true;
            }
        }
        return false;
    }

    private void showEapFieldsByMethod(int eapMethod) {
        this.mView.findViewById(2131887489).setVisibility(0);
        this.mView.findViewById(2131887501).setVisibility(0);
        this.mView.findViewById(2131887496).setVisibility(0);
        this.mView.findViewById(2131887493).setVisibility(0);
        this.mView.findViewById(2131887511).setVisibility(0);
        Context context = this.mConfigUi.getContext();
        if (!(this.mWifiExt == null || this.mAccessPoint == null)) {
            eapMethod = this.mWifiExt.getEapMethodbySpinnerPos(eapMethod, this.mAccessPoint.getSsidStr(), this.mAccessPoint.getSecurity());
        }
        if (eapMethod >= 4 && isHideEapSimByCardOrNetwork(context)) {
            eapMethod++;
        }
        switch (eapMethod) {
            case 0:
                if (this.mPhase2Adapter != this.mPhase2PeapAdapter) {
                    this.mPhase2Adapter = this.mPhase2PeapAdapter;
                    this.mPhase2Spinner.setAdapter(this.mPhase2Adapter);
                    if (this.mEapMethodFromSavedInstance == 0 && this.mPhase2FromSavedInstance > 0 && this.mPhase2FromSavedInstance < this.mPhase2Adapter.getCount()) {
                        this.mPhase2Spinner.setSelection(this.mPhase2FromSavedInstance);
                        this.mEapMethodFromSavedInstance = 0;
                        this.mPhase2FromSavedInstance = 0;
                    }
                }
                this.mView.findViewById(2131887491).setVisibility(0);
                this.mView.findViewById(2131887504).setVisibility(0);
                setUserCertInvisible();
                break;
            case 1:
                this.mView.findViewById(2131887499).setVisibility(0);
                setPhase2Invisible();
                setAnonymousIdentInvisible();
                setPasswordInvisible();
                break;
            case 2:
                if (this.mPhase2Adapter != this.mPhase2FullAdapter) {
                    this.mPhase2Adapter = this.mPhase2FullAdapter;
                    this.mPhase2Spinner.setAdapter(this.mPhase2Adapter);
                    if (2 == this.mEapMethodFromSavedInstance && this.mPhase2FromSavedInstance > 0 && this.mPhase2FromSavedInstance < this.mPhase2Adapter.getCount()) {
                        this.mPhase2Spinner.setSelection(this.mPhase2FromSavedInstance);
                        this.mEapMethodFromSavedInstance = 0;
                        this.mPhase2FromSavedInstance = 0;
                    }
                }
                this.mView.findViewById(2131887491).setVisibility(0);
                this.mView.findViewById(2131887504).setVisibility(0);
                setUserCertInvisible();
                break;
            case 3:
                setPhase2Invisible();
                setCaCertInvisible();
                setDomainInvisible();
                setAnonymousIdentInvisible();
                setUserCertInvisible();
                break;
            case 4:
            case 5:
            case 6:
                setPhase2Invisible();
                setCaCertInvisible();
                setDomainInvisible();
                setUserCertInvisible();
                setAnonymousIdentInvisible();
                if (System.getInt(context.getContentResolver(), "eapsim_view_removed", 0) == 1) {
                    setIdentityInvisible();
                    setPasswordInvisible();
                    break;
                }
                break;
        }
        if (this.mView.findViewById(2131887493).getVisibility() != 8) {
            String eapCertSelection = (String) this.mEapCaCertSpinner.getSelectedItem();
            if (eapCertSelection.equals(this.mDoNotValidateEapServerString) || eapCertSelection.equals(this.mUnspecifiedCertString)) {
                setDomainInvisible();
            }
        }
        if (this.mWifiExt != null && this.mAccessPoint != null) {
            this.mWifiExt.hideWifiConfigInfo(WifiExtUtils.buildHideList(this.mAccessPoint, this.mMode, this.mView));
        }
    }

    private void setIdentityInvisible() {
        this.mView.findViewById(2131887501).setVisibility(8);
        this.mEapIdentityView.setText("");
    }

    private void setPhase2Invisible() {
        this.mView.findViewById(2131887491).setVisibility(8);
        this.mPhase2Spinner.setSelection(0);
    }

    private void setCaCertInvisible() {
        this.mView.findViewById(2131887493).setVisibility(8);
        setSelection(this.mEapCaCertSpinner, this.mUnspecifiedCertString);
    }

    private void setDomainInvisible() {
        this.mView.findViewById(2131887496).setVisibility(8);
        this.mEapDomainView.setText("");
    }

    private void setUserCertInvisible() {
        this.mView.findViewById(2131887499).setVisibility(8);
        setSelection(this.mEapUserCertSpinner, this.mUnspecifiedCertString);
    }

    private void setAnonymousIdentInvisible() {
        this.mView.findViewById(2131887504).setVisibility(8);
        this.mEapAnonymousView.setText("");
    }

    private void setPasswordInvisible() {
        this.mPasswordView.setText("");
        this.mView.findViewById(2131887511).setVisibility(8);
    }

    public void setCertificate(Spinner spinner, String prefix, String cert, String space) {
        prefix = space + prefix;
        if (cert != null && cert.startsWith(prefix)) {
            setSelection(spinner, cert.substring(prefix.length()));
        }
    }

    public void showIpConfigFields() {
        WifiConfiguration config = null;
        this.mView.findViewById(2131887526).setVisibility(0);
        if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
            config = this.mAccessPoint.getConfig();
        }
        if (this.mIpSettingsSpinner.getSelectedItemPosition() == 1) {
            this.mView.findViewById(2131887529).setVisibility(0);
            this.mWifiExt.initNetworkPrefixAndMaskView(this.mView.findViewById(2131887534), this.mView.findViewById(2131887537));
            if (this.mIpAddressView == null) {
                this.mIpAddressView = (TextView) this.mView.findViewById(2131887531);
                this.mIpAddressView.addTextChangedListener(this);
                this.mGatewayView = (TextView) this.mView.findViewById(2131887533);
                this.mGatewayView.addTextChangedListener(this);
                this.mNetworkPrefixLengthView = (TextView) this.mView.findViewById(2131887536);
                this.mNetworkPrefixLengthView.addTextChangedListener(this);
                this.mNetworkNetmaskView = (TextView) this.mView.findViewById(2131887539);
                this.mNetworkNetmaskView.addTextChangedListener(this);
                this.mDns1View = (TextView) this.mView.findViewById(2131887541);
                this.mDns1View.addTextChangedListener(this);
                this.mDns2View = (TextView) this.mView.findViewById(2131887542);
                this.mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                if (staticConfig != null) {
                    if (staticConfig.ipAddress != null) {
                        this.mIpAddressView.setText(staticConfig.ipAddress.getAddress().getHostAddress());
                        this.mNetworkPrefixLengthView.setText(Integer.toString(staticConfig.ipAddress.getNetworkPrefixLength()));
                        this.mNetworkNetmaskView.setText(NetworkUtils.intToInetAddress(NetworkUtils.prefixLengthToNetmaskInt(staticConfig.ipAddress.getNetworkPrefixLength())).getHostAddress());
                    }
                    if (staticConfig.gateway != null) {
                        this.mGatewayView.setText(staticConfig.gateway.getHostAddress());
                    }
                    Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
                    if (dnsIterator.hasNext()) {
                        this.mDns1View.setText(((InetAddress) dnsIterator.next()).getHostAddress());
                    }
                    if (dnsIterator.hasNext()) {
                        this.mDns2View.setText(((InetAddress) dnsIterator.next()).getHostAddress());
                        return;
                    }
                    return;
                }
                DhcpInfo dhcpInfo = ((WifiManager) this.mContext.getSystemService("wifi")).getDhcpInfo();
                if (dhcpInfo != null) {
                    try {
                        if (dhcpInfo.ipAddress > 0) {
                            this.mIpAddressView.setText(NetworkUtils.intToInetAddress(dhcpInfo.ipAddress).getHostAddress());
                        }
                        if (dhcpInfo.gateway > 0) {
                            this.mGatewayView.setText(NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress());
                        }
                        String netMask = NetworkUtils.intToInetAddress(dhcpInfo.netmask).getHostAddress();
                        int networkPrefixLength = WifiExt.getNetworkPrefixLengthFromNetmask(netMask);
                        if (networkPrefixLength >= 0 && networkPrefixLength <= 32) {
                            this.mNetworkPrefixLengthView.setText(Integer.toString(networkPrefixLength));
                        }
                        if (dhcpInfo.netmask > 0) {
                            this.mNetworkNetmaskView.setText(netMask);
                        }
                        if (dhcpInfo.dns1 > 0) {
                            this.mDns1View.setText(NetworkUtils.intToInetAddress(dhcpInfo.dns1).getHostAddress());
                        }
                        if (dhcpInfo.dns2 > 0) {
                            this.mDns2View.setText(NetworkUtils.intToInetAddress(dhcpInfo.dns2).getHostAddress());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (TextUtils.isEmpty(this.mIpAddressView.getText())) {
                    this.mIpAddressView.setText(Utils.getWifiIpv4Addresses(this.mContext));
                    return;
                }
                return;
            }
            return;
        }
        this.mView.findViewById(2131887529).setVisibility(8);
    }

    private void showProxyFields() {
        WifiConfiguration config = null;
        this.mView.findViewById(2131887516).setVisibility(0);
        if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
            config = this.mAccessPoint.getConfig();
        }
        LinearLayout proxySettingsFields = (LinearLayout) this.mView.findViewById(2131887516);
        if (this.mProxySettingsSpinner.getSelectedItemPosition() == 1) {
            setVisibility(2131887519, 0);
            setVisibility(2131887522, 0);
            setVisibility(2131887520, 8);
            if (this.mProxyHostView == null) {
                this.mProxyHostView = (TextView) this.mView.findViewById(2131887523);
                this.mProxyHostView.addTextChangedListener(this);
                this.mProxyPortView = (TextView) this.mView.findViewById(2131887524);
                this.mProxyPortView.addTextChangedListener(this);
                this.mProxyExclusionListView = (TextView) this.mView.findViewById(2131887525);
                this.mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyInfo proxyProperties = config.getHttpProxy();
                if (proxyProperties != null) {
                    this.mProxyHostView.setText(proxyProperties.getHost());
                    this.mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    this.mProxyExclusionListView.setText(proxyProperties.getExclusionListAsString());
                }
            }
            if (proxySettingsFields != null) {
                proxySettingsFields.setShowDividers(4);
            }
        } else if (this.mProxySettingsSpinner.getSelectedItemPosition() == 2) {
            setVisibility(2131887519, 8);
            setVisibility(2131887522, 8);
            setVisibility(2131887520, 0);
            if (this.mProxyPacView == null) {
                this.mProxyPacView = (TextView) this.mView.findViewById(2131887521);
                this.mProxyPacView.addTextChangedListener(this);
            }
            if (config != null) {
                ProxyInfo proxyInfo = config.getHttpProxy();
                if (proxyInfo != null) {
                    this.mProxyPacView.setText(proxyInfo.getPacFileUrl().toString());
                }
            }
            if (proxySettingsFields != null) {
                proxySettingsFields.setShowDividers(4);
            }
        } else {
            setVisibility(2131887519, 8);
            setVisibility(2131887522, 8);
            setVisibility(2131887520, 8);
            if (proxySettingsFields != null) {
                proxySettingsFields.setShowDividers(0);
            }
        }
    }

    private void setVisibility(int id, int visibility) {
        View v = this.mView.findViewById(id);
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    private void loadCertificates(Spinner spinner, String prefix, String noCertificateString, boolean showMultipleCerts, boolean showUsePreinstalledCertOption) {
        Context context = this.mConfigUi.getContext();
        ArrayList<String> certs = new ArrayList();
        certs.add(this.mUnspecifiedCertString);
        if (showMultipleCerts) {
            certs.add(this.mMultipleCertSetString);
        }
        if (showUsePreinstalledCertOption) {
            certs.add(this.mUseSystemCertsString);
        }
        Object[] certsArray = null;
        try {
            certsArray = KeyStore.getInstance().list(prefix, 1010);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (certsArray == null) {
            certsArray = new String[0];
        }
        certs.addAll(Arrays.asList(certsArray));
        certs.add(noCertificateString);
        ArrayAdapter<String> adapter = new ArrayAdapter(context, 17367048, (String[]) certs.toArray(new String[certs.size()]));
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter(adapter);
    }

    private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; i--) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public void afterTextChanged(Editable s) {
        this.mTextViewChangedHandler.post(new Runnable() {
            public void run() {
                WifiConfigController.this.showWarningMessagesIfAppropriate();
                WifiConfigController.this.enableSubmitIfAppropriate();
            }
        });
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (textView != this.mPasswordView || id != 6 || !isSubmittable()) {
            return false;
        }
        this.mConfigUi.dispatchSubmit();
        return false;
    }

    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (view != this.mPasswordView || keyCode != 66 || !isSubmittable()) {
            return false;
        }
        this.mConfigUi.dispatchSubmit();
        return true;
    }

    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        if (view.getId() == 2131886368) {
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
        } else if (view.getId() != 2131887463) {
        } else {
            if (isChecked) {
                this.mView.findViewById(2131887515).setVisibility(0);
            } else {
                this.mView.findViewById(2131887515).setVisibility(8);
            }
        }
    }

    private void configEapForCmcc(int position) {
        if (this.mAccessPoint != null) {
            WifiConfiguration wifiConfig = this.mAccessPoint.getConfig();
            if (wifiConfig != null) {
                WifiEnterpriseConfig enterpriseConfig = wifiConfig.enterpriseConfig;
                int eapMethod = position;
                if (enterpriseConfig != null) {
                    eapMethod = enterpriseConfig.getEapMethod();
                }
                if (this.mWifiExt != null) {
                    eapMethod = this.mWifiExt.getEapMethodbySpinnerPos(eapMethod, this.mAccessPoint.getSsidStr(), this.mAccessPoint.getSecurity());
                }
                if (eapMethod != position) {
                    this.mEapIdentityView.setText("");
                    this.mEapAnonymousView.setText("");
                    this.mPasswordView.setText("");
                }
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == this.mSecuritySpinner) {
            this.mAccessPointSecurity = position;
            showSecurityFields();
            hidePasswordTips();
        } else if (parent == this.mEapMethodSpinner || parent == this.mEapCaCertSpinner) {
            if (parent == this.mEapMethodSpinner) {
                configEapForCmcc(position);
            }
            showSecurityFields();
        } else if (parent == this.mProxySettingsSpinner) {
            showProxyFields();
        } else {
            showIpConfigFields();
        }
        showWarningMessagesIfAppropriate();
        enableSubmitIfAppropriate();
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void updatePassword() {
        int i;
        TextView passwdView = (TextView) this.mView.findViewById(2131887420);
        if (((CheckBox) this.mView.findViewById(2131886368)).isChecked()) {
            i = 144;
        } else {
            i = 128;
        }
        passwdView.setInputType(i | 1);
    }

    public AccessPoint getAccessPoint() {
        return this.mAccessPoint;
    }

    private WifiInfo fetchNewestWifiInfo() {
        WifiInfo info = this.mAccessPoint.getInfo();
        WifiInfo connectedWifiInfo = this.mWifiManager.getConnectionInfo();
        if (info == null || connectedWifiInfo == null || info.getNetworkId() != connectedWifiInfo.getNetworkId() || !TextUtils.equals(info.getSSID(), connectedWifiInfo.getSSID())) {
            return info;
        }
        return connectedWifiInfo;
    }
}
