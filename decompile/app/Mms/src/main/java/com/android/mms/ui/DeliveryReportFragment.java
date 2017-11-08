package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.System;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsDeliveryReportFragment;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.HwListFragment;
import com.huawei.mms.util.HwNumberMatchUtils;
import com.huawei.mms.util.PrivacyModeReceiver;
import com.huawei.mms.util.PrivacyModeReceiver.ModeChangeListener;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DeliveryReportFragment extends HwListFragment {
    static final String[] MMS_REPORT_REQUEST_PROJECTION = new String[]{"address", "d_rpt", "rr"};
    static final String[] MMS_REPORT_STATUS_PROJECTION = new String[]{"address", "delivery_status", "read_status"};
    static final String[] SMS_REPORT_STATUS_PROJECTION = new String[]{"address", "status", "date_sent", NumberInfo.TYPE_KEY, "date"};
    private boolean isMultiRecipients = false;
    ModeChangeListener localPrivacyMonitor = new ModeChangeListener() {
        public void onModeChange(Context context, boolean isInPrivacy) {
            if (!isInPrivacy) {
                boolean isPrivacy;
                if ("sms".equals(DeliveryReportFragment.this.mMessageType)) {
                    isPrivacy = PrivacyModeReceiver.isPrivacySms(context, DeliveryReportFragment.this.mMessageId);
                } else {
                    isPrivacy = PrivacyModeReceiver.isPrivacyPdu(context, DeliveryReportFragment.this.mMessageId);
                }
                if (isPrivacy) {
                    DeliveryReportFragment.this.finishSelf(false);
                }
            }
        }
    };
    private RcsDeliveryReportFragment mCust = null;
    private long mMessageId;
    private String mMessageType;
    private long mUID;

    public static final class MmsReportRequest {
        private final boolean mIsDeliveryReportRequsted;
        private final boolean mIsReadReportRequested;
        private final String mRecipient;

        public MmsReportRequest(String recipient, int drValue, int rrValue) {
            boolean z;
            boolean z2 = true;
            this.mRecipient = recipient;
            if (drValue == 128) {
                z = true;
            } else {
                z = false;
            }
            this.mIsDeliveryReportRequsted = z;
            if (rrValue != 128) {
                z2 = false;
            }
            this.mIsReadReportRequested = z2;
        }

        public String getRecipient() {
            return this.mRecipient;
        }

        public boolean isReadReportRequested() {
            return this.mIsReadReportRequested;
        }
    }

    public static final class MmsReportStatus {
        public final int deliveryStatus;
        public final int readStatus;

        public MmsReportStatus(int drStatus, int rrStatus) {
            this.deliveryStatus = drStatus;
            this.readStatus = rrStatus;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.delivery_report_activity, container, false);
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mCust == null) {
            this.mCust = new RcsDeliveryReportFragment(getContext());
        }
        Intent intent = getIntent();
        this.mMessageId = getMessageId(icicle, intent);
        this.mMessageType = getMessageType(icicle, intent);
        this.mUID = getMessageUid(icicle, intent);
        this.isMultiRecipients = getMessageMultiRecipients(icicle, intent);
        initListAdapter();
        PrivacyStateListener.self().register(this.localPrivacyMonitor);
    }

    public void onDestroy() {
        PrivacyStateListener.self().unRegister(this.localPrivacyMonitor);
        super.onDestroy();
    }

    private void initListAdapter() {
        List<DeliveryReportItem> items = getReportItems();
        if (items == null) {
            items = new ArrayList(1);
            items.add(new DeliveryReportItem("", getString(R.string.status_none), null));
            MLog.w("DeliveryReportFragment", "cursor == null");
        }
        setListAdapter(new DeliveryReportAdapter(getContext(), items));
    }

    public void onResume() {
        super.onResume();
        refreshDeliveryReport();
    }

    private void refreshDeliveryReport() {
        ListView list = getListView();
        list.invalidateViews();
        list.requestFocus();
    }

    private long getMessageId(Bundle icicle, Intent intent) {
        long msgId = 0;
        if (icicle != null) {
            msgId = icicle.getLong("message_id");
        }
        if (msgId == 0) {
            return intent.getLongExtra("message_id", 0);
        }
        return msgId;
    }

    private String getMessageType(Bundle icicle, Intent intent) {
        String msgType = null;
        if (icicle != null) {
            msgType = icicle.getString("message_type");
        }
        if (msgType == null) {
            return intent.getStringExtra("message_type");
        }
        return msgType;
    }

    private long getMessageUid(Bundle aIcicle, Intent aIntent) {
        long lUid = 0;
        if (aIcicle != null) {
            lUid = aIcicle.getLong("group_id");
        }
        if (lUid == 0) {
            return aIntent.getLongExtra("group_id", 0);
        }
        return lUid;
    }

    private boolean getMessageMultiRecipients(Bundle aIcicle, Intent aIntent) {
        boolean isMultiRecipients = false;
        if (aIcicle != null) {
            isMultiRecipients = aIcicle.getBoolean("is_multi_recipients");
        }
        if (isMultiRecipients) {
            return isMultiRecipients;
        }
        return aIntent.getBooleanExtra("is_multi_recipients", false);
    }

    private List<DeliveryReportItem> getReportItems() {
        if (this.mMessageType != null && this.mMessageType.equals("sms")) {
            return getSmsReportItems();
        }
        if (this.mCust == null || !this.mCust.isRcsMsgType(this.mMessageType)) {
            return getMmsReportItems();
        }
        return this.mCust.getRcsReportItems(this.mMessageId);
    }

    private List<DeliveryReportItem> getSmsReportItems() {
        String selection;
        if (this.isMultiRecipients) {
            selection = "group_id = '" + String.valueOf(this.mUID) + "'";
        } else {
            selection = "_id = " + this.mMessageId;
        }
        Cursor c = SqliteWrapper.query(getContext(), getContext().getContentResolver(), Sms.CONTENT_URI, SMS_REPORT_STATUS_PROJECTION, selection, null, null);
        if (c == null) {
            return null;
        }
        try {
            if (c.getCount() <= 0) {
                return null;
            }
            List<DeliveryReportItem> items = new ArrayList();
            while (c.moveToNext()) {
                String deliveryDateString = null;
                long deliveryDate = c.getLong(2);
                if (c.getInt(3) == 2 && deliveryDate > 0) {
                    if (deliveryDate == 1) {
                        deliveryDate = c.getLong(4);
                    }
                    deliveryDateString = getString(R.string.delivered_label_report, new Object[]{MessageUtils.formatTimeStampString(getContext(), deliveryDate, true, true)});
                }
                items.add(new DeliveryReportItem(new StringBuffer().append(getString(R.string.recipient_label)).append('‪').append(c.getString(0)).append('‬').toString(), getString(R.string.status_label_report, new Object[]{getSmsStatusText(c.getInt(1), messageType)}), deliveryDateString));
            }
            c.close();
            return items;
        } finally {
            c.close();
        }
    }

    private String getMmsReportStatusText(MmsReportRequest request, Map<String, MmsReportStatus> reportStatus) {
        if (reportStatus == null) {
            return getString(R.string.mms_report_requested);
        }
        String recipient = request.getRecipient();
        MmsReportStatus status = queryStatusByRecipient(reportStatus, Contact.isEmailAddress(recipient) ? Mms.extractAddrSpec(recipient) : PhoneNumberUtils.stripSeparators(recipient));
        if (status == null) {
            return getString(R.string.mms_report_requested);
        }
        if (request.isReadReportRequested() && status.readStatus != 0) {
            switch (status.readStatus) {
                case 128:
                    return getString(R.string.status_read);
                case 129:
                    return getString(R.string.status_unread);
            }
        }
        switch (status.deliveryStatus) {
            case 0:
                return getString(R.string.mms_report_requested);
            case 128:
                if (1 == System.getInt(getContext().getContentResolver(), "show_mms_expired_status", 0)) {
                    return getString(R.string.status_expired);
                }
                return getString(R.string.status_failed_Toast);
            case 129:
            case 134:
                return getString(R.string.status_received);
            case 130:
                return getString(R.string.status_rejected);
            default:
                return getString(R.string.status_failed_Toast);
        }
    }

    public static MmsReportStatus queryStatusByRecipient(Map<String, MmsReportStatus> status, String recipient) {
        boolean bMmsDRInRussia = MmsConfig.getMmsDRInRussiaEnabled();
        boolean isEmail = Contact.isEmailAddress(recipient);
        for (Entry<String, MmsReportStatus> e : status.entrySet()) {
            if (isEmail) {
                if (TextUtils.equals((CharSequence) e.getKey(), recipient)) {
                    return (MmsReportStatus) e.getValue();
                }
            } else if (bMmsDRInRussia) {
                if (compare((String) e.getKey(), recipient)) {
                    return (MmsReportStatus) e.getValue();
                }
            } else if (PhoneNumberUtils.compare((String) e.getKey(), recipient)) {
                return (MmsReportStatus) e.getValue();
            }
        }
        return null;
    }

    private static boolean compare(String a, String b) {
        return HwNumberMatchUtils.isNumbersMatched(a, b);
    }

    private List<DeliveryReportItem> getMmsReportItems() {
        List<MmsReportRequest> reportReqs = getMmsReportRequests();
        if (reportReqs == null || reportReqs.size() == 0) {
            return null;
        }
        Map<String, MmsReportStatus> reportStatus = getMmsReportStatus();
        List<DeliveryReportItem> items = new ArrayList();
        for (MmsReportRequest reportReq : reportReqs) {
            items.add(new DeliveryReportItem(new StringBuffer().append(getString(R.string.recipient_label)).append('‪').append(reportReq.getRecipient()).append('‬').toString(), getString(R.string.status_label_report, new Object[]{getMmsReportStatusText(reportReq, reportStatus)}), null));
        }
        return items;
    }

    private Map<String, MmsReportStatus> getMmsReportStatus() {
        Cursor c = SqliteWrapper.query(getContext(), getContext().getContentResolver(), Uri.withAppendedPath(Mms.REPORT_STATUS_URI, String.valueOf(this.mMessageId)), MMS_REPORT_STATUS_PROJECTION, null, null, null);
        if (c == null) {
            return null;
        }
        try {
            Map<String, MmsReportStatus> statusMap = new HashMap();
            while (c.moveToNext()) {
                String recipient = c.getString(0);
                if (Contact.isEmailAddress(recipient)) {
                    recipient = Mms.extractAddrSpec(recipient);
                } else {
                    recipient = PhoneNumberUtils.stripSeparators(recipient);
                }
                statusMap.put(recipient, new MmsReportStatus(c.getInt(1), c.getInt(2)));
            }
            return statusMap;
        } finally {
            c.close();
        }
    }

    private List<MmsReportRequest> getMmsReportRequests() {
        Cursor c = SqliteWrapper.query(getContext(), getContext().getContentResolver(), Uri.withAppendedPath(Mms.REPORT_REQUEST_URI, String.valueOf(this.mMessageId)), MMS_REPORT_REQUEST_PROJECTION, null, null, null);
        if (c == null) {
            return null;
        }
        try {
            if (c.getCount() <= 0) {
                return null;
            }
            List<MmsReportRequest> reqList = new ArrayList();
            while (c.moveToNext()) {
                reqList.add(new MmsReportRequest(c.getString(0), c.getInt(1), c.getInt(2)));
            }
            c.close();
            return reqList;
        } finally {
            c.close();
        }
    }

    private String getSmsStatusText(int status, int type) {
        if (status == -1) {
            return getString(R.string.status_none);
        }
        if (status >= 64) {
            return getString(R.string.status_failed_Toast);
        }
        if (status < 32) {
            return getString(R.string.status_received);
        }
        if (type == 5) {
            return getString(R.string.status_failed_Toast);
        }
        return getString(R.string.status_pending);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return true;
    }
}
