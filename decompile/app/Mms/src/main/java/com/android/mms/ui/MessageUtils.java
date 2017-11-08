package com.android.mms.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimSmsManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.amap.api.services.core.AMapException;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.ISms.Stub;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.TempFileProvider;
import com.android.mms.activities.CopyTextFragment;
import com.android.mms.activities.MmsAllActivity;
import com.android.mms.attachment.ui.mediapicker.MediaPicker;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.model.CarrierContentRestriction;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.transaction.MmsMessageSender.ReadRecContent;
import com.android.mms.ui.twopane.RightPaneComposeMessageFragment;
import com.android.mms.util.AddressUtils;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.rcs.ui.RcsGroupChatMessageItem;
import com.android.rcs.ui.RcsMessageUtils;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.RetrieveConf;
import com.google.android.mms.pdu.SendReq;
import com.huawei.android.provider.TelephonyEx.Sms.Sent;
import com.huawei.android.telephony.MSimSmsManagerEx;
import com.huawei.android.telephony.SmsManagerEx;
import com.huawei.android.telephony.SmsMessageEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MultiModeListView;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwCustUpdateUserBehaviorImpl;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwSpecialUtils;
import com.huawei.mms.util.HwSpecialUtils.HwDateUtils;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint({"NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi"})
public class MessageUtils {
    static final HashMap<ComponentName, String> ALL_RELATED_CONTACTS_INFO = new HashMap();
    static final HashMap<Integer, ComponentName> ALL_RELATED_CONTACTS_INFO_BY_INDEX = new HashMap();
    public static final Uri CONTENT_URI = Uri.parse("content://com.huawei.numberlocation/numberlocation");
    public static final Uri CONTENT_URI_WITH_UID = Uri.parse("content://sms/group_id");
    public static final String DSDS_MODE_PROP = SystemProperties.get("ro.config.dsds_mode", "");
    private static int FRENCH_SPECIAL_CHARACTER_1 = 338;
    private static int FRENCH_SPECIAL_CHARACTER_2 = 339;
    private static String FRENCH_SPECIAL_CHARACTER_REPLACED_1 = "OE";
    private static String FRENCH_SPECIAL_CHARACTER_REPLACED_2 = "oe";
    private static final int[] GREEK_ABOUT_TO_7BIT = new int[]{916, 934, 915, 923, 937, 937, 937, 928, 936, 931, 931, 920, 926, 65, 65, 65, 65, 66, 66, 69, 69, 69, 69, 72, 72, 72, 72, 73, 73, 73, 73, 73, 73, 73, 75, 75, 77, 77, 78, 78, 79, 79, 79, 79, 80, 80, 84, 84, 88, 88, 89, 89, 89, 89, 89, 89, 89, 90, 90, 65, 65, 65, 65, 69, 69, 69, 73, 73, 73, 73, 79, 79, 79, 79, 85, 85, 85, 89, 97, 97, 97, 199, 101, 101, LocationRequest.PRIORITY_NO_POWER, LocationRequest.PRIORITY_NO_POWER, LocationRequest.PRIORITY_NO_POWER, 111, 111, 111, 117, 117, 121, 121, 338, 339, 111, 117, 79, 85, 97, 115, 99, 122, 122, 101, 111, 108, 110, 65, 83, 67, 90, 90, 69, 76, 78, 76, 108, 67, 99, 68, 100, 69, 101, 78, 110, 82, 114, 83, 115, 84, 116, 85, 117, 89, 121, 90, 122};
    private static final int[] GREEK_ALPHA = new int[]{948, 966, 947, 955, 911, 969, 974, 960, 968, 962, 963, 952, 958, 902, 913, 940, 945, 914, 946, 904, 917, 941, 949, 905, 919, 942, 951, 906, 912, 921, 938, 943, 953, 970, 922, 954, 924, 956, 925, 957, 908, 927, 959, 972, 929, 961, 932, 964, 935, 967, 910, 933, 939, 965, 971, 973, 944, 950, 918, 192, 193, 194, 195, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, 202, 203, 204, 205, 206, 207, 210, 211, 212, 213, 217, 218, 219, 221, 225, 226, 227, 231, 234, 235, 237, 238, 239, 243, 244, 245, 250, 251, 253, 255, 338, 339, 337, 369, 336, 368, 261, 347, 263, 380, 378, 281, 243, 322, 324, 260, 346, 262, 379, 377, 280, 321, 323, 317, 318, 268, 269, 270, 271, 282, 283, 327, 328, 344, 345, 352, 353, 356, 357, 366, 367, 221, 253, 381, 382};
    private static final int[] GSM_7bit_Default_Alphabet = new int[]{64, 916, 32, 48, 161, 80, 191, 112, 163, 95, 33, 49, 65, 81, 97, 113, 36, 934, 34, 50, 66, 82, 98, 114, 165, 915, 35, 51, 67, 83, 99, 115, 232, 923, 164, 52, 68, 84, 100, 116, 233, 937, 37, 53, 69, 85, 101, 117, 249, 928, 38, 54, 70, 86, 102, 118, 236, 936, 39, 55, 71, 87, OfflineMapStatus.EXCEPTION_SDCARD, 119, 242, 931, 40, 56, 72, 88, LocationRequest.PRIORITY_LOW_POWER, 120, 199, 920, 41, 57, 73, 89, LocationRequest.PRIORITY_NO_POWER, 121, 10, 926, 42, 58, 74, 90, 106, 122, 216, 43, 59, 75, 196, 107, 228, 248, 198, 44, 60, 76, 214, 108, 246, 13, 230, 45, 61, 77, 209, 109, 241, 197, 223, 46, 62, 78, 220, 110, 252, 229, 201, 47, 63, 79, 167, 111, 224, 123, 125, 92, 91, 126, 93, 124, 8364, 94};
    private static final String[] IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "png", "gif"};
    private static final boolean IS_CHINA_OPTB = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    public static final boolean IS_CHINA_TELECOM_OPTA_OPTB;
    private static final char[] NUMERIC_CHARS_SUGAR = new char[]{'-', '.', ',', '(', ')', ' ', '/', '\\', '*', '#', '+'};
    private static final String PLATFORM_PROP = SystemProperties.get("ro.board.platform", "UNDEFINED");
    public static final String VO_WIFI_API_NAME = (VERSION.SDK_INT > 23 ? "isWifiCallingAvailable" : "isWifiCallingEnabled");
    public static final ComponentName WEICHAT_COMPONENTNAME = new ComponentName("com.tencent.mm", "com.tencent.mm.plugin.accountsync.ui.ContactsSyncUI");
    public static final ComponentName WHATSAPP_COMPONENTNAME = new ComponentName("com.whatsapp", "com.whatsapp.accountsync.ProfileActivity");
    private static SparseIntArray charToGsmGreekSingle;
    private static SparseIntArray charToGsmVenezuela;
    private static SparseIntArray gsm7BitDefaultAlphabetMap;
    private static int m7bitEnabled = -1;
    private static RcsMessageUtils mHwCust = new RcsMessageUtils();
    private static HwCustMessageUtils mHwCustMessageUtils = ((HwCustMessageUtils) HwCustUtils.createObj(HwCustMessageUtils.class, new Object[0]));
    private static boolean mIsAlwaysShowSmsOptimization = false;
    private static boolean mZoomFlag = false;
    private static Map numericSugarMap = new HashMap(NUMERIC_CHARS_SUGAR.length);
    private static final int sDsdsMode;
    private static boolean sIsMediaPanelInScrollingStatus;
    private static final boolean sIsMultiSimEnabled = (!isMultiSimEnabledEx1() ? isMultiSimEnabledEx2() : true);
    private static String sLocalNumber;
    private static String[] sNoSubjectStrings;
    private static final Map<String, String> sRecipientAddress = new ConcurrentHashMap(20);
    private static final int[] sVideoDuration = new int[]{0, 5, 10, 15, 20, 30, 40, 50, 60, 90, 120};
    private static String sugarSpaceChars = "-.,() /\\+";

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            equals = false;
        }
        IS_CHINA_TELECOM_OPTA_OPTB = equals;
        ALL_RELATED_CONTACTS_INFO.put(WEICHAT_COMPONENTNAME, "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile");
        ALL_RELATED_CONTACTS_INFO.put(WHATSAPP_COMPONENTNAME, "vnd.android.cursor.item/vnd.com.whatsapp.profile");
        ALL_RELATED_CONTACTS_INFO_BY_INDEX.put(Integer.valueOf(0), WEICHAT_COMPONENTNAME);
        ALL_RELATED_CONTACTS_INFO_BY_INDEX.put(Integer.valueOf(1), WHATSAPP_COMPONENTNAME);
        for (int i = 0; i < NUMERIC_CHARS_SUGAR.length; i++) {
            numericSugarMap.put(Character.valueOf(NUMERIC_CHARS_SUGAR[i]), Character.valueOf(NUMERIC_CHARS_SUGAR[i]));
        }
        if ("cdma_gsm".equals(DSDS_MODE_PROP)) {
            sDsdsMode = 1;
        } else if ("umts_gsm".equals(DSDS_MODE_PROP)) {
            sDsdsMode = 2;
        } else if ("tdscdma_gsm".equals(DSDS_MODE_PROP)) {
            sDsdsMode = 3;
        } else {
            sDsdsMode = 0;
        }
    }

    public static boolean isFeiXinNumber(String number) {
        if (number == null || !number.startsWith(StringUtils.phoneFiled12520) || number.length() <= 5) {
            return false;
        }
        return true;
    }

    public static boolean isYiDongMishuNumber(String number) {
        if (number == null || !number.startsWith("10658583") || number.length() <= 8) {
            return false;
        }
        return true;
    }

    public static ISms getISmsServiceOrThrow() {
        ISms iccISms = getISmsService();
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    private static ISms getISmsService() {
        return Stub.asInterface(ServiceManager.getService("isms"));
    }

    public static void setSingleShiftTable(int[] temp) {
        try {
            getISmsServiceOrThrow().setEnabledSingleShiftTables(temp);
        } catch (RemoteException e) {
            MLog.i(HwCustUpdateUserBehaviorImpl.MMS, "RemoteException setEnabledSingleShiftTables");
        }
    }

    public static void setSmsCodingNationalCode(String code) {
        try {
            getISmsServiceOrThrow().setSmsCodingNationalCode(code);
        } catch (RemoteException e) {
            MLog.i(HwCustUpdateUserBehaviorImpl.MMS, "RemoteException setSmsCodingNationalCode");
        }
    }

    public static String getAdjustedSpecialNumber(String number, int subId) {
        String adjustedNumber = number;
        if (!HwSpecialUtils.isChinaRegion() || MccMncConfig.isChinaMobieOperator(MccMncConfig.getDefault().getOperator(subId))) {
            return number;
        }
        if (isFeiXinNumber(number)) {
            adjustedNumber = number.substring(5);
        }
        if (isYiDongMishuNumber(adjustedNumber)) {
            adjustedNumber = adjustedNumber.substring(8);
        }
        return adjustedNumber;
    }

    public static int getImageDisplyAlpha(boolean isSmsEnable) {
        return isSmsEnable ? 255 : 127;
    }

    public static RcsMessageUtils getHwCust() {
        return mHwCust;
    }

    public static SparseIntArray getDefault7bitsTable() {
        synchronized (MessageUtils.class) {
            if (charToGsmGreekSingle == null) {
                charToGsmGreekSingle = new SparseIntArray();
                charToGsmGreekSingle.clear();
                int len = GREEK_ALPHA.length;
                for (int i = 0; i < len; i++) {
                    charToGsmGreekSingle.put(GREEK_ALPHA[i], GREEK_ABOUT_TO_7BIT[i]);
                }
            }
        }
        return charToGsmGreekSingle;
    }

    public static SparseIntArray getDefault7bitsTableVenezuela() {
        if (charToGsmVenezuela == null) {
            return getDefault7bitsTable();
        }
        return charToGsmVenezuela;
    }

    public static void set7bitsTableVenezuela(String alphabetForVenezuela) {
        charToGsmVenezuela = new SparseIntArray();
        if (alphabetForVenezuela != null && !"".equals(alphabetForVenezuela)) {
            charToGsmVenezuela.clear();
            String[] character7bit16bit = alphabetForVenezuela.split("\\|");
            for (String split : character7bit16bit) {
                String[] characterAfterSplit = split.split(",");
                charToGsmVenezuela.put(Integer.parseInt(characterAfterSplit[0].trim().substring(2), 16), Integer.parseInt(characterAfterSplit[1].trim().substring(2), 16));
            }
        }
    }

    private MessageUtils() {
    }

    public static String cleanseMmsSubject(Context context, String subject) {
        if (TextUtils.isEmpty(subject)) {
            return subject;
        }
        synchronized (MessageUtils.class) {
            if (sNoSubjectStrings == null) {
                sNoSubjectStrings = context.getResources().getStringArray(R.array.empty_subject_strings);
            }
            for (String equalsIgnoreCase : sNoSubjectStrings) {
                if (subject.equalsIgnoreCase(equalsIgnoreCase)) {
                    return null;
                }
            }
            return subject;
        }
    }

    public static void set7bitsTable(String alphabetFromHwdefaults) {
        charToGsmGreekSingle = new SparseIntArray();
        if (alphabetFromHwdefaults != null && !"".equals(alphabetFromHwdefaults)) {
            charToGsmGreekSingle.clear();
            String[] character7bit16bit = MmsConfig.getChar_7bit().split("\\|");
            for (String split : character7bit16bit) {
                String[] characterAfterSplit = split.split(",");
                charToGsmGreekSingle.put(Integer.parseInt(characterAfterSplit[0].trim().substring(2), 16), Integer.parseInt(characterAfterSplit[1].trim().substring(2), 16));
            }
        }
    }

    private static final SparseIntArray getGsm7BitDefaultAlphabetMap() {
        SparseIntArray sparseIntArray;
        synchronized (MessageUtils.class) {
            if (gsm7BitDefaultAlphabetMap == null) {
                gsm7BitDefaultAlphabetMap = new SparseIntArray(GSM_7bit_Default_Alphabet.length);
                int length = GSM_7bit_Default_Alphabet.length;
                for (int i = 0; i < length; i++) {
                    gsm7BitDefaultAlphabetMap.put(GSM_7bit_Default_Alphabet[i], GSM_7bit_Default_Alphabet[i]);
                }
            }
            sparseIntArray = gsm7BitDefaultAlphabetMap;
        }
        return sparseIntArray;
    }

    public static CharSequence replaceAlphabetFor7Bit(CharSequence s, int start, int end) {
        CharSequence sResult = null;
        if (s == null || start < 0 || start > end || start > s.length() || end > s.length()) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, String.format("onTextChanged, paramter error, start(%d), end(%d)", new Object[]{Integer.valueOf(start), Integer.valueOf(end)}));
            return null;
        }
        String currentmccmnc;
        StringBuilder stringBuilder = new StringBuilder(s);
        boolean schanged = false;
        if (MmsApp.getDefaultTelephonyManager().isMultiSimEnabled()) {
            currentmccmnc = MmsApp.getDefaultTelephonyManager().getSimOperator(SubscriptionManager.getDefaultSubscriptionId());
        } else {
            currentmccmnc = MmsApp.getDefaultTelephonyManager().getSimOperator();
        }
        int smsAllCharTo7Bit = MmsConfig.getSmsAllCharTo7Bit();
        String alphabetForVenezuela = MmsConfig.getChar7bitVenezuela();
        int i = start;
        while (i < end) {
            int creplace;
            if (TextUtils.isEmpty(currentmccmnc) || !currentmccmnc.startsWith(MmsConfig.getCustMccFor7bitMatchMap()) || TextUtils.isEmpty(alphabetForVenezuela)) {
                creplace = getDefault7bitsTable().get(stringBuilder.charAt(i), Integer.MAX_VALUE);
            } else {
                creplace = getDefault7bitsTableVenezuela().get(stringBuilder.charAt(i), Integer.MAX_VALUE);
            }
            if (Integer.MAX_VALUE != creplace) {
                stringBuilder.setCharAt(i, (char) creplace);
                if (creplace == FRENCH_SPECIAL_CHARACTER_1) {
                    stringBuilder.replace(i, i + 1, FRENCH_SPECIAL_CHARACTER_REPLACED_1);
                    end++;
                } else if (creplace == FRENCH_SPECIAL_CHARACTER_2) {
                    stringBuilder.replace(i, i + 1, FRENCH_SPECIAL_CHARACTER_REPLACED_2);
                    end++;
                }
                schanged = true;
            }
            if (smsAllCharTo7Bit != -1 && getGsm7BitDefaultAlphabetMap().get(stringBuilder.charAt(i), Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                stringBuilder.setCharAt(i, (char) smsAllCharTo7Bit);
                schanged = true;
            }
            i++;
        }
        if (schanged) {
            sResult = stringBuilder.subSequence(0, stringBuilder.length());
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "onTextChanged, new text");
        }
        return sResult;
    }

    public static String getMessageDetails(Context context, Cursor cursor, int size, long uID, boolean isMultiRecipients) {
        if (cursor == null) {
            return null;
        }
        if (!"mms".equals(cursor.getString(0))) {
            return getTextMessageDetails(context, cursor, uID, isMultiRecipients);
        }
        switch (cursor.getInt(18)) {
            case 128:
            case 132:
                return getMultimediaMessageDetails(context, cursor, size);
            case 130:
                return getNotificationIndDetails(context, cursor);
            default:
                MLog.w(HwCustUpdateUserBehaviorImpl.MMS, "No details could be retrieved.");
                return "";
        }
    }

    private static String getNotificationIndDetails(Context context, Cursor cursor) {
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();
        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, cursor.getLong(1));
        try {
            NotificationInd nInd = (NotificationInd) PduPersister.getPduPersister(context).load(uri);
            details.append(res.getString(R.string.message_type_label));
            details.append(res.getString(R.string.multimedia_notification));
            String from = extractEncStr(context, nInd.getFrom());
            details.append('\n');
            details.append(res.getString(R.string.from_label));
            details.append('‪');
            if (mHwCustMessageUtils != null) {
                from = mHwCustMessageUtils.getContactName(from);
            }
            if (TextUtils.isEmpty(from)) {
                from = res.getString(R.string.hidden_sender_address);
            }
            details.append(from);
            details.append('‬');
            details.append('\n');
            Object[] objArr = new Object[1];
            objArr[0] = formatTimeStampString(context, nInd.getExpiry() * 1000, true, true);
            details.append(res.getString(R.string.expire_on, objArr));
            EncodedStringValue subject = nInd.getSubject();
            if (!(subject == null || TextUtils.isEmpty(subject.getString()))) {
                details.append('\n');
                details.append(res.getString(R.string.subject_label));
                details.append(subject.getString());
            }
            details.append('\n');
            details.append(res.getString(R.string.message_class_label));
            details.append(new String(nInd.getMessageClass(), Charset.defaultCharset()));
            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            details.append('\n');
            details.append(res.getString(R.string.message_size_label));
            details.append(format.format((nInd.getMessageSize() + 1023) / 1024));
            details.append(context.getString(R.string.kilobyte));
            return details.toString();
        } catch (MmsException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Failed to load the message: " + uri, (Throwable) e);
            return context.getResources().getString(R.string.cannot_get_details);
        }
    }

    private static String getMultimediaMessageDetails(Context context, Cursor cursor, int size) {
        if (cursor.getInt(18) == 130) {
            return getNotificationIndDetails(context, cursor);
        }
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();
        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, cursor.getLong(1));
        try {
            MultimediaMessagePdu msg = (MultimediaMessagePdu) PduPersister.getPduPersister(context).load(uri);
            details.append(res.getString(R.string.message_type_label));
            details.append(res.getString(R.string.multimedia_message));
            if (msg instanceof RetrieveConf) {
                String from = extractEncStr(context, ((RetrieveConf) msg).getFrom());
                details.append('\n');
                details.append(res.getString(R.string.from_label));
                details.append('‪');
                if (mHwCustMessageUtils != null) {
                    from = mHwCustMessageUtils.getContactName(from);
                }
                if (TextUtils.isEmpty(from)) {
                    from = res.getString(R.string.hidden_sender_address);
                }
                details.append(from);
                details.append('‬');
            }
            details.append('\n');
            details.append(res.getString(R.string.to_address_label));
            EncodedStringValue[] to = msg.getTo();
            if (to != null) {
                String addr;
                details.append('‪');
                int length = to.length;
                for (int i = 0; i < length - 1; i++) {
                    addr = to[i].getString();
                    if (mHwCustMessageUtils != null) {
                        addr = mHwCustMessageUtils.getContactName(addr);
                    }
                    details.append(addr);
                    details.append(";");
                    details.append(" ");
                }
                addr = to[length - 1].getString();
                if (mHwCustMessageUtils != null) {
                    addr = mHwCustMessageUtils.getContactName(addr);
                }
                details.append(addr);
                details.append('‬');
            } else {
                MLog.w(HwCustUpdateUserBehaviorImpl.MMS, "recipient list is empty!");
            }
            if (msg instanceof SendReq) {
                EncodedStringValue[] values = ((SendReq) msg).getBcc();
                if (values != null && values.length > 0) {
                    details.append('\n');
                    details.append(res.getString(R.string.bcc_label));
                    details.append(EncodedStringValue.concat(values));
                }
            }
            int msgBox = cursor.getInt(19);
            if (msgBox == 1) {
                long sentDate = cursor.getLong(16);
                if (sentDate > 0) {
                    details.append('\n');
                    details.append(res.getString(R.string.sent_label));
                    details.append(formatTimeStampString(context, 1000 * sentDate, true, true));
                }
            }
            details.append('\n');
            long mmsDate = msg.getDate();
            if (msgBox == 3) {
                details.append(res.getString(R.string.saved_label));
            } else if (msgBox == 1) {
                details.append(res.getString(R.string.received_label));
                mmsDate = cursor.getLong(15);
            } else {
                details.append(res.getString(R.string.sent_label));
            }
            details.append(formatTimeStampString(context, 1000 * mmsDate, true, true));
            EncodedStringValue subject = msg.getSubject();
            if (subject != null) {
                String subStr = subject.getString();
                if (!TextUtils.isEmpty(subStr)) {
                    details.append('\n');
                    details.append(res.getString(R.string.subject_label));
                    size += subStr.length();
                    details.append(subStr);
                }
            }
            details.append('\n');
            details.append(res.getString(R.string.priority_label));
            details.append(getPriorityDescription(context, msg.getPriority()));
            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            details.append('\n');
            details.append(res.getString(R.string.message_size_label));
            details.append(format.format((long) (((size - 1) / Place.TYPE_SUBLOCALITY_LEVEL_2) + 1)));
            details.append(" ");
            details.append(res.getString(R.string.kilobyte));
            return details.toString();
        } catch (MmsException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Failed to load the message: " + uri, (Throwable) e);
            return context.getResources().getString(R.string.cannot_get_details);
        }
    }

    private static String getTextMessageDetails(Context context, Cursor cursor, long uID, boolean isMultiRecipients) {
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "getTextMessageDetails");
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();
        details.append(res.getString(R.string.message_type_label));
        if (mHwCust == null || mHwCust.addMsgType(context, cursor) == null) {
            details.append(res.getString(R.string.text_message));
        } else {
            details.append(mHwCust.addMsgType(context, cursor));
        }
        details.append('\n');
        int smsType = cursor.getInt(9);
        if (Sms.isOutgoingFolder(smsType)) {
            details.append(res.getString(R.string.to_address_label));
        } else {
            details.append(res.getString(R.string.from_label));
        }
        details.append('‪');
        String contactName;
        if (mHwCustMessageUtils != null) {
            contactName = (0 >= uID || !isMultiRecipients) ? mHwCustMessageUtils.getContactName(cursor.getString(3)) : getAddress(context, uID);
            details.append(contactName);
        } else {
            if (0 >= uID || !isMultiRecipients) {
                contactName = cursor.getString(3);
            } else {
                contactName = getAddress(context, uID);
            }
            details.append(contactName);
        }
        details.append('‬');
        if (smsType == 1) {
            long sentDate = cursor.getLong(7);
            if (sentDate > 0) {
                details.append('\n');
                details.append(res.getString(R.string.sent_label));
                details.append(formatTimeStampString(context, sentDate, true, true));
            }
        }
        if (smsType == 1) {
            int mSmsNetworkType = getSimpleNetworkMode(cursor.getInt(25));
            if (!(mSmsNetworkType == 2 || mSmsNetworkType == 0)) {
                details.append('\n');
                details.append(res.getString(R.string.sms_service_center));
                String serviceCenter = cursor.getString(30);
                if (serviceCenter == null || serviceCenter.length() <= 0) {
                    details.append(res.getString(R.string.channel_unknown));
                } else {
                    details.append('‪');
                    details.append(serviceCenter);
                    details.append('‬');
                }
            }
        }
        details.append('\n');
        if (smsType == 3) {
            details.append(res.getString(R.string.saved_label));
        } else if (smsType == 1) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }
        details.append(formatTimeStampString(context, cursor.getLong(6), true, true));
        int errorCode = cursor.getInt(12);
        if (errorCode != 0) {
            details.append('\n').append(res.getString(R.string.error_code_label)).append(errorCode);
        }
        return details.toString();
    }

    private static String getPriorityDescription(Context context, int PriorityValue) {
        Resources res = context.getResources();
        switch (PriorityValue) {
            case 128:
                return res.getString(R.string.priority_low);
            case 130:
                return res.getString(R.string.priority_high);
            default:
                return res.getString(R.string.priority_normal);
        }
    }

    public static boolean hasImageInFirstSlidShow(SlideshowModel model) {
        return model.size() > 1 && model.get(0).hasImage();
    }

    public static int getAttachmentType(SlideshowModel model, MultimediaMessagePdu mmp) {
        String subject = null;
        if (model == null) {
            return -1;
        }
        int numberOfSlides = model.size();
        if (numberOfSlides > 1) {
            return 4;
        }
        if (numberOfSlides == 1) {
            SlideModel slide = model.get(0);
            if (slide.hasVCalendar()) {
                return 6;
            }
            if (slide.hasVcard()) {
                return 5;
            }
            if (slide.hasVideo()) {
                return 2;
            }
            if (slide.hasAudio() && slide.hasImage()) {
                return 4;
            }
            if (slide.hasAudio()) {
                return 3;
            }
            if (slide.hasImage()) {
                return 1;
            }
            if (slide.hasText()) {
                return 0;
            }
            if (mmp != null) {
                if (mmp.getSubject() != null) {
                    subject = mmp.getSubject().getString();
                }
                if (!TextUtils.isEmpty(subject)) {
                    return 0;
                }
            }
        }
        return -2;
    }

    public static String getMessageShowTime(Context context, long when) {
        return getMessageShowTime(context, when, true);
    }

    public static String getMessageShowTime(Context context, long when, boolean isShowdate) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();
        if (isShowdate) {
            if (then.year != now.year) {
                return DateFormat.getDateFormat(context).format(Long.valueOf(when)).toString();
            }
            if (then.yearDay != now.yearDay) {
                return formatDateNoYear(DateFormat.getDateFormat(context).format(Long.valueOf(when)).toString());
            }
            return HwDateUtils.formatChinaDateTime(context, when, 527105);
        } else if (then.yearDay != now.yearDay || then.year != now.year) {
            return HwDateUtils.formatChinaDateTime(context, when, 527107);
        } else {
            if (!isNeedLayoutRtl() || "ur".equals(Locale.getDefault().getLanguage())) {
                return new StringBuffer().append(context.getResources().getString(R.string.mms_today)).append(" ").append("‭").append(HwDateUtils.formatChinaDateTime(context, when, 527105)).append("‬").toString();
            }
            return new StringBuffer().append(context.getResources().getString(R.string.mms_today)).append(" ").append("‬").append(HwDateUtils.formatChinaDateTime(context, when, 527105)).append("‭").toString();
        }
    }

    private static String formatDateNoYear(String date) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        String strDateFormat = date.replaceAll("[\\d]{4}", "");
        if (!Character.isDigit(strDateFormat.charAt(0))) {
            strDateFormat = strDateFormat.substring(1);
        } else if (!Character.isDigit(strDateFormat.charAt(strDateFormat.length() - 1))) {
            strDateFormat = strDateFormat.substring(0, strDateFormat.length() - 1);
        }
        return strDateFormat;
    }

    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false);
    }

    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        return formatTimeStampString(context, when, fullFormat, false);
    }

    @SuppressLint({"NewApi"})
    public static String formatTimeStampString(Context context, long when, boolean fullFormat, boolean hwFormat) {
        if (hwFormat) {
            return HwDateUtils.formatChinaDateTime(context, when, 68117);
        }
        int format_flags;
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();
        if (then.year != now.year) {
            format_flags = 527124;
        } else if (then.yearDay != now.yearDay) {
            format_flags = 527120;
        } else {
            format_flags = 527105;
        }
        if (fullFormat) {
            format_flags |= 17;
        }
        return HwDateUtils.formatChinaDateTime(context, when, format_flags);
    }

    public static Intent getSelectAudioIntent(long sizeLimit) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.putExtra("android.intent.extra.LOCAL_ONLY", true);
        intent.setType("audio/*");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setPackage("com.android.mediacenter");
        if (-1 != sizeLimit) {
            intent.putExtra("android.provider.MediaStore.extra.MAX_BYTES", sizeLimit);
        }
        return intent;
    }

    private static Intent getRecordSoundIntent(long sizeLimit) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("audio/amr");
        intent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder");
        intent.putExtra("android.provider.MediaStore.extra.MAX_BYTES", sizeLimit);
        return intent;
    }

    public static void recordSound(Fragment fragment, int requestCode, long sizeLimit) {
        try {
            fragment.startActivityForResult(getRecordSoundIntent(sizeLimit), requestCode);
        } catch (ActivityNotFoundException e) {
            shwNoAppDialog(fragment.getActivity());
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Sound Recorder Open Exception " + e);
        }
    }

    private static Intent getRecordVideoIntent(long sizeLimit) {
        sizeLimit = (long) (((float) sizeLimit) * 0.98f);
        if (MLog.isLoggable("Mms_app", 2)) {
            log("recordVideo: durationLimit: " + 0 + " sizeLimit: " + sizeLimit);
        }
        Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");
        intent.putExtra("android.intent.extra.videoQuality", 0);
        intent.putExtra("android.intent.extra.sizeLimit", sizeLimit);
        intent.putExtra("android.intent.extra.durationLimit", 0);
        intent.putExtra("output", TempFileProvider.SCRAP_VIDEO_URI);
        return intent;
    }

    public static void recordVideo(Fragment fragment, int requestCode, long sizeLimit) {
        try {
            fragment.startActivityForResult(getRecordVideoIntent(sizeLimit), requestCode);
        } catch (ActivityNotFoundException e) {
            shwNoAppDialog(fragment.getActivity());
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Media Open Exception " + e);
        }
    }

    private static Intent getCapturePictureIntent() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("output", TempFileProvider.SCRAP_CONTENT_URI);
        return intent;
    }

    public static void capturePicture(Fragment fragment, int requestCode) {
        try {
            if (fragment.getActivity().getApplicationContext().checkCallingOrSelfPermission("android.permission.CAMERA") == 0) {
                fragment.startActivityForResult(getCapturePictureIntent(), requestCode);
            }
        } catch (ActivityNotFoundException e) {
            shwNoAppDialog(fragment.getActivity());
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "camera Open Exception " + e);
        } catch (Exception e2) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "camera Open Exception " + e2);
        }
    }

    public static void shwNoAppDialog(Context context) {
        new Builder(context).setTitle(17040255).setPositiveButton(context.getResources().getString(R.string.yes), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }).setMessage(17040257).show();
    }

    public static Intent getGalleryCompressIntent(Activity activity, int position, boolean isRcsMode, boolean isGroupChat) {
        Intent intent = new Intent(activity, GalleryCompressActivity.class);
        intent.putExtra("gallery-compress-pisition", position);
        if (activity != null) {
            intent.putExtra("gallery-compress-activity", activity.hashCode());
        }
        intent.putExtra("gallery-compress-rcs", isRcsMode);
        intent.putExtra("isGroupChat", isGroupChat);
        return intent;
    }

    public static Intent getSelectVideoIntent() {
        return getIntentForSelectMediaByType("video/*", true);
    }

    public static Intent getSelectImageIntent() {
        return getIntentForSelectMediaByType("image/*", false);
    }

    public static Intent getSelectImageIntent(Context context, int maxSlectedCount) {
        Intent innerIntent = new Intent("android.intent.action.GET_CONTENT");
        innerIntent.setType("*/*");
        innerIntent.setPackage("com.android.gallery3d");
        innerIntent.putExtra("support-multipick-items", true);
        innerIntent.putExtra("max-select-count", maxSlectedCount);
        innerIntent.putExtra("return-uris-for-multipick", true);
        return innerIntent;
    }

    public static Intent getIntentForSelectMediaByType(String contentType, boolean localFilesOnly) {
        return getIntentForSelectMediaByType(contentType, localFilesOnly, -1);
    }

    public static Intent getIntentForSelectMediaByType(String contentType, boolean localFilesOnly, long sizeLimit) {
        Intent innerIntent;
        if ("image/*".equals(contentType)) {
            innerIntent = new Intent("android.intent.action.PICK");
        } else {
            innerIntent = new Intent("android.intent.action.GET_CONTENT");
        }
        if (mHwCust != null) {
            innerIntent = mHwCust.selectMediaByType(innerIntent, contentType);
        }
        innerIntent.setType(contentType);
        if (localFilesOnly) {
            innerIntent.putExtra("android.intent.extra.LOCAL_ONLY", true);
        }
        innerIntent.putExtra("ForwardIntent", true);
        if (-1 != sizeLimit) {
            innerIntent.putExtra("android.provider.MediaStore.extra.MAX_BYTES", sizeLimit);
        }
        return innerIntent;
    }

    public static void viewSimpleSlideshow(Context context, SlideshowModel slideshow) {
        if (slideshow.isSimple()) {
            SlideModel slide = slideshow.get(0);
            if (slide != null) {
                MediaModel mm = null;
                if (slide.hasImage()) {
                    mm = slide.getImage();
                } else if (slide.hasVideo()) {
                    mm = slide.getVideo();
                } else if (slide.hasAudio()) {
                    mm = slide.getAudio();
                }
                if (mm != null) {
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.addFlags(1);
                    intent.putExtra("SingleItemOnly", true);
                    intent.putExtra("android.intent.extra.TITLE", mm.getSmilAndPartName());
                    String contentType = mm.getContentType();
                    Uri dataUri = mm.getUri();
                    if ("application/vnd.oma.drm.message".equals(contentType) || "application/vnd.oma.drm.content".equals(contentType)) {
                        contentType = MmsApp.getApplication().getDrmManagerClient().getOriginalMimeType(dataUri);
                    }
                    intent.setDataAndType(dataUri, contentType);
                    if (isEndWithImageExtension(mm.getSmilAndPartName()) && contentType.equals("application/oct-stream")) {
                        intent.setDataAndType(dataUri, "image/*");
                    }
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString("contentType", contentType);
                    editor.commit();
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Unsupported Format,startActivity(intent) error,intent : " + intent);
                        Activity activity = (Activity) context;
                        showErrorDialog(activity, context.getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
                    }
                    return;
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("viewSimpleSlideshow() called on a non-simple slideshow");
    }

    public static void showErrorDialog(Context context, String title, String message) {
        CharSequence title2;
        Builder builder = new Builder(context);
        if (message == null) {
            message = title;
            title2 = null;
        }
        builder.setIcon(R.drawable.csp_menu_expand_dark);
        builder.setTitle(title2);
        builder.setMessage(message);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public static boolean isRestrictedType(ArrayList<String[]> mediaset) {
        for (String[] mime : mediaset) {
            boolean found = false;
            for (Entry<String, Integer> sContentType : CarrierContentRestriction.getRestricedEntrySet()) {
                if (((String) sContentType.getKey()).equalsIgnoreCase(mime[0])) {
                    try {
                        if (Integer.parseInt(mime[1]) <= ((Integer) sContentType.getValue()).intValue()) {
                            found = true;
                            continue;
                        } else {
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        found = false;
                        MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "isRestrictedType caught ", (Throwable) e);
                        continue;
                    }
                    if (!found) {
                        return true;
                    }
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    public static AlertDialog showDiscardDraftConfirmDialog(Context context, OnClickListener listener) {
        AlertDialog alertDialog = new Builder(context).setPositiveButton(R.string.discard, listener).setNegativeButton(R.string.no, null).setTitle(R.string.discard_message_reason).create();
        alertDialog.show();
        setButtonTextColor(alertDialog, -1, context.getResources().getColor(R.drawable.text_color_red));
        return alertDialog;
    }

    public static void setLocalNumber(String number) {
        sLocalNumber = number;
    }

    public static String getLocalNumber() {
        if (sLocalNumber == null) {
            sLocalNumber = MmsApp.getDefaultTelephonyManager().getLine1Number();
        }
        return sLocalNumber;
    }

    public static String getLocalNumber(int subId) {
        if (!isMultiSimEnabled()) {
            return getLocalNumber();
        }
        sLocalNumber = MmsApp.getDefaultMSimTelephonyManager().getLine1Number(subId);
        return sLocalNumber;
    }

    public static boolean isLocalNumber(String number) {
        boolean z = true;
        if (number == null || number.indexOf(64) >= 0) {
            return false;
        }
        if (!PhoneNumberUtils.compare(number, getLocalNumber(0))) {
            z = PhoneNumberUtils.compare(number, getLocalNumber(1));
        }
        return z;
    }

    public static List<ReadRecContent> getReadReportData(Context context, Collection<Long> threadIds, int status) {
        StringBuilder selectionBuilder = new StringBuilder("m_type = 132 AND read = 0 AND rr = 128");
        if (threadIds != null) {
            String s = threadIds.toString();
            selectionBuilder.append(" AND thread_id IN (").append(s.substring(1, s.length() - 1)).append(")");
        }
        Context context2 = context;
        Cursor c = SqliteWrapper.query(context2, context.getContentResolver(), Inbox.CONTENT_URI, new String[]{"_id", "m_id", "sub_id"}, selectionBuilder.toString(), null, null);
        List<ReadRecContent> pendingRecords = new ArrayList();
        if (c != null) {
            try {
                if (c.getCount() != 0) {
                    while (c.moveToNext()) {
                        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
                        if (MLog.isLoggable("Mms_app", 2)) {
                            LogTag.debug("sendReadReport: uri = " + uri, new Object[0]);
                        }
                        pendingRecords.add(new ReadRecContent(c.getString(1), AddressUtils.getFrom(context, uri), status, c.getInt(2)));
                    }
                    LogTag.debug("sendReadReport: pending-size " + pendingRecords.size(), new Object[0]);
                    return pendingRecords;
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    public static void sendReadReport(Context context, List<ReadRecContent> datas) {
        HashMap<String, String> sendedMsgs = new HashMap();
        if (isMultiSimEnabled()) {
            for (ReadRecContent r : datas) {
                if (r.subscription == 0 && !sendedMsgs.containsKey(r.messageId)) {
                    MmsMessageSender.sendReadRec(context, r.to, r.messageId, r.status, 0);
                    sendedMsgs.put(r.messageId, "");
                }
            }
            sendedMsgs.clear();
            for (ReadRecContent r2 : datas) {
                if (r2.subscription == 1 && !sendedMsgs.containsKey(r2.messageId)) {
                    MmsMessageSender.sendReadRec(context, r2.to, r2.messageId, r2.status, 1);
                    sendedMsgs.put(r2.messageId, "");
                }
            }
            return;
        }
        for (ReadRecContent r22 : datas) {
            if (!sendedMsgs.containsKey(r22.messageId)) {
                MmsMessageSender.sendReadRec(context, r22.to, r22.messageId, r22.status);
                sendedMsgs.put(r22.messageId, "");
            }
        }
    }

    public static void handleReadReport(Context context, Collection<Long> threadIds, int status, Runnable callback) {
        List<ReadRecContent> msgToSend = getReadReportData(context, threadIds, status);
        if (msgToSend == null || msgToSend.size() == 0) {
            if (callback != null) {
                callback.run();
            }
        } else if (MmsConfig.isShowMmsReadReportDialog()) {
            confirmReadReportDialog(context, msgToSend, callback);
        } else {
            if (PreferenceUtils.isEnableAutoReplyMmsRR(context)) {
                ResEx.makeToast(context.getResources().getQuantityString(R.plurals.read_report_toast_msg, msgToSend.size(), new Object[]{Integer.valueOf(msgToSend.size())}), 0);
                sendReadReport(context, msgToSend);
            }
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static void confirmReadReportDialog(final Context context, final List<ReadRecContent> msgToSend, final Runnable callback) {
        OnClickListener positiveListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ResEx.makeToast(context.getResources().getQuantityString(R.plurals.read_report_toast_msg, msgToSend.size(), new Object[]{Integer.valueOf(msgToSend.size())}), 0);
                MessageUtils.sendReadReport(context, msgToSend);
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };
        OnClickListener negativeListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };
        OnCancelListener cancelListener = new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };
        String msg = context.getResources().getQuantityString(R.plurals.read_report_toast_msg, msgToSend.size(), new Object[]{Integer.valueOf(msgToSend.size())});
        Builder builder = new Builder(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        builder.setCancelable(true);
        builder.setTitle(R.string.confirm);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.yes, positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener);
        builder.setOnCancelListener(cancelListener);
        if (!(context instanceof Activity) || ((Activity) context).isFinishing()) {
            AlertDialog ad = builder.create();
            ad.getWindow().setType(AMapException.CODE_AMAP_ENGINE_TABLEID_NOT_EXIST);
            ad.show();
            return;
        }
        builder.show();
    }

    public static String extractEncStrFromCursor(Cursor cursor, int columnRawBytes, int columnCharset) {
        try {
            String rawBytes = cursor.getString(columnRawBytes);
            int charset = cursor.getInt(columnCharset);
            if (TextUtils.isEmpty(rawBytes)) {
                return "";
            }
            if (charset == 0) {
                return rawBytes;
            }
            return new EncodedStringValue(charset, PduPersister.getBytes(rawBytes)).getString();
        } catch (CursorIndexOutOfBoundsException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, " exctractEncStrFromCursor occur cursorIndexOutOfBoundsException " + e);
            return "";
        }
    }

    private static String extractEncStr(Context context, EncodedStringValue value) {
        if (value != null) {
            return value.getString();
        }
        return "";
    }

    public static void viewMmsMessageAttachment(Activity activity, Uri msgUri, SlideshowModel slideshow, AsyncDialog asyncDialog) {
        viewMmsMessageAttachment(activity, msgUri, slideshow, 0, asyncDialog);
    }

    public static void viewMmsMessageAttachment(final Activity activity, final Uri msgUri, final SlideshowModel slideshow, final int requestCode, AsyncDialog asyncDialog) {
        if (!(slideshow == null ? false : slideshow.isSimple()) || MmsConfig.isEnableSlideShowforSingleMedia()) {
            final boolean slidemode = "slideshowactivity".equals(MmsConfig.getPrefPlaymode());
            asyncDialog.runAsync(new Runnable() {
                public void run() {
                    if (slidemode && slideshow != null) {
                        PduPersister persister = PduPersister.getPduPersister(activity);
                        try {
                            PduBody pb = slideshow.toPduBody();
                            persister.updateParts(msgUri, pb, null);
                            slideshow.sync(pb);
                        } catch (MmsException e) {
                            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Unable to save message for preview");
                        }
                    }
                }
            }, new Runnable() {
                public void run() {
                    if (slidemode) {
                        MessageUtils.launchSlideshowActivity(activity, msgUri, requestCode);
                        return;
                    }
                    Intent intent = new Intent(activity, SlideSmootShowActivity.class);
                    intent.setData(msgUri);
                    activity.startActivityForResult(intent, requestCode);
                }
            }, R.string.building_slideshow_title);
            return;
        }
        viewSimpleSlideshow(activity, slideshow);
    }

    public static void viewMmsMessageAttachment(HwBaseFragment fragment, Uri msgUri, SlideshowModel slideshow, AsyncDialog asyncDialog) {
        viewMmsMessageAttachment(fragment, msgUri, slideshow, 0, asyncDialog);
    }

    public static void viewMmsMessageAttachment(final HwBaseFragment fragment, final Uri msgUri, final SlideshowModel slideshow, final int requestCode, AsyncDialog asyncDialog) {
        if (!(slideshow == null ? false : slideshow.isSimple()) || MmsConfig.isEnableSlideShowforSingleMedia()) {
            final boolean slidemode = "slideshowactivity".equals(MmsConfig.getPrefPlaymode());
            asyncDialog.runAsync(new Runnable() {
                public void run() {
                    if (slidemode && slideshow != null) {
                        PduPersister persister = PduPersister.getPduPersister(fragment.getContext());
                        try {
                            PduBody pb = slideshow.toPduBody();
                            persister.updateParts(msgUri, pb, null);
                            slideshow.sync(pb);
                        } catch (MmsException e) {
                            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Unable to save message for preview");
                        }
                    }
                }
            }, new Runnable() {
                public void run() {
                    if (slidemode) {
                        MessageUtils.launchSlideshowActivity(fragment, msgUri, requestCode);
                        return;
                    }
                    Intent intent = new Intent(fragment.getContext(), SlideSmootShowActivity.class);
                    intent.setData(msgUri);
                    fragment.startActivityForResult(intent, requestCode);
                }
            }, R.string.building_slideshow_title);
            return;
        }
        viewSimpleSlideshow(fragment.getContext(), slideshow);
    }

    public static void launchSlideshowActivity(Context context, Uri msgUri, int requestCode) {
        Intent intent = new Intent(context, SlideshowActivity.class);
        intent.setData(msgUri);
        intent.setFlags(536870912);
        if (requestCode <= 0 || !(context instanceof Activity)) {
            context.startActivity(intent);
        } else {
            ((Activity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static void launchSlideshowActivity(HwBaseFragment fragment, Uri msgUri, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), SlideshowActivity.class);
        intent.setData(msgUri);
        intent.setFlags(536870912);
        if (requestCode > 0) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            fragment.startActivity(intent);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isAlias(String string) {
        if (!MmsConfig.isAliasEnabled() || string == null || string.isEmpty()) {
            return false;
        }
        int len = string.length();
        if (len < MmsConfig.getAliasMinChars() || len > MmsConfig.getAliasMaxChars() || !Character.isLetter(string.charAt(0))) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            boolean z;
            char c = string.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '.') {
                z = true;
            } else {
                z = false;
            }
            if (!z) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmptyPhoneNumber(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (sugarSpaceChars.indexOf(str.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }

    private static String parsePhoneNumberForMms(String address, boolean allowed) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();
        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);
            if (c == '+' && builder.length() == 0) {
                builder.append(c);
            } else if (Character.isDigit(c)) {
                builder.append(c);
            } else if ((mHwCustMessageUtils == null || !mHwCustMessageUtils.isPoundChar(builder, c)) && numericSugarMap.get(Character.valueOf(c)) == null && !allowed) {
                return null;
            }
        }
        return builder.toString();
    }

    public static boolean isSugarChar(char c) {
        return numericSugarMap.get(Character.valueOf(c)) != null;
    }

    public static boolean isValidMmsAddress(String address) {
        String retVal;
        if (MmsConfig.isUseGgSmsAddressCheck()) {
            retVal = parseMmsAddress(address, true);
        } else {
            retVal = parseMmsAddress(address);
        }
        if (retVal == null || retVal.isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean isServerAddress(String address) {
        if (TextUtils.isEmpty(address) || Contact.isEmailAddress(address)) {
            return false;
        }
        int len = address.length();
        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);
            if (!Character.isDigit(c) && numericSugarMap.get(Character.valueOf(c)) == null) {
                return true;
            }
        }
        return false;
    }

    public static String parseMmsAddress(String address) {
        return parseMmsAddress(address, false);
    }

    public static String parseMmsAddress(String address, boolean allowed) {
        if (address == null) {
            return null;
        }
        if (Contact.isEmailAddress(address)) {
            return address;
        }
        String retVal = parsePhoneNumberForMms(address, allowed);
        if (retVal != null && retVal.length() != 0) {
            return retVal;
        }
        if (isAlias(address)) {
            return address;
        }
        return null;
    }

    private static void log(String msg) {
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "[MsgUtils] " + msg);
    }

    public static boolean isCardStatusValid(int subscription) {
        return 1 == getIccCardStatus(subscription);
    }

    public static int getIccCardStatus(int subscription) {
        if (isAirplanModeOn(MmsApp.getApplication().getApplicationContext())) {
            return 0;
        }
        if (!isCardPresent(subscription)) {
            return 2;
        }
        if (5 == getSimState(subscription) && getSubState(subscription)) {
            return 1;
        }
        return 0;
    }

    public static int getIccCardStatus() {
        if (isAirplanModeOn(MmsApp.getApplication().getApplicationContext())) {
            return 0;
        }
        switch (MmsApp.getDefaultTelephonyManager().getSimState()) {
            case 1:
                return 2;
            case 5:
                return 1;
            default:
                return 0;
        }
    }

    public static int getSimpleNetworkMode(int networkType) {
        switch (networkType) {
            case 1:
            case 2:
                return 3;
            case 3:
            case 8:
            case 9:
            case 10:
            case 15:
                return 1;
            case 4:
            case 5:
            case 6:
            case 7:
            case 12:
            case 14:
                return 2;
            case 13:
                return 3;
            default:
                return 0;
        }
    }

    public static int getDsdsMode() {
        return sDsdsMode;
    }

    public static int getNetworkType(int subscription) {
        int netWorkType = -1;
        try {
            MmsApp.getDefaultMSimTelephonyManager();
            netWorkType = MSimTelephonyManager.getNetworkType(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netWorkType;
    }

    public static int getPreferredSmsSubscription() {
        int preferredSmsSub = -1;
        try {
            preferredSmsSub = MSimSmsManagerEx.getPreferredSmsSubscription();
        } catch (Exception e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "hwext addon has not methoed getPreferredSmsSubscription");
            try {
                preferredSmsSub = MSimSmsManager.getDefault().getPreferredSmsSubscription();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return preferredSmsSub;
    }

    public static int getPreferredDataSubscription() {
        int preferredDataSub = -1;
        try {
            preferredDataSub = MmsApp.getDefaultMSimTelephonyManager().getPreferredDataSubscription();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preferredDataSub;
    }

    public static int getDefaultSubscription() {
        int defaultSubscription = -1;
        try {
            defaultSubscription = MmsApp.getDefaultMSimTelephonyManager().getDefaultSubscription();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultSubscription;
    }

    public static String getSmscAddrOnSubscription(int subscription) {
        String smscAddr = null;
        try {
            smscAddr = MSimSmsManagerEx.getSmscAddrOnSubscription(subscription);
        } catch (Exception e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "hwext addon has not methoed getSmscAddrOnSubscription");
            try {
                smscAddr = MSimSmsManager.getDefault().getSmscAddrOnSubscription(subscription);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return smscAddr;
    }

    public static void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int subscription) {
        try {
            MSimSmsManagerEx.sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, subscription);
        } catch (Exception e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "hwext addon has not method sendMultipartTextMessage");
            try {
                MSimSmsManager.getDefault().sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, subscription);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static int getSimState(int slotId) {
        try {
            return MmsApp.getDefaultTelephonyManager().getSimState(slotId);
        } catch (Exception e) {
            MLog.i(HwCustUpdateUserBehaviorImpl.MMS, "getSimState: caught " + e);
            return -1;
        }
    }

    public static final boolean isMultiSimEnabled() {
        return sIsMultiSimEnabled;
    }

    public static boolean isMultiSimEnabledEx1() {
        try {
            return TelephonyManagerEx.isMultiSimEnabled(MmsApp.getDefaultTelephonyManager());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isMultiSimEnabledEx2() {
        try {
            return MmsApp.getDefaultMSimTelephonyManager().isMultiSimEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isCardPresent(int slotId) {
        try {
            int state = getSimState(slotId);
            if (state == 0 || state == 1 || state == 8) {
                return false;
            }
            return true;
        } catch (Exception e) {
            MLog.i(HwCustUpdateUserBehaviorImpl.MMS, "isCardPresent: caught " + e);
            return false;
        }
    }

    public static boolean setSmscAddr(String smscAddr) {
        boolean setSucessOrNot = false;
        try {
            setSucessOrNot = SmsManagerEx.setSmscAddr(SmsManager.getDefault(), smscAddr);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorMonitor.reportRadar(907000016, 0, 0, "setSmscAddr exception", "");
        }
        return setSucessOrNot;
    }

    public static boolean setSmsAddressBySubID(String address, int subID) {
        boolean result = false;
        if (subID < 0) {
            try {
                result = SmsManagerEx.setSmscAddr(SmsManager.getDefault(), address);
            } catch (Exception e) {
                MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "set the sms center address exception");
            }
        } else {
            try {
                result = MSimSmsManagerEx.setSmscAddrOnSubscription(address, subID);
            } catch (Exception e2) {
                MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "hwext addon has not method setSmscAddrOnSubscription");
                try {
                    result = MSimSmsManager.getDefault().setSmscAddrOnSubscription(address, subID);
                } catch (Exception e22) {
                    e22.printStackTrace();
                    ErrorMonitor.reportRadar(907000016, 0, subID, "setSmsAddressBySubID exception", "");
                }
            }
        }
        return result;
    }

    public static String getSmsAddressBySubID(int subID) {
        if (subID >= 0) {
            return getSmscAddrOnSubscription(subID);
        }
        try {
            return SmsManagerEx.getSmscAddr(SmsManager.getDefault());
        } catch (Exception e) {
            e.printStackTrace();
            ErrorMonitor.reportRadar(907000016, 1, subID, "getSmsAddressBySubID exception", "");
            return null;
        }
    }

    public static int getCurrentPhoneType(int subscription) {
        int curPhoneType = -1;
        try {
            curPhoneType = MmsApp.getDefaultMSimTelephonyManager().getCurrentPhoneType(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curPhoneType;
    }

    public static boolean getSubState(int slotId) {
        try {
            return HwTelephonyManager.getDefault().getSubState((long) slotId) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getMmsAutoSetDataSubscription() {
        int mmsAutoSetDataSub = -1;
        try {
            mmsAutoSetDataSub = MmsApp.getDefaultMSimTelephonyManager().getMmsAutoSetDataSubscription();
        } catch (Exception e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "hwext addon has not method getMmsAutoSetDataSubscription");
        }
        return mmsAutoSetDataSub;
    }

    public static String getFeatureEnableMms(int subId) {
        String value = "";
        if (subId == 0) {
            try {
                value = "enableMMS_sub1";
                return "enableMMS_sub1";
            } catch (Exception e) {
                MLog.i(HwCustUpdateUserBehaviorImpl.MMS, "getFeatureEnableMms e=" + e);
                return value;
            }
        } else if (subId != 1) {
            return value;
        } else {
            value = "enableMMS_sub2";
            return "enableMMS_sub2";
        }
    }

    public static int getSubId(SmsMessage sms) {
        int value = 0;
        try {
            value = SmsMessageEx.getSubId(sms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Uri smsSentAddMessage(ContentResolver resolver, String address, String body, String subject, Long date, int subId) {
        Uri value = null;
        try {
            return Sent.addMessage(resolver, address, body, subject, date, subId);
        } catch (NoExtAPIException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "smsSentAddMessage exception: " + e);
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "TelephonyEx not Exists smsSentAddMessage, and gg oring will be used ");
            try {
                return Sms.Sent.addMessage(subId, resolver, address, body, subject, date);
            } catch (Exception e2) {
                MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "smsSentAddMessage exception: " + e2);
                return value;
            }
        } catch (Exception e22) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "smsSentAddMessage exception: " + e22);
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "TelephonyEx not Exists smsSentAddMessage, and gg oring will be used ");
            return Sms.Sent.addMessage(subId, resolver, address, body, subject, date);
        } catch (NoSuchMethodError e3) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "smsSentAddMessage exception: " + e3);
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "TelephonyEx not Exists smsSentAddMessage, and gg oring will be used ");
            return Sms.Sent.addMessage(subId, resolver, address, body, subject, date);
        }
    }

    public static boolean isNetworkRoaming(int subscription) {
        try {
            boolean isRoaming = MmsApp.getDefaultTelephonyManager().isNetworkRoaming(subscription);
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "card subscription = " + subscription + " is isRoaming " + isRoaming);
            if (mHwCust == null || !mHwCust.configRoamingNationalAsLocal() || !isRoaming || !mHwCust.isRoamingNationalP4(subscription)) {
                return isRoaming;
            }
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "Poland national roaming, force auto download, set roaming as false");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isNetworkRoaming() {
        boolean isRoaming = false;
        try {
            isRoaming = MmsApp.getDefaultTelephonyManager().isNetworkRoaming();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRoaming;
    }

    public static void setRoamingAutoRetrieveValue(ContentResolver resolver, int cardId, boolean isChecked) {
        int i = 1;
        String str;
        if (cardId == 1) {
            str = "auto_download_mms_card1_roaming";
            if (!isChecked) {
                i = 0;
            }
            System.putInt(resolver, str, i);
            MLog.v("AutoDownloadMms", "Card1 RoamingAutoRetrievalPref check states changed to:" + isChecked);
            MLog.v("AutoDownloadMms", "Card1 RoamingAutoRetrieve value stored in SettingProvider update to:" + getRoamingAutoRetrieveValue(resolver, cardId));
        } else if (cardId == 2) {
            str = "auto_download_mms_card2_roaming";
            if (!isChecked) {
                i = 0;
            }
            System.putInt(resolver, str, i);
            MLog.v("AutoDownloadMms", "Card2 RoamingAutoRetrievalPref check states changed to:" + isChecked);
            MLog.v("AutoDownloadMms", "Card2 RoamingAutoRetrieve value stored in SettingProvider update to:" + getRoamingAutoRetrieveValue(resolver, cardId));
        }
    }

    public static int getRoamingAutoRetrieveValue(ContentResolver resolver, int cardId) {
        if (cardId == 1) {
            return System.getInt(resolver, "auto_download_mms_card1_roaming", 0);
        }
        if (cardId == 2) {
            return System.getInt(resolver, "auto_download_mms_card2_roaming", 0);
        }
        return 0;
    }

    public static int getSIMMsgCount(Context context) {
        String sUri = "content://sms/icc";
        int reCount = 0;
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), Uri.parse("content://sms/icc"), null, null, null, null);
            if (cursor != null) {
                reCount = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "SQLiteException error", (Throwable) e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return reCount;
    }

    public static void printCursorInfo(Cursor c) {
        if (c != null) {
            int pos = c.getPosition();
            c.moveToPosition(-1);
            int counts = c.getColumnNames().length;
            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, "=======cursor size: " + c.getCount() + " , columns: " + counts);
            int i = 0;
            while (c.moveToNext()) {
                i++;
                MLog.v(HwCustUpdateUserBehaviorImpl.MMS, "---------Record: " + i);
                for (int j = 0; j < counts; j++) {
                    switch (c.getType(j)) {
                        case 0:
                            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, c.getColumnName(j) + " FIELD_TYPE_NULL: " + c.getString(j));
                            break;
                        case 1:
                            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, c.getColumnName(j) + " FIELD_TYPE_INTEGER : " + c.getInt(j));
                            break;
                        case 2:
                            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, c.getColumnName(j) + " FIELD_TYPE_FLOAT : " + c.getFloat(j));
                            break;
                        case 3:
                            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, c.getColumnName(j) + " FIELD_TYPE_STRING : " + c.getString(j));
                            break;
                        case 4:
                            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, c.getColumnName(j) + " FIELD_TYPE_BLOB : " + new String(c.getBlob(j), Charset.defaultCharset()));
                            break;
                        default:
                            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, c.getColumnName(j) + " FIELD_TYPE_DEFAULT : " + c.getString(j));
                            break;
                    }
                }
            }
            c.moveToPosition(pos);
            return;
        }
        MLog.v(HwCustUpdateUserBehaviorImpl.MMS, "cursor is null!");
    }

    public static boolean isDataSwitchOn(Context context) {
        Integer result = Integer.valueOf(Global.getInt(context.getContentResolver(), "mobile_data", 0));
        return result != null && 1 == result.intValue();
    }

    public static boolean isAirplanModeOn(Context context) {
        if (Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isUsingVoWifi(Context context) {
        return isVoWifiEnabled(context);
    }

    public static boolean isVoWifiEnabled(Context context) {
        if (isWifiCallEnabled() && getWifiConnected(context)) {
            return true;
        }
        return false;
    }

    public static boolean getWifiConnected(Context context) {
        if (context == null) {
            return false;
        }
        WifiManager wifiMgr = (WifiManager) context.getSystemService("wifi");
        ConnectivityManager cntMgr = (ConnectivityManager) context.getSystemService("connectivity");
        if (wifiMgr == null || cntMgr == null) {
            return false;
        }
        boolean wifiEnabled = wifiMgr.isWifiEnabled();
        NetworkInfo nwInfo = cntMgr.getActiveNetworkInfo();
        boolean wifiConnected = (nwInfo == null || !nwInfo.isConnected()) ? false : nwInfo.getType() == 1;
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "wifiEnabled" + wifiEnabled + "wifiConnected" + wifiConnected);
        if (!wifiEnabled) {
            wifiConnected = false;
        }
        return wifiConnected;
    }

    public static boolean isWifiCallEnabled() {
        boolean ret = false;
        TelephonyManager mSimTelephonyManager = MmsApp.getDefaultTelephonyManager();
        if (mSimTelephonyManager != null) {
            try {
                Method isWifiCallingEnabled = TelephonyManager.class.getDeclaredMethod(VO_WIFI_API_NAME, new Class[0]);
                isWifiCallingEnabled.setAccessible(true);
                Boolean isEnabled = (Boolean) isWifiCallingEnabled.invoke(mSimTelephonyManager, (Object[]) null);
                Method isImsRegistered = TelephonyManager.class.getDeclaredMethod("isImsRegistered", new Class[0]);
                isImsRegistered.setAccessible(true);
                Boolean isRegistered = (Boolean) isImsRegistered.invoke(mSimTelephonyManager, (Object[]) null);
                if (!(isEnabled == null || isRegistered == null)) {
                    if (isEnabled.booleanValue()) {
                        ret = isRegistered.booleanValue();
                    } else {
                        ret = false;
                    }
                    MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "isWifiCallingAvailable = " + isEnabled.booleanValue() + " isImsRegistered = " + isRegistered.booleanValue());
                }
                return ret;
            } catch (Exception aEx) {
                aEx.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable() {
        boolean z = false;
        Context context = MmsApp.getApplication().getApplicationContext();
        if (isUsingVoWifi(context)) {
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "vowifi enable, network available");
            return true;
        } else if (HwSpecialUtils.isAlwaysEnableMmsMobileLink(context)) {
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "single sim always enable mms mobile link enabled");
            if (!isAirplanModeOn(context)) {
                z = true;
            }
            return z;
        } else if (!isDataSwitchOn(context)) {
            return false;
        } else {
            if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
                MLog.i(HwCustUpdateUserBehaviorImpl.MMS, "The device is not Chinatelecom device.");
                if (isNetworkRoaming() && !getRoamingDataEnabled(context, 0)) {
                    MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "multi sim network roaming, but roaming data not enabled");
                    return false;
                }
            }
            NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(2);
            if (info != null) {
                z = info.isAvailable();
            }
            return z;
        }
    }

    public static boolean isNetworkAvailable(int sub) {
        boolean z = false;
        if (isCardPresent(sub)) {
            Context context = MmsApp.getApplication().getApplicationContext();
            if (!isMultiSimEnabled()) {
                return isNetworkAvailable();
            }
            if (HwSpecialUtils.isAlwaysEnableMmsMobileLink(context, sub)) {
                MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "multi sim always enable mms mobile link enabled, sub = " + sub);
                if (!isAirplanModeOn(context)) {
                    z = true;
                }
                return z;
            } else if (!isDataSwitchOn(context)) {
                return false;
            } else {
                if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
                    MLog.i(HwCustUpdateUserBehaviorImpl.MMS, "The device is not Chinatelecom device.");
                    if (isNetworkRoaming(sub) && !getRoamingDataEnabled(context, sub)) {
                        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "multi sim network roaming, but roaming data not enabled");
                        return false;
                    }
                }
                NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(2);
                if (info != null) {
                    z = info.isAvailable();
                }
                return z;
            }
        }
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "network not available, card not inserted, sub = " + sub);
        return false;
    }

    public static void setSettingsVaules(ContentResolver cr, String key, int value) {
        try {
            System.putInt(cr, key, value);
        } catch (Exception e) {
            MLog.w(HwCustUpdateUserBehaviorImpl.MMS, "System 'putInt':", e);
            try {
                System.putInt(cr, key, value);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isNeedShowToastWhenNetIsNotAvailable(Context context, MessageItem mMessageItem) {
        boolean cust = mMessageItem.isRcsChat();
        if (mMessageItem.isSms() || cust || isNetworkAvailable(mMessageItem.mSubId)) {
            return false;
        }
        ResEx.makeToast((int) R.string.mobileDataDisabled_Toast, 0);
        return true;
    }

    public static boolean isNormalASCII(CharSequence conString) {
        String FN_NUMBER_SEPARATORS = "\\/:*?\"<>|";
        int len = conString.length();
        for (int i = 0; i < len; i++) {
            char c = conString.charAt(i);
            if (c < ' ' || c > '~' || FN_NUMBER_SEPARATORS.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }

    public static int getResoureId(Context context) {
        return R.layout.conversation_list_item;
    }

    protected static void setZoomFlag(boolean flag) {
        mZoomFlag = flag;
    }

    protected static boolean getZoomFlag() {
        return mZoomFlag;
    }

    public static boolean setButtonTextColor(AlertDialog dialog, int which, int colorID) {
        Button button = dialog.getButton(which);
        if (button == null) {
            return false;
        }
        button.setTextColor(colorID);
        return true;
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager windowManager = activity.getWindowManager();
        Display display = null;
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
        }
        if (display == null) {
            return 0;
        }
        display.getMetrics(metric);
        return metric.widthPixels;
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager windowManager = activity.getWindowManager();
        Display display = null;
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
        }
        if (display == null) {
            return 0;
        }
        display.getMetrics(metric);
        return metric.heightPixels;
    }

    public static boolean isMultiSimActive() {
        if (isMultiSimEnabled() && 1 == getIccCardStatus(0)) {
            return 1 == getIccCardStatus(1);
        } else {
            return false;
        }
    }

    public static String getDefaultRintoneStr(Context context) {
        String uriPath = MmsConfig.getRingToneUriFromDatabase(context, "theme_message_path");
        String uriFromPath = null;
        if (uriPath != null) {
            uriFromPath = MessagingNotification.getUriByPath(context, uriPath);
        }
        if (uriFromPath != null) {
            return uriFromPath;
        }
        Uri uri = AssignRingTonePreference.getCustomRingtoneUri(context);
        if (uri == null) {
            uri = RingtoneManager.getDefaultUri(2);
        }
        return uri == null ? "" : uri.toString();
    }

    public static boolean isMmsText(CharSequence text) {
        boolean z = false;
        if (TextUtils.isEmpty(text) || MmsConfig.getMultipartSmsEnabled()) {
            return false;
        }
        if (SmsMessage.calculateLength(text, false)[0] >= MmsConfig.getSmsToMmsTextThreshold()) {
            z = true;
        }
        return z;
    }

    public static void goToLauncherHome(Activity activity) {
        MLog.v(HwCustUpdateUserBehaviorImpl.MMS, "goToLauncherHome::the calling activity is " + activity);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(268435456);
        intent.addCategory("android.intent.category.HOME");
        activity.startActivity(intent);
    }

    public static String getStorageStatus(Context context) {
        String[] PROJECTION = new String[]{"_id", "m_size"};
        ContentResolver cr = context.getContentResolver();
        Resources res = context.getResources();
        StringBuilder buffer = new StringBuilder();
        String smsSelection = "thread_id>0";
        Cursor cursor = cr.query(Mms.CONTENT_URI, PROJECTION, new StringBuffer(smsSelection).append(" and ").append("m_type").append(" in (").append(128).append(",").append(132).append(",").append(130).append(")").toString(), null, null);
        int mmsCount = 0;
        long size = 0;
        if (cursor != null) {
            try {
                mmsCount = cursor.getCount();
                if (cursor.moveToFirst()) {
                    while (true) {
                        size += (long) cursor.getInt(1);
                        if (!cursor.moveToNext()) {
                            break;
                        }
                    }
                }
                cursor.close();
            } catch (Exception e) {
                MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "cursor get value has an error >>>" + e);
            } catch (Throwable th) {
            }
        }
        buffer.append(res.getQuantityString(R.plurals.storage_dialog_mms, mmsCount, new Object[]{Integer.valueOf(mmsCount)}));
        buffer.append("\n");
        buffer.append(res.getString(R.string.storage_dialog_mms_readable_size, new Object[]{getHumanReadableSize(context, size)}));
        buffer.append("\n");
        cursor = cr.query(Sms.CONTENT_URI, new String[]{"_id"}, smsSelection, null, null);
        int smsCount = 0;
        if (cursor != null) {
            try {
                smsCount = cursor.getCount();
            } catch (Exception e2) {
                MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "can not get sms count!");
            } finally {
                cursor.close();
            }
        }
        buffer.append(res.getQuantityString(R.plurals.storage_dialog_sms, smsCount, new Object[]{Integer.valueOf(smsCount)}));
        buffer.append("\n");
        Bundle result = SqliteWrapper.call(context, MmsSms.CONTENT_URI, context.getPackageName(), "method_get_db_size", null, null);
        long dbsize = 0;
        if (result != null) {
            dbsize = result.getLong("db_size");
        }
        buffer.append(res.getString(R.string.storage_dialog_mms_database, new Object[]{getHumanReadableSize(context, dbsize)}));
        buffer.append("\n");
        long availSize = 0;
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            availSize = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        } catch (Exception e3) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Error while retrieving memory information");
        }
        buffer.append(res.getString(R.string.storage_dialog_available_mms_space, new Object[]{getHumanReadableSize(context, availSize)}));
        return buffer.toString();
        cursor.close();
    }

    public static String getHumanReadableSize(Context context, long size) {
        int unit;
        String tag;
        float fsize = (float) size;
        if (size < 1024) {
            unit = 17039497;
            tag = String.valueOf(size);
        } else if (size < 1048576) {
            unit = 17039498;
            tag = String.format("%.2f", new Object[]{Float.valueOf(fsize / 1024.0f)});
        } else if (size < 1073741824) {
            unit = 17039499;
            tag = String.format("%.2f", new Object[]{Float.valueOf(fsize / 1048576.0f)});
        } else {
            unit = 17039500;
            tag = String.format("%.2f", new Object[]{Float.valueOf(fsize / 1.07374182E9f)});
        }
        return tag + " " + context.getResources().getString(unit);
    }

    public static SpannableStringBuilder formatMessage(String body, int subId, Pattern highlight, String contentType, float scale) {
        return formatMessage(body, highlight, contentType, scale);
    }

    public static SpannableStringBuilder formatMessage(String body, Pattern highlight, String contentType, float scale) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        SmileyParser parser = SmileyParser.getInstance();
        if (!TextUtils.isEmpty(body)) {
            if (contentType == null || !"text/html".equals(contentType)) {
                buf.append(parser.addSmileySpans((CharSequence) body, SMILEY_TYPE.MESSAGE_TEXTVIEW, scale));
            } else {
                buf.append("\n");
                buf.append(Html.fromHtml(body));
            }
        }
        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(1), m.start(), m.end(), 0);
            }
        }
        return buf;
    }

    private static String getAddress(Context context, long uID) {
        if (uID <= 0) {
            return null;
        }
        try {
            String selection = "group_id = '" + String.valueOf(uID) + "'";
            Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), Sms.CONTENT_URI, new String[]{"address"}, selection, null, null);
            StringBuilder addesses = new StringBuilder();
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            if (!TextUtils.isEmpty(addesses)) {
                                addesses.append(";");
                                addesses.append(" ");
                            }
                            String addr = cursor.getString(0);
                            if (mHwCustMessageUtils != null) {
                                addr = mHwCustMessageUtils.getContactName(addr);
                            }
                            addesses.append(addr);
                        } while (cursor.moveToNext());
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return addesses.toString();
        } catch (SQLiteException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "SQLiteException e: " + e);
            return null;
        }
    }

    public static Uri smsAddMessageToUri(Context context, Uri uri, String address, String body, String subject, Long date, boolean read, boolean deliveryReport, long threadId, int subId, long groupId, String bodyAddressPos, String bodyTimePos) {
        ContentValues values = new ContentValues(10);
        values.put("sub_id", Integer.valueOf(subId));
        values.put("network_type", Integer.valueOf(getNetworkType(subId)));
        values.put("address", address);
        if (date != null) {
            values.put("date", date);
        }
        values.put("date_sent", read ? Integer.valueOf(1) : Integer.valueOf(0));
        values.put("subject", subject);
        values.put("body", body);
        if (deliveryReport) {
            values.put("status", Integer.valueOf(32));
        } else {
            values.put("status", Integer.valueOf(-1));
        }
        if (threadId != -1) {
            values.put("thread_id", Long.valueOf(threadId));
        }
        if (bodyAddressPos != null) {
            values.put("addr_body", bodyAddressPos);
        }
        if (bodyTimePos != null) {
            values.put("time_body", bodyTimePos);
        }
        String str = "group_id";
        if (groupId <= 0) {
            groupId = allocGroupId(context.getContentResolver());
        }
        values.put(str, Long.valueOf(groupId));
        return SqliteWrapper.insert(context, uri, values);
    }

    public static long allocGroupId(ContentResolver resolver) {
        try {
            return resolver.call(Sms.CONTENT_URI, "METHOD_ALLOC_GROUP_UID", String.valueOf(1), null).getLong("group_id");
        } catch (NullPointerException ex) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "getUid excetion: " + ex);
            return -1;
        } catch (IllegalArgumentException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "getUid excetion: " + e);
            return -1;
        }
    }

    public static boolean isCTCdmaCardInGsmMode() {
        if (!MmsConfig.isSupportCtInGsmMode()) {
            return false;
        }
        try {
            return 1 == getDsdsMode() && TelephonyManagerEx.isCTCdmaCardInGsmMode();
        } catch (Exception e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "isCTCdmaCardInGsmMode Exception");
        }
    }

    public static boolean isSmsOptimizationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_sms_optimization_characters", MmsConfig.getDefault7bitOptionValue());
    }

    public static boolean getRoamingDataEnabled(Context context, int sub) {
        Integer result = Integer.valueOf(0);
        if (sub == 1) {
            result = Integer.valueOf(Global.getInt(context.getContentResolver(), "data_roaming_sim2", 0));
        } else {
            result = Integer.valueOf(Global.getInt(context.getContentResolver(), "data_roaming", 0));
        }
        boolean isRoamingDataEnabled = result != null && 1 == result.intValue();
        if (isAirplanModeOn(context)) {
            return false;
        }
        return isRoamingDataEnabled;
    }

    public static void updateConvListInDeleteMode(Context context, long threadId, String address, Uri uri) {
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "incoming message, startConvListActivity");
        Intent intent = new Intent("com.huawei.android.conv.incomingmsg");
        intent.putExtra("START_TYPE", 1);
        intent.putExtra("thread_id", threadId);
        if (!TextUtils.isEmpty(address)) {
            intent.putExtra("address", address);
        } else if (uri != null) {
            intent.putExtra("address", AddressUtils.getFrom(context, uri));
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void setLayout(Context context, View view, int width, int height) {
        if (width == -1 || width == -2) {
            view.getLayoutParams().width = width;
        } else if (width > 0) {
            view.getLayoutParams().width = dipToPx(context, (float) width);
        }
        if (height == -1 || height == -2) {
            view.getLayoutParams().height = height;
        } else if (height > 0) {
            view.getLayoutParams().height = dipToPx(context, (float) height);
        }
    }

    public static void setMargin(Context context, View view, int start, int top, int end, int bottom) {
        MarginLayoutParams marginLayout = (MarginLayoutParams) view.getLayoutParams();
        if (start > 0) {
            marginLayout.setMarginStart(dipToPx(context, (float) start));
        }
        if (top > 0) {
            marginLayout.topMargin = dipToPx(context, (float) top);
        }
        if (end > 0) {
            marginLayout.setMarginEnd(dipToPx(context, (float) end));
        }
        if (bottom > 0) {
            marginLayout.bottomMargin = dipToPx(context, (float) bottom);
        }
    }

    public static void setPadding(Context context, View view, int start, int top, int end, int bottom) {
        view.setPaddingRelative(start < 0 ? view.getPaddingStart() : dipToPx(context, (float) start), top < 0 ? view.getPaddingTop() : dipToPx(context, (float) top), end < 0 ? view.getPaddingEnd() : dipToPx(context, (float) end), bottom < 0 ? view.getPaddingBottom() : dipToPx(context, (float) bottom));
    }

    public static void setTextSize(View view, float textSize, int unit) {
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(unit, textSize);
        }
    }

    public static int dipToPx(Context context, float dip) {
        return (int) ((dip * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(-1);
            try {
                retriever.release();
            } catch (RuntimeException e) {
            }
        } catch (RuntimeException e2) {
        } catch (Throwable th) {
            try {
                retriever.release();
            } catch (RuntimeException e3) {
            }
        }
        return bitmap;
    }

    public static boolean isNeedLayoutRtl(View v) {
        String language = Locale.getDefault().getLanguage();
        if ("ar".equals(language) || "fa".equals(language) || "iw".equals(language) || "ur".equals(language)) {
            return true;
        }
        try {
            Method method = Class.forName("android.view.View").getMethod("isLayoutRtl", new Class[0]);
            boolean result = false;
            if (v != null) {
                result = ((Boolean) method.invoke(v, new Object[0])).booleanValue();
            }
            return result;
        } catch (ClassNotFoundException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "isNeedLayoutRtl failed:ClassNotFoundException");
            return false;
        } catch (NoSuchMethodException e2) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "isNeedLayoutRtl failed:NoSuchMethodException");
            return false;
        } catch (IllegalAccessException e3) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "isNeedLayoutRtl failed:IllegalAccessException");
            return false;
        } catch (InvocationTargetException e4) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "isNeedLayoutRtl failed:InvocationTargetException");
            return false;
        } catch (NullPointerException e5) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "isNeedLayoutRtl failed, v is null");
            return false;
        }
    }

    public static boolean isNeedLayoutRtl() {
        return isNeedLayoutRtl(null);
    }

    protected static void goToConversationList(Activity act, boolean finishSelf, int folderType, boolean fromNotification) {
        if (finishSelf) {
            act.finish();
        }
        Intent intent = new Intent(act.getApplicationContext(), ConversationList.class);
        if (fromNotification) {
            intent.putExtra("fromNotification", true);
        }
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "goToConversationList , and fromNotification is " + fromNotification);
        act.startActivity(intent);
    }

    public static String getSelectedMessageBodies(Context context, Long[] selectedItems, Object listAdapter, MultiModeListView listView, int type) {
        StringBuffer msgsCopiedString = new StringBuffer();
        FavoritesListAdapter favoritesListAdapter = null;
        MessageListAdapter messageListAdapter = null;
        if (1 == type) {
            favoritesListAdapter = (FavoritesListAdapter) listAdapter;
            if (favoritesListAdapter == null || favoritesListAdapter.getCursor() == null) {
                MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "getSelectedMessageBodies::the favorite cursor is null, return");
                return "";
            }
        }
        messageListAdapter = (MessageListAdapter) listAdapter;
        if (messageListAdapter == null || messageListAdapter.getCursor() == null) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "getSelectedMessageBodies::the messagelist cursor is null, return");
            return "";
        }
        int count = selectedItems.length;
        if (count < 1) {
            MLog.v(HwCustUpdateUserBehaviorImpl.MMS, "getSelectedMessageBodies::the select item is 0, return");
            return "";
        }
        Long[] sortedItems = getSelectedItemsByPosition(type, selectedItems, listView);
        String msgType = "sms";
        for (int i = 0; i < count; i++) {
            if (sortedItems[i] != null) {
                MessageItem msgItem;
                long itemId = sortedItems[i].longValue();
                if (itemId < 0) {
                    itemId = -itemId;
                    msgType = "mms";
                } else {
                    msgType = "sms";
                }
                if (1 == type) {
                    msgItem = favoritesListAdapter.getCachedMessageItemWithIdAssigned(msgType, itemId, favoritesListAdapter.getCursor(), 1);
                } else if (mHwCust == null || !mHwCust.isRcsSwitchOn()) {
                    msgItem = messageListAdapter.getCachedMessageItemWithIdAssigned(msgType, itemId, messageListAdapter.getCursor());
                } else {
                    msgItem = mHwCust.getMsgItem(messageListAdapter, Long.valueOf(itemId));
                }
                if (msgItem != null) {
                    msgsCopiedString.append(getMsgText(context, msgItem));
                    if (i != count - 1) {
                        msgsCopiedString.append(System.lineSeparator());
                    }
                }
            } else {
                MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "getSelectedMessageBodies::the sorted message id is not valid!!");
            }
        }
        return msgsCopiedString.toString();
    }

    private static Long[] getSelectedItemsByPosition(int type, Long[] selectedItems, MultiModeListView listView) {
        if (hasMmsSelected(selectedItems)) {
            Long[] msgIdsByTime = new Long[selectedItems.length];
            int lenList = listView.getCount() - listView.getFooterViewsCount();
            int idx = listView.getHeaderViewsCount();
            int i = 0;
            while (idx < lenList) {
                int i2;
                long msgId = listView.getItemIdAtPosition(idx);
                if (listView.getRecorder().contains(msgId)) {
                    i2 = i + 1;
                    msgIdsByTime[i] = Long.valueOf(msgId);
                } else {
                    i2 = i;
                }
                if (i2 == selectedItems.length) {
                    break;
                }
                idx++;
                i = i2;
            }
            return msgIdsByTime;
        }
        Arrays.sort(selectedItems);
        return selectedItems;
    }

    private static boolean hasMmsSelected(Long[] msgIds) {
        for (Long id : msgIds) {
            if (id.longValue() < 0) {
                return true;
            }
        }
        return false;
    }

    public static String getMsgText(Context context, MessageItem msgItem) {
        if (msgItem == null) {
            return "";
        }
        if (msgItem.isRcsChat()) {
            if (msgItem.mBody != null) {
                return msgItem.mBody;
            }
            return "";
        } else if (!"sms".equals(msgItem.mType)) {
            StringBuffer sb = new StringBuffer();
            if (!TextUtils.isEmpty(msgItem.mSubject)) {
                sb.append(TextUtils.replace(context.getResources().getString(R.string.inline_subject_new), new String[]{"%s"}, new CharSequence[]{msgItem.mSubject}));
                sb.append(System.lineSeparator());
            }
            SlideshowModel slideshowModel = msgItem.getSlideshow();
            if (slideshowModel == null) {
                try {
                    slideshowModel = SlideshowModel.createFromMessageUri(context, msgItem.mMessageUri);
                } catch (MmsException e) {
                    MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "can't get slideshowModel and then can't get internal text");
                }
            }
            if (slideshowModel != null) {
                int slideListSize = slideshowModel.size();
                for (int i = 0; i < slideListSize; i++) {
                    SlideModel slide = slideshowModel.get(i);
                    if (slide != null) {
                        TextModel text = slide.getText();
                        if (text != null) {
                            sb.append(text.getText());
                            if (i != slideListSize - 1) {
                                sb.append(System.lineSeparator());
                            }
                        }
                    }
                }
            }
            return sb.toString();
        } else if (msgItem.mBody != null) {
            return msgItem.mBody;
        } else {
            return "";
        }
    }

    public static boolean msgHasText(Context context, MessageItem msgItem) {
        return !TextUtils.isEmpty(getMsgText(context, msgItem));
    }

    public static boolean msgsHaveText(Context context, Long[] msgIds, MessageListAdapter adapter, int loadType) {
        for (Long msgId : msgIds) {
            String type = "sms";
            Long msgId2;
            if (msgId2.longValue() < 0) {
                msgId2 = Long.valueOf(-msgId2.longValue());
                type = "mms";
            }
            if (msgHasText(context, adapter.getCachedMessageItemWithIdAssigned(type, msgId2.longValue(), adapter.getCursor(), loadType))) {
                return true;
            }
        }
        return false;
    }

    public static void viewMessageText(Context context, MessageItem msgItem) {
        if (msgItem != null) {
            viewText(context, createIntent(context, msgItem), null);
        }
    }

    public static void viewRcsMessageText(Context context, RcsGroupChatMessageItem msgItem, HwBaseFragment currentFragment) {
        if (msgItem != null && !TextUtils.isEmpty(msgItem.mBody)) {
            Intent intent = MmsAllActivity.createIntent(context, 1);
            intent.putExtra("msg_text", msgItem.mBody);
            intent.putExtra("name", context.getString(R.string.chat_topic_default));
            intent.putExtra(HarassNumberUtil.NUMBER, msgItem.mAddress);
            viewText(context, intent, currentFragment);
        }
    }

    public static void viewMessageText(Context context, MessageItem msgItem, HwBaseFragment currentFragment) {
        if (msgItem != null) {
            viewText(context, createIntent(context, msgItem), currentFragment);
        }
    }

    public static void viewText(Context context, Intent intent, HwBaseFragment currentFragment) {
        if (intent != null) {
            StatisticalHelper.incrementReportCount(context, 2111);
            String msgTextValue = intent.getStringExtra("msg_text");
            StatisticalHelper.reportEvent(context, 2112, String.valueOf(msgTextValue == null ? 0 : msgTextValue.length()));
            if (currentFragment == null) {
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "showCopyTextDialog start activity failed:: " + e);
                }
            } else {
                HwBaseFragment copyTextFragment = new CopyTextFragment();
                copyTextFragment.setIntent(intent);
                Activity activity = currentFragment.getActivity();
                if (HwMessageUtils.isSplitOn() && (activity instanceof ConversationList)) {
                    ((ConversationList) activity).changeRightAddToStack(copyTextFragment, currentFragment);
                }
            }
        }
    }

    public static void viewText(Context context, String text) {
        if (!TextUtils.isEmpty(text)) {
            Intent intent = MmsAllActivity.createIntent(context, 1);
            intent.putExtra("msg_text", text);
            viewText(context, intent, null);
        }
    }

    private static Intent createIntent(Context context, MessageItem msgItem) {
        if (TextUtils.isEmpty(getMsgText(context, msgItem))) {
            return null;
        }
        Intent intent = MmsAllActivity.createIntent(context, 1);
        intent.putExtra("msg_text", getMsgText(context, msgItem));
        intent.putExtra("name", msgItem.getName());
        intent.putExtra(HarassNumberUtil.NUMBER, msgItem.getNumber());
        intent.putExtra("size", msgItem.getReciSize());
        return intent;
    }

    public static int getAttachWidthAndHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.mms_attch_max_width_height);
    }

    public static byte[] encodeText(CharSequence text, int charset) {
        if (text == null) {
            return new byte[0];
        }
        if (charset == 0) {
            charset = 4;
            if (IS_CHINA_OPTB) {
                charset = 106;
            }
        }
        try {
            return text.toString().getBytes(CharacterSets.getMimeName(charset));
        } catch (UnsupportedEncodingException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Unsupported encoding: " + charset, (Throwable) e);
            return text.toString().getBytes(Charset.defaultCharset());
        }
    }

    public static void markAllAsRead(final Context context, int runningMode, Runnable afterMarkRunnable) {
        if (context != null) {
            Uri uri;
            String selection = "";
            if (runningMode == 5) {
                uri = Conversation.URI_HW_NOTIFICATIONS_MARK_AS_READ;
                selection = "read = 0 AND thread_id in (select _id from threads where number_type=" + 1 + ")";
            } else if (runningMode == 4) {
                uri = Conversation.URI_NOTIFICATIONS_MARK_AS_READ;
                selection = "read = 0 AND thread_id in (select _id from threads where number_type=" + 2 + ")";
            } else {
                uri = Conversation.URI_CONVERSATIONS_MARK_AS_READ;
                selection = "read = 0";
            }
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "in markAllAsRead(), runningMode = " + runningMode);
            final String targetSelection = selection;
            final Uri targetUri = uri;
            if (runningMode == 5 || runningMode == 4) {
                StatisticalHelper.incrementReportCount(context, 2147);
            } else {
                StatisticalHelper.incrementReportCount(context, 2148);
            }
            HwBackgroundLoader.getInst().postTask(new Runnable() {
                public void run() {
                    ContentResolver cr = context.getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put("read", Integer.valueOf(1));
                    values.put("seen", Integer.valueOf(1));
                    int affectedRows = cr.update(targetUri, values, targetSelection, null);
                    if (MessageUtils.mHwCust != null) {
                        MessageUtils.mHwCust.markOtherAsRead(cr, values);
                    }
                    MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "in markAllAsRead(), affectedRows = " + affectedRows);
                }
            });
            if (afterMarkRunnable != null) {
                afterMarkRunnable.run();
            }
        }
    }

    public static int startUsingNetworkFeature(ConnectivityManager cm, int type, String feature, int subid) {
        int value = 0;
        try {
            value = cm.startUsingNetworkFeature(0, feature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void stopUsingNetworkFeature(ConnectivityManager cm, int type, String feature, int subid) {
        try {
            cm.stopUsingNetworkFeature(0, feature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSimIdFromIntent(Intent intent, int defaultId) {
        int value = 0;
        try {
            value = MSimSmsManagerEx.getSimIdFromIntent(intent, defaultId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Intent setSimIdToIntent(Intent intent, int subId) {
        Intent retIntent = intent;
        try {
            retIntent = MSimSmsManagerEx.setSimIdToIntent(intent, subId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retIntent;
    }

    public static boolean isMTKPlatform() {
        boolean value = false;
        try {
            value = TelephonyManagerEx.isMTKPlatform();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String decodeByteArray(byte[] arr) {
        return decodeByteArray(arr, 0);
    }

    public static String decodeByteArray(byte[] arr, int charset) {
        String retStr = "";
        if (arr == null || arr.length == 0) {
            return retStr;
        }
        if (charset == 0) {
            charset = 4;
            if (IS_CHINA_OPTB) {
                charset = 106;
            }
        }
        try {
            retStr = new String(arr, CharacterSets.getMimeName(charset));
        } catch (UnsupportedEncodingException e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "Unsupported decodeByteArray: " + charset, (Throwable) e);
        }
        return retStr;
    }

    public static byte[] encodeText(CharSequence text) {
        return encodeText(text, 0);
    }

    public static String getGeocodedLocationFor(Context context, String phoneNumber) {
        try {
            return PhoneNumberOfflineGeocoder.getInstance().getDescriptionForNumber(PhoneNumberUtil.getInstance().parse(phoneNumber, MmsApp.getApplication().getCurrentCountryIso()), context.getResources().getConfiguration().locale);
        } catch (NumberParseException e) {
            return null;
        }
    }

    public static String encode(String val) {
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

    public static void gotoWeichatWithText(Context context, Uri uri, CharSequence text) {
        if (uri == null) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "gotoWeichatWithText cause null uri");
            return;
        }
        chatWithExactWeichatContact(context, uri);
        copyText(context, text);
    }

    public static void chatWithExactWeichatContact(Context context, Uri uri) {
        if (uri == null) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "gotoWeichat cause null uri");
            return;
        }
        try {
            context.startActivity(getRelatedContactChatIntent(new ComponentName("com.tencent.mm", "com.tencent.mm.plugin.accountsync.ui.ContactsSyncUI"), uri, "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile"));
        } catch (Exception e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "gotoWeichat cause exception" + e);
        }
    }

    public static void copyText(final Context context, final CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
                public void run() {
                    MessageUtils.copyToClipboard(context, text.toString());
                }
            }, 1000);
        }
    }

    public static void gotoWhatsappWithText(Context context, Uri uri, CharSequence text) {
        if (uri == null) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "gotoWhatsappWithText cause null uri");
            return;
        }
        chatWithExactWhatsappContact(context, uri);
        copyText(context, text);
    }

    public static void chatWithExactWhatsappContact(Context context, Uri uri) {
        if (uri == null) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "chatWithExactWhatsappContact cause null uri");
            return;
        }
        try {
            context.startActivity(getRelatedContactChatIntent(new ComponentName("com.whatsapp", "com.whatsapp.accountsync.ProfileActivity"), uri, "vnd.android.cursor.item/vnd.com.whatsapp.profile"));
        } catch (Exception e) {
            MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "chatWithExactWhatsappContact cause exception" + e);
        }
    }

    public static Intent getRelatedContactChatIntent(ComponentName component, Uri uri, String mimetype) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        if (component != null) {
            intent.setComponent(component);
        }
        intent.setType(mimetype);
        intent.setData(uri);
        intent.setFlags(67108864);
        return intent;
    }

    public static void gotoWeichat(Context context) {
        context.startActivity(getWeichatSendIntent(null));
    }

    public static Intent getWeichatSendIntent(String msg) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
        intent.setAction("android.intent.action.SEND");
        intent.setType("text/*");
        if (msg != null) {
            intent.putExtra("android.intent.extra.TEXT", msg);
        }
        intent.setFlags(67108864);
        return intent;
    }

    public static void forwardByChooser(Context context, Intent smsIntent, String forwardText, String chooserTitle) {
        forwardByChooser(context, smsIntent, forwardText, chooserTitle, -1);
    }

    public static void forwardByChooser(Context context, Intent smsIntent, String forwardText, String chooserTitle, int requestCode) {
        if (HwMessageUtils.isSplitOn() && (context instanceof ConversationList)) {
            Activity activity = (Activity) context;
            if (activity instanceof ConversationList) {
                HwBaseFragment fragment = new RightPaneComposeMessageFragment();
                fragment.setIntent(smsIntent);
                ((ConversationList) activity).openRightClearStack(fragment);
            }
        } else if (!(context instanceof Activity) || requestCode == -1) {
            context.startActivity(smsIntent);
        } else {
            ((Activity) context).startActivityForResult(smsIntent, requestCode);
        }
    }

    public static void copyToClipboard(Context context, String str) {
        ((ClipboardManager) context.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(null, str));
        Toast.makeText(context, R.string.toast_sms_copied_success_Toast, 1).show();
    }

    public static String correctForwardMsg(String forwardMsg) {
        if (forwardMsg == null || forwardMsg.length() <= System.lineSeparator().length() * 2) {
            return forwardMsg;
        }
        return forwardMsg.substring(0, forwardMsg.length() - (System.lineSeparator().length() * 2));
    }

    public static String getTextCount(CharSequence content) {
        CharSequence text = get7BitText(content);
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        int[] params = SmsMessage.calculateLength(text, false);
        int msgCount = params[0];
        int remainingInCurrentMessage = params[2];
        boolean showMsgCount = MmsConfig.getSmsTextCounterShowEnabled() || msgCount > 1;
        boolean showRemining = remainingInCurrentMessage <= 10;
        if (!showMsgCount && !showRemining) {
            return null;
        }
        String counterText;
        NumberFormat nf = NumberFormat.getIntegerInstance();
        if (showMsgCount) {
            counterText = nf.format((long) remainingInCurrentMessage) + " / " + nf.format((long) msgCount);
        } else {
            counterText = nf.format((long) remainingInCurrentMessage);
        }
        return counterText;
    }

    public static CharSequence get7BitText(CharSequence content) {
        CharSequence text7Bit;
        if (-1 == m7bitEnabled) {
            int i;
            if (MccMncConfig.is7bitEnable()) {
                i = 1;
            } else {
                i = 0;
            }
            m7bitEnabled = i;
        }
        if (m7bitEnabled == 1) {
            text7Bit = replaceAlphabetFor7Bit(content, 0, content.length());
        } else {
            text7Bit = content;
        }
        if (text7Bit == null) {
            return content;
        }
        return text7Bit;
    }

    public static void reset7BitEnabledValue() {
        m7bitEnabled = -1;
    }

    public static void set7bitsTableVenezuela() {
        MmsConfig.setSmsAllCharTo7Bit(-1);
        String alphabetForVenezuela = MmsConfig.getChar7bitVenezuela();
        String currentmccmnc = MmsApp.getDefaultTelephonyManager().getSimOperator();
        if (MccMncConfig.isValideOperator(currentmccmnc) && !TextUtils.isEmpty(alphabetForVenezuela) && currentmccmnc.startsWith(MmsConfig.getCustMccFor7bitMatchMap())) {
            set7bitsTableVenezuela(alphabetForVenezuela);
            MmsConfig.setHas7BitAlaphsetInHwDefaults(true);
            if (MmsConfig.getCustMccFor7bitMatchMap().equals("716")) {
                MmsConfig.setSmsAllCharTo7Bit(32);
            }
        }
    }

    public static boolean isMultiSimState() {
        return isMultiSimEnabled() && 1 == getIccCardStatus(0) && 1 == getIccCardStatus(1);
    }

    public static Intent getShowOrCreateContactIntent(String address) {
        if (address == null) {
            return null;
        }
        boolean isEmail = Contact.isEmailAddress(address);
        Intent addContactIntent = new Intent("com.android.contacts.action.SHOW_OR_CREATE_CONTACT", Uri.fromParts(isEmail ? "mailto" : "tel", address, null));
        if (isEmail) {
            addContactIntent.putExtra(Scopes.EMAIL, address);
        } else {
            addContactIntent.putExtra("phone", address);
            addContactIntent.putExtra("phone_type", 2);
        }
        addContactIntent.setFlags(524288);
        addContactIntent.putExtra("intent_key_is_from_dialpad", true);
        return addContactIntent;
    }

    public static int getWindowWidthPixels(Resources resources) {
        if (resources == null) {
            return 0;
        }
        return resources.getDisplayMetrics().widthPixels;
    }

    public static int getWindowShortPixels(Resources resources, Context context) {
        if (resources == null || context == null) {
            return 0;
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels <= dm.heightPixels ? dm.widthPixels : dm.heightPixels;
    }

    public static int getWindowHeightPixels(Resources resources) {
        if (resources == null) {
            return 0;
        }
        return resources.getDisplayMetrics().heightPixels;
    }

    public static void setAlwaysShowSmsOptimization(String mccmnc) {
        mIsAlwaysShowSmsOptimization = false;
        if (mHwCustMessageUtils != null) {
            mIsAlwaysShowSmsOptimization = mHwCustMessageUtils.isAlwaysShowSmsOptimization(mccmnc);
        }
    }

    public static boolean getIsAlwaysShowSmsOptimization() {
        return mIsAlwaysShowSmsOptimization;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static synchronized Calendar getCalendar() {
        Calendar instance;
        synchronized (MessageUtils.class) {
            instance = Calendar.getInstance(TimeZone.getDefault());
        }
        return instance;
    }

    public static int[] getImgWidthAndHeight(int width, int height, Context context) {
        if (width <= 0 || height <= 0) {
            return new int[]{width, height};
        }
        int widthPixels = getWindowShortPixels(context.getResources(), context);
        if (widthPixels == 0) {
            return new int[]{width, height};
        }
        int targetWidth = ((widthPixels - (((int) context.getResources().getDimension(R.dimen.message_block_margin_screen)) * 2)) - ((int) context.getResources().getDimension(R.dimen.checkbox_wapper_width))) + ((int) context.getResources().getDimension(R.dimen.avatar_view_message_list_item_margin_select));
        if ((context instanceof ConversationList) && ((ConversationList) context).isSplitState()) {
            targetWidth /= context.getResources().getInteger(R.integer.message_block_screen_layout_fix);
        }
        int targetHeight = (targetWidth / 3) * 4;
        return new int[]{targetWidth, targetHeight};
    }

    public static int[] getImgWidthAndHeightLimit(int width, int height, int maxWidth, int maxHeight, Context context) {
        if (width <= 0 || height <= 0) {
            return new int[]{width, height};
        }
        int targetWidth;
        int targetHeight;
        if (maxHeight <= 0 && maxWidth <= 0) {
            int widthPixels = getWindowShortPixels(context.getResources(), context);
            if (widthPixels == 0) {
                return new int[]{width, height};
            }
            targetWidth = ((widthPixels - ((int) context.getResources().getDimension(R.dimen.checkbox_wapper_width))) - (((int) context.getResources().getDimension(R.dimen.message_block_margin_screen)) * 2)) + ((int) context.getResources().getDimension(R.dimen.avatar_view_message_list_item_margin_select));
            if ((context instanceof ConversationList) && ((ConversationList) context).isSplitState()) {
                targetWidth /= context.getResources().getInteger(R.integer.message_block_screen_layout_fix);
            }
            targetHeight = (targetWidth / 3) * 4;
        } else if (maxHeight > 0 && maxWidth < 0) {
            targetHeight = maxHeight;
            targetWidth = width >= height ? (maxHeight / 3) * 4 : (maxHeight / 4) * 3;
        } else if (maxWidth <= 0 || maxHeight >= 0) {
            MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "getImgWidthAndHeightLimit don't support doule limit.");
            targetHeight = height;
            targetWidth = width;
        } else {
            targetWidth = maxWidth;
            targetHeight = width >= height ? (maxWidth / 4) * 3 : (maxWidth / 3) * 4;
        }
        return new int[]{targetWidth, targetHeight};
    }

    public static boolean getMultiSimState() {
        return isMultiSimEnabled() && 1 == getIccCardStatus(0) && 1 == getIccCardStatus(1);
    }

    public static boolean isEndWithImageExtension(String fileName) {
        if (TextUtils.isEmpty(fileName) || fileName.lastIndexOf(".") < 0) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(Locale.getDefault());
        for (String imageExtension : IMAGE_EXTENSIONS) {
            if (extension.endsWith(imageExtension)) {
                return true;
            }
        }
        return false;
    }

    public static void addFileToIndex(Context context, String imagePath) {
        if (!(context == null || TextUtils.isEmpty(imagePath))) {
            MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{imagePath}, null, null);
        }
    }

    public static void setWindowSystemUiVisibility(MediaPicker mediaPicker, boolean isDisplay) {
        if (mediaPicker != null && mediaPicker.isAdded()) {
            if (!(mediaPicker.getSystemUIVisibility() && isDisplay) && (mediaPicker.getSystemUIVisibility() || isDisplay)) {
                Window window = mediaPicker.getActivity().getWindow();
                if (isDisplay) {
                    window.clearFlags(Place.TYPE_SUBLOCALITY_LEVEL_2);
                } else {
                    window.addFlags(Place.TYPE_SUBLOCALITY_LEVEL_2);
                }
                mediaPicker.setSystemUIVisibility(isDisplay);
            }
        }
    }

    public static void setNavigationBarDefaultColor(MediaPicker mediaPicker, boolean isDefaultColor) {
        if (mediaPicker != null && mediaPicker.isAdded()) {
            Context context = mediaPicker.getActivity();
            Window window = mediaPicker.getActivity().getWindow();
            if (context != null && window != null) {
                if (isDefaultColor) {
                    window.setNavigationBarColor(context.getResources().getColor(R.color.navation_bar_white_color));
                } else {
                    window.setNavigationBarColor(context.getResources().getColor(R.color.navation_bar_black_color));
                }
            }
        }
    }

    public static void setIsMediaPanelInScrollingStatus(boolean isInScrollingStatus) {
        sIsMediaPanelInScrollingStatus = isInScrollingStatus;
    }

    public static boolean getIsMediaPanelInScrollingStatus() {
        return sIsMediaPanelInScrollingStatus;
    }

    public static String getLocationWebLink(Context context) {
        if (RcsMapLoaderFactory.isInChina(context)) {
            return "http://m.amap.com/?q=";
        }
        return "http://www.maps.google.com/maps?f=q&q=";
    }

    public static boolean isQcomPlatform() {
        return PLATFORM_PROP.startsWith("msm");
    }
}
