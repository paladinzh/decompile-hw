package com.android.mms.ui;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.text.TextUtils;
import android.util.Pair;
import cn.com.xy.sms.sdk.SmartSmsPublicinfoUtil;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.notification.BaseNotificationView;
import cn.com.xy.sms.sdk.ui.notification.DoActionActivity;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class SmartNewNotificationManager {
    private static int mRequestBtnClick = 100000;
    private static int mRequestLayoutClick = 300000;

    public static Map<String, Object> getSmartSmsResult(String msgId, String phoneNum, String msg, boolean isRemove, Map<String, Object> map) {
        return SmartSmsSdkUtil.getSmartNotifyResult(msgId, phoneNum, msg, isRemove);
    }

    public static boolean bindSmartNotifyView(Context context, Builder nBuilder, Map<String, Object> smartResultMap, String msgId, String phoneNum, String msg, Map<String, Object> extend) {
        if (smartResultMap == null || nBuilder == null) {
            return false;
        }
        try {
            Pair<String, String> titleAndContentPair = getTitleAndContentPair(smartResultMap, phoneNum, msg);
            nBuilder.setContentTitle((CharSequence) titleAndContentPair.first);
            nBuilder.setContentText((CharSequence) titleAndContentPair.second);
            bindAction(context, nBuilder, smartResultMap, msgId, phoneNum, msg, extend);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartNewNotificationManager.bindSmartNotifyView error: " + e.getMessage(), e);
        }
        return true;
    }

    public static boolean bindDropSmartNotifyView(Context context, NotificationCompat.Builder nBuilder, Map<String, Object> smartResultMap, String msgId, String phoneNum, String msg, Map<String, Object> extend) {
        if (smartResultMap == null || nBuilder == null) {
            return false;
        }
        try {
            Pair<String, String> titleAndContentPair = getTitleAndContentPair(smartResultMap, phoneNum, msg);
            nBuilder.setContentText((CharSequence) titleAndContentPair.second);
            nBuilder.setContentTitle((CharSequence) titleAndContentPair.first);
            bindDropAction(context, nBuilder, smartResultMap, msgId, phoneNum, msg, extend);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartNewNotificationManager.bindDropSmartNotifyView error: " + e.getMessage(), e);
        }
        return true;
    }

    private static Pair<String, String> getTitleAndContentPair(Map<String, Object> smartResultMap, String phoneNum, String msg) {
        String title = (String) smartResultMap.get("view_content_title");
        String text = (String) smartResultMap.get("view_content_text");
        if (isVerificationCode(smartResultMap)) {
            title = getVerificationTitle(phoneNum, title);
        } else {
            title = getOtherTitle(phoneNum, title);
        }
        if (StringUtils.isNull(title) || title.equals("NO_TITLE")) {
            title = phoneNum;
        }
        if (StringUtils.isNull(text)) {
            text = msg.trim();
        }
        return new Pair(title, text);
    }

    private static void bindDropAction(Context context, NotificationCompat.Builder nBuilder, Map<String, Object> smartResultMap, String msgId, String phoneNum, String msg, Map<String, Object> map) {
        if (nBuilder != null) {
            JSONArray actionArr = getActionData(smartResultMap);
            if (actionArr != null) {
                int len = actionArr.length();
                for (int i = 0; i < len; i++) {
                    nBuilder.addAction(createDropAction(context, getRequestCode(2), actionArr.optJSONObject(i), msgId, (String) smartResultMap.get("threadId")));
                }
            }
        }
    }

    private static Action createDropAction(Context ctx, int requestCode, JSONObject actionData, String msgId, String threadId) {
        return new Action.Builder(0, BaseNotificationView.getButtonName(actionData), getNotifyActionIntent(ctx, requestCode, actionData.optString("action_data"), actionData.optString("action"), actionData.optString(Constant.KEY_HW_PARSE_TIME), msgId, threadId)).build();
    }

    private static void bindAction(Context context, Builder nBuilder, Map<String, Object> mSmartResultMap, String msgId, String phoneNum, String msg, Map<String, Object> map) {
        if (nBuilder != null) {
            JSONArray actionArr = getActionData(mSmartResultMap);
            if (actionArr != null) {
                int len = actionArr.length();
                for (int i = 0; i < len; i++) {
                    nBuilder.addAction(createAction(context, getRequestCode(2), actionArr.optJSONObject(i), msgId, (String) mSmartResultMap.get("threadId")));
                }
            }
        }
    }

    private static Notification.Action createAction(Context ctx, int requestCode, JSONObject actionData, String msgId, String threadId) {
        return new Notification.Action.Builder(0, BaseNotificationView.getButtonName(actionData), getNotifyActionIntent(ctx, requestCode, actionData.optString("action_data"), actionData.optString("action"), actionData.optString(Constant.KEY_HW_PARSE_TIME), msgId, threadId)).build();
    }

    private static JSONArray getActionData(Map<String, Object> smartResultMap) {
        try {
            String adAction = (String) smartResultMap.get("ADACTION");
            if (!StringUtils.isNull(adAction)) {
                return new JSONArray(adAction);
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager.getButtonName ERROR: " + e.getMessage(), e);
        }
        return null;
    }

    private static PendingIntent getNotifyActionIntent(Context context, int id, String actionData, String actionType, String hwParseTime, String msgId, String threadId) {
        if (StringUtils.isNull(actionData)) {
            return null;
        }
        Intent contentIntent = new Intent();
        contentIntent.setClassName(context, DoActionActivity.class.getName());
        contentIntent.putExtra("thread_id", threadId);
        contentIntent.putExtra("action_data", actionData);
        contentIntent.putExtra("action_type", actionType);
        contentIntent.putExtra("msgId", msgId);
        contentIntent.putExtra(Constant.KEY_HW_PARSE_TIME, hwParseTime);
        contentIntent.addFlags(268566528);
        return PendingIntent.getActivityAsUser(context, id, contentIntent, 134217728, null, UserHandle.CURRENT_OR_SELF);
    }

    private static synchronized int getRequestCode(int requestType) {
        int res;
        synchronized (SmartNewNotificationManager.class) {
            res = 0;
            if (1 == requestType) {
                if (mRequestLayoutClick == 399999) {
                    mRequestLayoutClick = 300000;
                } else {
                    mRequestLayoutClick++;
                }
                res = mRequestLayoutClick;
            } else if (2 == requestType) {
                if (mRequestBtnClick == 299999) {
                    mRequestBtnClick = 200000;
                } else {
                    mRequestBtnClick++;
                }
                res = mRequestBtnClick;
            }
        }
        return res;
    }

    public static String getVerificationTitle(String phoneNum, String title) {
        String name = SmartSmsPublicinfoUtil.getName(phoneNum);
        if (TextUtils.isEmpty(name) || StringUtils.isNumber(name)) {
            return phoneNum + ContentUtil.LEFTBREAK + title + ContentUtil.RIGHTBREAK;
        }
        return name + title;
    }

    public static String getOtherTitle(String phoneNum, String title) {
        String newTitle = SmartSmsPublicinfoUtil.getName(phoneNum);
        if (TextUtils.isEmpty(newTitle)) {
            return title;
        }
        return newTitle;
    }

    public static boolean isVerificationCode(Map<String, Object> smartResultMap) {
        if (smartResultMap == null) {
            return false;
        }
        return "1".equals(String.valueOf(smartResultMap.get("Is_verification_code")));
    }
}
