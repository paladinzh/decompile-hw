package com.android.mms.transaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.Recycler;
import com.android.mms.widget.MmsWidgetProvider;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.AcknowledgeInd;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.RetrieveConf;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.io.IOException;
import java.nio.charset.Charset;

public class RetrieveTransaction extends Transaction {
    static final String[] PROJECTION = new String[]{"ct_l", "locked", "sub_id"};
    private final String mContentLocation;
    private int mDownloadButtonClickCount;
    HwCustRetrieveTransaction mHwCustRetrieveTransaction = ((HwCustRetrieveTransaction) HwCustUtils.createObj(HwCustRetrieveTransaction.class, new Object[0]));
    private boolean mLocked;
    private final Uri mUri;

    public RetrieveTransaction(Context context, int serviceId, TransactionSettings connectionSettings, String uri) throws MmsException {
        super(context, serviceId, connectionSettings);
        if (uri.startsWith("content://")) {
            this.mUri = Uri.parse(uri);
            String contentLocation = getContentLocation(context, this.mUri);
            this.mContentLocation = contentLocation;
            this.mId = contentLocation;
            MLog.v("Mms_TXM_RT", "X-Mms-Content-Location: " + this.mContentLocation);
            attach(RetryScheduler.getInstance(context));
            return;
        }
        throw new IllegalArgumentException("Initializing from X-Mms-Content-Location is abandoned!");
    }

