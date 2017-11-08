package com.android.contacts;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.util.HwLog;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.TelephonyCapabilities;
import com.google.android.gms.R;
import com.huawei.android.telephony.IIccPhoneBookManagerEx;
import com.huawei.cust.HwCustUtils;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Pattern;

public class SpecialCharSequenceMgr {
    private static final String CUST_OPTA = SystemProperties.get("ro.config.hw_opta", "");
    private static final boolean IS_FACTORY_RUNMODE = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    private static final boolean IS_SPRINT_ENG = SystemProperties.getBoolean("ro.config.hw_sprint_eng", false);
    private static final Pattern SPRINT_CODE_PATTERN = Pattern.compile("^#{2}[0-9]+#$");
    private static AlertDialog mAlertDialog;
    private static HwCustSpecialCharSequenceMgr mCust = null;
    private static volatile QueryHandler sPreviousAdnQueryHandler;

    private static class QueryHandler extends AsyncQueryHandler {
        private boolean mCanceled;

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            SpecialCharSequenceMgr.sPreviousAdnQueryHandler = null;
            if (this.mCanceled) {
                if (c != null) {
                    c.close();
                }
                return;
            }
            SimContactQueryCookie sc = (SimContactQueryCookie) cookie;
            sc.progressDialog.dismiss();
            EditText text = sc.getTextField();
            Context context = sc.progressDialog.getContext();
            boolean isNumberAvailable = false;
            if (!(c == null || text == null || !c.moveToPosition(sc.contactNum))) {
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                String number = c.getString(c.getColumnIndexOrThrow("number"));
                if (!TextUtils.isEmpty(number)) {
                    text.getText().replace(0, 0, number);
                    Toast.makeText(context, context.getString(R.string.menu_callNumber, new Object[]{name}), 0).show();
                    isNumberAvailable = true;
                }
            }
            if (!isNumberAvailable) {
                Toast.makeText(context, context.getString(R.string.str_communicationscreen_nonumber), 0).show();
            }
            if (c != null) {
                c.close();
            }
        }

