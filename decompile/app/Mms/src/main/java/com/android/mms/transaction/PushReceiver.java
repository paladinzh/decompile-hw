package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.HwCustEcidLookup;
import com.android.rcs.transaction.RcsPushReiver;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.R;
import com.google.android.mms.pdu.DeliveryInd;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadOrigInd;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class PushReceiver extends BroadcastReceiver {

    private static class ReceivePushTask extends AsyncTask<Intent, Void, Void> {
        private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
        private Context mContext;
        private HwCustPushReiver mCust = null;
        private RcsPushReiver mRcsPushReiver = null;

        public ReceivePushTask(Context context) {
            this.mContext = context;
            this.mCust = (HwCustPushReiver) HwCustUtils.createObj(HwCustPushReiver.class, new Object[]{this.mContext});
            this.mRcsPushReiver = new RcsPushReiver(this.mContext);
        }

        protected void makeToast(String report) {
            ResEx.makeToast((CharSequence) report, 1);
        }

        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];
            byte[] pushData = intent.getByteArrayExtra(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
            if (pushData == null) {
                MLog.e("Mms_TX_PushReceiver", "Null PUSH data");
                return null;
            }
            GenericPdu pdu = new PduParser(pushData, false).parse();
            if (pdu == null) {
                MLog.e("Mms_TX_PushReceiver", "Invalid PUSH data");
                return null;
            }
            PduPersister p = PduPersister.getPduPersister(this.mContext);
            ContentResolver cr = this.mContext.getContentResolver();
            int type = pdu.getMessageType();
            MLog.d("Mms_TX_PushReceiver", "message type:" + type);
            int radarSubId = 0;
            int subId;
            int networkType;
            ContentValues values;
            Uri uri;
            long threadId;
            switch (type) {
                case 130:
                    NotificationInd nInd = (NotificationInd) pdu;
                    if (MmsConfig.getTransIdEnabled()) {
                        byte[] contentLocation = nInd.getContentLocation();
                        if ((byte) 61 == contentLocation[contentLocation.length - 1]) {
                            byte[] transactionId = nInd.getTransactionId();
                            byte[] contentLocationWithId = new byte[(contentLocation.length + transactionId.length)];
                            System.arraycopy(contentLocation, 0, contentLocationWithId, 0, contentLocation.length);
                            System.arraycopy(transactionId, 0, contentLocationWithId, contentLocation.length, transactionId.length);
                            nInd.setContentLocation(contentLocationWithId);
                        }
                    }
                    if (this.mCust == null || !this.mCust.isRejectAnonymousMms(nInd)) {
                        if (!PushReceiver.isDuplicateNotification(this.mContext, nInd)) {
                            boolean isAutoDownload;
                            subId = MessageUtils.getSimIdFromIntent(intent, 0);
                            networkType = MessageUtils.getNetworkType(subId);
                            if (MessageUtils.isCTCdmaCardInGsmMode()) {
                                subId = 0;
                            }
                            values = new ContentValues(3);
                            values.put("sub_id", Integer.valueOf(subId));
                            values.put("network_type", Integer.valueOf(networkType));
                            if (MessageUtils.isMultiSimEnabled()) {
                                isAutoDownload = NotificationTransaction.allowAutoDownload(subId);
                            } else {
                                isAutoDownload = NotificationTransaction.allowAutoDownload();
                            }
                            uri = p.persist(pdu, Inbox.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(this.mContext), null);
                            if (mHwCustEcidLookup != null) {
                                mHwCustEcidLookup.addSender(this.mContext, uri);
                            }
                            threadId = PushReceiver.findThreadId(this.mContext, pdu, type, uri);
                            if (threadId > 0) {
                                MessageUtils.updateConvListInDeleteMode(this.mContext, threadId, null, uri);
                                ArrayList<String> numbers = Conversation.getAddressesByThreadId(this.mContext, threadId);
                                if (1 == numbers.size()) {
                                    ArrayList<String> orignalNum = new ArrayList();
                                    orignalNum.add(HwMessageUtils.replaceNumberFromDatabase((String) numbers.get(0), this.mContext));
                                    HwMessageUtils.updateRecentContactsToDB(this.mContext, orignalNum);
                                }
                            }
                            if (isAutoDownload) {
                                values.put("st", Integer.valueOf(129));
                            } else {
                                values.put("st", Integer.valueOf(128));
                            }
                            SqliteWrapper.update(this.mContext, cr, uri, values, null, null);
                            TransactionService.startMe(this.mContext, uri, 0);
                            StatisticalHelper.incrementReportCount(this.mContext, 2042);
                            if (this.mRcsPushReiver != null) {
                                this.mRcsPushReiver.handleRcsStatusSent(threadId, this.mContext);
                            }
                            radarSubId = subId;
                            break;
                        }
                        MLog.v("Mms_TX_PushReceiver", "Skip downloading duplicate message: " + new String(nInd.getContentLocation(), Charset.defaultCharset()));
                        break;
                    }
                    MLog.i("Mms_TX_PushReceiver", "Discard Anonymous Mms");
                    break;
                    break;
                case 134:
                case 136:
                    threadId = PushReceiver.findThreadId(this.mContext, pdu, type);
                    if (threadId != -1) {
                        uri = p.persist(pdu, Inbox.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(this.mContext), null);
                        subId = MessageUtils.getSimIdFromIntent(intent, 0);
                        networkType = MessageUtils.getNetworkType(subId);
                        if (MessageUtils.isCTCdmaCardInGsmMode()) {
                            subId = 0;
                        }
                        values = new ContentValues(3);
                        values.put("sub_id", Integer.valueOf(subId));
                        values.put("network_type", Integer.valueOf(networkType));
                        values.put("thread_id", Long.valueOf(threadId));
                        SqliteWrapper.update(this.mContext, cr, uri, values, null, null);
                        String address = null;
                        if (type == 134) {
                            String pushAddress;
                            String report;
                            DeliveryInd deliveryInd = (DeliveryInd) pdu;
                            if (deliveryInd.getTo() != null) {
                                EncodedStringValue encodedStringValue = deliveryInd.getTo()[0];
                                if (encodedStringValue != null) {
                                    address = Contact.get(encodedStringValue.getString(), false).getName();
                                }
                            }
                            int status = deliveryInd.getStatus();
                            MLog.i("Mms_TX_PushReceiver", "ReceivePushTask deliveryInd.getStatus() : " + status);
                            if (MessageUtils.isNeedLayoutRtl()) {
                                pushAddress = new StringBuffer().append('‪').append(address).append('‬').toString();
                            } else {
                                pushAddress = address;
                            }
                            if (status == 134 || status == 129) {
                                report = String.format(this.mContext.getString(R.string.delivery_toast_body), new Object[]{pushAddress});
                            } else {
                                report = String.format(this.mContext.getString(R.string.delivery_toast_body_fail), new Object[]{pushAddress});
                            }
                            final String str = report;
                            HwBackgroundLoader.getUIHandler().post(new Runnable() {
                                public void run() {
                                    if (MmsConfig.getMMSDeliveryReportsEnabled()) {
                                        ReceivePushTask.this.makeToast(str);
                                    }
                                }
                            });
                        }
                        radarSubId = subId;
                        break;
                    }
                    MLog.d("Mms_TX_PushReceiver", "Can not find associated SendReq");
                    break;
                    break;
                default:
                    try {
                        MLog.e("Mms_TX_PushReceiver", "Received unrecognized PDU.");
                        break;
                    } catch (Throwable e) {
                        ErrorMonitor.reportRadar(907000004, 0, "PushReceiver Failed to save the data from PUSH: type=" + type, e);
                        break;
                    } catch (Throwable e2) {
                        ErrorMonitor.reportRadar(907000004, 0, "PushReceiver do in backgroudn has Unexpected RuntimeException.", e2);
                        break;
                    }
            }
            MLog.v("Mms_TX_PushReceiver", "PUSH Intent processed. radarSubId:" + radarSubId);
            return null;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.w("Mms_TX_PushReceiver", "PushReceiver in SecondaryUser, exit.");
            return;
        }
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.WAP_PUSH_DELIVER") && "application/vnd.wap.mms-message".equals(intent.getType())) {
            MLog.v("Mms_TX_PushReceiver", "Received PUSH Intent: ***");
            ((PowerManager) context.getSystemService("power")).newWakeLock(1, "MMS PushReceiver").acquire(5000);
            startPushTask(context, intent);
        }
    }

    private static void startPushTask(final Context rContext, final Intent rIntent) {
        BugleActivityUtil.checkPermissionIfNeeded(rContext, new Runnable() {
            public void run() {
                new ReceivePushTask(rContext).execute(new Intent[]{rIntent});
            }
        });
    }

    private static long findThreadId(Context context, GenericPdu pdu, int type) {
        return findThreadId(context, pdu, type, null);
    }

    private static long findThreadId(Context context, GenericPdu pdu, int type, Uri uri) {
        String messageId = "";
        int mmsType = 128;
        StringBuilder sb = new StringBuilder(40);
        if (type == 134) {
            messageId = new String(((DeliveryInd) pdu).getMessageId(), Charset.defaultCharset());
        } else if (type != 130) {
            messageId = new String(((ReadOrigInd) pdu).getMessageId(), Charset.defaultCharset());
        } else if (uri == null) {
            MLog.d("Mms_TX_PushReceiver", "uri is null");
            return -1;
        } else {
            mmsType = 130;
            try {
                sb.append("_id");
                sb.append('=');
                sb.append(ContentUris.parseId(uri));
            } catch (NumberFormatException e) {
                MLog.e("Mms_TX_PushReceiver", "ContentUris parse Id NumberFormatException error >>>>" + e);
                return -1;
            } catch (UnsupportedOperationException e2) {
                MLog.e("Mms_TX_PushReceiver", "ContentUris parse Id has UnsupportedOperationException error >>>" + e2);
                return -1;
            }
        }
        if (130 != type) {
            sb.append("m_id");
            sb.append('=');
            sb.append(DatabaseUtils.sqlEscapeString(messageId));
        }
        sb.append(" AND ");
        sb.append("m_type");
        sb.append('=');
        sb.append(mmsType);
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), Mms.CONTENT_URI, new String[]{"thread_id"}, sb.toString(), null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    long j = cursor.getLong(0);
                    return j;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        MLog.d("Mms_TX_PushReceiver", "can not find Thread Id");
        return -1;
    }

    private static boolean isDuplicateNotification(Context context, NotificationInd nInd) {
        if (nInd.getContentLocation() != null) {
            String[] selectionArgs = new String[]{new String(nInd.getContentLocation(), Charset.defaultCharset())};
            Context context2 = context;
            Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), Mms.CONTENT_URI, new String[]{"_id"}, "ct_l = ?", selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        return true;
                    }
                    cursor.close();
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }
}
