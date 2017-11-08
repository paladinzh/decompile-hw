package com.android.rcs.transaction;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import com.android.common.contacts.DataUsageStatUpdater;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.CspFragment;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.utils.RcsProfile;
import java.util.ArrayList;

public class RcsSmsReceiverService {
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private Context mContext;

    public void setHwCustSmsReceiverService(Context context) {
        if (mIsRcsOn) {
            this.mContext = context;
        }
    }

    public void handleMessageReceived(String action, Intent intent, Handler handler) {
        if (!mIsRcsOn) {
            return;
        }
        if ("com.huawei.im.broadcast.MESSAGE".equals(action)) {
            handleImMsgReceived(intent);
        } else if ("com.huawei.rcs.message.errorcode".equals(action)) {
            handleSendImMsgError(intent, handler);
        } else if ("com.huawei.rcs.message.groupinvite".equals(action)) {
            handleGroupInvite(intent);
        } else if ("com.huawei.rcs.message.groupcreated".equals(action)) {
            handleGroupCreate(intent, handler);
        } else if ("com.huawei.rcs.ft.file.invite".equals(action)) {
            handleFtFileReceived(intent);
        }
    }

    private void handleImMsgReceived(Intent aIntent) {
        Cursor cur;
        Uri messageUri;
        int IncomingMessageType;
        long msgId = aIntent.getLongExtra("msgId", -1);
        int aThreadId = -1;
        if (aIntent.getIntExtra("msgChatType", -1) == 2) {
            cur = SqliteWrapper.query(this.mContext, Uri.parse("content://rcsim/rcs_group_message"), new String[]{"thread_id"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
            messageUri = ContentUris.withAppendedId(Uri.parse("content://rcsim/rcs_group_message"), msgId);
            IncomingMessageType = 3;
        } else {
            Uri messageUri2 = ContentUris.withAppendedId(Uri.parse("content://rcsim/chat"), msgId);
            cur = SqliteWrapper.query(this.mContext, Uri.parse("content://rcsim/chat"), new String[]{"thread_id", "address"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
            if (cur != null && cur.moveToFirst()) {
                int index = cur.getColumnIndex("address");
                if (index != -1) {
                    String number = cur.getString(index);
                    updateSendStats(this.mContext, number);
                }
            }
            IncomingMessageType = 2;
            messageUri = messageUri2;
        }
        if (cur != null) {
            try {
                if (cur.moveToFirst()) {
                    aThreadId = cur.getInt(cur.getColumnIndex("thread_id"));
                }
            } catch (Exception e) {
                MLog.e("RcsSmsReceiverService", "handleImMsgReceived occur exception " + e);
                if (cur != null) {
                    cur.close();
                }
            } catch (Throwable th) {
                if (cur != null) {
                    cur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        RcsMessagingNotification.blockingUpdateNewMessageIndicator(this.mContext, (long) aThreadId, false, messageUri, IncomingMessageType, null);
        CspFragment.setNotificationCleared(false);
    }

    private void updateSendStats(Context context, String phoneNumber) {
        DataUsageStatUpdater updater = new DataUsageStatUpdater(context);
        try {
            ArrayList<String> formatNumbers = new ArrayList();
            formatNumbers.add(HwMessageUtils.replaceNumberFromDatabase(phoneNumber, context.getApplicationContext()));
            updater.updateWithPhoneNumber(formatNumbers);
        } catch (SQLiteException e) {
            MLog.e("RcsSmsReceiverService", "too many SQL variables");
        }
    }

    private void handleSendImMsgError(final Intent aIntent, Handler mToastHandler) {
        mToastHandler.post(new Runnable() {
            public void run() {
                switch (aIntent.getIntExtra("rcs.message.error", -1)) {
                    case 1:
                        ResEx.makeToast(RcsSmsReceiverService.this.mContext.getString(R.string.IM_error_not_support_2G), 0);
                        return;
                    case 3:
                        MLog.i("RcsSmsReceiverService", "handleSendImMsgError IM not support offline message");
                        return;
                    case 5:
                        MLog.i("RcsSmsReceiverService", "handleSendImMsgError IM not support in temp group");
                        return;
                    case 6:
                        ResEx.makeToast(RcsSmsReceiverService.this.mContext.getString(R.string.IM_error_maxsize), 0);
                        return;
                    default:
                        return;
                }
            }
        });
    }

    private void handleGroupInvite(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (intent.getBooleanExtra("group_invite_clear", false)) {
                RcsMessagingNotification.clearGroupInviteNotification(this.mContext);
                MLog.d("RcsSmsReceiverService", "handleGroupInvite Handle clear group invite notifications");
                return;
            }
            String globalgroupId = bundle.getString("globalgroupId");
            String chairMan = bundle.getString("chairMan");
            String topic = bundle.getString("topic");
            if (checkGroupReady(globalgroupId)) {
                acceptGroupInvite(globalgroupId);
            } else {
                MLog.d("RcsSmsReceiverService", "handleGroupInvite Handle group invite globalgroupId : " + globalgroupId);
                RcsMessagingNotification.notifyNewGroupInviteIndicator(this.mContext, globalgroupId, chairMan, topic);
            }
        }
    }

    private void handleGroupCreate(Intent intent, Handler mToastHandler) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            boolean estabStatus = intent.getBooleanExtra("status", false);
            if (RcsProfile.isGroupAutoAccept() && estabStatus) {
                String topicStr = bundle.getString("topic");
                RcsMessagingNotification.notifyNewGroupCreatedIndicator(this.mContext, intent.getStringExtra("groupId"), null, topicStr);
            } else {
                MLog.w("RcsSmsReceiverService", "handleGroupCreate estabStatus: " + estabStatus);
            }
        }
    }

    private void handleFtFileReceived(Intent aIntent) {
        if (aIntent != null && aIntent.getExtras() != null) {
            Cursor cur;
            int IncomingMessageType;
            Uri messageUri;
            long msgId = aIntent.getLongExtra("msg_id", -1);
            int aThreadId = -1;
            Bundle bundle = aIntent.getExtras();
            int chatType = 1;
            if (bundle != null) {
                chatType = (int) bundle.getLong("msgChatType");
            }
            Uri messageUri2;
            if (chatType == 2) {
                messageUri2 = ContentUris.withAppendedId(Uri.parse("content://rcsim/rcs_group_message"), msgId);
                cur = SqliteWrapper.query(this.mContext, Uri.parse("content://rcsim/rcs_group_message"), new String[]{"thread_id"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
                IncomingMessageType = 3;
                messageUri = messageUri2;
            } else {
                messageUri2 = ContentUris.withAppendedId(Uri.parse("content://rcsim/chat"), msgId);
                cur = SqliteWrapper.query(this.mContext, Uri.parse("content://rcsim/chat"), new String[]{"thread_id"}, "_id = ?", new String[]{String.valueOf(msgId)}, null);
                IncomingMessageType = 2;
                messageUri = messageUri2;
            }
            if (cur != null) {
                try {
                    if (cur.moveToFirst()) {
                        aThreadId = cur.getInt(cur.getColumnIndex("thread_id"));
                    }
                } catch (Exception e) {
                    MLog.e("RcsSmsReceiverService", "handleFtFileReceived occur exception " + e);
                    if (cur != null) {
                        cur.close();
                    }
                } catch (Throwable th) {
                    if (cur != null) {
                        cur.close();
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
            RcsMessagingNotification.blockingUpdateNewMessageIndicator(this.mContext, (long) aThreadId, false, messageUri, IncomingMessageType, bundle);
            CspFragment.setNotificationCleared(false);
        }
    }

    private boolean checkGroupReady(String globalgroupId) {
        int status = 0;
        Cursor cur = SqliteWrapper.query(this.mContext, Uri.parse("content://rcsim/rcs_groups"), new String[]{"status"}, "global_group_id = ?", new String[]{globalgroupId}, null);
        if (cur != null) {
            try {
                if (cur.moveToFirst()) {
                    status = cur.getInt(cur.getColumnIndex("status"));
                }
            } catch (Exception e) {
                MLog.e("RcsSmsReceiverService", "checkGroupReady occur exception " + e);
                if (cur != null) {
                    cur.close();
                }
            } catch (Throwable th) {
                if (cur != null) {
                    cur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        MLog.d("RcsSmsReceiverService", "checkGroupReady status: " + status);
        return status > 0 && status != 3;
    }

    private void acceptGroupInvite(String globalgroupId) {
        try {
            if (RcsProfile.getRcsService() == null) {
                MLog.e("RcsSmsReceiverService", "acceptGroupInvite getRcsService is null");
            } else {
                RcsProfile.getRcsService().acceptGroupInviteAccept(globalgroupId);
            }
        } catch (RemoteException aRme) {
            MLog.e("RcsSmsReceiverService", "acceptGroupInvite accept group failed exception : " + aRme.getMessage());
        }
    }
}
