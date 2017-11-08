package com.android.contacts;

import android.content.Context;
import android.net.Uri;
import java.util.ArrayList;

public class HwCustChooseSubActivity {
    public Context mContext;

    public HwCustChooseSubActivity(Context context) {
        this.mContext = context;
    }

    public boolean buildSimViewEntry(String number, Uri uri, ArrayList<SimViewEntry> arrayList) {
        return false;
    }

    public void encryptCallChooseDialog(int slot, Uri uri, boolean aLearn, String number) {
    }

    public boolean isCdmaBySlot(int slot) {
        return false;
    }
}
