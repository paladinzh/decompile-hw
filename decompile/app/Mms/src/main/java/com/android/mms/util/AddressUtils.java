package com.android.mms.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Telephony.Mms;
import android.text.TextUtils;
import com.google.android.gms.R;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.cspcommon.ex.SqliteWrapper;

public class AddressUtils {
    private AddressUtils() {
    }

    public static String getFrom(Context context, Uri uri) {
        String msgId = uri.getLastPathSegment();
        Builder builder = Mms.CONTENT_URI.buildUpon();
        builder.appendPath(msgId).appendPath("addr");
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), builder.build(), new String[]{"address", "charset"}, "type=137", null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String from = cursor.getString(0);
                    if (!TextUtils.isEmpty(from)) {
                        String string = new EncodedStringValue(cursor.getInt(1), PduPersister.getBytes(from)).getString();
                        return string;
                    }
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return context.getString(R.string.hidden_sender_address);
    }
}
