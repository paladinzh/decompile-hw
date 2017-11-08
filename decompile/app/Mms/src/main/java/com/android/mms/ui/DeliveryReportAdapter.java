package com.android.mms.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.google.android.gms.R;
import java.util.List;

public class DeliveryReportAdapter extends ArrayAdapter<DeliveryReportItem> {
    Context mContext = null;

    public DeliveryReportAdapter(Context context, List<DeliveryReportItem> items) {
        super(context, R.layout.delivery_report_list_item, R.id.recipient, items);
        this.mContext = context;
    }

    public View getView(int position, View view, ViewGroup viewGroup) {
        DeliveryReportListItem listItem;
        DeliveryReportItem item = (DeliveryReportItem) getItem(position);
        if (view == null) {
            listItem = (DeliveryReportListItem) LayoutInflater.from(getContext()).inflate(R.layout.delivery_report_list_item, viewGroup, false);
        } else if (!(view instanceof DeliveryReportListItem)) {
            return view;
        } else {
            listItem = (DeliveryReportListItem) view;
        }
        listItem.bind(item.recipient, item.status, item.deliveryDate);
        if (TextUtils.isEmpty(item.deliveryDate)) {
            listItem.setMinimumHeight((int) this.mContext.getResources().getDimension(R.dimen.delivery_report_item_height));
        } else {
            listItem.setMinimumHeight((int) this.mContext.getResources().getDimension(R.dimen.delivery_report_item_with_date_height));
        }
        return listItem;
    }

    public boolean isEnabled(int position) {
        return false;
    }
}
