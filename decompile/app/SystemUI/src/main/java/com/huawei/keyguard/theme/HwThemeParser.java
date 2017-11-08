package com.huawei.keyguard.theme;

import android.content.Context;
import android.text.TextUtils;
import com.android.keyguard.R$string;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.XmlUtils;
import com.huawei.keyguard.util.XmlUtils.INoteReader;
import com.huawei.keyguard.util.XmlUtils.NodeAttributeReader;
import fyusion.vislib.BuildConfig;
import java.io.File;
import org.w3c.dom.Node;

public class HwThemeParser {
    private static HwThemeParser sInstance = null;
    private static long sLastModifyTime = 0;
    private String mClass;
    public String mDefaultTheme = null;
    private String mDynamicPath;
    private String mEnableRotate = null;
    private INoteReader mFullNodeReader = new NodeAttributeReader(new INoteReader() {
        public void parseNode(Node node) {
            String name = node.getNodeName();
            String value = node.getNodeValue();
            if ("style".equalsIgnoreCase(name)) {
                HwThemeParser.this.mStyle = value;
            } else if ("wallpaper".equalsIgnoreCase(name)) {
                HwThemeParser.this.mWallpaper = value;
            } else if ("layout".equalsIgnoreCase(name)) {
                HwThemeParser.this.mLayout = value;
            } else if ("slideinAmaze".equalsIgnoreCase(name)) {
                HwThemeParser.this.mSlideInAamze = "true".equalsIgnoreCase(value);
            } else if ("enableRotate".equalsIgnoreCase(name)) {
                HwThemeParser.this.mEnableRotate = value;
            } else if ("package".equalsIgnoreCase(name)) {
                HwThemeParser.this.mPackage = value;
            } else if ("class".equalsIgnoreCase(name)) {
                HwThemeParser.this.mClass = value;
            } else if ("dynamicPath".equalsIgnoreCase(name)) {
                HwThemeParser.this.mDynamicPath = value;
            } else if ("id".equalsIgnoreCase(name)) {
                HwThemeParser.this.mThemeId = value;
            } else if ("enable_update_magazine".equalsIgnoreCase(name) && !"true".equalsIgnoreCase(value)) {
                KeyguardCfg.setMagazineUpdateDisabled();
                KeyguardCfg.setMagzieUpdateInSettings(GlobalContext.getContext());
            }
        }
    });
    private String mLayout;
    private String mPackage;
    private INoteReader mPartialNodeReader = new NodeAttributeReader(new INoteReader() {
        public void parseNode(Node node) {
            String name = node.getNodeName();
            String value = node.getNodeValue();
            if ("layout".equalsIgnoreCase(name)) {
                HwThemeParser.this.mLayout = value;
            }
        }
    });
    private boolean mSlideInAamze;
    private String mStyle;
    private String mThemeId = BuildConfig.FLAVOR;
    private String mWallpaper;

    public static HwThemeParser getInstance() {
        if (sInstance == null) {
            sInstance = new HwThemeParser();
            sInstance.parseThemeFromXml();
        }
        return sInstance;
    }

    public boolean getSlideInAmazeFlag() {
        return this.mSlideInAamze;
    }

    public void parseThemeFromXml() {
        if (this.mDefaultTheme == null) {
            this.mDefaultTheme = ThemeCfg.getDefaultTheme();
        }
        if (isThemeFileChagned(this.mDefaultTheme)) {
            parseThemeFromXml(this.mDefaultTheme);
            HwLog.d("HwThemeParser", "Theme file's content has chagned. style " + this.mStyle + "; Wallpaper: " + this.mWallpaper + "; SlideInAamze: " + this.mSlideInAamze + "; EnableRotate " + this.mEnableRotate);
        }
    }

    private boolean isThemeFileChagned(String filePath) {
        long modifyTime = new File(filePath).lastModified();
        if (sLastModifyTime == modifyTime) {
            return false;
        }
        sLastModifyTime = modifyTime;
        return true;
    }

    private void parseThemeFromXml(String filePath) {
        cleanOptional();
        KeyguardCfg.init(GlobalContext.getContext());
        XmlUtils.parseXmlNode(filePath, "item", this.mFullNodeReader);
    }

    public String getStyle() {
        return this.mStyle;
    }

    public String getStyle(Context context) {
        if (context == null || !TextUtils.isEmpty(this.mStyle)) {
            return this.mStyle;
        }
        return context.getString(R$string.default_style);
    }

    public String getWallpager() {
        return this.mWallpaper;
    }

    public String getLayout(Context context) {
        String layout = getLayout();
        return TextUtils.isEmpty(layout) ? context.getString(R$string.default_layout) : layout;
    }

    public String getLayout() {
        if (TextUtils.isEmpty(this.mLayout)) {
            XmlUtils.parseXmlNode(ThemeCfg.getMagazineTheme(), "item", this.mPartialNodeReader);
        }
        return this.mLayout;
    }

    public String getEnableRotate() {
        return this.mEnableRotate;
    }

    public String getPackageName() {
        return this.mPackage;
    }

    public String getDynamicPath() {
        return this.mDynamicPath;
    }

    private void cleanOptional() {
        this.mPackage = null;
        this.mClass = null;
    }

    private HwThemeParser() {
    }

    public boolean isSupportOrientationByTheme() {
        String enablerotate = getEnableRotate();
        if (enablerotate == null || enablerotate.equals("false")) {
            return false;
        }
        return true;
    }
}
