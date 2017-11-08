package com.android.contacts.hap.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BackgroundViewCacher {
    private static BackgroundViewCacher mInstance;
    private BackgroundHandler mBkHdlr;
    private float mFontScale;
    private LayoutInflater mInflator;
    private int mOrientation;
    private HashMap<Integer, List<View>> mViewCache = new HashMap();

    private class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper aLooper) {
            super(aLooper);
        }

        public void handleMessage(Message aMsg) {
            switch (aMsg.what) {
            }
            int count = aMsg.arg1;
            inflateMainTypes(count);
            inflateAllTypes(count);
            BackgroundViewCacher.this.cancel();
        }

        private void inflateMainTypes(int count) {
            inflateFor(R.layout.contact_detail_fragment, count);
            inflateFor(R.layout.call_log_detail_fragment, count);
        }

        private void inflateAllTypes(int count) {
            inflateFor(R.layout.detail_item_phone, count * 3);
            inflateFor(R.layout.detail_item_with_label_default, count * 5);
            inflateFor(R.layout.call_detail_history_item, count * 6);
        }

        private void inflateFor(int aLayoutId, int aTotalCount) {
            List<View> items = BackgroundViewCacher.this.getViewItems(aLayoutId);
            int count = aTotalCount - items.size();
            for (int i = 0; i < count; i++) {
                View view = BackgroundViewCacher.this.mInflator.inflate(aLayoutId, null);
                view.setTag(R.layout.detail_item_phone, Integer.valueOf(aLayoutId));
                items.add(view);
            }
        }
    }

    public static BackgroundViewCacher getInstance(Context aContext) {
        Resources res = aContext.getResources();
        int tempOrientation = res.getConfiguration().orientation;
        float tempFontScale = res.getConfiguration().fontScale;
        if (mInstance == null) {
            mInstance = new BackgroundViewCacher(aContext);
        } else if (tempOrientation != mInstance.mOrientation || ((double) Math.abs(mInstance.mFontScale - tempFontScale)) > 0.001d) {
            mInstance = new BackgroundViewCacher(aContext);
        }
        return mInstance;
    }

    public void clearViewCache() {
        if (this.mViewCache != null) {
            this.mViewCache.clear();
        }
    }

    private BackgroundViewCacher(Context aContext) {
        Context context = new ContextThemeWrapper(aContext.getApplicationContext(), aContext.getApplicationContext().getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        context.getTheme().applyStyle(R.style.PeopleThemeWithListSelector, true);
        this.mInflator = LayoutInflater.from(context);
        Resources res = context.getResources();
        this.mOrientation = res.getConfiguration().orientation;
        this.mFontScale = res.getConfiguration().fontScale;
    }

    public synchronized void startInflatring() {
        if (this.mBkHdlr == null) {
            HandlerThread thread = new HandlerThread("Detail view inflator handler");
            thread.setPriority(10);
            thread.start();
            this.mBkHdlr = new BackgroundHandler(thread.getLooper());
        }
        this.mBkHdlr.sendMessage(this.mBkHdlr.obtainMessage(0, CommonUtilMethods.calcIfNeedSplitScreen() ? 4 : 1, 0));
    }

    private synchronized void cancel() {
        if (!(this.mBkHdlr == null || this.mBkHdlr.hasMessages(0))) {
            this.mBkHdlr.getLooper().quit();
            this.mBkHdlr = null;
        }
    }

    public View getViewFromCache(int aLayoutId) {
        Integer key = Integer.valueOf(aLayoutId);
        if (this.mViewCache != null && this.mViewCache.containsKey(key)) {
            List<View> viewCacheList = (List) this.mViewCache.get(key);
            if (!(viewCacheList == null || viewCacheList.size() <= 0 || viewCacheList.get(0) == null)) {
                if (aLayoutId == R.layout.contact_detail_fragment && viewCacheList.size() == 1) {
                    startInflatring();
                }
                return (View) viewCacheList.remove(0);
            }
        }
        View view = this.mInflator.inflate(aLayoutId, null);
        view.setTag(R.layout.detail_item_phone, Integer.valueOf(aLayoutId));
        return view;
    }

    private List<View> getViewItems(int aLayoutId) {
        Integer key = Integer.valueOf(aLayoutId);
        if (this.mViewCache.containsKey(key)) {
            return (List) this.mViewCache.get(key);
        }
        List<View> viewCacheList = new ArrayList();
        this.mViewCache.put(Integer.valueOf(aLayoutId), viewCacheList);
        return viewCacheList;
    }
}
