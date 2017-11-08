package com.huawei.keyguard.events;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.OperationCanceledException;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import com.huawei.keyguard.events.MonitorImpl.MonitorChangeListener;
import com.huawei.keyguard.events.RcsMessageMonitorImpl.ENUM_MESSAGE_TYPE;
import com.huawei.keyguard.util.HwLog;

public class MessageMonitor extends MonitorImpl {
    private static String PREFERENCES_FILE_FULL_PATH = "/data/data/com.android.mms/shared_prefs/auto_downLoad.xml";
    private static final String[] PROJECTION = new String[]{"thread_id", "address", "body"};
    private static String mDefaultSmsName = "com.android.contacts";
    private static RcsMessageMonitorImpl mRcsObject = new RcsMessageMonitorImpl();
    private static long sNewThreadId = 0;
    private String mSelecting = "(msg_box=1 AND read=0 AND m_type!= 134 AND m_type!= 136)";

    public static class MessageInfo {
        public String mFromName;
        public String mMsgContent;
        public String mPhoneNum;
        public int mUnReadCount;

        public String toString() {
            return "[MessageInfo] unReadNm = " + this.mUnReadCount + ", mPhoneNum = " + this.mPhoneNum + ", mFromName = " + this.mFromName + ", mMsgContent = " + this.mMsgContent;
        }

        public int getUnReadCount() {
            return this.mUnReadCount;
        }

        public void setUnReadCount(int unReadCount) {
            this.mUnReadCount = unReadCount;
        }
    }

