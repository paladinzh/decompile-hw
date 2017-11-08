package com.android.contacts.util;

import android.content.Intent;
import android.net.Uri;

public class StructuredPostalUtils {
    private StructuredPostalUtils() {
    }

    public static Intent getViewPostalAddressIntent(String postalAddress) {
        return new Intent("android.intent.action.VIEW", getPostalAddressUri(postalAddress));
    }

    public static Uri getPostalAddressUri(String postalAddress) {
        return Uri.parse("geo:0,0?q=" + Uri.encode(postalAddress));
    }
}
