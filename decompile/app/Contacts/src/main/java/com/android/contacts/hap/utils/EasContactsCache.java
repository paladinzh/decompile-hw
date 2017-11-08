package com.android.contacts.hap.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.util.LogConfig;
import com.google.android.gms.R;
import java.lang.ref.SoftReference;
import java.util.HashMap;

public class EasContactsCache {
    private static SoftReference<Bitmap> sEasSmallBitmapCache;
    private static EasContactsCache sInstance = null;
    private Context mContext;
    private HashMap<Long, Boolean> mEasContactsCache;
    private BackgroundHdlr mHandler;
    private boolean mIsNeedRefresh = true;

    private class BackgroundHdlr extends HandlerThread implements Callback {
        private Handler mLoaderThreadHandler;
        private ContentResolver mResolver;

        public BackgroundHdlr(ContentResolver aResolver) {
            super("EasContactsCache");
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
                    if (LogConfig.HWDBG) {
                        Log.d("EasContactsCache", "Processing a request");
                    }
                    EasContactsCache.this.mEasContactsCache = updateEasContactsList();
                    if (LogConfig.HWDBG) {
                        Log.d("EasContactsCache", "Processing a request is completed");
                    }
                    return true;
                default:
                    return false;
            }
        }

        protected HashMap<Long, Boolean> updateEasContactsList() {
            HashMap<Long, Boolean> mapofAllEASContactIds = new HashMap();
            StringBuilder whereBuilder = new StringBuilder();
            whereBuilder.append("account_type").append("='").append(HwCustCommonConstants.EAS_ACCOUNT_TYPE).append("' AND ").append("deleted").append("=0");
            Cursor cursor = this.mResolver.query(RawContacts.CONTENT_URI, new String[]{"account_type", "contact_id"}, whereBuilder.toString(), null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            mapofAllEASContactIds.put(Long.valueOf(cursor.getLong(cursor.getColumnIndex("contact_id"))), Boolean.valueOf(true));
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                } catch (Throwable th) {
                    cursor.close();
                }
            }
            return mapofAllEASContactIds;
        }
    }

    private EasContactsCache(Context aContext) {
        this.mContext = aContext;
        this.mHandler = new BackgroundHdlr(this.mContext.getContentResolver());
        this.mHandler.start();
    }

    public static EasContactsCache getInstance(Context aContext) {
        if (sInstance == null) {
            sInstance = new EasContactsCache(aContext);
        }
        return sInstance;
    }

    public synchronized void refresh() {
        if (LogConfig.HWDBG) {
            Log.d("EasContactsCache", "Refresh is called mIsNeedRefresh=" + this.mIsNeedRefresh);
        }
        if (this.mIsNeedRefresh) {
            this.mHandler.requestRefresh();
            this.mIsNeedRefresh = false;
        }
    }

    public synchronized void setDataChange() {
        this.mIsNeedRefresh = true;
    }

    public void stop() {
        this.mHandler.quit();
    }

    public boolean isEasContact(long aContactId) {
        if (LogConfig.HWDBG) {
            Log.d("EasContactsCache", "getMatchedContact is called with contactid:" + aContactId);
        }
        if (this.mEasContactsCache == null || !this.mEasContactsCache.containsKey(Long.valueOf(aContactId))) {
            return false;
        }
        if (LogConfig.HWDBG) {
            Log.d("EasContactsCache", "getMatchedContact given contact is eas and return :true");
        }
        return true;
    }

    public static Bitmap getEasSmallIcon(Context aContext) {
        if (!HwCustCommonConstants.EAS_ACCOUNT_ICON_DISP_EMABLED) {
            return null;
        }
        if (sEasSmallBitmapCache != null && sEasSmallBitmapCache.get() != null) {
            return (Bitmap) sEasSmallBitmapCache.get();
        }
        Bitmap bm = BitmapFactory.decodeResource(aContext.getResources(), R.drawable.contact_dial_fun_menu_pressed);
        sEasSmallBitmapCache = new SoftReference(bm);
        return bm;
    }
}
