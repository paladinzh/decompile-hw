package cn.com.xy.sms.sdk.ui.popu.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class BottomButtonUtil {
    public static final long BUTTON_TIME_CYC = 3600000;
    public static final String EMPTY_GROUP = "EMPTY_GROUP";
    public static final String TIME_EX = "TIME";

    public static JSONArray getActionArrayData(Context context, String adAction, BusinessSmsMessage message) {
        JSONArray jSONArray = null;
        try {
            if (!StringUtils.isNull(adAction)) {
                jSONArray = new JSONArray(adAction);
            }
            return jSONArray;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setButtonTextAndImg(TextView buttonText, String action, boolean disLogo) {
        try {
            int resLogoId = SimpleButtonUtil.bindButtonData(buttonText, action, StringUtils.isNull(buttonText.getText().toString()), true);
            if (!disLogo || resLogoId == -1) {
                buttonText.setCompoundDrawables(null, null, null, null);
            } else {
                buttonText.setCompoundDrawablesWithIntrinsicBounds(Constant.getContext().getResources().getDrawable(resLogoId), null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBotton(View button, TextView buttonText, JSONObject actionMap, boolean disLogo, final Activity mContext, final BusinessSmsMessage message) {
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(View v) {
                try {
                    JSONObject jsonObject = (JSONObject) v.getTag();
                    HashMap<String, String> valueMap = new HashMap();
                    valueMap.put("simIndex", message.simIndex + "");
                    valueMap.put("phoneNum", message.originatingAddress + "");
                    valueMap.put("content", message.getMessageBody() + "");
                    valueMap.put("viewType", message.viewType + "");
                    valueMap.put("msgId", message.getExtendParamValue("msgId") + "");
                    JsonUtil.putJsonToMap(jsonObject, valueMap);
                    DuoquUtils.doAction(mContext, (String) JsonUtil.getValueFromJsonObject(jsonObject, "action_data"), valueMap);
                } catch (Exception e) {
                    if (LogManager.debug) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (actionMap != null) {
            if (!disLogo) {
                buttonText.setCompoundDrawables(null, null, null, null);
            }
            String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
            String btnName = (String) JsonUtil.getValueFromJsonObject(actionMap, "btn_name");
            if (!StringUtils.isNull(btnName)) {
                buttonText.setText(btnName);
                setButtonTextAndImg(buttonText, action, disLogo);
            }
            if (!StringUtils.isNull(action)) {
                button.setTag(actionMap);
                button.setOnClickListener(onClickListener);
            }
        }
        ViewManger.setRippleDrawable(button);
    }

    public static JSONArray getAdAction(BusinessSmsMessage message, String groupKey, int count, Map<String, Object> extend) {
        if (message == null) {
            return null;
        }
        boolean isEmptyGroupKey = false;
        try {
            JSONArray cacheArray;
            if (StringUtils.isNull(groupKey)) {
                isEmptyGroupKey = true;
            }
            if (isEmptyGroupKey) {
                cacheArray = getAdActionFromCache(message, EMPTY_GROUP, extend);
            } else {
                cacheArray = getAdActionFromCache(message, groupKey, extend);
            }
            if (cacheArray != null) {
                return cacheArray;
            }
            String objAction = getAdAction(message, extend);
            if (StringUtils.isNull(objAction)) {
                return null;
            }
            JSONArray jsonArray = new JSONArray(objAction);
            if (jsonArray.length() <= 0) {
                return null;
            }
            JSONArray tempJsonArr = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject tempObject = getButtonItem(jsonArray.optJSONObject(i), groupKey, extend);
                if (tempObject != null) {
                    tempJsonArr.put(tempObject);
                }
                if (count > 0 && tempJsonArr.length() >= count) {
                    break;
                }
            }
            if (isEmptyGroupKey) {
                message.putValue(EMPTY_GROUP, tempJsonArr);
                message.putValue("EMPTY_GROUPTIME", Long.valueOf(System.currentTimeMillis()));
            } else {
                message.putValue(groupKey, tempJsonArr);
                message.putValue(groupKey + TIME_EX, Long.valueOf(System.currentTimeMillis()));
            }
            return tempJsonArr;
        } catch (Throwable e) {
            LogManager.e("XIAOYUAN", "getAdAction:" + e.getMessage(), e);
            return null;
        }
    }

    public static JSONObject getButtonItem(JSONObject tempObject, String groupKey, Map<String, Object> map) {
        if (tempObject == null) {
            return null;
        }
        String groupValue = tempObject.optString("groupValue");
        if ((StringUtils.isNull(groupKey) || StringUtils.isNull(groupValue) || groupKey.equals(groupValue)) && isBetweenTime(tempObject.optLong("sTime"), tempObject.optLong("eTime"))) {
            return tempObject;
        }
        return null;
    }

    private static boolean isBetweenTime(long startTime, long endTime) {
        boolean z = true;
        boolean z2 = false;
        if (startTime == 0 && endTime == 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (startTime == 0) {
            if (now >= endTime) {
                z = false;
            }
            return z;
        } else if (endTime == 0) {
            if (now < startTime) {
                z = false;
            }
            return z;
        } else {
            if (now >= startTime && now < endTime) {
                z2 = true;
            }
            return z2;
        }
    }

    public static String getAdAction(BusinessSmsMessage message, Map<String, Object> map) {
        if (message == null) {
            try {
                return "";
            } catch (Throwable th) {
                return "";
            }
        }
        Object newAction = "";
        newAction = message.getValue("NEW_ADACTION");
        if (newAction == null || StringUtils.isNull(newAction.toString())) {
            return (String) message.getValue("ADACTION");
        }
        return (String) newAction;
    }

    private static JSONArray getAdActionFromCache(BusinessSmsMessage message, String groupKey, Map<String, Object> map) {
        Object time = message.getValue(groupKey + TIME_EX);
        if (time == null) {
            return null;
        }
        long lastUpdateTime = ((Long) time).longValue();
        if (lastUpdateTime == 0 || System.currentTimeMillis() - 3600000 > lastUpdateTime) {
            return null;
        }
        return (JSONArray) message.getValue(groupKey);
    }

    public static String getFirstGroupValue(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return "";
        }
        int i = 0;
        while (i < jsonArray.length()) {
            try {
                JSONObject tempObject = jsonArray.optJSONObject(i);
                if (tempObject != null) {
                    String groupValue = tempObject.optString("groupValue");
                    if (!StringUtils.isNull(groupValue)) {
                        return groupValue;
                    }
                }
                i++;
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
            }
        }
        return "";
    }

    public static JSONArray getAdAction(JSONArray jsonArray, String groupKey, int count, Map<String, Object> extend) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        JSONArray tempJsonArr = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tempObject = getButtonItem(jsonArray.optJSONObject(i), groupKey, extend);
            if (tempObject != null) {
                tempJsonArr.put(tempObject);
            }
            if (count > 0 && tempJsonArr.length() >= count) {
                break;
            }
        }
        return tempJsonArr;
    }
}
