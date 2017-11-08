package com.android.rcs.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.ui.DeliveryReportItem;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.ArrayList;
import java.util.List;

public class RcsDeliveryReportFragment {
    public static final Uri GET_CHAT_ALL_URI = Uri.parse("content://rcsim/chat");
    static final String[] SMS_REPORT_STATUS_PROJECTION = new String[]{"address", "status", "date_sent", NumberInfo.TYPE_KEY, "date"};
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private Context mContext;

    public RcsDeliveryReportFragment(Context context) {
        this.mContext = context;
    }

    public boolean isRcsMsgType(String messageType) {
        if (mIsRcsOn) {
            return "chat".equals(messageType);
        }
        return false;
    }

    public List<DeliveryReportItem> getRcsReportItems(long messageId) {
        if (!mIsRcsOn) {
            return null;
        }
        Cursor c = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), GET_CHAT_ALL_URI, SMS_REPORT_STATUS_PROJECTION, "_id = " + messageId, null, null);
        if (c == null) {
            MLog.w("RcsDeliveryReportFragment", "getRcsReportItems query cursor is null");
            return null;
        }
        try {
            if (c.getCount() <= 0) {
                MLog.w("RcsDeliveryReportFragment", "get none im report items");
                return null;
            }
            List<DeliveryReportItem> items = new ArrayList();
            while (c.moveToNext()) {
                String str = null;
                long deliveryDate = c.getLong(2);
                int messageType = c.getInt(3);
                if (messageType == 2 && deliveryDate >= 0) {
                    if (deliveryDate == 1 || deliveryDate == 0) {
                        deliveryDate = c.getLong(4);
                    }
                    str = this.mContext.getString(R.string.delivered_label) + MessageUtils.formatTimeStampString(this.mContext, deliveryDate, true, true);
                    MLog.d("RcsDeliveryReportFragment", "deliveryDateString = " + str);
                }
                items.add(new DeliveryReportItem(this.mContext.getString(R.string.recipient_label) + '‪' + c.getString(0) + '‬', this.mContext.getString(R.string.status_label) + getImStatusText(c.getInt(1), messageType), str));
            }
            c.close();
            return items;
        } finally {
            c.close();
        }
    }

    private String getImStatusText(int status, int type) {
        if (status == -1) {
            return this.mContext.getString(R.string.status_none);
        }
        if (status >= 64) {
            return this.mContext.getString(R.string.status_failed_Toast);
        }
        if (status >= 32) {
            if (type == 5) {
                return this.mContext.getString(R.string.status_failed_Toast);
            }
            return this.mContext.getString(R.string.status_pending);
        } else if (status == 0) {
            return this.mContext.getString(R.string.status_read);
        } else {
            return this.mContext.getString(R.string.status_received);
        }
    }
}
