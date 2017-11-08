package com.huawei.watermark.wmutil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import com.android.gallery3d.R;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.WMConfig;
import com.huawei.watermark.wmdata.WMFileProcessor;
import java.io.Closeable;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public class WMBaseUtil {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMBaseUtil.class.getSimpleName());
    private static int mLcdDpi;
    private static int mSystemDpi;
    private static ReflectClass properties;

    static {
        try {
            properties = new ReflectClass("android.os.SystemProperties", new Class[0]);
            mLcdDpi = ((Integer) properties.invokeS("getInt", "ro.sf.lcd_density", Integer.valueOf(0))).intValue();
            mSystemDpi = ((Integer) properties.invokeS("getInt", "persist.sys.dpi", Integer.valueOf(mLcdDpi))).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Initialize SystemProperties failed.");
        }
    }

    public static Bitmap convertViewToBitmap(View view) {
        Log.d(TAG, "convertViewToBitmap");
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    public static boolean containType(int value, int type) {
        if ((value & type) != 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int[] getWaterMarkAbsolutePosition(View childview) {
        if (childview == null || childview.getContext() == null) {
            return new int[4];
        }
        String tag = childview.getContext().getResources().getString(R.string.water_mark_mview_tag);
        if (WMStringUtil.isEmptyString(tag)) {
            return new int[4];
        }
        View watermarkview = null;
        while (true) {
            View tempview = (View) childview.getParent();
            if (tempview == null) {
                break;
            }
            try {
                Object tagobj = tempview.getTag();
                if (tagobj != null && (tagobj instanceof String) && tag.equals((String) tagobj)) {
                    break;
                }
                childview = tempview;
            } catch (Exception e) {
                WMLog.d("WMBaseUtil", "WMBaseUtil getWaterMarkAbsolutePosition e=" + e.toString());
            }
        }
        int[] location = new int[4];
        if (watermarkview == null) {
            return location;
        }
        watermarkview.getLocationInWindow(location);
        location[2] = watermarkview.getWidth();
        location[3] = watermarkview.getHeight();
        return location;
    }

    public static int dpToPixel(float dp, Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        return Math.round(metrics.density * dp);
    }

    @TargetApi(17)
    public static int[] getScreenPixel(Activity activity) {
        activity.getWindowManager().getDefaultDisplay().getRealSize(new Point());
        return new int[]{size.x, size.y};
    }

    public static int getScreenWidth(Activity activity) {
        Display d = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getRealSize(size);
        return Math.min(size.x, size.y);
    }

    public static boolean isSimplifiedOrTraditionalChineseLanguage(Locale locale) {
        if (Locale.CHINESE.getLanguage().equals(locale.getLanguage()) || Locale.CHINA.getLanguage().equals(locale.getLanguage()) || Locale.SIMPLIFIED_CHINESE.getLanguage().equals(locale.getLanguage()) || Locale.TAIWAN.getLanguage().equals(locale.getLanguage())) {
            return true;
        }
        return Locale.TRADITIONAL_CHINESE.getLanguage().equals(locale.getLanguage());
    }

    public static String getProductLocaleRegion() {
        return properties.invokeS("get", "ro.product.locale.language") + "_" + properties.invokeS("get", "ro.product.locale.region");
    }

    public static boolean isInside(int viewx, int viewy, int vieww, int viewh, int x, int y) {
        if (viewx >= x || viewx + vieww <= x || viewy >= y || viewy + viewh <= y) {
            return false;
        }
        return true;
    }

    public static boolean isChinaProductLocaleRegion() {
        return "zh_CN".equals(getProductLocaleRegion());
    }

    public static int getNowSupportLanguageType(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String region = locale.getCountry();
        boolean isChineseTraditional = region != null ? (region.toLowerCase(locale).endsWith("tw") || region.toLowerCase(locale).endsWith("hk")) ? true : region.toLowerCase(locale).endsWith("mo") : false;
        Log.d(TAG, "Is ChineseTraditional? = " + isChineseTraditional);
        if (language != null && language.toLowerCase(locale).endsWith(WMConfig.SUPPORTZH) && !isChineseTraditional) {
            return 0;
        }
        if (WMUIUtil.isLayoutDirectionRTL(context)) {
            return 2;
        }
        return 1;
    }

    public static boolean supportJELLYBEANMR1() {
        if (VERSION.SDK_INT >= 17) {
            return true;
        }
        return false;
    }

    public static boolean isHighResolutionTablet(Context context) {
        return WMResourceUtil.isTabletProduct(context) ? WMResourceUtil.isHighResTabletProduct(context) : false;
    }

    public static int reparamsMarginOfDPI(int bigDimens, int dimens, int smallDimens, Context context) {
        if (mSystemDpi == context.getResources().getInteger(R.integer.system_small_dpi)) {
            return smallDimens;
        }
        if (mSystemDpi == context.getResources().getInteger(R.integer.system_big_dpi)) {
            return bigDimens;
        }
        return dimens;
    }

    public static String getSupportLanguage(Context context, String path) {
        boolean z;
        if (WMStringUtil.isEmptyString(path)) {
            z = false;
        } else {
            z = true;
        }
        WMAssertUtil.Assert(z, String.format("wm path cannot be null，path： %s", new Object[]{path}));
        Closeable closeable = null;
        Closeable closeable2 = null;
        try {
            closeable = WMFileProcessor.getInstance().openZipInputStream(context, path);
            closeable2 = WMZipUtil.openZipEntryInputStream("wm.xml", closeable);
            if (closeable == null || closeable.available() > 15728640) {
                Log.i(TAG, "zis is too big, return.");
                return null;
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(closeable2, XmlUtils.INPUT_ENCODING);
            for (int evnType = parser.getEventType(); evnType != 1; evnType = parser.next()) {
                switch (evnType) {
                    case 2:
                        if (!parser.getName().equalsIgnoreCase("wmconfig")) {
                            break;
                        }
                        String res = parser.getAttributeValue(null, "supportlanguage");
                        WMFileUtil.closeSilently(closeable2);
                        WMFileUtil.closeSilently(closeable);
                        return res;
                    default:
                        break;
                }
            }
            WMFileUtil.closeSilently(closeable2);
            WMFileUtil.closeSilently(closeable);
            return null;
        } catch (Exception e) {
            WMLog.e(TAG, "parse xml got an exception", e);
        } finally {
            WMFileUtil.closeSilently(closeable2);
            WMFileUtil.closeSilently(closeable);
        }
    }

    public static boolean isWaterMarkCust() {
        return ((Boolean) properties.invokeS("getBoolean", "ro.config.cust_watermark", Boolean.valueOf(false))).booleanValue();
    }
}
