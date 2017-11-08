package com.android.contacts.hap.list;

import android.content.Context;
import android.database.Cursor;
import java.util.List;

public class HwCustFrequntContactMultiselectListLoader {
    protected static final String TAG = "HwCustFrequntContactMultiselectListLoader";
    Context mContext;

    public HwCustFrequntContactMultiselectListLoader(Context context) {
        this.mContext = context;
    }

    public boolean getEnableEmailContactInMms() {
        return false;
    }

    public void setSelectionQueryArgs(StringBuilder selectionBuilder, List<String> list) {
    }

    public void initService(Context context) {
    }

    public void setSelectionAndSelectionArgsForCustomizations(int filterType, StringBuilder aSelectionBuilder, List<String> list) {
    }

    public boolean isCustomizationSkipByNumber(int filterType, String number) {
        return true;
    }

    public void setParentContext(Context context) {
    }

    public void addSelectedNumsToUri(int filterType, Cursor cursor) {
    }
}
