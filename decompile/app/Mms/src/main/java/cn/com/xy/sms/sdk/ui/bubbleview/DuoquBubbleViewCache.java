package cn.com.xy.sms.sdk.ui.bubbleview;

import android.annotation.TargetApi;
import android.util.LruCache;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.popupview.BubblePopupView;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

@TargetApi(12)
public class DuoquBubbleViewCache {
    static final int DUOQU_CACHE_ITEM_VIEW_MAX_SIZE = 200;
    static final String TAG = "XIAOYUAN";
    private static HashMap<String, DuoquBubbleViewCache> mDuoquBubbleViewCache = new HashMap();
    public LruCache<String, LinkedList<BubblePopupView>> mFormatItemViewCacheMapList = new LruCache(200);
    public LruCache<String, BusinessSmsMessage> mFormatSmsDataCache = new LruCache(200);
    public HashMap<String, Map<String, PartViewParam>> viewParamCache = new HashMap();

    public static DuoquBubbleViewCache getDuoquBubbleViewCache(String key) {
        return (DuoquBubbleViewCache) mDuoquBubbleViewCache.get(key);
    }

    public static DuoquBubbleViewCache createDuoquBubbleViewCache(String key) {
        DuoquBubbleViewCache bubbleViewCache = (DuoquBubbleViewCache) mDuoquBubbleViewCache.get(key);
        if (bubbleViewCache != null) {
            return bubbleViewCache;
        }
        bubbleViewCache = new DuoquBubbleViewCache();
        mDuoquBubbleViewCache.put(key, bubbleViewCache);
        return bubbleViewCache;
    }

    public void putMsgToCache(String cacheKey, BusinessSmsMessage msg) {
        if (cacheKey != null && msg != null) {
            synchronized (this.mFormatSmsDataCache) {
                this.mFormatSmsDataCache.put(cacheKey, msg);
            }
        }
    }

    public BusinessSmsMessage getFomratSmsData(String cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        return (BusinessSmsMessage) this.mFormatSmsDataCache.get(cacheKey);
    }

    public void putBubbleItemTypeViewToCache(String cacheKey, LinkedList<BubblePopupView> bubbleViews) {
        if (cacheKey != null && bubbleViews != null) {
            synchronized (this.mFormatItemViewCacheMapList) {
                this.mFormatItemViewCacheMapList.put(cacheKey, bubbleViews);
            }
        }
    }

    public LinkedList<BubblePopupView> getFomratItemViewList(String cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        return (LinkedList) this.mFormatItemViewCacheMapList.get(cacheKey);
    }

    private void clearCacheData() {
        if (this.mFormatSmsDataCache != null) {
            synchronized (this.mFormatSmsDataCache) {
                this.mFormatSmsDataCache.evictAll();
            }
        }
        if (this.mFormatItemViewCacheMapList != null) {
            synchronized (this.mFormatItemViewCacheMapList) {
                this.mFormatItemViewCacheMapList.evictAll();
            }
        }
        if (this.viewParamCache != null) {
            this.viewParamCache.clear();
        }
    }

    public static void clearCacheData(String phoneNum) {
        DuoquBubbleViewCache bubbleViewCache = (DuoquBubbleViewCache) mDuoquBubbleViewCache.get(phoneNum);
        if (bubbleViewCache != null) {
            bubbleViewCache.clearCacheData();
        }
    }

    public static void clearFormatItemViewCacheMapList() {
        try {
            for (Entry<String, DuoquBubbleViewCache> viewCache : mDuoquBubbleViewCache.entrySet()) {
                DuoquBubbleViewCache bubbleCache = (DuoquBubbleViewCache) viewCache.getValue();
                if (bubbleCache != null) {
                    bubbleCache.mFormatItemViewCacheMapList.evictAll();
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquBubbleViewCache clearFormatItemViewCacheMapList " + e.getMessage(), e);
        }
    }
}
