package com.android.contacts.calllog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import com.android.contacts.PhoneCallDetails.EncryptPhoneCallDetails;
import com.google.android.gms.R;

public class EncryptCallLogDetailHistoryAdapter_ViewHolder {
    private ImageView encryptCallView;

    public void initEncryptCallView(View view) {
        View eView = view.findViewById(R.id.encrypt_call);
        if (eView == null) {
            eView = null;
        } else {
            ImageView eView2 = (ImageView) eView;
        }
        this.encryptCallView = eView;
    }

    public int updateSizeOfOtherItemsExceptNumberView(Context context, int sizeOfOtherItemsExceptNumberView) {
        int encryptCallViewWidth = context.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_cardtype_width);
        if (this.encryptCallView == null || this.encryptCallView.getVisibility() != 0) {
            return sizeOfOtherItemsExceptNumberView;
        }
        return sizeOfOtherItemsExceptNumberView + encryptCallViewWidth;
    }

    public void updateEncryptCallViewVisibility(EncryptPhoneCallDetails details) {
        if (this.encryptCallView != null) {
            this.encryptCallView.setVisibility(details.isEncryptCall() ? 0 : 8);
        }
    }
}
