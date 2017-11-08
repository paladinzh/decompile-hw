package cn.com.xy.sms.sdk.ui.simplebubbleview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.ui.popu.simplepart.SimpleBubbleBottom;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.SdkCallBack;
import com.android.mms.ui.MessageItem;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressLint({"NewApi"})
public class DuoquSimpleBubbleViewManager {
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_ID = (byte) 1;
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_VALUE = (byte) 2;
    public static final String TAG = "DuoquSimpleBubbleViewManager";

    public static void getSimpleBubbleData(String msgIds, MessageItem messageItem, byte returnCacheType, HashMap<String, Object> extend, SdkCallBack callBack, boolean scrollFing) throws Exception {
        getSimpleBubbleData(msgIds, messageItem.mAddress, messageItem.mSmsServiceCenter, messageItem.mBody, messageItem.mDate, returnCacheType, extend, callBack, scrollFing);
    }

    public static void getSimpleBubbleData(String msgIds, String phoneNum, String smsCenterNum, String smsContent, long smsReceiveTime, byte returnCacheType, HashMap<String, Object> hashMap, SdkCallBack callBack, boolean scrollFing) throws Exception {
        ParseBubbleManager.queryDataByMsgItem(msgIds, phoneNum, smsContent, smsCenterNum, 1, smsReceiveTime, callBack, scrollFing);
    }

    public static View getBubbleView(Activity ctx, String msgIds, String phoneNum, String smsCenterNum, String smsContent, long smsReceiveTime, byte returnCacheType, ViewGroup parentView, HashMap<String, Object> extend) {
        try {
            JSONObject jsonObject = ParseBubbleManager.queryDataByMsgItem(msgIds, phoneNum, smsContent, smsCenterNum, 1, smsReceiveTime);
            if (jsonObject == null) {
                return null;
            }
            JSONArray cacheValue = jsonObject.optJSONArray("session_reuslt");
            if (cacheValue == null) {
                return null;
            }
            return getSimpleBubbleView(ctx, cacheValue, parentView, extend);
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquSimpleBubbleViewManager.getBubbleView error: " + e.getMessage(), e);
            return null;
        }
    }

    public static View getSimpleBubbleView(Activity ctx, JSONArray jsonArray, ViewGroup buttonGroup, HashMap<String, Object> extend) throws Exception {
        if (jsonArray == null || buttonGroup == null) {
            return null;
        }
        SimpleBubbleBottom simpleBubbleBottom = null;
        if (buttonGroup.getChildCount() > 0) {
            simpleBubbleBottom = (SimpleBubbleBottom) buttonGroup.getChildAt(0);
        }
        if (simpleBubbleBottom != null) {
            try {
                simpleBubbleBottom.setContent(jsonArray, extend);
            } catch (Exception e) {
                simpleBubbleBottom = null;
            }
        }
        if (simpleBubbleBottom == null) {
            simpleBubbleBottom = new SimpleBubbleBottom(ctx, jsonArray, extend);
            simpleBubbleBottom.setId(Integer.MAX_VALUE);
            buttonGroup.addView(simpleBubbleBottom);
        }
        return simpleBubbleBottom;
    }
}
