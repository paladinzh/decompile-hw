package com.android.contacts.calllog;

import android.widget.TextView;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustCallLogDetailHistoryAdapterImpl extends HwCustCallLogDetailHistoryAdapter {
    public void hideCallTypeAndDurationView(TextView callTypeView, TextView durationView) {
        if (!HwCustContactFeatureUtils.isIncludeCallDurationDisplay()) {
            if (callTypeView != null) {
                callTypeView.setVisibility(4);
                callTypeView.setText("");
            }
            if (durationView != null) {
                durationView.setVisibility(4);
                durationView.setText("");
            }
        }
    }
}
