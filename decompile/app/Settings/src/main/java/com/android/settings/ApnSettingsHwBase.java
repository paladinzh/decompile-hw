package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.internal.telephony.dataconnection.ApnReminderEx;
import java.util.ArrayList;

public class ApnSettingsHwBase extends RestrictedSettingsFragment {
    private static final boolean IS_SPRINT;
    private static final boolean IS_TRACFONE;
    protected static final Uri PREFERAPN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static final boolean[][] plmn_table = new boolean[][]{new boolean[]{true, false, true}, new boolean[]{false, true, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}};
    private static final boolean[][] plmn_table_mms = new boolean[][]{new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}};
    private boolean isLifeEEApnShow = SystemProperties.getBoolean("ro.lifeAndEe.ApnShow", false);
    protected PreferenceCategory mCategory_apn_general;
    protected PreferenceCategory mCategory_apn_mms;
    private HwCustApnSettingsHwBase mCustApnSettingsHwBase;
    private HwTelephonyManager mHwTelephonyManager;
    private boolean mIsChinaTelecomCard = false;
    private boolean mIsFullNetSupport;
    protected int mSubscription = 0;

    static class SelectionHolder {
        String selection;
        String[] selectionArgs;

        SelectionHolder() {
        }
    }

    static {
        boolean equals;
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("378")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        } else {
            equals = false;
        }
        IS_TRACFONE = equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("237")) {
            z = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        }
        IS_SPRINT = z;
    }

    public ApnSettingsHwBase(String restrictionKey) {
        super(restrictionKey);
    }

    private void broadcastPrePostPay() {
        Intent intent = new Intent("android.intent.action.ACTION_PRE_POST_PAY");
        intent.addFlags(536870912);
        intent.putExtra("prePostPayState", false);
        ActivityManagerNative.broadcastStickyIntent(intent, null, ActivityManager.getCurrentUser());
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mHwTelephonyManager = HwTelephonyManager.getDefault();
        this.mIsFullNetSupport = this.mHwTelephonyManager.isFullNetworkSupported();
        try {
            Activity activity = getActivity();
            if (!(activity == null || activity.getIntent() == null)) {
                this.mSubscription = activity.getIntent().getIntExtra("subscription", MSimTelephonyManager.getDefault().getDefaultSubscription());
            }
        } catch (RuntimeException e) {
            Log.d("ApnSettingsHwBase", "unabled to getDefault sim");
        }
        if (this.mSubscription == 0 || this.mSubscription == 1) {
            this.mIsChinaTelecomCard = this.mHwTelephonyManager.isCTSimCard(this.mSubscription);
            Utils.changePermanentMenuKey(getActivity());
            this.mCustApnSettingsHwBase = (HwCustApnSettingsHwBase) HwCustUtils.createObj(HwCustApnSettingsHwBase.class, new Object[]{this});
            return;
        }
        finish();
    }

    protected int getMetricsCategory() {
        return 12;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(2131230734);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (!Utils.isSmsCapable(getActivity())) {
            removePreference("category_apn_mms");
        }
        this.mCategory_apn_general = (PreferenceCategory) findPreference("category_apn_general");
        this.mCategory_apn_mms = (PreferenceCategory) findPreference("category_apn_mms");
        SettingsExtUtils.setEmuiTheme(getActivity());
    }

    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        boolean prePostPay = false;
        if (activity != null) {
            prePostPay = activity.getIntent().getBooleanExtra("prePostPayState", false);
        }
        if (prePostPay) {
            broadcastPrePostPay();
        }
    }

    protected void clearAllCategories() {
        if (this.mCategory_apn_general != null && this.mCategory_apn_general.getPreferenceCount() > 0) {
            this.mCategory_apn_general.removeAll();
        }
        if (this.mCategory_apn_mms != null && this.mCategory_apn_mms.getPreferenceCount() > 0) {
            this.mCategory_apn_mms.removeAll();
        }
    }

    protected void addPreferenceCategories(PreferenceGroup root) {
        if (this.mCategory_apn_general != null && this.mCategory_apn_general.getPreferenceCount() > 0) {
            root.addPreference(this.mCategory_apn_general);
        }
        if (this.mCategory_apn_mms != null && this.mCategory_apn_mms.getPreferenceCount() > 0) {
            root.addPreference(this.mCategory_apn_mms);
        }
    }

    protected String getOperatorNumericSelection() {
        String[] mccmncs = getOperatorNumeric();
        String where = ((mccmncs[0] != null ? "numeric=\"" + mccmncs[0] + "\"" : "") + (mccmncs[1] != null ? " or numeric=\"" + mccmncs[1] + "\"" : "")) + getTelecomOperatorNumericSelection(this.mSubscription);
        Log.d("ApnSettingsHwBase", "getOperatorNumericSelection: " + where);
        return where;
    }

    private boolean isDualModeCard(int subscription) {
        if (this.mHwTelephonyManager != null) {
            int SubType = this.mHwTelephonyManager.getCardType(subscription);
            if (41 == SubType || 43 == SubType || 40 == SubType) {
                return true;
            }
        }
        return false;
    }

    private boolean isCTCardForFullNet(int subscription) {
        if (this.mIsFullNetSupport) {
            return TelephonyManagerEx.isCTSimCard(subscription);
        }
        return false;
    }

    protected String updateApnNameForTelecom(String name, Cursor cursor, String type) {
        int preSet = cursor.getInt(cursor.getColumnIndexOrThrow("visible"));
        Log.d("ApnSettingsHwBase", "preSet: " + preSet);
        if (preSet != 1) {
            return name;
        }
        if (name.equalsIgnoreCase("ctlte")) {
            return getString(2131624337);
        }
        if (name.equalsIgnoreCase("CTNET")) {
            return getString(2131624336);
        }
        if (!name.equalsIgnoreCase("CTWAP") || type.equals("mms")) {
            return name;
        }
        return getString(2131624335);
    }

    private String getTelecomOperatorNumericSelection(int subscription) {
        String where = "";
        if (SystemProperties.getBoolean("ro.config.cmdm_apn_not_display", false)) {
            where = where + "and name != \"CMDM\"";
            Log.d("ApnSettingsHwBase", "ro.config.cmdm_apn_not_display is work, add cmdm apn not display");
        }
        if ((!this.mIsChinaTelecomCard || (!isCurrentSlotSupportLTE(subscription) && (1 != subscription || !isDualModeCard(subscription)))) && !isCTCardForFullNet(subscription)) {
            return where;
        }
        String telecomSelection = " and ((visible = 1";
        MSimTelephonyManager.getDefault();
        switch (MSimTelephonyManager.getNetworkType(subscription)) {
            case 13:
            case 14:
                if (!SystemProperties.getBoolean("ro.config.apn_lte_ctnet", false) || !isNetworkRoaming(subscription)) {
                    telecomSelection = telecomSelection + " and (bearer=14 or bearer=13)";
                    break;
                }
                telecomSelection = telecomSelection + " and bearer=0";
                break;
                break;
            default:
                telecomSelection = telecomSelection + " and bearer=0";
                break;
        }
        return where + (telecomSelection + ") or visible is null)");
    }

    private boolean isNetworkRoaming(int sub) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            return TelephonyManager.getDefault().isNetworkRoaming(sub);
        }
        return TelephonyManager.getDefault().isNetworkRoaming();
    }

    protected String[] getOperatorNumeric() {
        ArrayList<String> result = new ArrayList();
        String numeric;
        if (!Utils.isMultiSimEnabled()) {
            numeric = SystemProperties.get("gsm.sim.operator.numeric", "");
            result.add(numeric);
            result = getOperatorNumeric(getActivity(), numeric, result);
        } else if ((Utils.getMainCardSlotId() == this.mSubscription && this.mIsChinaTelecomCard) || isCTCardForFullNet(this.mSubscription)) {
            result.add(SystemProperties.get("ro.cdma.home.operator.numeric", "46003"));
        } else {
            numeric = MSimTelephonyManager.getTelephonyProperty("gsm.sim.operator.numeric", this.mSubscription, "");
            String operator = SystemProperties.get("ro.hwpp_plmn_sub2", "0");
            if (this.mIsChinaTelecomCard && numeric != null && !numeric.equals("") && operator.contains(numeric)) {
                Log.d("ApnSettingsHwBase", "sub2 is dobule mode card.");
                numeric = SystemProperties.get("gsm.national_roaming.apn", numeric);
            }
            result.add(numeric);
        }
        return (String[]) result.toArray(new String[2]);
    }

    private ArrayList<String> getOperatorNumeric(Context context, String numeric, ArrayList<String> result) {
        int activePhone = TelephonyManager.from(context).getPhoneType();
        Log.d("ApnSettingsHwBase", "getOperatorNumeric: numeric is " + numeric + " activePhone is " + activePhone);
        if (this.mIsChinaTelecomCard) {
            result.remove(numeric);
            result.add(SystemProperties.get("ro.cdma.home.operator.numeric", "46003"));
        } else if ((IS_TRACFONE || IS_SPRINT) && activePhone == 2) {
            result.remove(numeric);
            int dataNetworkType = TelephonyManager.getDefault().getDataNetworkType();
            String numeric_sim = SystemProperties.get("gsm.apn.sim.operator.numeric", "");
            String numeric_ruim = SystemProperties.get("net.cdma.ruim.operator.numeric", "");
            Log.d("ApnSettingsHwBase", "getOperatorNumeric: dataNetworkType is " + dataNetworkType + " numeric_sim is " + numeric_sim + "numeric_ruim is " + numeric_ruim);
            if ("".equals(numeric_sim) || "".equals(numeric_ruim)) {
                if ("".equals(numeric_sim) && !"".equals(numeric_ruim)) {
                    result.add(numeric_ruim);
                } else if ("".equals(numeric_sim) || !"".equals(numeric_ruim)) {
                    Log.w("ApnSettingsHwBase", "getOperatorNumeric: both apn numeric from sim and ruim are empty ");
                    result.add(numeric_sim);
                } else {
                    result.add(numeric_sim);
                }
            } else if (dataNetworkType == 13 || dataNetworkType == 14) {
                result.add(numeric_sim);
                result.add(numeric_ruim);
            } else {
                result.add(numeric_ruim);
                result.add(numeric_sim);
            }
        }
        return result;
    }

    protected SelectionHolder getSelectionForSomeCarriers() {
        String str = null;
        String[] strArr = null;
        if (!this.isLifeEEApnShow) {
            return null;
        }
        String[] mccmncs = getOperatorNumeric();
        String preferredApnName = getSelectedApnName();
        Log.d("ApnSettingsHwBase", "preferredApnName =" + preferredApnName);
        if (!(mccmncs[0] == null || !mccmncs[0].equals("23433") || preferredApnName == null)) {
            if (preferredApnName.equals("everywhere") || preferredApnName.equals("eezone")) {
                str = " numeric = ? AND  (apn = 'everywhere' or + apn = 'eezone')";
                strArr = new String[]{"23433"};
            } else if (preferredApnName.equals("tslpaygnet") || preferredApnName.equals("tslmms")) {
                str = " numeric = ? AND  (apn = 'tslpaygnet' or + apn = 'tslmms')";
                strArr = new String[]{"23433"};
            }
        }
        String preferredCarrierName = getSelectedCarrierName();
        Log.d("ApnSettingsHwBase", "preferredCarrierName =" + preferredCarrierName);
        if (!(mccmncs[0] == null || !mccmncs[0].equals("23430") || preferredCarrierName == null)) {
            if (preferredCarrierName.equals("EE") || preferredCarrierName.equals("EE zone")) {
                str = " numeric = ? AND  (name = 'EE' or + name = 'EE zone')";
                strArr = new String[]{"23430"};
            } else if (preferredCarrierName.equals("ASDA Mobile") || preferredCarrierName.equals("ASDA MMS")) {
                str = " numeric = ? AND  (name = 'ASDA Mobile' or + name = 'ASDA MMS')";
                strArr = new String[]{"23430"};
            }
        }
        if (str == null || strArr == null) {
            return null;
        }
        SelectionHolder selection = new SelectionHolder();
        selection.selection = str;
        selection.selectionArgs = strArr;
        return selection;
    }

    protected String getOrangeSelectedApnName() {
        return getSelectedApnName();
    }

    private String getSelectedApnName() {
        String name = null;
        Cursor cursor = null;
        try {
            if (this.mCustApnSettingsHwBase != null) {
                cursor = getContentResolver().query(this.mCustApnSettingsHwBase.getPreferredApnUri(PREFERAPN_URI, this.mSubscription), new String[]{"apn"}, null, null, "name ASC");
            } else {
                cursor = getContentResolver().query(PREFERAPN_URI, new String[]{"apn"}, null, null, "name ASC");
            }
            if (TelephonyManager.getDefault().isMultiSimEnabled() && !SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
                Log.d("ApnSettingsHwBase", "getSelectedApnName mSubscription" + this.mSubscription);
                if (cursor != null) {
                    cursor.close();
                }
                Uri uri = ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription);
                cursor = getContentResolver().query(uri, new String[]{"apn"}, null, null, "name ASC");
            }
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
            return name;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getSelectedCarrierName() {
        String name = null;
        Cursor cursor = null;
        try {
            if (this.mCustApnSettingsHwBase != null) {
                cursor = getContentResolver().query(this.mCustApnSettingsHwBase.getPreferredApnUri(PREFERAPN_URI, this.mSubscription), new String[]{"name"}, null, null, "name ASC");
            } else {
                cursor = getContentResolver().query(PREFERAPN_URI, new String[]{"name"}, null, null, "name ASC");
            }
            if (TelephonyManager.getDefault().isMultiSimEnabled() && !SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
                Log.d("ApnSettingsHwBase", "getSelectedCarrierName mSubscription" + this.mSubscription);
                if (cursor != null) {
                    cursor.close();
                }
                Uri uri = ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription);
                cursor = getContentResolver().query(uri, new String[]{"name"}, null, null, "name ASC");
            }
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
            return name;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected void showApnReminderDialog(Context context) {
        String plmn;
        String imsi;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService("phone");
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            plmn = telephonyManager.getSimOperator(this.mSubscription);
            imsi = telephonyManager.getSubscriberId(this.mSubscription);
        } else {
            plmn = telephonyManager.getSimOperator();
            imsi = telephonyManager.getSubscriberId();
        }
        try {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                ApnReminderEx.getInstance().restoreApn(context, plmn, imsi, this.mSubscription);
            } else {
                ApnReminderEx.getInstance().restoreApn(context, plmn, imsi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void saveSelectedApnKey(ContentResolver resolver, ContentValues values) {
        if (resolver == null) {
            Log.e("ApnSettingsHwBase", "saveSelectedApnKey Error!");
            return;
        }
        if (!Utils.isMultiSimEnabled()) {
            resolver.update(PREFERAPN_URI, values, null, null);
        } else if (this.mCustApnSettingsHwBase != null) {
            resolver.update(this.mCustApnSettingsHwBase.getPreferredApnUri(ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription), this.mSubscription), values, null, null);
        } else {
            resolver.update(ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription), values, null, null);
        }
    }

    protected Cursor readSelectedApnKeyFromDb() {
        if (!Utils.isMultiSimEnabled()) {
            return getContentResolver().query(PREFERAPN_URI, new String[]{"_id"}, null, null, "name ASC");
        } else if (this.mCustApnSettingsHwBase != null) {
            return getContentResolver().query(this.mCustApnSettingsHwBase.getPreferredApnUri(ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription), this.mSubscription), new String[]{"_id"}, null, null, "name ASC");
        } else {
            return getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription), new String[]{"_id"}, null, null, "name ASC");
        }
    }

    public void finish() {
        super.finish();
        new HwAnimationReflection(getActivity()).overrideTransition(2);
    }

    public boolean isShowWapApn(String apn, String type) {
        Log.d("ApnSettingsHwBase", "isShowWapApn: apn = " + apn + " type = " + type);
        if (this.mCustApnSettingsHwBase != null && this.mCustApnSettingsHwBase.checkShouldHideApn(apn)) {
            return false;
        }
        if (!isCurrentSlotSupportLTE(this.mSubscription) && !this.mIsFullNetSupport) {
            return !"ctwap".equals(apn);
        } else {
            if (!"ctwap".equals(apn)) {
                return true;
            }
            int cardIndex = getCardType();
            int netIndex = getNetWork(this.mSubscription);
            Log.d("ApnSettingsHwBase", "cardIndex:" + cardIndex);
            Log.d("ApnSettingsHwBase", "netIndex :" + netIndex);
            if (type == null || type.contains("default") || type.equals("")) {
                return plmn_table[netIndex][cardIndex];
            }
            return plmn_table_mms[netIndex][cardIndex];
        }
    }

    private boolean isCurrentSlotSupportLTE(int subscription) {
        return subscription == Utils.getMainCardSlotId();
    }

    private int getCardType() {
        String simOperator = TelephonyManager.getDefault().getSimOperator();
        if (SystemProperties.get("ro.ct_card.mccmnc", "46003,46012").contains(simOperator)) {
            return 0;
        }
        if ("45502".equals(simOperator)) {
            return 1;
        }
        return 2;
    }

    private int getNetWork(int subscription) {
        int netIndex;
        String networkOperator = MSimTelephonyManager.getDefault().getNetworkOperator(subscription);
        boolean isCdmaType = false;
        try {
            isCdmaType = 2 == MSimTelephonyManager.getDefault().getCurrentPhoneType(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkOperator.startsWith("460")) {
            netIndex = 0;
        } else if (networkOperator.startsWith("455")) {
            netIndex = 1;
        } else if (isCdmaType) {
            netIndex = 2;
        } else {
            netIndex = 3;
        }
        MSimTelephonyManager.getDefault();
        int radioTech = MSimTelephonyManager.getNetworkType(subscription);
        if (radioTech == 13 || radioTech == 14) {
            return 4;
        }
        return netIndex;
    }
}