    private static void initDefaultSmsAppName(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.android.contacts", "com.android.mms.ui.ComposeMessageActivity");
        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
            mDefaultSmsName = "com.android.mms";
        } else {
            mDefaultSmsName = "com.android.contacts";
        }
    }

    public MessageMonitor(Context context, MonitorChangeListener callback, int monitorId) {
        super(context, callback, monitorId);
        initDefaultSmsAppName(context);
    }

    public void register() {
        registerContent(MmsSms.CONTENT_URI);
        if (mRcsObject != null) {
            RcsMessageMonitorImpl rcsMessageMonitorImpl = mRcsObject;
            if (RcsMessageMonitorImpl.isRCSEnable()) {
                registerContent(Uri.parse("content://rcsim/"));
            }
        }
    }

    public static Intent getMmsIntent(int unread) {
        if (unread == 0) {
            return getMmsMainIntent();
        }
        if (unread != 1) {
            return getMultiMmsIntent();
        }
        Intent intent = getNewMmsViewIntent();
        if (mRcsObject != null) {
            return mRcsObject.getRCSMmsIntent(intent, mDefaultSmsName, sNewThreadId);
        }
        return intent;
    }

    private static Intent getMmsMainIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setFlags(805306368);
        intent.setClassName(mDefaultSmsName, "com.android.mms.ui.ConversationList");
        return intent;
    }

    private static Intent getNewMmsViewIntent() {
        if (sNewThreadId == 0) {
            return getMmsMainIntent();
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setFlags(805306368);
        if (mRcsObject != null) {
            mRcsObject.addXmsModeToIntent(intent);
        }
        Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, sNewThreadId);
        intent.setClassName(mDefaultSmsName, "com.android.mms.ui.ComposeMessageActivity");
        intent.setData(uri);
        return intent;
    }

    private static Intent getMultiMmsIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setFlags(872415232);
        intent.setClassName(mDefaultSmsName, "com.android.mms.ui.ConversationList");
        return intent;
    }

    private MessageInfo onQueryMmsDatabase() {
        MessageInfo info = null;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Mms.CONTENT_URI, null, this.mSelecting, null, null);
        } catch (SQLiteException ex) {
            HwLog.w("MessageMonitor", "queryMMs ex = " + ex.toString());
        } catch (OperationCanceledException ex2) {
            HwLog.w("MessageMonitor", "queryMMs ex = " + ex2.toString());
        } catch (SecurityException ex3) {
            HwLog.w("MessageMonitor", "queryMMs ex= " + ex3.toString());
        }
        if (cursor != null) {
            info = new MessageInfo();
            info.mUnReadCount = cursor.getCount();
            if (cursor.moveToLast()) {
                int index = cursor.getColumnIndex("thread_id");
                if (index > -1) {
                    sNewThreadId = cursor.getLong(index);
                    if (mRcsObject != null) {
                        mRcsObject.setNewThreadType(ENUM_MESSAGE_TYPE.MMS);
                    }
                }
            }
            cursor.close();
        } else {
            HwLog.w("MessageMonitor", "onQueryDatabase query Mms fail");
        }
        return info;
    }

    private MessageInfo onQuerySmsDatabase(MessageInfo info) {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Sms.CONTENT_URI, PROJECTION, "(type = 1 AND read=0 AND seen = 0)", null, null);
        } catch (SQLiteException ex) {
            HwLog.w("MessageMonitor", "querySms ex = " + ex.toString());
        } catch (OperationCanceledException ex2) {
            HwLog.w("MessageMonitor", "querySms ex = " + ex2.toString());
        } catch (SecurityException ex3) {
            HwLog.w("MessageMonitor", "querySms ex = " + ex3.toString());
        }
        if (cursor != null) {
            if (info == null) {
                info = new MessageInfo();
            }
            info.mUnReadCount += cursor.getCount();
            if (cursor.moveToLast()) {
                int index = cursor.getColumnIndex("thread_id");
                if (index > -1) {
                    sNewThreadId = cursor.getLong(index);
                    if (mRcsObject != null) {
                        mRcsObject.setNewThreadType(ENUM_MESSAGE_TYPE.SMS);
                    }
                }
                index = cursor.getColumnIndex("address");
                if (index > -1) {
                    info.mPhoneNum = cursor.getString(index);
                }
                index = cursor.getColumnIndex("body");
                if (index > -1) {
                    info.mMsgContent = cursor.getString(index);
                }
            }
            cursor.close();
        } else {
            HwLog.w("MessageMonitor", "onQueryDatabase query Mms fail");
        }
        return info;
    }

    private MessageInfo onQueryRcsDatabase(MessageInfo info) {
        if (mRcsObject != null) {
            sNewThreadId = mRcsObject.onQueryDatabase(this.mContext, sNewThreadId, null, PROJECTION, "(type = 1 AND read=0 AND seen = 0)", info);
        }
        if (info != null) {
            if (info.mUnReadCount != 1) {
                sNewThreadId = 0;
                if (mRcsObject != null) {
                    mRcsObject.setNewThreadType(ENUM_MESSAGE_TYPE.UNUSED);
                }
            } else if (info.mFromName == null && info.mPhoneNum != null) {
                info.mFromName = getNameByPhoneNum(this.mContext, info.mPhoneNum);
            }
        }
        return info;
    }

    Object onQueryDatabase() {
        return onQueryRcsDatabase(onQuerySmsDatabase(onQueryMmsDatabase()));
    }

    private String getNameByPhoneNum(Context context, String phoneNum) {
        if (context == null) {
            HwLog.w("MessageMonitor", "getNameByPhoneNum context is null");
            return null;
        } else if (TextUtils.isEmpty(phoneNum)) {
            HwLog.w("MessageMonitor", "getNameByPhoneNum phoneNum is invalid");
            return null;
        } else {
            try {
                Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
                Cursor cursor = null;
                String name = null;
                try {
                    cursor = context.getContentResolver().query(uri, new String[]{"_id", "number", "display_name"}, null, null, null);
                } catch (SQLiteException ex) {
                    HwLog.w("MessageMonitor", "queryPhoneLookup ex = " + ex.toString());
                } catch (OperationCanceledException ex2) {
                    HwLog.w("MessageMonitor", "queryPhoneLookup ex = " + ex2.toString());
                } catch (SecurityException ex3) {
                    HwLog.w("MessageMonitor", "queryPhoneLookup ex = " + ex3.toString());
                }
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int index = cursor.getColumnIndex("display_name");
                    if (index > -1) {
                        name = cursor.getString(index);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (name != null) {
                    return name;
                }
                return phoneNum;
            } catch (NullPointerException e) {
                HwLog.w("MessageMonitor", "getNameByPhoneNum withAppendedPath baseUri is null");
                return null;
            }
        }
    }
}