    private String getContentLocation(Context context, Uri uri) throws MmsException {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), uri, PROJECTION, null, null, null);
        this.mLocked = false;
        if (cursor != null) {
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    this.mLocked = cursor.getInt(1) == 1;
                    this.mSubId = cursor.getInt(2);
                    if (!(this.mSubId == 0 || this.mSubId == 1)) {
                        this.mSubId = 0;
                    }
                    this.mNetworkType = MessageUtils.getNetworkType(this.mSubId);
                    String string = cursor.getString(0);
                    return string;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        throw new MmsException("Cannot get X-Mms-Content-Location from: " + uri);
    }

    public void run() {
        Cursor cursor;
        try {
            DownloadManager.getInstance().markState(this.mUri, 129);
            byte[] resp = getPdu(this.mContentLocation, this.mUri.toString());
            GenericPdu genericPdu = null;
            if (resp != null) {
                genericPdu = (RetrieveConf) new PduParser(resp, false).parse();
            }
            if (genericPdu == null) {
                throw new MmsException("Invalid M-Retrieve.conf PDU.");
            }
            Uri msgUri = null;
            if (isDuplicateMessage(this.mContext, genericPdu)) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mUri);
            } else {
                msgUri = PduPersister.getPduPersister(this.mContext).persist(genericPdu, Inbox.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(this.mContext), null);
                ContentValues contentValues;
                if (SystemProperties.getBoolean("ro.config.show_mms_storage", false)) {
                    contentValues = new ContentValues(5);
                    contentValues.put("m_size", Integer.valueOf(resp.length));
                } else {
                    contentValues = new ContentValues(4);
                }
                cursor = null;
                long sentDate = 0;
                cursor = SqliteWrapper.query(this.mContext, msgUri, null, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    sentDate = cursor.getLong(cursor.getColumnIndex("date"));
                    values.put("date_sent", Long.valueOf(sentDate));
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (this.mHwCustRetrieveTransaction == null || this.mHwCustRetrieveTransaction.isLocalReceivedDate(System.currentTimeMillis(), sentDate)) {
                    values.put("date", Long.valueOf(System.currentTimeMillis() / 1000));
                }
                values.put("sub_id", Integer.valueOf(this.mSubId));
                values.put("network_type", Integer.valueOf(this.mNetworkType));
                SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), msgUri, values, null, null);
                this.mTransactionState.setState(1);
                this.mTransactionState.setContentUri(msgUri);
                updateContentLocation(this.mContext, msgUri, this.mContentLocation, this.mLocked);
            }
            MessageListAdapter.removeConnectionManagerFromMap(this.mUri.toString());
            SqliteWrapper.delete(this.mContext, this.mUri, null, null);
            if (msgUri != null) {
                Recycler.getMmsRecycler().deleteOldMessagesInSameThreadAsMessage(this.mContext, msgUri);
                MmsWidgetProvider.notifyDatasetChanged(this.mContext);
            }
            sendAcknowledgeInd(genericPdu);
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mUri);
            } else {
                StatisticalHelper.incrementReportCount(this.mContext, 2004);
                StatisticalHelper.incrementReportCount(this.mContext, 2044);
            }
            notifyObservers();
        } catch (Throwable th) {
            if (this.mTransactionState.getState() != 1) {
                this.mTransactionState.setState(2);
                this.mTransactionState.setContentUri(this.mUri);
            } else {
                StatisticalHelper.incrementReportCount(this.mContext, 2004);
                StatisticalHelper.incrementReportCount(this.mContext, 2044);
            }
            notifyObservers();
        }
    }

    private static boolean isDuplicateMessage(Context context, RetrieveConf rc) {
        if (rc.getMessageId() != null) {
            String selection = "(m_id = ? AND m_type = ?)";
            String[] selectionArgs = new String[]{new String(rc.getMessageId(), Charset.defaultCharset()), String.valueOf(132)};
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, Mms.CONTENT_URI, new String[]{"_id", "sub", "sub_cs"}, selection, selectionArgs, null);
                if (cursor != null && cursor.getCount() > 0) {
                    boolean isDuplicateMessageExtra = isDuplicateMessageExtra(cursor, rc);
                    return isDuplicateMessageExtra;
                } else if (cursor != null) {
                    cursor.close();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return false;
    }

    private static boolean isDuplicateMessageExtra(Cursor cursor, RetrieveConf rc) {
        EncodedStringValue encodedSubjectStored = null;
        CharSequence subjectReceived = null;
        EncodedStringValue encodedSubjectReceived = rc.getSubject();
        if (encodedSubjectReceived != null) {
            subjectReceived = encodedSubjectReceived.getString();
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int subjectIdx = cursor.getColumnIndex("sub");
            int charsetIdx = cursor.getColumnIndex("sub_cs");
            String subject = cursor.getString(subjectIdx);
            int charset = cursor.getInt(charsetIdx);
            if (subject != null) {
                encodedSubjectStored = new EncodedStringValue(charset, PduPersister.getBytes(subject));
            }
            if (encodedSubjectStored == null && encodedSubjectReceived == null) {
                return true;
            }
            if (!(encodedSubjectStored == null || encodedSubjectReceived == null)) {
                String subjectStored = encodedSubjectStored.getString();
                if (!TextUtils.isEmpty(subjectStored) && !TextUtils.isEmpty(subjectReceived)) {
                    return subjectStored.equals(subjectReceived);
                }
                if (TextUtils.isEmpty(subjectStored) && TextUtils.isEmpty(subjectReceived)) {
                    return true;
                }
            }
            cursor.moveToNext();
        }
        return false;
    }

    private void sendAcknowledgeInd(RetrieveConf rc) throws MmsException, IOException {
        byte[] tranId = rc.getTransactionId();
        if (tranId != null) {
            AcknowledgeInd acknowledgeInd = new AcknowledgeInd(18, tranId);
            String lineNumber = MessageUtils.getLocalNumber();
            if (!TextUtils.isEmpty(lineNumber)) {
                acknowledgeInd.setFrom(new EncodedStringValue(lineNumber));
            }
            if (MmsConfig.getMMSSendDeliveryReportsEnabled()) {
                boolean reportAllowed = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean("pref_key_mms_enable_to_send_delivery_reports", MmsConfig.getDefaultMMSSendDeliveryReports());
                MLog.v("MSG_APP_Mms", "sendAcknowledgeInd reportAllowed" + reportAllowed);
                try {
                    acknowledgeInd.setReportAllowed(reportAllowed ? 128 : 129);
                } catch (InvalidHeaderValueException e) {
                    MLog.v("MSG_APP_Mms", "acknowledgeInd.setReportAllowed Failed !!");
                }
            }
            if (this.mHwCustRetrieveTransaction != null) {
                this.mHwCustRetrieveTransaction.sendAcknowledgeInd(acknowledgeInd);
            }
            if (MmsConfig.getNotifyWapMMSC()) {
                sendPdu(new PduComposer(this.mContext, acknowledgeInd).make(), this.mContentLocation);
            } else {
                sendPdu(new PduComposer(this.mContext, acknowledgeInd).make());
            }
        }
    }

    private static void updateContentLocation(Context context, Uri uri, String contentLocation, boolean locked) {
        ContentValues values = new ContentValues(2);
        values.put("ct_l", contentLocation);
        values.put("locked", Boolean.valueOf(locked));
        SqliteWrapper.update(context, context.getContentResolver(), uri, values, null, null);
    }

    public int getType() {
        return 1;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public int getDownloadButtonClickCount() {
        return this.mDownloadButtonClickCount;
    }

    public void setDownloadButtonClickCount(int mDownloadButtonClickCount) {
        this.mDownloadButtonClickCount = mDownloadButtonClickCount;
    }
}
