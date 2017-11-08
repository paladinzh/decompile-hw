package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Telephony.Carriers;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import com.android.settings.Utils.ImmersionIcon;
import com.huawei.cust.HwCustUtils;
import java.util.HashSet;
import java.util.Set;

public class ApnEditor extends ApnEditorHwBase implements OnPreferenceChangeListener, OnKeyListener {
    private static final String TAG = ApnEditor.class.getSimpleName();
    private static String sNotSet;
    private boolean isSprintConvertible;
    private EditTextPreference mApn;
    private EditTextPreference mApnType;
    private ListPreference mAuthType;
    private int mBearerInitialVal = 0;
    private MultiSelectListPreference mBearerMulti;
    private SwitchPreference mCarrierEnabled;
    private String mCurMcc;
    private String mCurMnc;
    private boolean mFirstTime;
    private HwCustApnEditorHwBase mHwCustApnEditorHwBase;
    private EditTextPreference mMcc;
    private EditTextPreference mMmsPort;
    private EditTextPreference mMmsProxy;
    private EditTextPreference mMmsc;
    private EditTextPreference mMnc;
    private EditTextPreference mMvnoMatchData;
    private String mMvnoMatchDataStr;
    private ListPreference mMvnoType;
    private String mMvnoTypeStr;
    private EditTextPreference mName;
    private boolean mNameChanged = false;
    private EditTextPreference mPassword;
    private EditTextPreference mPort;
    private Uri mProviderUri = Carriers.CONTENT_URI;
    private EditTextPreference mProxy;
    private Resources mRes;
    private EditTextPreference mServer;
    private int mSubId;
    private TelephonyManager mTelephonyManager;
    private EditTextPreference mUser;
    private String originalApnName = "";

