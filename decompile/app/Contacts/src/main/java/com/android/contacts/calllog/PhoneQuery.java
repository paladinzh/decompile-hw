package com.android.contacts.calllog;

import android.net.Uri;
import com.android.contacts.compatibility.CompatUtils;

final class PhoneQuery {
    private static final String[] BACKWARD_COMPATIBLE_NON_SIP_PHONE_LOOKUP_PROJECTION = new String[]{"_id", "display_name", "type", "label", "number", "normalized_number", "photo_id", "lookup", "photo_uri"};
    private static final String[] PHONE_LOOKUP_PROJECTION = new String[]{"contact_id", "display_name", "type", "label", "number", "normalized_number", "photo_id", "lookup", "photo_uri"};

    PhoneQuery() {
    }

    public static String[] getPhoneLookupProjection(Uri phoneLookupUri) {
        if (CompatUtils.isNCompatible()) {
            return PHONE_LOOKUP_PROJECTION;
        }
        String[] strArr;
        if (phoneLookupUri.getBooleanQueryParameter("sip", false)) {
            strArr = PHONE_LOOKUP_PROJECTION;
        } else {
            strArr = BACKWARD_COMPATIBLE_NON_SIP_PHONE_LOOKUP_PROJECTION;
        }
        return strArr;
    }
}
