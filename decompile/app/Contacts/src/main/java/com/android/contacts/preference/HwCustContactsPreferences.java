package com.android.contacts.preference;

import android.content.Context;

public class HwCustContactsPreferences {
    static final int DISPLAY_ORDER = 1;
    static final int SORT_ORDER = 0;
    String changeSortByLang = null;
    Context mContext;

    public HwCustContactsPreferences(Context context) {
        this.mContext = context;
    }

    public boolean isChangeSortByLang() {
        return false;
    }

    public int getOrderByLang(int oldOrder, int i) {
        return -1;
    }
}