    public static class ErrorDialog extends DialogFragment {
        public static void showError(ApnEditor editor) {
            ErrorDialog dialog = new ErrorDialog();
            dialog.setTargetFragment(editor, 0);
            dialog.show(editor.getFragmentManager(), "error");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getContext()).setPositiveButton(17039370, null).setMessage(((ApnEditor) getTargetFragment()).getErrorMsg()).create();
        }
    }

    public void onCreate(Bundle icicle) {
        int i;
        super.onCreate(icicle);
        this.mHwCustApnEditorHwBase = (HwCustApnEditorHwBase) HwCustUtils.createObj(HwCustApnEditorHwBase.class, new Object[]{this});
        if (!Utils.isSmsCapable(getContext())) {
            removePreference("apn_mms_proxy");
            removePreference("apn_mms_port");
            removePreference("apn_mmsc");
        }
        sNotSet = getResources().getString(2131625368);
        this.mName = (EditTextPreference) findPreference("apn_name");
        this.mApn = (EditTextPreference) findPreference("apn_apn");
        this.mProxy = (EditTextPreference) findPreference("apn_http_proxy");
        this.mPort = (EditTextPreference) findPreference("apn_http_port");
        this.mUser = (EditTextPreference) findPreference("apn_user");
        this.mServer = (EditTextPreference) findPreference("apn_server");
        this.mPassword = (EditTextPreference) findPreference("apn_password");
        this.mMmsProxy = (EditTextPreference) findPreference("apn_mms_proxy");
        this.mMmsPort = (EditTextPreference) findPreference("apn_mms_port");
        this.mMmsc = (EditTextPreference) findPreference("apn_mmsc");
        this.mMcc = (EditTextPreference) findPreference("apn_mcc");
        this.mMnc = (EditTextPreference) findPreference("apn_mnc");
        this.mApnType = (EditTextPreference) findPreference("apn_type");
        this.mAuthType = (ListPreference) findPreference("auth_type");
        this.mAuthType.setOnPreferenceChangeListener(this);
        this.mProtocol = (ListPreference) findPreference("apn_protocol");
        this.mProtocol.setOnPreferenceChangeListener(this);
        this.mRoamingProtocol = (ListPreference) findPreference("apn_roaming_protocol");
        this.mRoamingProtocol.setOnPreferenceChangeListener(this);
        this.mCarrierEnabled = (SwitchPreference) findPreference("carrier_enabled");
        this.mBearerMulti = (MultiSelectListPreference) findPreference("bearer_multi");
        CharSequence[] bearerEntries = this.mBearerMulti.getEntries();
        CharSequence[] bearerEntryValues = this.mBearerMulti.getEntryValues();
        if (bearerEntries.length < bearerEntryValues.length) {
            CharSequence[] newBearerEntriesValues = new CharSequence[bearerEntries.length];
            for (i = 0; i < newBearerEntriesValues.length; i++) {
                newBearerEntriesValues[i] = bearerEntryValues[i];
            }
            this.mBearerMulti.setEntryValues(newBearerEntriesValues);
        } else if (bearerEntries.length > bearerEntryValues.length) {
            CharSequence[] newBearerEntries = new CharSequence[bearerEntryValues.length];
            for (i = 0; i < newBearerEntries.length; i++) {
                newBearerEntries[i] = bearerEntries[i];
            }
            this.mBearerMulti.setEntries(newBearerEntries);
        }
        this.mBearerMulti.setOnPreferenceChangeListener(this);
        if (this.mHwCustApnEditorHwBase != null) {
            this.mHwCustApnEditorHwBase.custForApnBearer(getPreferenceScreen(), this.mBearerMulti);
        }
        this.mMvnoType = (ListPreference) findPreference("mvno_type");
        this.mMvnoType.setOnPreferenceChangeListener(this);
        this.mMvnoMatchData = (EditTextPreference) findPreference("mvno_match_data");
        if (this.mHwCustApnEditorHwBase != null) {
            this.mHwCustApnEditorHwBase.removeApnMvno(this.mMvnoType, this.mMvnoMatchData, getPreferenceScreen());
        }
        this.mRes = getResources();
        Intent intent = getIntent();
        String action = intent.getAction();
        this.mSubId = intent.getIntExtra("sub_id", -1);
        this.mSlotId = intent.getIntExtra("slotid", 0);
        if (this.mHwCustApnEditorHwBase != null) {
            this.mProviderUri = this.mHwCustApnEditorHwBase.getApnUri(this.mProviderUri, this.mSlotId);
        }
        this.mFirstTime = icicle == null;
        if (icicle != null) {
            this.mNameChanged = icicle.getBoolean("name_changed", false);
            this.mBearerInitialVal = icicle.getInt("bearer", 0);
        }
        if (intent.getData() == null) {
            Log.e(TAG, "Failed to edit apn, data=" + getIntent().getData());
            finish();
            return;
        }
        Uri uri;
        if ("android.intent.action.EDIT".equals(action)) {
            uri = intent.getData();
            if (uri.isPathPrefixMatch(Carriers.CONTENT_URI)) {
                this.mUri = uri;
            } else {
                Log.e(TAG, "Edit request not for carrier table. Uri: " + uri);
                finish();
                return;
            }
        } else if ("android.intent.action.INSERT".equals(action)) {
            if (this.mFirstTime || icicle.getInt("pos") == 0) {
                uri = intent.getData();
                if (uri.isPathPrefixMatch(Carriers.CONTENT_URI)) {
                    this.mUri = getContentResolver().insert(uri, new ContentValues());
                } else {
                    Log.e(TAG, "Insert request not for carrier table. Uri: " + uri);
                    finish();
                    return;
                }
            }
            this.mUri = ContentUris.withAppendedId(this.mProviderUri, (long) icicle.getInt("pos"));
            this.mNewApn = true;
            this.mMvnoTypeStr = intent.getStringExtra("mvno_type");
            this.mMvnoMatchDataStr = intent.getStringExtra("mvno_match_data");
            if (this.mUri == null) {
                Log.w(TAG, "Failed to insert new telephony provider into " + getIntent().getData());
                finish();
                return;
            }
            setResult(-1, new Intent().setAction(this.mUri.toString()));
        } else {
            finish();
            return;
        }
        this.mIsPresetAPN = isPresetAPN(this.mUri);
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        for (i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(this);
        }
        if (this.mHwCustApnEditorHwBase != null) {
            this.mIsReadOnly = this.mHwCustApnEditorHwBase.isApnReadable(getContext(), this.mSlotId);
        }
        if (this.mHwCustApnEditorHwBase != null) {
            this.apnDisableEditOrDel = initApnEidtOrDel(getContext(), this.mSlotId);
        }
        SettingsExtUtils.setEmuiTheme(getContext());
    }

    protected int getMetricsCategory() {
        return 13;
    }

    public void onResume() {
        super.onResume();
        insertEmptyRecordIfNeed();
        loadDataAndFillUiIfNeed();
    }

    public void onPause() {
        super.onPause();
    }

    protected void fillUi(String defaultOperatorNumeric) {
        String currentOperatorNumeric;
        String mcc;
        String mnc;
        CharSequence[] authTypeValues;
        String mccmnc;
        String authType;
        Object obj;
        CharSequence roamingProtocol;
        String[] ipValue;
        HashSet<String> bearers;
        int bearerBitmask;
        int i;
        String authVal;
        if (this.mNameChanged || this.mCursor == null || this.mHwCustApnEditorHwBase == null) {
            if (this.mFirstTime) {
            }
            if (this.mFirstTime) {
                this.mFirstTime = false;
                if (this.mCursor == null) {
                    this.mName.setText(this.mCursor.getString(1));
                    this.mApn.setText(this.mCursor.getString(2));
                    this.mProxy.setText(this.mCursor.getString(3));
                    this.mPort.setText(this.mCursor.getString(4));
                    this.mUser.setText(this.mCursor.getString(5));
                    this.mServer.setText(this.mCursor.getString(6));
                    this.mPassword.setText(this.mCursor.getString(7));
                    if (this.mMmsProxy != null) {
                        this.mMmsProxy.setText(this.mCursor.getString(12));
                    }
                    if (this.mMmsPort != null) {
                        this.mMmsPort.setText(this.mCursor.getString(13));
                    }
                    if (this.mMmsc != null) {
                        this.mMmsc.setText(this.mCursor.getString(8));
                    }
                    if (isRoamingBroker()) {
                        this.mMcc.setText(this.mCursor.getString(9));
                        this.mMnc.setText(this.mCursor.getString(10));
                    } else {
                        currentOperatorNumeric = getSimOperator();
                        if (currentOperatorNumeric != null && currentOperatorNumeric.length() > 4) {
                            this.mMcc.setText(currentOperatorNumeric.substring(0, 3));
                            this.mMnc.setText(currentOperatorNumeric.substring(3));
                        }
                    }
                    this.mApnType.setText(this.mCursor.getString(15));
                    if (this.mPppPwd != null) {
                        this.mPppPwd.setText(this.mCursor.getString(23));
                    }
                    if (this.mNewApn) {
                        if (defaultOperatorNumeric != null && defaultOperatorNumeric.length() > 4) {
                            mcc = defaultOperatorNumeric.substring(0, 3);
                            mnc = defaultOperatorNumeric.substring(3);
                            this.mMcc.setText(mcc);
                            this.mMnc.setText(mnc);
                            this.mCurMnc = mnc;
                            this.mCurMcc = mcc;
                            if (this.mOldContentValues != null) {
                                this.mOldContentValues.put("mcc", mcc);
                                this.mOldContentValues.put("mnc", mnc);
                                this.mOldContentValues.put("numeric", mcc + mnc);
                                this.mOldContentValues.put("current", Integer.valueOf(1));
                            }
                        }
                        if (this.mHwCustApnEditorHwBase != null) {
                            this.mHwCustApnEditorHwBase.setDefaultPort(getContext(), this.mPort);
                            this.mHwCustApnEditorHwBase.setDefaultApnType(this.mApnType);
                        }
                    }
                    int authVal2 = this.mCursor.getInt(14);
                    authTypeValues = this.mAuthType.getEntryValues();
                    if (authTypeValues == null && authVal2 >= 0 && authVal2 < authTypeValues.length) {
                        this.mAuthType.setValueIndex(authVal2);
                    } else if (TelephonyManager.getDefault().getPhoneType() != 2) {
                        this.mAuthType.setValue("3");
                    } else {
                        this.mAuthType.setValue(null);
                    }
                    mccmnc = this.mCurMcc + this.mCurMnc;
                    authType = getAuthTypeConfiguration(mccmnc);
                    if (authType != null) {
                        this.mAuthType.setValue(authType);
                    }
                    obj = null;
                    roamingProtocol = null;
                    if (this.mHwCustApnEditorHwBase != null) {
                        ipValue = this.mHwCustApnEditorHwBase.getDefaultProtocol(getContext(), mccmnc);
                        if (ipValue.length >= 2) {
                            obj = ipValue[0];
                            roamingProtocol = ipValue[1];
                        }
                    }
                    if (TextUtils.isEmpty(obj) && this.mNewApn) {
                        this.mProtocol.setValue(obj);
                    } else {
                        this.mProtocol.setValue(this.mCursor.getString(16));
                    }
                    if (TextUtils.isEmpty(roamingProtocol) && this.mNewApn) {
                        this.mRoamingProtocol.setValue(roamingProtocol);
                    } else {
                        this.mRoamingProtocol.setValue(this.mCursor.getString(20));
                    }
                    this.mCarrierEnabled.setChecked(this.mCursor.getInt(17) != 1);
                    this.mBearerInitialVal = this.mCursor.getInt(18);
                    bearers = new HashSet();
                    bearerBitmask = this.mCursor.getInt(19);
                    if (bearerBitmask == 0) {
                        i = 1;
                        while (bearerBitmask != 0) {
                            if ((bearerBitmask & 1) == 1) {
                                bearers.add("" + i);
                            }
                            bearerBitmask >>= 1;
                            i++;
                        }
                    } else if (this.mBearerInitialVal == 0) {
                        bearers.add("0");
                    }
                    if (!(this.mBearerInitialVal == 0 || bearers.contains("" + this.mBearerInitialVal))) {
                        bearers.add("" + this.mBearerInitialVal);
                    }
                    this.mBearerMulti.setValues(bearers);
                    this.mMvnoType.setValue(this.mCursor.getString(21));
                    this.mMvnoMatchData.setEnabled(false);
                    this.mMvnoMatchData.setText(this.mCursor.getString(22));
                    if (!(!this.mNewApn || this.mMvnoTypeStr == null || this.mMvnoMatchDataStr == null)) {
                        this.mMvnoType.setValue(this.mMvnoTypeStr);
                        this.mMvnoMatchData.setText(this.mMvnoMatchDataStr);
                    }
                } else {
                    return;
                }
            }
            this.mName.setSummary(checkNull(this.mName.getText()));
            this.mApn.setSummary(checkNull(this.mApn.getText()));
            this.mProxy.setSummary(checkNull(this.mProxy.getText()));
            this.mPort.setSummary(checkNull(this.mPort.getText()));
            this.mUser.setSummary(checkNull(this.mUser.getText()));
            this.mServer.setSummary(checkNull(this.mServer.getText()));
            this.mPassword.setSummary(starify(this.mPassword.getText()));
            if (this.mMmsProxy != null) {
                this.mMmsProxy.setSummary(checkNull(this.mMmsProxy.getText()));
            }
            if (this.mMmsPort != null) {
                this.mMmsPort.setSummary(checkNull(this.mMmsPort.getText()));
            }
            if (this.mMmsc != null) {
                this.mMmsc.setSummary(checkNull(this.mMmsc.getText()));
            }
            this.mMcc.setSummary(checkNull(this.mMcc.getText()));
            this.mMnc.setSummary(checkNull(this.mMnc.getText()));
            this.mApnType.setSummary(checkNull(this.mApnType.getText()));
            if (this.mPppPwd != null) {
                this.mPppPwd.setSummary(checkNull(this.mPppPwd.getText()));
            }
            authVal = this.mAuthType.getValue();
            if (authVal == null) {
                int authValIndex = Integer.parseInt(authVal);
                this.mAuthType.setValueIndex(authValIndex);
                this.mAuthType.setSummary(this.mRes.getStringArray(2131361876)[authValIndex]);
            } else {
                this.mAuthType.setSummary(sNotSet);
            }
            this.mProtocol.setSummary(checkNull(protocolDescription(this.mProtocol.getValue(), this.mProtocol)));
            this.mRoamingProtocol.setSummary(checkNull(protocolDescription(this.mRoamingProtocol.getValue(), this.mRoamingProtocol)));
            this.mBearerMulti.setSummary(checkNull(bearerMultiDescription(this.mBearerMulti.getValues())));
            this.mMvnoType.setSummary(checkNull(mvnoDescription(this.mMvnoType.getValue())));
            this.mMvnoMatchData.setSummary(checkNull(this.mMvnoMatchData.getText()));
            if (this.mIsPresetAPN && SystemProperties.getBoolean("ro.config.cdma_apn_unable", false)) {
                Log.i(TAG, "CDMA apn is pre-setted,user could not edit MmsProxy,MmsPort and Mmsc in fillUi.");
                if (this.mMmsProxy != null) {
                    this.mMmsProxy.setEnabled(false);
                }
                if (this.mMmsPort != null) {
                    this.mMmsPort.setEnabled(false);
                }
                if (this.mMmsc != null) {
                    this.mMmsc.setEnabled(false);
                }
            }
            if (this.mHwCustApnEditorHwBase != null) {
                this.isSprintConvertible = this.mHwCustApnEditorHwBase.isSprintConvertibleApn(getContext(), this.originalApnName);
            }
            if (this.mIsPresetAPN && !this.isSprintConvertible && ((SystemProperties.getBoolean("ro.config.pre_apn_unable", false) && (this.mHwCustApnEditorHwBase == null || !this.mHwCustApnEditorHwBase.isDunApnEditableAndDeletable(getContext(), this.mCursor))) || this.mIsReadOnly || this.apnDisableEditOrDel[0])) {
                Log.i(TAG, "APN is pre-setted,user could not edit or delete it in fillUi.");
                this.mName.setEnabled(false);
                this.mApn.setEnabled(false);
                this.mProxy.setEnabled(false);
                this.mPort.setEnabled(false);
                this.mUser.setEnabled(false);
                this.mServer.setEnabled(false);
                this.mPassword.setEnabled(false);
                this.mBearerMulti.setEnabled(false);
                if (this.mMmsProxy != null) {
                    this.mMmsProxy.setEnabled(false);
                }
                if (this.mMmsPort != null) {
                    this.mMmsPort.setEnabled(false);
                }
                if (this.mMmsc != null) {
                    this.mMmsc.setEnabled(false);
                }
                this.mMcc.setEnabled(false);
                this.mMnc.setEnabled(false);
                this.mAuthType.setEnabled(false);
                this.mApnType.setEnabled(false);
                if (this.mHwCustApnEditorHwBase == null || this.mHwCustApnEditorHwBase.disableProtocol()) {
                    this.mProtocol.setEnabled(false);
                    this.mRoamingProtocol.setEnabled(false);
                }
                this.mMvnoType.setEnabled(false);
                this.mMvnoMatchData.setEnabled(false);
                if (this.mPppPwd != null) {
                    this.mPppPwd.setEnabled(false);
                }
            }
            if (getResources().getBoolean(2131492918)) {
                this.mCarrierEnabled.setEnabled(false);
            } else {
                this.mCarrierEnabled.setEnabled(true);
            }
        }
        this.originalApnName = this.mHwCustApnEditorHwBase.getApnDisplayTitle(getContext(), this.mCursor.getString(1));
        this.mName.setText(this.originalApnName);
        if (this.mFirstTime) {
            this.mFirstTime = false;
            if (this.mCursor == null) {
                this.mName.setText(this.mCursor.getString(1));
                this.mApn.setText(this.mCursor.getString(2));
                this.mProxy.setText(this.mCursor.getString(3));
                this.mPort.setText(this.mCursor.getString(4));
                this.mUser.setText(this.mCursor.getString(5));
                this.mServer.setText(this.mCursor.getString(6));
                this.mPassword.setText(this.mCursor.getString(7));
                if (this.mMmsProxy != null) {
                    this.mMmsProxy.setText(this.mCursor.getString(12));
                }
                if (this.mMmsPort != null) {
                    this.mMmsPort.setText(this.mCursor.getString(13));
                }
                if (this.mMmsc != null) {
                    this.mMmsc.setText(this.mCursor.getString(8));
                }
                if (isRoamingBroker()) {
                    this.mMcc.setText(this.mCursor.getString(9));
                    this.mMnc.setText(this.mCursor.getString(10));
                } else {
                    currentOperatorNumeric = getSimOperator();
                    this.mMcc.setText(currentOperatorNumeric.substring(0, 3));
                    this.mMnc.setText(currentOperatorNumeric.substring(3));
                }
                this.mApnType.setText(this.mCursor.getString(15));
                if (this.mPppPwd != null) {
                    this.mPppPwd.setText(this.mCursor.getString(23));
                }
                if (this.mNewApn) {
                    mcc = defaultOperatorNumeric.substring(0, 3);
                    mnc = defaultOperatorNumeric.substring(3);
                    this.mMcc.setText(mcc);
                    this.mMnc.setText(mnc);
                    this.mCurMnc = mnc;
                    this.mCurMcc = mcc;
                    if (this.mOldContentValues != null) {
                        this.mOldContentValues.put("mcc", mcc);
                        this.mOldContentValues.put("mnc", mnc);
                        this.mOldContentValues.put("numeric", mcc + mnc);
                        this.mOldContentValues.put("current", Integer.valueOf(1));
                    }
                    if (this.mHwCustApnEditorHwBase != null) {
                        this.mHwCustApnEditorHwBase.setDefaultPort(getContext(), this.mPort);
                        this.mHwCustApnEditorHwBase.setDefaultApnType(this.mApnType);
                    }
                }
                int authVal22 = this.mCursor.getInt(14);
                authTypeValues = this.mAuthType.getEntryValues();
                if (authTypeValues == null) {
                }
                if (TelephonyManager.getDefault().getPhoneType() != 2) {
                    this.mAuthType.setValue(null);
                } else {
                    this.mAuthType.setValue("3");
                }
                mccmnc = this.mCurMcc + this.mCurMnc;
                authType = getAuthTypeConfiguration(mccmnc);
                if (authType != null) {
                    this.mAuthType.setValue(authType);
                }
                obj = null;
                roamingProtocol = null;
                if (this.mHwCustApnEditorHwBase != null) {
                    ipValue = this.mHwCustApnEditorHwBase.getDefaultProtocol(getContext(), mccmnc);
                    if (ipValue.length >= 2) {
                        obj = ipValue[0];
                        roamingProtocol = ipValue[1];
                    }
                }
                if (TextUtils.isEmpty(obj)) {
                }
                this.mProtocol.setValue(this.mCursor.getString(16));
                if (TextUtils.isEmpty(roamingProtocol)) {
                }
                this.mRoamingProtocol.setValue(this.mCursor.getString(20));
                if (this.mCursor.getInt(17) != 1) {
                }
                this.mCarrierEnabled.setChecked(this.mCursor.getInt(17) != 1);
                this.mBearerInitialVal = this.mCursor.getInt(18);
                bearers = new HashSet();
                bearerBitmask = this.mCursor.getInt(19);
                if (bearerBitmask == 0) {
                    i = 1;
                    while (bearerBitmask != 0) {
                        if ((bearerBitmask & 1) == 1) {
                            bearers.add("" + i);
                        }
                        bearerBitmask >>= 1;
                        i++;
                    }
                } else if (this.mBearerInitialVal == 0) {
                    bearers.add("0");
                }
                bearers.add("" + this.mBearerInitialVal);
                this.mBearerMulti.setValues(bearers);
                this.mMvnoType.setValue(this.mCursor.getString(21));
                this.mMvnoMatchData.setEnabled(false);
                this.mMvnoMatchData.setText(this.mCursor.getString(22));
                this.mMvnoType.setValue(this.mMvnoTypeStr);
                this.mMvnoMatchData.setText(this.mMvnoMatchDataStr);
            } else {
                return;
            }
        }
        this.mName.setSummary(checkNull(this.mName.getText()));
        this.mApn.setSummary(checkNull(this.mApn.getText()));
        this.mProxy.setSummary(checkNull(this.mProxy.getText()));
        this.mPort.setSummary(checkNull(this.mPort.getText()));
        this.mUser.setSummary(checkNull(this.mUser.getText()));
        this.mServer.setSummary(checkNull(this.mServer.getText()));
        this.mPassword.setSummary(starify(this.mPassword.getText()));
        if (this.mMmsProxy != null) {
            this.mMmsProxy.setSummary(checkNull(this.mMmsProxy.getText()));
        }
        if (this.mMmsPort != null) {
            this.mMmsPort.setSummary(checkNull(this.mMmsPort.getText()));
        }
        if (this.mMmsc != null) {
            this.mMmsc.setSummary(checkNull(this.mMmsc.getText()));
        }
        this.mMcc.setSummary(checkNull(this.mMcc.getText()));
        this.mMnc.setSummary(checkNull(this.mMnc.getText()));
        this.mApnType.setSummary(checkNull(this.mApnType.getText()));
        if (this.mPppPwd != null) {
            this.mPppPwd.setSummary(checkNull(this.mPppPwd.getText()));
        }
        authVal = this.mAuthType.getValue();
        if (authVal == null) {
            this.mAuthType.setSummary(sNotSet);
        } else {
            int authValIndex2 = Integer.parseInt(authVal);
            this.mAuthType.setValueIndex(authValIndex2);
            this.mAuthType.setSummary(this.mRes.getStringArray(2131361876)[authValIndex2]);
        }
        this.mProtocol.setSummary(checkNull(protocolDescription(this.mProtocol.getValue(), this.mProtocol)));
        this.mRoamingProtocol.setSummary(checkNull(protocolDescription(this.mRoamingProtocol.getValue(), this.mRoamingProtocol)));
        this.mBearerMulti.setSummary(checkNull(bearerMultiDescription(this.mBearerMulti.getValues())));
        this.mMvnoType.setSummary(checkNull(mvnoDescription(this.mMvnoType.getValue())));
        this.mMvnoMatchData.setSummary(checkNull(this.mMvnoMatchData.getText()));
        Log.i(TAG, "CDMA apn is pre-setted,user could not edit MmsProxy,MmsPort and Mmsc in fillUi.");
        if (this.mMmsProxy != null) {
            this.mMmsProxy.setEnabled(false);
        }
        if (this.mMmsPort != null) {
            this.mMmsPort.setEnabled(false);
        }
        if (this.mMmsc != null) {
            this.mMmsc.setEnabled(false);
        }
        if (this.mHwCustApnEditorHwBase != null) {
            this.isSprintConvertible = this.mHwCustApnEditorHwBase.isSprintConvertibleApn(getContext(), this.originalApnName);
        }
        Log.i(TAG, "APN is pre-setted,user could not edit or delete it in fillUi.");
        this.mName.setEnabled(false);
        this.mApn.setEnabled(false);
        this.mProxy.setEnabled(false);
        this.mPort.setEnabled(false);
        this.mUser.setEnabled(false);
        this.mServer.setEnabled(false);
        this.mPassword.setEnabled(false);
        this.mBearerMulti.setEnabled(false);
        if (this.mMmsProxy != null) {
            this.mMmsProxy.setEnabled(false);
        }
        if (this.mMmsPort != null) {
            this.mMmsPort.setEnabled(false);
        }
        if (this.mMmsc != null) {
            this.mMmsc.setEnabled(false);
        }
        this.mMcc.setEnabled(false);
        this.mMnc.setEnabled(false);
        this.mAuthType.setEnabled(false);
        this.mApnType.setEnabled(false);
        this.mProtocol.setEnabled(false);
        this.mRoamingProtocol.setEnabled(false);
        this.mMvnoType.setEnabled(false);
        this.mMvnoMatchData.setEnabled(false);
        if (this.mPppPwd != null) {
            this.mPppPwd.setEnabled(false);
        }
        if (getResources().getBoolean(2131492918)) {
            this.mCarrierEnabled.setEnabled(false);
        } else {
            this.mCarrierEnabled.setEnabled(true);
        }
    }

    private String protocolDescription(String raw, ListPreference protocol) {
        int protocolIndex = protocol.findIndexOfValue(raw);
        if (protocolIndex == -1) {
            return null;
        }
        try {
            return this.mRes.getStringArray(2131361878)[protocolIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.w(TAG, "protocolDescription ArrayIndexOutOfBoundsException:", e);
            return null;
        }
    }

    private String bearerMultiDescription(Set<String> raw) {
        String[] values = this.mRes.getStringArray(2131361880);
        StringBuilder retVal = new StringBuilder();
        boolean first = true;
        for (String bearer : raw) {
            int bearerIndex = this.mBearerMulti.findIndexOfValue(bearer);
            if (first) {
                try {
                    retVal.append(values[bearerIndex]);
                    first = false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.w(TAG, "bearerMultiDescription ArrayIndexOutOfBoundsException:", e);
                }
            } else {
                retVal.append(", ").append(values[bearerIndex]);
            }
        }
        String val = retVal.toString();
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        return val;
    }

    private String mvnoDescription(String newValue) {
        boolean z = false;
        int mvnoIndex = this.mMvnoType.findIndexOfValue(newValue);
        String oldValue = this.mMvnoType.getValue();
        if (mvnoIndex == -1) {
            return null;
        }
        String[] values = this.mRes.getStringArray(2131361882);
        EditTextPreference editTextPreference = this.mMvnoMatchData;
        if (mvnoIndex != 0) {
            z = true;
        }
        editTextPreference.setEnabled(z);
        if (!(newValue == null || newValue.equals(oldValue))) {
            if (values[mvnoIndex].equals("SPN")) {
                this.mMvnoMatchData.setText(this.mTelephonyManager.getSimOperatorName());
            } else if (values[mvnoIndex].equals("IMSI")) {
                this.mMvnoMatchData.setText(this.mTelephonyManager.getSimOperator(this.mSubId) + "x");
            } else if (values[mvnoIndex].equals("GID")) {
                this.mMvnoMatchData.setText(this.mTelephonyManager.getGroupIdLevel1());
            }
        }
        try {
            return values[mvnoIndex];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.w(TAG, "bearerMultiDescription ArrayIndexOutOfBoundsException:", e);
            return null;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String str = null;
        if (preference.equals(this.mPassword)) {
            preference.setSummary(starify(newValue != null ? String.valueOf(newValue) : ""));
        } else if (!(preference.equals(this.mCarrierEnabled) || preference.equals(this.mBearerMulti))) {
            if (newValue != null) {
                str = String.valueOf(newValue);
            }
            preference.setSummary(checkNull(str));
        }
        String key = preference.getKey();
        if ("auth_type".equals(key)) {
            try {
                int index = Integer.parseInt((String) newValue);
                this.mAuthType.setValueIndex(index);
                this.mAuthType.setSummary(this.mRes.getStringArray(2131361876)[index]);
            } catch (NumberFormatException e) {
                Log.w(TAG, "onPreferenceChange NumberFormatException:", e);
                return false;
            }
        } else if ("apn_protocol".equals(key)) {
            protocol = protocolDescription((String) newValue, this.mProtocol);
            if (protocol == null) {
                return false;
            }
            this.mProtocol.setValue((String) newValue);
            this.mProtocol.setSummary(protocol);
        } else if ("apn_roaming_protocol".equals(key)) {
            protocol = protocolDescription((String) newValue, this.mRoamingProtocol);
            if (protocol == null) {
                return false;
            }
            this.mRoamingProtocol.setValue((String) newValue);
            this.mRoamingProtocol.setSummary(protocol);
        } else if ("bearer_multi".equals(key)) {
            String bearer = bearerMultiDescription((Set) newValue);
            if (bearer == null) {
                return false;
            }
            this.mBearerMulti.setValues((Set) newValue);
            this.mBearerMulti.setSummary((CharSequence) bearer);
        } else if ("mvno_type".equals(key)) {
            String mvno = mvnoDescription((String) newValue);
            if (mvno == null) {
                return false;
            }
            this.mMvnoType.setValue((String) newValue);
            this.mMvnoType.setSummary(mvno);
        }
        return true;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!this.mIsPresetAPN || (!this.mIsReadOnly && (!this.apnDisableEditOrDel[0] || !this.apnDisableEditOrDel[1]))) {
            if (!this.mIsPresetAPN || ((!SystemProperties.getBoolean("ro.config.pre_apn_NotDel", false) || (this.mHwCustApnEditorHwBase != null && this.mHwCustApnEditorHwBase.isDunApnEditableAndDeletable(getContext(), this.mCursor))) && !this.apnDisableEditOrDel[1])) {
                if (!this.mNewApn) {
                    menu.add(0, 1, 0, 2131625395).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getContext(), ImmersionIcon.IMM_DELETE))).setShowAsAction(1);
                }
                return;
            }
            Log.i(TAG, "APN is pre-setted, user could not edit or delete it onCreateOptionsMenu.");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                deleteApn();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnKeyListener(this);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 0) {
            return false;
        }
        switch (keyCode) {
            case 4:
                if (this.mIsPresetAPN && ((SystemProperties.getBoolean("ro.config.pre_apn_unable", false) && (this.mHwCustApnEditorHwBase == null || !this.mHwCustApnEditorHwBase.isDunApnEditableAndDeletable(getContext(), this.mCursor))) || this.apnDisableEditOrDel[0])) {
                    Log.i(TAG, "APN is pre-setted,no need to show dialog, just exit the activity");
                    finish();
                } else if (testIsAnyItemsChanged()) {
                    showDialog(1);
                } else {
                    finish();
                }
                return true;
            default:
                return false;
        }
    }

    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        String name = this.mName.getText();
        if (!(this.originalApnName == null || name == null || this.originalApnName.equals(name.trim()))) {
            icicle.putBoolean("name_changed", true);
        }
        if (validateAndSave(true) && this.mCursor != null && this.mCursor.getCount() > 0) {
            icicle.putInt("pos", this.mCursor.getInt(0));
            icicle.putInt("bearer", this.mCursor.getInt(18));
        }
    }

    protected boolean validateAndSave(boolean force) {
        String name = checkNotSet(this.mName.getText());
        String apn = checkNotSet(this.mApn.getText());
        String mcc = checkNotSet(this.mMcc.getText());
        String mnc = checkNotSet(this.mMnc.getText());
        if (getErrorMsg() != null && !force) {
            ErrorDialog.showError(this);
            return false;
        } else if (this.mCursor != null && !this.mCursor.moveToFirst()) {
            Log.w(TAG, "Could not go to the first row in the Cursor when saving data.");
            return false;
        } else if (!force || !this.mNewApn || name.length() >= 1 || apn.length() >= 1) {
            int bearerVal;
            ContentValues values = new ContentValues();
            if (!this.originalApnName.equals(name.trim())) {
                String str = "name";
                if (name.length() < 1) {
                    name = getResources().getString(2131626181);
                }
                values.put(str, name);
            }
            values.put("apn", apn);
            values.put("proxy", checkNotSet(this.mProxy.getText()));
            values.put("port", checkNotSet(this.mPort.getText()));
            if (this.mMmsProxy != null) {
                values.put("mmsproxy", checkNotSet(this.mMmsProxy.getText()));
            }
            if (this.mMmsPort != null) {
                values.put("mmsport", checkNotSet(this.mMmsPort.getText()));
            }
            values.put("user", checkNotSet(this.mUser.getText()));
            values.put("server", checkNotSet(this.mServer.getText()));
            values.put("password", checkNotSet(this.mPassword.getText()));
            if (this.mMmsc != null) {
                values.put("mmsc", checkNotSet(this.mMmsc.getText()));
            }
            String authVal = this.mAuthType.getValue();
            if (authVal != null) {
                values.put("authtype", Integer.valueOf(Integer.parseInt(authVal)));
            }
            values.put("protocol", checkNotSet(this.mProtocol.getValue()));
            values.put("roaming_protocol", checkNotSet(this.mRoamingProtocol.getValue()));
            values.put("type", checkNotSet(this.mApnType.getText()));
            if (!(isRoamingBroker() && (mcc + mnc).equals(getSimOperator()))) {
                values.put("mcc", mcc);
                values.put("mnc", mnc);
                values.put("numeric", mcc + mnc);
            }
            if (this.mPppPwd != null) {
                values.put("ppppwd", checkNotSet(this.mPppPwd.getText()));
            }
            if (this.mCurMnc != null && this.mCurMcc != null && this.mCurMnc.equals(mnc) && this.mCurMcc.equals(mcc)) {
                values.put("current", Integer.valueOf(1));
            }
            int bearerBitmask = 0;
            for (String bearer : this.mBearerMulti.getValues()) {
                if (Integer.parseInt(bearer) == 0) {
                    bearerBitmask = 0;
                    break;
                }
                bearerBitmask |= ServiceState.getBitmaskForTech(Integer.parseInt(bearer));
            }
            values.put("bearer_bitmask", Integer.valueOf(bearerBitmask));
            if (bearerBitmask == 0 || this.mBearerInitialVal == 0) {
                bearerVal = 0;
            } else if (ServiceState.bitmaskHasTech(bearerBitmask, this.mBearerInitialVal)) {
                bearerVal = this.mBearerInitialVal;
            } else {
                bearerVal = 0;
            }
            values.put("bearer", Integer.valueOf(bearerVal));
            values.put("mvno_type", checkNotSet(this.mMvnoType.getValue()));
            values.put("mvno_match_data", checkNotSet(this.mMvnoMatchData.getText()));
            values.put("carrier_enabled", Integer.valueOf(this.mCarrierEnabled.isChecked() ? 1 : 0));
            if (isAnyItemsChanged(values)) {
                asynUpdateValuesToDb(getContentResolver(), this.mUri, values);
            }
            return true;
        } else {
            getContentResolver().delete(this.mUri, null, null);
            this.mIsDeleted = true;
            this.mNeedReloadData = true;
            return false;
        }
    }

    private boolean testIsAnyItemsChanged() {
        String name = checkNotSet(this.mName.getText());
        String apn = checkNotSet(this.mApn.getText());
        String mcc = checkNotSet(this.mMcc.getText());
        String mnc = checkNotSet(this.mMnc.getText());
        if (this.mCursor == null || this.mCursor.moveToFirst()) {
            int bearerVal;
            ContentValues values = new ContentValues();
            if (!this.originalApnName.equals(name.trim())) {
                String str = "name";
                if (name.length() < 1) {
                    name = getResources().getString(2131626181);
                }
                values.put(str, name);
            }
            values.put("apn", apn);
            values.put("proxy", checkNotSet(this.mProxy.getText()));
            values.put("port", checkNotSet(this.mPort.getText()));
            if (this.mMmsProxy != null) {
                values.put("mmsproxy", checkNotSet(this.mMmsProxy.getText()));
            }
            if (this.mMmsPort != null) {
                values.put("mmsport", checkNotSet(this.mMmsPort.getText()));
            }
            values.put("user", checkNotSet(this.mUser.getText()));
            values.put("server", checkNotSet(this.mServer.getText()));
            values.put("password", checkNotSet(this.mPassword.getText()));
            if (this.mMmsc != null) {
                values.put("mmsc", checkNotSet(this.mMmsc.getText()));
            }
            String authVal = this.mAuthType.getValue();
            if (authVal != null) {
                values.put("authtype", Integer.valueOf(Integer.parseInt(authVal)));
            }
            values.put("protocol", checkNotSet(this.mProtocol.getValue()));
            values.put("roaming_protocol", checkNotSet(this.mRoamingProtocol.getValue()));
            values.put("type", checkNotSet(this.mApnType.getText()));
            if (!(isRoamingBroker() && (mcc + mnc).equals(getSimOperator()))) {
                values.put("mcc", mcc);
                values.put("mnc", mnc);
                values.put("numeric", mcc + mnc);
            }
            if (this.mPppPwd != null) {
                values.put("ppppwd", checkNotSet(this.mPppPwd.getText()));
            }
            if (this.mCurMnc != null && this.mCurMcc != null && this.mCurMnc.equals(mnc) && this.mCurMcc.equals(mcc)) {
                values.put("current", Integer.valueOf(1));
            }
            int bearerBitmask = 0;
            for (String bearer : this.mBearerMulti.getValues()) {
                if (Integer.parseInt(bearer) == 0) {
                    bearerBitmask = 0;
                    break;
                }
                bearerBitmask |= ServiceState.getBitmaskForTech(Integer.parseInt(bearer));
            }
            values.put("bearer_bitmask", Integer.valueOf(bearerBitmask));
            if (bearerBitmask == 0 || this.mBearerInitialVal == 0) {
                bearerVal = 0;
            } else if (ServiceState.bitmaskHasTech(bearerBitmask, this.mBearerInitialVal)) {
                bearerVal = this.mBearerInitialVal;
            } else {
                bearerVal = 0;
            }
            values.put("bearer", Integer.valueOf(bearerVal));
            values.put("mvno_type", checkNotSet(this.mMvnoType.getValue()));
            values.put("mvno_match_data", checkNotSet(this.mMvnoMatchData.getText()));
            values.put("carrier_enabled", Integer.valueOf(this.mCarrierEnabled.isChecked() ? 1 : 0));
            if (isAnyItemsChanged(values)) {
                return true;
            }
            return false;
        }
        Log.w(TAG, "Could not go to the first row in the Cursor when saving data.");
        return false;
    }

    private String getErrorMsg() {
        String name = checkNotSet(this.mName.getText());
        String apn = checkNotSet(this.mApn.getText());
        String mcc = checkNotSet(this.mMcc.getText());
        String mnc = checkNotSet(this.mMnc.getText());
        if (name.length() < 1) {
            return this.mRes.getString(2131625400);
        }
        if (apn.length() < 1) {
            return this.mRes.getString(2131625401);
        }
        if (mcc.length() != 3) {
            return this.mRes.getString(2131625402);
        }
        if ((mnc.length() & 65534) != 2) {
            return this.mRes.getString(2131625403);
        }
        return null;
    }

    private void deleteApn() {
        getContentResolver().delete(this.mUri, null, null);
        finish();
    }

    private String starify(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        }
        char[] password = new char[value.length()];
        for (int i = 0; i < password.length; i++) {
            password[i] = '*';
        }
        return new String(password);
    }

    private String checkNull(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        }
        return value;
    }

    private String checkNotSet(String value) {
        if (value == null || value.equals(sNotSet)) {
            return "";
        }
        return value;
    }
}
