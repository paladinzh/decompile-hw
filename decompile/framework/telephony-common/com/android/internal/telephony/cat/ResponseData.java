package com.android.internal.telephony.cat;

import com.android.internal.telephony.CallFailCause;
import java.io.ByteArrayOutputStream;

abstract class ResponseData {
    public abstract void format(ByteArrayOutputStream byteArrayOutputStream);

    ResponseData() {
    }

    public static void writeLength(ByteArrayOutputStream buf, int length) {
        if (length > CallFailCause.INTERWORKING_UNSPECIFIED) {
            buf.write(129);
        }
        buf.write(length);
    }
}
