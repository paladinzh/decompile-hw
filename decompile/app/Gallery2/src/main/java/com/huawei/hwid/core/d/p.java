package com.huawei.hwid.core.d;

import android.util.Xml;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class p {
    public static void a(XmlSerializer xmlSerializer, String str, String str2) throws IllegalArgumentException, IllegalStateException, IOException {
        if (str2 != null && xmlSerializer != null && str != null) {
            xmlSerializer.startTag(null, str).text(str2).endTag(null, str);
        }
    }

    public static XmlSerializer a(OutputStream outputStream) throws IllegalArgumentException, IllegalStateException, IOException {
        XmlSerializer newSerializer = Xml.newSerializer();
        newSerializer.setOutput(outputStream, XmlUtils.INPUT_ENCODING);
        return newSerializer;
    }

    public static XmlPullParser a(byte[] bArr) throws XmlPullParserException {
        XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
        newPullParser.setInput(new ByteArrayInputStream(bArr), XmlUtils.INPUT_ENCODING);
        return newPullParser;
    }
}
