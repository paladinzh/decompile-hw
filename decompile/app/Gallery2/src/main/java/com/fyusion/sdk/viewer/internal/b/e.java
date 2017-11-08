package com.fyusion.sdk.viewer.internal.b;

import com.android.gallery3d.gadget.XmlUtils;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/* compiled from: Unknown */
public interface e {
    public static final Charset a = Charset.forName(XmlUtils.INPUT_ENCODING);

    void a(MessageDigest messageDigest);

    String d();

    boolean equals(Object obj);
}
