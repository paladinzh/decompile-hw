package com.android.contacts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.StatusUpdates;

public class ContactPresenceIconUtil {
    public static Drawable getPresenceIcon(Context context, int status) {
        switch (status) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return context.getResources().getDrawable(StatusUpdates.getPresenceIconResourceId(status));
            default:
                return null;
        }
    }
}
