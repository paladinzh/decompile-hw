package com.huawei.mms.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Jlog;
import android.util.Patterns;
import android.view.IWindowManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.ui.notification.DoActionActivity;
import com.android.common.contacts.DataUsageStatUpdater;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.Recycler;
import com.android.mms.widget.MmsWidgetProvider;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.places.Place;
import com.huawei.csp.util.MmsInfo;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor.Radar;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import com.huawei.mms.ui.HwMultiSimSendButton;
import com.huawei.tmr.util.TMRManagerProxy;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class HwMessageUtils {
    private static final byte[] CHINESE_BODA_BYTE_ARR = new byte[]{(byte) 98, (byte) -24, (byte) 98, (byte) 83};
    private static final byte[] CHINESE_GUANJI_BYTE_ARR = new byte[]{(byte) 81, (byte) 115, (byte) 103, (byte) 58};
    private static final byte[] CHINESE_HUJIAO_BYTE_ARR = new byte[]{(byte) 84, (byte) 124, (byte) 83, (byte) -21};
    private static final byte[] CHINESE_JIETONG_BYTE_ARR = new byte[]{(byte) 99, (byte) -91, (byte) -112, (byte) 26};
    private static final byte[] CHINESE_LAIDIAN_BYTE_ARR = new byte[]{(byte) 103, (byte) 101, (byte) 117, (byte) 53};
    private static final byte[] CHINESE_LOUJIE_BYTE_ARR = new byte[]{(byte) 111, (byte) 15, (byte) 99, (byte) -91};
    private static final byte[] CHINESE_TIXING_BYTE_ARR = new byte[]{(byte) 99, (byte) -48, (byte) -111, (byte) -110};
    private static final byte[] CHINESE_WEIJIE_BYTE_ARR = new byte[]{(byte) 103, (byte) 42, (byte) 99, (byte) -91};
    private static final boolean ENCRYPT_PROP = SystemProperties.getBoolean("ro.config.support_encrypt", false);
    public static final boolean IS_CHINA_REGION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    public static final boolean OPERATOR_SOFTBANK;
    private static final Uri RISK_URL_CHECK_URI = Uri.parse("content://com.huawei.systemmanager.BlockCheckProvider/checkurl/0");
    private static boolean isLessThanThreeMinutesAfterReboot = true;
    private static boolean isUsingOutgoingServiceCenter = false;
    public static final HwCustHwMessageUtils mCust = ((HwCustHwMessageUtils) HwCustUtils.createObj(HwCustHwMessageUtils.class, new Object[0]));
    private static double mDeviceSize = 0.0d;
    private static Boolean mIsEncryptCallEnabled = null;
    private static boolean sNetWorkAccessable = true;
    private static String sPreviousDialedNumber;
    private static int sSupportCBS = -1;

    public static boolean isGroupSmsId(android.content.Context r11, long r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0048 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = 1;
        r9 = 0;
        r0 = "(%s=%d)";
        r1 = 2;
        r1 = new java.lang.Object[r1];
        r2 = "group_id";
        r1[r9] = r2;
        r2 = java.lang.Long.valueOf(r12);
        r1[r8] = r2;
        r3 = java.lang.String.format(r0, r1);
        r6 = 0;
        r1 = android.provider.Telephony.Sms.CONTENT_URI;	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        r2 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        r4 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        r5 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        r0 = r11;	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        r6 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        if (r6 != 0) goto L_0x002a;
    L_0x0024:
        if (r6 == 0) goto L_0x0029;
    L_0x0026:
        r6.close();
    L_0x0029:
        return r9;
    L_0x002a:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        if (r0 <= r8) goto L_0x0037;
    L_0x0030:
        r0 = r8;
    L_0x0031:
        if (r6 == 0) goto L_0x0036;
    L_0x0033:
        r6.close();
    L_0x0036:
        return r0;
    L_0x0037:
        r0 = r9;
        goto L_0x0031;
    L_0x0039:
        r7 = move-exception;
        r0 = "HwMessageUtils";	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        r1 = "isGroupSmsId: SqliteWrapper.query failed";	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        com.huawei.cspcommon.MLog.e(r0, r1);	 Catch:{ Exception -> 0x0039, all -> 0x0049 }
        if (r6 == 0) goto L_0x0048;
    L_0x0045:
        r6.close();
    L_0x0048:
        return r9;
    L_0x0049:
        r0 = move-exception;
        if (r6 == 0) goto L_0x004f;
    L_0x004c:
        r6.close();
    L_0x004f:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.mms.util.HwMessageUtils.isGroupSmsId(android.content.Context, long):boolean");
    }

    static {
        boolean equals;
        if ("111".equals(SystemProperties.get("ro.config.hw_opta"))) {
            equals = "392".equals(SystemProperties.get("ro.config.hw_optb"));
        } else {
            equals = false;
        }
        OPERATOR_SOFTBANK = equals;
    }

    public static void noticeMediaChanged(Context context) {
        if (!((AudioManager) context.getSystemService("audio")).isMusicActive()) {
            String directory = SystemProperties.get("persist.sys.hw_external_path", "");
            if (TextUtils.isEmpty(directory)) {
                directory = Environment.getExternalStorageDirectory().toString();
            }
            noticeMediaChanged(context, directory);
        }
    }

    public static void noticeMediaChanged(Context context, String directory) {
        if (!TextUtils.isEmpty(directory)) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            intent.setData(Uri.fromFile(new File(directory)));
            context.sendBroadcast(intent);
        }
    }

    public static void copyToClipboard(Context context, String str) {
        ((ClipboardManager) context.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(null, str));
    }

    public static void launch(Context context) {
        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("geo:")));
    }

    public static void launchUrl(String url, Context context, boolean isPreview) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        intent.putExtra("com.android.browser.application_id", context.getPackageName());
        if (isPreview) {
            intent.putExtra("android.intent.action.START_PEEK_ACTIVITY", "startPeekActivity");
        }
        context.startActivity(intent);
    }

    public static void launchEvent(long[] time, String content, Context context) {
        Intent intent = new Intent("android.intent.action.EDIT");
        intent.setType("vnd.android.cursor.item/event");
        intent.setClassName("com.android.calendar", "com.android.calendar.EditEventActivity");
        intent.putExtra("title", content);
        intent.putExtra("beginTime", time[0]);
        intent.putExtra("founder_packagename", context.getPackageName());
        if (time.length > 1) {
            intent.putExtra("endTime", time[1]);
        }
        StatisticalHelper.incrementReportCount(context, 2066);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            MLog.e("HwMessageUtils", "Start calendar failed");
        }
    }

    public static void setBookMark(String url, View widget) {
        Context context = widget.getContext();
        Intent intent = new Intent("android.intent.action.INSERT", Browser.BOOKMARKS_URI);
        intent.putExtra("title", context.getResources().getString(R.string.book_mark_title));
        intent.putExtra(Constant.URLS, url);
        List<ResolveInfo> lists = context.getPackageManager().queryIntentActivities(intent, 32);
        if (lists == null || lists.size() <= 0) {
            addChromeBookMark(context, url);
        } else {
            context.startActivity(intent);
        }
    }

    public static void addChromeBookMark(Context context, String url) {
        Intent chromeIntent = new Intent("com.android.chrome.ADDBOOKMARK");
        chromeIntent.putExtra("title", context.getResources().getString(R.string.book_mark_title));
        chromeIntent.putExtra(Constant.URLS, url);
        chromeIntent.addCategory("android.intent.category.DEFAULT");
        context.startActivity(chromeIntent);
    }

    public static void saveNewContact(String url, Context context) {
        if (!TextUtils.isEmpty(url)) {
            Intent createIntent = createContactIntent(url, "android.intent.action.INSERT");
            if (createIntent != null) {
                createIntent.setData(Contacts.CONTENT_URI);
                if (!isSplitOn()) {
                    createIntent.putExtra("intent_key_is_from_dialpad", true);
                }
                safeStartActivity(context, createIntent);
            }
        }
    }

    public static void saveExistContact(String url, Context context) {
        if (!TextUtils.isEmpty(url)) {
            Intent createIntent = createContactIntent(url, "android.intent.action.INSERT_OR_EDIT");
            if (createIntent != null) {
                createIntent.setType("vnd.android.cursor.item/contact");
                createIntent.putExtra("handle_create_new_contact", false);
                if (!isSplitOn()) {
                    createIntent.putExtra("intent_key_is_from_dialpad", true);
                }
                safeStartActivity(context, createIntent);
            }
        }
    }

    private static Intent createContactIntent(String url, String actionInsert) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Intent createIntent = new Intent(actionInsert);
        if (url.startsWith("tel:")) {
            createIntent.putExtra("phone", url.substring("tel:".length(), url.length()));
        } else if (url.startsWith("mailto:")) {
            createIntent.putExtra(Scopes.EMAIL, url.substring("mailto:".length(), url.length()));
        } else if (url.startsWith("geo:0,0?q=")) {
            createIntent.putExtra("postal", url.substring("geo:0,0?q=".length(), url.length()));
        }
        return createIntent;
    }

    public static void messageContentSend(String url, Context context) {
        String address;
        if (url.startsWith("tel:")) {
            address = url.substring("tel:".length(), url.length());
        } else {
            address = url.substring("mailto:".length(), url.length());
        }
        Intent sendIntent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:" + address));
        sendIntent.setFlags(872415232);
        context.startActivity(sendIntent);
    }

    public static void viewContact(Uri contactUri, Context context) {
        Intent intent = new Intent("android.intent.action.VIEW", contactUri);
        intent.setFlags(524288);
        context.startActivity(intent);
    }

    public static String copyUrl(String prefix, String url, String bodyText) {
        String tempBody = bodyText.toLowerCase(Locale.getDefault());
        String urlLowerCase = url.toLowerCase(Locale.getDefault());
        if (!tempBody.contains(urlLowerCase)) {
            return url.substring(prefix.length(), url.length());
        }
        int index = tempBody.indexOf(urlLowerCase);
        return bodyText.substring(index, url.length() + index);
    }

    public static void toEditBeforeCall(String number, Context context) {
        if (TextUtils.isEmpty(number)) {
            MLog.w(HwCustUpdateUserBehaviorImpl.MMS, "number is null!");
            return;
        }
        context.startActivity(new Intent("android.intent.action.DIAL", Uri.fromParts("tel", number, null)));
    }

    public static void dialNumber(String numberUri, Activity context) {
        if (TextUtils.isEmpty(numberUri)) {
            MLog.w(HwCustUpdateUserBehaviorImpl.MMS, "context : " + context + "number is null");
            return;
        }
        hideKeyBoard(context);
        if (isInEncryptCall(context)) {
            MLog.d("HwMessageUtils", "call menus condition turns true, will show call menus ");
            showCallMenuInContacts(context, numberUri, false);
            return;
        }
        boolean enableCard1 = false;
        boolean enableCard2 = false;
        if (MessageUtils.isMultiSimEnabled()) {
            enableCard1 = 1 == MessageUtils.getIccCardStatus(0);
            enableCard2 = 1 == MessageUtils.getIccCardStatus(1);
        }
        int subscriptionId = 0;
        if (enableCard1 && enableCard2) {
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "dual sim is enabled");
            switch (getSimCardMode(context)) {
                case 0:
                    dialNumberBySubscription(context, numberUri, 0);
                    break;
                case 1:
                    dialNumberBySubscription(context, numberUri, 1);
                    break;
                default:
                    showDialDialog(context, numberUri);
                    break;
            }
            return;
        }
        if (enableCard1) {
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "dual sim is enabled");
            subscriptionId = 0;
        } else if (enableCard2) {
            subscriptionId = 1;
        }
        dialNumberBySubscription(context, numberUri, subscriptionId);
    }

    private static void showDialDialog(Activity activity, String numberUri) {
        showCallMenuInContacts(activity, numberUri, false);
    }

    public static void dialNumberBySubscription(Context context, String number, int subscriptionId) {
        Intent intent = new Intent("android.intent.action.CALL", Uri.parse(number));
        if (CommonGatherLinks.mHwCust != null) {
            intent = CommonGatherLinks.mHwCust.getIntentForUssdNumber(intent, number);
        }
        if (MessageUtils.isMultiSimEnabled()) {
            intent = MessageUtils.setSimIdToIntent(intent, subscriptionId);
        }
        context.startActivity(intent);
    }

    public static void dialNumberForSingleCard(Context context, String number) {
        context.startActivity(new Intent("android.intent.action.CALL", Uri.parse(number)));
    }

    public static void safeStartActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            MessageUtils.shwNoAppDialog(context);
            MLog.e("HwMessageUtils", "No Activity found to handle Intent");
        }
    }

    public static Intent createAddContactIntent(String address) {
        Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT");
        intent.setType("vnd.android.cursor.item/contact");
        if (Contact.isEmailAddress(address)) {
            intent.putExtra(Scopes.EMAIL, address);
        } else {
            intent.putExtra("phone", address);
            intent.putExtra("phone_type", 2);
        }
        intent.setFlags(524288);
        intent.putExtra("intent_key_is_from_dialpad", true);
        return intent;
    }

    public static void hideKeyBoard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService("input_method");
        if (inputMethodManager == null) {
            MLog.e("HwMessageUtils", "hideKeyboard can't get inputMethodManager.");
            return;
        }
        View v = ((Activity) context).getCurrentFocus();
        if (v == null || v.getWindowToken() == null) {
            MLog.e("HwMessageUtils", "Can't get hide KeyBoard as no focus view or no token." + v);
        } else {
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 2);
        }
    }

    public static void showKeyboard(Context context, View textView) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService("input_method");
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(textView, 2, new ResultReceiver(new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            }));
        }
    }

    public static boolean isCbsEnabled(Context context) {
        int i = 1;
        if (-1 != sSupportCBS) {
            boolean z;
            if (1 != sSupportCBS) {
                z = false;
            }
            return z;
        }
        boolean isCellBroadcastAppLinkEnabled = MmsConfig.isCBSEnabled().booleanValue();
        if (isCellBroadcastAppLinkEnabled) {
            try {
                if (context.getPackageManager().getApplicationEnabledSetting("com.android.cellbroadcastreceiver") == 2) {
                    isCellBroadcastAppLinkEnabled = false;
                }
            } catch (IllegalArgumentException e) {
                isCellBroadcastAppLinkEnabled = false;
            }
        }
        if (!isCellBroadcastAppLinkEnabled) {
            i = 0;
        }
        sSupportCBS = i;
        return isCellBroadcastAppLinkEnabled;
    }

    public static boolean needShowCallMenus(Context context) {
        if (context == null) {
            return false;
        }
        if (isInEncryptCall((Activity) context)) {
            return true;
        }
        if (!MessageUtils.isMultiSimEnabled()) {
            return false;
        }
        boolean z = (MessageUtils.isCardStatusValid(0) && MessageUtils.isCardStatusValid(1)) ? !isSimCardInSimpleMode(context) : false;
        return z;
    }

    public static void showCallMenuInContacts(Activity activity, String number, boolean previous) {
        if (previous) {
            number = sPreviousDialedNumber;
        } else {
            sPreviousDialedNumber = number;
        }
        String telPre = "tel:";
        if (!TextUtils.isEmpty(number)) {
            if (number.toLowerCase(Locale.getDefault()).startsWith(telPre)) {
                number = number.substring(telPre.length());
            }
            Intent chooseSubIntent = new Intent("com.android.contacts.action.CHOOSE_SUB_HUAWEI", Uri.fromParts("tel", number, null));
            chooseSubIntent.putExtra("fromMms", true);
            chooseSubIntent.putExtra("extra_show_edit_before_call", false);
            chooseSubIntent.addFlags(268435456);
            chooseSubIntent.addFlags(8388608);
            activity.startActivity(chooseSubIntent);
        }
    }

    public static void callNumberByMultiSim(final Activity acitivty, final String strNumberUri) {
        String TEL_PREFIX = "tel:";
        final String number = strNumberUri.replace("tel:", "");
        View view = acitivty.getLayoutInflater().inflate(R.layout.mms_alert_dialog_content, null);
        final HwMultiSimSendButton sim1call = (HwMultiSimSendButton) view.findViewById(R.id.sim1);
        final HwMultiSimSendButton sim2call = (HwMultiSimSendButton) view.findViewById(R.id.sim2);
        if (sim1call == null || sim2call == null) {
            MLog.e("HwMessageUtils", "callNumberByMultiSim get SendButton Fail");
            return;
        }
        Builder builder = new Builder(acitivty);
        builder.setView(view);
        builder.setTitle(acitivty.getString(R.string.menu_call_back, new Object[]{number}));
        final AlertDialog alertDialog = builder.create();
        OnClickListener clickListener = new OnClickListener() {
            public void onClick(View v) {
                HwMessageUtils.dialNumberBySubscription(acitivty, strNumberUri, v == sim1call ? 0 : 1);
                alertDialog.dismiss();
            }
        };
        alertDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface arg0) {
                if (acitivty != null && (acitivty instanceof DoActionActivity) && !acitivty.isFinishing()) {
                    acitivty.finish();
                }
            }
        });
        sim1call.setContentDescription(acitivty.getString(R.string.call_by_card1_hint));
        sim1call.setText(HwDualCardNameHelper.self().readCardName(0));
        sim1call.setLeftDrawables(R.drawable.mms_dial_call_1);
        sim1call.setOnClickListener(clickListener);
        sim2call.setContentDescription(acitivty.getString(R.string.call_by_card2_hint));
        sim2call.setText(HwDualCardNameHelper.self().readCardName(1));
        sim2call.setLeftDrawables(R.drawable.mms_dial_call_2);
        sim2call.setOnClickListener(clickListener);
        ThreadEx.execute(new Runnable() {
            public void run() {
                final int subId = HwMessageUtils.getDefaultCard(acitivty, number);
                Activity activity = acitivty;
                final HwMultiSimSendButton hwMultiSimSendButton = sim1call;
                final HwMultiSimSendButton hwMultiSimSendButton2 = sim2call;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        int i;
                        int i2 = 0;
                        HwMultiSimSendButton hwMultiSimSendButton = hwMultiSimSendButton;
                        if (subId == 0) {
                            i = 0;
                        } else {
                            i = 8;
                        }
                        hwMultiSimSendButton.setCurSimIndicatorVisibility(i);
                        HwMultiSimSendButton hwMultiSimSendButton2 = hwMultiSimSendButton2;
                        if (subId != 1) {
                            i2 = 8;
                        }
                        hwMultiSimSendButton2.setCurSimIndicatorVisibility(i2);
                    }
                });
            }
        });
        alertDialog.show();
    }

    public static void callNumberByMultiSimWithCheckMode(Activity acitivty, String strNumberUri) {
        switch (getSimCardMode(acitivty)) {
            case 0:
                dialNumberBySubscription(acitivty, strNumberUri, 0);
                return;
            case 1:
                dialNumberBySubscription(acitivty, strNumberUri, 1);
                return;
            default:
                callNumberByMultiSim(acitivty, strNumberUri);
                return;
        }
    }

    private static int getDefaultCard(Context context, String number) {
        String[] projection = new String[]{"subscription_id"};
        String selection = "number = ?";
        Cursor cursor = null;
        String formatNumber = MmsConfig.filteNumberByLocal(number);
        try {
            cursor = SqliteWrapper.query(context, Calls.CONTENT_URI, projection, selection, new String[]{formatNumber}, null);
            if (cursor == null || !cursor.moveToLast()) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        MLog.w("HwMessageUtils", "c.close() exception");
                    }
                }
                return -1;
            }
            int i = cursor.getInt(0);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e2) {
                    MLog.w("HwMessageUtils", "c.close() exception");
                }
            }
            return i;
        } catch (SQLiteException e3) {
            MLog.w("HwMessageUtils", "query operation SQLiteException exception");
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e4) {
                    MLog.w("HwMessageUtils", "c.close() exception");
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e5) {
                    MLog.w("HwMessageUtils", "c.close() exception");
                }
            }
        }
    }

    public static boolean isSuperPowerSaveModeOn() {
        boolean isSuperPowerSaveMode = SystemProperties.getBoolean("sys.super_power_save", false);
        MLog.d("HwMessageUtils", "Super power save mode: " + isSuperPowerSaveMode);
        return isSuperPowerSaveMode;
    }

    public static void setBtnLayoutParam(Context context, View view, int btnWidth, int btnHeight) {
        LayoutParams params = view.getLayoutParams();
        params.width = btnWidth;
        params.height = btnHeight;
        view.setLayoutParams(params);
        adjustNameViewWidth(context, view, btnWidth);
    }

    private static void adjustNameViewWidth(Context context, View sendBtnView, int width) {
        if (sendBtnView != null) {
            TextView textView = (TextView) sendBtnView.findViewById(R.id.button_text);
            if (textView != null) {
                int orientation = context.getResources().getConfiguration().orientation;
                if (orientation == 2) {
                    textView.setMaxWidth(context.getResources().getDimensionPixelSize(R.dimen.mms_sendbtntext_landscape_width));
                    textView.setSingleLine(false);
                    textView.setMaxLines(2);
                    textView.setEllipsize(TruncateAt.valueOf("END"));
                } else if (orientation == 1) {
                    textView.setMaxWidth(context.getResources().getDimensionPixelSize(R.dimen.mms_sendbtntext_portrait_width));
                    textView.setSingleLine(false);
                    textView.setMaxLines(2);
                    textView.setEllipsize(TruncateAt.valueOf("END"));
                }
            }
        }
    }

    public static boolean checkHarassmentService() {
        if (Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService")) == null || MmsConfig.isInSimpleUI() || !MmsConfig.isSupportHarassment()) {
            return false;
        }
        return true;
    }

    public static int getUnreadHarassmentSmsCount(Context context) {
        Bundle bundle = new Bundle();
        bundle.putInt("UNREAD_COUNT_SMS", 0);
        int count = 0;
        ContentProviderClient contentProviderClient = null;
        try {
            contentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/"));
            if (contentProviderClient == null) {
                MLog.e("HwMessageUtils", "getUnreadHarassmentSmsCount client return null");
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                return 0;
            }
            Bundle result = contentProviderClient.call("queryUnreadCount", null, bundle);
            if (result != null) {
                count = result.getInt("UNREAD_COUNT_SMS");
            }
            if (contentProviderClient != null) {
                contentProviderClient.release();
            }
            return count;
        } catch (Exception e) {
            MLog.e("HwMessageUtils", "getUnreadHarassmentSmsCount exception: " + e);
            if (contentProviderClient != null) {
                contentProviderClient.release();
            }
        } catch (Throwable th) {
            if (contentProviderClient != null) {
                contentProviderClient.release();
            }
        }
    }

    public static String getAddressPos(String text) {
        if (!MmsConfig.isSupportCNAddress()) {
            return "";
        }
        StringBuffer stringBuf = new StringBuffer();
        if (TextUtils.isEmpty(text) || text.length() < 1) {
            return "";
        }
        int[] iArr = null;
        try {
            iArr = TMRManagerProxy.getAddress(text);
        } catch (UnsatisfiedLinkError e) {
            MLog.e("Mms_app", "getAddr has an error >>> " + e);
        }
        if (iArr != null) {
            for (int value : iArr) {
                stringBuf.append(String.valueOf(value)).append(",");
            }
        }
        return stringBuf.toString();
    }

    public static int[] getAddrFromTMRManager(String text) {
        int[] msgRec = new int[1];
        if (!MmsConfig.isSupportCNAddress() || TextUtils.isEmpty(text) || text.length() < 1) {
            return msgRec;
        }
        try {
            msgRec = TMRManagerProxy.getAddress(text);
        } catch (UnsatisfiedLinkError e) {
            MLog.e("Mms_app", "getAddr has an error >>> " + e);
        }
        return msgRec;
    }

    public static final String convertStringCharset(String originalString, String sourceCharset, String targetCharset) {
        if (sourceCharset.equalsIgnoreCase(targetCharset)) {
            return originalString;
        }
        ByteBuffer byteBuffer = Charset.forName(sourceCharset).encode(originalString);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        try {
            return new String(bytes, targetCharset);
        } catch (UnsupportedEncodingException e) {
            MLog.e("HwMessageUtils", "Failed to encode: charset=" + targetCharset);
            return null;
        }
    }

    public static int getDefaultFollowNotificationState(Context context) {
        String messageRingtonePath = MmsConfig.getRingToneUriFromDatabase(context, "theme_message_path");
        String notificationPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_notification_sound_path");
        if (TextUtils.isEmpty(messageRingtonePath) || TextUtils.isEmpty(notificationPath)) {
            return 0;
        }
        if (messageRingtonePath.equalsIgnoreCase(notificationPath)) {
            return 1;
        }
        return 2;
    }

    public static boolean isDefaultMessageRingtone(Context context) {
        String messageRingtonePath = MmsConfig.getRingToneUriFromDatabase(context, "theme_message_path");
        String messageRingtoneUri = MmsConfig.getRingToneUriFromDatabase(context, 0);
        if (TextUtils.isEmpty(messageRingtonePath)) {
            return true;
        }
        if (messageRingtoneUri == null) {
            return false;
        }
        String ringtoneStr = MessagingNotification.getUriByPath(context, messageRingtonePath);
        return ringtoneStr != null && ringtoneStr.equalsIgnoreCase(messageRingtoneUri);
    }

    public static boolean isDefaultMessageRingtone(Context context, int subId) {
        String messageRingtonePath = MmsConfig.getRingToneUriFromDatabase(context, "theme_message_path");
        String messageRingtoneUri = MmsConfig.getRingToneUriFromDatabase(context, subId);
        if (TextUtils.isEmpty(messageRingtonePath) || messageRingtoneUri == null) {
            return true;
        }
        if ("null".equalsIgnoreCase(messageRingtoneUri)) {
            return false;
        }
        String ringtoneStr = MessagingNotification.getUriByPath(context, messageRingtonePath);
        return ringtoneStr != null && ringtoneStr.equalsIgnoreCase(messageRingtoneUri);
    }

    public static String getRingtoneString(Context context) {
        if (MessageUtils.isMultiSimEnabled()) {
            return getRingtoneString(context, 0);
        }
        boolean isFollowNotification;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultFollowNotificationState = 1;
        if (!sp.contains("pref_mms_is_follow_notification")) {
            defaultFollowNotificationState = getDefaultFollowNotificationState(context);
            if (defaultFollowNotificationState != 0) {
                boolean isDefaultFollowNotification = isDefaultFollowNotification(context, defaultFollowNotificationState, sp);
                Editor editor = sp.edit();
                editor.putBoolean("pref_mms_is_follow_notification", isDefaultFollowNotification);
                editor.commit();
            }
        }
        if (defaultFollowNotificationState == 0) {
            isFollowNotification = true;
        } else {
            isFollowNotification = sp.getBoolean("pref_mms_is_follow_notification", false);
        }
        String ringtoneStr;
        String uriPath;
        if (isFollowNotification) {
            ringtoneStr = MmsConfig.getRingToneUriFromDatabase(context, "notification_sound");
            if (TextUtils.isEmpty(ringtoneStr)) {
                return null;
            }
            if (!MessagingNotification.isUriAvalible(context, ringtoneStr)) {
                uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_notification_sound_path");
                if (uriPath != null) {
                    ringtoneStr = MessagingNotification.getUriByPath(context, uriPath);
                }
                if (!(ringtoneStr == null || MessagingNotification.isUriAvalible(context, ringtoneStr))) {
                    Uri ringtoneUri = RingtoneManager.getDefaultUri(2);
                    ringtoneStr = ringtoneUri == null ? null : ringtoneUri.toString();
                }
            }
            return ringtoneStr;
        }
        ringtoneStr = MmsConfig.getRingToneUriFromDatabase(context, 0);
        if (TextUtils.isEmpty(ringtoneStr)) {
            return null;
        }
        if (!MessagingNotification.isUriAvalible(context, ringtoneStr)) {
            if (getDefaultFollowNotificationState(context) == 2) {
                uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_message_path");
                if (uriPath != null) {
                    ringtoneStr = MessagingNotification.getUriByPath(context, uriPath);
                }
                if (TextUtils.isEmpty(ringtoneStr) || MessagingNotification.isUriAvalible(context, ringtoneStr)) {
                    return ringtoneStr;
                }
            }
            ringtoneStr = MmsConfig.getRingToneUriFromDatabase(context, "notification_sound");
            if (TextUtils.isEmpty(ringtoneStr)) {
                return null;
            }
            if (!MessagingNotification.isUriAvalible(context, ringtoneStr)) {
                uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_notification_sound_path");
                if (uriPath != null) {
                    ringtoneStr = MessagingNotification.getUriByPath(context, uriPath);
                }
                if (!(TextUtils.isEmpty(ringtoneStr) || MessagingNotification.isUriAvalible(context, ringtoneStr))) {
                    Uri uri = RingtoneManager.getDefaultUri(2);
                    ringtoneStr = uri == null ? null : uri.toString();
                }
            }
        }
        return ringtoneStr;
    }

    public static String getRingtoneString(Context context, int subId) {
        boolean isFollowNotification;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultFollowNotificationState = 1;
        String followNotificationKey = "pref_mms_is_follow_notification_sub0";
        if (subId == 1) {
            followNotificationKey = "pref_mms_is_follow_notification_sub1";
        }
        if (!sp.contains(followNotificationKey)) {
            defaultFollowNotificationState = getDefaultFollowNotificationState(context);
            if (defaultFollowNotificationState != 0) {
                boolean isDefaultFollowNotification = isDefaultFollowNotification(context, defaultFollowNotificationState, sp);
                Editor editor = sp.edit();
                editor.putBoolean(followNotificationKey, isDefaultFollowNotification);
                editor.commit();
            }
        }
        if (defaultFollowNotificationState == 0) {
            isFollowNotification = true;
        } else {
            isFollowNotification = sp.getBoolean(followNotificationKey, false);
        }
        String ringtoneStr;
        String uriPath;
        if (isFollowNotification) {
            ringtoneStr = MmsConfig.getRingToneUriFromDatabase(context, "notification_sound");
            if (TextUtils.isEmpty(ringtoneStr)) {
                return null;
            }
            if (!MessagingNotification.isUriAvalible(context, ringtoneStr)) {
                uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_notification_sound_path");
                if (uriPath != null) {
                    ringtoneStr = MessagingNotification.getUriByPath(context, uriPath);
                }
                if (!(ringtoneStr == null || MessagingNotification.isUriAvalible(context, ringtoneStr))) {
                    Uri ringtoneUri = RingtoneManager.getDefaultUri(2);
                    ringtoneStr = ringtoneUri == null ? null : ringtoneUri.toString();
                }
            }
            return ringtoneStr;
        }
        ringtoneStr = MmsConfig.getRingToneUriFromDatabase(context, subId);
        if (TextUtils.isEmpty(ringtoneStr) || "null".equals(ringtoneStr)) {
            return null;
        }
        if (!MessagingNotification.isUriAvalible(context, ringtoneStr)) {
            if (getDefaultFollowNotificationState(context) == 2) {
                uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_message_path");
                if (uriPath != null) {
                    ringtoneStr = MessagingNotification.getUriByPath(context, uriPath);
                }
                if (TextUtils.isEmpty(ringtoneStr) || MessagingNotification.isUriAvalible(context, ringtoneStr)) {
                    return ringtoneStr;
                }
            }
            ringtoneStr = MmsConfig.getRingToneUriFromDatabase(context, "notification_sound");
            if (TextUtils.isEmpty(ringtoneStr)) {
                return null;
            }
            if (!MessagingNotification.isUriAvalible(context, ringtoneStr)) {
                uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_notification_sound_path");
                if (uriPath != null) {
                    ringtoneStr = MessagingNotification.getUriByPath(context, uriPath);
                }
                if (!(TextUtils.isEmpty(ringtoneStr) || MessagingNotification.isUriAvalible(context, ringtoneStr))) {
                    Uri uri = RingtoneManager.getDefaultUri(2);
                    ringtoneStr = uri == null ? null : uri.toString();
                }
            }
        }
        return ringtoneStr;
    }

    private static boolean isDefaultFollowNotification(Context context, int defaultFollowNotificationState, SharedPreferences sp) {
        boolean isDefaultFollowNotification = false;
        if (defaultFollowNotificationState == 1) {
            isDefaultFollowNotification = true;
        }
        if (!isDefaultMessageRingtone(context)) {
            isDefaultFollowNotification = false;
        }
        if (sp.contains("pref_key_ringtone")) {
            return false;
        }
        return isDefaultFollowNotification;
    }

    public static String getSystemDefaultRingTone(Context context) {
        String ringtoneStr = null;
        String uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_notification_sound_path");
        if (uriPath != null) {
            ringtoneStr = MessagingNotification.getUriByPath(context, uriPath);
        }
        if (ringtoneStr == null || MessagingNotification.isUriAvalible(context, ringtoneStr)) {
            return ringtoneStr;
        }
        Uri ringtoneUri = RingtoneManager.getDefaultUri(2);
        return ringtoneUri == null ? null : ringtoneUri.toString();
    }

    public static String getTimePosString(String text) {
        if (!MmsConfig.isSupportTimeParse()) {
            return "";
        }
        StringBuffer stringBuf = new StringBuffer();
        if (TextUtils.isEmpty(text) || text.length() < 1) {
            return "";
        }
        int[] iArr = null;
        try {
            iArr = TMRManagerProxy.getTime(text);
        } catch (Exception e) {
            MLog.e("Mms_app", "getTime has an error >>> " + e);
        } catch (NoSuchMethodError e2) {
            e2.printStackTrace();
        }
        if (iArr == null) {
            return "";
        }
        for (int value : iArr) {
            stringBuf.append(String.valueOf(value)).append(",");
        }
        return stringBuf.toString();
    }

    public static int[] getTimePosition(String text) {
        int[] msgRec = null;
        if (!MmsConfig.isSupportTimeParse() || TextUtils.isEmpty(text) || text.length() < 1) {
            return msgRec;
        }
        try {
            msgRec = TMRManagerProxy.getTime(text);
        } catch (Exception e) {
            MLog.e("Mms_app", "getTime has an error >>> " + e);
        } catch (NoSuchMethodError e2) {
            e2.printStackTrace();
        }
        return msgRec;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void checkRiskUrlAndWriteToDB(Context context, Uri uri, String sourceText, String address) {
        if (!(!getRiskUrlEnable(context) || TextUtils.isEmpty(sourceText) || uri == null || context == null)) {
            Intent intent = new Intent(context, RiskUrlCheckService.class);
            intent.putExtra("msg_address", address);
            intent.putExtra("msg_body", sourceText);
            intent.putExtra("msg_uri", uri.toString());
            context.startService(intent);
        }
    }

    public static String getRiskUrlPosString(Context context, String sourceText) {
        if (!getRiskUrlEnable(context) || TextUtils.isEmpty(sourceText) || !isNetWorkAccessable(context)) {
            return "";
        }
        return checkWarningUrl(context, new String[]{sourceText});
    }

    public static String getUnOfficialUrlPosString(Context context, String address, String sourceText) {
        if (!getRiskUrlEnable(context) || TextUtils.isEmpty(sourceText) || TextUtils.isEmpty(address) || !Contact.IS_CHINA_REGION) {
            return "";
        }
        return checkWarningUrl(context, new String[]{address, sourceText});
    }

    public static String checkWarningUrl(Context context, String[] pamas) {
        if (pamas == null || pamas.length == 0) {
            return "";
        }
        String sourceText;
        StringBuffer stringunOfficialBuf = new StringBuffer();
        StringBuffer stringBuf = new StringBuffer();
        boolean checkForRisk = true;
        if (pamas.length == 1) {
            sourceText = pamas[0];
        } else {
            checkForRisk = false;
            sourceText = pamas[1];
        }
        Matcher m = Patterns.AUTOLINK_WEB_URL_EMUI.matcher(sourceText);
        int unofficialUrlCount = 0;
        int riskUrlCount = 0;
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (CommonGatherLinks.sUrlMatchFilter.acceptMatch(sourceText, start, end)) {
                Cursor cursor;
                int checkResult = -2;
                String url = CommonGatherLinks.makeUrl(m.group(0), CommonGatherLinks.BROWER_SCHEMES, m);
                if (checkForRisk) {
                    cursor = SqliteWrapper.query(context, context.getContentResolver(), RISK_URL_CHECK_URI, new String[]{url}, null, null, null);
                } else {
                    cursor = SqliteWrapper.query(context, context.getContentResolver(), RISK_URL_CHECK_URI, new String[]{url, pamas[0]}, null, null, null);
                }
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            checkResult = cursor.getInt(cursor.getColumnIndex("result"));
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
                if (checkResult == 1) {
                    StatisticalHelper.incrementReportCount(context, 2160);
                    StatisticalHelper.reportEvent(context, 2163, url);
                    MLog.i("HwMessageUtils", "found risk url in message body");
                    riskUrlCount++;
                    stringBuf.append(String.valueOf(start)).append(",").append(String.valueOf(end - 1)).append(",");
                } else if (checkResult == 0) {
                    continue;
                } else if (checkResult == -1) {
                    StatisticalHelper.incrementReportCount(context, 2210);
                    StatisticalHelper.reportEvent(context, 2213, pamas[0] + "  " + url);
                    MLog.i("HwMessageUtils", "found unofficial url in message body");
                    unofficialUrlCount++;
                    stringunOfficialBuf.append(String.valueOf(start)).append(",").append(String.valueOf(end - 1)).append(",");
                } else {
                    MLog.i("HwMessageUtils", "failed to check risk url, maybe caused by network error");
                    setNetWorkState(false);
                    return "";
                }
            }
        }
        String result;
        if (riskUrlCount != 0) {
            result = String.valueOf(-6) + "," + String.valueOf(riskUrlCount) + "," + stringBuf.toString();
            MLog.i("HwMessageUtils", "getRiskUrlPos result: <" + result + ">");
            return result;
        } else if (unofficialUrlCount == 0) {
            return "0,";
        } else {
            StatisticalHelper.reportEvent(context, 2214, unofficialUrlCount + "");
            result = String.valueOf(-7) + "," + String.valueOf(unofficialUrlCount) + "," + stringunOfficialBuf.toString();
            MLog.i("HwMessageUtils", "getUnOfficialUrlPos result: <" + result + ">");
            return result;
        }
    }

    public static void setNetWorkState(boolean accessable) {
        sNetWorkAccessable = accessable;
    }

    private static boolean isNetWorkAccessable(Context context) {
        if (context == null || !sNetWorkAccessable) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        if (networkInfo != null && networkInfo.length > 0) {
            for (NetworkInfo state : networkInfo) {
                if (state.getState() == State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsSpannableStringBuilder(CharSequence str, CharSequence sub) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(sub)) {
            return false;
        }
        boolean ressult;
        if (str.toString().indexOf(sub.toString(), 0) >= 0) {
            ressult = true;
        } else {
            ressult = false;
        }
        return ressult;
    }

    public static int[] spanStringToPosition(String content) {
        int[] positionResult = null;
        if (!TextUtils.isEmpty(content)) {
            String[] posSubString = content.split(",");
            positionResult = new int[posSubString.length];
            int index = 0;
            try {
                for (String pos : posSubString) {
                    if (!TextUtils.isEmpty(pos)) {
                        positionResult[index] = Integer.parseInt(pos);
                        index++;
                    }
                }
            } catch (NumberFormatException e) {
                MLog.e("HwMessageUtils", "Number Format Exception when get Position");
            }
        }
        return positionResult;
    }

    public static String formatSqlString(String selection) {
        if (TextUtils.isEmpty(selection)) {
            return "";
        }
        StringBuilder ret = new StringBuilder();
        int len = selection.length();
        for (int i = 0; i < len; i++) {
            char ch = selection.charAt(i);
            switch (ch) {
                case '%':
                case Place.TYPE_TRAIN_STATION /*92*/:
                case Place.TYPE_VETERINARY_CARE /*95*/:
                    ret.append('\\');
                    break;
                default:
                    break;
            }
            ret.append(ch);
        }
        return ret.toString();
    }

    public static String formatNumberString(String number) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        StringBuilder ret = new StringBuilder();
        ret.append('‪').append(number).append('‬');
        return ret.toString();
    }

    public static String formatRegexString(String pattern) {
        StringBuilder ret = new StringBuilder();
        int len = pattern.length();
        for (int i = 0; i < len; i++) {
            char ch = pattern.charAt(i);
            switch (ch) {
                case '$':
                case '%':
                case '&':
                case '\'':
                case '(':
                case ')':
                case '*':
                case '+':
                case Place.TYPE_HARDWARE_STORE /*46*/:
                case Place.TYPE_HEALTH /*47*/:
                case Place.TYPE_TAXI_STAND /*91*/:
                case Place.TYPE_TRAIN_STATION /*92*/:
                case Place.TYPE_TRAVEL_AGENCY /*93*/:
                case '|':
                    ret.append('\\');
                    break;
                default:
                    break;
            }
            ret.append(ch);
        }
        return ret.toString();
    }

    public static String replaceNumberFromDatabase(String originNumber, Context context) {
        if (TextUtils.isEmpty(originNumber) || Contact.isEmailAddress(originNumber)) {
            return originNumber;
        }
        String minMatch = PhoneNumberUtils.toCallerIDMinMatch(PhoneNumberUtils.normalizeNumber(originNumber));
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, Contact.getPhoneWithUri(), new String[]{"data1"}, Contact.getCallerIdSelection(), new String[]{minMatch}, null);
            if (cursor == null || cursor.getCount() < 1) {
                if (cursor != null) {
                    cursor.close();
                }
                return originNumber;
            }
            Cursor matchedCursor = HwNumberMatchUtils.getMatchedCursor(cursor, originNumber);
            if (matchedCursor != null) {
                String string = matchedCursor.getString(cursor.getColumnIndexOrThrow("data1"));
                if (cursor != null) {
                    cursor.close();
                }
                return string;
            }
            if (cursor != null) {
                cursor.close();
            }
            return originNumber;
        } catch (Exception e) {
            MLog.e("HwMessageUtils", "replaceNumbersFromDatabases occur exception when query contact!" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean displayMmsSentTime(long localTime, long networkTime, int subId) {
        if ((localTime < networkTime + Constant.FIVE_MINUTES && localTime > networkTime - Constant.FIVE_MINUTES) || mCust.isDeviceTimeForRecievingMms()) {
            return false;
        }
        boolean isRoaming;
        if (!MessageUtils.isMultiSimEnabled() || subId == -1) {
            isRoaming = MessageUtils.isNetworkRoaming();
        } else {
            isRoaming = MessageUtils.isNetworkRoaming(subId);
        }
        if (isRoaming) {
            return false;
        }
        MLog.d("HwMessageUtils", "display sent time..");
        return true;
    }

    public static boolean isInKidsmodes(Context context) {
        List<RunningTaskInfo> list = null;
        String KIDSMODE_STATE_KEY = "hwkidsmode_running";
        String KIDSMODE_PKG_NAME = "com.huawei.kidsmode";
        if (Global.getInt(context.getContentResolver(), "hwkidsmode_running", 0) == 0) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        if (am != null) {
            list = am.getRunningTasks(30);
        }
        if (list == null) {
            return false;
        }
        for (RunningTaskInfo info : list) {
            if (!info.topActivity.getPackageName().equals("com.huawei.kidsmode")) {
                if (info.baseActivity.getPackageName().equals("com.huawei.kidsmode")) {
                }
            }
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "Mms check current is in kidsmode.");
            return true;
        }
        return false;
    }

    public static void displaySoftInput(Context context, View view) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService("input_method");
        if (inputManager != null) {
            inputManager.showSoftInput(view, 1);
        }
    }

    public static void updateRecentContactsToDB(Context context, ArrayList<String> numbers) {
        try {
            new DataUsageStatUpdater(context).updateWithPhoneNumber(numbers);
        } catch (SQLiteException e) {
            MLog.e("HwMessageUtils", "updateRecentContactsToDB occur exception: " + e);
        } catch (SecurityException e2) {
            MLog.e("HwMessageUtils", "updateRecentContactsToDB occur SecurityException: " + e2);
        }
    }

    public static void disableFrameRandar(String functionName) {
        Jlog.d(82, MmsInfo.getSmsAppName(MmsApp.getApplication().getApplicationContext()), functionName);
    }

    public static void enableFrameRandar(String functionName) {
        Jlog.d(81, MmsInfo.getSmsAppName(MmsApp.getApplication().getApplicationContext()), functionName);
    }

    public static void showJlogByID(int logId, int spareTime, String msg) {
        Jlog.d(logId, spareTime, msg);
    }

    public static boolean isInfoMsg(Context context, Uri msgUri) {
        if (Conversation.get(context, MessagingNotification.getSmsThreadId(context, msgUri), true).getNumberType() != 0) {
            return true;
        }
        return false;
    }

    public static void setIsUsingOutgoingServiceCenter(boolean value) {
        isUsingOutgoingServiceCenter = value;
    }

    public static boolean getIsUsingOutgoingServiceCenter() {
        return isUsingOutgoingServiceCenter;
    }

    public static String getImsiFromCard(Context context, int sub) {
        String imsi = "";
        if (context == null) {
            MLog.e("HwMessageUtils", "get imsi from card context null, return");
            return "";
        }
        TelephonyManager tm = TelephonyManager.from(context);
        if (tm == null) {
            MLog.e("HwMessageUtils", "get imsi from card tm null, return");
            return "";
        }
        try {
            if (MessageUtils.isMultiSimEnabled()) {
                imsi = tm.getSubscriberId(sub);
            } else {
                imsi = tm.getSubscriberId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imsi != null) {
            MLog.d("HwMessageUtils", "get the imsi len :" + imsi.length());
        }
        return imsi;
    }

    public static boolean isInRoaming() {
        boolean isRoaming;
        if (!MessageUtils.isMultiSimEnabled()) {
            isRoaming = MessageUtils.isNetworkRoaming();
        } else if (MessageUtils.isNetworkRoaming(0)) {
            isRoaming = true;
        } else {
            isRoaming = MessageUtils.isNetworkRoaming(1);
        }
        MLog.d("HwMessageUtils", "is in roaming:" + isRoaming);
        return isRoaming;
    }

    public static void triggleCallMissingChr(Context context, int sub, String number, String smsContent, int judgeType) {
        if (isLessThanThreeMinutesAfterReboot) {
            if (SystemClock.elapsedRealtime() < 180000) {
                MLog.d("HwMessageUtils", "less than 3 minute, ignore");
                return;
            }
            isLessThanThreeMinutesAfterReboot = false;
        }
        final Context context2 = context;
        final String str = number;
        final String str2 = smsContent;
        final int i = judgeType;
        final int i2 = sub;
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                String chineseJieTong;
                String chineseGuanJi;
                String chineseHuJiao;
                String chineseBoDa;
                int ret = SmartSmsSdkUtil.parseSmsType(context2, str, str2, null, null, i);
                MLog.d("HwMessageUtils", "parseSmsType return:" + ret);
                if (2 == ret) {
                    MLog.e("HwMessageUtils", "find one call missing");
                    Radar.reportChr(i2, 1316, "find one call missing");
                } else if (str2 != null && str2.length() <= 140) {
                    String chineseLaiDian = "";
                    String chineseWeiJie = "";
                    String chineseJieTong2 = "";
                    String chineseLouJie = "";
                    String chineseGuanJi2 = "";
                    String chineseHuJiao2 = "";
                    String chineseBoDa2 = "";
                    String chineseTiXing = "";
                    try {
                        String str = new String(HwMessageUtils.CHINESE_LAIDIAN_BYTE_ARR, "utf-16be");
                        try {
                            str = new String(HwMessageUtils.CHINESE_WEIJIE_BYTE_ARR, "utf-16be");
                        } catch (UnsupportedEncodingException e) {
                            chineseLaiDian = str;
                            MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                        }
                        try {
                            chineseJieTong = new String(HwMessageUtils.CHINESE_JIETONG_BYTE_ARR, "utf-16be");
                            try {
                                str = new String(HwMessageUtils.CHINESE_LOUJIE_BYTE_ARR, "utf-16be");
                            } catch (UnsupportedEncodingException e2) {
                                chineseJieTong2 = chineseJieTong;
                                chineseWeiJie = str;
                                chineseLaiDian = str;
                                MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                            }
                            try {
                                chineseGuanJi = new String(HwMessageUtils.CHINESE_GUANJI_BYTE_ARR, "utf-16be");
                            } catch (UnsupportedEncodingException e3) {
                                chineseLouJie = str;
                                chineseJieTong2 = chineseJieTong;
                                chineseWeiJie = str;
                                chineseLaiDian = str;
                                MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                            }
                            try {
                                chineseHuJiao = new String(HwMessageUtils.CHINESE_HUJIAO_BYTE_ARR, "utf-16be");
                            } catch (UnsupportedEncodingException e4) {
                                chineseGuanJi2 = chineseGuanJi;
                                chineseLouJie = str;
                                chineseJieTong2 = chineseJieTong;
                                chineseWeiJie = str;
                                chineseLaiDian = str;
                                MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                            }
                            try {
                                chineseBoDa = new String(HwMessageUtils.CHINESE_BODA_BYTE_ARR, "utf-16be");
                            } catch (UnsupportedEncodingException e5) {
                                chineseHuJiao2 = chineseHuJiao;
                                chineseGuanJi2 = chineseGuanJi;
                                chineseLouJie = str;
                                chineseJieTong2 = chineseJieTong;
                                chineseWeiJie = str;
                                chineseLaiDian = str;
                                MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                            }
                        } catch (UnsupportedEncodingException e6) {
                            chineseWeiJie = str;
                            chineseLaiDian = str;
                            MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                        }
                        try {
                            str = new String(HwMessageUtils.CHINESE_TIXING_BYTE_ARR, "utf-16be");
                            if ((str2.contains(str) || str2.contains(str) || str2.contains(chineseJieTong) || str2.contains(str) || str2.contains(chineseGuanJi)) && (str2.contains(chineseHuJiao) || str2.contains(chineseBoDa) || str2.contains(str))) {
                                MLog.e("HwMessageUtils", "find one call missing_");
                                Radar.reportChr(i2, 1316, "find one call missing_");
                            }
                        } catch (UnsupportedEncodingException e7) {
                            chineseBoDa2 = chineseBoDa;
                            chineseHuJiao2 = chineseHuJiao;
                            chineseGuanJi2 = chineseGuanJi;
                            chineseLouJie = str;
                            chineseJieTong2 = chineseJieTong;
                            chineseWeiJie = str;
                            chineseLaiDian = str;
                            MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                        }
                    } catch (UnsupportedEncodingException e8) {
                        MLog.e("HwMessageUtils", "parseSmsType get callMissingMessageProfix error");
                    }
                }
            }
        });
    }

    public static int querySubscription(Context context, Uri uri) {
        if (!MessageUtils.isMultiSimEnabled()) {
            return 0;
        }
        int ddsSub = MessageUtils.getPreferredDataSubscription();
        int i = context;
        Cursor cursor = SqliteWrapper.query(i, context.getContentResolver(), uri, new String[]{"sub_id"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    i = cursor.getInt(cursor.getColumnIndexOrThrow("sub_id"));
                    return i;
                }
                cursor.close();
            } catch (Exception e) {
                return ddsSub;
            } finally {
                cursor.close();
            }
        }
        return ddsSub;
    }

    public static void sendSms(Context context, long threadId, String msgbody, int subscription) {
        final Context context2 = context;
        final long j = threadId;
        final String str = msgbody;
        final int i = subscription;
        ThreadEx.execute(new Runnable() {
            public void run() {
                String[] dests = Conversation.get(context2, j, true).getRecipients().getNumbers();
                HwMessageUtils.sendSmsWorker(context2, j, dests, str, i == -1 ? MessageUtils.getPreferredSmsSubscription() : i);
                ArrayList<String> formatNumbers = new ArrayList();
                for (String number : dests) {
                    formatNumbers.add(HwMessageUtils.replaceNumberFromDatabase(number, context2));
                }
                try {
                    new DataUsageStatUpdater(context2).updateWithPhoneNumber(formatNumbers);
                } catch (SQLiteException e) {
                    MLog.e("HwMessageUtils", "too many SQL variables");
                }
            }
        });
        ResEx.makeToast((int) R.string.sending_message, 0);
    }

    private static void sendSmsWorker(Context context, long threadId, String[] dests, String msgText, int subscription) {
        for (int i = 0; i < dests.length; i++) {
            String afterParseContact = MessageUtils.parseMmsAddress(dests[i]);
            if (!TextUtils.isEmpty(afterParseContact)) {
                dests[i] = afterParseContact;
            }
        }
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.d("Mms_TXN", "sendSmsWorker sending message with recipients, threadId=" + threadId);
        }
        if (MccMncConfig.is7bitEnable()) {
            CharSequence snew = MessageUtils.replaceAlphabetFor7Bit(msgText.subSequence(0, msgText.length()), 0, msgText.length());
            if (snew != null) {
                msgText = snew.toString();
            }
        }
        try {
            new SmsMessageSender(context, dests, msgText, threadId, subscription).sendMessage(threadId);
            Recycler.getSmsRecycler().deleteOldMessagesByThreadId(context, threadId);
        } catch (Exception e) {
            MLog.e("HwMessageUtils", "Failed to send SMS message, threadId=" + threadId, (Throwable) e);
        }
        MmsWidgetProvider.notifyDatasetChanged(context);
    }

    public static boolean saveDraft(Context context, long threadId, String msgbody, int subscription) {
        if (threadId <= 0) {
            return false;
        }
        final Context context2 = context;
        final long j = threadId;
        final String str = msgbody;
        final int i = subscription;
        ThreadEx.execute(new Runnable() {
            public void run() {
                Conversation conv = Conversation.get(context2, j, true);
                ContentValues values = new ContentValues(4);
                values.put("thread_id", Long.valueOf(j));
                values.put("body", str);
                values.put(NumberInfo.TYPE_KEY, Integer.valueOf(3));
                values.put("sub_id", Integer.valueOf(i));
                MLog.d("HwMessageUtils", "saveDraft for the receiver and msgbody.");
                SqliteWrapper.insert(context2, context2.getContentResolver(), Sms.CONTENT_URI, values);
                conv.setDraftState(true);
                conv.setHasTempDraft(true);
            }
        });
        return true;
    }

    public static int getSimCardMode(Context context) {
        return Global.getInt(context.getContentResolver(), "default_simcard_slotid", -1);
    }

    public static int getMultiSimState() {
        int card1Statet = MessageUtils.getIccCardStatus(0);
        int card2Statet = MessageUtils.getIccCardStatus(1);
        if (MessageUtils.isMultiSimEnabled()) {
            if (1 == card1Statet && 1 == card2Statet) {
                return 0;
            }
            if (1 == card1Statet && (1 != MessageUtils.getDsdsMode() || 2 != MessageUtils.getCurrentPhoneType(0))) {
                return 1;
            }
            if (1 != card2Statet || 2 == MessageUtils.getCurrentPhoneType(1)) {
                return 2;
            }
            return 1;
        } else if (MmsApp.getDefaultTelephonyManager().hasIccCard()) {
            return 1;
        } else {
            return 2;
        }
    }

    public static boolean isSimCardInSimpleMode(Context context) {
        if (!MessageUtils.isMultiSimEnabled()) {
            return false;
        }
        switch (Global.getInt(context.getContentResolver(), "default_simcard_slotid", -1)) {
            case 0:
                return true;
            case 1:
                return true;
            default:
                return false;
        }
    }

    public static String convertDateToVersion(long date) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(date);
        int year = mCalendar.get(1);
        if (year > 2015) {
            year -= 2015;
        } else {
            year = 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append((year / 10) + 1).append('.').append(year % 10).append('.').append(mCalendar.get(2) + 1).append('.').append(mCalendar.get(5));
        return sb.toString();
    }

    public static boolean isPkgInstalled(Context context, String pkgName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
        }
        if (packageInfo == null) {
            return false;
        }
        return true;
    }

    public static int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static boolean isSplitOn() {
        return mDeviceSize >= 8.0d;
    }

    public static double calculateDeviceSize(Context context) {
        if (mDeviceSize > 0.0d) {
            return mDeviceSize;
        }
        IWindowManager iwm = IWindowManager.Stub.asInterface(ServiceManager.checkService("window"));
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(dm);
        if (iwm != null) {
            Point point = new Point();
            try {
                iwm.getInitialDisplaySize(0, point);
                mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / dm.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
                return mDeviceSize;
            } catch (RemoteException e) {
            }
        }
        mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return mDeviceSize;
    }

    public static boolean getRiskUrlEnable(Context context) {
        if (context == null) {
            return false;
        }
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_risk_url_check", false);
    }

    public static int getSplitActionBarHeight(Context context) {
        TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[]{16843499});
        int dimension = (int) actionbarSizeTypedArray.getDimension(0, 0.0f);
        actionbarSizeTypedArray.recycle();
        return dimension;
    }

    public static boolean isInEncryptCall(Activity context) {
        boolean z = true;
        if (!ENCRYPT_PROP) {
            return false;
        }
        if (context == null || context.getContentResolver() == null) {
            MLog.i("HwMessageUtils", "isInEncryptCall getContentResolver fail !");
            return false;
        }
        if (mIsEncryptCallEnabled == null) {
            int encryptCallStatus = Secure.getInt(context.getContentResolver(), "encrypt_version", 0);
            MLog.i("HwMessageUtils", "isInEncryptCall encryptCallStatus = " + encryptCallStatus);
            if (1 != encryptCallStatus) {
                z = false;
            }
            mIsEncryptCallEnabled = Boolean.valueOf(z);
        }
        return mIsEncryptCallEnabled.booleanValue();
    }
}
