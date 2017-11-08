package cn.com.xy.sms.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.mms.ui.menu.ISmartSmsUIHolder;
import cn.com.xy.sms.sdk.ui.dialog.DialogActivity;
import cn.com.xy.sms.sdk.ui.dialog.SendSmsDialog;
import cn.com.xy.sms.sdk.ui.notification.DoActionActivity;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.UIConstant;
import cn.com.xy.sms.sdk.ui.popu.web.NearbyPointListFragment;
import cn.com.xy.sms.sdk.ui.popu.web.SdkWebFragment;
import cn.com.xy.sms.sdk.ui.settings.PermissionRequestActivity;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkParamUtil;
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.tmr.util.TMRManagerProxy;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmartSmsSdkDoAction extends AbsSdkDoAction {
    public static final long EIGHT_HOUR_MILLISECOND = 28800000;
    public static final int GET_PHONE_NUMBER = 1;
    public static final int GET_SIM_SERIAL_NUMBER = 2;
    public static final int GET_SUBSCRIBER_ID = 3;
    public static final String HUAWEI_LIFE_DOWN_URL = "http://x.bizport.cn/get_huawei_lifeapp?xy_key=6800766%E7%94%9F%E6%B4%BB%E6%9C%8D%E5%8A%A1";
    public static final String TAG = "XIAOYUAN";
    private static long mGetWifiTypeLastTime = 0;
    private static SoftReference<Object> mWifiManagerObj = null;
    private static int mWifiType = 0;

    private String getExtendParam(long type, JSONObject data, Map<String, String> extend) {
        if (data == null) {
            return "";
        }
        try {
            StringBuffer paramObj = new StringBuffer();
            String rechargeAmountKey = "rechargeAmount";
            String mobile = data.optString("mobile");
            String rechargeAmount = data.optString(rechargeAmountKey);
            if (TextUtils.isEmpty(mobile)) {
                String simIndex = data.optString("simIndex");
                if (!(TextUtils.isEmpty(simIndex) || extend == null || !extend.containsKey("simIndex"))) {
                    simIndex = (String) extend.get("simIndex");
                }
                int index = 0;
                if (simIndex != null) {
                    try {
                        index = Integer.parseInt(simIndex);
                        if (index == -1) {
                            index = 0;
                        }
                    } catch (Exception e) {
                        smartSdkExceptionLog("SmartSmsSdkDoAction.getExtendParam error: " + e.getMessage(), e);
                    }
                }
                mobile = MessageUtils.getLocalNumber(index);
                if (TextUtils.isEmpty(mobile)) {
                    mobile = MessageUtils.getLocalNumber(0);
                }
                if (TextUtils.isEmpty(mobile)) {
                    mobile = MessageUtils.getLocalNumber(1);
                }
                if (!TextUtils.isEmpty(mobile)) {
                    paramObj.append("&mobile=" + StringUtils.getPhoneNumberNo86(mobile));
                }
            } else {
                paramObj.append("&mobile=" + mobile);
            }
            if (!TextUtils.isEmpty(rechargeAmountKey)) {
                paramObj.append("&rechargeAmount=" + rechargeAmount);
            }
            return paramObj.toString();
        } catch (Exception e2) {
            smartSdkExceptionLog("SmartSmsSdkDoAction.getExtendParam error: " + e2.getMessage(), e2);
            return "";
        }
    }

    public int recharge(Context context, JSONObject data, Map<String, String> extend) {
        return hwLife(context, "100", getExtendParam(1, data, extend));
    }

    public int orderTraiffc(Context context, JSONObject data, Map<String, String> extend) {
        return hwLife(context, "101", getExtendParam(1, data, extend));
    }

    public int payWaterGas(Context context, JSONObject data, Map<String, String> map) {
        if (data == null) {
            return -1;
        }
        String paymentType = data.optString("paymentType");
        if (ContentUtil.WATER.equals(paymentType)) {
            return hwLife(context, "103", "");
        }
        if (ContentUtil.ELECTRIC.equals(paymentType)) {
            return hwLife(context, "105", "");
        }
        return hwLife(context, "104", "");
    }

    private int hwLife(Context context, String serviceType, String jsonExpandParam) {
        try {
            Intent intent;
            if (checkHasAppName(context, "com.huawei.lives")) {
                intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                if (TextUtils.isEmpty(jsonExpandParam)) {
                    jsonExpandParam = "";
                }
                intent.setData(Uri.parse("hwlives://externalapp/openwith?serviceId=" + serviceType + jsonExpandParam + "&source=xiaoyuan"));
                context.startActivity(intent);
                return 0;
            }
            intent = new Intent();
            intent.setClass(context, DialogActivity.class);
            intent.addFlags(131072);
            context.startActivity(intent);
            return 0;
        } catch (Exception e) {
            smartSdkExceptionLog("SmartSmsSdkDoAction.hwLife error: " + e.getMessage(), e);
            return -1;
        }
    }

    public String getContactName(Context context, String phoneNum) {
        return null;
    }

    public JSONObject getContactObj(Context context, String phoneNum) {
        if (context == null || StringUtils.isNull(phoneNum)) {
            return null;
        }
        try {
            Contact contact = Contact.get(phoneNum, true);
            JSONObject jsonObj;
            String harassName;
            if (contact != null) {
                jsonObj = new JSONObject();
                String contactName = contact.getName();
                if (StringUtils.isNull(contactName) || phoneNum.equals(contactName.replace(" ", ""))) {
                    harassName = HarassNumberUtil.queryHarassNameByNumber(context, phoneNum);
                    if (StringUtils.isNull(harassName)) {
                        jsonObj.put(UIConstant.CONTACT_TYPE, 1);
                        jsonObj.put(UIConstant.CONTACT_NAME, context.getString(R.string.duoqu_calls_reminder_stranger));
                    } else {
                        jsonObj.put(UIConstant.CONTACT_TYPE, 2);
                        jsonObj.put(UIConstant.CONTACT_NAME, harassName);
                    }
                } else {
                    jsonObj.put(UIConstant.CONTACT_TYPE, 0);
                    jsonObj.put(UIConstant.CONTACT_NAME, contactName);
                }
                return jsonObj;
            }
            jsonObj = new JSONObject();
            harassName = HarassNumberUtil.queryHarassNameByNumber(context, phoneNum);
            if (StringUtils.isNull(harassName)) {
                jsonObj.put(UIConstant.CONTACT_TYPE, 1);
                jsonObj.put(UIConstant.CONTACT_NAME, context.getString(R.string.duoqu_calls_reminder_stranger));
            } else {
                jsonObj.put(UIConstant.CONTACT_TYPE, 2);
                jsonObj.put(UIConstant.CONTACT_NAME, harassName);
            }
            return jsonObj;
        } catch (Exception e) {
            smartSdkExceptionLog("SmartSmsSdkDoAction.getContactObj error: " + e.getMessage(), e);
            return null;
        }
    }

    public void sendSms(Context context, String phoneNum, String sms, int simIndex, Map<String, String> map) {
        if (simIndex == -1) {
            simIndex = 0;
        }
        if (context != null) {
            if (!(context instanceof Activity) || SmartSmsSdkUtil.isSendSmsNoRemind(context)) {
                try {
                    new SmsMessageSender(context, new String[]{phoneNum}, sms, 0, simIndex).sendMessage(0);
                    forwardToComposeMessage(context, phoneNum);
                } catch (Exception e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("sendSms " + e.getMessage(), e);
                }
            } else {
                SendSmsDialog.show(context, phoneNum, sms, simIndex);
            }
        }
    }

    public static void forwardToComposeMessage(Context context, String phoneNum) {
        if (context != null && (context instanceof ISmartSmsUIHolder) && !((ISmartSmsUIHolder) context).equalMsgNumber(phoneNum)) {
            Intent intent = new Intent(context, ComposeMessageActivity.class);
            intent.putExtra("address", phoneNum);
            context.startActivity(intent);
        }
    }

    public void downLoadUrl(Context ctx, String url) {
        openAppByAppName(ctx, "com.huawei.appmarket");
    }

    public void downLoadApp(Context context, String appName, String url, Map<String, String> extend) {
        boolean runsuccess = false;
        try {
            if (!StringUtils.isNull(appName)) {
                if (checkHasAppName(context, appName)) {
                    openAppByAppName(context, appName);
                    runsuccess = true;
                } else {
                    String appMarketName = "com.huawei.appmarket";
                    if (checkHasAppName(context, appMarketName)) {
                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + appName));
                        intent.setPackage(appMarketName);
                        intent.addFlags(268435456);
                        context.startActivity(intent);
                        runsuccess = true;
                    }
                }
            }
        } catch (Throwable th) {
            runsuccess = false;
        }
        if (!runsuccess) {
            JSONObject jsobj = null;
            if (extend != null && extend.containsKey("menuName")) {
                jsobj = new JSONObject();
                try {
                    jsobj.put("menuName", extend.get("menuName"));
                } catch (JSONException ex) {
                    smartSdkExceptionLog("SmartSmsSdkDoAction.downLoadApp error: " + ex.getMessage(), ex);
                }
            }
            openUrl(context, url, jsobj);
        }
    }

    public void openAppByAppName(Context context, String appName, HashMap<String, String> hashMap) {
        try {
            Intent it = context.getPackageManager().getLaunchIntentForPackage(appName);
            if (it != null) {
                context.startActivity(it);
            } else {
                MLog.e("XIAOYUAN", "openAppByAppName faild, intent is null!");
            }
        } catch (Exception e) {
            smartSdkExceptionLog("SmartSmsSdkDoAction.openAppByAppName error: " + e.getMessage(), e);
        }
    }

    public void openAppByAppName(Context context, String appName, String appDownUrl) {
        try {
            if (checkHasAppName(context, appName)) {
                Intent it = context.getPackageManager().getLaunchIntentForPackage(appName);
                if (it != null) {
                    context.startActivity(it);
                    return;
                } else {
                    MLog.e("XIAOYUAN", "openAppByAppName faild, intent is null!");
                    return;
                }
            }
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + appName));
            intent.setPackage("com.huawei.appmarket");
            intent.addFlags(268435456);
            context.startActivity(intent);
        } catch (Exception e) {
            smartSdkExceptionLog("SmartSmsSdkDoAction.openAppByAppName error: " + e.getMessage(), e);
        }
    }

    public String getIccidBySimIndex(int index) {
        return getTelephonyData(index, 2);
    }

    public String getPhoneNumberBySimIndex(int index) {
        return getTelephonyData(index, 1);
    }

    private String getTelephonyData(int index, int type) {
        if (MessageUtils.isMultiSimEnabled()) {
            switch (type) {
                case 1:
                    try {
                        return MmsApp.getDefaultMSimTelephonyManager().getLine1Number(index);
                    } catch (Exception e) {
                        smartSdkExceptionLog("SmartSmsSdkDoAction.getTelephonyData error: " + e.getMessage(), e);
                        return null;
                    }
                case 2:
                    return MmsApp.getDefaultMSimTelephonyManager().getSimSerialNumber(index);
                case 3:
                    return MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(index);
                default:
                    return null;
            }
        }
        TelephonyManager tm = (TelephonyManager) Constant.getContext().getSystemService("phone");
        switch (type) {
            case 1:
                return tm.getLine1Number();
            case 2:
                return tm.getSimSerialNumber();
            case 3:
                return tm.getSubscriberId();
            default:
                return null;
        }
    }

    public void deleteMsgForDatabase(Context context, String arg1) {
    }

    public void markAsReadForDatabase(Context context, String arg1) {
    }

    public void openSms(Context context, String arg1, Map<String, String> map) {
    }

    public void callPhone(Context ac, String phoneNum, int simIndex) {
        HwMessageUtils.dialNumber("tel:" + phoneNum, (Activity) ac);
    }

    private int getLimited(int limit) {
        if (limit > 100) {
            return 100;
        }
        return limit;
    }

    public List<JSONObject> getReceiveMsgByReceiveTime(String phone, long startReceiveTime, long endReceiveTime, int limit) {
        Exception e;
        Throwable th;
        List<JSONObject> list = null;
        String[] projection = new String[]{"_id", "address", "body", "service_center", "date"};
        StringBuffer sbSelection = new StringBuffer(" date > ");
        sbSelection.append(startReceiveTime);
        sbSelection.append("  and date < ");
        sbSelection.append(endReceiveTime);
        String[] strArr = null;
        if (!StringUtils.isNull(phone)) {
            sbSelection.append(" and address = ?");
            strArr = new String[]{phone};
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(Constant.getContext(), Uri.parse("content://sms/inbox"), projection, sbSelection.toString(), strArr, "date desc LIMIT " + getLimited(limit) + " OFFSET 0");
            if (cursor != null && cursor.getCount() > 0) {
                List<JSONObject> jsonList = new ArrayList();
                while (cursor.moveToNext()) {
                    try {
                        JSONObject smsJson = new JSONObject();
                        smsJson.put("msgId", cursor.getString(0));
                        smsJson.put("phone", cursor.getString(1));
                        smsJson.put("msg", cursor.getString(2));
                        smsJson.put("centerNum", cursor.getString(3));
                        smsJson.put("smsReceiveTime", cursor.getString(4));
                        jsonList.add(smsJson);
                    } catch (Exception e2) {
                        e = e2;
                        list = jsonList;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                list = jsonList;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e3) {
            e = e3;
            try {
                smartSdkExceptionLog("SmartSmsSdkDoAction.getReceiveMsgByReceiveTime error: " + e.getMessage(), e);
                if (cursor != null) {
                    cursor.close();
                }
                return list;
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        return list;
    }

    public void openMap(Context context, String title, String address, double lon, double lat) {
        if (!StringUtils.isNull(address) || (lon > 0.0d && lat > 0.0d)) {
            String queryParam;
            address = address.replace(" ", ",").replace(context.getResources().getString(R.string.duoqu_left_brackets), ",").replace("(", ",").replace(context.getResources().getString(R.string.duoqu_dot), ",").replace(context.getResources().getString(R.string.duoqu_right_brackets), "").replace(")", "").replace("?", "").replace("&", "").replace("#", "").trim();
            if (StringUtils.isNull(address)) {
                queryParam = "geocoder?location=" + lat + "," + lon + "&coord_type=gcj02";
            } else if (lon == 0.0d || lat == 0.0d) {
                queryParam = "geocoder?address=" + address;
            } else {
                queryParam = "marker?location=" + lat + "," + lon + "&title=" + title + "&content=" + address;
            }
            try {
                String uriStr = "http://api.map.baidu.com/" + queryParam + "&output=html&src=xiaoyuan|" + context.getResources().getString(R.string.duoqu_tip_duoqu_name);
                JSONObject js = new JSONObject();
                js.put(Constant.URLS, uriStr);
                js.put(NumberInfo.TYPE_KEY, "WEB_URL");
                if (context instanceof ConversationList) {
                    callWebActivity(context, js, "WEB_URL", null, null);
                } else {
                    PopupUtil.startWebActivity(context, js, "WEB_URL", null);
                }
            } catch (Exception e) {
                smartSdkExceptionLog("SmartSmsSdkDoAction.openMap error: " + e.getMessage(), e);
            }
            return;
        }
        Toast.makeText(Constant.getContext(), context.getResources().getString(R.string.duoqu_tip_false), 0).show();
    }

    private static void setWifiType(int wifiType) {
        mWifiType = wifiType;
    }

    private static void setGetWifiTypeLastTime(long getWifiTypeLastTime) {
        mGetWifiTypeLastTime = getWifiTypeLastTime;
    }

    private static void setWifiManagerObj(SoftReference<Object> wifiManagerObj) {
        mWifiManagerObj = wifiManagerObj;
    }

    public int getWifiType(Context context) {
        if (context == null || System.currentTimeMillis() - mGetWifiTypeLastTime < 30000) {
            return mWifiType;
        }
        setGetWifiTypeLastTime(System.currentTimeMillis());
        boolean isMeteredWifi = false;
        try {
            synchronized (this) {
                Object obj = null;
                if (mWifiManagerObj != null) {
                    obj = mWifiManagerObj.get();
                }
                Class<?> clazz = Class.forName("com.huawei.android.net.wifi.WifiManagerCommonEx");
                Method method = clazz.getMethod("getHwMeteredHint", new Class[]{Context.class});
                if (obj == null) {
                    obj = clazz.newInstance();
                    setWifiManagerObj(new SoftReference(obj));
                }
                isMeteredWifi = ((Boolean) method.invoke(obj, new Object[]{context})).booleanValue();
            }
        } catch (RuntimeException e) {
            smartSdkExceptionLog("SmartSmsSdkDoAction getWifiType RuntimeException:" + e.getMessage(), e);
        } catch (Throwable e2) {
            smartSdkExceptionLog("SmartSmsSdkDoAction getWifiType error:" + e2.getMessage(), e2);
        }
        if (isMeteredWifi) {
            setWifiType(1);
            return 1;
        }
        setWifiType(0);
        return 0;
    }

    public void smartSdkExceptionLog(String msg, Throwable e) {
        SmartSmsSdkUtil.smartSdkExceptionLog(msg, e);
    }

    public void onEventCallback(int eventType, Map<String, Object> map) {
        switch (eventType) {
            case 0:
                SmartSmsPublicinfoUtil.reLoadContact();
                setDefaultAlgorithmVersion();
                return;
            default:
                return;
        }
    }

    private void setDefaultAlgorithmVersion() {
        try {
            if (StringUtils.isNull(SdkParamUtil.getParamValue(Constant.getContext(), Constant.SMART_ALGORITHM_PVER))) {
                SdkParamUtil.setParamValue(Constant.getContext(), Constant.SMART_ALGORITHM_PVER, "20151126101010");
            }
        } catch (Exception e) {
            smartSdkExceptionLog("SmartSmsSdkDoAction.setDefaultAlgorithmVersion error: " + e.getMessage(), e);
        }
    }

    public void getLocation(Context context, Handler handler) {
        XyLoactionManager.getLocation(context, handler);
    }

    public void nearSite(Context context, String address, String arg2, String arg3, Map<String, String> extend) {
        if (context != null && !TextUtils.isEmpty(address)) {
            boolean openFragement = false;
            if (context instanceof ConversationList) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("address", address);
                    if (extend != null && extend.containsKey("menuName")) {
                        object.put("menuName", extend.get("menuName"));
                    }
                    HwBaseFragment f = new NearbyPointListFragment();
                    f.loasList(object);
                    ((ConversationList) context).changeRightAddToStack(f);
                } catch (JSONException e) {
                }
                openFragement = true;
            }
            if (!openFragement) {
                try {
                    Intent intent = new Intent();
                    intent.setClass(context, PermissionRequestActivity.class);
                    intent.putExtra("address", address);
                    if (extend != null && extend.containsKey("menuName")) {
                        intent.putExtra("menuName", (String) extend.get("menuName"));
                    }
                    intent.putExtra("request_target_key", 1);
                    context.startActivity(intent);
                } catch (Throwable e2) {
                    smartSdkExceptionLog("SmartSmsSdkDoAction nearSite error: " + e2.getMessage(), e2);
                }
            }
        }
    }

    public void doRemind(Context context, String msgid, String title, String eventLocation, String description, String startTime, String endTime, String remind, Map<String, String> extend) {
        if (extend != null) {
            if (extend.containsKey(Constant.KEY_HW_PARSE_TIME)) {
                if (!StringUtils.isNull((String) extend.get(Constant.KEY_HW_PARSE_TIME))) {
                    try {
                        JSONArray hwParseTime = new JSONArray((String) extend.get(Constant.KEY_HW_PARSE_TIME));
                        if (hwParseTime.length() <= 1) {
                            super.doRemind(context, msgid, title, eventLocation, description, startTime, endTime, remind, extend);
                            finishDoActionActivity(context);
                            return;
                        }
                        showMeetingTimeDialog(context, msgid, title, eventLocation, description, hwParseTime, extend);
                        return;
                    } catch (Exception ex) {
                        super.doRemind(context, msgid, title, eventLocation, description, startTime, endTime, remind, extend);
                        finishDoActionActivity(context);
                        ex.printStackTrace();
                    }
                }
            }
        }
        super.doRemind(context, msgid, title, eventLocation, description, startTime, endTime, remind, extend);
    }

    @SuppressLint({"NewApi"})
    private void showMeetingTimeDialog(final Context context, String msgid, String title, String eventLocation, String description, JSONArray hwParseTime, Map<String, String> extend) throws Exception {
        String[] listItems = getTimeItems(context, hwParseTime);
        if (listItems == null || listItems.length == 0) {
            throw new Exception("showMeetingTimeDialog listItems is null");
        }
        final Context context2 = context;
        final String str = msgid;
        final String str2 = title;
        final String str3 = eventLocation;
        final String str4 = description;
        final JSONArray jSONArray = hwParseTime;
        final Map<String, String> map = extend;
        new Builder(context).setIconAttribute(16843605).setTitle(R.string.duoqu_time_choose).setSingleChoiceItems(listItems, 0, null).setPositiveButton(R.string.duoqu_confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SmartSmsSdkDoAction.this.doRemind(context2, str, str2, str3, str4, jSONArray.optJSONObject(((AlertDialog) dialog).getListView().getCheckedItemPosition()), map);
            }
        }).setNegativeButton(R.string.duoqu_cancel, null).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                SmartSmsSdkDoAction.this.finishDoActionActivity(context);
            }
        }).show();
    }

    private void finishDoActionActivity(Context context) {
        if (context != null && (context instanceof DoActionActivity)) {
            ((DoActionActivity) context).finish();
        }
    }

    private void doRemind(Context context, String msgid, String title, String eventLocation, String description, JSONObject dateTimeInfo, Map<String, String> extend) {
        if (dateTimeInfo != null) {
            super.doRemind(context, msgid, title, eventLocation, description, dateTimeInfo.optString("startTime"), dateTimeInfo.optString("endTime"), null, extend);
        }
    }

    private String[] getTimeItems(Context context, JSONArray timeArr) {
        if (timeArr == null || timeArr.length() == 0) {
            return new String[0];
        }
        try {
            String[] timeTimes = new String[timeArr.length()];
            SimpleDateFormat sdfMdHHmm = new SimpleDateFormat(context.getResources().getString(R.string.duoqu_date_format_date_time));
            SimpleDateFormat sdfHHmm = new SimpleDateFormat(context.getResources().getString(R.string.duoqu_date_format_time));
            for (int i = 0; i < timeTimes.length; i++) {
                JSONObject timeInfo = timeArr.getJSONObject(i);
                long startTimeMillis = timeInfo.optLong("startTime");
                long endTimeMillis = timeInfo.optLong("endTime");
                Date startTime = new Date(startTimeMillis);
                timeTimes[i] = sdfMdHHmm.format(startTime);
                if (endTimeMillis > 0) {
                    Date endTime = new Date(endTimeMillis);
                    if (endTime.getMonth() == startTime.getMonth() && endTime.getDate() == startTime.getDate()) {
                        timeTimes[i] = timeTimes[i] + " - " + sdfHHmm.format(endTime);
                    } else {
                        timeTimes[i] = timeTimes[i] + " - " + sdfMdHHmm.format(endTime);
                    }
                }
            }
            return timeTimes;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[0];
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public JSONArray getTimeSubInfo(String smsContent, long smsReceiveTime) {
        int[] timePos = HwMessageUtils.getTimePosition(smsContent);
        if (timePos == null) {
            return null;
        }
        int timeTotal = timePos[0];
        int length = smsContent.length();
        if ((timeTotal * 3) + 1 > timePos.length) {
            return null;
        }
        JSONArray timeSubArr = new JSONArray();
        int i = 0;
        JSONObject timeSubJson = null;
        while (i < timeTotal) {
            JSONObject timeSubJson2;
            int timeType = timePos[(i * 3) + 1];
            int timeBegin = timePos[(i * 3) + 2];
            int timeEnd = timePos[(i * 3) + 3] + 1;
            if (timeBegin >= length || timeEnd > length) {
                timeSubJson2 = timeSubJson;
            } else {
                Date[] date = null;
                try {
                    date = TMRManagerProxy.convertDate(smsContent.substring(timeBegin, timeEnd), smsReceiveTime);
                } catch (Exception e) {
                    MLog.e("XIAOYUAN", "Convert Date error.");
                } catch (NoSuchMethodError e2) {
                    e2.printStackTrace();
                }
                if (date != null) {
                    try {
                        if (date.length != 0) {
                            timeSubJson2 = new JSONObject();
                            switch (timeType) {
                                case 0:
                                case 2:
                                    try {
                                        timeSubJson2.put("startTime", String.valueOf(date[0].getTime()));
                                        break;
                                    } catch (Exception e3) {
                                        Exception ex = e3;
                                        break;
                                    }
                                case 1:
                                    timeSubJson2.put("startTime", String.valueOf(date[0].getTime() + EIGHT_HOUR_MILLISECOND));
                                    break;
                                case 3:
                                    timeSubJson2.put("startTime", String.valueOf(date[0].getTime()));
                                    if (date.length > 1) {
                                        timeSubJson2.put("endTime", String.valueOf(date[1].getTime()));
                                        break;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception e4) {
                        ex = e4;
                        timeSubJson2 = timeSubJson;
                    }
                }
                timeSubJson2 = timeSubJson;
            }
            i++;
            timeSubJson = timeSubJson2;
        }
        if (timeSubArr.length() > 0) {
            return timeSubArr;
        }
        timeSubJson2 = timeSubJson;
        return null;
        ex.printStackTrace();
        return null;
    }

    public boolean callWebActivity(Context context, JSONObject actionData, String actionType, String phoneNum, Map<String, String> extend) {
        try {
            if (actionType.startsWith("WEB_")) {
                putExtendParamToActionData(actionData, extend);
                AbsSdkDoAction.openStartWebActivity(context, actionData, actionType, phoneNum, extend);
                return true;
            }
            if (context instanceof ConversationList) {
                HwBaseFragment fragment = new SdkWebFragment();
                JSONObject object = new JSONObject();
                object.put("actionType", actionType);
                object.put("JSONDATA", actionData);
                fragment.loadWebViewUrl(object);
                ((ConversationList) context).changeRightAddToStack(fragment);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Drawable getDrawableByNumber(Context context, String classfyCode, Map<String, Object> map) {
        if (context == null || context.getResources() == null) {
            return null;
        }
        Resources resources = context.getResources();
        if (TextUtils.isEmpty(classfyCode)) {
            return resources.getDrawable(R.drawable.duoqu_ic_sms_notice);
        }
        Drawable drawable;
        if (classfyCode.startsWith("001")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_financial);
        } else if (classfyCode.startsWith("002")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_operator);
        } else if (classfyCode.startsWith("003")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_ecommerce);
        } else if (classfyCode.startsWith("004")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_insurance);
        } else if (classfyCode.startsWith("005")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_travel);
        } else if (classfyCode.startsWith("006")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_life);
        } else if (classfyCode.startsWith("007")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_entertainment);
        } else if (classfyCode.startsWith("008")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_traffic);
        } else if (classfyCode.startsWith("009")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_public_service);
        } else if (classfyCode.startsWith("010")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_beauty);
        } else if (classfyCode.startsWith("011")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_food);
        } else if (classfyCode.startsWith("012")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_shopping);
        } else if (classfyCode.startsWith("013")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_health);
        } else if (classfyCode.startsWith("014")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_sport);
        } else if (classfyCode.startsWith("015")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_organization);
        } else if (classfyCode.startsWith("016")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_education);
        } else if (classfyCode.startsWith("017")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_security);
        } else if (classfyCode.startsWith("018")) {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_enterprise_service);
        } else {
            drawable = resources.getDrawable(R.drawable.duoqu_ic_sms_notice);
        }
        return drawable;
    }

    public void toService(Context context, String actionType, JSONObject data) {
        if (!TextUtils.isEmpty(actionType) && data != null) {
            String appName;
            if (actionType.startsWith("taxi") && actionType.endsWith("Service")) {
                appName = data.optString("appName");
                if (TextUtils.isEmpty(appName) || !"com.huawei.lives".equalsIgnoreCase(appName)) {
                    super.toService(context, actionType, data);
                } else {
                    hwLife(context, "112", "");
                }
            } else if (actionType.startsWith("hotel") && actionType.endsWith("Service")) {
                appName = data.optString("appName");
                if (TextUtils.isEmpty(appName) || !"com.huawei.lives".equalsIgnoreCase(appName)) {
                    super.toService(context, actionType, data);
                } else {
                    hwLife(context, "109", "");
                }
            } else {
                super.toService(context, actionType, data);
            }
        }
    }

    public JSONObject getTelephonyInfoBySimIndex(int simIndex) {
        Exception e;
        JSONObject jSONObject = null;
        try {
            JSONObject json = new JSONObject();
            try {
                json.put("mid", getTelephonyData(simIndex, 3));
                json.put(IccidInfoManager.ICCID, getTelephonyData(simIndex, 2));
                return json;
            } catch (Exception e2) {
                e = e2;
                jSONObject = json;
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsSdkDoAction getTelephonyInfoBySimIndex error: " + e.getMessage(), e);
                return jSONObject;
            }
        } catch (Exception e3) {
            e = e3;
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsSdkDoAction getTelephonyInfoBySimIndex error: " + e.getMessage(), e);
            return jSONObject;
        }
    }

    private static void putExtendParamToActionData(JSONObject actionData, Map<String, String> extend) {
        if (actionData != null && extend != null) {
            try {
                if (!StringUtils.isNull((String) extend.get("checkString"))) {
                    actionData.put("checkString", String.valueOf(extend.get("checkString")));
                }
                if (!StringUtils.isNull((String) extend.get("msgKey"))) {
                    actionData.put("msgKey", String.valueOf(extend.get("msgKey")));
                }
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsSdkDoAction putExtendParamToActionData error: " + e.getMessage(), e);
            }
        }
    }

    public void statisticAction(String sceneId, String type, Map<String, Object> map) {
        if (!StringUtils.isNull(type) && !Constant.ACTION_PARSE.equals(type) && !ThemeUtil.SET_NULL_STR.equals(type)) {
            StatisticalHelper.reportEvent(MmsApp.getApplication(), 8002, sceneId + "," + type);
        }
    }
}
