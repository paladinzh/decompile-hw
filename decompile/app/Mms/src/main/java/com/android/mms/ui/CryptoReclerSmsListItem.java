package com.android.mms.ui;

import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;

public class CryptoReclerSmsListItem {
    private ImageView mEncryptSmsImg = null;

    private void showEncryptSmsImg(View rootView) {
        if (rootView != null) {
            if (this.mEncryptSmsImg == null) {
                ViewStub stub = (ViewStub) rootView.findViewById(R.id.encrypt_mms_image_view_stub);
                if (stub == null) {
                    MLog.d("CryptoReclerSmsListItem", "can not find encrypt_mms_image_view_stub !");
                    return;
                } else {
                    stub.setLayoutResource(R.layout.encrypt_sms_image_view);
                    this.mEncryptSmsImg = (ImageView) stub.inflate();
                }
            }
            this.mEncryptSmsImg.setVisibility(0);
        }
    }

    public String checkForCryptoMessage(RecyclerSmsListItem recyclerSmsListItem, String bodyText, View itemView) {
        if (CryptoMessageServiceProxy.isLocalEncrypted(bodyText) || CryptoMessageServiceProxy.isLocalStoredNEMsg(bodyText)) {
            showEncryptSmsImg(itemView);
            return recyclerSmsListItem.getContext().getString(R.string.sms_encrypt_info);
        }
        if (this.mEncryptSmsImg != null) {
            this.mEncryptSmsImg.setVisibility(8);
        }
        return bodyText;
    }
}
