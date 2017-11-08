package android.support.graphics.drawable;

import android.content.res.TypedArray;
import org.xmlpull.v1.XmlPullParser;

class TypedArrayUtils {
    TypedArrayUtils() {
    }

    public static boolean hasAttribute(XmlPullParser parser, String attrName) {
        return parser.getAttributeValue("http://schemas.android.com/apk/res/android", attrName) != null;
    }

    public static float getNamedFloat(TypedArray a, XmlPullParser parser, String attrName, int resId, float defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getFloat(resId, defaultValue);
        }
        return defaultValue;
    }

    public static boolean getNamedBoolean(TypedArray a, XmlPullParser parser, String attrName, int resId, boolean defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getBoolean(resId, defaultValue);
        }
        return defaultValue;
    }

    public static int getNamedInt(TypedArray a, XmlPullParser parser, String attrName, int resId, int defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getInt(resId, defaultValue);
        }
        return defaultValue;
    }

    public static int getNamedColor(TypedArray a, XmlPullParser parser, String attrName, int resId, int defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getColor(resId, defaultValue);
        }
        return defaultValue;
    }
}
