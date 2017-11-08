package com.android.contacts.list;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.contacts.MoreContactUtils;

public class ContactTilePhoneFrequentView extends ContactTileView {
    private String mPhoneNumberString;

    public ContactTilePhoneFrequentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected OnClickListener createClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                if (ContactTilePhoneFrequentView.this.mListener != null) {
                    if (TextUtils.isEmpty(ContactTilePhoneFrequentView.this.mPhoneNumberString)) {
                        ContactTilePhoneFrequentView.this.mListener.onContactSelected(ContactTilePhoneFrequentView.this.getLookupUri(), MoreContactUtils.getTargetRectFromView(ContactTilePhoneFrequentView.this.getContext(), ContactTilePhoneFrequentView.this));
                    } else {
                        ContactTilePhoneFrequentView.this.mListener.onCallNumberDirectly(ContactTilePhoneFrequentView.this.mPhoneNumberString);
                    }
                }
            }
        };
    }
}
