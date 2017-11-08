package com.android.mms.transaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.Threads;
import android.telephony.SubscriptionManager;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.Recycler;
import com.android.mms.widget.MmsWidgetProvider;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.NotifyRespInd;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.io.IOException;
import java.nio.charset.Charset;

public class NotificationTransaction extends Transaction {
    private String mContentLocation;
    HwCustNotificationTransaction mHwCustNotificationTransaction;
    private NotificationInd mNotificationInd;
    private Uri mUri;

    public NotificationTransaction(Context context, int serviceId, TransactionSettings connectionSettings, String uriString) {
        super(context, serviceId, connectionSettings);
        this.mHwCustNotificationTransaction = (HwCustNotificationTransaction) HwCustUtils.createObj(HwCustNotificationTransaction.class, new Object[0]);
        this.mUri = Uri.parse(uriString);
        try {
            this.mNotificationInd = (NotificationInd) PduPersister.getPduPersister(context).load(this.mUri);
            this.mContentLocation = new String(this.mNotificationInd.getContentLocation(), Charset.defaultCharset());
            this.mId = this.mContentLocation;
            MLog.d("Mms_TXM_NT", "mId:" + this.mId);
            this.mSubId = Transaction.querySubscription(context, this.mUri);
            attach(RetryScheduler.getInstance(context));
            updateSubIdNetworkType(context, this.mUri);
        } catch (MmsException e) {
            MLog.e("Mms_TXM_NT", "Failed to load NotificationInd from:uriString ", (Throwable) e);
            throw new IllegalArgumentException("handled");
        }
    }

    public NotificationTransaction(Context context, int serviceId, TransactionSettings connectionSettings, NotificationInd ind) {
        boolean z = false;
        super(context, serviceId, connectionSettings);
        this.mHwCustNotificationTransaction = (HwCustNotificationTransaction) HwCustUtils.createObj(HwCustNotificationTransaction.class, new Object[0]);
        try {
            PduPersister pduPersister = PduPersister.getPduPersister(context);
            Uri uri = Inbox.CONTENT_URI;
            if (!allowAutoDownload()) {
                z = true;
            }
            this.mUri = pduPersister.persist(ind, uri, z, PreferenceUtils.getIsGroupMmsEnabled(context), null);
            this.mNotificationInd = ind;
            this.mContentLocation = new String(this.mNotificationInd.getContentLocation(), Charset.defaultCharset());
            this.mId = this.mContentLocation;
            MLog.d("Mms_TXM_NT", "mId:" + this.mId);
            updateSubIdNetworkType(context, this.mUri);
        } catch (MmsException e) {
            MLog.e("Mms_TXM_NT", "Failed to save NotificationInd in constructor.", (Throwable) e);
            throw new IllegalArgumentException("exception-handled");
        }
    }

