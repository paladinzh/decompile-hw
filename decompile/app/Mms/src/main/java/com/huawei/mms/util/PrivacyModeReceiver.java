package com.huawei.mms.util;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.SearchDataLoader.ConversationMatcher;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PrivacyModeReceiver extends BroadcastReceiver {
    private static final String[] collumn_privacy = new String[]{"privacy_mode"};

    public interface ModeChangeListener {
        void onModeChange(Context context, boolean z);
    }

    static class CacheInvalidator implements ModeChangeListener {
        CacheInvalidator() {
        }

        public void onModeChange(Context context, boolean isInPrivacy) {
            Contact.invalidateCache();
            ConversationMatcher.getDefault().reset();
            HwBackgroundLoader.getInst().reloadDataDelayed(8, 500);
            if (!isInPrivacy) {
                MLog.v("PrivacyReceiver", "onModeChange::change to guest mode, cancel all notifications");
                PrivacyModeReceiver.cancelAllNotification(context);
                Contact.clear(context);
            }
        }
    }

    public static class PrivacyStateListener {
        private static PrivacyStateListener inst = null;
        static final boolean isEnabled = MmsConfig.isSupportPrivacy();
        private boolean isInPrivacyMode = false;
        Queue<ModeChangeListener> listeners = new ConcurrentLinkedQueue();
        private ContentObserver mPrivacyModeObserver;

        private PrivacyStateListener() {
            this.listeners.add(new CacheInvalidator());
        }

        public static synchronized PrivacyStateListener self() {
            PrivacyStateListener privacyStateListener;
            synchronized (PrivacyStateListener.class) {
                if (inst == null) {
                    inst = new PrivacyStateListener();
                }
                privacyStateListener = inst;
            }
            return privacyStateListener;
        }

        public boolean isInPrivacyMode() {
            return this.isInPrivacyMode;
        }

        public void setInPrivacyMode(boolean isInPrivacyMode) {
            this.isInPrivacyMode = isInPrivacyMode;
        }

        public void register(ModeChangeListener l) {
            if (isEnabled) {
                this.listeners.add(l);
            }
        }

        public void unRegister(ModeChangeListener l) {
            if (isEnabled) {
                this.listeners.remove(l);
            }
        }

        public void notifyAll(Context context, boolean isInPrivacyMode) {
            for (ModeChangeListener l : this.listeners) {
                l.onModeChange(context, isInPrivacyMode);
            }
        }

        public void registerSecureDatabasesChanged(final Context context) {
            if (isEnabled) {
                this.mPrivacyModeObserver = new ContentObserver(new Handler()) {
                    public void onChange(boolean selfChange) {
                        if (1 == PrivacyModeReceiver.getPrivacyState(context)) {
                            MLog.v("PrivacyReceiver", "onChange::change to guest mode, cancel all notifications");
                            PrivacyModeReceiver.cancelAllNotification(context);
                        }
                    }
                };
                context.getContentResolver().registerContentObserver(Secure.getUriFor("privacy_mode_state"), true, this.mPrivacyModeObserver);
            }
        }

        public void unregisterSecureDatabasesChanged(Context context) {
            if (isEnabled && this.mPrivacyModeObserver != null) {
                context.getContentResolver().unregisterContentObserver(this.mPrivacyModeObserver);
            }
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("android.intent.actions.PRIVACY_MODE_CHANGED".equals(action)) {
                boolean isInPrivacyMode = intent.getIntExtra("privacy_mode_value", 1) == 0;
                PrivacyStateListener.self().setInPrivacyMode(isInPrivacyMode);
                if (MmsConfig.isSupportPrivacy()) {
                    MLog.v("PrivacyReceiver", "onReceive::notifyAll of mode change");
                    PrivacyStateListener.self().notifyAll(context, isInPrivacyMode);
                }
                return;
            }
            MLog.e("PrivacyReceiver", "PrivacyModeReceiver got unknow action: " + action);
        }
    }

    public static boolean checkPrivacyState(Context context) {
        if (MmsConfig.isSupportPrivacy()) {
            boolean isInPriv = false;
            if (getPrivacyModeOn(context) == 0 || (1 == getPrivacyModeOn(context) && getPrivacyState(context) == 0)) {
                isInPriv = true;
            }
            MLog.v("PrivacyReceiver", "checkPrivacyState::the isInPriv is: " + isInPriv);
            PrivacyStateListener.self().setInPrivacyMode(isInPriv);
            return isInPriv;
        }
        PrivacyStateListener.self().setInPrivacyMode(true);
        return false;
    }

    private static int getPrivacyModeOn(Context context) {
        int mode = 0;
        try {
            mode = Secure.getInt(context.getContentResolver(), "privacy_mode_on", 0);
        } catch (Exception e) {
            MLog.e("PrivacyReceiver", "getPrivacyModeOn::query data from SettingsProvider exception: " + e);
        }
        MLog.v("PrivacyReceiver", "The privacy mode is: " + (1 == mode ? "ON" : "OFF"));
        return mode;
    }

    private static int getPrivacyState(Context context) {
        int state = 0;
        try {
            state = Secure.getInt(context.getContentResolver(), "privacy_mode_state", 1);
        } catch (Exception e) {
            MLog.e("PrivacyReceiver", "getPrivacyState::query data from SettingsProvider exception: " + e);
        }
        MLog.v("PrivacyReceiver", "The privacy state is: " + (1 == state ? "non-privacy mode" : "privacy mode"));
        return state;
    }

    public static boolean isPrivacyPdu(Context context, long pdu_id) {
        return isPrivacyMsg(context, ContentUris.withAppendedId(Mms.CONTENT_URI, pdu_id));
    }

    public static boolean isPrivacySms(Context context, long sms_id) {
        return isPrivacyMsg(context, ContentUris.withAppendedId(Sms.CONTENT_URI, sms_id));
    }

    public static boolean isPrivacyMsg(Context context, Uri uri) {
        int privacy_mode = -1;
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, uri, collumn_privacy, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                privacy_mode = 2;
            } else if (cursor.moveToFirst()) {
                privacy_mode = cursor.getInt(0);
                MLog.d("PrivacyReceiver", "CHECK " + uri + " ret: " + privacy_mode);
            } else {
                privacy_mode = 0;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.w("PrivacyReceiver", "isPrivacyMsg query fail", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (privacy_mode > 1) {
            return true;
        }
        return false;
    }

    public static boolean isPrivacyThread(Context context, long thread_id) {
        if (0 == thread_id || -1 == thread_id || -2 == thread_id) {
            return false;
        }
        boolean z;
        int privacy_mode = -1;
        Cursor cursor = null;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, Conversation.sAllThreadsUri, collumn_privacy, "_id=?", new String[]{String.valueOf(thread_id)}, null);
            if (cursor == null || cursor.getCount() == 0) {
                privacy_mode = 2;
            } else if (cursor.moveToFirst()) {
                privacy_mode = cursor.getInt(0);
                MLog.d("PrivacyReceiver", "CHECK Conversation " + thread_id + " ret: " + privacy_mode);
            } else {
                privacy_mode = 0;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.w("PrivacyReceiver", "isPrivacyThread query fail", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (privacy_mode > 1) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private static void cancelAllNotification(Context context) {
        MessagingNotification.cancelNotification(context, 123);
        MessagingNotification.cancelNotification(context, 789);
        MessagingNotification.cancelNotification(context, 531);
    }
}
