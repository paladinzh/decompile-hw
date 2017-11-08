package com.huawei.mms.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.HwCustHttpUtilsImpl;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.widget.MmsWidgetProvider;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.CspFragment;
import java.util.List;

public class FloatMmsRequsetReceiver extends BroadcastReceiver {
    private static final Uri CONTENT_URI = Uri.parse("content://com.huawei.vdrive/setting");

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(Context context, Intent intent) {
        MLog.d("FloatMmsRequsetHandle", "FloatMms FloatMmsRequsetReceiver get action ");
        if (intent != null && intent.getAction() != null) {
            MLog.d("FloatMmsRequsetHandle", "FloatMms FloatMmsRequsetReceiver get action: " + intent.getAction());
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (!(action == null || bundle == null || !"android.intent.actions.floatmms.request".equals(action))) {
                handleRequest(context, bundle);
            }
        }
    }

    public void handleRequest(Context context, Bundle bundle) {
        String cmd = bundle.getString("REQTYP");
        if (!TextUtils.isEmpty(cmd)) {
            if ("SAVE".equals(cmd)) {
                saveDraft(context, bundle);
            } else if ("SEND".equals(cmd)) {
                sendSms(context, bundle, bundle.getInt("subscription", -1));
            } else if ("UPDATE".equals(cmd)) {
                cancelNewMessageNotification(context);
                notifyWidgetDataChanged(context);
            } else if ("MARK_READ".equals(cmd)) {
                markMessageAsRead(context, bundle);
            } else if ("DELETE".equals(cmd)) {
                deleteSms(context, bundle);
            }
        }
    }

    public static boolean deleteSms(Context context, Bundle bundle) {
        String strUri = bundle.getString("uri");
        if (TextUtils.isEmpty(strUri)) {
            MLog.e("FloatMmsRequsetHandle", "Delete from Float with invalide param");
            return false;
        }
        try {
            StatisticalHelper.incrementReportCount(context, 2059);
            if (HwMessageUtils.isInfoMsg(context, Uri.parse(strUri))) {
                StatisticalHelper.incrementReportCount(context, 2060);
            }
            SqliteWrapper.delete(context, context.getContentResolver(), Uri.parse(strUri), null, null);
        } catch (Exception e) {
            MLog.w("FloatMmsRequsetHandle", "deleteSms error e=" + e);
        }
        return true;
    }

    private void markMessageAsRead(Context context, Bundle bundle) {
        if (bundle == null) {
            MLog.w("FloatMmsRequsetHandle", "markMessageAsRead bundle is null");
            return;
        }
        ContentValues contentValues = new ContentValues(2);
        contentValues.put("read", Integer.valueOf(bundle.getInt("read")));
        contentValues.put("seen", Integer.valueOf(bundle.getInt("seen")));
        Context context2 = context;
        SqliteWrapper.update(context2, context.getContentResolver(), ContentUris.withAppendedId(Sms.CONTENT_URI, bundle.getLong("_id")), contentValues, null, null);
    }

    public static boolean saveDraft(Context context, Bundle bundle) {
        String msgbody = bundle.getString("body");
        long threadId = Conversation.getOrCreateThreadId(context, bundle.getString("receiver"));
        if (threadId <= 0) {
            return false;
        }
        HwMessageUtils.saveDraft(context, threadId, msgbody, bundle.getInt("subscription", -1));
        return true;
    }

    public static void sendSms(Context context, Bundle bundle, int subscription) {
        String msgbody = bundle.getString("body");
        String receiver = bundle.getString("receiver");
        long threadId = Conversation.getOrCreateThreadId(context, receiver);
        MLog.d("FloatMmsRequsetHandle", "sendSms for the receiver and msgbody.");
        if (!TextUtils.isEmpty(msgbody) && !TextUtils.isEmpty(receiver)) {
            HwMessageUtils.sendSms(context, threadId, msgbody, subscription);
        }
    }

