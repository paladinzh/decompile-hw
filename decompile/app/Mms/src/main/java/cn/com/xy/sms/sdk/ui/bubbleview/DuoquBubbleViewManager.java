package cn.com.xy.sms.sdk.ui.bubbleview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import cn.com.xy.sms.sdk.ISmartSmsEvent;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.popupview.BubblePopupView;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.ParseRichBubbleManager;
import cn.com.xy.sms.util.SdkCallBack;
import com.android.mms.ui.MessageItem;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressLint({"NewApi"})
public class DuoquBubbleViewManager {
    public static final int DUOQU_BUBBLE_VIEW_ID = 999999999;
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_ID = (byte) 1;
    public static final byte DUOQU_RETURN_CACHE_SDK_MSG_VALUE = (byte) 2;
    private static final String DUOQU_VIEW_ID = "View_fdes";
    static final String TAG = "XIAOYUAN";

    public static Map<String, Object> parseMsgToBubbleCardResult(Context ctx, String msgId, String phoneNum, String smsCenterNum, String smsContent, long smsReceiveTime, byte returnCacheType, HashMap<String, String> extend) {
        try {
            return ParseManager.parseMsgToBubbleCardResult(ctx, msgId, phoneNum, smsCenterNum, smsContent, smsReceiveTime, returnCacheType, extend);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getRichBubbleData(Activity ctx, String msgIds, MessageItem messageItem, byte returnCacheType, View itemView, ViewGroup parentView, ViewGroup richItemGroup, AdapterView adViews, SdkCallBack xyCallBack, boolean scrollFing) {
        return getRichBubbleData(ctx, msgIds, messageItem.mAddress, messageItem.mSmsServiceCenter, messageItem.mBody, messageItem.mDate, returnCacheType, itemView, parentView, richItemGroup, adViews, messageItem.getExtendMap(), xyCallBack, scrollFing);
    }

    public static JSONObject getRichBubbleData(Activity ctx, String msgIds, String phoneNum, String smsCenterNum, String smsContent, long smsReceiveTime, byte returnCacheType, View itemView, ViewGroup parentView, ViewGroup richItemGroup, AdapterView adViews, HashMap<String, Object> extend, SdkCallBack xyCallBack, boolean scrollFing) {
        if (StringUtils.isNull(msgIds)) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-1), null, msgIds);
            return null;
        }
        try {
            ParseRichBubbleManager.queryDataByMsgItem(msgIds, phoneNum, smsContent, smsReceiveTime, smsCenterNum, 2, xyCallBack, scrollFing, extend);
        } catch (Exception e) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-1), null, msgIds);
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquBubbleViewManager.getRichBubbleData error: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private static View getBubblePopupView(Activity ctx, BusinessSmsMessage msg, String viewId, View itemView, ViewGroup apView, DuoquBubbleViewCache bubbleViewCache, ISmartSmsEvent smartSmsEvent) throws Exception {
        BubblePopupView bubblePopupView = null;
        LinkedList<BubblePopupView> linkedList = null;
        if (!StringUtils.isNull(viewId)) {
            linkedList = bubbleViewCache.getFomratItemViewList(viewId + ctx.hashCode());
            if (linkedList != null) {
                int index;
                int size = linkedList.size();
                int cnt = 0;
                do {
                    bubblePopupView = getCacheItemView(linkedList);
                    index = ViewManger.indexOfChild(bubblePopupView, apView);
                    cnt++;
                    if (index == -1) {
                        break;
                    }
                } while (cnt < size);
                if (index != -1) {
                    bubblePopupView = null;
                }
                if (bubblePopupView != null) {
                    try {
                        bubblePopupView.setSmartSmsEvent(smartSmsEvent);
                        bubblePopupView.reBindData(ctx, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        bubblePopupView = null;
                    }
                }
            }
        }
        if (bubblePopupView == null) {
            bubblePopupView = new BubblePopupView(ctx);
            bubblePopupView.setSmartSmsEvent(smartSmsEvent);
            Map<String, PartViewParam> map = (Map) bubbleViewCache.viewParamCache.get(viewId);
            if (map == null) {
                map = ViewManger.parseViewPartParam(viewId);
                bubbleViewCache.viewParamCache.put(viewId, map);
            }
            msg.putValue("viewPartParam", map);
            bubblePopupView.init(ctx, msg, null);
            bubblePopupView.setId(DUOQU_BUBBLE_VIEW_ID);
            if (linkedList == null) {
                linkedList = new LinkedList();
                bubbleViewCache.putBubbleItemTypeViewToCache(viewId + ctx.hashCode(), linkedList);
            }
            addCacheItemView(bubblePopupView, linkedList);
        }
        return bubblePopupView;
    }

    @SuppressLint({"NewApi"})
    private static void addCacheItemView(BubblePopupView itewView, LinkedList<BubblePopupView> listView) {
        listView.offerLast(itewView);
    }

    @SuppressLint({"NewApi"})
    private static BubblePopupView getCacheItemView(LinkedList<BubblePopupView> listView) {
        if (listView == null) {
            return null;
        }
        BubblePopupView itemView = (BubblePopupView) listView.pollFirst();
        if (itemView == null) {
            return itemView;
        }
        addCacheItemView(itemView, listView);
        return itemView;
    }

    public static View getRichBubbleView(Activity ctx, JSONObject jsobj, String smsId, MessageItem messageItem, View itemView, AdapterView adViews, HashMap<String, Object> extend, ISmartSmsEvent smartSmsEvent) {
        return getRichBubbleView(ctx, jsobj, smsId, messageItem.mBody, messageItem.mDate, messageItem.mAddress, itemView, adViews, extend, smartSmsEvent);
    }

    public static View getRichBubbleView(Activity ctx, JSONObject jsobj, String smsId, String smsContent, long smsReceiveTime, String phoneNum, View itemView, AdapterView adViews, HashMap<String, Object> extend, ISmartSmsEvent smartSmsEvent) {
        View richview = null;
        try {
            String key = smsId + smsReceiveTime;
            DuoquBubbleViewCache bubbleViewCache = DuoquBubbleViewCache.createDuoquBubbleViewCache(phoneNum);
            BusinessSmsMessage msg = bubbleViewCache.getFomratSmsData(key);
            String viewId;
            if (msg != null) {
                viewId = (String) msg.getValue(DUOQU_VIEW_ID);
                try {
                    msg.extendParamMap = extend;
                    msg.putValue(ContentUtil.DUOQU_IS_SAFE_VERIFY_CODE_KEY, (String) extend.get(ContentUtil.DUOQU_IS_SAFE_VERIFY_CODE_KEY));
                    msg.messageBody = smsContent;
                    msg.smsId = Long.parseLong(smsId);
                    msg.msgTime = smsReceiveTime;
                    richview = getBubblePopupView(ctx, msg, viewId, itemView, adViews, bubbleViewCache, smartSmsEvent);
                } catch (Exception e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("DuoquBubbleViewManager.getRichBubbleData viewId" + viewId + " error: " + e.getMessage(), e);
                    e.printStackTrace();
                }
                return richview;
            }
            if (jsobj != null) {
                if (jsobj.has(DUOQU_VIEW_ID)) {
                    viewId = jsobj.getString(DUOQU_VIEW_ID);
                    msg = BusinessSmsMessage.createMsgObj();
                    msg.smsId = Long.parseLong(smsId);
                    msg.msgTime = smsReceiveTime;
                    msg.viewType = (byte) 1;
                    msg.bubbleJsonObj = jsobj;
                    msg.messageBody = smsContent;
                    msg.originatingAddress = phoneNum;
                    msg.titleNo = jsobj.optString("title_num");
                    msg.extendParamMap = extend;
                    msg.simIndex = XyUtil.getSimIndex(extend);
                    msg.putValue(ContentUtil.DUOQU_IS_SAFE_VERIFY_CODE_KEY, (String) extend.get(ContentUtil.DUOQU_IS_SAFE_VERIFY_CODE_KEY));
                    richview = getBubblePopupView(ctx, msg, viewId, itemView, adViews, bubbleViewCache, smartSmsEvent);
                    bubbleViewCache.putMsgToCache(key, msg);
                }
            }
            return richview;
        } catch (Exception e2) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquBubbleViewManager.getRichBubbleView error: " + e2.getMessage(), e2);
            e2.printStackTrace();
        }
    }

    public static void beforeInitBubbleView(Activity ctx, String phone, JSONObject obj) {
        if (ctx != null) {
            try {
                if (!(ctx.isFinishing() || obj == null || !obj.has("useBubbleViews"))) {
                    JSONArray arr = new JSONArray(obj.getString("useBubbleViews"));
                    int len = arr.length();
                    BusinessSmsMessage msg = BusinessSmsMessage.createMsgObj();
                    msg.viewType = (byte) 1;
                    DuoquBubbleViewCache duoquBubbleViewCache = DuoquBubbleViewCache.createDuoquBubbleViewCache(phone);
                    for (int i = 0; i < len; i++) {
                        String viewId = arr.getString(i);
                        if (!StringUtils.isNull(viewId)) {
                            LinkedList<BubblePopupView> linkedList = duoquBubbleViewCache.getFomratItemViewList(viewId + ctx.hashCode());
                            if (linkedList == null) {
                                linkedList = new LinkedList();
                                duoquBubbleViewCache.putBubbleItemTypeViewToCache(viewId + ctx.hashCode(), linkedList);
                            }
                            int viewCacheSize = linkedList.size();
                            if (viewCacheSize < 4) {
                                Map<String, PartViewParam> map = (Map) duoquBubbleViewCache.viewParamCache.get(viewId);
                                if (map == null) {
                                    map = ViewManger.parseViewPartParam(viewId);
                                    duoquBubbleViewCache.viewParamCache.put(viewId, map);
                                }
                                msg.putValue("viewPartParam", map);
                                while (true) {
                                    BubblePopupView view = new BubblePopupView(ctx);
                                    view.init(ctx, msg, null);
                                    view.setId(DUOQU_BUBBLE_VIEW_ID);
                                    addCacheItemView(view, linkedList);
                                    viewCacheSize++;
                                    if (viewCacheSize >= 4) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
            }
        }
    }
}
