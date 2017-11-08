package com.huawei.keyguard.cover;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AnalogClockResourceUtils {
    public static String parseThemeClockType() {
        String themeType = "OtherTheme";
        Document document = XmlUtils.getXMLDocument("/data/skin/coverscreen/theme.xml");
        if (document == null) {
            return themeType;
        }
        Element rootElement = document.getDocumentElement();
        if (rootElement == null) {
            return themeType;
        }
        NodeList list = rootElement.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if ("ClockStyle".equalsIgnoreCase(node.getNodeName())) {
                themeType = node.getTextContent();
                break;
            }
        }
        HwLog.d("AmazingCoverUtils", "parseThemeClockType : themeType = " + themeType);
        return themeType;
    }

    public static boolean isThemeAnalogClockType() {
        if (!SystemProperties.getBoolean("ro.config.enable_amazingcover", false)) {
            return false;
        }
        return "1".equals(parseThemeClockType());
    }

    public static Drawable[] getAnalogClockDrawable() {
        return new Drawable[]{Drawable.createFromPath("/data/skin/coverscreen/drawable/cover_analog_clock_h_shadow.png"), Drawable.createFromPath("/data/skin/coverscreen/drawable/cover_analog_clock_m_shadow.png"), Drawable.createFromPath("/data/skin/coverscreen/drawable/cover_analog_clock_s_shadow.png"), Drawable.createFromPath("/data/skin/coverscreen/drawable/cover_analog_clock_h.png"), Drawable.createFromPath("/data/skin/coverscreen/drawable/cover_analog_clock_m.png"), Drawable.createFromPath("/data/skin/coverscreen/drawable/cover_analog_clock_s.png")};
    }

    public static Drawable getAnalogClockBg() {
        return Drawable.createFromPath("/data/skin/coverscreen/drawable/cover_analog_clock_bg.png");
    }

    public static Drawable[] getAnalogClockDrawableFromResource(Context context) {
        return new Drawable[]{context.getDrawable(R$drawable.cover_analog_clock_h_shadow), context.getDrawable(R$drawable.cover_analog_clock_m_shadow), context.getDrawable(R$drawable.cover_analog_clock_s_shadow), context.getDrawable(R$drawable.cover_analog_clock_h), context.getDrawable(R$drawable.cover_analog_clock_m), context.getDrawable(R$drawable.cover_analog_clock_s)};
    }

    public static Drawable getAnalogClockBgFromResource(Context context) {
        return context.getDrawable(R$drawable.cover_analog_clock_bg);
    }
}