    public static void noticeNewMessage(Context context, Long threadId, Uri messageUri) {
        boolean isPopupMsgEnabled;
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(context);
        if (MmsConfig.isEnablePopupMessage()) {
            isPopupMsgEnabled = sharePref.getBoolean("pref_key_enable_popup_message", true);
        } else {
            isPopupMsgEnabled = false;
        }
        if (!isPopupMsgEnabled || ((isProcessRunning(context, "com.huawei.vdrive") && isInDriverMode(context)) || HwMessageUtils.isInKidsmodes(context))) {
            MLog.d("FloatMmsRequsetHandle", "noticeNewMessage: isPopupMsgEnabled = " + isPopupMsgEnabled);
        } else if (MmsConfig.isCoexistWithMms() || !isInMmsView(context)) {
            try {
                startPopupMsgAcitvity(context, messageUri);
            } catch (ActivityNotFoundException e) {
                MLog.d("FloatMmsRequsetHandle", "startPopupMsgAcitvity Has Exeption as HwFloatMms not installed ");
            }
        } else {
            MLog.d("FloatMmsRequsetHandle", "noticeNewMessage: isInMmsView");
        }
    }

    private static void startPopupMsgAcitvity(Context context, Uri msgUri) {
        Intent intent = new Intent("android.intent.actions.FLOAT_NEW_MSG");
        intent.setComponent(new ComponentName("com.huawei.floatMms", "com.huawei.floatMms.FloatMmsCmdService"));
        intent.setData(msgUri.normalizeScheme());
        context.startService(intent);
    }

    public static void stopPopupMsgAcitvity(Context context) {
        if (!MmsConfig.isCoexistWithMms()) {
            context.sendBroadcast(new Intent("huawei.floatapp.notice.orginapp.launch.mms"));
        }
    }

    public static void cancelNewMessageNotification(Context context) {
        Conversation.markAllConversationsAsSeen(context);
        MessagingNotification.cancelNotification(context, 123);
    }

    public static void notifyWidgetDataChanged(Context context) {
        MmsWidgetProvider.notifyDatasetChanged(context);
    }

    public static boolean isProcessRunning(Context context, String processName) {
        if (TextUtils.isEmpty(processName)) {
            return false;
        }
        try {
            List<RunningAppProcessInfo> run = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
            if (run == null) {
                return false;
            }
            for (RunningAppProcessInfo ra : run) {
                if (processName.equals(ra.processName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized boolean isInDriverMode(Context context) {
        synchronized (FloatMmsRequsetReceiver.class) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            boolean isDriverMode = false;
            try {
                cursor = resolver.query(CONTENT_URI, new String[]{HwCustHttpUtilsImpl.CHAMELEON_COLUMNS_VALUE}, "name = ?", new String[]{"vdrive_state"}, null);
                if (cursor == null) {
                    MLog.w("FloatMmsRequsetHandle", "Can't get key vdrive_state from " + CONTENT_URI);
                    if (cursor != null) {
                        cursor.close();
                    }
                } else {
                    Object value = null;
                    if (cursor.moveToNext()) {
                        value = cursor.getString(0);
                    }
                    if (!TextUtils.isEmpty(value) && 1 == Integer.parseInt(value)) {
                        isDriverMode = true;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                MLog.e("FloatMmsRequsetHandle", "query the driver database exception" + e);
                if (cursor != null) {
                    cursor.close();
                }
                return isDriverMode;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private static boolean isInMmsView(Context context) {
        List<RunningTaskInfo> rti = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(2);
        if (rti == null) {
            return false;
        }
        String packageName = ((RunningTaskInfo) rti.get(0)).topActivity.getPackageName();
        String className = ((RunningTaskInfo) rti.get(0)).topActivity.getClassName();
        MLog.d("FloatMmsRequsetHandle", "package0=" + packageName + "class0=" + className);
        if (!packageName.equals(context.getApplicationInfo().packageName)) {
            return false;
        }
        if (className.endsWith(ComposeMessageActivity.class.getSimpleName())) {
            return true;
        }
        return CspFragment.isFragmentActived();
    }
}
