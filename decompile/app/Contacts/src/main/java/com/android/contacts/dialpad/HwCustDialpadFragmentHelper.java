package com.android.contacts.dialpad;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class HwCustDialpadFragmentHelper {
    public void customizeDialPadView(View fragmentView, Activity activity, LayoutInflater inflater, boolean mIsLandscape) {
    }

    public int getDialBtnWidthFromCust(int dialBtnWidth, Context context) {
        return dialBtnWidth;
    }

    public void removeVvmIcon(View dialpadView) {
    }

    public boolean setSearchBtnsPadding(View mSearchButton, View overflowMenuButton, View mDeleteButton, View mCollectionButtion, Context context) {
        return false;
    }

    public boolean isVOWifiCallEnabled(Context aContext) {
        return false;
    }

    public int getVOWifiCallBtnIconForSingleSim(int aImageId) {
        return aImageId;
    }
}
