package com.android.contacts.hap.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.util.RingtoneUpdateServiceOnMediaMount;
import com.android.contacts.util.HwLog;

public class MediaChangeReceiver extends BroadcastReceiver {
    private static boolean sIsMounted = false;
    private static boolean sIsScannerFinished = false;
    private static boolean sIsScannerStarted = false;

    private static class MyHandler extends Handler {
        private static MyHandler sMyHandler;
        private static HandlerThread sUpdateThread = new HandlerThread("update_custom_ringtone_thread");
        private final Uri CONTACTS_URI = Contacts.CONTENT_URI;
        private final String[] PROJECTION = new String[]{"_id", "lookup", "custom_ringtone", "display_name"};
        private ContentResolver mContentResolver;
        private Context mContext;

        static {
            sUpdateThread.start();
        }

        private MyHandler(Looper looper, Context context) {
            super(looper);
            this.mContext = context;
            this.mContentResolver = this.mContext.getContentResolver();
        }

        public static synchronized MyHandler getInstance(Context context) {
            MyHandler myHandler;
            synchronized (MyHandler.class) {
                if (sMyHandler == null) {
                    sMyHandler = new MyHandler(sUpdateThread.getLooper(), context);
                }
                myHandler = sMyHandler;
            }
            return myHandler;
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                SharedPreferences sp = this.mContext.getSharedPreferences("com.android.contacts.custom_ringtone", 0);
                if (sp.getAll().isEmpty()) {
                    if (HwLog.HWDBG) {
                        HwLog.d("MediaChangeReceiver", "no key-values in sharedpreference");
                    }
                    return;
                }
                Cursor cursor = null;
                cursor = this.mContentResolver.query(this.CONTACTS_URI, this.PROJECTION, "custom_ringtone is not null", null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    ContentValues cv = new ContentValues(1);
                    do {
                        long id = cursor.getLong(0);
                        String customRingtoneUriString = cursor.getString(2);
                        if (RingtoneManager.getRingtone(this.mContext, Uri.parse(customRingtoneUriString)) == null) {
                            try {
                                String path = sp.getString(customRingtoneUriString, null);
                                if (path != null) {
                                    Uri possibleRingtoneUri = CommonUtilMethods.getRingtoneUriFromPath(this.mContext, path);
                                    if (possibleRingtoneUri != null) {
                                        if (RingtoneManager.getRingtone(this.mContext, possibleRingtoneUri) == null) {
                                            HwLog.e("MediaChangeReceiver", "the ringtone of possibleRingtoneUri is null: ");
                                        } else {
                                            cv.clear();
                                            cv.put("custom_ringtone", possibleRingtoneUri.toString());
                                            int updated = this.mContentResolver.update(Contacts.getLookupUri(id, cursor.getString(1)), cv, null, null);
                                            cv.clear();
                                            cv.put("data1", possibleRingtoneUri.toString());
                                            int updatedData = this.mContentResolver.update(Data.CONTENT_URI, cv, "raw_contact_id = ? and mimetype = ?", new String[]{Long.toString(id), "vnd.android.huawei.cursor.item/ringtone"});
                                            if (HwLog.HWDBG) {
                                                HwLog.d("MediaChangeReceiver", "updated=" + updated + ", updatedData=" + updatedData);
                                            }
                                            if (updated == 1 && updatedData == 1) {
                                                if (HwLog.HWDBG) {
                                                    HwLog.d("MediaChangeReceiver", "update successfuly");
                                                }
                                                sp.edit().remove(customRingtoneUriString).putString(possibleRingtoneUri.toString(), path).commit();
                                            }
                                        }
                                    }
                                }
                            } catch (SQLiteException e) {
                                e.printStackTrace();
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } catch (Throwable th) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            }
                        }
                    } while (cursor.moveToNext());
                    if (cursor != null) {
                        cursor.close();
                    }
                } else if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static boolean isMounted() {
        return sIsMounted;
    }

    public static void setIsMounted(boolean sIsMounted) {
        sIsMounted = sIsMounted;
    }

    public static boolean isScannerStarted() {
        return sIsScannerStarted;
    }

    public static void setIsScannerStarted(boolean sIsScannerStarted) {
        sIsScannerStarted = sIsScannerStarted;
    }

    public static boolean isScannerFinished() {
        return sIsScannerFinished;
    }

    public static void setScannerFinished(boolean sIsScannerFinished) {
        sIsScannerFinished = sIsScannerFinished;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (!(action == null || action.equals("android.intent.action.MEDIA_EJECT"))) {
                if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                    setIsMounted(true);
                } else if (action.equals("android.intent.action.MEDIA_SCANNER_STARTED")) {
                    if (isMounted()) {
                        setIsScannerStarted(true);
                    } else {
                        setIsScannerStarted(false);
                    }
                } else if (action.equals("android.intent.action.MEDIA_SCANNER_FINISHED")) {
                    if (isScannerStarted()) {
                        setScannerFinished(true);
                    } else {
                        setScannerFinished(false);
                    }
                    if (isScannerFinished()) {
                        MyHandler handler = MyHandler.getInstance(context);
                        if (handler.hasMessages(0)) {
                            handler.removeMessages(0);
                        }
                        handler.sendEmptyMessage(0);
                        context.startService(new Intent(context.getApplicationContext(), RingtoneUpdateServiceOnMediaMount.class));
                    }
                }
            }
        }
    }
}
