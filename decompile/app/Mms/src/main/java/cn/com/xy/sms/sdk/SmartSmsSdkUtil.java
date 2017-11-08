package cn.com.xy.sms.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewCache;
import cn.com.xy.sms.sdk.ui.dialog.EnhanceServiceDialog;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.ParseNotificationManager;
import cn.com.xy.sms.util.ParseRichBubbleManager;
import cn.com.xy.sms.util.ParseSmsToBubbleUtil;
import cn.com.xy.sms.util.SdkParamUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageItem;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class SmartSmsSdkUtil {
    private static final int BUBBLE_RICH = 2;
    public static final int DUOQU_BUBBLE_DATA_CACHE_SIZE = 200;
    public static final String DUOQU_SDK_CHANNEL = "7N4EhHawHUAWEI2";
    private static final int PARSE_MSG_TYPE_SIMPLE_AND_RICH = 3;
    public static final String RESET_SMART_SMS_ENHANCE = "reset_smart_sms_enhance";
    public static final String RESET_SMART_SMS_UPDATE_TYPE = "reset_smart_sms_update_type";
    public static final String SMARTSMS_BUBBLE = "smartsms_bubble";
    public static final String SMARTSMS_ENHANCE = "smartsms_enhance";
    public static final String SMARTSMS_HAS_SHOW_FIRST = "smartsms_has_show_first";
    private static final String SMARTSMS_INIT_SDK = "smartsms_init_sdk";
    public static final String SMARTSMS_NO_SHOW_AGAIN = "smartsms_no_show_again";
    private static final String SMARTSMS_SENDSMS_NO_REMIND = "smartsms_sendsms_no_remind";
    public static final String SMARTSMS_UPDATE_TYPE = "smartsms_update_type";
    public static final String TAG = "XIAOYUAN";
    private static final int UPDATE_TYPE_CLOSE = 0;
    private static final int UPDATE_TYPE_WALAN = 1;
    private static int mBubbleActivityResumeHashCode = -1;
    private static String mBubbleActivityResumePhoneNum = null;
    private static final LruCache<String, JSONObject> mBubbleDataCache = new LruCache(DUOQU_BUBBLE_DATA_CACHE_SIZE);
    private static boolean mHasInitParams = false;
    private static boolean mIsInitSdk;
    private static boolean mNoShowUpdateDialog = false;
    private static boolean mResetSmartSmsEnhance = false;
    private static boolean mResetSmartSmsUpdateType = false;
    private static boolean mSendSmsNoRemind;
    private static int mSettingSmartSmsBubble;
    private static boolean mSettingSmartSmsEnhance;
    private static boolean mSettingSmartSmsHasShowFirst;
    private static boolean mSettingSmartSmsNoShowAgain;
    private static boolean mSettingSmartSmsNoShowAgainTemp = false;
    private static int mSettingSmartSmsUpdateType;
    private static final LruCache<String, Map<String, Object>> mSmartNotifyResultMsg = new LruCache(10);
    private static String sUnclearCacheNumber;

    public static void init(Context context) {
        try {
            HashMap<String, String> extend = new HashMap();
            extend.put(Constant.ONLINE_UPDATE_SDK, "0");
            extend.put(Constant.SUPPORT_NETWORK_TYPE, "0");
            extend.put("smartsms_enhance", "false");
            extend.put(Constant.SECRETKEY, "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJd9YnU8PNarv+mLd1Fm48CZJ8P8+Wqws0tF0Iyskd50P/Bujd0n5fvtuzIeIkrivx6yGunYere3F48GNhn0KejJygCCnxPQpmrNCtK7BJKXbc3Jp07gMFkM7Si8TU87MG3+MY5FyzywkngBNs+zH9OZj4BgkBLL9uN8Ps3gSEdrAgMBAAECgYBVm/oEHqKS/kRaCwLG8cpLkUGztEaPUIRCSZXtqahVeoSXryJklKOXl2VukTD3+OPgyO4EsN3I7KNXpD72s9DqaB8uEb1G2mLrGvW4+fN+ofeJuqaOaPHazxh0MV/ewm+Cb2qBX00TdiBDbH/5CBJOj4uybB0QRHPBCRRaxPESwQJBAONNtlI0xT5ouN4kXPjqfYQXVxsxsyExJ73EZSPaBh0wu9Pt4k6Al3EWZq0cnXARJv29cgQmIKH0tBoI7j/kYskCQQCqnWzv8V35VL5q3yItn+0Owr0BPOS28wBPG+mLbWU/aRZJ6mqCQ9o6gn/HtL4NyF90rpGxqJhu79Dm6jpAEB6TAkAPUrPRwuDxHhooT4c8+IHRn8kteiI7QJcPQegXjKEQ4rImzUiORjjvVLVDQkSSw3U/cb/366ITiO2DO8rj37+RAkEAnBKcZ7ZFf+K8uejaTCBC68DKwwogMxeBzdw7vRbairIn+H/e5MELLYDZQSeev97vKz7R+lG+96SbVdEobhgQzwJBALsl6BmbMqL9qXp7bJaW9PjUZgEBtKOxbDjq2Nj+9b0LmfbU0Yc6PrXB9kLf5YuK+R9nhAvdt1piwbJMtlTs5Q4=");
            extend.put(Constant.RSAPRVKEY, "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCWgloZGkDjgPrcyV4HorO8f7+I6tU5+uyCEVhpuXfkLSogD/3z+sPQCiqKCiUR5hhgUKiVIhSAcEZ/b3N2Im/kWxDCJpakPrcfhM4dhe/22wZC0GpdLcgruXq6ooYLJptfbs/k9TZHt+bzyMdweVfqcCR9XI03GlDT2/uEJkU9iSDjh7yrNp+j5y7NCL8MIjOW5qsS0OVT38C13zPgdMwJ7DHDoqkbDTj0+uUe9noR3siOopZokBbPAJPOnsBdAGLXMrxcIt1JIUc5Scn6VtLvVUTO6UYHPal0r1/z5dGwR9Ub0oka3XeR++YbWbgUPZ1Y7QVDNU/lCu9gf3obE2ptAgMBAAECggEAXOedwf0IrOZC8+KA3mm6VNLuYg/DCwUu1KLsVmab8QdIB36XnfDeyh5cX9UfSMslpN5lEZl12kpz2dnsBbIAK49i6tVzC3ODCjuqF2ND2rccSEXqQhYuh374mRgVcfCk2+XFAmmy1dZZA4aeRXlBoX8TpyZoLOlbl0slGE+fUfnM7zojtY2YjSXhVaYrbNBz7iTIBUA8lKb7GMO8Uur1LP8XcV7Hj+zQCK3R+OvPlveNfkUjUv5C/4QnBNeUHrMCahPQLnOhzR6skJdBrGvKd7xzgI5ovKhzO5KdNmyzeFoI9WYy8vNkEAHU2jF6eIjN73aU2cvS2TgB/44R6a6/KQKBgQDhDahUGaBoptxAyM6J1y8kbhtCmqTe82Vofpx4G3l2L7HeL2HPdAnqK5eLFEb4vYSm/4lnBtoSdLQM8nhf3TF5Wj4NhsnjZo3B+GByvoGBFNzAvQm9nT/rprl4U9dNLaZBWRcHs+b5BZwpwYvr0C5xMFakmPdnpclkiFRpnLB/PwKBgQCrNJYFYgDYCSos27ewUlNbTxAX0q0IO1JW5rXchRwW4yyXcIXluNsjAvZ3C5+eRQfZZ/+5G3VOHYdMrCP90OCv647mIJc7KWkso3E69iNdpvDh8sB0Dk0SdnSdfYsbfm6wvBFgaZfeeYEHd1EAiVg/TlAOktLEYpMRHk8tJjSXUwKBgQCdC+/MCDoE7XPDjZ17WPsfu+ov90RYBJdebQP5WIrQ64V+m6fwoXQNwi+1MQg5qd64vpd+mqxwtpmycz2HAA73NP8aZ1XY5wbDNfTv/XrXvdwyb3gtpl8lram9ixwyUkmzTl0g1ey3F7Aa/2IJw7O07tt3bvsui+VWzz/Al0Qe9wKBgQCEDIc5l6F2/3qcJsvDH3xTjOOovEHhe3/CryfN8oz7yjR2ib98s9uxYXOf1kNC25N2SBShPWB446WbqZoJBraGeXU6YiDC7OnGGDoZdAdLEJvyaj2uRemEOAEyB8bnadkOT8BHUKpmWej/TTQY6dY63bg5xVzzQ+SS64G+HR2rKQKBgCquLXJTC8Dn9q18/rIdDp1mYceoAyYXwunO7Q6Z/q1sLRUnCIh96O4mTcaFZVyzA/4iXCUKhhD1XmBqBLCbUHp/RfQmP2aPK9G70ZUC+sU/oLngW2zrmHDzGBsUptqFzWSzOciXnDE38wY3XAWOZR/JitnjFWk58lW74EUVeeLg");
            Context context2 = context;
            ParseManager.initSdk(context2, DUOQU_SDK_CHANNEL, getICCID(context), true, true, extend);
            initSettingParam(context);
            ParseManager.setSdkDoAction(new SmartSmsSdkDoAction());
            Handler hd = new Handler() {
                public void handleMessage(Message msg) {
                    ParseSmsToBubbleUtil.beforeHandParseReceiveSms(VTMCDataCache.MAXSIZE, 3);
                }
            };
            hd.sendMessageDelayed(hd.obtainMessage(), 6000);
        } catch (Throwable e) {
            smartSdkExceptionLog("cn.com.xy.sms.sdk.SmartSmsSdkUtil.init error:" + e.getMessage(), e);
        }
    }

    public static String getICCID(Context context) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService("phone");
            if (!StringUtils.isNull(manager.getSimSerialNumber())) {
                return manager.getSimSerialNumber();
            }
        } catch (Throwable e) {
            smartSdkExceptionLog("cn.com.xy.sms.sdk.SmartSmsSdkUtil.getICCID error:" + e.getMessage(), e);
        }
        return "";
    }

    public static void clearCache(int acHashCode, String phoneNum) {
        try {
            if (acHashCode == mBubbleActivityResumeHashCode || phoneNum == null || !phoneNum.equals(mBubbleActivityResumePhoneNum)) {
                if (mBubbleDataCache != null) {
                    mBubbleDataCache.evictAll();
                }
                ParseBubbleManager.clearAllCache(phoneNum);
                ParseRichBubbleManager.clearCacheBubbleData(phoneNum);
                DuoquBubbleViewCache.clearCacheData(phoneNum);
            }
        } catch (Throwable e) {
            smartSdkExceptionLog("SmartSmsSdk " + e.getMessage(), e);
        }
    }

    public static void setBubbleActivityResumePhoneNum(int acHashCode, String bubblePhone) {
        mBubbleActivityResumePhoneNum = bubblePhone;
        mBubbleActivityResumeHashCode = acHashCode;
    }

    public static void putBubbleDataToCache(String key, JSONObject value) {
        if (key != null && value != null) {
            synchronized (mBubbleDataCache) {
                mBubbleDataCache.put(key, value);
            }
        }
    }

    public static JSONObject getBubbleDataFromCache(MessageItem messageItem) {
        String key = String.valueOf(messageItem.mMsgId) + String.valueOf(messageItem.mDate);
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        return (JSONObject) mBubbleDataCache.get(key);
    }

    public static JSONObject getBubbleDataFromCache(String key) {
        if (key == null) {
            return null;
        }
        return (JSONObject) mBubbleDataCache.get(key);
    }

    public static String getCheckString(String key) {
        if (key == null) {
            return null;
        }
        JSONObject json = getBubbleDataFromCache(key);
        if (json == null) {
            return null;
        }
        return json.optString("checkString");
    }

    public static void putCheckString(String key, String checkString) {
        try {
            JSONObject json = getBubbleDataFromCache(key);
            if (json != null) {
                json.put("checkString", checkString);
            }
        } catch (Exception e) {
            smartSdkExceptionLog("cn.com.xy.sms.sdk.SmartSmsSdkUtil.putCheckString error:" + e.getMessage(), e);
        }
    }

    public static Map<String, Object> parseMsg(String msgId, SmsMessage[] msgs) {
        try {
            String bodyText;
            ParseRichBubbleManager.deleteParseDataFromCache(msgId);
            mSmartNotifyResultMsg.remove(msgId);
            if (pduCount == 1) {
                bodyText = replaceFormFeeds(msgs[0].getDisplayMessageBody());
            } else {
                StringBuilder body = new StringBuilder();
                for (SmsMessage sms : msgs) {
                    if (sms.mWrappedSmsMessage != null) {
                        body.append(sms.getDisplayMessageBody());
                    }
                }
                bodyText = replaceFormFeeds(body.toString());
            }
            String phoneNum = msgs[0].getOriginatingAddress();
            String centerNum = msgs[0].getServiceCenterAddress();
            HashMap<String, String> extend = new HashMap();
            extend.put("HW_MEETING_WRAP", "true");
            extend.put("from", "1");
            Map<String, Object> res = ParseNotificationManager.parseNotificationMsg(Constant.getContext(), msgId, phoneNum, centerNum, bodyText, msgs[0].getTimestampMillis(), extend);
            if (res != null) {
                res.put("xy_notify_msg", bodyText);
                mSmartNotifyResultMsg.put(msgId, res);
            }
            return res;
        } catch (Throwable e) {
            smartSdkExceptionLog("cn.com.xy.sms.sdk.SmartSmsSdkUtil.parseMsg error:" + e.getMessage(), e);
            return null;
        }
    }

    public static Map<String, Object> getSmartNotifyResult(String msgId, String phoneNum, String msg, boolean isRemove) {
        Map<String, Object> res = (Map) mSmartNotifyResultMsg.get(msgId);
        if (res == null || msg == null || !msg.equals(res.get("xy_notify_msg"))) {
            return null;
        }
        if (isRemove) {
            mSmartNotifyResultMsg.remove(msgId);
        }
        return res;
    }

    public static void clearSmartNotifyResult(String msgId) {
        mSmartNotifyResultMsg.remove(msgId);
    }

    public static String formatPhoneNum(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        phoneNumber = phoneNumber.replaceAll(" ", "").replaceAll("-", "");
        if (phoneNumber.startsWith("86")) {
            phoneNumber = phoneNumber.substring(2, phoneNumber.length());
        } else if (phoneNumber.startsWith(StringUtils.MPLUG86)) {
            phoneNumber = phoneNumber.substring(3, phoneNumber.length());
        } else if (phoneNumber.startsWith("0086")) {
            phoneNumber = phoneNumber.substring(4, phoneNumber.length());
        }
        return phoneNumber;
    }

    private static String replaceFormFeeds(String s) {
        String str = "";
        if (s != null) {
            return s.replace('\f', '\n');
        }
        return str;
    }

    public static void restoreSettingParam(Context context) {
        setEnhance(context, false);
        setBubbleStyle(context, 2);
        setUpdateType(context, 0);
        setNoShowAgain(context, false);
    }

    public static void initSettingParam(Context context) {
        if (context != null) {
            try {
                getResetSmartSmsEnhance(context);
                getResetSmartSmsUpdateType(context);
                getEnhance(context);
                getBubbleStyle(context);
                getUpdateType(context);
                getNoShowAgain(context);
                getHasShowFirst(context);
                isSendSmsNoRemind(context);
                isInitSdk(context);
                mHasInitParams = true;
            } catch (Throwable e) {
                smartSdkExceptionLog("initSettingParam error: " + e.getMessage(), e);
            }
        }
    }

    public static boolean getEnhance(Context context) {
        if (mHasInitParams) {
            return mSettingSmartSmsEnhance;
        }
        String value = SdkParamUtil.getParamValue(context, "smartsms_enhance");
        if (StringUtils.isNull(value)) {
            mSettingSmartSmsEnhance = false;
        } else {
            mSettingSmartSmsEnhance = Boolean.parseBoolean(value);
        }
        return mSettingSmartSmsEnhance;
    }

    public static void setEnhance(Context context, boolean value) {
        setEnhance(context, value, false);
    }

    public static void setEnhance(Context context, boolean value, boolean resetSmartSmsEnhance) {
        setResetSmartSmsEnhance(context, resetSmartSmsEnhance);
        SdkParamUtil.setParamValue(context, "smartsms_enhance", String.valueOf(value));
        mSettingSmartSmsEnhance = value;
    }

    public static void resetEnhance(Context context) {
        if (getResetSmartSmsEnhance(context)) {
            setEnhance(context, false);
        }
    }

    private static void setResetSmartSmsEnhance(Context context, boolean value) {
        SdkParamUtil.setParamValue(context, RESET_SMART_SMS_ENHANCE, String.valueOf(value));
        mResetSmartSmsEnhance = value;
    }

    private static boolean getResetSmartSmsEnhance(Context context) {
        if (mHasInitParams) {
            return mResetSmartSmsEnhance;
        }
        String value = SdkParamUtil.getParamValue(context, RESET_SMART_SMS_ENHANCE);
        if (StringUtils.isNull(value)) {
            mResetSmartSmsEnhance = false;
        } else {
            mResetSmartSmsEnhance = Boolean.parseBoolean(value);
        }
        return mResetSmartSmsEnhance;
    }

    public static int getBubbleStyle(Context context) {
        if (mHasInitParams) {
            return mSettingSmartSmsBubble;
        }
        mSettingSmartSmsBubble = PreferenceManager.getDefaultSharedPreferences(context).getInt(SMARTSMS_BUBBLE, 2);
        return mSettingSmartSmsBubble;
    }

    public static void setBubbleStyle(Context context, int value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(SMARTSMS_BUBBLE, value);
        editor.commit();
        mSettingSmartSmsBubble = value;
    }

    public static int getUpdateType(Context context) {
        if (mHasInitParams) {
            return mSettingSmartSmsUpdateType;
        }
        String value = SdkParamUtil.getParamValue(context, Constant.SUPPORT_NETWORK_TYPE);
        if (StringUtils.isNull(value)) {
            mSettingSmartSmsUpdateType = 0;
        } else {
            mSettingSmartSmsUpdateType = Integer.parseInt(value);
        }
        return mSettingSmartSmsUpdateType;
    }

    public static void setUpdateType(Context context, int value) {
        setUpdateType(context, value, false);
    }

    public static void setUpdateType(Context context, int value, boolean resetSmartSmsUpdateType) {
        setResetSmartSmsUpdateType(context, resetSmartSmsUpdateType);
        SdkParamUtil.setParamValue(context, Constant.SUPPORT_NETWORK_TYPE, String.valueOf(value));
        mSettingSmartSmsUpdateType = value;
    }

    public static void resetUpdateType(Context context) {
        if (getResetSmartSmsUpdateType(context)) {
            setUpdateType(context, 0);
        }
    }

    private static void setResetSmartSmsUpdateType(Context context, boolean value) {
        SdkParamUtil.setParamValue(context, RESET_SMART_SMS_UPDATE_TYPE, String.valueOf(value));
        mResetSmartSmsUpdateType = value;
    }

    private static boolean getResetSmartSmsUpdateType(Context context) {
        if (mHasInitParams) {
            return mResetSmartSmsUpdateType;
        }
        String value = SdkParamUtil.getParamValue(context, RESET_SMART_SMS_UPDATE_TYPE);
        if (StringUtils.isNull(value)) {
            mResetSmartSmsUpdateType = false;
        } else {
            mResetSmartSmsUpdateType = Boolean.parseBoolean(value);
        }
        return mResetSmartSmsUpdateType;
    }

    public static boolean getNoShowAgain(Context context) {
        if (mHasInitParams) {
            return mSettingSmartSmsNoShowAgain;
        }
        mSettingSmartSmsNoShowAgain = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SMARTSMS_NO_SHOW_AGAIN, false);
        return mSettingSmartSmsNoShowAgain;
    }

    public static void setNoShowAgain(Context context, boolean value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(SMARTSMS_NO_SHOW_AGAIN, value);
        editor.commit();
        mSettingSmartSmsNoShowAgain = value;
    }

    public static boolean getHasShowFirst(Context context) {
        if (mHasInitParams) {
            return mSettingSmartSmsHasShowFirst;
        }
        mSettingSmartSmsHasShowFirst = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SMARTSMS_HAS_SHOW_FIRST, false);
        return mSettingSmartSmsHasShowFirst;
    }

    public static void setHasShowFirst(Context context, boolean value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(SMARTSMS_HAS_SHOW_FIRST, value);
        editor.commit();
        mSettingSmartSmsHasShowFirst = value;
    }

    public static boolean isSendSmsNoRemind(Context context) {
        if (mHasInitParams) {
            return mSendSmsNoRemind;
        }
        mSendSmsNoRemind = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SMARTSMS_SENDSMS_NO_REMIND, false);
        return mSendSmsNoRemind;
    }

    public static void setSendSmsNoRemind(Context context, boolean value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(SMARTSMS_SENDSMS_NO_REMIND, value);
        editor.commit();
        mSendSmsNoRemind = value;
    }

    public static boolean isInitSdk(Context context) {
        if (mHasInitParams) {
            return mIsInitSdk;
        }
        mIsInitSdk = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SMARTSMS_INIT_SDK, false);
        return mIsInitSdk;
    }

    public static void setIsInitSdk(Context context, boolean value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(SMARTSMS_INIT_SDK, value);
        editor.commit();
        mIsInitSdk = value;
    }

    public static boolean isNoShowEnhanceDialog() {
        return mSettingSmartSmsNoShowAgainTemp;
    }

    public static void setNoShowEnhanceDialog(boolean value) {
        mSettingSmartSmsNoShowAgainTemp = value;
    }

    public static void showEnhanceServiceDialog(final Context ctx, final OnDismissListener onDismissListener) {
        try {
            new AsyncTask() {
                protected Object doInBackground(Object... arg0) {
                    try {
                        if (!MmsConfig.getSupportSmartSmsFeature() || SmartSmsSdkUtil.isNoShowEnhanceDialog() || SmartSmsSdkUtil.getNoShowAgain(ctx)) {
                            return null;
                        }
                        SmartSmsSdkUtil.resetEnhance(ctx);
                        SmartSmsSdkUtil.resetUpdateType(ctx);
                        if (SmartSmsSdkUtil.getEnhance(ctx)) {
                            return null;
                        }
                        return Boolean.valueOf(true);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsSDKUtil showEnhanceServiceDialog doInBackground", e);
                        return null;
                    }
                }

                protected void onPostExecute(Object result) {
                    if (result != null) {
                        try {
                            EnhanceServiceDialog.show(ctx, onDismissListener);
                        } catch (Throwable e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsSDKUtil showEnhanceServiceDialog onPostExecute", e);
                        }
                    }
                }
            }.execute(new Object[0]);
        } catch (Throwable e) {
            smartSdkExceptionLog("SmartSmsSDKUtil showEnhanceServiceDialog", e);
        }
    }

    public static boolean isNoShowUpdateDialog() {
        return mNoShowUpdateDialog;
    }

    public static void setNoShowUpdateDialog(boolean value) {
        mNoShowUpdateDialog = value;
    }

    public static void showSmartsmsDialogs(final Context context, final OnDismissListener onDismissListener) {
        if (context != null) {
            if (isInitSdk(context)) {
                showEnhanceServiceDialog(context, onDismissListener);
            } else {
                Handler myHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        SmartSmsSdkUtil.setIsInitSdk(context, true);
                        SmartSmsSdkUtil.showEnhanceServiceDialog(context, onDismissListener);
                    }
                };
                myHandler.sendMessageDelayed(myHandler.obtainMessage(), 500);
            }
        }
    }

    public static void smartSdkExceptionLog(String msg, Throwable e) {
        if (msg != null) {
            if (e == null) {
                MLog.e("XIAOYUAN", msg);
            } else {
                MLog.e("XIAOYUAN", msg, e);
            }
        }
    }

    public static void clearSmartSmsCacheData(List<Long> msgIds) {
        if (msgIds != null && msgIds.size() != 0) {
            try {
                for (Long msgId : msgIds) {
                    ParseRichBubbleManager.deleteBubbleDataFromCache("", String.valueOf(msgId));
                }
            } catch (Throwable e) {
                smartSdkExceptionLog("SmartSmsSdkUtil isEnterpriseSms: " + e.getMessage(), e);
            }
        }
    }

    public static int parseSmsType(Context context, String number, String smsContent, String smsCenterNum, Map<String, String> extend, int judgeType) {
        try {
            return ParseManager.parseSmsType(context, number, smsContent, smsCenterNum, extend, judgeType);
        } catch (Throwable e) {
            smartSdkExceptionLog("SmartSmsSdkUtil parseSmsType: " + e.getMessage(), e);
            return -1;
        }
    }

    public static boolean isEnterpriseSms(Context context, String phoneNumber, String smsContent, Map<String, String> extend) {
        try {
            return ParseManager.isEnterpriseSms(context, phoneNumber, smsContent, extend);
        } catch (Throwable e) {
            smartSdkExceptionLog("SmartSmsSdkUtil isEnterpriseSms: " + e.getMessage(), e);
            return false;
        }
    }

    public static void setThemeMode(int themeMode) {
        if (Constant.getContext() != null) {
            ContentUtil.setThemeMode(themeMode);
        }
    }

    public static void resetSmartSmsState() {
        ThreadEx.execute(new Runnable() {
            public void run() {
                SmartSmsSdkUtil.resetEnhance(MmsApp.getApplication());
                SmartSmsSdkUtil.resetUpdateType(MmsApp.getApplication());
            }
        });
    }

    public static void setUnclearCacheNumber(String unclearCacheNumber) {
        sUnclearCacheNumber = unclearCacheNumber;
    }

    public static String getUnclearCacheNumber() {
        return sUnclearCacheNumber;
    }

    public static boolean activityIsFinish(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return true;
        }
        return false;
    }

    public static void setSmartSmsFunctionTipsOpen(Context context) {
        int autoUpdateType = getUpdateType(context);
        if (autoUpdateType == 0) {
            autoUpdateType = 1;
        }
        setEnhance(context, true, false);
        setNoShowAgain(context, true);
        setUpdateType(context, autoUpdateType, false);
        DuoquUtils.getSdkDoAction().simChange();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_key_risk_url_check", true).putBoolean(SMARTSMS_NO_SHOW_AGAIN, true).apply();
    }

    public static void initContext(Context ctx) {
        Constant.initContext(ctx);
    }
}
