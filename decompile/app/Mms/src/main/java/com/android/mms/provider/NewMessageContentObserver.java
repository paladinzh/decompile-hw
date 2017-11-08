package com.android.mms.provider;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import com.android.messaging.util.OsUtil;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwBackgroundLoader;

public class NewMessageContentObserver extends ContentObserver {
    private static final String[] MMSSMS_STATUS_PROJECTION = new String[]{"unread_count"};
    private static final String[] MMS_STATUS_PROJECTION = new String[]{"thread_id", "date", "_id", "sub", "sub_cs"};
    private static final String[] SMS_STATUS_PROJECTION = new String[]{"thread_id", "date", "address", "subject", "body"};
    private static boolean hasInitMmsUnReadFiled = false;
    public static final Uri mRcsThreadUri = Uri.parse("content://rcsim/conversations").buildUpon().appendQueryParameter("unreadRcsThreads", "true").build();
    private static Uri mThreadUri = Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    private static final boolean sIsRcsEnable;
    private static boolean sMmsUnReadFiled = true;
    private Context mContext;
    private RefreshLauncherUnreadCountThread mRefreshThread;
    public int mUnreadCount = -1;
    private final Handler mUpdate = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4096:
                    NewMessageContentObserver.this.startRefreshLauncherIcon();
                    return;
                default:
                    return;
            }
        }
    };
    private Uri[] mUri;

    public class RefreshLauncherUnreadCountThread extends Thread {
        public void run() {
            NewMessageContentObserver.this.getMessageCountAndUpdateToLuancher();
        }
    }

    private class UpdateLauncherUnreadCountRunnable implements Runnable {
        private boolean isSetZero = false;

        public UpdateLauncherUnreadCountRunnable(boolean setZero) {
            this.isSetZero = setZero;
        }

        public void run() {
            int newUnreadCount = 0;
            if (!this.isSetZero) {
                newUnreadCount = NewMessageContentObserver.this.getNewMessageCount(NewMessageContentObserver.this.mContext);
            }
            NewMessageContentObserver.this.mUnreadCount = newUnreadCount;
            NewMessageContentObserver.this.updateUnreadCountToLuancher(newUnreadCount);
        }
    }

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.hw_rcs_product", false)) {
            z = SystemProperties.getBoolean("ro.config.hw_rcs_vendor", false);
        }
        sIsRcsEnable = z;
    }

    public NewMessageContentObserver(Context context, Handler handler) {
        super(handler);
        this.mContext = context;
        if (sIsRcsEnable) {
            this.mUri = new Uri[]{mThreadUri, mRcsThreadUri};
            return;
        }
        this.mUri = new Uri[]{mThreadUri};
    }

    public void onChange(boolean selfChange) {
        MLog.i("NewMessageContentObserver ", "onChange() : selfChange = " + selfChange);
        this.mUpdate.removeMessages(4096);
        this.mUpdate.sendEmptyMessageDelayed(4096, 500);
    }

    public void registerUpdaterObserver(boolean notifyForDescendents) {
        if (this.mUri != null) {
            for (Uri registerContentObserver : this.mUri) {
                this.mContext.getContentResolver().registerContentObserver(registerContentObserver, notifyForDescendents, this);
            }
            if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
                HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
                    public void run() {
                        MLog.i("NewMessageContentObserver ", "update new message count to luancher when app first load");
                        NewMessageContentObserver.this.getMessageCountAndUpdateToLuancher();
                    }
                });
            }
        }
    }

    public void unregisterUpdaterObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this);
    }

    private void updateUnreadCountToLuancher(int unReadCount) {
        Bundle bunlde = new Bundle();
        bunlde.putString("package", "com.android.mms");
        bunlde.putString("class", "com.android.mms.ui.ConversationList");
        MLog.i("NewMessageContentObserver ", " updateLuancherUnreadCount unreadCount = " + unReadCount);
        bunlde.putInt("badgenumber", unReadCount);
        try {
            this.mContext.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", "", bunlde);
        } catch (SecurityException e) {
            MLog.e("NewMessageContentObserver ", "updateUnreadCountToLuancher SecurityException " + e.getMessage());
        } catch (Exception e2) {
            MLog.e("NewMessageContentObserver ", "updateUnreadCountToLuancher Exception " + e2.getMessage());
        }
        MLog.i("NewMessageContentObserver ", "updateUnreadCountToLuancher end ================");
    }

    private int getNewMessageCount(Context context) {
        if (unReadFieldExist(context)) {
            return getMissedMessageCount(context);
        }
        return getMissedMmsAndSmsCount(context);
    }

    private int getMissedMmsAndSmsCount(Context context) {
        int nNewMessage = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Sms.CONTENT_URI, SMS_STATUS_PROJECTION, "(type = 1 AND read = 0)", null, "date desc");
            if (cursor != null) {
                nNewMessage = cursor.getCount() + 0;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.w("NewMessageContentObserver ", " get new message count: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        cursor = null;
        try {
            cursor = context.getContentResolver().query(Mms.CONTENT_URI, MMS_STATUS_PROJECTION, "(msg_box = 1 and read = 0 and m_type != 134 and m_type != 136)", null, "date desc");
            if (cursor != null) {
                nNewMessage += cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            MLog.e("NewMessageContentObserver ", "get mms count: " + e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return nNewMessage;
    }

    private int getMissedMessageCount(Context context) {
        int nNewMessage = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(mThreadUri, MMSSMS_STATUS_PROJECTION, "(unread_count > 0)", null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex("unread_count");
                if (cursor.moveToFirst()) {
                    do {
                        nNewMessage += cursor.getInt(columnIndex);
                    } while (cursor.moveToNext());
                }
            }
            nNewMessage += getMissedRcsMessageCount(context);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            MLog.e("NewMessageContentObserver ", "Get unread message count from thread: " + ex);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return nNewMessage;
    }

    private boolean unReadFieldExist(Context context) {
        if (hasInitMmsUnReadFiled) {
            return sMmsUnReadFiled;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(mThreadUri, MMSSMS_STATUS_PROJECTION, "(unread_count >= 0)", null, null);
            sMmsUnReadFiled = true;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            sMmsUnReadFiled = false;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        hasInitMmsUnReadFiled = true;
        return sMmsUnReadFiled;
    }

    private int getMissedRcsMessageCount(Context context) {
        if (!sIsRcsEnable) {
            return 0;
        }
        int nNewRcsMessage = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(mRcsThreadUri, MMSSMS_STATUS_PROJECTION, "(unread_count > 0)", null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex("unread_count");
                if (cursor.moveToFirst()) {
                    do {
                        nNewRcsMessage += cursor.getInt(columnIndex);
                    } while (cursor.moveToNext());
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException ex) {
            MLog.e("NewMessageContentObserver ", "Get unread message count from rcs thread: " + ex);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return nNewRcsMessage;
    }

    private void getMessageCountAndUpdateToLuancher() {
        MLog.i("NewMessageContentObserver ", " handle update icon message");
        int oldUnreadCount = this.mUnreadCount;
        int newUnreadCount = getNewMessageCount(this.mContext);
        MLog.w("NewMessageContentObserver ", "newUnreadCount:" + newUnreadCount + ",oldUnreadCount:" + oldUnreadCount);
        if (newUnreadCount != oldUnreadCount) {
            this.mUnreadCount = newUnreadCount;
            updateUnreadCountToLuancher(newUnreadCount);
        }
    }

    public void updateLauncherUnreadCount() {
        HwBackgroundLoader.getBackgroundHandler().postDelayed(new UpdateLauncherUnreadCountRunnable(false), 500);
    }

    public void resetLauncherUnreadCount() {
        HwBackgroundLoader.getBackgroundHandler().postDelayed(new UpdateLauncherUnreadCountRunnable(true), 500);
    }

    private void startRefreshLauncherIcon() {
        if (this.mRefreshThread == null) {
            this.mRefreshThread = createRefreshLauncherThread();
            this.mRefreshThread.start();
            return;
        }
        if (this.mRefreshThread.isAlive()) {
            MLog.d("NewMessageContentObserver ", "startRefreshLauncherIcon RefreshThread interrupt");
            this.mRefreshThread.interrupt();
        }
        this.mRefreshThread = null;
        this.mRefreshThread = createRefreshLauncherThread();
        this.mRefreshThread.start();
    }

    private RefreshLauncherUnreadCountThread createRefreshLauncherThread() {
        return new RefreshLauncherUnreadCountThread();
    }
}
