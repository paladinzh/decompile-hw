package com.android.contacts.util;

import android.provider.ContactsContract.CommonDataKinds.Event;
import com.google.android.gms.R;

public class EventExt {
    public static int getTypeResource(int type) {
        if (type == 4) {
            return R.string.event_lunar_birthday;
        }
        if (type == 2) {
            return R.string.event_important_date;
        }
        return Event.getTypeResource(Integer.valueOf(type));
    }
}
