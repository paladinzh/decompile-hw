package com.android.mms.transaction;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.Mms.Outbox;
import android.provider.Telephony.MmsSms.PendingMessages;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.DownloadManager;
import com.google.android.gms.R;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.MmsRadarInfoManager;

public class RetryScheduler implements Observer {
    private static RetryScheduler sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;

    private RetryScheduler(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public static RetryScheduler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RetryScheduler(context);
        }
        return sInstance;
    }

    public void update(Observable observable) {
        Transaction t;
        try {
            boolean isDownLoad;
            t = (Transaction) observable;
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.v("MSG_APP_RetryScheduler", "[RetryScheduler] update " + observable);
            }
            MmsRadarInfoManager mmsRadarInfoManager = MmsRadarInfoManager.getInstance();
            if (t instanceof NotificationTransaction) {
                isDownLoad = true;
            } else {
                isDownLoad = t instanceof RetrieveTransaction;
            }
            boolean isSend = t instanceof SendTransaction;
            if (isDownLoad || isSend || (t instanceof ReadRecTransaction)) {
                TransactionState state = t.getState();
                if (state.getState() == 2) {
                    Uri uri = state.getContentUri();
                    if (uri != null) {
                        scheduleRetry(uri);
                    }
                } else {
                    if (isDownLoad) {
                        mmsRadarInfoManager.reportReceiveOrSendResult(true, 1332, 0, 0, "");
                    }
                    if (isSend) {
                        mmsRadarInfoManager.reportReceiveOrSendResult(true, 1331, 0, 0, "");
                    }
                }
                t.detach(this);
            }
            setRetryAlarm(this.mContext);
        } catch (Throwable th) {
            setRetryAlarm(this.mContext);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void scheduleRetry(Uri uri) {
        long msgId = ContentUris.parseId(uri);
        Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContentResolver, uriBuilder.build(), null, null, null, null);
        MLog.d("MSG_APP_RetryScheduler", "scheduleRetry cursor: " + cursor);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    int msgType = cursor.getInt(cursor.getColumnIndexOrThrow("msg_type"));
                    int retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                    int errorType = 1;
                    DefaultRetryScheme defaultRetryScheme = new DefaultRetryScheme(this.mContext, retryIndex);
                    ContentValues contentValues = new ContentValues(4);
                    long current = System.currentTimeMillis();
                    boolean isRetryDownloading = msgType == 130;
                    boolean retry = true;
                    int respStatus = getResponseStatus(msgId);
                    int errorString = 0;
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("MSG_APP_RetryScheduler", "[RetryScheduler] entry msgType = " + msgType + "retryIndex = " + retryIndex + "errorType = " + 1 + "isRetryDownloading = " + isRetryDownloading + "respStatus = " + respStatus);
                    }
                    if (isRetryDownloading) {
                        respStatus = getRetrieveStatus(msgId);
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("MSG_APP_RetryScheduler", "MSG_APP_respStatus = " + respStatus);
                        }
                        if (respStatus == 228) {
                            DownloadManager.getInstance().showErrorCodeToast(R.string.service_message_not_found_Toast);
                            SqliteWrapper.delete(this.mContext, this.mContext.getContentResolver(), uri, null, null);
                            cursor.close();
                            return;
                        } else if (respStatus == 130) {
                            DownloadManager.getInstance().showErrorCodeToast(R.string.service_not_activated);
                            retry = false;
                        }
                    } else {
                        switch (respStatus) {
                            case 130:
                            case 225:
                                errorString = R.string.service_not_activated;
                                break;
                            case 132:
                                errorString = R.string.invalid_destination;
                                break;
                            case 134:
                                errorString = R.string.service_network_problem;
                                break;
                            case 194:
                            case 228:
                                errorString = R.string.service_message_not_found_Toast;
                                break;
                            default:
                                MLog.d("MSG_APP_RetryScheduler", "RetryScheduler scheduleRetry with error respStatus");
                                break;
                        }
                        if (errorString != 0) {
                            DownloadManager.getInstance().showErrorCodeToast(errorString);
                            retry = false;
                        }
                    }
                    int retryLimitNum = defaultRetryScheme.getRetryLimit();
                    if (MessageUtils.isMultiSimEnabled()) {
                        retryLimitNum = getRetryLimitNum(uri, retryLimitNum);
                    }
                    if (retryIndex >= retryLimitNum || !retry) {
                        errorType = 10;
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("MSG_APP_RetryScheduler", "MSG_APP_[RetryScheduler] PERMANENT fail");
                        }
                        MmsRadarInfoManager mmsRadarInfoManager = MmsRadarInfoManager.getInstance();
                        int sub = Transaction.querySubscription(this.mContext, uri);
                        if (130 == msgType || 132 == msgType) {
                            mmsRadarInfoManager.reportReceiveOrSendResult(false, 1332, sub, respStatus, "mms rec fail");
                        } else {
                            mmsRadarInfoManager.reportReceiveOrSendResult(false, 1331, sub, respStatus, "mms send fail");
                        }
                        if (isRetryDownloading) {
                            Cursor c = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uri, new String[]{"thread_id"}, null, null, null);
                            long threadId = -1;
                            if (c != null) {
                                if (c.moveToFirst()) {
                                    threadId = c.getLong(0);
                                }
                                c.close();
                            }
                            if (threadId != -1) {
                                MessagingNotification.notifyDownloadFailed(this.mContext, threadId);
                            }
                            DownloadManager.getInstance().markState(uri, 135);
                            MessageListAdapter.saveConnectionManagerToMap(uri.toString(), MessageListAdapter.getUserStopTransaction(uri.toString()), false, true, MessageListAdapter.getMmsTransactionCleintFromMap(uri.toString()));
                        } else {
                            ContentValues readValues = new ContentValues(1);
                            readValues.put("read", Integer.valueOf(0));
                            SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), uri, readValues, null, null);
                            MessagingNotification.notifySendFailed(this.mContext, true);
                        }
                    } else {
                        long retryAt = current + defaultRetryScheme.getWaitingInterval();
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("MSG_APP_RetryScheduler", "scheduleRetry: retry for " + uri + " is scheduled at " + (retryAt - System.currentTimeMillis()) + "ms from now");
                        }
                        contentValues.put("due_time", Long.valueOf(retryAt));
                        if (MessageListAdapter.getManualDownloadFromMap(uri.toString())) {
                            MessageListAdapter.saveConnectionManagerToMap(uri.toString(), MessageListAdapter.getUserStopTransaction(uri.toString()), false, true, MessageListAdapter.getMmsTransactionCleintFromMap(uri.toString()));
                        }
                        if (isRetryDownloading) {
                            DownloadManager.getInstance().markState(uri, 130);
                        }
                    }
                    contentValues.put("err_type", Integer.valueOf(errorType));
                    contentValues.put("retry_index", Integer.valueOf(retryIndex));
                    contentValues.put("last_try", Long.valueOf(current));
                    SqliteWrapper.update(this.mContext, this.mContentResolver, PendingMessages.CONTENT_URI, contentValues, "_id=" + cursor.getLong(cursor.getColumnIndexOrThrow("_id")), null);
                } else {
                    MLog.v("MSG_APP_RetryScheduler", "Cannot found correct pending status for: " + msgId);
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
    }

    private int getResponseStatus(long msgID) {
        int respStatus = 0;
        Cursor cursor;
        try {
            cursor = SqliteWrapper.query(this.mContext, this.mContentResolver, Outbox.CONTENT_URI, null, "_id=" + msgID, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    respStatus = cursor.getInt(cursor.getColumnIndexOrThrow("resp_st"));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("MSG_APP_RetryScheduler", " cursor closed  unformally");
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (respStatus != 0) {
            MLog.e("MSG_APP_RetryScheduler", "Response status is: " + respStatus);
        }
        return respStatus;
    }

    private int getRetrieveStatus(long msgID) {
        int retrieveStatus = 0;
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContentResolver, Inbox.CONTENT_URI, null, "_id=" + msgID, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    retrieveStatus = cursor.getInt(cursor.getColumnIndexOrThrow("resp_st"));
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        if (retrieveStatus != 0 && MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("MSG_APP_RetryScheduler", "Retrieve status is: " + retrieveStatus);
        }
        return retrieveStatus;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getRetryLimitNum(Uri uri, int num) {
        int retryLimit = num;
        int ddsSub = MessageUtils.getMmsAutoSetDataSubscription();
        int curSub = ddsSub;
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContentResolver, uri, new String[]{"sub_id"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    curSub = cursor.getInt(cursor.getColumnIndexOrThrow("sub_id"));
                }
                cursor.close();
            } catch (Exception e) {
                return num;
            } catch (Throwable th) {
                cursor.close();
            }
        }
        MLog.e("DSMMS", "ddsSub = " + ddsSub + ", curSub = " + curSub);
        return num;
    }

    public static void setRetryAlarm(Context context) {
        Cursor cursor = PduPersister.getPduPersister(context).getPendingMessages(Long.MAX_VALUE);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    long retryAt = cursor.getLong(cursor.getColumnIndexOrThrow("due_time"));
                    MLog.d("MSG_APP_RetryScheduler", "retryAt: " + retryAt);
                    if (0 != retryAt && retryAt > System.currentTimeMillis()) {
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("MSG_APP_RetryScheduler", "Next retry is scheduled at" + (retryAt - System.currentTimeMillis()) + "ms from now");
                        }
                        TransactionService.retryStart(context, retryAt);
                        return;
                    }
                } finally {
                    cursor.close();
                }
            }
            cursor.close();
        }
    }
}
