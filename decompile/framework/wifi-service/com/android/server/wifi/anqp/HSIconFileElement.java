package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HSIconFileElement extends ANQPElement {
    private final byte[] mIconData;
    private final StatusCode mStatusCode;
    private final String mType;

    public enum StatusCode {
        Success,
        FileNotFound,
        Unspecified
    }

    public HSIconFileElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        if (payload.remaining() < 4) {
            throw new ProtocolException("Truncated icon file: " + payload.remaining());
        }
        int statusID = payload.get() & 255;
        this.mStatusCode = statusID < StatusCode.values().length ? StatusCode.values()[statusID] : null;
        this.mType = Constants.getPrefixedString(payload, 1, StandardCharsets.US_ASCII);
        this.mIconData = new byte[(payload.getShort() & Constants.SHORT_MASK)];
        payload.get(this.mIconData);
    }

    public StatusCode getStatusCode() {
        return this.mStatusCode;
    }

    public String getType() {
        return this.mType;
    }

    public byte[] getIconData() {
        return this.mIconData;
    }

    public String toString() {
        return "HSIconFile{statusCode=" + this.mStatusCode + ", type='" + this.mType + '\'' + ", iconData=" + this.mIconData.length + " bytes }";
    }
}
