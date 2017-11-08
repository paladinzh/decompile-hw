package com.android.gallery3d.gadget;

import android.content.res.XmlResourceParser;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlUtils {
    public static final String INPUT_ENCODING = "UTF-8";
    public static final String START_TAG = "layout";
    public static final String TAG = "XmlUtils";
    public static final String TAG_ITEM = "item";

    public static ArrayList<AttributeEntry> parserXml(String dir, String fileName) {
        File file = new File(dir, fileName);
        if (!file.exists()) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            GalleryLog.i(TAG, "new FileInputStream() failed, reason: FileNotFoundException.");
        }
        return parserFileXml(inputStream);
    }

    public static ArrayList<AttributeEntry> parserFileXml(InputStream in) {
        XmlPullParser xmlPullParser = null;
        ArrayList<AttributeEntry> arrayList = null;
        if (in == null) {
            return null;
        }
        try {
            xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
            xmlPullParser.setInput(in, INPUT_ENCODING);
            arrayList = doParseDocument(xmlPullParser);
            Utils.closeSilently((Closeable) in);
            if (xmlPullParser != null) {
                try {
                    xmlPullParser.setInput(null);
                } catch (XmlPullParserException e) {
                    GalleryLog.i(TAG, "XmlPullParser.setInput() failed in parserFileXml(InputStream) method.");
                }
            }
        } catch (IOException e2) {
            GalleryLog.i(TAG, "An IOException has occurred in parserFileXml(InputStream) method.");
            Utils.closeSilently((Closeable) in);
            if (xmlPullParser != null) {
                try {
                    xmlPullParser.setInput(null);
                } catch (XmlPullParserException e3) {
                    GalleryLog.i(TAG, "XmlPullParser.setInput() failed in parserFileXml(InputStream) method.");
                }
            }
        } catch (XmlPullParserException e4) {
            GalleryLog.i(TAG, "An XmlPullParserException has occurred in parserFileXml(InputStream) method.");
            Utils.closeSilently((Closeable) in);
            if (xmlPullParser != null) {
                try {
                    xmlPullParser.setInput(null);
                } catch (XmlPullParserException e5) {
                    GalleryLog.i(TAG, "XmlPullParser.setInput() failed in parserFileXml(InputStream) method.");
                }
            }
        } catch (NumberFormatException e6) {
            GalleryLog.i(TAG, "An NumberFormatException has occurred in parserFileXml(InputStream) method.");
            Utils.closeSilently((Closeable) in);
            if (xmlPullParser != null) {
                try {
                    xmlPullParser.setInput(null);
                } catch (XmlPullParserException e7) {
                    GalleryLog.i(TAG, "XmlPullParser.setInput() failed in parserFileXml(InputStream) method.");
                }
            }
        } catch (Throwable th) {
            Utils.closeSilently((Closeable) in);
            if (xmlPullParser != null) {
                try {
                    xmlPullParser.setInput(null);
                } catch (XmlPullParserException e8) {
                    GalleryLog.i(TAG, "XmlPullParser.setInput() failed in parserFileXml(InputStream) method.");
                }
            }
        }
        return arrayList;
    }

    private static ArrayList<AttributeEntry> doParseDocument(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<AttributeEntry> arrayList = null;
        int eventCode = parser.getEventType();
        while (1 != eventCode) {
            if (2 == eventCode && START_TAG.equals(parser.getName())) {
                arrayList = new ArrayList();
            }
            if (2 == eventCode && TAG_ITEM.equalsIgnoreCase(parser.getName())) {
                AttributeEntry attributeEntry = new AttributeEntry();
                attributeEntry.setId(parser.getAttributeValue(0));
                attributeEntry.setMarginLeft(parser.getAttributeValue(1));
                attributeEntry.setMarginBottom(parser.getAttributeValue(2));
                attributeEntry.setmFlag(parser.getAttributeValue(3));
                attributeEntry.setmHeight(parser.getAttributeValue(4));
                attributeEntry.setmWidth(parser.getAttributeValue(5));
                if (arrayList != null) {
                    arrayList.add(attributeEntry);
                }
            }
            eventCode = parser.next();
        }
        return arrayList;
    }

    public static ArrayList<AttributeEntry> parserXml(XmlResourceParser xrp) {
        ArrayList<AttributeEntry> arrayList = null;
        if (xrp == null) {
            return null;
        }
        XmlResourceParser parser = xrp;
        try {
            arrayList = doParseDocument(xrp);
        } catch (IOException e) {
            GalleryLog.i(TAG, "An IOException has occurred in parserXml(XmlResourceParser) method.");
        } catch (XmlPullParserException e2) {
            GalleryLog.i(TAG, "An XmlPullParserException has occurred in parserXml(XmlResourceParser) method.");
        } catch (NumberFormatException e3) {
            GalleryLog.i(TAG, "An NumberFormatException has occurred in parserXml(XmlResourceParser) method.");
        }
        return arrayList;
    }
}
