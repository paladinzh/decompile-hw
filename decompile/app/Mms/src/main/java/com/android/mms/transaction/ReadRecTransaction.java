package com.android.mms.transaction;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Telephony.Mms.Sent;
import android.provider.Telephony.MmsSms.PendingMessages;
import com.android.mms.ui.MessageUtils;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadRecInd;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.io.IOException;

public class ReadRecTransaction extends Transaction {
    private final Uri mReadReportURI;

    public ReadRecTransaction(Context context, int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        this.mReadReportURI = Uri.parse(uri);
        this.mId = uri;
        this.mSubId = Transaction.querySubscription(context, this.mReadReportURI);
        attach(RetryScheduler.getInstance(context));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        Uri uri;
        Builder uriBuilder;
        Cursor cursor;
        PduPersister persister = PduPersister.getPduPersister(this.mContext);
        long msgId;
        int retryIndex;
        int retryLimitNum;
        try {
            GenericPdu readRecInd = (ReadRecInd) persister.load(this.mReadReportURI);
            readRecInd.setFrom(new EncodedStringValue(MessageUtils.getLocalNumber(this.mSubId)));
            sendPdu(new PduComposer(this.mContext, readRecInd).make());
            uri = persister.move(this.mReadReportURI, Sent.CONTENT_URI);
            this.mTransactionState.setState(1);
            this.mTransactionState.setContentUri(uri);
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mReadReportURI);
                msgId = ContentUris.parseId(this.mReadReportURI);
                uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
                uriBuilder.appendQueryParameter("protocol", "mms");
                uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
                cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uriBuilder.build(), null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                            retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                            retryLimitNum = new DefaultRetryScheme(this.mContext, retryIndex).getRetryLimit();
                            MLog.d("Mms_TXM_RRT", "have tried to send msgId=" + msgId + " retryIndex=" + retryIndex + " retryLimitNum=" + retryLimitNum);
                            if (retryIndex >= retryLimitNum) {
                                MLog.d("Mms_TXM_RRT", "set transaction state to successful!");
                                uri = persister.move(this.mReadReportURI, Sent.CONTENT_URI);
                                this.mTransactionState.setState(1);
                                this.mTransactionState.setContentUri(uri);
                            }
                        }
                        cursor.close();
                    } catch (Exception e) {
                        MLog.e("Mms_TXM_RRT", "ReadRecTransaction has exception when update state.", (Throwable) e);
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
            notifyObservers();
        } catch (IOException e2) {
            MLog.e("Mms_TXM_RRT", "Failed to send M-Read-Rec.Ind.", (Throwable) e2);
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mReadReportURI);
                msgId = ContentUris.parseId(this.mReadReportURI);
                uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
                uriBuilder.appendQueryParameter("protocol", "mms");
                uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
                cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uriBuilder.build(), null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                            retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                            retryLimitNum = new DefaultRetryScheme(this.mContext, retryIndex).getRetryLimit();
                            MLog.d("Mms_TXM_RRT", "have tried to send msgId=" + msgId + " retryIndex=" + retryIndex + " retryLimitNum=" + retryLimitNum);
                            if (retryIndex >= retryLimitNum) {
                                MLog.d("Mms_TXM_RRT", "set transaction state to successful!");
                                uri = persister.move(this.mReadReportURI, Sent.CONTENT_URI);
                                this.mTransactionState.setState(1);
                                this.mTransactionState.setContentUri(uri);
                            }
                        }
                        cursor.close();
                    } catch (Exception e3) {
                        MLog.e("Mms_TXM_RRT", "ReadRecTransaction has exception when update state.", (Throwable) e3);
                    } catch (Throwable th2) {
                        cursor.close();
                    }
                }
            }
            notifyObservers();
        } catch (MmsException e4) {
            MLog.e("Mms_TXM_RRT", "Failed to load message from Outbox.", (Throwable) e4);
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mReadReportURI);
                msgId = ContentUris.parseId(this.mReadReportURI);
                uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
                uriBuilder.appendQueryParameter("protocol", "mms");
                uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
                cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uriBuilder.build(), null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                            retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                            retryLimitNum = new DefaultRetryScheme(this.mContext, retryIndex).getRetryLimit();
                            MLog.d("Mms_TXM_RRT", "have tried to send msgId=" + msgId + " retryIndex=" + retryIndex + " retryLimitNum=" + retryLimitNum);
                            if (retryIndex >= retryLimitNum) {
                                MLog.d("Mms_TXM_RRT", "set transaction state to successful!");
                                uri = persister.move(this.mReadReportURI, Sent.CONTENT_URI);
                                this.mTransactionState.setState(1);
                                this.mTransactionState.setContentUri(uri);
                            }
                        }
                        cursor.close();
                    } catch (Exception e32) {
                        MLog.e("Mms_TXM_RRT", "ReadRecTransaction has exception when update state.", (Throwable) e32);
                    } catch (Throwable th3) {
                        cursor.close();
                    }
                }
            }
            notifyObservers();
        } catch (RuntimeException e5) {
            MLog.e("Mms_TXM_RRT", "Unexpected RuntimeException.", (Throwable) e5);
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mReadReportURI);
                msgId = ContentUris.parseId(this.mReadReportURI);
                uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
                uriBuilder.appendQueryParameter("protocol", "mms");
                uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
                cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uriBuilder.build(), null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                            retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                            retryLimitNum = new DefaultRetryScheme(this.mContext, retryIndex).getRetryLimit();
                            MLog.d("Mms_TXM_RRT", "have tried to send msgId=" + msgId + " retryIndex=" + retryIndex + " retryLimitNum=" + retryLimitNum);
                            if (retryIndex >= retryLimitNum) {
                                MLog.d("Mms_TXM_RRT", "set transaction state to successful!");
                                uri = persister.move(this.mReadReportURI, Sent.CONTENT_URI);
                                this.mTransactionState.setState(1);
                                this.mTransactionState.setContentUri(uri);
                            }
                        }
                        cursor.close();
                    } catch (Exception e322) {
                        MLog.e("Mms_TXM_RRT", "ReadRecTransaction has exception when update state.", (Throwable) e322);
                    } catch (Throwable th4) {
                        cursor.close();
                    }
                }
            }
            notifyObservers();
        } catch (Throwable th5) {
            Throwable th6 = th5;
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mReadReportURI);
                msgId = ContentUris.parseId(this.mReadReportURI);
                uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
                uriBuilder.appendQueryParameter("protocol", "mms");
                uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
                cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uriBuilder.build(), null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                            retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                            retryLimitNum = new DefaultRetryScheme(this.mContext, retryIndex).getRetryLimit();
                            MLog.d("Mms_TXM_RRT", "have tried to send msgId=" + msgId + " retryIndex=" + retryIndex + " retryLimitNum=" + retryLimitNum);
                            if (retryIndex >= retryLimitNum) {
                                MLog.d("Mms_TXM_RRT", "set transaction state to successful!");
                                uri = persister.move(this.mReadReportURI, Sent.CONTENT_URI);
                                this.mTransactionState.setState(1);
                                this.mTransactionState.setContentUri(uri);
                            }
                        }
                        cursor.close();
                    } catch (Exception e3222) {
                        MLog.e("Mms_TXM_RRT", "ReadRecTransaction has exception when update state.", (Throwable) e3222);
                    } catch (Throwable th7) {
                        cursor.close();
                    }
                }
            }
            notifyObservers();
        }
    }

    public int getType() {
        return 3;
    }
}
