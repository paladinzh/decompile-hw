package com.huawei.keyguard.util;

import android.content.Context;
import android.os.Environment;
import com.huawei.keyguard.theme.ThemeCfg;
import com.huawei.keyguard.util.XmlUtils.INoteReader;
import fyusion.vislib.BuildConfig;
import java.io.File;
import org.w3c.dom.Node;

public class ParseDescriptionXml implements INoteReader {
    private String mScreenSize = BuildConfig.FLAVOR;

    public void parseNode(Node node) {
        this.mScreenSize = node.getTextContent();
    }

    public void parseScreenSizeFromXml() {
        XmlUtils.parseXmlNode(ThemeCfg.getThemeDescription(), "screen", this);
        HwLog.d("ParseDescriptionXml", "parseScreenSizeFromXml mScreenSize = " + this.mScreenSize);
    }

    public float getScalePara(Context context) {
        if (context == null) {
            HwLog.w("ParseDescriptionXml", "getScalePara context is null");
            return 1.0f;
        }
        int width = context.getResources().getDisplayMetrics().widthPixels;
        String unlockScreenSize = getCurrentUnlockThemeScreenSize();
        HwLog.d("ParseDescriptionXml", "getScalePara unlockScreenSize = " + unlockScreenSize);
        if (unlockScreenSize != null) {
            this.mScreenSize = unlockScreenSize;
        }
        int themeWidth = width;
        if ("FHD".equalsIgnoreCase(this.mScreenSize)) {
            themeWidth = 1080;
        } else if ("HD".equalsIgnoreCase(this.mScreenSize)) {
            themeWidth = 720;
        } else if ("QHD".equalsIgnoreCase(this.mScreenSize)) {
            themeWidth = 540;
        } else if ("FWVGA".equalsIgnoreCase(this.mScreenSize) || "WVGA".equalsIgnoreCase(this.mScreenSize)) {
            themeWidth = 480;
        }
        float scalePara = ((float) width) / ((float) themeWidth);
        HwLog.d("ParseDescriptionXml", "getScreenScale scalePara = " + scalePara);
        return scalePara;
    }

    public String getCurrentUnlockThemeScreenSize() {
        String rootPath = Environment.getDataDirectory().getAbsolutePath();
        if (rootPath == null) {
            HwLog.w("ParseDescriptionXml", "getCurrentUnlockThemeScreenSize rootPaht is null");
            return null;
        } else if (new File(rootPath + "/skin/unlock/FHD").exists()) {
            return "FHD";
        } else {
            if (new File(rootPath + "/skin/unlock/HD").exists()) {
                return "HD";
            }
            if (new File(rootPath + "/skin/unlock/QHD").exists()) {
                return "QHD";
            }
            if (new File(rootPath + "/skin/unlock/FWVGA").exists()) {
                return "FWVGA";
            }
            if (new File(rootPath + "/skin/unlock/WVGA").exists()) {
                return "WVGA";
            }
            return null;
        }
    }
}
