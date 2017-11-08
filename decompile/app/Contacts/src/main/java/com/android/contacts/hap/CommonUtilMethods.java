package com.android.contacts.hap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.ConfigurationEx;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore.Audio.Media;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telecom.PhoneAccountHandle;
import android.telephony.CallerInfoHW;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.AlignmentSpan.Standard;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Jlog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.ext.phone.SetupPhoneAccount;
import com.android.contacts.hap.hotline.HLUtils;
import com.android.contacts.hap.receiver.ContactsPropertyChangeReceiver;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.sim.IIccPhoneBookAdapter;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.base.BaseSimAccountType;
import com.android.contacts.hap.sim.extended.ExtendedSimAccountType;
import com.android.contacts.interactions.ManageContactsActivity;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.TextUtil;
import com.android.contacts.widget.PinnedHeaderListView;
import com.android.internal.app.WindowDecorActionBar;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.android.telephony.MSimSmsManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cspcommon.util.CommonConstants;
import com.huawei.cspcommon.util.ContactQuery;
import com.huawei.cspcommon.util.SortUtils;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.cust.HwCustUtils;
import huawei.android.text.format.HwDateUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CommonUtilMethods {
    private static final boolean DBG = HwLog.HWDBG;
    private static final String[] IPHEAD = new String[]{"10193", "11808", "12593", "17900", "17901", "17908", "17909", "17910", "17911", "17931", "17950", "17951", "17960", "17968", "17969", "96435"};
    private static final boolean IS_HAVE_EARPIECE = SystemProperties.getBoolean("ro.huawei.earpiece_available", true);
    public static final int NUM_LONG;
    private static String SIMCARD1_NAME;
    private static String SIMCARD2_NAME;
    private static Pattern SPLIT_PATTERN = Pattern.compile("([\\w-\\.]+)@((?:[\\w]+\\.)+)([a-zA-Z]{2,4})|[\\w]+");
    public static final int configMatchNum = SystemProperties.getInt("ro.config.hwft_MatchNum", 7);
    public static final int configMatchNumAbsent = SystemProperties.getInt("ro.config.hwft_MatchNum", 0);
    private static final boolean isLiteFeatureProducts = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    public static final Object mContactCursorLoad = new Object();
    private static HwCustCommonUtilMethods mCust = null;
    private static Bundle mInstanceState;
    private static Object mSyncObject = new Object();
    private static int sAlphaScrollerEnabled = -1;
    private static TelephonyManager sTelephonyManager = null;
    private static String spnSimName1;
    private static String spnSimName2;

    public interface SelectCardCallback {
        void cancel(DialogInterface dialogInterface);

        void confirm(View view, int i);
    }

    public static class RefreshDialerButtonTask extends AsyncTask<Void, Void, Integer> {
        private ImageView mCard1Image;
        private View mCard1NameDial;
        private TextView mCard1Text;
        private ImageView mCard2Image;
        private View mCard2NameDial;
        private TextView mCard2Text;
        private Context mContext;
        private String mNumber;
        private TextView mSimCard1;
        private TextView mSimCard2;
        private int mSlotId = -1;

        public RefreshDialerButtonTask(View card1NameDial, View card2NameDial, Context context, String number) {
            this.mCard1NameDial = card1NameDial;
            this.mCard2NameDial = card2NameDial;
            this.mContext = context;
            this.mNumber = IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(number);
        }

        protected Integer doInBackground(Void... params) {
            String[] projection = new String[]{"subscription_id"};
            String selection = "number = ?";
            Cursor cursor = null;
            try {
                cursor = this.mContext.getContentResolver().query(Calls.CONTENT_URI, projection, selection, new String[]{this.mNumber}, null);
                if (cursor != null && cursor.moveToLast()) {
                    this.mSlotId = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return Integer.valueOf(this.mSlotId);
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            int resultid = result.intValue();
            if (this.mContext instanceof ContactDetailActivity) {
                int recommendColor = this.mContext.getResources().getColor(R.color.detail_sim_recommend_color);
                int detail_sim_text = this.mContext.getResources().getColor(R.color.detail_sim_text);
                switch (resultid) {
                    case 0:
                        this.mCard1Text.setTextColor(recommendColor);
                        this.mCard2Text.setTextColor(detail_sim_text);
                        this.mSimCard1.setTextColor(recommendColor);
                        this.mSimCard2.setTextColor(detail_sim_text);
                        this.mCard1Image.setImageResource(R.drawable.contacts_call_blue);
                        this.mCard2Image.setImageResource(R.drawable.contacts_call_normal);
                        return;
                    case 1:
                        this.mCard2Text.setTextColor(recommendColor);
                        this.mCard1Text.setTextColor(detail_sim_text);
                        this.mSimCard2.setTextColor(recommendColor);
                        this.mSimCard1.setTextColor(detail_sim_text);
                        this.mCard2Image.setImageResource(R.drawable.contacts_call_blue);
                        this.mCard1Image.setImageResource(R.drawable.contacts_call_normal);
                        return;
                    default:
                        this.mCard2Text.setTextColor(detail_sim_text);
                        this.mCard1Text.setTextColor(detail_sim_text);
                        this.mSimCard1.setTextColor(detail_sim_text);
                        this.mSimCard2.setTextColor(detail_sim_text);
                        this.mCard1Image.setImageResource(R.drawable.contacts_call_normal);
                        this.mCard2Image.setImageResource(R.drawable.contacts_call_normal);
                        return;
                }
            }
            switch (resultid) {
                case 0:
                    this.mCard1NameDial.setBackgroundResource(R.drawable.rectangle);
                    return;
                case 1:
                    this.mCard2NameDial.setBackgroundResource(R.drawable.rectangle);
                    return;
                default:
                    this.mCard1NameDial.setBackgroundResource(R.drawable.btn_call);
                    this.mCard2NameDial.setBackgroundResource(R.drawable.btn_call);
                    return;
            }
        }
    }

    static {
        int i;
        if (configMatchNum < 7) {
            i = 7;
        } else {
            i = configMatchNum;
        }
        NUM_LONG = i;
    }

    public static boolean getIsLiteFeatureProducts() {
        return isLiteFeatureProducts;
    }

    public static boolean getIsHaveEarpiece() {
        return IS_HAVE_EARPIECE;
    }

    public static int getPinnedHeaderListViewResId(Context aContext) {
        boolean isLocaleSupports = aContext.getResources().getBoolean(R.bool.config_enable_alphaScroller);
        if (isAlphaScrollerEnabled(aContext) && isLocaleSupports) {
            return R.layout.pinned_header_listview_alpha_indexer;
        }
        return R.layout.pinned_header_listview;
    }

    public static boolean isAlphaScrollerEnabled(Context aContext) {
        if (sAlphaScrollerEnabled == -1) {
            if (isFastScrollerIsVisibleToChilds(aContext)) {
                sAlphaScrollerEnabled = 1;
            } else {
                sAlphaScrollerEnabled = 0;
            }
        }
        if (sAlphaScrollerEnabled == 1) {
            return true;
        }
        return false;
    }

    private static boolean isFastScrollerIsVisibleToChilds(Context aContext) {
        Method scrollMethod = getFastScrollerFieldFromListView(aContext);
        if (HwLog.HWFLOW) {
            HwLog.i("CommonUtilMethods isFastScrollerIsVisibleToChilds", "fieldValue : " + scrollMethod);
        }
        return scrollMethod != null;
    }

    public static Method getFastScrollerFieldFromListView(Context aContext) {
        Method lMethod = null;
        try {
            lMethod = Class.forName("android.widget.AbsListView").getDeclaredMethod("getScrollerInner", new Class[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lMethod;
    }

    public static Object getFastScrollerFieldValueFromListView(Context aContext, PinnedHeaderListView sourceObj) {
        if (sourceObj == null) {
            return null;
        }
        Object fastScrollerPropertyValue = null;
        try {
            Method lMethod = getFastScrollerFieldFromListView(aContext);
            if (lMethod != null) {
                lMethod.setAccessible(true);
                fastScrollerPropertyValue = lMethod.invoke(sourceObj, new Object[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fastScrollerPropertyValue;
    }

    public static void setFastScrollerFieldValueInListView(Context aContext, Object aFieldValue, PinnedHeaderListView sourceObj) {
        if (sourceObj != null) {
            try {
                Class<?> clazz = Class.forName("android.widget.AbsListView");
                Class<?> clazzFastScroller = Class.forName("android.widget.FastScroller");
                Method lMethod = clazz.getDeclaredMethod("setScrollerInner", new Class[]{clazzFastScroller});
                if (lMethod != null) {
                    lMethod.setAccessible(true);
                    lMethod.invoke(sourceObj, new Object[]{aFieldValue});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isActivityAvailable(Context aContext, Intent aIntent) {
        if (aContext.getPackageManager().queryIntentActivities(aIntent, 65536).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isAirplaneModeOn(Context aContext) {
        int airplaneModeSettings = 0;
        try {
            airplaneModeSettings = System.getInt(aContext.getContentResolver(), "airplane_mode_on");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (airplaneModeSettings == 1) {
            return true;
        }
        return false;
    }

    public static boolean isGroupEmailSupported(String accountTypeString, String dataSet, Context context) {
        AccountType accountType = AccountTypeManager.getInstance(context).getAccountType(accountTypeString, dataSet);
        if (accountType == null || (!(accountType instanceof BaseSimAccountType) && !(accountType instanceof ExtendedSimAccountType))) {
            return true;
        }
        return false;
    }

    public static String getWritableAccountStrExcludeSim(boolean contactWritableOnly, Context context) {
        StringBuilder accountTypeSb = new StringBuilder();
        for (AccountWithDataSet account : AccountTypeManager.getInstance(context).getAccounts(contactWritableOnly)) {
            if (!isSimAccount(account.type)) {
                accountTypeSb.append("'");
                accountTypeSb.append(account.type).append("',");
            }
        }
        if (accountTypeSb.length() > 0) {
            accountTypeSb.setLength(accountTypeSb.length() - 1);
        }
        return String.valueOf(accountTypeSb);
    }

    public static List<AccountWithDataSet> getWritableAccountListStrExcludeSim(boolean contactWritableOnly, Context context) {
        List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(context).getAccounts(contactWritableOnly);
        List<AccountWithDataSet> newAccounts = new ArrayList();
        for (AccountWithDataSet account : accounts) {
            if (!isSimAccount(account.type)) {
                newAccounts.add(account);
            }
        }
        return newAccounts;
    }

    public static boolean isPredefinedGroup(String sync1Value) {
        if (sync1Value == null || !sync1Value.contains("PREDEFINED_HUAWEI_GROUP")) {
            return false;
        }
        return true;
    }

    public static String parseGroupDisplayName(String accountType, String title, Context context, String sync4, int titleRes, String packageName) {
        String tempTitle;
        if ("com.android.huawei.phone".equalsIgnoreCase(accountType)) {
            if (CallInterceptDetails.BRANDED_STATE.equalsIgnoreCase(sync4) || titleRes == 0 || !SetupPhoneAccount.getPredefinedGroupabelResId().contains(Integer.valueOf(titleRes)) || packageName == null) {
                return title;
            }
            tempTitle = (String) context.getPackageManager().getText(packageName, titleRes, null);
            if (TextUtils.isEmpty(tempTitle)) {
                return title;
            }
            return tempTitle;
        } else if (!isSimAccount(accountType)) {
            return title;
        } else {
            tempTitle = SimFactoryManager.getSimCardDisplayLabel(accountType);
            if (TextUtils.isEmpty(tempTitle)) {
                return title;
            }
            return tempTitle;
        }
    }

    public static void checkPrimary(ArrayList<DetailViewEntry> aEntries) {
        boolean lIsPrimaryFound = false;
        int lEntriesCount = aEntries.size();
        if (lEntriesCount != 0 && 1 != lEntriesCount) {
            for (int lCounter = 0; lCounter < lEntriesCount; lCounter++) {
                DetailViewEntry lCurrentEntry = (DetailViewEntry) aEntries.get(lCounter);
                boolean lIsPrimary = lCurrentEntry.isPrimary;
                if (lIsPrimaryFound) {
                    lCurrentEntry.isPrimary = false;
                }
                if (lIsPrimary) {
                    lIsPrimaryFound = true;
                }
            }
        }
    }

    public static String extractNetworkPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        boolean firstCharAdded = false;
        boolean CLIRPFilter = true;
        if (EmuiFeatureManager.isProductCustFeatureEnable() && mCust == null) {
            mCust = (HwCustCommonUtilMethods) HwCustUtils.createObj(HwCustCommonUtilMethods.class, new Object[0]);
        }
        int i = 0;
        while (i < len) {
            char c = phoneNumber.charAt(i);
            boolean charAdded = isDialable(c) && !(c == '+' && firstCharAdded);
            boolean startsWith = CLIRPFilter ? !phoneNumber.startsWith("#31#+") ? phoneNumber.startsWith("*31#+") : true : false;
            if (mCust != null) {
                charAdded = mCust.isEnableSimAddPlus(charAdded, isDialable(c), startsWith);
            }
            if (charAdded) {
                firstCharAdded = true;
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            } else if (startsWith && phoneNumber.charAt(i) == '+') {
                CLIRPFilter = false;
            }
            i++;
        }
        int pos = addPlusChar(phoneNumber);
        if (pos >= 0 && ret.length() > pos) {
            ret.insert(pos, '+');
        }
        return ret.toString();
    }

    public static final boolean isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == 'N' || c == ',';
    }

    public static final boolean isStartsPostDial(char c) {
        return c == ';';
    }

    private static int addPlusChar(String number) {
        int pos = -1;
        if (number.startsWith("#31#+")) {
            pos = "#31#+".length() - 1;
        }
        if (number.startsWith("*31#+")) {
            return "*31#+".length() - 1;
        }
        return pos;
    }

    public static InputFilter getProfileInputFilter() {
        return new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned arg3, int arg4, int arg5) {
                int i = start;
                while (i < end) {
                    if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.isSpace(source.charAt(i))) {
                        return "";
                    }
                    i++;
                }
                return null;
            }
        };
    }

    public static boolean compareNumsHw(String num1, String num2) {
        try {
            return CallerInfoHW.getInstance().compareNums(num1, num2);
        } catch (NoExtAPIException e) {
            HwLog.w("CommonUtilMethods", "compareNumsHw 2 NoExtAPIException");
            return PhoneNumberUtils.compare(num1, num2);
        }
    }

    public static boolean compareNumsHw(String num1, String countryIso1, String num2, String countryIso2) {
        try {
            CallerInfoHW callerInfoHW = CallerInfoHW.getInstance();
            if (callerInfoHW != null) {
                return callerInfoHW.compareNums(num1, countryIso1, num2, countryIso2);
            }
        } catch (NoExtAPIException e) {
            HwLog.w("CommonUtilMethods", "compareNumsHw 4 NoExtAPIException");
        }
        return PhoneNumberUtils.compare(num1, num2);
    }

    public static String getCountryIsoFromDbNumberHw(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        try {
            CallerInfoHW callerInfoHW = CallerInfoHW.getInstance();
            if (callerInfoHW != null) {
                return callerInfoHW.getCountryIsoFromDbNumber(number);
            }
        } catch (NoExtAPIException e) {
            HwLog.w("CommonUtilMethods", "getCountryIsoFromDbNumber NoExtAPIException");
        }
        return null;
    }

    public static int getCallerInfoHW(Cursor cursor, String compNum, String columnName, String countryISO) {
        try {
            CallerInfoHW callerInfoHW = CallerInfoHW.getInstance();
            if (callerInfoHW != null) {
                return callerInfoHW.getCallerIndex(cursor, compNum, columnName, countryISO);
            }
            return -1;
        } catch (NoExtAPIException e) {
            HwLog.w("CommonUtilMethods", "getCallerIndex NoExtAPIException");
            return -2;
        }
    }

    public static String normalizeNumber(String phoneNumber) {
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (PhoneNumberUtils.isISODigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isSimAccount(String aAccountType) {
        if ("com.android.huawei.sim".equals(aAccountType) || "com.android.huawei.secondsim".equals(aAccountType)) {
            return true;
        }
        return false;
    }

    public static boolean isSim1Account(String aAccountType) {
        return "com.android.huawei.sim".equals(aAccountType);
    }

    public static boolean isSim2Account(String aAccountType) {
        return "com.android.huawei.secondsim".equals(aAccountType);
    }

    private static Intent getDialNumberIntent(Uri numberUri, int subscriptionId, boolean isDialByProximity) {
        Intent intent = new Intent(QueryUtil.isSystemAppForContacts() ? "android.intent.action.CALL_PRIVILEGED" : "android.intent.action.CALL", numberUri);
        intent.setFlags(276824064);
        if (isDialByProximity) {
            intent.putExtra("dial_by_proximity", true);
        }
        if (SimFactoryManager.isDualSim()) {
            PhoneAccountHandle accountHandle = CallUtil.makePstnPhoneAccountHandleWithPrefix(false, subscriptionId);
            if (accountHandle != null) {
                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", accountHandle);
            }
            try {
                MSimSmsManagerEx.setSimIdToIntent(intent, subscriptionId);
            } catch (Exception e) {
                intent.putExtra("subscription", subscriptionId);
            }
        }
        return intent;
    }

    private static void dial(Context context, boolean isFromDetail, boolean sendReport, Intent intent) {
        if (context != null) {
            try {
                if (EmuiFeatureManager.isProductCustFeatureEnable() && mCust == null) {
                    mCust = (HwCustCommonUtilMethods) HwCustUtils.createObj(HwCustCommonUtilMethods.class, new Object[0]);
                }
                if (mCust == null || !mCust.checkAndInitCall(context, intent)) {
                    int i;
                    context.startActivity(intent);
                    if (sendReport) {
                        StatisticalHelper.reportDialPortal(context, isFromDetail ? 1 : 2);
                        if (HwLog.HWFLOW) {
                            HwLog.i("CommonUtilMethods", "dial DIAL_TYPE_CONTACT_DETAIL=" + isFromDetail);
                        }
                    }
                    if (isFromDetail) {
                        i = 82;
                    } else {
                        i = 83;
                    }
                    ExceptionCapture.reportScene(i);
                }
            } catch (ActivityNotFoundException anfe) {
                HwLog.w("CommonUtilMethods", "ActivityNotFoundException occured when dial number");
                anfe.printStackTrace();
            }
        }
    }

    public static void dialNumber(Context context, Uri numberUri, int subscriptionId, boolean isFromDetail, boolean sendReport) {
        dial(context, isFromDetail, sendReport, getDialNumberIntent(numberUri, subscriptionId, false));
        StatisticalHelper.sendReport(3000, subscriptionId);
        if (HwLog.HWFLOW) {
            HwLog.i("CommonUtilMethods", "dialNumber the subid is :" + subscriptionId);
        }
    }

    public static void dialNumberByProximity(Context context, Uri numberUri, int subscriptionId, boolean isFromDetail, boolean sendReport) {
        dial(context, isFromDetail, sendReport, getDialNumberIntent(numberUri, subscriptionId, true));
        StatisticalHelper.sendReport(3000, subscriptionId);
        if (HwLog.HWFLOW) {
            HwLog.i("CommonUtilMethods", "dialNumberByProximity the subid is :" + subscriptionId);
        }
    }

    public static boolean isMergeFeatureEnabled() {
        if (ContactsPropertyChangeReceiver.getStubEnabledFlag()) {
            return ContactsPropertyChangeReceiver.getMergeEnabledFlag();
        }
        return QueryUtil.isHAPProviderInstalled();
    }

    public static Intent getShareContactsIntent() {
        Intent lIntent = new Intent();
        lIntent.setAction("android.intent.action.HAP_SHARE_CONTACTS");
        return lIntent;
    }

    public static Intent getMergeContactsIntent(ContactListFilter filter) {
        Intent lIntent = new Intent();
        lIntent.setAction("android.intent.action.MERGE_DUPLICATED_CONTACTS");
        lIntent.putExtra("contactListFilter", filter);
        return lIntent;
    }

    public static Intent getImportContactsViaBtIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setComponent(new ComponentName("com.huawei.bluetooth", "com.huawei.bluetooth.BluetoothPbapDevices"));
        return intent;
    }

    public static Intent getImportContactsViaWifiIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setPackage("com.huawei.android.wfdft");
        return intent;
    }

    public static Intent getImportContactsViaotherPhonesIntent() {
        Intent lIntent = new Intent();
        lIntent.setAction("com.android.huawei.bluetooth.prepare");
        return lIntent;
    }

    public static Intent getImportContactsFromQQIntent() {
        return new Intent("com.huawei.android.LAUNCH_QQ");
    }

    public static void dialNumber(final Context aContext, final Uri aNumberUri, String aToUse, boolean isFromDetail, boolean sendReport) {
        if (SimFactoryManager.isDualSim()) {
            boolean isFirstSimEnabled = getFirstSimEnabled();
            boolean isSecondSimEnabled = getSecondSimEnabled();
            ArrayAdapter<String> adapter = new ArrayAdapter(aContext, R.layout.select_dialog_item);
            String[] simStrings = getSimCombinationString(aContext);
            if (isFirstSimEnabled && isSecondSimEnabled) {
                adapter.insert(String.format(aContext.getString(R.string.contact_menu_dualsim_callNumber), new Object[]{aToUse, simStrings[0]}), 0);
                adapter.insert(String.format(aContext.getString(R.string.contact_menu_dualsim_callNumber), new Object[]{aToUse, simStrings[1]}), 1);
                String lTitle = String.format(aContext.getString(R.string.recentCalls_callNumber), new Object[]{aToUse});
                Builder builder = new Builder(aContext);
                final boolean z = isFromDetail;
                final boolean z2 = sendReport;
                OnClickListener clickListener = new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            CommonUtilMethods.dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(0), z, z2);
                        } else {
                            CommonUtilMethods.dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(1), z, z2);
                        }
                    }
                };
                builder.setTitle(lTitle);
                builder.setSingleChoiceItems(adapter, -1, clickListener);
                AlertDialog alertDialog = builder.create();
                if (aContext instanceof PeopleActivity) {
                    ((PeopleActivity) aContext).mGlobalDialogReference = alertDialog;
                }
                alertDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (aContext != null && (aContext instanceof PeopleActivity)) {
                            ((PeopleActivity) aContext).mGlobalDialogReference = null;
                        }
                    }
                });
                alertDialog.show();
                return;
            } else if (isFirstSimEnabled) {
                dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(0), isFromDetail, sendReport);
                return;
            } else if (isSecondSimEnabled) {
                dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(1), isFromDetail, sendReport);
                return;
            } else if (!isFirstSimEnabled && !isSecondSimEnabled) {
                dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(0), isFromDetail, sendReport);
                return;
            } else {
                return;
            }
        }
        dialNumber(aContext, aNumberUri, -1, isFromDetail, sendReport);
    }

    public static void dialNumberFromcalllog(final Context aContext, final Uri aNumberUri, String aTitle, int aSimType, final boolean isFromDetail, final boolean sendReport, String number) {
        if (aContext != null) {
            if (SimFactoryManager.isDualSim()) {
                boolean isFirstSimEnabled = getFirstSimEnabled();
                boolean isSecondSimEnabled = getSecondSimEnabled();
                if (isFirstSimEnabled && isSecondSimEnabled) {
                    int defaultExtremeSimplicitySim = SimFactoryManager.getDefaultSimcard();
                    if (-1 != defaultExtremeSimplicitySim) {
                        dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(defaultExtremeSimplicitySim), isFromDetail, sendReport);
                    } else {
                        setSimcardName(aContext);
                        showSelectCardDialog(aContext, aTitle, R.drawable.contact_dial_call_1, R.drawable.contact_dial_call_2, new SelectCardCallback() {
                            public void confirm(View v, int slotId) {
                                CommonUtilMethods.dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(slotId), isFromDetail, sendReport);
                            }

                            public void cancel(DialogInterface dialog) {
                            }
                        }, number);
                    }
                } else if (isFirstSimEnabled) {
                    dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(0), isFromDetail, sendReport);
                } else if (isSecondSimEnabled) {
                    dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(1), isFromDetail, sendReport);
                } else if (!(isFirstSimEnabled || isSecondSimEnabled)) {
                    dialNumber(aContext, aNumberUri, SimFactoryManager.getSubscriptionIdBasedOnSlot(0), isFromDetail, sendReport);
                }
            } else {
                dialNumber(aContext, aNumberUri, -1, isFromDetail, sendReport);
            }
        }
    }

    public static void showSelectCardDialog(final Context aContext, String aTitle, int card1ResId, int card2ResId, SelectCardCallback callback, String number) {
        final SelectCardCallback selectCardCallback;
        Builder builder = new Builder(aContext);
        View view = ((Activity) aContext).getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
        ((TextView) view.findViewById(R.id.alert_dialog_content)).setVisibility(8);
        builder.setView(view);
        LinearLayout callbuttonLayout = (LinearLayout) view.findViewById(R.id.call_buttons_container);
        callbuttonLayout.setVisibility(0);
        View sim1call = callbuttonLayout.findViewById(R.id.sim1);
        if (sim1call != null) {
            setDialBtnTextAndImage(sim1call, SIMCARD1_NAME, card1ResId);
            adjustNameViewWidth(aContext, sim1call);
        }
        View sim2call = callbuttonLayout.findViewById(R.id.sim2);
        if (sim2call != null) {
            setDialBtnTextAndImage(sim2call, SIMCARD2_NAME, card2ResId);
            adjustNameViewWidth(aContext, sim2call);
        }
        new RefreshDialerButtonTask(sim1call, sim2call, aContext, number).execute(new Void[0]);
        builder.setTitle(aTitle);
        final AlertDialog alertDialog = builder.create();
        if (aContext instanceof ContactDetailActivity) {
            ((ContactDetailActivity) aContext).mGlobalDialogReference = alertDialog;
        }
        if (sim1call != null) {
            selectCardCallback = callback;
            sim1call.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectCardCallback.confirm(v, 0);
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                }
            });
        }
        if (sim2call != null) {
            selectCardCallback = callback;
            sim2call.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectCardCallback.confirm(v, 1);
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                }
            });
        }
        selectCardCallback = callback;
        alertDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                selectCardCallback.cancel(dialog);
                if (aContext != null && (aContext instanceof ContactDetailActivity)) {
                    ((ContactDetailActivity) aContext).mGlobalDialogReference = null;
                }
            }
        });
        alertDialog.show();
    }

    public static void setDialBtnTextAndImage(View cardNameDial, String cardName, int imageId) {
        if (cardNameDial != null) {
            ((TextView) cardNameDial.findViewById(R.id.button_text)).setText(cardName);
            ImageView imageView = (ImageView) cardNameDial.findViewById(R.id.button_image);
            imageView.setImageResource(imageId);
            ViewUtil.setStateListIcon(cardNameDial.getContext(), imageView, false);
        }
    }

    public static void setSimcardName(Context aContext) {
        if (EmuiVersion.isSupportEmui()) {
            SIMCARD1_NAME = imsiToSimName(aContext, SimFactoryManager.getSimSerialNumber(0));
            SIMCARD2_NAME = imsiToSimName(aContext, SimFactoryManager.getSimSerialNumber(1));
            if (nameIsEmpty(SIMCARD1_NAME)) {
                SIMCARD1_NAME = spnSimName1;
            }
            if (nameIsEmpty(SIMCARD2_NAME)) {
                SIMCARD2_NAME = spnSimName2;
            }
            if (nameIsEmpty(SIMCARD1_NAME)) {
                SIMCARD1_NAME = MSimTelephonyManager.getDefault().getNetworkOperatorName(0);
            }
            if (nameIsEmpty(SIMCARD2_NAME)) {
                SIMCARD2_NAME = MSimTelephonyManager.getDefault().getNetworkOperatorName(1);
            }
            if (nameIsEmpty(SIMCARD1_NAME)) {
                SIMCARD1_NAME = aContext.getString(R.string.dialpad_name_no_service);
            }
            if (nameIsEmpty(SIMCARD2_NAME)) {
                SIMCARD2_NAME = aContext.getString(R.string.dialpad_name_no_service);
                return;
            }
            return;
        }
        TelephonyManager telMgr = (TelephonyManager) aContext.getSystemService("phone");
        SIMCARD1_NAME = telMgr.getNetworkOperatorName();
        SIMCARD2_NAME = telMgr.getNetworkOperatorName();
    }

    public static boolean getSimNameFromBroadcast(Context context, Intent intent) {
        boolean z;
        if ("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED".equals(intent.getAction())) {
            z = true;
        } else {
            z = "android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED".equals(intent.getAction());
        }
        if (!z) {
            return false;
        }
        String cardFromDb = null;
        if (EmuiVersion.isSupportEmui()) {
            if ("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED".equals(intent.getAction())) {
                cardFromDb = imsiToSimName(context, SimFactoryManager.getSimSerialNumber(0));
            } else {
                cardFromDb = imsiToSimName(context, SimFactoryManager.getSimSerialNumber(1));
            }
        }
        String name = "";
        boolean showSpn = intent.getBooleanExtra("showSpn", false);
        boolean showPlmn = intent.getBooleanExtra("showPlmn", false);
        String spn = intent.getStringExtra("spn");
        String plmn = intent.getStringExtra("plmn");
        if (HwLog.HWFLOW) {
            HwLog.i("CommonUtilMethods", "action:" + intent.getAction() + " networkName showSpn = " + showSpn + " spn = " + spn + " showPlmn = " + showPlmn + " plmn = " + plmn);
        }
        StringBuilder str = new StringBuilder();
        boolean something = false;
        if (showPlmn && plmn != null) {
            str.append(plmn);
            something = true;
        }
        if (showSpn && spn != null) {
            if (something) {
                str.append('|');
            }
            str.append(spn);
        }
        if (nameIsEmpty(cardFromDb)) {
            name = str.toString();
        } else {
            name = cardFromDb;
        }
        if ("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED".equals(intent.getAction())) {
            spnSimName1 = str.toString();
            if (name.equals(SIMCARD1_NAME)) {
                return false;
            }
            SIMCARD1_NAME = name;
            return true;
        } else if (!"android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED".equals(intent.getAction())) {
            return false;
        } else {
            spnSimName2 = str.toString();
            if (name.equals(SIMCARD2_NAME)) {
                return false;
            }
            SIMCARD2_NAME = name;
            return true;
        }
    }

    private static boolean nameIsEmpty(String name) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("null") || name.equals("0")) {
            return true;
        }
        return false;
    }

    public static boolean getFirstSimEnabled() {
        boolean firstSimStatus;
        if (SimFactoryManager.isSIM1CardPresent()) {
            firstSimStatus = SimFactoryManager.isSimEnabled(0);
        } else {
            firstSimStatus = false;
        }
        if (CommonConstants.sRo_config_hw_dsda) {
            return firstSimStatus;
        }
        if (firstSimStatus) {
            return !SimFactoryManager.phoneIsOffhook(1);
        } else {
            return false;
        }
    }

    public static boolean getSecondSimEnabled() {
        boolean secondSimSlot;
        if (SimFactoryManager.isSIM2CardPresent()) {
            secondSimSlot = SimFactoryManager.isSimEnabled(1);
        } else {
            secondSimSlot = false;
        }
        if (CommonConstants.sRo_config_hw_dsda) {
            return secondSimSlot;
        }
        if (secondSimSlot) {
            return !SimFactoryManager.phoneIsOffhook(0);
        } else {
            return false;
        }
    }

    public static String[] getSimCombinationString(Context aContext) {
        return new String[]{aContext.getString(R.string.str_filter_sim1), aContext.getString(R.string.str_filter_sim2)};
    }

    public static Intent getContactSelectionIntentForSpeedDial(Context aContext, ArrayList<String> aContactsAdded, Bundle aBundle) {
        Intent lIntent = new Intent();
        lIntent.setAction("android.intent.action.PICK");
        lIntent.setClass(aContext, ContactSelectionActivity.class);
        lIntent.setType("vnd.android.cursor.dir/phone_v2");
        lIntent.putExtra("speed_dial", true);
        lIntent.putStringArrayListExtra("contacts_added", aContactsAdded);
        if (aBundle != null) {
            lIntent.putExtras(aBundle);
        }
        return lIntent;
    }

    public static Intent getSpeedDialIntent() {
        return new Intent("android.intent.action.HAP_LAUNCH_SPEED_DIAL_SETTINGS");
    }

    public static void updateFavoritesWidget(Context aContext) {
        if (HwLog.HWDBG) {
            HwLog.d("CommonUtilMethods", "updateFavoritesWidget!!!");
        }
        aContext.sendBroadcast(new Intent("com.android.contacts.favorites.updated"));
    }

    public static SpannableStringBuilder getSearchViewSpannableHint(Context aContext, String aHintText, float aTextSize) {
        boolean aIsMirror = isLayoutRTL();
        SpannableStringBuilder ssb = new SpannableStringBuilder("");
        ssb.append(aHintText);
        if (aIsMirror) {
            ssb.setSpan(new Standard(Alignment.ALIGN_RIGHT), 0, ssb.toString().length(), 33);
        } else {
            ssb.setSpan(new Standard(Alignment.ALIGN_LEFT), 0, ssb.toString().length(), 33);
        }
        ssb.setSpan(new ForegroundColorSpan(aContext.getResources().getColor(R.color.contact_searchfield_hint_text_color)), 0, ssb.toString().length(), 33);
        return ssb;
    }

    public static Intent getIntentForCopyFromSimActivity(int aSubscription) {
        Intent lIntent = new Intent();
        lIntent.setAction("android.intent.action.HAP_COPY_FROM_SIM");
        String lSimAccoutnName = SimFactoryManager.getAccountName(aSubscription);
        String lAccountType = SimFactoryManager.getAccountType(aSubscription);
        lIntent.putExtra("extra_account_name", lSimAccoutnName);
        lIntent.putExtra("extra_account_type", lAccountType);
        lIntent.putExtra("extra_account_data_set", "");
        return lIntent;
    }

    public static boolean isSimplifiedModeEnabled() {
        return CommonConstants.isSimplifiedModeEnabled();
    }

    public static boolean isSimpleModeOn() {
        boolean z = false;
        try {
            ConfigurationEx mExtraConfig = new com.huawei.android.content.res.ConfigurationEx(ActivityManagerNative.getDefault().getConfiguration()).getExtraConfig();
            if (mExtraConfig != null && 2 == mExtraConfig.getConfigItem(2)) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isPrivateContact(Cursor aCursor) {
        boolean isPrivateContact = false;
        if (aCursor != null) {
            int lPirvateContactColumnIndex = aCursor.getColumnIndex("is_private");
            if (-1 != lPirvateContactColumnIndex) {
                isPrivateContact = aCursor.getInt(lPirvateContactColumnIndex) == 1;
            }
        }
        if (HwLog.HWDBG) {
            HwLog.d("CommonUtilMethods", "isPrivateContact :: " + isPrivateContact);
        }
        return isPrivateContact;
    }

    public static void launchManageContactsActivity(Context aContext, boolean aContactsAreAvailable, String callingActivity) {
        Intent lIntent = new Intent(aContext, ManageContactsActivity.class);
        lIntent.putExtra("CONTACTS_ARE_AVAILABLE", aContactsAreAvailable);
        lIntent.putExtra("CALLING_ACTIVITY", callingActivity);
        aContext.startActivity(lIntent);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getPathFromUri(Context context, Uri uri) {
        if (uri == null || context == null || context.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(0);
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e2) {
            HwLog.e("CommonUtilMethods", "an IllegalArgument in getPathFromUri");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isUriOfExternalSource(Uri uri) {
        if (uri != null) {
            return uri.toString().startsWith(Media.EXTERNAL_CONTENT_URI.toString());
        }
        return false;
    }

    public static Uri getRingtoneUriFromPath(Context context, String path) {
        if (context.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
            return null;
        }
        String SELECTION;
        String pathUri;
        boolean pathBeginWithStorage = true;
        String INTERNAL_PATH_HEAD = getSdcardPath(context, true);
        String EXTERNAL_PATH_HEAD = getSdcardPath(context, false);
        String pathBody = null;
        if (INTERNAL_PATH_HEAD != null && path.startsWith(INTERNAL_PATH_HEAD)) {
            pathBody = path.substring(INTERNAL_PATH_HEAD.length(), path.length());
        } else if (EXTERNAL_PATH_HEAD == null || !path.startsWith(EXTERNAL_PATH_HEAD)) {
            pathBeginWithStorage = false;
        } else {
            pathBody = path.substring(EXTERNAL_PATH_HEAD.length(), path.length());
        }
        Uri ROOT_INTERNAL = Media.INTERNAL_CONTENT_URI;
        Uri ROOT_EXTERNAL = Media.EXTERNAL_CONTENT_URI;
        String[] PROJECTION = new String[]{"_id"};
        String DATA_COLUMN = "_data";
        if (pathBeginWithStorage) {
            SELECTION = "_data = ? or _data = ?";
        } else {
            SELECTION = "_data = ?";
        }
        String[] SELECTION_ARGS = pathBeginWithStorage ? new String[]{INTERNAL_PATH_HEAD + pathBody, EXTERNAL_PATH_HEAD + pathBody} : new String[]{path};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ROOT_EXTERNAL, PROJECTION, SELECTION, SELECTION_ARGS, null);
            if (cursor != null && cursor.moveToFirst()) {
                pathUri = ROOT_EXTERNAL.toString();
                if (DBG) {
                    HwLog.d("CommonUtilMethods", "cursor.getInt(0) = " + cursor.getInt(0));
                }
                if (cursor.getInt(0) > 0) {
                    Uri parse = Uri.parse(pathUri + "/" + cursor.getInt(0));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return parse;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        try {
            cursor = context.getContentResolver().query(ROOT_INTERNAL, PROJECTION, SELECTION, SELECTION_ARGS, null);
            if (cursor != null && cursor.moveToFirst()) {
                pathUri = ROOT_INTERNAL.toString();
                if (cursor.getInt(0) > 0) {
                    parse = Uri.parse(pathUri + "/" + cursor.getInt(0));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return parse;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e2) {
            e2.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getSdcardPath(Context context, boolean isInner) {
        StorageManager sManager = (StorageManager) context.getSystemService("storage");
        StorageVolume[] sVolumes = sManager.getVolumeList();
        if (sVolumes == null) {
            return null;
        }
        int i = 0;
        while (i < sVolumes.length) {
            if (!isUsbStorage(sManager, sVolumes[i]) && sVolumes[i].isEmulated() == isInner) {
                String path = sVolumes[i].getPath();
                if (!(path == null || path.length() <= 0 || path.endsWith(File.separator))) {
                    path = path + File.separator;
                }
                return path;
            }
            i++;
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isUsbStorage(StorageManager storageManager, StorageVolume volume) {
        if (volume == null || volume.isPrimary() || !volume.isRemovable() || volume.getUuid() == null) {
            return false;
        }
        VolumeInfo volumeInfo = storageManager.findVolumeByUuid(volume.getUuid());
        if (volumeInfo == null) {
            return false;
        }
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo == null || !diskInfo.isUsb()) {
            return false;
        }
        return true;
    }

    public static boolean isIpCallEnabled() {
        boolean z = false;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            return false;
        }
        if (ContactsPropertyChangeReceiver.getStubEnabledFlag()) {
            if (ContactsPropertyChangeReceiver.getIpCallEnabledFlag() && !isSimplifiedModeEnabled()) {
                z = true;
            }
            return z;
        }
        if (SystemProperties.getBoolean("ro.config.hw_support_ipcall", false) && !isSimplifiedModeEnabled()) {
            z = true;
        }
        return z;
    }

    public static Uri initializeDefaultRingtone(Context aContext, String aAccountType) {
        String uriString;
        if ("com.android.huawei.secondsim".equals(aAccountType)) {
            uriString = System.getString(aContext.getContentResolver(), "ringtone2");
        } else {
            uriString = System.getString(aContext.getContentResolver(), "ringtone");
        }
        if (uriString != null) {
            return Uri.parse(uriString);
        }
        return null;
    }

    public static Intent getRingtoneIntent(Context aContext, Uri aRingtone) {
        Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
        intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", true);
        intent.putExtra("android.intent.extra.ringtone.TYPE", 1);
        intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", true);
        intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", aRingtone);
        intent.addCategory("android.intent.category.HWRING");
        if (aContext.getPackageManager().queryIntentActivities(intent, 0).size() == 0) {
            intent.removeCategory("android.intent.category.HWRING");
        }
        return intent;
    }

    public static String processNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        String normalizeNumber = PhoneNumberUtils.normalizeNumber(number);
        return deleteIPHead(deleteCountryCode(number));
    }

    public static String deleteCountryCode(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        String newNumber;
        if (number.matches("^86\\d{3,11}$")) {
            newNumber = number.substring(2);
        } else if (number.matches("^\\+86\\d{3,11}$") || number.matches("^086\\d{3,11}$")) {
            newNumber = number.substring(3);
        } else if (number.matches("^0086\\d{3,11}$")) {
            newNumber = number.substring(4);
        } else {
            newNumber = number;
        }
        if (DBG) {
            HwLog.d("CommonUtilMethods", "deleteCountryCode new Number");
        }
        return newNumber;
    }

    public static String deleteIPHead(String number) {
        if (TextUtils.isEmpty(number) || !EmuiFeatureManager.isChinaArea()) {
            return number;
        }
        int numberLen = number.length();
        if (numberLen < 5) {
            if (DBG) {
                HwLog.d("CommonUtilMethods", "deleteIPHead() numberLen is short than 5!");
            }
            return number;
        }
        if (Arrays.binarySearch(IPHEAD, number.substring(0, 5)) >= 0) {
            String subNum = number.substring(5, numberLen);
            if (subNum.matches("(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$")) {
                number = subNum;
            }
        }
        if (DBG) {
            HwLog.d("CommonUtilMethods", "deleteIPHead() new Number ");
        }
        return number;
    }

    public static boolean isContainIPHead(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        if (number.length() < 5) {
            if (DBG) {
                HwLog.d("CommonUtilMethods", "deleteIPHead() numberLen is short than 5!");
            }
            return false;
        }
        boolean result = false;
        if (Arrays.binarySearch(IPHEAD, number.substring(0, 5)) >= 0) {
            result = true;
        }
        return result;
    }

    public static String getSim1CardName() {
        return SIMCARD1_NAME;
    }

    public static String getSim2CardName() {
        return SIMCARD2_NAME;
    }

    private static int deletePredifeContacts(Context context) {
        return context.getContentResolver().delete(RawContacts.CONTENT_URI, "sync4 = 'PREDEFINED_HUAWEI_CONTACT' AND deleted = 0 ", null);
    }

    public static synchronized void loadPredifeContactsFromCustOtaUpdate(final Context context) {
        synchronized (CommonUtilMethods.class) {
            Thread loadPredife = new Thread() {
                public void run() {
                    try {
                        if (CommonUtilMethods.checkCustFileChangeOrNot(context) && CommonUtilMethods.deletePredifeContacts(context) > 0 && SetupPhoneAccount.addTypeAndParseLoadPreDefinedContacts(context)) {
                            System.putInt(context.getContentResolver(), "hw_service_contact_loaded", 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            loadPredife.setPriority(10);
            loadPredife.start();
        }
    }

    private static boolean checkCustFileChangeOrNot(Context aContext) {
        SharedPreferences sp = SharePreferenceUtil.getDefaultSp_de(aContext);
        String oldDigest = sp.getString("hw_default_digest", "still_no_digest");
        byte[] digest = computeSha1Digest("/xml/predefined_data.xml");
        String newDigest = null;
        if (digest != null) {
            try {
                newDigest = Base64.encodeToString(digest, 4).trim();
            } catch (Exception e) {
                HwLog.w("CommonUtilMethods", "Got execption convert disgest to string.", e);
            }
        }
        if (oldDigest.equals(newDigest) || (newDigest == null && "still_no_digest".equals(oldDigest))) {
            return false;
        }
        Editor editor = sp.edit();
        editor.putString("hw_default_digest", newDigest);
        editor.apply();
        return true;
    }

    public static String getMD5Digest(String val) {
        if (val == null) {
            return val;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes("UTF-8"));
            return getString(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            HwLog.w("CommonUtilMethods", "Got execption NoSuchAlgorithm.", e);
            return val;
        } catch (UnsupportedEncodingException e2) {
            HwLog.w("CommonUtilMethods", "Got execption UnsupportedEncodingException.", e2);
            return val;
        }
    }

    private static String getString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (byte append : b) {
            sb.append(append);
        }
        return sb.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] computeSha1Digest(String path) {
        FileInputStream fis;
        Throwable th;
        ArrayList<File> files = new ArrayList();
        try {
            files = HwCfgFilePolicy.getCfgFileList(path, 0);
        } catch (NoClassDefFoundError e) {
            HwLog.e("CommonUtilMethods", "caught exception:", e);
        }
        if (files == null || files.size() == 0) {
            HwLog.w("CommonUtilMethods", "No config file found for:" + path);
            return null;
        }
        byte[] data = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            FileInputStream fis2 = null;
            for (File file : files) {
                try {
                    try {
                        fis = new FileInputStream(file);
                        while (true) {
                            try {
                                int read = fis.read(data);
                                if (read == -1) {
                                    break;
                                }
                                sha1.update(data, 0, read);
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        if (fis != null) {
                            fis.close();
                        }
                        fis2 = fis;
                    } catch (Throwable th3) {
                        th = th3;
                        fis = fis2;
                    }
                } catch (NoSuchAlgorithmException e2) {
                    NoSuchAlgorithmException e3 = e2;
                    fis = fis2;
                } catch (FileNotFoundException e4) {
                    FileNotFoundException e5 = e4;
                    fis = fis2;
                } catch (IOException e6) {
                    IOException e7 = e6;
                    fis = fis2;
                }
            }
            return sha1.digest();
        } catch (IOException e72) {
            HwLog.w("CommonUtilMethods", "Got execption close fileinputstream.", e72);
        } catch (NoSuchAlgorithmException e8) {
            e3 = e8;
        } catch (FileNotFoundException e9) {
            e5 = e9;
        }
        HwLog.w("CommonUtilMethods", "Got execption NoSuchAlgorithmException.", e3);
        return null;
        HwLog.w("CommonUtilMethods", "Got execption IOException.", e72);
        return null;
        if (fis != null) {
            fis.close();
        }
        throw th;
        HwLog.w("CommonUtilMethods", "Got execption FileNotFound.", e5);
        return null;
        throw th;
    }

    public static boolean isTalkBackEnabled(Context aContext) {
        AccessibilityManager aAccMgr = (AccessibilityManager) aContext.getSystemService("accessibility");
        return aAccMgr.isTouchExplorationEnabled() ? aAccMgr.isEnabled() : false;
    }

    public static String trimNumberForMatching(String aNumber) {
        if (aNumber == null) {
            return null;
        }
        String number = PhoneNumberUtils.stripSeparators(aNumber);
        int numLength = number.length();
        int lenMatch = getLengthForMatching(numLength);
        if (numLength > lenMatch) {
            number = number.substring(numLength - lenMatch);
        }
        return number;
    }

    public static int getLengthForMatching(int numLength) {
        int numMatch = SystemProperties.getInt("gsm.hw.matchnum", 0);
        int numMatchShort = SystemProperties.getInt("gsm.hw.matchnum.short", 0);
        if (configMatchNumAbsent != 0) {
            return NUM_LONG;
        }
        if (numMatch == 0 && numMatchShort == 0) {
            return 7;
        }
        if (numMatch <= 7) {
            numMatch = 7;
        }
        if (numMatchShort <= 7) {
            numMatchShort = 7;
        }
        if (numLength >= numMatch) {
            return numMatch;
        }
        return numMatchShort;
    }

    public static boolean isLayoutRTL() {
        switch (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            case 1:
                return true;
            default:
                return false;
        }
    }

    public static boolean isServiceRunning(Context mContext, String serviceName) {
        boolean z = false;
        List<RunningServiceInfo> serviceList = ((ActivityManager) mContext.getSystemService("activity")).getRunningServices(200);
        if (serviceList == null || serviceList.size() == 0) {
            return 1 == null;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (((RunningServiceInfo) serviceList.get(i)).service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        if (1 == null) {
            z = true;
        }
        return z;
    }

    public static Intent getPrivacyProtectionIntent() {
        return new Intent("com.huawei.privacymode.action.STARTUP_PRIVACY_MODE");
    }

    public static boolean hasPrivacyProtectionActivity(Context aContext) {
        if (aContext == null) {
            return false;
        }
        PackageManager packageManager = aContext.getPackageManager();
        Intent intent = new Intent("com.huawei.privacymode.action.STARTUP_PRIVACY_MODE");
        intent.setPackage("com.huawei.privacymode");
        List<ResolveInfo> homes = packageManager.queryIntentActivities(intent, 0);
        if (homes == null || homes.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isStudentModeOn(Context aContext) {
        boolean z = false;
        if (aContext == null) {
            return false;
        }
        try {
            if (Secure.getInt(aContext.getContentResolver(), "childmode_status") == 1) {
                z = isParentControlValid(aContext);
            }
            return z;
        } catch (Exception e) {
            HwLog.e("CommonUtilMethods", e.getMessage());
            return false;
        }
    }

    public static boolean isParentControlValid(Context context) {
        if (context == null) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            pm.getPackageInfo("com.huawei.parentcontrol", 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isPrivacyModeEnabled(Context aContext) {
        boolean z = false;
        if (aContext == null) {
            return false;
        }
        try {
            if (Secure.getInt(aContext.getContentResolver(), "privacy_mode_state", 0) == 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    public static void showFragment(FragmentManager fragmentManager, DialogFragment fragment, String TAG) {
        if (fragmentManager != null && fragment != null) {
            try {
                fragment.show(fragmentManager, TAG);
            } catch (IllegalStateException ise) {
                HwLog.w(TAG, "show fragment error", ise);
            }
        }
    }

    public static void adjustNameViewWidth(Context context, View cardNameDial) {
        if (cardNameDial != null) {
            TextView textView = (TextView) cardNameDial.findViewById(R.id.button_text);
            if ((context.getResources().getDimensionPixelSize(R.dimen.contact_dialog_button_width) - context.getResources().getDimensionPixelSize(R.dimen.contact_dialpad_dial_del_button_width)) * 2 < TextUtil.getTextWidth(textView.getText().toString(), (float) context.getResources().getDimensionPixelSize(R.dimen.dialpad_text_width))) {
                textView.setTextSize(1, (float) context.getResources().getInteger(R.integer.dialpad_button_text_size));
                textView.setText(textView.getText().toString() + ' ');
                return;
            }
            textView.setTextSize(1, (float) context.getResources().getInteger(R.integer.dialpad_button_text_normal_size));
        }
    }

    public static String getAlphaEncodeNameforSIM(IIccPhoneBookAdapter lIccPhoneBookAdapter, String oldData, int maxLength) {
        if (TextUtils.isEmpty(oldData)) {
            return null;
        }
        int maxNameLength = maxLength;
        if (oldData.length() < maxLength) {
            maxNameLength = oldData.length();
        }
        while (maxNameLength > 0) {
            String newString = oldData.substring(0, maxNameLength);
            if (lIccPhoneBookAdapter.getAlphaEncodedLength(newString) <= maxLength) {
                return newString;
            }
            maxNameLength--;
        }
        return oldData;
    }

    public static void hideSoftKeyboard(Context mContext, View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService("input_method");
            if (inputMethodManager != null && inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static int getProfileVersion(Context context) {
        int version = -1;
        Uri uri = Uri.withAppendedPath(Profile.CONTENT_URI, "raw_contacts");
        if (context != null) {
            Cursor cursor = null;
            try {
                StringBuilder selection = new StringBuilder();
                selection.append("deleted").append("=0");
                cursor = context.getContentResolver().query(uri, new String[]{"version"}, selection.toString(), null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    version = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return version;
    }

    public static boolean isCoverOpen() {
        boolean bCoverOpen = true;
        try {
            Class<?> mCoverManagerClass = Class.forName("android.cover.CoverManager");
            bCoverOpen = ((Boolean) mCoverManagerClass.getMethod("isCoverOpen", new Class[0]).invoke(mCoverManagerClass.newInstance(), new Object[0])).booleanValue();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (IllegalArgumentException e6) {
            e6.printStackTrace();
        }
        if (DBG) {
            HwLog.d("CommonUtilMethods isCoverOpen", "returns " + bCoverOpen);
        }
        return bCoverOpen;
    }

    public static String imsiToSimName(Context aContext, String aImsi) {
        String simName = Global.getString(aContext.getContentResolver(), "sim_card_name_" + encode(aImsi));
        if (TextUtils.isEmpty(simName)) {
            return Global.getString(aContext.getContentResolver(), "sim_card_name_" + aImsi);
        }
        return simName;
    }

    private static String encode(String val) {
        if (val == null || val.length() == 0) {
            return val;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(val.getBytes("UTF-8"));
            return byte2hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        for (byte b2 : b) {
            stmp = Integer.toHexString(b2 & 255);
            if (stmp.length() == 1) {
                hs = hs.append("0").append(stmp);
            } else {
                hs = hs.append(stmp);
            }
        }
        return hs.toString();
    }

    public static String getHiCloudAccountLogOnDisplayString(Context mContext) {
        if (mContext == null) {
            return "";
        }
        return mContext.getString(R.string.HiCloud_Account_LogOn_Display_String);
    }

    public static String getHiCloudAccountPhoneDisplayString(Context mContext) {
        if (mContext == null) {
            return "";
        }
        return mContext.getString(R.string.phone_account_name);
    }

    public static File getExternalSDCardPath(Context mContext) {
        StorageManager storageManager = (StorageManager) mContext.getSystemService("storage");
        File sdCardPath = null;
        for (StorageVolume volume : storageManager.getVolumeList()) {
            if (volume != null) {
                if ("mounted".equals(volume.getState())) {
                    if (!volume.isPrimary() && volume.isRemovable()) {
                        if (volume.getUuid() == null) {
                            HwLog.w("CommonUtilMethods", "volume uuid is null");
                        } else {
                            VolumeInfo volumeInfo = storageManager.findVolumeByUuid(volume.getUuid());
                            if (volumeInfo != null) {
                                DiskInfo diskInfo = volumeInfo.getDisk();
                                if (diskInfo != null && diskInfo.isSd()) {
                                    sdCardPath = new File(volume.getPath());
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }
                if (sdCardPath != null) {
                    break;
                }
            }
        }
        return sdCardPath;
    }

    public static File getUseStroagePathsPriorSDCardDefault(Context mContext) {
        File storagePath = getExternalSDCardPath(mContext);
        if (storagePath == null) {
            return Environment.getExternalStorageDirectory();
        }
        return storagePath;
    }

    public static boolean checkApkExist(Context context, String packageName) {
        boolean z = true;
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            PackageManager pm = context.getPackageManager();
            pm.getApplicationInfo(packageName, 0);
            int state = pm.getApplicationEnabledSetting(packageName);
            if (!(state == 0 || 1 == state)) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isWifiOnlyVersion() {
        boolean ret_val = !EmuiFeatureManager.isSystemSMSCapable() ? !EmuiFeatureManager.isSystemVoiceCapable() : false;
        if (DBG) {
            HwLog.d("CommonUtilMethods", "isWifiOnlyVersion return ret_val = " + ret_val);
        }
        return ret_val;
    }

    public static boolean isDataOnlyVersion() {
        boolean ret_val = EmuiFeatureManager.isSystemSMSCapable() ? !EmuiFeatureManager.isSystemVoiceCapable() : false;
        if (DBG) {
            HwLog.d("CommonUtilMethods", "isDataOnlyVersion return ret_val = " + ret_val);
        }
        return ret_val;
    }

    public static int calcDailerTabIndex() {
        int ret_val = 0;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            ret_val = -1;
        }
        if (DBG) {
            HwLog.d("CommonUtilMethods", "calcDailerTabIndex TabState.DIALER = " + ret_val);
        }
        return ret_val;
    }

    public static int calcAllContactsTabIndex() {
        int ret_val = 1;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            ret_val = 0;
        }
        if (DBG) {
            HwLog.d("CommonUtilMethods", "calcAllContactsTabIndex TabState.ALL = " + ret_val);
        }
        return ret_val;
    }

    public static int calcFavorOrYPTabIndex() {
        int ret_val = 2;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            ret_val = 1;
        }
        if (HwLog.HWFLOW) {
            HwLog.i("CommonUtilMethods", "calcYellowPageTabIndex TabState.YELLOWPAGE = " + ret_val);
        }
        return ret_val;
    }

    public static int calcTabCount() {
        int ret_val = 3;
        if (!EmuiFeatureManager.isSystemVoiceCapable()) {
            ret_val = 2;
        }
        if (HwLog.HWFLOW) {
            HwLog.i("CommonUtilMethods", "calcTabCount TabState.COUNT = " + ret_val);
        }
        return ret_val;
    }

    public static boolean calcIfNeedSplitScreen() {
        return EmuiFeatureManager.isNeedSplitScreen();
    }

    public static boolean isLargeThemeApplied(Resources resources) {
        int color = resources.getColor(R.color.background_change);
        if (color == 0 || color == 2) {
            return true;
        }
        return false;
    }

    public static boolean isNotSupportRippleInLargeTheme(Resources resources) {
        return resources.getColor(R.color.background_change) == 2;
    }

    public static boolean isLargeThemeApplied(int bgChangeColor) {
        return bgChangeColor == 0 || bgChangeColor == 2;
    }

    public static boolean isSpecialLanguageForDialpad() {
        if (EmuiFeatureManager.isProductCustFeatureEnable() && mCust == null) {
            mCust = (HwCustCommonUtilMethods) HwCustUtils.createObj(HwCustCommonUtilMethods.class, new Object[0]);
        }
        if ((mCust != null && mCust.isHebrewLanForDialpad()) || SortUtils.isTWChineseDialpadShow()) {
            return true;
        }
        String[] specialLanguages = new String[]{"ru", "fa", "ar", "el", "ja", "th"};
        String language = Locale.getDefault().getLanguage();
        for (String equals : specialLanguages) {
            if (equals.equals(language)) {
                if (DBG) {
                    HwLog.d("CommonUtilMethods", "isSpecialLanguageForDialpad true");
                }
                return true;
            }
        }
        return false;
    }

    public static int getAccountHashCode(String accountType, String accountName) {
        if (accountType == null || accountName == null) {
            return 0;
        }
        return (accountType.hashCode() ^ accountName.hashCode()) & 4095;
    }

    public static String getHiCloudAccountLogOffDisplayString(Context mContext) {
        if (mContext == null) {
            return "";
        }
        return mContext.getString(R.string.contacts_HiCloud_Account_LogOff_Display_String);
    }

    public static String getHiCloudAccountLogOnSyncStateDisplayString(Context mContext, boolean isSync) {
        if (mContext == null) {
            return "";
        }
        String result;
        if (isSync) {
            result = getHiCloudAccountLogOnDisplayString(mContext);
        } else {
            result = getHiCloudAccountLogOffDisplayString(mContext);
        }
        return result;
    }

    public static int getEmergencyNumberSimSlot(String number, boolean isMultiSim) {
        if (TextUtils.isEmpty(number)) {
            return -1;
        }
        if (!isMultiSim) {
            return PhoneNumberUtils.isEmergencyNumber(number) ? 0 : -1;
        } else {
            boolean isSIM1CardPresent = SimFactoryManager.isSIM1CardPresent();
            boolean isSIM2CardPresent = SimFactoryManager.isSIM2CardPresent();
            boolean isSIM1CardEnable = SimFactoryManager.isSimEnabled(0);
            boolean isSIM2CardEnable = SimFactoryManager.isSimEnabled(1);
            if (isSIM1CardPresent && isSIM1CardEnable && isSIM2CardPresent && isSIM2CardEnable && PhoneNumberUtils.isEmergencyNumber(1, number) && PhoneNumberUtils.isEmergencyNumber(0, number)) {
                if (SimFactoryManager.isMultiSimDsda() || !SimFactoryManager.phoneIsOffhook(1)) {
                    return SimFactoryManager.getUserDefaultSubscription();
                }
                return 1;
            } else if (!isSIM1CardPresent && isSIM2CardPresent && PhoneNumberUtils.isEmergencyNumber(1, number)) {
                return 1;
            } else {
                if (!isSIM1CardEnable && !isSIM2CardEnable && PhoneNumberUtils.isEmergencyNumber(0, number)) {
                    return 0;
                }
                if (isSIM1CardEnable && PhoneNumberUtils.isEmergencyNumber(0, number)) {
                    return 0;
                }
                if (isSIM2CardPresent && PhoneNumberUtils.isEmergencyNumber(1, number)) {
                    return 1;
                }
                return PhoneNumberUtils.isEmergencyNumber(number) ? 0 : -1;
            }
        }
    }

    public static boolean isEmergencyNumber(String number, boolean isMultiSim) {
        return getEmergencyNumberSimSlot(number, isMultiSim) != -1;
    }

    public static boolean isLocalDefaultAccount(String accountType) {
        if ("com.android.huawei.phone".equalsIgnoreCase(accountType)) {
            return true;
        }
        return false;
    }

    public static boolean isLocalDefaultAccount(String accountType, String accountName) {
        if ("com.android.huawei.phone".equalsIgnoreCase(accountType) && "Phone".equalsIgnoreCase(accountName)) {
            return true;
        }
        return false;
    }

    public static boolean equalNumbers(String number1, String number2) {
        boolean mUseCallerInfo = SystemProperties.getBoolean("ro.config.hw_caller_info", true);
        if (PhoneNumberUtils.isUriNumber(number1) || PhoneNumberUtils.isUriNumber(number2)) {
            return compareSipAddresses(number1, number2);
        }
        if (number1 != null && number2 != null && number1.length() > 1 && number2.length() > 1 && (number1.codePointAt(number1.length() - 1) != number2.codePointAt(number2.length() - 1) || number1.codePointAt(number1.length() - 2) != number2.codePointAt(number2.length() - 2))) {
            return false;
        }
        if (mUseCallerInfo) {
            return compareNumsHw(number1, number2);
        }
        return PhoneNumberUtils.compare(number1, number2);
    }

    public static boolean compareSipAddresses(String number1, String number2) {
        boolean z = false;
        if (number1 == null || number2 == null) {
            return false;
        }
        String userinfo1;
        String rest1;
        String userinfo2;
        String rest2;
        int index1 = number1.indexOf(64);
        if (index1 != -1) {
            userinfo1 = number1.substring(0, index1);
            rest1 = number1.substring(index1);
        } else {
            userinfo1 = number1;
            rest1 = "";
        }
        int index2 = number2.indexOf(64);
        if (index2 != -1) {
            userinfo2 = number2.substring(0, index2);
            rest2 = number2.substring(index2);
        } else {
            userinfo2 = number2;
            rest2 = "";
        }
        if (userinfo1.equals(userinfo2)) {
            z = rest1.equalsIgnoreCase(rest2);
        }
        return z;
    }

    public static StringBuilder getDeleteCallLogIds(Context context, StringBuilder callLogIds) {
        StringBuilder callIds = callLogIds;
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            cursor = context.getContentResolver().query(Calls.CONTENT_URI_WITH_VOICEMAIL, new String[]{"_id", "number", "type"}, "_id IN (" + callLogIds + ")", null, null);
            cursor2 = context.getContentResolver().query(Calls.CONTENT_URI_WITH_VOICEMAIL, new String[]{"_id", "number", "type"}, "type <> 4 AND _id NOT IN (" + callLogIds + ")", null, null);
            if (cursor == null || cursor2 == null || cursor.getCount() == 0 || cursor2.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                return callLogIds;
            }
            cursor.moveToFirst();
            do {
                String number = cursor.getString(cursor.getColumnIndex("number"));
                if (cursor.getInt(cursor.getColumnIndex("type")) != 4) {
                    cursor2.moveToFirst();
                    while (true) {
                        if (equalNumbers(number, cursor2.getString(cursor2.getColumnIndex("number")))) {
                            if (callLogIds.length() != 0) {
                                callLogIds.append(",");
                            }
                            callLogIds.append(cursor2.getInt(cursor2.getColumnIndex("_id")));
                        }
                        if (!cursor2.moveToNext()) {
                            break;
                        }
                    }
                } else {
                    if (callLogIds.length() != 0) {
                        callLogIds.append(",");
                    }
                    callLogIds.append(cursor.getInt(cursor.getColumnIndex("_id")));
                }
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
            return callLogIds;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        }
    }

    public static boolean getShowCallLogMergeStatus(Context context) {
        return context != null && System.getInt(context.getContentResolver(), "call_log_merge_type", 0) == 1;
    }

    public static boolean getIsRefreshCalllog(Context context) {
        if (context != null) {
            return SharePreferenceUtil.getDefaultSp_de(context).getBoolean("reference_is_refresh_calllog", false);
        }
        return false;
    }

    public static boolean equalByNameOrNumber(String name1, String number1, String name2, String number2) {
        boolean result = false;
        if (!(name1 == null || name2 == null || name1.length() != name2.length())) {
            result = name1.equals(name2);
        }
        if (result) {
            return result;
        }
        if (!isChineseNumber(number1) || isChineseMobilePhoneNumber(number1) || !isChineseNumber(number2) || isChineseMobilePhoneNumber(number2)) {
            return true;
        }
        return normalizeNumber(number1).length() == normalizeNumber(number2).length();
    }

    private static boolean isChineseMobilePhoneNumber(String number) {
        if (!TextUtils.isEmpty(number) && number.length() >= 11 && number.substring(number.length() - 11).matches("^(1)\\d{10}$")) {
            return true;
        }
        return false;
    }

    private static boolean isChineseNumber(String number) {
        return "CN".equalsIgnoreCase(getCountryIsoFromDbNumberHw(number));
    }

    public static boolean isChineseLanguage() {
        if ("CN".equals(Locale.getDefault().getCountry())) {
            return true;
        }
        return false;
    }

    public static void predefineCust(final Context context, boolean inNewThread) {
        Runnable run = new Runnable() {
            public void run() {
                synchronized (CommonUtilMethods.mSyncObject) {
                    try {
                        if (-1 == System.getInt(context.getContentResolver(), "hw_service_contact_loaded", -1) && SetupPhoneAccount.addTypeAndParseLoadPreDefinedContacts(context)) {
                            System.putInt(context.getContentResolver(), "hw_service_contact_loaded", 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (inNewThread) {
            new Thread(run, "predefineCust").start();
        } else {
            run.run();
        }
    }

    public static TelephonyManager getTelephonyManager(Context context) {
        if (context == null) {
            return null;
        }
        if (sTelephonyManager == null) {
            sTelephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService("phone");
        }
        return sTelephonyManager;
    }

    public static String getMultiSelectionTitle(Context context, int count) {
        if (context == null) {
            return null;
        }
        return count > 0 ? context.getString(R.string.contacts_selected_text) : context.getString(R.string.contacts_not_selected_text);
    }

    public static String getSIMCountryIso(int slotId) {
        String defaultCountryIso = SimFactoryManager.getSimCountryIso(slotId);
        if (TextUtils.isEmpty(defaultCountryIso)) {
            return defaultCountryIso;
        }
        return defaultCountryIso.toUpperCase(Locale.getDefault());
    }

    public static void enableFrameRadar(String method) {
        Jlog.d(81, "com.android.contacts", method);
    }

    public static void disableFrameRadar(String method) {
        Jlog.d(82, "com.android.contacts", method);
    }

    public static boolean checkConnectivityStatus(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info == null || !info.isAvailable()) {
            return false;
        }
        return true;
    }

    public static boolean isNetworkWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null && info.isAvailable() && info.getType() == 1) {
            return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null) {
            return null;
        }
        return cm.getActiveNetworkInfo();
    }

    public static boolean existGroup(Context context, String accountType) {
        if (context == null) {
            HwLog.w("CommonUtilMethods", "In existGroup(): context is null, please check");
            return false;
        }
        String lAccountType = accountType;
        Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI, new String[]{"_id"}, "account_type=?", new String[]{accountType}, null);
        boolean existGroup = false;
        if (cursor != null) {
            int size = cursor.getCount();
            if (HwLog.HWFLOW) {
                HwLog.i("CommonUtilMethods", "cursor size:" + size);
            }
            if (size > 0) {
                existGroup = true;
            }
            cursor.close();
        }
        return existGroup;
    }

    public static void appendEscapedSQLString(StringBuilder sb, String sqlString) {
        sb.append('\'');
        if (sqlString.indexOf(39) != -1) {
            int length = sqlString.length();
            for (int i = 0; i < length; i++) {
                char c = sqlString.charAt(i);
                if (c == '\'') {
                    sb.append('\'');
                }
                sb.append(c);
            }
        } else {
            sb.append(sqlString);
        }
        sb.append('\'');
    }

    public static int queryCallNumberLastSlot(String number, Context context) {
        int slotId = -1;
        if (TextUtils.isEmpty(number) || context == null) {
            return -1;
        }
        String[] projection = new String[]{"number", "subscription_id"};
        String selection = "_id IN ( SELECT _id FROM Calls WHERE features<> 32 AND PHONE_NUMBERS_EQUAL(number,?) GROUP BY number )";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Calls.CONTENT_URI, projection, selection, new String[]{number}, "date DESC");
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!compareNumsHw(number, cursor.getString(cursor.getColumnIndex("number")))) {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                }
                slotId = cursor.getInt(cursor.getColumnIndex("subscription_id"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            HwLog.w("CommonUtilMethods", "can't get slot id!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            HwLog.w("CommonUtilMethods", "can't get slot id!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return slotId;
    }

    public static int queryLastCallNumberFromCust(String number, Context context) {
        if (EmuiFeatureManager.isProductCustFeatureEnable() && mCust == null) {
            mCust = (HwCustCommonUtilMethods) HwCustUtils.createObj(HwCustCommonUtilMethods.class, new Object[0]);
        }
        if (mCust != null) {
            return mCust.queryLastCallNumberFromCust(number, context);
        }
        return -1;
    }

    public static int queryLastCallNumberFromEncryptCall(String number, Context context) {
        int slotId = -1;
        if (TextUtils.isEmpty(number) || context == null) {
            return -1;
        }
        String[] projection = new String[]{"number", "subscription_id", "encrypt_call"};
        StringBuilder selection = new StringBuilder().append("_id").append(" IN ( SELECT ").append("_id").append(" FROM Calls WHERE ").append("features").append("<> ").append(32).append(" AND PHONE_NUMBERS_EQUAL(").append("number").append(",?) GROUP BY ").append("number").append(" )");
        Cursor cursor = null;
        int encrypt = -1;
        try {
            cursor = context.getContentResolver().query(Calls.CONTENT_URI, projection, selection.toString(), new String[]{number}, "date DESC");
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!compareNumsHw(number, cursor.getString(cursor.getColumnIndex("number")))) {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                }
                slotId = cursor.getInt(cursor.getColumnIndex("subscription_id"));
                encrypt = cursor.getInt(cursor.getColumnIndex("encrypt_call"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Log.d("CommonUtilMethods", "can't get slot id!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (encrypt == 1) {
            if (slotId == 0) {
                slotId = 2;
            } else if (slotId == 1) {
                slotId = 3;
            }
        }
        return slotId;
    }

    public static boolean isMissedType(int callType) {
        if (callType == 3 || callType == 5) {
            return true;
        }
        return false;
    }

    public static String[] getProjection(ContactsPreferences mContactsPrefs) {
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            if (mContactsPrefs.getDisplayOrder() == 1) {
                return ContactQuery.CONTACT_PROJECTION_PRIMARY_PRIVATE;
            }
            return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE_PRIVATE;
        } else if (mContactsPrefs.getDisplayOrder() == 1) {
            return ContactQuery.CONTACT_PROJECTION_PRIMARY;
        } else {
            return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE;
        }
    }

    public static Uri configureFilterUri(Context mContext, ContactListFilter filter, Uri uri) {
        if (filter == null || filter.filterType == -3 || filter.filterType == -6) {
            return uri;
        }
        Uri.Builder builder = uri.buildUpon();
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(mContext)) {
            builder.appendQueryParameter("directory", String.valueOf(0));
        }
        if (filter.filterType == 0) {
            filter.addAccountQueryParameterToUrl(builder);
        }
        return builder.build();
    }

    public static boolean isCustomFilterForPhoneNumbersOnly(Context mContext) {
        return SharePreferenceUtil.getDefaultSp_de(mContext).getBoolean("only_phones", false);
    }

    private static void filterHotNumber(int filter, StringBuilder selection, boolean showSimContactsOrisDisplayPhoneNumber) {
        if (HLUtils.isShowHotNumberOnTop) {
            switch (filter) {
                case -3:
                    selection.append("is_care=0");
                    selection.append(" AND ");
                    break;
                case -2:
                    if (!showSimContactsOrisDisplayPhoneNumber) {
                        selection.append(" AND ");
                    }
                    selection.append("is_care=0");
                    break;
                case 0:
                    selection.append("is_care=0");
                    if (showSimContactsOrisDisplayPhoneNumber) {
                        selection.append(" AND ");
                        break;
                    }
                    break;
            }
        }
    }

    public static void configureFilterSelection(Context mContext, ContactListFilter filter, StringBuilder selection, List<String> selectionArgs, boolean mIgnoreShowSimContactsPref) {
        int i = 0;
        if (filter != null) {
            SharedPreferences pref = SharePreferenceUtil.getDefaultSp_de(mContext);
            boolean showSimContactspreference = pref.getBoolean("preference_show_sim_contacts", true);
            if (mIgnoreShowSimContactsPref) {
                showSimContactspreference = true;
            }
            boolean isDisplayOnlyContactsWithPhoneNumber = pref.getBoolean("preference_contacts_only_phonenumber", false);
            List<AccountWithDataSet> accounts;
            StringBuilder queryParam;
            switch (filter.filterType) {
                case -16:
                    accounts = AccountTypeManager.getInstance(mContext).getAccounts(true);
                    queryParam = new StringBuilder();
                    for (AccountWithDataSet account : accounts) {
                        selectionArgs.add(account.type);
                        queryParam.append("?,");
                    }
                    if (queryParam.length() > 0) {
                        queryParam.setLength(queryParam.length() - 1);
                    }
                    selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE account_type IN ( ").append(queryParam.toString()).append(") AND ").append("account_type").append("!=? AND ").append("raw_contact_is_read_only").append("=0 )");
                    selectionArgs.add("com.android.huawei.secondsim");
                    break;
                case -15:
                    accounts = AccountTypeManager.getInstance(mContext).getAccounts(true);
                    queryParam = new StringBuilder();
                    for (AccountWithDataSet account2 : accounts) {
                        selectionArgs.add(account2.type);
                        queryParam.append("?,");
                    }
                    if (queryParam.length() > 0) {
                        queryParam.setLength(queryParam.length() - 1);
                    }
                    selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE account_type IN ( ").append(queryParam.toString()).append(") AND ").append("account_type").append("!=? AND ").append("raw_contact_is_read_only").append("=0 )");
                    selectionArgs.add("com.android.huawei.sim");
                    break;
                case -13:
                    accounts = AccountTypeManager.getInstance(mContext).getAccounts(true);
                    queryParam = new StringBuilder();
                    for (AccountWithDataSet account22 : accounts) {
                        selectionArgs.add(account22.type);
                        queryParam.append("?,");
                    }
                    if (queryParam.length() > 0) {
                        i = queryParam.length() - 1;
                    }
                    queryParam.setLength(i);
                    selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE ((account_type IN ( ").append(queryParam.toString()).append(") OR ").append("account_type").append(" IS NULL ) AND ").append("raw_contact_is_read_only").append("=0))");
                    break;
                case -12:
                    accounts = AccountTypeManager.getInstance(mContext).getAccounts(true);
                    queryParam = new StringBuilder();
                    for (AccountWithDataSet account222 : accounts) {
                        selectionArgs.add(account222.type);
                        queryParam.append("?,");
                    }
                    if (queryParam.length() > 0) {
                        queryParam.setLength(queryParam.length() - 1);
                    }
                    if (!SimFactoryManager.isDualSim()) {
                        selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE account_type IN ( ").append(queryParam.toString()).append(") AND ").append("account_type").append("!=? AND ").append("raw_contact_is_read_only").append("=0 )");
                        selectionArgs.add("com.android.huawei.sim");
                        break;
                    }
                    selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE account_type IN ( ").append(queryParam.toString()).append(") AND ").append("account_type").append("!=? AND ").append("account_type").append("!=? AND ").append("raw_contact_is_read_only").append("=0 )");
                    selectionArgs.add("com.android.huawei.sim");
                    selectionArgs.add("com.android.huawei.secondsim");
                    break;
                case -5:
                    selection.append("has_phone_number=1");
                    break;
                case -4:
                    selection.append("starred!=0");
                    break;
                case -3:
                    filterHotNumber(filter.filterType, selection, showSimContactspreference);
                    selection.append("in_visible_group=1");
                    if ((isCustomFilterForPhoneNumbersOnly(mContext) | isDisplayOnlyContactsWithPhoneNumber) != 0) {
                        selection.append(" AND has_phone_number=1");
                        break;
                    }
                    break;
                case -2:
                    if (!showSimContactspreference) {
                        selection.append("_id").append(" IN (SELECT DISTINCT ").append("contact_id").append(" FROM view_raw_contacts WHERE ");
                        if (SimFactoryManager.isDualSim()) {
                            selection.append("account_type").append(" NOT IN (?,?)");
                            selectionArgs.add("com.android.huawei.sim");
                            selectionArgs.add("com.android.huawei.secondsim");
                        } else {
                            selection.append("account_type").append("!=?");
                            selectionArgs.add("com.android.huawei.sim");
                        }
                        selection.append(")");
                    }
                    filterHotNumber(filter.filterType, selection, showSimContactspreference);
                    if (isDisplayOnlyContactsWithPhoneNumber) {
                        if (!TextUtils.isEmpty(selection)) {
                            selection.append(" AND ");
                        }
                        selection.append("has_phone_number").append("=1");
                        break;
                    }
                    break;
                case 0:
                    filterHotNumber(filter.filterType, selection, isDisplayOnlyContactsWithPhoneNumber);
                    if (isDisplayOnlyContactsWithPhoneNumber) {
                        selection.append("has_phone_number=1");
                        break;
                    }
                    break;
            }
        }
    }

    public static long getContactIdFromUri(Uri contactLookupUri) {
        if (contactLookupUri == null) {
            HwLog.w("CommonUtilMethods", "contactLookupUri is null");
            return -1;
        }
        List<String> pathSegmentList = contactLookupUri.getPathSegments();
        long contactId = -1;
        if (pathSegmentList.size() == 4) {
            try {
                contactId = Long.parseLong((String) pathSegmentList.get(3));
            } catch (NumberFormatException e) {
                HwLog.w("CommonUtilMethods", "Fail to get segment contact_id from uri.");
                contactId = 0;
            }
        }
        return contactId;
    }

    public static String getAccountTypeFromUri(Context context, long contactId) {
        String accountType = null;
        Cursor cursor = null;
        try {
            cursor = ContactQuery.queryContactInfo(context, "contact_id=?", new String[]{String.valueOf(contactId)}, null);
            if (cursor != null && cursor.moveToFirst()) {
                accountType = cursor.getString(17);
            }
            if (cursor != null) {
                cursor.close();
            }
            return accountType;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void constructAndSendSummaryNotification(Context context, String title) {
        if (context != null) {
            ((NotificationManager) context.getSystemService("notification")).notify("contact_summary_notification_tag", 100, new Notification.Builder(context).setAutoCancel(true).setSmallIcon(getBitampIcon(context, R.drawable.ic_notification_contacts)).setGroup("group_key_contacts").setGroupSummary(true).setShowWhen(true).getNotification());
        }
    }

    public static void constructAndSendVvmSummaryNotification(Context context, String title) {
        if (context != null) {
            ((NotificationManager) context.getSystemService("notification")).notify("contact_summary_notification_tag", 101, new Notification.Builder(context).setAutoCancel(true).setSmallIcon(getBitampIcon(context, R.drawable.ic_notification_voicemail)).setGroup("group_key_contacts_vvm").setAppName(context.getString(R.string.voicemail)).setGroupSummary(true).setShowWhen(true).getNotification());
        }
    }

    public static View addFootEmptyViewPortrait(ListView listView, Context context) {
        boolean isPortrait = true;
        if (context == null || listView == null) {
            return null;
        }
        boolean isNeedaddFoot = false;
        if (context.getResources().getConfiguration().orientation != 1) {
            isPortrait = false;
        }
        if (isPortrait) {
            isNeedaddFoot = true;
        }
        if (context instanceof Activity) {
            Activity act = (Activity) context;
            boolean multiWindowMode = act.isInMultiWindowMode();
            if (multiWindowMode) {
                isNeedaddFoot = true;
            }
            if (calcIfNeedSplitScreen() && ContactSplitUtils.isSpiltTwoColumn(act, multiWindowMode) && !isPortrait) {
                isNeedaddFoot = true;
            }
        }
        View footerView = null;
        if (isNeedaddFoot) {
            footerView = LayoutInflater.from(context).inflate(R.layout.blank_footer_view, listView, false);
            listView.addFooterView(footerView, null, false);
        }
        return footerView;
    }

    public static void disableActionBarShowHideAnimation(ActionBar actionBar) {
        if (actionBar instanceof WindowDecorActionBar) {
            ((WindowDecorActionBar) actionBar).setShowHideAnimationEnabled(false);
        }
    }

    public static boolean isVoicemailAvailable(int aSubscriptionId) {
        boolean z = false;
        try {
            String voicemailNumber = SimFactoryManager.getVoiceMailNumber(aSubscriptionId);
            if (!(voicemailNumber == null || TextUtils.isEmpty(voicemailNumber))) {
                z = true;
            }
            return z;
        } catch (SecurityException e) {
            HwLog.w("CommonUtilMethods", "SecurityException is thrown. Maybe privilege isn't sufficient.");
            return false;
        }
    }

    public static String upPercase(String title) {
        if (title != null) {
            return title.toUpperCase();
        }
        return null;
    }

    public static int getMarginTopPix(Activity activity, double d, boolean isPor) {
        return (int) ((((double) getScreenHeight(activity)) * d) - ((double) getActionBarAndStatusHeight(activity, isPor)));
    }

    public static int getStatusHeight(Activity activity) {
        return activity.getResources().getDimensionPixelSize(R.dimen.contact_statusbar_height);
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) activity.getSystemService("window")).getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getActionBarAndStatusHeight(Activity activity, boolean isPor) {
        int actionBar = activity.getResources().getDimensionPixelSize(R.dimen.contact_action_bar_horizontal_height);
        if (isPor) {
            actionBar = activity.getResources().getDimensionPixelSize(R.dimen.contact_action_bar_vertical_height);
        }
        return getStatusHeight(activity) + actionBar;
    }

    public static final void saveInstanceState(Bundle bundle) {
        mInstanceState = bundle;
    }

    public static final Bundle getInstanceState() {
        return mInstanceState;
    }

    public static void clearInstanceState() {
        mInstanceState = null;
    }

    public static Icon getBitampIcon(Context context, int resId) {
        if (context == null) {
            return null;
        }
        Config config;
        Drawable drawable = context.getResources().getDrawable(resId);
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }

    public static String convertTimeToBeDetailer(Context context, long originTimeMilli) {
        boolean isSameYear = true;
        if (!DateFormat.is24HourFormat(context) && DateUtils.isToday(originTimeMilli) && isChineseLanguage()) {
            String chinaDateTime = HwDateUtils.formatChinaDateTime(context, originTimeMilli, 1);
            if (chinaDateTime != null) {
                return chinaDateTime;
            }
            return DateUtils.getRelativeTimeSpanString(context, originTimeMilli).toString();
        } else if (DateUtils.isToday(originTimeMilli)) {
            return DateUtils.getRelativeTimeSpanString(context, originTimeMilli).toString();
        } else {
            String format;
            Date date = new Date(originTimeMilli);
            if (date.getYear() != new Date(System.currentTimeMillis()).getYear()) {
                isSameYear = false;
            }
            if (isSameYear) {
                format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "Md");
            } else {
                format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "Mdy");
            }
            return new SimpleDateFormat(format, Locale.getDefault()).format(date);
        }
    }

    public static void setNameViewDirection(TextView view) {
        if (EmuiFeatureManager.isProductCustFeatureEnable() && mCust == null) {
            mCust = (HwCustCommonUtilMethods) HwCustUtils.createObj(HwCustCommonUtilMethods.class, new Object[0]);
        }
        if (mCust != null) {
            mCust.setNameViewDirection(view);
        }
    }
}