        public void cancel() {
            this.mCanceled = true;
            cancelOperation(-1);
        }
    }

    private static class SimContactQueryCookie implements OnCancelListener {
        public int contactNum;
        private QueryHandler mHandler;
        private int mToken;
        public ProgressDialog progressDialog;
        private EditText textField;

        public SimContactQueryCookie(int number, QueryHandler handler, int token) {
            this.contactNum = number;
            this.mHandler = handler;
            this.mToken = token;
        }

        public synchronized EditText getTextField() {
            return this.textField;
        }

        public synchronized void setTextField(EditText text) {
            this.textField = text;
        }

        public synchronized void onCancel(DialogInterface dialog) {
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
            }
            this.textField = null;
            this.mHandler.cancelOperation(this.mToken);
        }
    }

    private SpecialCharSequenceMgr() {
    }

    public static boolean handleChars(Context context, String input, EditText textField) {
        if (context == null) {
            return false;
        }
        return handleChars(context, input, false, textField);
    }

    static boolean handleChars(Context context, String input, boolean useSystemWindow, EditText textField) {
        if (mCust == null && EmuiFeatureManager.isProductCustFeatureEnable()) {
            mCust = (HwCustSpecialCharSequenceMgr) HwCustUtils.createObj(HwCustSpecialCharSequenceMgr.class, new Object[0]);
        }
        String dialString = PhoneNumberUtils.stripSeparators(input);
        if (handleIMEIDisplay(context, dialString, useSystemWindow) || handlePinEntry(context, dialString) || handleNamName(context, dialString) || handleAdnEntry(context, dialString, textField) || handleSecretCode(context, dialString)) {
            return true;
        }
        if (mCust != null) {
            return mCust.handleCustSpecialCharSequence(context, dialString);
        }
        return false;
    }

    static boolean handleNamName(Context context, String input) {
        int len = input.length();
        if (len > 3 && input.startsWith("##") && input.endsWith("*")) {
            String lSubString = input.substring(2, len - 1);
            if ("626".equalsIgnoreCase(lSubString)) {
                context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://626")));
                return true;
            } else if ("7764".equalsIgnoreCase(lSubString)) {
                context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://7764")));
                return true;
            } else if ("4357".equalsIgnoreCase(lSubString)) {
                context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://4357")));
                return true;
            }
        }
        return false;
    }

    public static void cleanup() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            HwLog.wtf("SpecialCharSequenceMgr", "cleanup() is called outside the main thread");
            return;
        }
        if (sPreviousAdnQueryHandler != null) {
            sPreviousAdnQueryHandler.cancel();
            sPreviousAdnQueryHandler = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void dismissAlertDialog() {
        synchronized (SpecialCharSequenceMgr.class) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                HwLog.wtf("SpecialCharSequenceMgr", "dismissAlertDialog() is called outside the main thread");
            } else if (mAlertDialog != null && mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
                mAlertDialog = null;
            }
        }
    }

    private static String formateInput(String input) {
        if (input.equals("*#2846#")) {
            return "2845";
        }
        if (input.equals("*#2846*")) {
            return "2847";
        }
        if (input.equals("*#28465#")) {
            return "28465";
        }
        return null;
    }

    static boolean handleSecretCode(Context context, String input) {
        boolean secretBroadcast;
        if (mCust == null && EmuiFeatureManager.isProductCustFeatureEnable()) {
            mCust = (HwCustSpecialCharSequenceMgr) HwCustUtils.createObj(HwCustSpecialCharSequenceMgr.class, new Object[0]);
        }
        int len = input.length();
        String cust = CUST_OPTA;
        if ("177".equalsIgnoreCase(cust)) {
            secretBroadcast = true;
        } else {
            secretBroadcast = "153".equalsIgnoreCase(cust);
        }
        boolean sprint = IS_SPRINT_ENG;
        boolean isFactoryVersion = IS_FACTORY_RUNMODE;
        if (mCust != null && mCust.checkForDisableHiddenMenuItems(context, input)) {
            return true;
        }
        Intent intent;
        if (len > 8) {
            if (input.startsWith("*#*#")) {
                if (input.endsWith("#*#*")) {
                    String number = input.substring(4, len - 4);
                    intent = new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + number));
                    if ("2846579".equals(number)) {
                        if (CommonUtilMethods.isStudentModeOn(context) || !MultiUsersUtils.isCurrentUserOwner()) {
                            return false;
                        }
                        intent.addFlags(268435456);
                    } else if ("2846579159".equals(number) && (CommonUtilMethods.isStudentModeOn(context) || !MultiUsersUtils.isCurrentUserOwner())) {
                        return false;
                    }
                    context.sendBroadcast(intent);
                    return true;
                }
            }
        }
        if (!input.equals("*#2846#")) {
            if (!input.equals("*#2846*")) {
                if (!input.equals("*#28465#")) {
                    if (isFactoryVersion && "#1#".equals(input)) {
                        sendBroadcastBySpecifiedSecretCode(context, "19467328");
                        return true;
                    }
                    String lSubString;
                    Context context2;
                    if (len > 3) {
                        if (input.startsWith("*#")) {
                            if (input.endsWith("#")) {
                                lSubString = input.substring(2, len - 1);
                                if ("0100".equalsIgnoreCase(lSubString) || "0000".equalsIgnoreCase(lSubString)) {
                                    context2 = context;
                                    context2.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + input.substring(2, len - 1))));
                                    return true;
                                }
                                if (mCust != null && mCust.isEnableCustomSwitch(lSubString, context)) {
                                    return true;
                                }
                                return false;
                            }
                        }
                    }
                    if (sprint && SPRINT_CODE_PATTERN.matcher(input).matches()) {
                        if (input.startsWith("##21#")) {
                            HwLog.i("SpecialCharSequenceMgr", "mmicode code for callforward");
                            return false;
                        }
                        context2 = context;
                        context2.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("sprint_secret_code://" + input.substring(2, len - 1))));
                        return true;
                    }
                    if (input.equals("*94932580#")) {
                        String content = input.substring(1, len - 1);
                        context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("hw_secret_code://" + content)));
                        return true;
                    }
                    if (secretBroadcast && len > 3) {
                        if (input.startsWith("##")) {
                            if (input.endsWith("*")) {
                                lSubString = input.substring(2, len - 1);
                                if ("4357".equalsIgnoreCase(lSubString)) {
                                    context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + lSubString)));
                                    return true;
                                }
                                return false;
                            }
                        }
                    }
                    if (secretBroadcast && len > 2) {
                        if (input.startsWith("##")) {
                            lSubString = input.substring(2, len);
                            if ("236985".equalsIgnoreCase(lSubString)) {
                                context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + lSubString)));
                                return true;
                            }
                            return false;
                        }
                    }
                    if (input.equals("*#28465#*")) {
                        context.sendBroadcast(new Intent("android.provider.Telephony.Intents.SECRET_CODE_ACTION", Uri.parse("android_secret_code://28456")));
                        return true;
                    } else if ("##786#".equals(input) || "##25327#".equals(input)) {
                        context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("sprint_secret_code://786")));
                        return true;
                    } else if ("##873283#".equals(input)) {
                        context.sendBroadcast(new Intent("com.huawei.android.intent.action.UPDATE"));
                        return true;
                    } else if ("##66264#".equals(input)) {
                        context.sendBroadcast(new Intent("com.huawei.android.intent.action.OMANI"));
                        return true;
                    } else if (mCust != null && mCust.handleSimUnLockBroadcast(context, input)) {
                        return true;
                    } else {
                        try {
                            String subString = IIccPhoneBookManagerEx.getDefault().getSecretCodeSubString(input);
                            if (subString != null) {
                                context.sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + subString)));
                                return true;
                            }
                        } catch (RuntimeException e) {
                            HwLog.w("SpecialCharSequenceMgr", "handleSecretCode:" + e.getMessage());
                        }
                        return false;
                    }
                }
            }
        }
        intent = new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + formateInput(input)));
        intent.addFlags(268435456);
        context.sendBroadcast(intent);
        return true;
    }

    static void sendBroadcastBySpecifiedSecretCode(Context context, String lSubString) {
        if (context != null) {
            Intent intent = new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + lSubString));
            intent.addFlags(268435456);
            context.sendBroadcast(intent);
        }
    }

    static boolean handleAdnEntry(Context context, String input, EditText textField) {
        int len = input.length();
        if (len > 1 && len < 5) {
            if (input.endsWith("#")) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (telephonyManager == null || !TelephonyCapabilities.supportsAdn(telephonyManager.getCurrentPhoneType())) {
                        return false;
                    }
                    if (((KeyguardManager) context.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
                        return false;
                    }
                    int index = Integer.parseInt(input.substring(0, len - 1));
                    QueryHandler handler = new QueryHandler(context.getContentResolver());
                    SimContactQueryCookie sc = new SimContactQueryCookie(index - 1, handler, -1);
                    sc.contactNum = index - 1;
                    sc.setTextField(textField);
                    sc.progressDialog = new ProgressDialog(context);
                    int lSubId = -1;
                    Uri lSimUri = null;
                    if (SimFactoryManager.isDualSim()) {
                        lSubId = SimFactoryManager.getPreferredVoiceSubscripion();
                        lSimUri = SimFactoryManager.getProviderUri(lSubId);
                    }
                    sc.progressDialog.setTitle(String.format(context.getString(R.string.simContacts_title), new Object[]{SimFactoryManager.getSimCardDisplayLabel(lSubId)}));
                    sc.progressDialog.setMessage(String.format(context.getString(R.string.simContacts_emptyLoading), new Object[]{SimFactoryManager.getSimCardDisplayLabel(lSubId)}));
                    sc.progressDialog.setIndeterminate(true);
                    sc.progressDialog.setCancelable(true);
                    sc.progressDialog.setOnCancelListener(sc);
                    sc.progressDialog.show();
                    if (SimFactoryManager.isDualSim()) {
                        handler.startQuery(-1, sc, lSimUri, new String[]{"number"}, null, null, null);
                    } else {
                        handler.startQuery(-1, sc, Uri.parse("content://icc/adn"), new String[]{"number"}, null, null, null);
                    }
                    if (sPreviousAdnQueryHandler != null) {
                        sPreviousAdnQueryHandler.cancel();
                    }
                    sPreviousAdnQueryHandler = handler;
                    return true;
                } catch (NumberFormatException e) {
                }
            }
        }
        return false;
    }

    static boolean handlePinEntry(Context context, String input) {
        if ((!input.startsWith("**04") && !input.startsWith("**05")) || !input.endsWith("#") || SimFactoryManager.isDualSim()) {
            return false;
        }
        try {
            ITelephony telephony = Stub.asInterface(ServiceManager.getService("phone"));
            if (telephony != null) {
                return telephony.handlePinMmi(input);
            }
            HwLog.w("SpecialCharSequenceMgr", "Telephony service is null, can't call handlePinMmi");
        } catch (RemoteException e) {
            HwLog.e("SpecialCharSequenceMgr", "Failed to handlePinMmi due to remote exception");
            return false;
        }
        return false;
    }

    static void showDual_IMEI_Panel(Context context, boolean useSystemWindow) {
        int lSimCombination = SimFactoryManager.getSimCombination();
        String str = null;
        String str2 = null;
        boolean sameIMEI = false;
        String lStringForTitle = null;
        String lMeid = context.getResources().getString(R.string.meid);
        String lImei = context.getResources().getString(R.string.imei);
        String lPesn = context.getResources().getString(R.string.pesn);
        String lImei1 = context.getResources().getString(R.string.imei1);
        String lImei2 = context.getResources().getString(R.string.imei2);
        if (lSimCombination == 1) {
            str = (lMeid + ":" + SimFactoryManager.getDeviceId(0) + "\n" + lPesn + ":" + SimFactoryManager.getPesn(0)).toUpperCase();
            str2 = (lMeid + ":" + SimFactoryManager.getDeviceId(1) + "\n" + lPesn + ":" + SimFactoryManager.getPesn(1)).toUpperCase();
            lStringForTitle = lMeid + "_" + lMeid;
        } else if (lSimCombination == 2) {
            String imei1;
            String imei2;
            int isGet4GSubscription = SimFactoryManager.getUserDefaultSubscription();
            if (isGet4GSubscription == 0) {
                imei1 = SimFactoryManager.getImei(0);
                imei2 = SimFactoryManager.getImei(1);
            } else {
                imei1 = SimFactoryManager.getImei(1);
                imei2 = SimFactoryManager.getImei(0);
            }
            MSimTelephonyManager lMSimTelephonyManager = MSimTelephonyManager.getDefault();
            if (!(lMSimTelephonyManager == null || 2 == lMSimTelephonyManager.getCurrentPhoneType(isGet4GSubscription))) {
                if (isGet4GSubscription == 0 && 2 == lMSimTelephonyManager.getCurrentPhoneType(1)) {
                    isGet4GSubscription = 1;
                } else if (1 == isGet4GSubscription && 2 == lMSimTelephonyManager.getCurrentPhoneType(0)) {
                    isGet4GSubscription = 0;
                }
            }
            String meid = SimFactoryManager.getMeid(isGet4GSubscription);
            String pesn = SimFactoryManager.getPesn(isGet4GSubscription);
            if (meid == null) {
                meid = SimFactoryManager.getDeviceId(0);
            }
            if (pesn == null) {
                pesn = SimFactoryManager.getPesn(0);
            }
            if (!TextUtils.equals(imei1, imei2) && !TextUtils.isEmpty(imei1) && !TextUtils.isEmpty(imei2)) {
                str2 = lImei1 + ":" + imei1 + "\n" + lImei2 + ":" + imei2;
            } else if (TextUtils.isEmpty(imei1) && !TextUtils.isEmpty(imei2)) {
                str2 = lImei + ":" + imei2;
            } else if (TextUtils.isEmpty(imei2) && !TextUtils.isEmpty(imei1)) {
                str2 = lImei + ":" + imei1;
            } else if (TextUtils.isEmpty(imei1) && TextUtils.isEmpty(imei2)) {
                str2 = lImei + ":" + SimFactoryManager.getDeviceId(1);
            } else {
                str2 = lImei + ":" + imei1;
            }
            str = (lMeid + ":" + meid + "\n" + lPesn + ":" + pesn).toUpperCase();
            lStringForTitle = lMeid + "_" + lImei;
        } else if (lSimCombination == 3) {
            str = lImei + ":" + SimFactoryManager.getDeviceId(0);
            str2 = (lMeid + ":" + SimFactoryManager.getDeviceId(1) + "\n" + lPesn + ":" + SimFactoryManager.getPesn(1)).toUpperCase();
            lStringForTitle = lImei + "_" + lMeid;
        } else if (lSimCombination == 4) {
            String imei1Str;
            String imei2Str;
            if (SimFactoryManager.getUserDefaultSubscription() == 0) {
                imei1Str = SimFactoryManager.getDeviceId(0);
                imei2Str = SimFactoryManager.getDeviceId(1);
            } else {
                imei1Str = SimFactoryManager.getDeviceId(1);
                imei2Str = SimFactoryManager.getDeviceId(0);
            }
            if (imei1Str == null || !imei1Str.equalsIgnoreCase(imei2Str)) {
                lStringForTitle = lImei + "_" + lImei;
                str = lImei1 + ":" + imei1Str;
                str2 = lImei2 + ":" + imei2Str;
            } else {
                lStringForTitle = lImei;
                str = lImei + ":" + imei1Str;
                str2 = lImei + ":" + imei2Str;
                sameIMEI = true;
            }
        }
        if (HwLog.HWDBG) {
            if (str != null) {
                HwLog.d("SpecialCharSequenceMgr", "meidStr " + str.length());
            } else {
                HwLog.d("SpecialCharSequenceMgr", "meidStr is having null value");
            }
            if (str2 != null) {
                HwLog.d("SpecialCharSequenceMgr", "imeiStr " + str2.length());
            } else {
                HwLog.d("SpecialCharSequenceMgr", "imeiStr is having null value");
            }
        }
        Builder builder = new Builder(context).setTitle(lStringForTitle).setPositiveButton(17039370, null).setCancelable(false).setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface aDialog, int aKeyCode, KeyEvent aKeyEvent) {
                if (84 == aKeyCode) {
                    return true;
                }
                return false;
            }
        });
        LayoutInflater inflator = (LayoutInflater) context.getSystemService("layout_inflater");
        if (inflator != null) {
            View view = inflator.inflate(R.layout.alert_dialog_content, null);
            TextView content = (TextView) view.findViewById(R.id.alert_dialog_content);
            if (sameIMEI) {
                content.setText(str);
            } else {
                content.setText(str + "\n" + str2);
            }
            content.setTextIsSelectable(true);
            builder.setView(view);
        } else if (sameIMEI) {
            builder.setMessage(str);
        } else {
            builder.setMessage(str + "\n" + str2);
        }
        mAlertDialog = builder.show();
    }

    static boolean handleIMEIDisplay(Context context, String input, boolean useSystemWindow) {
        if (input.equals("*#06#")) {
            if (SimFactoryManager.isDualSim()) {
                showDual_IMEI_Panel(context, useSystemWindow);
                return true;
            }
            int phoneType = ((TelephonyManager) context.getSystemService("phone")).getCurrentPhoneType();
            if (SystemProperties.getBoolean("ro.config.cdma_quiet", false) || phoneType == 1) {
                showIMEIPanel(context, useSystemWindow);
                return true;
            } else if (phoneType == 2) {
                showMEIDPanel(context, useSystemWindow);
                return true;
            }
        }
        return false;
    }

    static void showIMEIPanel(Context context, boolean useSystemWindow) {
        if (mCust == null && EmuiFeatureManager.isProductCustFeatureEnable()) {
            mCust = (HwCustSpecialCharSequenceMgr) HwCustUtils.createObj(HwCustSpecialCharSequenceMgr.class, new Object[0]);
        }
        String imeiStr = CommonUtilMethods.getTelephonyManager(context).getImei();
        String lTitleString = context.getString(R.string.imei);
        if (mCust != null) {
            imeiStr = mCust.customizedImeiDisplay(context, imeiStr);
            lTitleString = mCust.customizedImeiTitle(context, lTitleString, imeiStr);
        }
        Builder builder = new Builder(context).setTitle(lTitleString).setPositiveButton(17039370, null).setCancelable(false);
        LayoutInflater inflator = (LayoutInflater) context.getSystemService("layout_inflater");
        if (inflator != null) {
            View view = inflator.inflate(R.layout.alert_dialog_content, null);
            TextView content = (TextView) view.findViewById(R.id.alert_dialog_content);
            content.setText(imeiStr);
            content.setTextIsSelectable(true);
            builder.setView(view);
        } else {
            builder.setMessage(imeiStr);
        }
        mAlertDialog = builder.show();
    }

    static void showMEIDPanel(Context context, boolean useSystemWindow) {
        if (mCust == null && EmuiFeatureManager.isProductCustFeatureEnable()) {
            mCust = (HwCustSpecialCharSequenceMgr) HwCustUtils.createObj(HwCustSpecialCharSequenceMgr.class, new Object[0]);
        }
        TelephonyManager lTeleMngr = (TelephonyManager) context.getSystemService("phone");
        String meidStr = null;
        CharSequence charSequence = null;
        try {
            charSequence = context.getString(R.string.meid);
            Method method = TelephonyManager.class.getDeclaredMethod("getPesn", new Class[0]);
            method.setAccessible(true);
            String lPesn = (String) method.invoke(lTeleMngr, (Object[]) null);
            if (!TextUtils.isEmpty(lPesn)) {
                meidStr = charSequence + ":" + HwTelephonyManager.getDefault().getMeid() + "\n" + context.getString(R.string.pesn) + ":" + lPesn;
            }
            if (mCust != null) {
                meidStr = mCust.customizedMeidDisplay(context, meidStr);
                charSequence = mCust.customizedMeidTitle(context, charSequence, meidStr);
            }
        } catch (Exception aEx) {
            aEx.printStackTrace();
        }
        if (meidStr == null) {
            meidStr = HwTelephonyManager.getDefault().getMeid();
        }
        if (mCust != null) {
            meidStr = mCust.getCustomizedMEID(context, meidStr);
        }
        if (meidStr != null) {
            meidStr = meidStr.toUpperCase(Locale.getDefault());
            Builder builder = new Builder(context).setTitle(charSequence).setPositiveButton(17039370, null).setCancelable(false);
            LayoutInflater inflator = (LayoutInflater) context.getSystemService("layout_inflater");
            if (inflator != null) {
                View view = inflator.inflate(R.layout.alert_dialog_content, null);
                TextView content = (TextView) view.findViewById(R.id.alert_dialog_content);
                content.setTextIsSelectable(true);
                content.setText(meidStr);
                builder.setView(view);
            } else {
                builder.setMessage(meidStr);
            }
            mAlertDialog = builder.show();
        }
    }
}