    private void updateSubIdNetworkType(Context context, Uri uri) {
        this.mSubId = -1;
        Cursor cursor = SqliteWrapper.query(context, uri, new String[]{"sub_id", "network_type"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    this.mSubId = cursor.getInt(0);
                    if (!(this.mSubId == 0 || this.mSubId == 1)) {
                        this.mSubId = 0;
                    }
                    this.mNetworkType = cursor.getInt(1);
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
    }

    public static boolean allowAutoDownload() {
        boolean autoDownload = DownloadManager.getInstance().isAuto();
        boolean dataSuspended = MmsApp.getDefaultTelephonyManager().getDataState() == 3;
        if (!autoDownload || dataSuspended) {
            return false;
        }
        return true;
    }

    public static boolean allowAutoDownload(int subid) {
        boolean autoDownload = DownloadManager.getInstance().isAuto(subid);
        boolean dataSuspended = false;
        int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!SubscriptionManager.isValidSubscriptionId(dataSubId)) {
            dataSuspended = MmsApp.getDefaultTelephonyManager().getDataState() == 3;
        } else if (dataSubId == subid) {
            dataSuspended = MmsApp.getDefaultTelephonyManager().getDataState() == 3;
        }
        MLog.i("Mms_TXM_NT", "dataSubId:" + dataSubId + ",subid:" + subid + ",dataSuspended:" + dataSuspended + ",autoDownload:" + autoDownload);
        if (!autoDownload || dataSuspended) {
            return false;
        }
        return true;
    }

    public void run() {
        boolean autoDownload;
        Cursor c4sentDate;
        DownloadManager downloadManager = DownloadManager.getInstance();
        if (MessageUtils.isMultiSimEnabled()) {
            autoDownload = allowAutoDownload(this.mSubId);
        } else {
            autoDownload = allowAutoDownload();
        }
        Cursor c;
        try {
            MLog.v("Mms_TXM_NT", "Notification transaction launched: " + this);
            int status = 131;
            if (autoDownload) {
                downloadManager.markState(this.mUri, 129);
                if (this.mUri != null) {
                    MessageListAdapter.saveConnectionManagerToMap(this.mUri.toString(), false, true, false, null);
                }
                MLog.v("Mms_TXM_NT", "Content-Location: " + this.mContentLocation);
                byte[] retrieveConfData = null;
                if (this.mUri != null) {
                    retrieveConfData = getPdu(this.mContentLocation, this.mUri.toString());
                }
                if (retrieveConfData != null) {
                    GenericPdu pdu = new PduParser(retrieveConfData, false).parse();
                    if (pdu == null || pdu.getMessageType() != 132) {
                        MLog.e("Mms_TXM_NT", "Invalid M-RETRIEVE.CONF PDU. " + (pdu != null ? "message type: " + pdu.getMessageType() : "null pdu"));
                        this.mTransactionState.setState(2);
                        status = 132;
                    } else {
                        Uri uri = PduPersister.getPduPersister(this.mContext).persist(pdu, Inbox.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(this.mContext), null);
                        ContentValues contentValues;
                        if (SystemProperties.getBoolean("ro.config.show_mms_storage", false)) {
                            contentValues = new ContentValues(5);
                            contentValues.put("m_size", Integer.valueOf(retrieveConfData.length));
                        } else {
                            contentValues = new ContentValues(4);
                        }
                        c4sentDate = SqliteWrapper.query(this.mContext, uri, null, null, null, null);
                        long sentDate = 0;
                        if (c4sentDate != null) {
                            if (c4sentDate.moveToNext()) {
                                sentDate = c4sentDate.getLong(c4sentDate.getColumnIndex("date"));
                                values.put("date_sent", Long.valueOf(sentDate));
                            }
                            c4sentDate.close();
                        }
                        if (this.mHwCustNotificationTransaction == null || this.mHwCustNotificationTransaction.isLocalReceivedDate(System.currentTimeMillis(), sentDate)) {
                            values.put("date", Long.valueOf(System.currentTimeMillis() / 1000));
                        }
                        c = SqliteWrapper.query(this.mContext, this.mUri, null, null, null, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                values.put("sub_id", Integer.valueOf(c.getInt(c.getColumnIndex("sub_id"))));
                            }
                            c.close();
                        }
                        values.put("network_type", Integer.valueOf(this.mNetworkType));
                        SqliteWrapper.update(this.mContext, uri, values, null, null);
                        MessageListAdapter.removeConnectionManagerFromMap(this.mUri.toString());
                        SqliteWrapper.delete(this.mContext, this.mUri, null, null);
                        SqliteWrapper.delete(this.mContext, Threads.OBSOLETE_THREADS_URI, null, null);
                        this.mUri = uri;
                        status = 129;
                    }
                }
                MLog.v("Mms_TXM_NT", "status=0x" + Integer.toHexString(status));
                switch (status) {
                    case 129:
                        StatisticalHelper.incrementReportCount(this.mContext, 2043);
                        this.mTransactionState.setState(1);
                        break;
                    case 131:
                        if (this.mTransactionState.getState() == 0) {
                            this.mTransactionState.setState(1);
                            break;
                        }
                        break;
                    default:
                        MLog.d("Mms_TXM_NT", "NotificationTrasation ERROR branch in run");
                        break;
                }
                sendNotifyRespInd(status);
                Recycler.getMmsRecycler().deleteOldMessagesInSameThreadAsMessage(this.mContext, this.mUri);
                MmsWidgetProvider.notifyDatasetChanged(this.mContext);
                this.mTransactionState.setContentUri(this.mUri);
                if (!autoDownload) {
                    this.mTransactionState.setState(1);
                } else if (this.mTransactionState.getState() == 1) {
                    StatisticalHelper.incrementReportCount(this.mContext, 2004);
                }
                if (this.mTransactionState.getState() != 1) {
                    this.mTransactionState.setState(2);
                }
                notifyObservers();
                return;
            }
            downloadManager.markState(this.mUri, 128);
            sendNotifyRespInd(131);
            this.mTransactionState.setContentUri(this.mUri);
            if (!autoDownload) {
                this.mTransactionState.setState(1);
            } else if (this.mTransactionState.getState() == 1) {
                StatisticalHelper.incrementReportCount(this.mContext, 2004);
            }
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
            }
            notifyObservers();
        } catch (IOException e) {
            this.mTransactionState.setState(2);
        } catch (Throwable t) {
            try {
                MLog.e("Mms_TXM_NT", MLog.getStackTraceString(t));
            } finally {
                this.mTransactionState.setContentUri(this.mUri);
                if (!autoDownload) {
                    this.mTransactionState.setState(1);
                } else if (this.mTransactionState.getState() == 1) {
                    StatisticalHelper.incrementReportCount(this.mContext, 2004);
                }
                if (this.mTransactionState.getState() != 1) {
                    this.mTransactionState.setState(2);
                }
                notifyObservers();
            }
        }
    }

    private void sendNotifyRespInd(int status) throws MmsException, IOException {
        NotifyRespInd notifyRespInd = new NotifyRespInd(18, this.mNotificationInd.getTransactionId(), status);
        if (MmsConfig.getMMSSendDeliveryReportsEnabled()) {
            boolean reportAllowed = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean("pref_key_mms_enable_to_send_delivery_reports", MmsConfig.getDefaultMMSSendDeliveryReports());
            MLog.v("MSG_APP_Mms", "sendNotifyRespInd reportAllowed" + reportAllowed);
            try {
                notifyRespInd.setReportAllowed(reportAllowed ? 128 : 129);
            } catch (InvalidHeaderValueException e) {
                MLog.v("MSG_APP_Mms", "notifyRespInd.setReportAllowed Failed !!");
            }
        }
        if (MmsConfig.getNotifyWapMMSC()) {
            sendPdu(new PduComposer(this.mContext, notifyRespInd).make(), this.mContentLocation);
        } else {
            sendPdu(new PduComposer(this.mContext, notifyRespInd).make());
        }
    }

    public int getType() {
        return 0;
    }

    public Uri getUri() {
        return this.mUri;
    }
}
