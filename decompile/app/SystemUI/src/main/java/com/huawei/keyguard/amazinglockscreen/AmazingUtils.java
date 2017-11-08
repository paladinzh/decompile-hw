package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.ParseDescriptionXml;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class AmazingUtils {
    private static float sScalePara = 1.0f;

    public static void calScreenScale(Context context) {
        ParseDescriptionXml parseXml = new ParseDescriptionXml();
        parseXml.parseScreenSizeFromXml();
        sScalePara = parseXml.getScalePara(context);
        HwLog.d("AmazingUtils", "calScreenScale sScalePara=" + sScalePara);
    }

    public static float getScalePara() {
        if (sScalePara == 0.0f) {
            sScalePara = 1.0f;
        }
        return sScalePara;
    }

    public static void calScaleParaEMUI30(Context context, Element rootElement) {
        int themeScreenWidth = parseScreenWidth(rootElement);
        if (context == null) {
            HwLog.w("AmazingUtils", "calScaleParaEMUI30 context is null");
            return;
        }
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        if (themeScreenWidth != 0) {
            sScalePara = ((float) screenWidth) / ((float) themeScreenWidth);
        }
    }

    private static int parseScreenWidth(Element rootElement) {
        NamedNodeMap rootAttrs = rootElement.getAttributes();
        int screenWidth = 0;
        if (rootAttrs == null) {
            return 0;
        }
        int attrsLength = rootAttrs.getLength();
        for (int j = 0; j < attrsLength; j++) {
            String name = rootAttrs.item(j).getNodeName();
            String value = rootAttrs.item(j).getNodeValue();
            if ("screenwidth".equalsIgnoreCase(name)) {
                screenWidth = Integer.parseInt(value);
                break;
            }
        }
        return screenWidth;
    }
}
