package com.android.contacts.hap.preference;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

public class ContactSwitchPreference extends SwitchPreference {
    public ContactSwitchPreference(Context context) {
        super(context);
    }

    public ContactSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContactSwitchPreference(Context context, AttributeSet attrs, int defstyles) {
        super(context, attrs, defstyles);
    }
}
