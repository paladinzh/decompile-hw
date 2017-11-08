package com.android.gallery3d.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.SystemProperties;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.MultiWindowStatusHolder.IMultiWindowModeChangeListener;
import com.huawei.gallery.displayengine.DisplayEngine;
import com.huawei.gallery.displayengine.DisplayEngineFactory;
import com.huawei.gallery.displayengine.ScreenNailAceDisplayEngine;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEngine;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.gallery.displayengine.ScreenNailScaleDisplayEngine;
import com.huawei.gallery.sceneDetection.SceneDetectionInfoParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class DisplayEngineUtils {
    private static int sActivityRef = 0;
    private static boolean sCapDoAce;
    private static boolean sCapDoAcm;
    private static boolean sCapDoGmp;
    private static boolean sCapDoHardwareAce;
    private static boolean sCapDoHardwareSharpness;
    private static boolean sCapDoSharpness;
    private static boolean sCapDoSr;
    private static boolean sCapWideColorGamut;
    private static String sCurrentImageFilePath = null;
    private static String sCurrentXmlFilePath = null;
    private static boolean sDisplayEngineEnable = false;
    private static Method sGetDisplayEffectSupportedMethod = null;
    private static boolean sHardwareCapInit = false;
    private static int sHardwareCaps = 0;
    private static HardwareDisplayEngineProcessThread sHwProcThread = null;
    private static DisplayEngineInitThread sInitThread = null;
    private static boolean sInited = false;
    private static boolean sIsInMultiWindowMode = false;
    private static IMultiWindowModeChangeListener sMultiWindowModeChangeListener = new IMultiWindowModeChangeListener() {
        public void multiWindowModeChangeCallback(boolean isInMultiWindowMode) {
            synchronized (DisplayEngineUtils.sMultiWindowModeSyncObject) {
                DisplayEngineUtils.sIsInMultiWindowMode = isInMultiWindowMode;
                if (DisplayEngineUtils.sIsInMultiWindowMode && DisplayEngineUtils.sActivityRef > 0) {
                    DisplayEngineUtils.updateEffectActivityOnPause();
                }
            }
        }
    };
    private static final Object sMultiWindowModeSyncObject = new Object();
    private static Object sObjectDisplayEffect = null;
    private static Method sSetDisplayEffectParamMethod = null;

    public static class DisplayEngineCapParams {
        public boolean bCapDoAce;
        public boolean bCapDoAcm;
        public boolean bCapDoGmp;
        public boolean bCapDoHardwareAce;
        public boolean bCapDoHardwareSharpness;
        public boolean bCapDoSharpness;
        public boolean bCapDoSr;
        public boolean bCapWideColorGamut;
    }

    private static class DisplayEngineInitThread extends Thread {
        private Context mContext;

        public DisplayEngineInitThread(Context context) {
            this.mContext = context;
        }

        public void run() {
            boolean -wrap4;
            long start = System.currentTimeMillis();
            if (DisplayEngineUtils.displayEngineProbe() && DisplayEngineUtils.displaySizeDetect() && DisplayEngineUtils.heapDetect() && DisplayEngineUtils.initXmlFilePath(this.mContext) && DisplayEngineUtils.initAlgoCapCfg()) {
                -wrap4 = DisplayEngineUtils.initHardwareCap();
            } else {
                -wrap4 = false;
            }
            DisplayEngineUtils.sDisplayEngineEnable = -wrap4;
            MultiWindowStatusHolder.registerMultiWindowModeChangeListener(DisplayEngineUtils.sMultiWindowModeChangeListener, true);
            GalleryLog.d("DisplayEngineUtils", "DisplayEngineInit end, sDisplayEngineEnable : " + DisplayEngineUtils.sDisplayEngineEnable);
            GalleryLog.d("DisplayEngineUtils", "DisplayEngineInitThread run cost time : " + (System.currentTimeMillis() - start));
        }
    }

    public enum Display_HardwareType {
        HIACE(1),
        CSC(2),
        BITEXTEND(4),
        DITHER(8),
        ARSR1P(16),
        SBL(32),
        ACM(64),
        IGM(128),
        XCC(256),
        GMP(512),
        GAMMA(1024);
        
        private int mType;

        private Display_HardwareType(int type) {
            this.mType = type;
        }

        public int getType() {
            return this.mType;
        }

        public String toString() {
            return String.valueOf(this.mType);
        }
    }

    public enum Display_ImageType {
        YUV400(0),
        YUV420(1),
        YUV422(2),
        YUV444(3),
        RGB(4),
        RGBA(5),
        ImageTypeEnd(6);
        
        private int mType;

        private Display_ImageType(int type) {
            this.mType = type;
        }

        public int getType() {
            return this.mType;
        }

        public String toString() {
            return String.valueOf(this.mType);
        }
    }

    public enum Display_SceneType {
        GALLERY_UI(0),
        GALLERY_IMAGE(1),
        GALLERY_EXIT(2),
        SceneTypeEnd(3);
        
        private int mType;

        private Display_SceneType(int type) {
            this.mType = type;
        }

        public int getType() {
            return this.mType;
        }

        public String toString() {
            return String.valueOf(this.mType);
        }
    }

    public static class ExifInfo {
        public int colorSpace;
        public int iso;
    }

    private static class HardwareDisplayEngineProcessThread extends Thread {
        private static final /* synthetic */ int[] -com-android-gallery3d-util-DisplayEngineUtils$Display_SceneTypeSwitchesValues = null;
        private String mImageFilePath;
        private Display_SceneType mSceneType;
        private int mSharpnessLevel;

        private static /* synthetic */ int[] -getcom-android-gallery3d-util-DisplayEngineUtils$Display_SceneTypeSwitchesValues() {
            if (-com-android-gallery3d-util-DisplayEngineUtils$Display_SceneTypeSwitchesValues != null) {
                return -com-android-gallery3d-util-DisplayEngineUtils$Display_SceneTypeSwitchesValues;
            }
            int[] iArr = new int[Display_SceneType.values().length];
            try {
                iArr[Display_SceneType.GALLERY_EXIT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Display_SceneType.GALLERY_IMAGE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Display_SceneType.GALLERY_UI.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Display_SceneType.SceneTypeEnd.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -com-android-gallery3d-util-DisplayEngineUtils$Display_SceneTypeSwitchesValues = iArr;
            return iArr;
        }

        public HardwareDisplayEngineProcessThread(Display_SceneType sceneType, String imageFilePath, int sharpnessLevel) {
            this.mSceneType = sceneType;
            this.mImageFilePath = imageFilePath;
            this.mSharpnessLevel = sharpnessLevel;
        }

        public void run() {
            int hwAlgoCtrlBits;
            long start = System.currentTimeMillis();
            switch (-getcom-android-gallery3d-util-DisplayEngineUtils$Display_SceneTypeSwitchesValues()[this.mSceneType.ordinal()]) {
                case 1:
                    hwAlgoCtrlBits = Display_SceneType.GALLERY_EXIT.getType() << 16;
                    break;
                case 2:
                    hwAlgoCtrlBits = (((DisplayEngineUtils.sHardwareCaps & Display_HardwareType.HIACE.getType()) & (DisplayEngineUtils.getCapDoHardwareAce() ? -1 : 0)) | ((DisplayEngineUtils.getCapDoHardwareSharpness() ? -1 : 0) & (DisplayEngineUtils.sHardwareCaps & Display_HardwareType.ARSR1P.getType()))) | (Display_SceneType.GALLERY_IMAGE.getType() << 16);
                    break;
                case 3:
                    hwAlgoCtrlBits = (((DisplayEngineUtils.sHardwareCaps & Display_HardwareType.HIACE.getType()) & (DisplayEngineUtils.getCapDoHardwareAce() ? -1 : 0)) | ((DisplayEngineUtils.getCapDoHardwareSharpness() ? -1 : 0) & (DisplayEngineUtils.sHardwareCaps & Display_HardwareType.ARSR1P.getType()))) | (Display_SceneType.GALLERY_UI.getType() << 16);
                    break;
                default:
                    hwAlgoCtrlBits = 0;
                    break;
            }
            GalleryLog.d("DisplayEngineUtils", String.format("display_effect hwAlgoCtrlBits=0x%x, mSharpnessLevel=%d", new Object[]{Integer.valueOf(hwAlgoCtrlBits), Integer.valueOf(this.mSharpnessLevel)}));
            byte[] sceneInfo = SceneDetectionInfoParser.getSceneInfo(hwAlgoCtrlBits, this.mImageFilePath, this.mSharpnessLevel);
            if (sceneInfo.length == 0) {
                GalleryLog.w("DisplayEngineUtils", "getSceneInfo result is null, do nothing and return.");
                return;
            }
            try {
                int[] intSendBuffer = new int[((sceneInfo.length / 4) + (sceneInfo.length % 4 > 0 ? 1 : 0))];
                Arrays.fill(intSendBuffer, 0);
                int intSendBufferSize = intSendBuffer.length * 4;
                int sceneInfoOffset = 0;
                int intSendBufferOffset = 0;
                while (intSendBufferOffset < sceneInfo.length / 4) {
                    intSendBuffer[intSendBufferOffset] = (((sceneInfo[sceneInfoOffset] & 255) + ((sceneInfo[sceneInfoOffset + 1] & 255) << 8)) + ((sceneInfo[sceneInfoOffset + 2] & 255) << 16)) + ((sceneInfo[sceneInfoOffset + 3] & 255) << 24);
                    sceneInfoOffset += 4;
                    intSendBufferOffset++;
                }
                int leftOffset = sceneInfo.length - (sceneInfo.length % 4);
                int leftcount = 0;
                while (leftOffset < sceneInfo.length) {
                    intSendBuffer[intSendBufferOffset] = intSendBuffer[intSendBufferOffset] + ((sceneInfo[leftOffset] & 255) << (leftcount * 8));
                    leftOffset++;
                    leftcount++;
                }
                GalleryLog.d("DisplayEngineUtils", "display_effect send sceneInfo to display hardware. sceneInfo.length=" + sceneInfo.length + ", intSendBuffer.length=" + intSendBuffer.length);
                DisplayEngineUtils.sSetDisplayEffectParamMethod.invoke(DisplayEngineUtils.sObjectDisplayEffect, new Object[]{Integer.valueOf(0), intSendBuffer, Integer.valueOf(intSendBufferSize)});
                if ("true".equals(SystemProperties.get("hw.display_effect_debug", "false"))) {
                    if (this.mImageFilePath != null) {
                        GalleryLog.d("DisplayEngineUtils", "display_effect current image file is: " + this.mImageFilePath);
                    }
                    for (int i = 0; i < intSendBuffer.length; i++) {
                        GalleryLog.d("DisplayEngineUtils", String.format("display_effect intSendBuffer is int[%d] : [0x%x]", new Object[]{Integer.valueOf(i), Integer.valueOf(intSendBuffer[i])}));
                    }
                }
            } catch (InvocationTargetException e) {
                GalleryLog.w("DisplayEngineUtils", "HardwareDisplayEngineProcessThread InvocationTargetException:" + e);
            } catch (IllegalAccessException e2) {
                GalleryLog.w("DisplayEngineUtils", "HardwareDisplayEngineProcessThread IllegalAccessException:" + e2);
            }
            GalleryLog.d("DisplayEngineUtils", "display_effect HardwareDisplayEngineProcessThread run cost time : " + (System.currentTimeMillis() - start));
        }
    }

    public static native int displayEngineACEProcess(Bitmap bitmap, Bitmap bitmap2, int i, long j, int i2, int i3, int i4, int i5, int i6, boolean z, long j2);

    public static native int displayEngineCommonCreate(String str, int i, int i2, int i3, DisplayEngine displayEngine);

    public static native int displayEngineCommonDestroy(long j);

    public static native int displayEngineCommonProcess(Bitmap bitmap, int i, int i2, long j);

    public static native int displayEngineCreate(String str, int i, int i2, float f, int i3, int i4, DisplayEngine displayEngine);

    public static native int displayEngineDestroy(long j);

    public static native int displayEngineGetAlgoEnableState(String str, DisplayEngineCapParams displayEngineCapParams);

    public static native int displayEngineGetCommonSharpnessLevel(long j);

    public static native int displayEngineSRProcess(Bitmap bitmap, Bitmap bitmap2, int i, long j, float f, float f2, float f3, int i2, int i3, int i4, long j2);

    private static native String getApparatusModel(int i);

    private static native String getVersionAndTime(int i);

    private static boolean displayEngineProbe() {
        try {
            System.loadLibrary("jnihw_display_engine");
            return true;
        } catch (UnsatisfiedLinkError e) {
            GalleryLog.w("DisplayEngineUtils", "jnihw_display_engine so load fail.");
            return false;
        }
    }

    public static String getXmlFilePath() {
        return sCurrentXmlFilePath;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void init(Context context) {
        synchronized (DisplayEngineUtils.class) {
            if (sInited) {
                return;
            }
            sInited = true;
            GalleryLog.d("DisplayEngineUtils", "init");
            if (sInitThread == null) {
                sInitThread = new DisplayEngineInitThread(context);
                sInitThread.start();
            }
        }
    }

    public static synchronized void waitForInit() {
        synchronized (DisplayEngineUtils.class) {
            if (sInitThread == null) {
                return;
            }
            GalleryLog.d("DisplayEngineUtils", "wait for init thread ...");
            try {
                sInitThread.join();
            } catch (InterruptedException e) {
            }
            sInitThread = null;
        }
    }

    private static ArrayList<File> getCfgFileList(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> sFilePolicyClazz = Class.forName("com.huawei.cust.HwCfgFilePolicy");
        return (ArrayList) sFilePolicyClazz.getMethod("getCfgFileList", new Class[]{String.class, Integer.TYPE}).invoke(sFilePolicyClazz, new Object[]{fileName, Integer.valueOf(type)});
    }

    private static boolean initXmlFilePath(Context context) {
        String str = null;
        String productXmlFileName = getProductXmlFileName();
        String defaultXmlFileDir = context.getFilesDir().getAbsolutePath() + "/display_engine/";
        String assetsXmlFileDir = "display_engine/";
        GalleryLog.d("DisplayEngineUtils", "productXmlFileName = " + productXmlFileName + ", defaultXmlFileDir = " + defaultXmlFileDir);
        if (productXmlFileName != null) {
            try {
                for (File xmlFile : getCfgFileList("xml/gallery/display_engine/" + productXmlFileName, 0)) {
                    if (xmlFile != null) {
                        str = xmlFile.getCanonicalPath();
                        GalleryLog.d("DisplayEngineUtils", "xmlFile by XML_CFG_PATH = " + xmlFile.getCanonicalPath());
                        break;
                    }
                }
            } catch (Exception e) {
                GalleryLog.w("DisplayEngineUtils", "Exception HwCfgFilePolicy.getCfgFileList is not find.");
            } catch (NoClassDefFoundError e2) {
                GalleryLog.w("DisplayEngineUtils", "NoClassDefFoundError HwCfgFilePolicy.getCfgFileList is not find.");
            }
        }
        if (str == null) {
            if (copyFileFromAssets(context, assetsXmlFileDir + "display.xml", defaultXmlFileDir + "display.xml")) {
                str = defaultXmlFileDir + "display.xml";
            } else {
                GalleryLog.e("DisplayEngineUtils", "Error: can't find the correct config xml!");
            }
        }
        if (str == null) {
            return false;
        }
        sCurrentXmlFilePath = str;
        GalleryLog.d("DisplayEngineUtils", "sCurrentXmlFilePath = " + sCurrentXmlFilePath);
        return true;
    }

    private static boolean copyFileFromAssets(Context context, String assetsFileName, String targetFilePath) {
        IOException e;
        Throwable th;
        if (assetsFileName == null || targetFilePath == null) {
            return false;
        }
        Closeable in = null;
        Closeable closeable = null;
        try {
            Closeable in2 = new BufferedInputStream(context.getAssets().open(assetsFileName));
            try {
                File target = new File(targetFilePath);
                if (target.getParentFile().exists() || target.getParentFile().mkdirs()) {
                    Closeable out = new BufferedOutputStream(new FileOutputStream(target));
                    try {
                        byte[] buffer = new byte[FragmentTransaction.TRANSIT_EXIT_MASK];
                        for (int length = in2.read(buffer); length > 0; length = in2.read(buffer)) {
                            out.write(buffer, 0, length);
                        }
                        GalleryLog.d("DisplayEngineUtils", "copyFileFromAssets from " + assetsFileName + " to " + targetFilePath);
                        Utils.closeSilently(in2);
                        Utils.closeSilently(out);
                        return true;
                    } catch (IOException e2) {
                        e = e2;
                        closeable = out;
                        in = in2;
                        try {
                            GalleryLog.w("DisplayEngineUtils", "Error: IOException, copyFileFromAssets from " + assetsFileName + " to " + targetFilePath + ". " + e.getMessage());
                            Utils.closeSilently(in);
                            Utils.closeSilently(closeable);
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            Utils.closeSilently(in);
                            Utils.closeSilently(closeable);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        closeable = out;
                        in = in2;
                        Utils.closeSilently(in);
                        Utils.closeSilently(closeable);
                        throw th;
                    }
                }
                GalleryLog.e("DisplayEngineUtils", "copyFileFromAssets mkdirs failed!");
                Utils.closeSilently(in2);
                Utils.closeSilently(null);
                return false;
            } catch (IOException e3) {
                e = e3;
                in = in2;
                GalleryLog.w("DisplayEngineUtils", "Error: IOException, copyFileFromAssets from " + assetsFileName + " to " + targetFilePath + ". " + e.getMessage());
                Utils.closeSilently(in);
                Utils.closeSilently(closeable);
                return false;
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                Utils.closeSilently(in);
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            GalleryLog.w("DisplayEngineUtils", "Error: IOException, copyFileFromAssets from " + assetsFileName + " to " + targetFilePath + ". " + e.getMessage());
            Utils.closeSilently(in);
            Utils.closeSilently(closeable);
            return false;
        }
    }

    private static String getProductXmlFileName() {
        String hardwareVersion = null;
        String lcd_model = null;
        String hardwareVersionString = SystemProperties.get("ro.confg.hw_hardwareversion", "");
        if (hardwareVersionString.isEmpty()) {
            hardwareVersionString = getVersionAndTime(4);
        }
        if (hardwareVersionString != null) {
            hardwareVersion = hardwareVersionString.trim();
        }
        GalleryLog.i("DisplayEngineUtils", "hardwareVersion= " + hardwareVersion);
        String lcdModelString = getApparatusModel(6);
        if (lcdModelString != null) {
            lcd_model = lcdModelString.trim();
        }
        GalleryLog.i("DisplayEngineUtils", "lcd_mode= " + lcd_model);
        if (hardwareVersion == null || lcd_model == null) {
            return null;
        }
        char[] lcdArray = lcd_model.toCharArray();
        for (int i = 0; i < lcd_model.length(); i++) {
            char c = lcdArray[i];
            boolean isLegalChar = ((c < 'A' || c > 'Z') && ((c < 'a' || c > 'z') && !((c >= '0' && c <= '9') || c == '_' || c == '-'))) ? c == '.' : true;
            if (!isLegalChar) {
                lcdArray[i] = '_';
            }
        }
        String targetXml = hardwareVersion.split("_")[0] + "-" + String.copyValueOf(lcdArray) + ".xml";
        GalleryLog.i("DisplayEngineUtils", "detected target xml is " + targetXml);
        return targetXml;
    }

    public static boolean isDisplayEngineEnable() {
        boolean z = false;
        synchronized (sMultiWindowModeSyncObject) {
            if (sDisplayEngineEnable && !sIsInMultiWindowMode) {
                z = true;
            }
        }
        return z;
    }

    public static boolean isOptimizationEnable() {
        return isDisplayEngineEnable();
    }

    private static boolean displaySizeDetect() {
        int heightPixels = GalleryUtils.getHeightPixels();
        if (GalleryUtils.getWidthPixels() < 1080 || heightPixels < 1920) {
            GalleryLog.w("DisplayEngineUtils", String.format("display size[w:%d,h:%d] is < min limit[w:%d,h:%d], displaySizeDetect fail.", new Object[]{Integer.valueOf(widthPixels), Integer.valueOf(heightPixels), Integer.valueOf(1080), Integer.valueOf(1920)}));
            return false;
        }
        GalleryLog.d("DisplayEngineUtils", String.format("display size[w:%d,h:%d] is >= min limit[w:%d,h:%d], displaySizeDetect OK.", new Object[]{Integer.valueOf(widthPixels), Integer.valueOf(heightPixels), Integer.valueOf(1080), Integer.valueOf(1920)}));
        return true;
    }

    private static boolean heapDetect() {
        String heapProperty = "dalvik.vm.heapsize";
        String vmHeapSize = SystemProperties.get(heapProperty, "16m");
        if (vmHeapSize == null || !(vmHeapSize.endsWith("m") || vmHeapSize.endsWith("M"))) {
            GalleryLog.w("DisplayEngineUtils", "Property : " + heapProperty + " = " + vmHeapSize + " can not be parsed, heapDetect fail.");
        } else {
            String strTmp = vmHeapSize.substring(0, vmHeapSize.length() - 1);
            if (!strTmp.equals("")) {
                for (char c : strTmp.toCharArray()) {
                    if (c < '0' || c > '9') {
                        GalleryLog.w("DisplayEngineUtils", "Property : " + heapProperty + " = " + vmHeapSize + " is parsed error, heapDetect fail.");
                        return false;
                    }
                }
                int size = Integer.valueOf(strTmp).intValue();
                if (size >= 256) {
                    GalleryLog.d("DisplayEngineUtils", "heap size : " + size + " is >= min size : " + 256 + ", heapDetect OK.");
                    return true;
                }
                GalleryLog.w("DisplayEngineUtils", "heap size : " + size + " is smaller than min size:+" + 256);
            }
        }
        return false;
    }

    public static boolean isWideColorGamut(int colorSpace) {
        return ((short) colorSpace) == (short) -1;
    }

    public static ExifInfo getInfoFromEXIF(MediaItem mediaItem) {
        ExifInfo exifInfo = new ExifInfo();
        if (mediaItem != null) {
            TraceController.traceBegin("DisplayEngineUtils.getInfoFromEXIF");
            MediaDetails details = mediaItem.getDetails();
            if (details != null) {
                Object isoObject = details.getDetail(102);
                Object colorSpaceObject = details.getDetail(113);
                if (isoObject instanceof String) {
                    try {
                        exifInfo.iso = Integer.parseInt((String) isoObject);
                    } catch (NumberFormatException e) {
                        GalleryLog.e("DisplayEngineUtils", "Invalid iso value.");
                    }
                }
                if (colorSpaceObject instanceof String) {
                    try {
                        exifInfo.colorSpace = Integer.parseInt((String) colorSpaceObject);
                    } catch (NumberFormatException e2) {
                        GalleryLog.e("DisplayEngineUtils", "Invalid colorspace value.");
                    }
                }
            }
            TraceController.traceEnd();
        }
        GalleryLog.d("DisplayEngineUtils", String.format("getInfoFromEXIF colorSpace=%d iso=%d", new Object[]{Integer.valueOf(exifInfo.colorSpace), Integer.valueOf(exifInfo.iso)}));
        return exifInfo;
    }

    public static ScreenNailCommonDisplayEngine obtainScreenNailCommon(Bitmap screenNail, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool, ExifInfo exifInfo) {
        if (screenNail == null) {
            return null;
        }
        boolean notFound;
        ScreenNailCommonDisplayEngine commonDisplayEngine = null;
        if (!(mediaItem == null || displayEnginePool == null)) {
            commonDisplayEngine = displayEnginePool.get(mediaItem);
        }
        GalleryLog.printDFXLog("DisplayEngineUtils");
        if (commonDisplayEngine == null) {
            notFound = true;
        } else {
            notFound = false;
        }
        if (notFound) {
            commonDisplayEngine = (ScreenNailCommonDisplayEngine) DisplayEngineFactory.buildDisplayEngine(screenNail.getWidth(), screenNail.getHeight(), 0, 0);
            if (commonDisplayEngine == null) {
                return null;
            }
            if (exifInfo == null) {
                exifInfo = getInfoFromEXIF(mediaItem);
            }
            if (!commonDisplayEngine.extractCommonInfoFromScreenNail(screenNail, exifInfo.iso, exifInfo.colorSpace)) {
                return null;
            }
        }
        return commonDisplayEngine;
    }

    public static ScreenNailCommonDisplayEngine obtainScreenNailCommon(Bitmap screenNail, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool) {
        return obtainScreenNailCommon(screenNail, mediaItem, displayEnginePool, null);
    }

    public static Bitmap processScreenNailACE(Bitmap screenNail, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool, int nailType, ExifInfo exifInfo) {
        if (screenNail == null) {
            return null;
        }
        boolean notFound;
        int displayEngType;
        GalleryLog.printDFXLog("DisplayEngineUtils");
        DisplayEngine commonDisplayEngine = null;
        if (!(mediaItem == null || displayEnginePool == null)) {
            commonDisplayEngine = displayEnginePool.get(mediaItem);
        }
        if (commonDisplayEngine == null) {
            notFound = true;
        } else {
            notFound = false;
        }
        if (notFound) {
            commonDisplayEngine = (ScreenNailCommonDisplayEngine) DisplayEngineFactory.buildDisplayEngine(screenNail.getWidth(), screenNail.getHeight(), 0, 0);
            if (commonDisplayEngine == null) {
                return screenNail;
            }
            if (exifInfo == null) {
                exifInfo = getInfoFromEXIF(mediaItem);
            }
            if (!commonDisplayEngine.extractCommonInfoFromScreenNail(screenNail, exifInfo.iso, exifInfo.colorSpace)) {
                return screenNail;
            }
        }
        switch (nailType) {
            case 1:
                if (!getCapWideColorGamut() || exifInfo == null || !isWideColorGamut(exifInfo.colorSpace)) {
                    displayEngType = 2;
                    break;
                }
                displayEngType = 7;
                break;
            case 2:
                displayEngType = 3;
                break;
            default:
                displayEngType = 1;
                break;
        }
        ScreenNailAceDisplayEngine aceDisplayEngine = (ScreenNailAceDisplayEngine) DisplayEngineFactory.buildDisplayEngine(screenNail.getWidth(), screenNail.getHeight(), 0, displayEngType);
        if (aceDisplayEngine != null) {
            Bitmap aceNailBitmap = null;
            BitmapPool bitmapPool = MediaItem.getThumbPool();
            if (bitmapPool != null) {
                aceNailBitmap = bitmapPool.getBitmap(screenNail.getWidth(), screenNail.getHeight());
            }
            if (aceNailBitmap == null) {
                aceNailBitmap = Bitmap.createBitmap(screenNail.getWidth(), screenNail.getHeight(), Config.ARGB_8888);
            }
            if (aceNailBitmap != null) {
                if (!aceDisplayEngine.process(screenNail, aceNailBitmap, screenNail.getWidth(), screenNail.getHeight(), commonDisplayEngine)) {
                    GalleryLog.e("DisplayEngineUtils", "aceScreenNailProcess failed");
                    recycleBitmap(bitmapPool, aceNailBitmap);
                    aceNailBitmap = null;
                }
            }
            aceDisplayEngine.destroy();
            if (aceNailBitmap != null) {
                recycleBitmap(bitmapPool, screenNail);
                screenNail = aceNailBitmap;
            }
        }
        if (mediaItem == null || displayEnginePool == null) {
            commonDisplayEngine.destroy();
        } else if (notFound) {
            displayEnginePool.add(mediaItem, commonDisplayEngine);
        }
        return screenNail;
    }

    public static Bitmap processScreenNailACE(Bitmap screenNail, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool) {
        return processScreenNailACE(screenNail, mediaItem, displayEnginePool, 0, null);
    }

    public static Bitmap processScreenNailACE(Bitmap screenNail, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool, int nailType) {
        return processScreenNailACE(screenNail, mediaItem, displayEnginePool, nailType, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap processScreenNailScale(Bitmap screenNail, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool, int fullImageWidth, int fullImageHeight, int level, float scaleRatio, ScreenNailScaleDisplayEngine snScaleDisplayEngine) {
        if (screenNail == null) {
            return null;
        }
        if (snScaleDisplayEngine == null) {
            return screenNail;
        }
        GalleryLog.d("DisplayEngineUtils", "processScreenNailScale , screenNail:[w=" + screenNail.getWidth() + ",h=" + screenNail.getHeight() + "],g:[gx=" + GalleryUtils.getWidthPixels() + ",gy=" + GalleryUtils.getHeightPixels() + "], fullImageWidth=" + fullImageWidth + ", fullImageHeight=" + fullImageHeight + ",level=" + level + ",scaleRatio=" + scaleRatio);
        DisplayEngine commonDisplayEngine = null;
        if (!(mediaItem == null || displayEnginePool == null)) {
            commonDisplayEngine = displayEnginePool.get(mediaItem);
        }
        boolean notFound = commonDisplayEngine == null;
        if (notFound) {
            commonDisplayEngine = (ScreenNailCommonDisplayEngine) DisplayEngineFactory.buildDisplayEngine(screenNail.getWidth(), screenNail.getHeight(), 0, 0);
        }
        ExifInfo exifInfo = getInfoFromEXIF(mediaItem);
        if (commonDisplayEngine != null) {
            if (notFound) {
            }
            Bitmap bitmap = null;
            BitmapPool bitmapPool = MediaItem.getThumbPool();
            int dstBitmapWidth = (int) Math.floor((double) (((float) screenNail.getWidth()) * scaleRatio));
            int dstBitmapHeight = (int) Math.floor((double) (((float) screenNail.getHeight()) * scaleRatio));
            GalleryLog.d("DisplayEngineUtils", "processScreenNailScale dstBitmapWidth=" + dstBitmapWidth + ",dstBitmapHeight=" + dstBitmapHeight);
            if (dstBitmapWidth <= 0 || dstBitmapHeight <= 0) {
                if (notFound) {
                    commonDisplayEngine.destroy();
                }
                return screenNail;
            }
            if (bitmapPool != null) {
                bitmap = bitmapPool.getBitmap(dstBitmapWidth, dstBitmapHeight);
            }
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(dstBitmapWidth, dstBitmapHeight, Config.ARGB_8888);
            }
            if (bitmap != null) {
                boolean scaleResult;
                synchronized (snScaleDisplayEngine) {
                    TraceController.traceBegin("srProcess");
                    scaleResult = snScaleDisplayEngine.process(screenNail, bitmap, scaleRatio, fullImageWidth, fullImageHeight, level, commonDisplayEngine);
                    TraceController.traceEnd();
                }
                if (!scaleResult) {
                    GalleryLog.e("DisplayEngineUtils", "scaleDisplayEngine.process failed");
                    recycleBitmap(bitmapPool, bitmap);
                    bitmap = null;
                }
            }
            if (bitmap != null) {
                recycleBitmap(bitmapPool, screenNail);
                screenNail = bitmap;
            }
            if (mediaItem == null || displayEnginePool == null) {
                commonDisplayEngine.destroy();
            } else if (notFound) {
                displayEnginePool.add(mediaItem, commonDisplayEngine);
            }
            return screenNail;
        }
        return screenNail;
    }

    public static synchronized boolean processWithHardwareDisplayEngine(Display_SceneType sceneType, MediaItem item, ScreenNailCommonDisplayEngine commonDisplayEngine) {
        synchronized (DisplayEngineUtils.class) {
            long start = System.currentTimeMillis();
            if (sceneType.getType() >= Display_SceneType.SceneTypeEnd.getType()) {
                GalleryLog.w("DisplayEngineUtils", "processWithHardwareDisplayEngine sceneType is : " + sceneType.getType() + ", exceed SceneTypeEnd : " + Display_SceneType.SceneTypeEnd.getType());
                return false;
            } else if (sceneType.getType() == Display_SceneType.GALLERY_IMAGE.getType() || (item == null && commonDisplayEngine == null)) {
                String str = null;
                int sharpnessLevel = -202;
                if (sceneType.getType() == Display_SceneType.GALLERY_IMAGE.getType()) {
                    if (item == null || commonDisplayEngine == null) {
                        GalleryLog.w("DisplayEngineUtils", "processWithHardwareDisplayEngine item and commonDisplayEngine must not be null when sceneType is GALLERY_IMAGE.");
                        return false;
                    }
                    str = item.getFilePath();
                    if (str == null) {
                        GalleryLog.w("DisplayEngineUtils", "processWithHardwareDisplayEngine imageFilePath is not found when sceneType is GALLERY_IMAGE.");
                        return false;
                    }
                    sharpnessLevel = commonDisplayEngine.getSharpnessLevel();
                    if (-202 == sharpnessLevel) {
                        GalleryLog.w("DisplayEngineUtils", "processWithHardwareDisplayEngine sharpnessLevel must not be ENGINE_INVALID_VALUE when sceneType is GALLERY_IMAGE.");
                        return false;
                    }
                }
                waitForInit();
                if (sHardwareCapInit) {
                    if (sHwProcThread != null) {
                        try {
                            sHwProcThread.join();
                            sHwProcThread = null;
                        } catch (InterruptedException e) {
                            return false;
                        }
                    }
                    sHwProcThread = new HardwareDisplayEngineProcessThread(sceneType, str, sharpnessLevel);
                    sHwProcThread.start();
                    GalleryLog.d("DisplayEngineUtils", "display_effect hardwareDisplayEngineProcess cost time : " + (System.currentTimeMillis() - start));
                    return true;
                }
                GalleryLog.w("DisplayEngineUtils", "processWithHardwareDisplayEngine sHardwareCapInit == false.");
                return false;
            } else {
                GalleryLog.w("DisplayEngineUtils", "processWithHardwareDisplayEngine item and commonDisplayEnginePool must be null when sceneType is not GALLERY_IMAGE.");
                return false;
            }
        }
    }

    public static synchronized void updateEffectActivityOnResume() {
        synchronized (DisplayEngineUtils.class) {
            GalleryLog.d("DisplayEngineUtils", "display_effect updateEffectActivityOnResume");
            if (sActivityRef == 0) {
                processWithHardwareDisplayEngine(Display_SceneType.GALLERY_UI, null, null);
            }
            sActivityRef++;
        }
    }

    public static synchronized void updateEffectActivityOnPause() {
        synchronized (DisplayEngineUtils.class) {
            GalleryLog.d("DisplayEngineUtils", "display_effect updateEffectActivityOnPause");
            if (sActivityRef > 0) {
                sActivityRef--;
            } else {
                GalleryLog.w("DisplayEngineUtils", "updateEffectActivityOnPause sActivityRef=" + sActivityRef);
            }
            if (sActivityRef == 0) {
                processWithHardwareDisplayEngine(Display_SceneType.GALLERY_EXIT, null, null);
            }
        }
    }

    public static synchronized void updateEffectImageReview(MediaItem item, ScreenNailCommonDisplayEngine commonDisplayEngine) {
        synchronized (DisplayEngineUtils.class) {
            GalleryLog.d("DisplayEngineUtils", "display_effect updateEffectImageReview");
            if (item == null || commonDisplayEngine == null) {
                GalleryLog.d("DisplayEngineUtils", "display_effect updateEffectImageReview do nothing because item or commonDisplayEngine is null.");
            } else {
                String imageFilePath = item.getFilePath();
                if (imageFilePath == null || imageFilePath.equals(sCurrentImageFilePath)) {
                    GalleryLog.d("DisplayEngineUtils", "display_effect updateEffectImageReview do nothing because currentImage is not changed.");
                } else if (processWithHardwareDisplayEngine(Display_SceneType.GALLERY_IMAGE, item, commonDisplayEngine)) {
                    sCurrentImageFilePath = imageFilePath;
                }
            }
        }
    }

    public static synchronized void updateEffectImageReviewExit() {
        synchronized (DisplayEngineUtils.class) {
            GalleryLog.d("DisplayEngineUtils", "display_effect updateEffectImageReviewExit");
            if (sActivityRef > 0) {
                processWithHardwareDisplayEngine(Display_SceneType.GALLERY_UI, null, null);
            }
            sCurrentImageFilePath = null;
        }
    }

    private static void recycleBitmap(BitmapPool bitmapPool, Bitmap bitmap) {
        if (bitmapPool != null) {
            bitmapPool.recycle(bitmap);
        } else {
            bitmap.recycle();
        }
    }

    public static boolean getCapDoAce() {
        return sCapDoAce;
    }

    public static boolean getCapDoHardwareAce() {
        return sCapDoHardwareAce;
    }

    public static boolean getCapDoSr() {
        return sCapDoSr;
    }

    public static boolean getCapDoSharpness() {
        return sCapDoSharpness;
    }

    public static boolean getCapDoHardwareSharpness() {
        return sCapDoHardwareSharpness;
    }

    public static boolean getCapDoGmp() {
        return sCapDoGmp;
    }

    public static boolean getCapDoAcm() {
        return sCapDoAcm;
    }

    public static boolean getCapWideColorGamut() {
        return sCapWideColorGamut;
    }

    private static boolean initAlgoCapCfg() {
        DisplayEngineCapParams capParams = new DisplayEngineCapParams();
        int ret = displayEngineGetAlgoEnableState(getXmlFilePath(), capParams);
        sCapDoAce = capParams.bCapDoAce;
        sCapDoHardwareAce = capParams.bCapDoHardwareAce;
        sCapDoSr = capParams.bCapDoSr;
        sCapDoSharpness = capParams.bCapDoSharpness;
        sCapDoHardwareSharpness = capParams.bCapDoHardwareSharpness;
        sCapDoGmp = capParams.bCapDoGmp;
        sCapDoAcm = capParams.bCapDoAcm;
        sCapWideColorGamut = capParams.bCapWideColorGamut;
        GalleryLog.d("DisplayEngineUtils", "initAlgoCapCfg display_effect capParams =   sCapDoAce:" + getCapDoAce() + ", sCapDoHardwareAce:" + getCapDoHardwareAce() + ", sCapDoSr:" + getCapDoSr() + ", sCapDoSharpness:" + getCapDoSharpness() + ", sCapDoHardwareSharpness:" + getCapDoHardwareSharpness() + ", sCapDoGmp:" + getCapDoGmp() + ", sCapDoAcm:" + getCapDoAcm() + ", sCapWideColorGamut:" + getCapWideColorGamut());
        if (ret == 0) {
            return true;
        }
        return false;
    }

    private static boolean initHardwareCap() {
        if (sHardwareCapInit) {
            return true;
        }
        try {
            Class clazzDisplayEffect = Class.forName("com.huawei.android.hwsmartdisplay.HwSmartDisplay");
            sObjectDisplayEffect = clazzDisplayEffect.newInstance();
            sGetDisplayEffectSupportedMethod = clazzDisplayEffect.getMethod("getDisplayEffectSupported", new Class[]{Integer.TYPE});
            sSetDisplayEffectParamMethod = clazzDisplayEffect.getMethod("setDisplayEffectParam", new Class[]{Integer.TYPE, int[].class, Integer.TYPE});
            sHardwareCaps = ((Integer) sGetDisplayEffectSupportedMethod.invoke(sObjectDisplayEffect, new Object[]{Integer.valueOf(0)})).intValue();
            if (sHardwareCaps != 0) {
                sHardwareCapInit = true;
            }
            GalleryLog.d("DisplayEngineUtils", "display_effect sHardwareCaps: " + sHardwareCaps + ", sHardwareCapInit:" + sHardwareCapInit);
        } catch (ClassNotFoundException e) {
            GalleryLog.w("DisplayEngineUtils", "initHardwareCap Class Not Found");
        } catch (NoSuchMethodException e2) {
            GalleryLog.w("DisplayEngineUtils", "initHardwareCap No Such Method");
        } catch (InvocationTargetException e3) {
            GalleryLog.w("DisplayEngineUtils", "initHardwareCap InvocationTargetException");
        } catch (IllegalAccessException e4) {
            GalleryLog.w("DisplayEngineUtils", "initHardwareCap IllegalAccessExecption");
        } catch (InstantiationException e5) {
            GalleryLog.w("DisplayEngineUtils", "initHardwareCap InstantiationException");
        }
        return true;
    }
}
