package com.android.mms.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.R;

public class DeliveryReportListItem extends LinearLayout {
    private TextView mDeliveryDateView;
    private ImageView mIconView;
    private TextView mRecipientView;
    private TextView mStatusView;

    DeliveryReportListItem(Context context) {
        super(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mRecipientView = (TextView) findViewById(R.id.recipient);
        this.mStatusView = (TextView) findViewById(R.id.status);
        this.mDeliveryDateView = (TextView) findViewById(R.id.delivery_date);
        this.mIconView = (ImageView) findViewById(R.id.icon);
    }

    public DeliveryReportListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public final void bind(String recipient, String status, String deliveryDate) {
        if (TextUtils.isEmpty(recipient)) {
            this.mRecipientView.setText("");
        } else {
            this.mRecipientView.setText(recipient);
        }
        this.mStatusView.setText(status);
        Context context = getContext();
        String receivedStr = context.getString(R.string.status_received);
        String failedStr = context.getString(R.string.status_failed_Toast);
        String pendingStr = context.getString(R.string.status_pending);
        String rejectStr = context.getString(R.string.status_rejected);
        if (status.compareTo(receivedStr) == 0) {
            this.mIconView.setImageResource(R.drawable.csp_menu_collapse_dark);
        } else if (status.compareTo(failedStr) == 0) {
            this.mIconView.setImageResource(R.drawable.csp_menu_expand_dark);
        } else if (status.compareTo(pendingStr) == 0) {
            this.mIconView.setImageResource(R.drawable.csp_menu_msg_compose_holo_dark);
        } else if (status.compareTo(rejectStr) == 0) {
            this.mIconView.setImageResource(R.drawable.csp_menu_expand_dark);
        }
        if (TextUtils.isEmpty(deliveryDate)) {
            this.mDeliveryDateView.setVisibility(8);
            return;
        }
        this.mDeliveryDateView.setVisibility(0);
        this.mDeliveryDateView.setText(deliveryDate);
    }
}
