package com.android.contacts.hap.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import java.lang.ref.SoftReference;
import java.util.HashMap;

public class SimContactsCache {
    private static final boolean DEBUG = HwLog.HWDBG;
    private static Object mLock = new Object();
    private static final Integer mSimSlot_1 = Integer.valueOf(0);
    private static final Integer mSimSlot_2 = Integer.valueOf(1);
    private static final Integer mSimSlot_Default = Integer.valueOf(-1);
    private static SimContactsCache sInstance = null;
    private static HashMap<Integer, SoftReference<Bitmap>> sSimSmallBitmapCache;
    private Context mContext;
    private BackgroundHdlr mHandler = new BackgroundHdlr(this.mContext.getContentResolver());
    private HashMap<Long, Integer> mSimContactsCache;

    private class BackgroundHdlr extends HandlerThread implements Callback {
        private Handler mLoaderThreadHandler;
        private ContentResolver mResolver;

        public BackgroundHdlr(ContentResolver aResolver) {
            super("SimContactsCache");
            this.mResolver = aResolver;
        }

        public void ensureHandler() {
            if (this.mLoaderThreadHandler == null) {
                this.mLoaderThreadHandler = new Handler(getLooper(), this);
            }
        }

        public void requestRefresh() {
            ensureHandler();
            this.mLoaderThreadHandler.removeMessages(1);
            Message msg = new Message();
            msg.what = 1;
            this.mLoaderThreadHandler.sendMessage(msg);
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (SimContactsCache.DEBUG) {
                        HwLog.d("SimContactsCache", "Processing a request");
                    }
                    SimContactsCache.this.mSimContactsCache = updateSimContactsList();
                    if (SimContactsCache.DEBUG) {
                        HwLog.d("SimContactsCache", "Processing a request is completed");
                    }
                    return true;
                default:
                    return false;
            }
        }

        protected HashMap<Long, Integer> updateSimContactsList() {
            HashMap<Long, Integer> mapofAllSimContactIds = new HashMap();
            StringBuilder whereBuilder = new StringBuilder();
            whereBuilder.append("account_type").append("='").append("com.android.huawei.sim").append("' OR ").append("account_type").append("='").append("com.android.huawei.secondsim").append("'");
            Cursor cursor = this.mResolver.query(RawContacts.CONTENT_URI, new String[]{"account_type", "contact_id"}, whereBuilder.toString(), null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String accountType = cursor.getString(cursor.getColumnIndex("account_type"));
                        if (accountType != null && CommonUtilMethods.isSimAccount(accountType)) {
                            Long id = Long.valueOf(cursor.getLong(cursor.getColumnIndex("contact_id")));
                            if (!SimFactoryManager.isDualSim()) {
                                try {
                                    mapofAllSimContactIds.put(id, SimContactsCache.mSimSlot_Default);
                                } catch (Throwable th) {
                                    cursor.close();
                                }
                            } else if ("com.android.huawei.sim".equals(accountType)) {
                                mapofAllSimContactIds.put(id, SimContactsCache.mSimSlot_1);
                            } else {
                                mapofAllSimContactIds.put(id, SimContactsCache.mSimSlot_2);
                            }
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return mapofAllSimContactIds;
        }
    }

    private SimContactsCache(Context aContext) {
        this.mContext = aContext;
        this.mHandler.start();
    }

    public static SimContactsCache getInstance(Context aContext) {
        if (sInstance == null) {
            sInstance = new SimContactsCache(aContext);
        }
        return sInstance;
    }

    public static void clearSimSmallBitmapCache() {
        synchronized (mLock) {
            sSimSmallBitmapCache = null;
        }
    }

    public void refresh() {
        if (DEBUG) {
            HwLog.d("SimContactsCache", "Refresh is called");
        }
        this.mHandler.requestRefresh();
    }

    public void stop() {
        this.mHandler.quit();
    }

    public int getMatchedContact(long aContactId) {
        if (DEBUG) {
            HwLog.d("SimContactsCache", "getMatchedContact is called with contactid:" + aContactId);
        }
        if (this.mSimContactsCache == null || !this.mSimContactsCache.containsKey(Long.valueOf(aContactId))) {
            return -100;
        }
        return ((Integer) this.mSimContactsCache.get(Long.valueOf(aContactId))).intValue();
    }

    public static Bitmap getSimSmallBitmap(Context aContext, int aSlotId) {
        if (!EmuiFeatureManager.isSimAccountIndicatorEnabled()) {
            return null;
        }
        synchronized (mLock) {
            if (sSimSmallBitmapCache == null) {
                sSimSmallBitmapCache = new HashMap();
            }
            Integer key = Integer.valueOf(aSlotId);
            SoftReference<Bitmap> bmRef = (SoftReference) sSimSmallBitmapCache.get(key);
            if (bmRef == null || bmRef.get() == null) {
                Resources res = aContext.getResources();
                Drawable drawable = res.getDrawableForDensity(SimFactoryManager.getSimAccountIconResourceId(aSlotId, false), res.getDisplayMetrics().densityDpi);
                Bitmap bm = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
                Canvas canvas = new Canvas(bm);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(canvas);
                sSimSmallBitmapCache.put(key, new SoftReference(bm));
                return bm;
            }
            Bitmap bitmap = (Bitmap) bmRef.get();
            return bitmap;
        }
    }
}
