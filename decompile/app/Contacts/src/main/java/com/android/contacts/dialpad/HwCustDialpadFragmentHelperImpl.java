package com.android.contacts.dialpad;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.contacts.activities.HwCustCommonUtilMethods;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;

public class HwCustDialpadFragmentHelperImpl extends HwCustDialpadFragmentHelper {
    public void customizeDialPadView(View fragmentView, final Activity activity, LayoutInflater inflater, boolean mIsLandscape) {
        if (HwCustContactFeatureUtils.isShowVisualMailBox() && !mIsLandscape) {
            LinearLayout dialpadAdditionalButtonsLayout = (LinearLayout) fragmentView.findViewById(R.id.dialpadAdditionalButtonsWithIpCall);
            View dialButton = dialpadAdditionalButtonsLayout.findViewById(R.id.dialButton);
            View vvmView = inflater.inflate(R.layout.vvm_button, null, false);
            int i = 0;
            while (dialpadAdditionalButtonsLayout.getChildAt(i) != dialButton) {
                i++;
            }
            dialpadAdditionalButtonsLayout.addView(vvmView, i + 1);
            View vvmMenuButton = fragmentView.findViewById(R.id.vvmButton);
            if (vvmMenuButton != null) {
                vvmMenuButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        HwCustCommonUtilMethods.startVVM(activity);
                    }
                });
            }
        }
    }

    public boolean setSearchBtnsPadding(View mSearchButton, View overflowMenuButton, View mDeleteButton, View mCollectionButtion, Context context) {
        if (!HwCustContactFeatureUtils.isShowVisualMailBox() || context == null) {
            return false;
        }
        int paddingStart = context.getResources().getDimensionPixelSize(R.dimen.contact_dialpad_btn_search_padding_hidden);
        int paddingEnd = paddingStart;
        if (mSearchButton != null) {
            mSearchButton.setPadding(paddingStart, 0, paddingStart, 0);
        }
        if (overflowMenuButton != null) {
            overflowMenuButton.setPadding(paddingStart, 0, paddingStart, 0);
        }
        if (mDeleteButton != null) {
            mDeleteButton.setPadding(paddingStart, 0, paddingStart, 0);
        }
        if (mCollectionButtion != null) {
            mCollectionButtion.setPadding(paddingStart, 0, paddingStart, 0);
        }
        return true;
    }

    public int getDialBtnWidthFromCust(int dialBtnWidth, Context context) {
        if (!HwCustContactFeatureUtils.isShowVisualMailBox() || context == null) {
            return dialBtnWidth;
        }
        return context.getResources().getDimensionPixelSize(R.dimen.dialpad_dial_button_width);
    }

    public void removeVvmIcon(View dialpadView) {
        if (HwCustContactFeatureUtils.isShowVisualMailBox() && dialpadView != null) {
            ((ImageView) ((LinearLayout) dialpadView.findViewById(R.id.contacts_dialpad)).findViewById(R.id.dialpad_key_voicemail)).setVisibility(8);
        }
    }

    public boolean isVOWifiCallEnabled(Context aContext) {
        return HwCustContactFeatureUtils.isWifiCallEnabled(aContext);
    }

    public int getVOWifiCallBtnIconForSingleSim(int aImageId) {
        return HwCustContactFeatureUtils.isVOWifiFeatureEnabled() ? R.drawable.call_wifi_dialer_selector : aImageId;
    }
}
