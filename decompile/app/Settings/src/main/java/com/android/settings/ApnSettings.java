package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony.Carriers;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.uicc.UiccController;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class ApnSettings extends ApnSettingsHwBase implements OnPreferenceChangeListener {
    private static final Uri DEFAULTAPN_URI = Uri.parse("content://telephony/carriers/restore");
    private static final Uri URL_RESTOREAPN_USING_SUBID = Uri.parse("content://telephony/carriers/restore/subId");
    private static final Uri URL_TELEPHONY_USING_SUBID = Uri.parse("content://telephony/carriers/subId");
    private static boolean mRestoreDefaultApnMode;
    private boolean mAllowAddingApns;
    private boolean mHideImsApn = true;
    private boolean mHideXcapApn = true;
    private HwCustApnSettingsHwBase mHwCustApnSettingsHwBase = null;
    private IntentFilter mMobileStateFilter;
    private final BroadcastReceiver mMobileStateReceiver = new BroadcastReceiver() {
        private static final /* synthetic */ int[] -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues = null;

        private static /* synthetic */ int[] -getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues() {
            if (-com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues != null) {
                return -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues;
            }
            int[] iArr = new int[DataState.values().length];
            try {
                iArr[DataState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DataState.CONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DataState.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DataState.SUSPENDED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues = iArr;
            return iArr;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.ANY_DATA_STATE")) {
                switch (AnonymousClass1.-getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues()[ApnSettings.getMobileDataState(intent).ordinal()]) {
                    case 1:
                        if (!ApnSettings.mRestoreDefaultApnMode) {
                            ApnSettings.this.fillList();
                            break;
                        }
                        break;
                }
            }
            if (intent.getAction().equals("android.intent.action.refreshapn")) {
                ApnSettings.this.fillList();
            }
        }
    };
    private String mMvnoMatchData;
    private String mMvnoType;
    private RestoreApnProcessHandler mRestoreApnProcessHandler;
    private RestoreApnUiHandler mRestoreApnUiHandler;
    private HandlerThread mRestoreDefaultApnThread;
    private String mSelectedKey;
    private SubscriptionInfo mSubscriptionInfo;
    private UiccController mUiccController;
    private UserManager mUm;
    private boolean mUnavailable;
    private UserManager mUserManager;

    private class RestoreApnProcessHandler extends Handler {
        private Handler mRestoreApnUiHandler;

        public RestoreApnProcessHandler(Looper looper, Handler restoreApnUiHandler) {
            super(looper);
            this.mRestoreApnUiHandler = restoreApnUiHandler;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Uri uri;
                    ContentResolver resolver = ApnSettings.this.getContentResolver();
                    if (ApnSettings.this.mHwCustApnSettingsHwBase != null) {
                        uri = ApnSettings.this.mHwCustApnSettingsHwBase.getRestoreAPnUri(ApnSettings.DEFAULTAPN_URI, ApnSettings.this.mSubscription);
                    } else {
                        uri = ApnSettings.DEFAULTAPN_URI;
                    }
                    if (TelephonyManager.getDefault().isMultiSimEnabled() && !SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
                        Log.d("ApnSettings", "EVENT_RESTORE_DEFAULTAPN_START mSubscription" + ApnSettings.this.mSubscription);
                        uri = ContentUris.withAppendedId(ApnSettings.URL_RESTOREAPN_USING_SUBID, (long) ApnSettings.this.mSubscription);
                    }
                    resolver.delete(uri, null, null);
                    this.mRestoreApnUiHandler.sendEmptyMessage(2);
                    return;
                default:
                    return;
            }
        }
    }

    private class RestoreApnUiHandler extends Handler {
        private RestoreApnUiHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    Activity activity = ApnSettings.this.getActivity();
                    if (activity != null) {
                        ApnSettings.this.showApnReminderDialog(activity);
                        ApnSettings.this.fillList();
                        ApnSettings.this.getPreferenceScreen().setEnabled(true);
                        ApnSettings.mRestoreDefaultApnMode = false;
                        try {
                            ApnSettings.this.removeDialog(1001);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    ApnSettings.mRestoreDefaultApnMode = false;
                    return;
            }
        }
    }

    public ApnSettings() {
        super("no_config_mobile_networks");
    }

    private static DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra("state");
        if (str != null) {
            return (DataState) Enum.valueOf(DataState.class, str);
        }
        return DataState.DISCONNECTED;
    }

    protected int getMetricsCategory() {
        return 12;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Activity activity = getActivity();
        Intent i = activity.getIntent();
        int subId = -1;
        if (i != null) {
            subId = i.getIntExtra("sub_id", -1);
        }
        this.mHwCustApnSettingsHwBase = (HwCustApnSettingsHwBase) HwCustUtils.createObj(HwCustApnSettingsHwBase.class, new Object[]{this});
        this.mUm = (UserManager) getSystemService("user");
        this.mMobileStateFilter = new IntentFilter("android.intent.action.ANY_DATA_STATE");
        this.mMobileStateFilter.addAction("android.intent.action.refreshapn");
        setIfOnlyAvailableForAdmins(true);
        this.mSubscriptionInfo = SubscriptionManager.from(activity).getActiveSubscriptionInfo(subId);
        this.mUiccController = UiccController.getInstance();
        this.mUserManager = UserManager.get(activity);
        this.mAllowAddingApns = ((CarrierConfigManager) getSystemService("carrier_config")).getConfig().getBoolean("allow_adding_apns_bool");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (getEmptyTextView() != null) {
            getEmptyTextView().setText(2131624066);
        }
        this.mUnavailable = isUiRestricted();
        setHasOptionsMenu(!this.mUnavailable);
        if (this.mUnavailable) {
            setPreferenceScreen(new PreferenceScreen(getPrefContext(), null));
            getPreferenceScreen().removeAll();
            return;
        }
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        if (!this.mUnavailable) {
            getActivity().registerReceiver(this.mMobileStateReceiver, this.mMobileStateFilter);
            if (!mRestoreDefaultApnMode) {
                fillList();
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (!this.mUnavailable) {
            getActivity().unregisterReceiver(this.mMobileStateReceiver);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mRestoreDefaultApnThread != null) {
            this.mRestoreDefaultApnThread.quit();
        }
    }

    public EnforcedAdmin getRestrictionEnforcedAdmin() {
        UserHandle user = UserHandle.of(this.mUserManager.getUserHandle());
        if (!this.mUserManager.hasUserRestriction("no_config_mobile_networks", user) || this.mUserManager.hasBaseUserRestriction("no_config_mobile_networks", user)) {
            return null;
        }
        return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
    }

    private void fillList() {
        String where = getOperatorNumericSelection();
        if (TextUtils.isEmpty(where)) {
            Log.d("ApnSettings", "getOperatorNumericSelection is empty ");
            return;
        }
        Cursor cursor;
        String[] args = null;
        SelectionHolder selection = getSelectionForSomeCarriers();
        if (selection != null) {
            where = selection.selection;
            args = selection.selectionArgs;
        }
        if (this.mHideImsApn) {
            where = where + " AND NOT (type='ims')";
        }
        if (this.mHideXcapApn) {
            where = where + " AND NOT (type='xcap')";
        }
        if (this.mHwCustApnSettingsHwBase != null && this.mHwCustApnSettingsHwBase.isSortbyId()) {
            cursor = getContentResolver().query(this.mHwCustApnSettingsHwBase.getApnUri(Carriers.CONTENT_URI, this.mSubscription), new String[]{"_id", "name", "apn", "type", "mvno_type", "mvno_match_data", "visible"}, where, args, "_id ASC");
        } else if (this.mHwCustApnSettingsHwBase != null) {
            cursor = getContentResolver().query(this.mHwCustApnSettingsHwBase.getApnUri(Carriers.CONTENT_URI, this.mSubscription), new String[]{"_id", "name", "apn", "type", "mvno_type", "mvno_match_data", "visible"}, where, args, "name ASC");
        } else {
            cursor = getContentResolver().query(Carriers.CONTENT_URI, new String[]{"_id", "name", "apn", "type", "mvno_type", "mvno_match_data", "visible"}, where, args, "name ASC");
        }
        if (TelephonyManager.getDefault().isMultiSimEnabled() && !SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
            Log.d("ApnSettings", "fillList mSubscription" + this.mSubscription);
            if (cursor != null) {
                cursor.close();
            }
            Uri uri = ContentUris.withAppendedId(URL_TELEPHONY_USING_SUBID, (long) this.mSubscription);
            if (this.mHwCustApnSettingsHwBase == null || !this.mHwCustApnSettingsHwBase.isSortbyId()) {
                cursor = getContentResolver().query(uri, new String[]{"_id", "name", "apn", "type", "mvno_type", "mvno_match_data", "visible"}, where, args, "name ASC");
            } else {
                cursor = getContentResolver().query(uri, new String[]{"_id", "name", "apn", "type", "mvno_type", "mvno_match_data", "visible"}, where, args, "_id ASC");
            }
        }
        if (cursor != null) {
            PreferenceGroup apnList = (PreferenceGroup) findPreference("apn_list");
            clearAllCategories();
            apnList.removeAll();
            ApnPreference.initChecked();
            Log.d("ApnSettings", "[apnTracker]ApnPreference.initChecked(), mSubscription is" + this.mSubscription);
            ArrayList<Preference> otherNoSelectableList = new ArrayList();
            this.mSelectedKey = getSelectedApnKey();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(1);
                if (this.mHwCustApnSettingsHwBase != null) {
                    name = this.mHwCustApnSettingsHwBase.getApnDisplayTitle(getActivity(), name);
                }
                String apn = cursor.getString(2);
                String key = cursor.getString(0);
                String type = cursor.getString(3);
                String mvnoType = cursor.getString(4);
                String mvnoMatchData = cursor.getString(5);
                name = updateApnNameForTelecom(name, cursor, type);
                if (!isShowWapApn(apn, type)) {
                    cursor.moveToNext();
                } else if (this.mHwCustApnSettingsHwBase != null && this.mHwCustApnSettingsHwBase.isHideSpecialAPN(getOperatorNumeric()[0], apn)) {
                    cursor.moveToNext();
                } else if (this.mHwCustApnSettingsHwBase == null || !this.mHwCustApnSettingsHwBase.hideApnCustbyPreferred(getOperatorNumeric()[0], getOrangeSelectedApnName(), apn)) {
                    boolean selectable;
                    ApnPreference pref = new ApnPreference(getPrefContext());
                    pref.setKey(key);
                    pref.setTitle((CharSequence) name);
                    pref.setSummary((CharSequence) apn);
                    pref.setPersistent(false);
                    pref.setSlotId(this.mSubscription);
                    pref.setOnPreferenceChangeListener(this);
                    if (type != null) {
                        if (!type.equals("mms")) {
                            if (!type.equals("dun")) {
                                selectable = true;
                            }
                        }
                        selectable = false;
                    } else {
                        selectable = true;
                    }
                    pref.setSelectable(selectable);
                    if (selectable) {
                        if (this.mSelectedKey != null && this.mSelectedKey.equals(key)) {
                            pref.setChecked();
                        }
                        if (this.mHwCustApnSettingsHwBase != null) {
                            this.mHwCustApnSettingsHwBase.addOrangeSpecialPreference(apn, name, this.mCategory_apn_general, pref, getOrangeSelectedApnName());
                        } else {
                            this.mCategory_apn_general.addPreference(pref);
                        }
                    } else {
                        if (type != null) {
                            if (type.equals("mms")) {
                                if (this.mCategory_apn_mms != null) {
                                    this.mCategory_apn_mms.addPreference(pref);
                                }
                            }
                        }
                        otherNoSelectableList.add(pref);
                    }
                    cursor.moveToNext();
                } else {
                    cursor.moveToNext();
                }
            }
            cursor.close();
            for (Preference preference : otherNoSelectableList) {
                this.mCategory_apn_general.addPreference(preference);
            }
            addPreferenceCategories(apnList);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!this.mUnavailable) {
            if (this.mAllowAddingApns) {
                menu.add(0, 1, 0, getResources().getString(2131625396)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_ADD)));
            }
            menu.add(0, 2, 0, getResources().getString(2131625405)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), 17301589));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                addNewApn();
                return true;
            case 2:
                restoreDefaultApn();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewApn() {
        Uri uri;
        int subId;
        if (this.mHwCustApnSettingsHwBase != null) {
            uri = this.mHwCustApnSettingsHwBase.getApnUri(Carriers.CONTENT_URI, this.mSubscription);
        } else {
            uri = Carriers.CONTENT_URI;
        }
        Intent intent = new Intent("android.intent.action.INSERT", uri);
        intent.putExtra("slotid", this.mSubscription);
        if (this.mSubscriptionInfo != null) {
            subId = this.mSubscriptionInfo.getSubscriptionId();
        } else {
            subId = -1;
        }
        intent.putExtra("sub_id", subId);
        if (!(TextUtils.isEmpty(this.mMvnoType) || TextUtils.isEmpty(this.mMvnoMatchData))) {
            intent.putExtra("mvno_type", this.mMvnoType);
            intent.putExtra("mvno_match_data", this.mMvnoMatchData);
        }
        intent.putExtra("operator", getOperatorNumeric()[0]);
        startActivity(intent);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        Uri uri;
        int pos = Integer.parseInt(preference.getKey());
        if (this.mHwCustApnSettingsHwBase != null) {
            uri = this.mHwCustApnSettingsHwBase.getApnUri(Carriers.CONTENT_URI, this.mSubscription);
        } else {
            uri = Carriers.CONTENT_URI;
        }
        Intent it = new Intent("android.intent.action.EDIT", ContentUris.withAppendedId(uri, (long) pos));
        it.putExtra("slotid", this.mSubscription);
        startActivity(it);
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue instanceof String) {
            setSelectedApnKey((String) newValue);
        }
        return true;
    }

    private void setSelectedApnKey(String key) {
        this.mSelectedKey = key;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", this.mSelectedKey);
        saveSelectedApnKey(resolver, values);
    }

    private String getSelectedApnKey() {
        String key = null;
        Cursor cursor = readSelectedApnKeyFromDb();
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(0);
        }
        cursor.close();
        return key;
    }

    private boolean restoreDefaultApn() {
        if (Utils.isWifiOnly(getContext())) {
            return false;
        }
        showDialog(1001);
        mRestoreDefaultApnMode = true;
        if (this.mRestoreApnUiHandler == null) {
            this.mRestoreApnUiHandler = new RestoreApnUiHandler();
        }
        if (this.mRestoreApnProcessHandler == null || this.mRestoreDefaultApnThread == null) {
            this.mRestoreDefaultApnThread = new HandlerThread("Restore default APN Handler: Process Thread");
            this.mRestoreDefaultApnThread.start();
            this.mRestoreApnProcessHandler = new RestoreApnProcessHandler(this.mRestoreDefaultApnThread.getLooper(), this.mRestoreApnUiHandler);
        }
        this.mRestoreApnProcessHandler.sendEmptyMessage(1);
        return true;
    }

    public Dialog onCreateDialog(int id) {
        if (id != 1001) {
            return null;
        }
        ProgressDialog dialog = new ProgressDialog(getActivity()) {
            public boolean onTouchEvent(MotionEvent event) {
                return true;
            }
        };
        dialog.setMessage(getResources().getString(2131625404));
        dialog.setCancelable(false);
        return dialog;
    }
}
