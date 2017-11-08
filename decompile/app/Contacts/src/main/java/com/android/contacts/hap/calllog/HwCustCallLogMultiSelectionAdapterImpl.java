package com.android.contacts.hap.calllog;

import android.content.Context;
import android.database.Cursor;
import com.android.contacts.calllog.CallLogListItemViews;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustCallLogMultiSelectionAdapterImpl extends HwCustCallLogMultiSelectionAdapter {
    protected static final int DEFAULT_CALL_FEATURES_VALUE = 0;

    public HwCustCallLogMultiSelectionAdapterImpl(Context mContext) {
        super(mContext);
    }

    public void checkCallTypeFeaturesVisibility(CallLogListItemViews views, Cursor c) {
        long callTypeFeatures = 0;
        if (HwCustContactFeatureUtils.isSupportCallFeatureIcon()) {
            int calltypefeatureindex = c.getColumnIndex("features");
            if (calltypefeatureindex >= 0) {
                callTypeFeatures = c.getLong(calltypefeatureindex);
            }
        }
        views.phoneCallDetailsViews.hdcallIcon.setVisibility(callTypeFeatures == 1 ? 0 : 8);
    }
}
