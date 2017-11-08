package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.data.Contact;
import com.android.mms.ui.DeliveryReportFragment.MmsReportRequest;
import com.android.mms.ui.DeliveryReportFragment.MmsReportStatus;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwCustMessageListItemImpl extends HwCustMessageListItem {
    private static final int ENCRYPT_NOTIFY_REFRESH = 1;
    private static final String TAG = "HwCustMessageListItemImpl";
    private String mSearchString = "";

    public HwCustMessageListItemImpl(Context context) {
        super(context);
    }

    public void showMmsReportMoreStatus(Context context, MessageItem messageItem, TextView messageStatus) {
        if (HwCustMmsConfigImpl.getEnableMmsReportMoreStatus()) {
            String statusText = checkMmsReportItems(context, messageItem);
            if (!TextUtils.isEmpty(statusText)) {
                messageStatus.setText(statusText);
            }
        }
    }

    public static String checkMmsReportItemStatus(Context context, MmsReportRequest request, Map<String, MmsReportStatus> reportStatus) {
        if (reportStatus == null) {
            return null;
        }
        String recipient = request.getRecipient();
        if (Contact.isEmailAddress(recipient)) {
            recipient = Mms.extractAddrSpec(recipient);
        } else {
            recipient = PhoneNumberUtils.stripSeparators(recipient);
        }
        MmsReportStatus status = DeliveryReportFragment.queryStatusByRecipient(reportStatus, recipient);
        if (status == null) {
            return null;
        }
        if (status.readStatus == 128) {
            return context.getString(R.string.status_read);
        }
        if (status.deliveryStatus == 129) {
            return context.getString(R.string.message_status_delivered);
        }
        return null;
    }

    public static String checkMmsReportItems(Context context, MessageItem msgItem) {
        List<MmsReportRequest> reportReqs = getMmsReportRequests(context, msgItem);
        if (reportReqs == null || reportReqs.size() == 0) {
            return null;
        }
        Map<String, MmsReportStatus> reportStatus = getMmsReportStatus(context, msgItem);
        String status = null;
        CharSequence tmp = null;
        for (MmsReportRequest reportReq : reportReqs) {
            status = checkMmsReportItemStatus(context, reportReq, reportStatus);
            if (status != null) {
                if (status.equals(context.getString(R.string.status_read))) {
                    return status;
                }
                if (tmp == null && status.equals(context.getString(R.string.message_status_delivered))) {
                    tmp = status;
                }
            }
        }
        if (!TextUtils.isEmpty(tmp) && TextUtils.isEmpty(r4)) {
            status = tmp;
        }
        return status;
    }

    public static List<MmsReportRequest> getMmsReportRequests(Context context, MessageItem msgItem) {
        Context context2 = context;
        Cursor c = SqliteWrapper.query(context2, context.getContentResolver(), Uri.withAppendedPath(Mms.REPORT_REQUEST_URI, String.valueOf(msgItem.getMessageId())), DeliveryReportFragment.MMS_REPORT_REQUEST_PROJECTION, null, null, null);
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

    public static Map<String, MmsReportStatus> getMmsReportStatus(Context context, MessageItem msgItem) {
        Context context2 = context;
        Cursor c = SqliteWrapper.query(context2, context.getContentResolver(), Uri.withAppendedPath(Mms.REPORT_STATUS_URI, String.valueOf(msgItem.getMessageId())), DeliveryReportFragment.MMS_REPORT_STATUS_PROJECTION, null, null, null);
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

    public void setSearchString(String aSearchString) {
        if (HwCustMmsConfigImpl.getSupportSearchConversation()) {
            this.mSearchString = aSearchString;
        }
    }

    public void highlightWord(TextView aSpanTextView, int aBoxType) {
        if (HwCustMmsConfigImpl.getSupportSearchConversation() && aSpanTextView != null && aSpanTextView.getText() != null && !TextUtils.isEmpty(aSpanTextView.getText().toString()) && !TextUtils.isEmpty(this.mSearchString)) {
            String aSpanText = aSpanTextView.getText().toString().toLowerCase();
            this.mSearchString = this.mSearchString.toLowerCase();
            int offset = aSpanText.indexOf(this.mSearchString, 0);
            Spannable lWordtoSpan = new SpannableString(aSpanTextView.getText());
            int index = 0;
            while (index < aSpanText.length() && offset != -1) {
                offset = aSpanText.indexOf(this.mSearchString, index);
                if (offset != -1) {
                    lWordtoSpan.setSpan(new StyleSpan(1), offset, this.mSearchString.length() + offset, 33);
                    lWordtoSpan.setSpan(new AbsoluteSizeSpan(15, true), offset, this.mSearchString.length() + offset, 33);
                    aSpanTextView.setText(lWordtoSpan, BufferType.SPANNABLE);
                }
                index = offset + 1;
            }
        }
    }
}
