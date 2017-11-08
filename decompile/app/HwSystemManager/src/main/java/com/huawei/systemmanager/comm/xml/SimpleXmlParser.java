package com.huawei.systemmanager.comm.xml;

import android.content.Context;
import android.util.Xml;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.util.HwLog;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class SimpleXmlParser {
    private static final int SECONDARY_LEVEL_NODE = 2;
    private static final String TAG = SimpleXmlParser.class.getSimpleName();

    SimpleXmlParser() {
    }

    public static List<SimpleXmlRow> parseSimpleXml(String filePath) {
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream fis = new FileInputStream(filePath);
            try {
                XmlPullParser xpp = Xml.newPullParser();
                xpp.setInput(fis, null);
                List<SimpleXmlRow> objs = new ArrayList();
                parseSimpleXmlInner(xpp, objs);
                ParserAssistUtils.close(fis);
                return objs;
            } catch (FileNotFoundException e) {
                inputStream = fis;
                HwLog.w(TAG, "parseSimpleXml catch FileNotFoundException: " + filePath);
                ParserAssistUtils.close(inputStream);
                return null;
            } catch (XmlPullParserException e2) {
                inputStream = fis;
                HwLog.e(TAG, "parseSimpleXml catch XmlPullParserException: " + filePath);
                ParserAssistUtils.close(inputStream);
                return null;
            } catch (IOException e3) {
                inputStream = fis;
                try {
                    HwLog.e(TAG, "parseSimpleXml catch IOException: " + filePath);
                    ParserAssistUtils.close(inputStream);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    ParserAssistUtils.close(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = fis;
                ParserAssistUtils.close(inputStream);
                throw th;
            }
        } catch (FileNotFoundException e4) {
            HwLog.w(TAG, "parseSimpleXml catch FileNotFoundException: " + filePath);
            ParserAssistUtils.close(inputStream);
            return null;
        } catch (XmlPullParserException e5) {
            HwLog.e(TAG, "parseSimpleXml catch XmlPullParserException: " + filePath);
            ParserAssistUtils.close(inputStream);
            return null;
        } catch (IOException e6) {
            HwLog.e(TAG, "parseSimpleXml catch IOException: " + filePath);
            ParserAssistUtils.close(inputStream);
            return null;
        }
    }

    public static List<SimpleXmlRow> parseSimpleAssetXml(Context context, String assetFile) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetFile);
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(inputStream, null);
            List<SimpleXmlRow> objs = new ArrayList();
            parseSimpleXmlInner(xpp, objs);
            return objs;
        } catch (FileNotFoundException e) {
            HwLog.e(TAG, "parseSimpleXml catch FileNotFoundException: " + assetFile);
            return null;
        } catch (XmlPullParserException e2) {
            HwLog.e(TAG, "parseSimpleXml catch XmlPullParserException: " + assetFile);
            return null;
        } catch (IOException e3) {
            HwLog.e(TAG, "parseSimpleXml catch IOException: " + assetFile);
            return null;
        } finally {
            ParserAssistUtils.close(inputStream);
        }
    }

    public static List<SimpleXmlRow> parseSimpleXml(XmlPullParser xpp) {
        try {
            List<SimpleXmlRow> objs = new ArrayList();
            parseSimpleXmlInner(xpp, objs);
            return objs;
        } catch (XmlPullParserException e) {
            HwLog.e(TAG, "parseSimpleXml catch XmlPullParserException");
            return null;
        } catch (IOException e2) {
            HwLog.e(TAG, "parseSimpleXml catch IOException");
            return null;
        }
    }

    private static void parseSimpleXmlInner(XmlPullParser xpp, List<SimpleXmlRow> objs) throws XmlPullParserException, IOException {
        int eventType = xpp.getEventType();
        Object obj = null;
        while (eventType != 1) {
            if (eventType == 2) {
                if (2 == xpp.getDepth()) {
                    obj = new SimpleXmlRow(xpp.getName());
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        obj.addAttribute(xpp.getAttributeName(i), xpp.getAttributeValue(i));
                    }
                }
            } else if (eventType == 3 && 2 == xpp.getDepth()) {
                objs.add(obj);
            }
            eventType = xpp.next();
        }
    }
}
