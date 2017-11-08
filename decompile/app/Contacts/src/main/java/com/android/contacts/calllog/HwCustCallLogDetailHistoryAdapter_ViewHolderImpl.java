package com.android.contacts.calllog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import com.android.contacts.HwCustPhoneCallDetails;
import com.android.contacts.hap.encryptcall.EncryptCallUtils;
import com.google.android.gms.R;

public class HwCustCallLogDetailHistoryAdapter_ViewHolderImpl extends HwCustCallLogDetailHistoryAdapter_ViewHolder {
    private ImageView encryptCallView;

    public void initEncryptCallView(View view) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            View eView = view.findViewById(R.id.encrypt_call);
            if (eView == null) {
                eView = null;
            } else {
                ImageView eView2 = (ImageView) eView;
            }
            this.encryptCallView = eView;
        }
    }

    public int updateSizeOfOtherItemsExceptNumberView(Context context, int sizeOfOtherItemsExceptNumberView) {
        if (!EncryptCallUtils.isEncryptCallEnable()) {
            return sizeOfOtherItemsExceptNumberView;
        }
        int encryptCallViewWidth = context.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_cardtype_width);
        if (this.encryptCallView == null || this.encryptCallView.getVisibility() != 0) {
            return sizeOfOtherItemsExceptNumberView;
        }
        return sizeOfOtherItemsExceptNumberView + encryptCallViewWidth;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateEncryptCallViewVisibility(HwCustPhoneCallDetails details) {
        if (!(details == null || !EncryptCallUtils.isEncryptCallEnable() || this.encryptCallView == null)) {
            int i;
            ImageView imageView = this.encryptCallView;
            if (details.isEncryptCall()) {
                i = 0;
            } else {
                i = 8;
            }
            imageView.setVisibility(i);
        }
    }
}
