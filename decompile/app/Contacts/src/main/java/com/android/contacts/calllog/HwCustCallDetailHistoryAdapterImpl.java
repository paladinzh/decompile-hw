package com.android.contacts.calllog;

import android.widget.TextView;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustCallDetailHistoryAdapterImpl extends HwCustCallDetailHistoryAdapter {
    public void hideCallTypeAndDurationView(TextView callTypeView, TextView detailView) {
        if (!HwCustContactFeatureUtils.isIncludeCallDurationDisplay()) {
            if (callTypeView != null) {
                callTypeView.setVisibility(4);
                callTypeView.setText("");
            }
            if (detailView != null) {
                detailView.setVisibility(4);
                detailView.setText("");
            }
        }
    }
}
