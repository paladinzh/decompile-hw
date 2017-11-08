package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.PinUpdateRequeset;
import com.android.mms.util.DraftCache;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.NumberUtils;
import java.util.ArrayList;

public class MmsPermReceiver extends BroadcastReceiver {
    private static ArrayList<IEventListener> sEventListener = null;

    public interface IEventListener {
        boolean onReceive(Context context, Intent intent);
    }

    private static class MultiUserEventListener implements IEventListener {
        private MultiUserEventListener() {
        }

        public boolean onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.huawei.intent.mms.action.SCHEDUL_MMS".equals(action)) {
                if (intent.hasExtra("uri")) {
                    TransactionService.startMe(context, intent.getStringExtra(NumberInfo.TYPE_KEY), intent.getIntExtra(NumberInfo.TYPE_KEY, 2));
                } else {
                    TransactionService.startMe(context);
                }
                return true;
            }
            if ("com.huawei.mms.action.subuser.event".equals(action)) {
                handleSubUserCommAction(context, intent, action, intent.getStringExtra("CMD_TYP"));
            }
            return false;
        }

        private void handleSubUserCommAction(Context context, Intent intent, String action, String cmd) {
            if (intent.getIntExtra("SENDER_PID", 0) != Process.myPid()) {
                if (((UserManager) context.getSystemService("user")).hasUserRestriction("no_sms")) {
                    MLog.i("MmsPermReceiver", "Ignore SubUserCommAction as restricted");
                } else if (intent.getIntExtra("mms_notification_id", -1) == 123) {
                    MessagingNotification.cancelNotificationOfOwner(context);
                } else if ("UPD_PINUP_THREAD".equals(cmd)) {
                    long[] allThreads = intent.getLongArrayExtra("ALL_THREADS");
                    if (allThreads != null && intent.hasExtra("IS_PINUP")) {
                        boolean state = intent.getBooleanExtra("IS_PINUP", false);
                        ArrayList<Long> threads = new ArrayList(allThreads.length);
                        for (long valueOf : allThreads) {
                            threads.add(Long.valueOf(valueOf));
                        }
                        Conversation.updatePinupCacheWithoutNotice(new PinUpdateRequeset(state, threads));
                    }
                } else {
                    long tid = intent.getLongExtra("thread_id", 0);
                    if (tid != 0) {
                        MLog.e("MmsPermReceiver", "No Id is assigned for " + cmd);
                    }
                    if ("UPD_CURRENT_UI_THREAD".equals(cmd)) {
                        MessagingNotification.setCurrentlyDisplayedThreadIdWithoutNotice(tid);
                    } else if ("UPD_DRAFT_THREAD".equals(cmd)) {
                        if (intent.hasExtra("IS_DRAFT")) {
                            DraftCache.getInstance().changeDraftState(tid, intent.getBooleanExtra("IS_DRAFT", false));
                        }
                    } else if ("UPD_HUAWEI_NAME".equals(cmd)) {
                        Conversation.updateHwSendNameWithoutNotice(context, intent.getLongExtra("thread_id", 0), intent.getStringExtra("SENDER_ADDRESS"), intent.getStringExtra("SNIPPET"));
                    }
                }
            }
        }
    }

    private static synchronized ArrayList<IEventListener> getEventListener() {
        synchronized (MmsPermReceiver.class) {
            if (sEventListener != null) {
                ArrayList<IEventListener> arrayList = sEventListener;
                return arrayList;
            }
            sEventListener = new ArrayList();
            sEventListener.add(NotificationReceiver.getInst());
            sEventListener.add(new MultiUserEventListener());
            arrayList = sEventListener;
            return arrayList;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            MLog.e("MmsPermReceiver", "MmsPermReceiver get empty message");
            return;
        }
        ArrayList<IEventListener> listeners = getEventListener();
        int size = listeners.size();
        int i = 0;
        while (i < size) {
            if (!((IEventListener) listeners.get(i)).onReceive(context, intent)) {
                i++;
            } else {
                return;
            }
        }
        MLog.e("MmsPermReceiver", "MmsPermReceiver action  maybe unhandled." + intent.getAction());
    }

    public static void broadcastForSendMms(Context context) {
        context.sendBroadcastAsUser(new Intent("com.huawei.intent.mms.action.SCHEDUL_MMS", null, context, MmsPermReceiver.class), UserHandle.OWNER);
    }

    public static void broadcastForSendMms(Context context, String action) {
        Intent intent = new Intent("com.huawei.intent.mms.action.SCHEDUL_MMS", null, context, MmsPermReceiver.class);
        intent.putExtra("transaction_start_action", action);
        context.sendBroadcastAsUser(intent, UserHandle.OWNER);
    }

    public static void broadcastForSendMms(Context context, String uri, int type) {
        Intent intent = new Intent("com.huawei.intent.mms.action.SCHEDUL_MMS", null, context, MmsPermReceiver.class);
        intent.putExtra("uri", uri);
        intent.putExtra(NumberInfo.TYPE_KEY, type);
        context.sendBroadcastAsUser(intent, UserHandle.OWNER);
    }

    private static Intent getSubUserIntent(Context context) {
        Intent intent = new Intent("com.huawei.mms.action.subuser.event", null, context, MmsPermReceiver.class);
        intent.putExtra("SENDER_PID", Process.myPid());
        return intent;
    }

    public static Intent getNotificationDeleteIntent(Context context) {
        Intent intent = new Intent("com.huawei.mms.action.subuser.event", null, context, MmsPermReceiver.class);
        intent.putExtra("SENDER_PID", Process.myPid());
        intent.putExtra("mms_notification_id", 123);
        return intent;
    }

    public static void noticeCurrentThreadId(Context context, long threadId) {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            Intent intent = getSubUserIntent(context);
            intent.putExtra("CMD_TYP", "UPD_CURRENT_UI_THREAD");
            intent.putExtra("thread_id", threadId);
            context.sendBroadcastAsUser(intent, UserHandle.OWNER);
        }
    }

    public static void noticeThreadDraftState(long threadId, boolean hasDraft) {
        Context context = MmsApp.getApplication();
        if (OsUtil.isAtLeastL() && OsUtil.isSupportMultiUser(context)) {
            Intent intent = getSubUserIntent(context);
            intent.putExtra("CMD_TYP", "UPD_DRAFT_THREAD");
            intent.putExtra("IS_DRAFT", hasDraft);
            intent.putExtra("thread_id", threadId);
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public static void noticeThreadPinupState(PinUpdateRequeset request) {
        Context context = MmsApp.getApplication();
        if (OsUtil.isAtLeastL() && OsUtil.isSupportMultiUser(context)) {
            if (request.getThreads().length == 0) {
                MLog.w("MmsPermReceiver", "Skip Notice ThreadPinupState");
                return;
            }
            Intent intent = getSubUserIntent(context);
            intent.putExtra("CMD_TYP", "UPD_PINUP_THREAD");
            intent.putExtra("ALL_THREADS", request.getThreads());
            intent.putExtra("IS_PINUP", request.isPinup());
            MLog.d("MmsPermReceiver", "Notice ThreadPinupState");
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void noticeHwSendName(Context context, long threadId, String address, String snippet) {
        if (threadId > 0 && NumberUtils.isHwMessageNumber(address) && OsUtil.isAtLeastL() && OsUtil.isSupportMultiUser(context)) {
            Intent intent = getSubUserIntent(context);
            intent.putExtra("CMD_TYP", "UPD_HUAWEI_NAME");
            intent.putExtra("SENDER_ADDRESS", address);
            intent.putExtra("SNIPPET", snippet);
            intent.putExtra("thread_id", threadId);
            MLog.d("MmsPermReceiver", "Notice HwSendName");
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }
}
