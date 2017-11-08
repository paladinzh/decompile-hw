package com.android.contacts.ext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.android.contacts.model.Contact;
import java.util.ArrayList;

public class HwCustContactAndProfileInitializer {
    public void handleInitPreBootCust(Context mContext) {
    }

    public void handleSimSwapCustomization(Context mContext) {
    }

    public boolean isShowMyInfoForMyProfile() {
        return false;
    }

    public void showMyInfoForMyProfile(Context aContext, String aDisplayName, TextView aNameTextView) {
    }

    public void putNameEntryForQRCode(Context aContext, String mTitleName, ArrayList<String> arrayList, Bundle aBundle) {
    }

    public void checkAndAddIntentExtra(Intent aIntent, String aDisplayName, Context aContext) {
    }

    public CharSequence getDisplayNameForProfileDetails(Context aContext, Contact data, CharSequence defaultName) {
        return defaultName;
    }
}
