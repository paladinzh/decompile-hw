package com.huawei.keyguard.events;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.SystemProperties;
import com.huawei.keyguard.events.MessageMonitor.MessageInfo;
import com.huawei.keyguard.util.HwLog;

public class RcsMessageMonitorImpl {
    private static final /* synthetic */ int[] -com-huawei-keyguard-events-RcsMessageMonitorImpl$ENUM_MESSAGE_TYPESwitchesValues = null;
    private static final Uri RCS_URI_CONVERSATIONS = Uri.parse("content://rcsim/conversations");
    private static final Uri mRcsGroupMessageThreadUri = Uri.parse("content://rcsim/rcs_group_message");
    private static final Uri mRcsImThreadUri = Uri.parse("content://rcsim/chat");
    private static int rcsEnabled = -1;
    public ENUM_MESSAGE_TYPE sNewThreadType = ENUM_MESSAGE_TYPE.UNUSED;

    public enum ENUM_MESSAGE_TYPE {
        UNUSED,
        SMS,
        MMS,
        IM,
        GROUPCHAT
    }

    private static /* synthetic */ int[] -getcom-huawei-keyguard-events-RcsMessageMonitorImpl$ENUM_MESSAGE_TYPESwitchesValues() {
        if (-com-huawei-keyguard-events-RcsMessageMonitorImpl$ENUM_MESSAGE_TYPESwitchesValues != null) {
            return -com-huawei-keyguard-events-RcsMessageMonitorImpl$ENUM_MESSAGE_TYPESwitchesValues;
        }
        int[] iArr = new int[ENUM_MESSAGE_TYPE.values().length];
        try {
            iArr[ENUM_MESSAGE_TYPE.GROUPCHAT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ENUM_MESSAGE_TYPE.IM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ENUM_MESSAGE_TYPE.MMS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ENUM_MESSAGE_TYPE.SMS.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ENUM_MESSAGE_TYPE.UNUSED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -com-huawei-keyguard-events-RcsMessageMonitorImpl$ENUM_MESSAGE_TYPESwitchesValues = iArr;
        return iArr;
    }

    public static boolean isRCSEnable() {
        if (rcsEnabled == -1) {
            boolean enable;
            int i;
            if (SystemProperties.getBoolean("ro.config.hw_rcs_product", false)) {
                enable = SystemProperties.getBoolean("ro.config.hw_rcs_vendor", false);
            } else {
                enable = false;
            }
            if (enable) {
                i = 1;
            } else {
                i = 0;
            }
            rcsEnabled = i;
        }
        if (1 == rcsEnabled) {
            return true;
        }
        return false;
    }

    public long onQueryDatabase(Context context, long sNewThreadId, Cursor c, String[] PROJECTION, String NEW_INCOMING_SM_CONSTRAINT, MessageInfo info) {
        ContentResolver resolver = null;
        if (!isRCSEnable()) {
            return sNewThreadId;
        }
        if (info == null) {
            HwLog.w("RcsMessageMonitorImpl", "onQueryDatabase with empty info");
        }
        if (context != null) {
            resolver = context.getContentResolver();
        }
        if (resolver == null) {
            HwLog.e("RcsMessageMonitorImpl", "onQueryDatabase with invalide context: " + context);
            return sNewThreadId;
        }
        return onQueryRcsGroupThreadUri(resolver, onQueryRcsImThreadUri(resolver, sNewThreadId, PROJECTION, NEW_INCOMING_SM_CONSTRAINT, info), PROJECTION, NEW_INCOMING_SM_CONSTRAINT, info);
    }

    private long onQueryRcsImThreadUri(ContentResolver resolver, long sNewThreadId, String[] PROJECTION, String NEW_INCOMING_SM_CONSTRAINT, MessageInfo info) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(mRcsImThreadUri, PROJECTION, "((type = 1 OR type = 101) AND read=0 AND seen = 0)", null, null);
            if (cursor == null || cursor.getCount() == 0) {
                HwLog.w("RcsMessageMonitorImpl", "onQueryDatabase query im fail");
                if (cursor != null) {
                    cursor.close();
                }
                return sNewThreadId;
            }
            if (info != null) {
                info.mUnReadCount += cursor.getCount();
            }
            HwLog.d("RcsMessageMonitorImpl", " unread and unseen im message :" + cursor.getCount());
            if (cursor.moveToLast()) {
                int index = cursor.getColumnIndex("thread_id");
                if (index > -1) {
                    sNewThreadId = cursor.getLong(index);
                    this.sNewThreadType = ENUM_MESSAGE_TYPE.IM;
                }
                if (info != null) {
                    index = cursor.getColumnIndex("address");
                    if (index > -1) {
                        info.mPhoneNum = cursor.getString(index);
                    }
                    index = cursor.getColumnIndex("body");
                    if (index > -1) {
                        info.mMsgContent = cursor.getString(index);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return sNewThreadId;
        } catch (SQLiteException e) {
            HwLog.e("RcsMessageMonitorImpl", "Query im fail", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            HwLog.e("RcsMessageMonitorImpl", "Query im fail", e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private long onQueryRcsGroupThreadUri(ContentResolver resolver, long sNewThreadId, String[] PROJECTION, String NEW_INCOMING_SM_CONSTRAINT, MessageInfo info) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(mRcsGroupMessageThreadUri, PROJECTION, "((type = 1 OR type = 101) AND read=0 AND seen = 0)", null, null);
            if (cursor == null || cursor.getCount() == 0) {
                HwLog.w("RcsMessageMonitorImpl", "onQueryDatabase query group message fail");
                if (cursor != null) {
                    cursor.close();
                }
                return sNewThreadId;
            }
            if (info != null) {
                info.mUnReadCount += cursor.getCount();
            }
            HwLog.d("RcsMessageMonitorImpl", "unread and unseen group message :" + cursor.getCount());
            if (cursor.moveToLast()) {
                int index = cursor.getColumnIndex("thread_id");
                if (index > -1) {
                    sNewThreadId = cursor.getLong(index);
                    this.sNewThreadType = ENUM_MESSAGE_TYPE.GROUPCHAT;
                }
                if (info != null) {
                    index = cursor.getColumnIndex("address");
                    if (index > -1) {
                        info.mPhoneNum = cursor.getString(index);
                    }
                    index = cursor.getColumnIndex("body");
                    if (index > -1) {
                        info.mMsgContent = cursor.getString(index);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return sNewThreadId;
        } catch (SQLiteException e) {
            HwLog.e("RcsMessageMonitorImpl", "Query group fail", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            HwLog.e("RcsMessageMonitorImpl", "Query group fail", e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Intent getRCSIMIntent(String packageName, long threadId) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClassName(packageName, "com.android.mms.ui.ComposeMessageActivity");
        intent.putExtra("conversation_mode", 2);
        intent.setData(ContentUris.withAppendedId(RCS_URI_CONVERSATIONS, threadId).buildUpon().appendQueryParameter("threadType", String.valueOf(2)).build());
        intent.setFlags(805306368);
        return intent;
    }

    private Intent getRCSGroupIntent(String packageName, long threadId) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClassName(packageName, "com.android.mms.ui.GroupChatComposeMessageActivity");
        intent.putExtra("bundle_group_thread_id", threadId);
        intent.setData(ContentUris.withAppendedId(RCS_URI_CONVERSATIONS, threadId).buildUpon().build());
        intent.setFlags(805306368);
        return intent;
    }

    public void addXmsModeToIntent(Intent intent) {
        if (isRCSEnable() && intent != null) {
            intent.putExtra("conversation_mode", 1);
        }
    }

    public Intent getRCSMmsIntent(Intent intent, String packageName, long threadId) {
        if (!isRCSEnable()) {
            return intent;
        }
        Intent newIntent;
        switch (-getcom-huawei-keyguard-events-RcsMessageMonitorImpl$ENUM_MESSAGE_TYPESwitchesValues()[this.sNewThreadType.ordinal()]) {
            case 1:
                newIntent = getRCSGroupIntent(packageName, threadId);
                break;
            case 2:
                newIntent = getRCSIMIntent(packageName, threadId);
                break;
            case 3:
            case 4:
                newIntent = intent;
                break;
            default:
                newIntent = new Intent();
                break;
        }
        return newIntent;
    }

    public void setNewThreadType(ENUM_MESSAGE_TYPE type) {
        if (isRCSEnable()) {
            this.sNewThreadType = type;
        }
    }
}
