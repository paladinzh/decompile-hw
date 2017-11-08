package com.android.contacts.ext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.model.Contact;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import java.util.ArrayList;

public class HwCustContactAndProfileInitializerImpl extends HwCustContactAndProfileInitializer {
    private static final String TAG = "HwCustContactAndProfilePopulatorImpl";

    public void handleInitPreBootCust(Context aContext) {
        if (HwCustContactFeatureUtils.isAutoInsertSimNumberToProfile()) {
            HwCustProfileSimNumberUpdaterUtil.checkAndInsertProfile(aContext);
        }
    }

    public void handleSimSwapCustomization(Context aContext) {
        if (HwCustCommonConstants.IS_AAB_ATT || HwCustContactFeatureUtils.isAutoInsertSimNumberToProfile()) {
            HwCustProfileSimNumberUpdaterUtil.checkAndUpdateProfileNumber(aContext);
        }
    }

    public boolean isShowMyInfoForMyProfile() {
        return HwCustContactFeatureUtils.isShowMyInfoForMyProfile();
    }

    public void showMyInfoForMyProfile(Context aContext, String aDisplayName, TextView aNameTextView) {
        if (aContext != null && aNameTextView != null) {
            if (TextUtils.isEmpty(aDisplayName)) {
                aDisplayName = aContext.getString(R.string.string_aab_my_info);
            }
            aNameTextView.setText(aDisplayName);
        }
    }

    public void putNameEntryForQRCode(Context aContext, String aTitleName, ArrayList<String> aEntries, Bundle aBundle) {
        if (HwCustContactFeatureUtils.isShowMyInfoForMyProfile() && aContext != null && aBundle != null) {
            if (aEntries == null || aEntries.size() <= 0) {
                aBundle.putString(aTitleName, aContext.getString(R.string.string_aab_my_info));
            }
        }
    }

    public void checkAndAddIntentExtra(Intent aIntent, String aDisplayName, Context aContext) {
        if (HwCustContactFeatureUtils.isShowMyInfoForMyProfile() && aIntent != null && aContext != null && TextUtils.isEmpty(aDisplayName)) {
            aIntent.putExtra("name", aContext.getString(R.string.string_aab_my_info));
        }
    }

    public CharSequence getDisplayNameForProfileDetails(Context aContext, Contact data, CharSequence defaultName) {
        if (!HwCustContactFeatureUtils.isShowMyInfoForMyProfile() || data == null || !data.isUserProfile() || aContext == null || 40 == data.getDisplayNameSource()) {
            return defaultName;
        }
        return aContext.getResources().getString(R.string.string_aab_my_info);
    }
}
