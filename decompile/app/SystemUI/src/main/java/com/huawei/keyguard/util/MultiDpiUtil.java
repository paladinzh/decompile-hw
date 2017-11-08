package com.huawei.keyguard.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Point;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.IWindowManager.Stub;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$xml;
import com.huawei.keyguard.DpiConfig;
import fyusion.vislib.BuildConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class MultiDpiUtil {
    private static int sCurrentDpi = -1;
    private static SparseArray<PadRes> sPadResMap = null;

    private static class PadRes {
        private int mLargeRes;
        private int mMediumRes;
        private int mSmallRes;

        public int getPadRes(int mode) {
            switch (mode) {
                case 1:
                    return this.mSmallRes;
                case 2:
                    return this.mMediumRes;
                case 3:
                    return this.mLargeRes;
                default:
                    return -1;
            }
        }

        public PadRes(int smallRes, int mediumRes, int largeRes) {
            this.mSmallRes = smallRes;
            this.mMediumRes = mediumRes;
            this.mLargeRes = largeRes;
        }
    }

    private static void preCheck(Context context) {
        synchronized (MultiDpiUtil.class) {
            if (sPadResMap != null) {
                return;
            }
            sPadResMap = new SparseArray();
            sPadResMap.put(R$dimen.music_lyric_display_position_X, new PadRes(R$dimen.music_lyric_display_position_X_small, R$dimen.music_lyric_display_position_X_medium, R$dimen.music_lyric_display_position_X_large));
            sCurrentDpi = getCurrentModel(context);
        }
    }

    public static void loadRes(Handler h, final Context context) {
        h.post(new Runnable() {
            public void run() {
                MultiDpiUtil.preCheck(context);
            }
        });
    }

    public static CharSequence[] getActualDpiArrayForDevice(Context context) {
        CharSequence[] dipValues = new CharSequence[]{"-1", "-1", "-1"};
        List<DpiConfig> dpiConfigs = getDpiArrayFromXml(R$xml.display_dpi_categories, context.getResources());
        if (dpiConfigs == null || dpiConfigs.size() < 1) {
            HwLog.d("MultiDpiUtil", "getActualDpiArrayForDevice()-->ERROR!, dpiConfigs is empty !!");
            return dipValues;
        }
        int curDpi = Integer.parseInt(SystemProperties.get("ro.sf.lcd_density", BuildConfig.FLAVOR));
        Point initialSize = getActualScreenInitialSize();
        int curWidth = initialSize.x;
        int cutHeight = initialSize.y;
        List<DpiConfig> dpiConfigsAdaptedGivenDpi = getAdaptedGivenDpiConfigs(dpiConfigs, curDpi);
        if (dpiConfigsAdaptedGivenDpi.size() == 1) {
            setDpiValues(dipValues, (DpiConfig) dpiConfigsAdaptedGivenDpi.get(0));
        } else if (dpiConfigsAdaptedGivenDpi.size() > 1) {
            List<DpiConfig> dpiConfigsAdaptedGivenWidth = getAdaptedGivenWidthConfigs(dpiConfigsAdaptedGivenDpi, curWidth);
            if (dpiConfigsAdaptedGivenWidth.size() == 1) {
                setDpiValues(dipValues, (DpiConfig) dpiConfigsAdaptedGivenWidth.get(0));
            } else if (dpiConfigsAdaptedGivenWidth.size() > 1) {
                int tempHeight = 0;
                int targetIndex = 0;
                int i = 0;
                while (i < dpiConfigsAdaptedGivenWidth.size()) {
                    if (((DpiConfig) dpiConfigsAdaptedGivenWidth.get(i)).getHeight() <= cutHeight && ((DpiConfig) dpiConfigsAdaptedGivenWidth.get(i)).getHeight() >= tempHeight) {
                        tempHeight = ((DpiConfig) dpiConfigsAdaptedGivenWidth.get(i)).getHeight();
                        targetIndex = i;
                    }
                    i++;
                }
                setDpiValues(dipValues, (DpiConfig) dpiConfigsAdaptedGivenWidth.get(targetIndex));
            } else {
                HwLog.e("MultiDpiUtil", "getActualDpiArrayForDevice()-->no appropriate width found, will return default value ");
            }
        } else {
            HwLog.e("MultiDpiUtil", "getActualDpiArrayForDevice()-->no appropriate dpi found, will return default value ");
        }
        return dipValues;
    }

    private static void setDpiValues(CharSequence[] dipValues, DpiConfig dc) {
        try {
            dipValues[0] = dc.getSmallDpi();
            dipValues[1] = dc.getMidDpi();
            dipValues[2] = dc.getLargeDpi();
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e("MultiDpiUtil", "setDpiValues()-->ArrayIndexOutOfBoundsException :" + e.toString());
        }
    }

    private static Point getActualScreenInitialSize() {
        Point initialSize = new Point();
        try {
            Stub.asInterface(ServiceManager.checkService("window")).getInitialDisplaySize(0, initialSize);
        } catch (RemoteException e) {
            HwLog.e("MultiDpiUtil", "getActualScreenWidth()-->RemoteException : " + e.toString());
        } catch (Exception e2) {
            HwLog.e("MultiDpiUtil", "getActualScreenWidth()-->Exception : " + e2.toString());
        }
        return initialSize;
    }

    private static List<DpiConfig> getDpiArrayFromXml(int resid, Resources resources) {
        if (resources == null) {
            HwLog.w("MultiDpiUtil", "getDpiArrayFromXml()-->resources is null.");
            return null;
        }
        DpiConfig currentDpiConfig;
        XmlResourceParser xmlResourceParser = null;
        List<DpiConfig> dpiConfigs = new ArrayList();
        try {
            xmlResourceParser = resources.getXml(resid);
            int type;
            do {
                type = xmlResourceParser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            String nodeName = xmlResourceParser.getName();
            String small = "-1";
            String mid = "-1";
            String large = "-1";
            if ("muldpi".equals(nodeName)) {
                int nodeType = xmlResourceParser.next();
                DpiConfig currentDpiConfig2 = null;
                while (nodeType != 1) {
                    switch (nodeType) {
                        case 0:
                            currentDpiConfig = currentDpiConfig2;
                            break;
                        case 2:
                            try {
                                String name = xmlResourceParser.getName();
                                if (!"dpi".equalsIgnoreCase(name)) {
                                    if (currentDpiConfig2 != null) {
                                        if (!"small".equalsIgnoreCase(name)) {
                                            if (!"mid".equalsIgnoreCase(name)) {
                                                if ("large".equalsIgnoreCase(name)) {
                                                    currentDpiConfig2.setLargeDpi(xmlResourceParser.getAttributeValue(null, "value"));
                                                    currentDpiConfig = currentDpiConfig2;
                                                    break;
                                                }
                                            }
                                            currentDpiConfig2.setMidDpi(xmlResourceParser.getAttributeValue(null, "value"));
                                            currentDpiConfig = currentDpiConfig2;
                                            break;
                                        }
                                        currentDpiConfig2.setSmallDpi(xmlResourceParser.getAttributeValue(null, "value"));
                                        currentDpiConfig = currentDpiConfig2;
                                        break;
                                    }
                                    currentDpiConfig = currentDpiConfig2;
                                    break;
                                }
                                currentDpiConfig = new DpiConfig();
                                try {
                                    currentDpiConfig.setNumber(Integer.parseInt(xmlResourceParser.getAttributeValue(null, "number")));
                                    currentDpiConfig.setDpiValue(Integer.parseInt(xmlResourceParser.getAttributeValue(null, "dpiValue")));
                                    currentDpiConfig.setWidth(Integer.parseInt(xmlResourceParser.getAttributeValue(null, "width")));
                                    currentDpiConfig.setHeight(Integer.parseInt(xmlResourceParser.getAttributeValue(null, "height")));
                                    break;
                                } catch (NumberFormatException e) {
                                    HwLog.e("MultiDpiUtil", "getDpiArrayFromXml()-->NumberFormatException e : " + e.toString());
                                    break;
                                }
                            } catch (XmlPullParserException e2) {
                                XmlPullParserException e3 = e2;
                                currentDpiConfig = currentDpiConfig2;
                                break;
                            } catch (IOException e4) {
                                IOException e5 = e4;
                                currentDpiConfig = currentDpiConfig2;
                                break;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                currentDpiConfig = currentDpiConfig2;
                                break;
                            }
                        case 3:
                            if ("dpi".equalsIgnoreCase(xmlResourceParser.getName()) && currentDpiConfig2 != null) {
                                HwLog.d("MultiDpiUtil", "getDpiArrayFromXml()--END_TAG ,add one currentDpiConfig");
                                dpiConfigs.add(currentDpiConfig2);
                                currentDpiConfig = null;
                                break;
                            }
                            currentDpiConfig = currentDpiConfig2;
                            break;
                            break;
                        default:
                            currentDpiConfig = currentDpiConfig2;
                            break;
                    }
                }
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                currentDpiConfig = currentDpiConfig2;
                return dpiConfigs;
            }
            throw new RuntimeException("XML document must start with <muldpi> tag; found" + nodeName + " at " + xmlResourceParser.getPositionDescription());
        } catch (XmlPullParserException e6) {
            e3 = e6;
        } catch (IOException e7) {
            e5 = e7;
        }
        try {
            HwLog.e("MultiDpiUtil", "getDpiArrayFromXml()-->XmlPullParserException e : " + e3.toString());
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return dpiConfigs;
        } catch (Throwable th3) {
            th2 = th3;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            throw th2;
        }
        nodeType = xmlResourceParser.next();
        currentDpiConfig2 = currentDpiConfig;
        HwLog.e("MultiDpiUtil", "getDpiArrayFromXml()-->IOException e : " + e5.toString());
        if (xmlResourceParser != null) {
            xmlResourceParser.close();
        }
        return dpiConfigs;
    }

    private static List<DpiConfig> getAdaptedGivenDpiConfigs(List<DpiConfig> dpiConfigs, int curDpi) {
        List<DpiConfig> dpiConfigsAdaptedGivenDpi = new ArrayList();
        int tempDpiValue = Integer.MAX_VALUE;
        for (DpiConfig dc : dpiConfigs) {
            if (dc.getDpiValue() >= curDpi && dc.getDpiValue() <= tempDpiValue) {
                tempDpiValue = dc.getDpiValue();
            }
        }
        HwLog.d("MultiDpiUtil", "getAdaptedGivenDpiConfigs()-->tempDpiValue final = " + tempDpiValue);
        for (DpiConfig dc2 : dpiConfigs) {
            if (dc2.getDpiValue() == tempDpiValue) {
                dpiConfigsAdaptedGivenDpi.add(dc2);
            }
        }
        return dpiConfigsAdaptedGivenDpi;
    }

    private static List<DpiConfig> getAdaptedGivenWidthConfigs(List<DpiConfig> dpiConfigsAdaptedGivenDpi, int curWidth) {
        List<DpiConfig> dpiConfigsAdaptedGivenWidth = new ArrayList();
        int tempWidth = 0;
        for (DpiConfig dc : dpiConfigsAdaptedGivenDpi) {
            if (dc.getWidth() <= curWidth && dc.getWidth() >= tempWidth) {
                tempWidth = dc.getWidth();
            }
        }
        HwLog.d("MultiDpiUtil", "getAdaptedGivenWidthConfigs()-->tempWidth final = " + tempWidth);
        for (DpiConfig dc2 : dpiConfigsAdaptedGivenDpi) {
            if (dc2.getWidth() == tempWidth) {
                dpiConfigsAdaptedGivenWidth.add(dc2);
            }
        }
        HwLog.d("MultiDpiUtil", "getAdaptedGivenWidthConfigs()-->dpiConfigsAdaptedGivenWidth.size() = " + dpiConfigsAdaptedGivenWidth.size());
        return dpiConfigsAdaptedGivenWidth;
    }

    private static int getCurrentModel(Context context) {
        int[] dipList = new int[]{1, 2, 3};
        String currentModeEx = SystemProperties.get("persist.sys.dpi", BuildConfig.FLAVOR);
        if (TextUtils.isEmpty(currentModeEx)) {
            return 0;
        }
        CharSequence[] dipValues = getActualDpiArrayForDevice(context);
        int length = dipValues.length;
        for (int i = 0; i < length; i++) {
            if (currentModeEx.equals(dipValues[i])) {
                return dipList[i];
            }
        }
        return 0;
    }

    public static int getResId(Context context, int defRes) {
        preCheck(context);
        synchronized (MultiDpiUtil.class) {
            PadRes currentRes = (PadRes) sPadResMap.get(defRes);
            int retDimen = currentRes == null ? -1 : currentRes.getPadRes(sCurrentDpi);
            if (retDimen != -1) {
                defRes = retDimen;
            }
        }
        return defRes;
    }
}
