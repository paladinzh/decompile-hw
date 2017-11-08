package cn.com.xy.sms.sdk.util.a;

import android.util.Xml;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;

/* compiled from: Unknown */
public final class a {
    private static void a(InputStream inputStream, String[] strArr, XyCallBack xyCallBack) {
        XmlPullParser newPullParser = Xml.newPullParser();
        newPullParser.setInput(inputStream, "UTF-8");
        for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
            switch (eventType) {
                case 2:
                    String name = newPullParser.getName();
                    for (Object equals : strArr) {
                        if (name.equals(equals)) {
                            xyCallBack.execute(name, newPullParser.nextText());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
