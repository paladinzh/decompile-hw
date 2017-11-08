package com.android.contacts.hap.list;

import android.content.Context;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustContactMultiselectionFragmentImpl extends HwCustContactMultiselectionFragment {
    protected static final String TAG = "HwContactMultiselectionFragmentImpl";

    public HwCustContactMultiselectionFragmentImpl(Context context) {
        super(context);
    }

    public boolean supportReadOnly() {
        return HwCustContactFeatureUtils.isSupportPredefinedReadOnlyFeature();
    }
}
