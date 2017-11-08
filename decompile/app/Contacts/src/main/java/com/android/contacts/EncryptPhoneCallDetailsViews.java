package com.android.contacts;

import android.view.View;
import android.widget.ImageView;
import com.google.android.gms.R;

public class EncryptPhoneCallDetailsViews {
    private ImageView mEncryptCallView;

    public void initEncryptCallView(View view) {
        View eView = view.findViewById(R.id.encrypt_call);
        if (eView == null) {
            eView = null;
        } else {
            ImageView eView2 = (ImageView) eView;
        }
        this.mEncryptCallView = eView;
    }

    public ImageView getEncryptCallView() {
        return this.mEncryptCallView;
    }
}
