package com.huawei.watermark.wmdata;

import android.content.Context;
import android.os.storage.StorageManager;
import com.android.gallery3d.R;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.WMConfig;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.wmdata.wmlistdata.WMSingleWatermarkDataDZ;
import com.huawei.watermark.wmdata.wmlistdata.WMSingleWatermarkDataEN;
import com.huawei.watermark.wmdata.wmlistdata.WMSingleWatermarkDataZH;
import com.huawei.watermark.wmdata.wmlistdata.WMWatermarkListData;
import com.huawei.watermark.wmdata.wmlistdata.basedata.WMSingleTypeData;
import com.huawei.watermark.wmdata.wmlistdata.basedata.WMSingleWatermarkData;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMCustomConfigurationUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipFile;

public class WMFileProcessor {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMFileProcessor.class.getSimpleName());
    private static WMFileProcessor instance;
    private static String sCategoryIndexKey = "sCategoryIndexKey";
    private static String sWMInCategoryIndexKey = "sWMInCategoryIndexKey";
    private String mCustWMZipPath;
    private final String[] mIds;
    private boolean mIsCustWaterMark;
    private boolean mLayoutDirectionRTL;
    private int mNowCategoryIndex;
    private int mNowWatermarkInCategoryIndex;
    private int mScanStatus;
    private int mSupportLanguage;
    private List<String> mWMCfgDirs;
    private ConcurrentMap<String, WMSingleTypeData> mWmTypeDataHashMapDZ;
    private ConcurrentMap<String, WMSingleTypeData> mWmTypeDataHashMapEN;
    private ConcurrentMap<String, WMSingleTypeData> mWmTypeDataHashMapZH;
    private ConcurrentMap<String, Integer> mWmTypeToTypenameId;
    private Vector<String> wmTypeNameList;

    public interface OnWatermarkDataInitStatusListener {
        void onInitFinish();

        void onInitFinishCategoryData();

        void onInitStart();
    }

    private WMFileProcessor() {
        this.mScanStatus = 1;
        this.mWmTypeToTypenameId = new ConcurrentHashMap();
        this.wmTypeNameList = new Vector();
        this.mWmTypeDataHashMapZH = new ConcurrentHashMap();
        this.mWmTypeDataHashMapEN = new ConcurrentHashMap();
        this.mWmTypeDataHashMapDZ = new ConcurrentHashMap();
        this.mNowCategoryIndex = 0;
        this.mNowWatermarkInCategoryIndex = 0;
        this.mLayoutDirectionRTL = false;
        this.mIsCustWaterMark = false;
        this.mWMCfgDirs = new ArrayList();
        this.mIds = new String[]{"ic_camera_watermark_menu_location", "ic_camera_watermark_menu_food", "ic_camera_watermark_menu_time", "wm_jar_locallib_category_specialicon_bg_selector_imageview", "ic_camera_watermark_menu_weather", "ic_camera_watermark_menu_mood", "ic_camera_watermark_menu_sport"};
        this.mScanStatus = 1;
        this.mIsCustWaterMark = WMBaseUtil.isWaterMarkCust();
    }

    public boolean isWatermarkSupport() {
        if (scanWMZipFileCount() == 0) {
            return false;
        }
        return true;
    }

    public int scanWMZipFileCount() {
        File wmdir = new File("system/watermark/wm");
        if (!wmdir.exists() || !wmdir.isDirectory()) {
            return 0;
        }
        File[] wms = wmdir.listFiles();
        if (wms == null || wms.length == 0) {
            return 0;
        }
        return wms.length;
    }

    private void removeNoResourceCategories() {
        int i;
        List<String> needRemoveList = new ArrayList();
        int length = this.wmTypeNameList.size();
        for (i = 0; i < length; i++) {
            String categoryname = getTypeNameWithIndex(i);
            if (getSingleTypeDataFromName(categoryname) == null) {
                needRemoveList.add(categoryname);
            }
        }
        for (i = 0; i < needRemoveList.size(); i++) {
            this.wmTypeNameList.remove(needRemoveList.get(i));
        }
    }

    public int scanAssetsInitWatermarkData(final Context context, final OnWatermarkDataInitStatusListener listener) {
        int result = this.mScanStatus;
        if (this.mScanStatus == 1) {
            synchronized (this) {
                this.mWMCfgDirs.clear();
                this.mWMCfgDirs.add("system/watermark/wm");
                if (this.mIsCustWaterMark) {
                    this.mCustWMZipPath = getCustWmStoragePath(context);
                    if (this.mCustWMZipPath != null) {
                        this.mWMCfgDirs.add(this.mCustWMZipPath);
                    }
                }
            }
            new Thread(new Runnable() {
                public void run() {
                    listener.onInitStart();
                    WMFileProcessor.this.mScanStatus = 2;
                    WMFileProcessor.this.wmTypeNameList.clear();
                    WMFileProcessor.this.mWmTypeToTypenameId.clear();
                    WMFileProcessor.this.mWmTypeDataHashMapZH.clear();
                    WMFileProcessor.this.mWmTypeDataHashMapEN.clear();
                    WMFileProcessor.this.mWmTypeDataHashMapDZ.clear();
                    WMFileProcessor.this.wmTypeNameList.add(GpsMeasureMode.MODE_3_DIMENSIONAL);
                    WMFileProcessor.this.wmTypeNameList.add("1");
                    WMFileProcessor.this.wmTypeNameList.add("5");
                    WMFileProcessor.this.wmTypeNameList.add("6");
                    WMFileProcessor.this.wmTypeNameList.add(GpsMeasureMode.MODE_2_DIMENSIONAL);
                    WMFileProcessor.this.wmTypeNameList.add("7");
                    WMFileProcessor.this.mWmTypeToTypenameId.put("1", Integer.valueOf(R.string.water_mark_category_location));
                    WMFileProcessor.this.mWmTypeToTypenameId.put(GpsMeasureMode.MODE_2_DIMENSIONAL, Integer.valueOf(R.string.water_mark_category_food));
                    WMFileProcessor.this.mWmTypeToTypenameId.put(GpsMeasureMode.MODE_3_DIMENSIONAL, Integer.valueOf(R.string.water_mark_category_time));
                    WMFileProcessor.this.mWmTypeToTypenameId.put("5", Integer.valueOf(R.string.water_mark_category_weather));
                    WMFileProcessor.this.mWmTypeToTypenameId.put("6", Integer.valueOf(R.string.water_mark_category_mood));
                    WMFileProcessor.this.mWmTypeToTypenameId.put("7", Integer.valueOf(R.string.water_mark_category_sport));
                    try {
                        synchronized (WMFileProcessor.this) {
                            int length = WMFileProcessor.this.mWMCfgDirs.size();
                            for (int j = 0; j < length; j++) {
                                File wmdir = new File((String) WMFileProcessor.this.mWMCfgDirs.get(j));
                                if (wmdir.isDirectory()) {
                                    File[] wms = wmdir.listFiles();
                                    if (!(wms == null || wms.length == 0)) {
                                        int i;
                                        String[] templist = new String[wms.length];
                                        for (i = 0; i < wms.length; i++) {
                                            templist[i] = wms[i].getName();
                                        }
                                        if (templist.length > 0) {
                                            for (i = 0; i < templist.length; i++) {
                                                String[] infostrlist = templist[i].split("_");
                                                if (infostrlist.length == 3 && "wm".equals(infostrlist[0]) && WMFileProcessor.this.isTypeLegal(infostrlist[1]) && WMFileProcessor.this.getIndexFromPath(infostrlist[2]) != -1) {
                                                    WMFileProcessor.this.addSingleWMDataToZHMap(new WMSingleWatermarkDataZH(context, templist[i], infostrlist[1], templist[i], WMFileProcessor.this.getIndexFromPath(infostrlist[2])), infostrlist[1]);
                                                    WMFileProcessor.this.addSingleWMDataToENMap(new WMSingleWatermarkDataEN(context, templist[i], infostrlist[1], templist[i], WMFileProcessor.this.getIndexFromPath(infostrlist[2])), infostrlist[1]);
                                                    WMFileProcessor.this.addSingleWMDataToDZMap(new WMSingleWatermarkDataDZ(context, templist[i], infostrlist[1], templist[i], WMFileProcessor.this.getIndexFromPath(infostrlist[2])), infostrlist[1]);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        WMFileProcessor.this.mSupportLanguage = WMWatermarkListData.getInstance(context).getIntValue(WMComponent.SP_SUPPORT_LANGUAGE_TYPE, 0);
                        WMFileProcessor.this.mScanStatus = 3;
                        WMFileProcessor.this.removeNoResourceCategories();
                        listener.onInitFinishCategoryData();
                        listener.onInitFinish();
                    } catch (SecurityException e) {
                        try {
                            WMLog.e(WMFileProcessor.TAG, "list watermark zip files got exception.", e);
                        } finally {
                            WMFileProcessor.this.mSupportLanguage = WMWatermarkListData.getInstance(context).getIntValue(WMComponent.SP_SUPPORT_LANGUAGE_TYPE, 0);
                            WMFileProcessor.this.mScanStatus = 3;
                            WMFileProcessor.this.removeNoResourceCategories();
                            listener.onInitFinishCategoryData();
                            listener.onInitFinish();
                        }
                    }
                }
            }).start();
        }
        return result;
    }

    public boolean getFinishInitWatermarkData() {
        return this.mScanStatus == 3;
    }

    public int getCategoryIconIdFromName(Context context, String name) {
        int pos;
        try {
            pos = Integer.parseInt(name) - 1;
        } catch (Exception e) {
            pos = 0;
        }
        if (pos < 0) {
            pos = 0;
        }
        return WMResourceUtil.getDrawableId(context, this.mIds[pos]);
    }

    private void addSingleWMDataToZHMap(WMSingleWatermarkData singlewmdata, String type) {
        if (!WMConfig.SUPPORTALL.equalsIgnoreCase(singlewmdata.getWMSupportLanguage()) ? WMConfig.SUPPORTZH.equalsIgnoreCase(singlewmdata.getWMSupportLanguage()) : true) {
            WMSingleTypeData singletypedata = (WMSingleTypeData) this.mWmTypeDataHashMapZH.get(type);
            if (singletypedata == null) {
                singletypedata = new WMSingleTypeData();
                this.mWmTypeDataHashMapZH.put(type, singletypedata);
            }
            addWMDataVecElements(singlewmdata, singletypedata);
            this.mWmTypeDataHashMapZH.put(type, singletypedata);
        }
    }

    private void addSingleWMDataToENMap(WMSingleWatermarkData singlewmdata, String type) {
        if (!WMConfig.SUPPORTALL.equalsIgnoreCase(singlewmdata.getWMSupportLanguage()) ? "en".equalsIgnoreCase(singlewmdata.getWMSupportLanguage()) : true) {
            WMSingleTypeData singletypedata = (WMSingleTypeData) this.mWmTypeDataHashMapEN.get(type);
            if (singletypedata == null) {
                singletypedata = new WMSingleTypeData();
                this.mWmTypeDataHashMapEN.put(type, singletypedata);
            }
            addWMDataVecElements(singlewmdata, singletypedata);
            this.mWmTypeDataHashMapEN.put(type, singletypedata);
        }
    }

    private void addSingleWMDataToDZMap(WMSingleWatermarkData singlewmdata, String type) {
        if (!WMConfig.SUPPORTALL.equalsIgnoreCase(singlewmdata.getWMSupportLanguage()) ? "en".equalsIgnoreCase(singlewmdata.getWMSupportLanguage()) : true) {
            WMSingleTypeData singletypedata = (WMSingleTypeData) this.mWmTypeDataHashMapDZ.get(type);
            if (singletypedata == null) {
                singletypedata = new WMSingleTypeData();
                this.mWmTypeDataHashMapDZ.put(type, singletypedata);
            }
            addWMDataVecElements(singlewmdata, singletypedata);
            this.mWmTypeDataHashMapDZ.put(type, singletypedata);
        }
    }

    private void addWMDataVecElements(WMSingleWatermarkData singlewmdata, WMSingleTypeData singletypedata) {
        boolean inserted = false;
        int j = 0;
        while (j < singletypedata.wmDataVec.size()) {
            int elementIndex = ((WMSingleWatermarkData) singletypedata.wmDataVec.elementAt(j)).getWMIndex();
            if (elementIndex > singlewmdata.getWMIndex()) {
                singletypedata.wmDataVec.add(j, singlewmdata);
                inserted = true;
                break;
            } else if (elementIndex == singlewmdata.getWMIndex()) {
                inserted = true;
                break;
            } else {
                j++;
            }
        }
        if (!inserted) {
            singletypedata.wmDataVec.add(singlewmdata);
        }
    }

    public static synchronized WMFileProcessor getInstance() {
        WMFileProcessor wMFileProcessor;
        synchronized (WMFileProcessor.class) {
            if (instance == null) {
                instance = new WMFileProcessor();
            }
            wMFileProcessor = instance;
        }
        return wMFileProcessor;
    }

    public void changeNowSupportLanguage(Context context, String key, int type) {
        this.mSupportLanguage = type;
        this.mLayoutDirectionRTL = WMUIUtil.isLayoutDirectionRTL(context);
        if (this.mLayoutDirectionRTL) {
            setNowCategoryIndex(context, key, getTypeNameListCount() - 1);
        } else {
            setNowCategoryIndex(context, key, 0);
        }
        setNowWatermarkInCategoryIndex(context, key, 0);
    }

    private int getSingleCategoryWMCount(int typeindex) {
        if (this.mScanStatus != 3 || typeindex > getTypeNameListCount()) {
            return 0;
        }
        WMSingleTypeData tempsingletypedata = getSingleTypeDataFromName(getTypeNameWithIndex(typeindex));
        if (tempsingletypedata == null) {
            return 0;
        }
        return tempsingletypedata.wmDataVec.size();
    }

    public int getNowTypeWMCount(Context context, String key) {
        if (this.mScanStatus != 3) {
            return 0;
        }
        return getSingleCategoryWMCount(getNowCategoryIndex(context, key));
    }

    public String getNowCategoryWmPathWithPosition(Context context, String key, int mWmIndex) {
        return getWmPath(getNowCategoryIndex(context, key), mWmIndex);
    }

    public String getWmPath(int mWmTypeIndex, int mWmIndex) {
        if (this.mScanStatus != 3 || mWmTypeIndex < 0 || mWmTypeIndex > getTypeNameListCount()) {
            return null;
        }
        WMSingleTypeData tempsingletypedata = getSingleTypeDataFromName(getTypeNameWithIndex(mWmTypeIndex));
        if (tempsingletypedata != null && mWmIndex >= 0 && mWmIndex < tempsingletypedata.wmDataVec.size()) {
            return ((WMSingleWatermarkData) tempsingletypedata.wmDataVec.elementAt(mWmIndex)).getWMPath();
        }
        return null;
    }

    public InputStream openZipInputStream(Context context, String path) {
        try {
            String zipPath = getZipFilePath(path);
            if (zipPath != null) {
                return new FileInputStream(zipPath + File.separator + path);
            }
        } catch (Exception e) {
            WMLog.e(TAG, "openZipInputStream on system got an exception", e);
        }
        return null;
    }

    public ZipFile openZipFile(String path) {
        try {
            String zipPath = getZipFilePath(path);
            if (zipPath != null) {
                return new ZipFile(zipPath + File.separator + path);
            }
        } catch (Exception e) {
            WMLog.e(TAG, "openZipInputStream on system got an exception", e);
        }
        return null;
    }

    private Vector<String> getTypeNameList() {
        return this.wmTypeNameList;
    }

    public int getTypeNameListCount() {
        return getTypeNameList().size();
    }

    public String getCategoryShowNamefromName(Context context, String name) {
        if (context == null) {
            return null;
        }
        return context.getResources().getString(((Integer) this.mWmTypeToTypenameId.get(name)).intValue());
    }

    public WMSingleTypeData getSingleTypeDataFromName(String name) {
        if (this.mScanStatus != 3) {
            return null;
        }
        WMSingleTypeData tempsingletypedata = null;
        switch (this.mSupportLanguage) {
            case 0:
                tempsingletypedata = (WMSingleTypeData) this.mWmTypeDataHashMapZH.get(name);
                break;
            case 1:
            case 2:
                if (!WMCustomConfigurationUtil.isEuropeanZone()) {
                    tempsingletypedata = (WMSingleTypeData) this.mWmTypeDataHashMapEN.get(name);
                    break;
                }
                tempsingletypedata = (WMSingleTypeData) this.mWmTypeDataHashMapDZ.get(name);
                break;
        }
        return tempsingletypedata;
    }

    public String getTypeNameWithIndex(int index) {
        if (index > getTypeNameListCount()) {
            return null;
        }
        if (this.mLayoutDirectionRTL) {
            index = (getTypeNameListCount() - 1) - index;
        }
        if (index < 0) {
            return null;
        }
        return (String) this.wmTypeNameList.elementAt(index);
    }

    public void setNowCategoryIndex(Context context, String key, int index) {
        if (this.mScanStatus == 3 || index == 0) {
            if (context != null) {
                setNowCategoryIndexFromSP(context, key, index);
            }
            this.mNowCategoryIndex = index;
        }
    }

    public int getNowCategoryIndex(Context context, String key) {
        if (this.mScanStatus != 3) {
            return 0;
        }
        if (context != null) {
            this.mNowCategoryIndex = getNowCategoryIndexFromSP(context, key);
        }
        return this.mNowCategoryIndex;
    }

    private int getNowCategoryIndexFromSP(Context context, String key) {
        if (this.mScanStatus != 3 || context == null) {
            return 0;
        }
        return WMWatermarkListData.getInstance(context).getIntValue(sCategoryIndexKey + key, WMUIUtil.isLayoutDirectionRTL(context) ? this.wmTypeNameList.size() - 1 : 0);
    }

    private void setNowCategoryIndexFromSP(Context context, String key, int index) {
        if ((this.mScanStatus == 3 || index == 0) && context != null) {
            WMWatermarkListData.getInstance(context).setIntValue(sCategoryIndexKey + key, index);
        }
    }

    public void setNowWatermarkInCategoryIndex(Context context, String key, int index) {
        if (this.mScanStatus == 3 || index == 0) {
            if (context != null) {
                setNowWatermarkInCategoryIndexFromSP(context, key, index);
            }
            this.mNowWatermarkInCategoryIndex = index;
        }
    }

    public int getNowWatermarkInCategoryIndex(Context context, String key) {
        if (this.mScanStatus != 3) {
            return 0;
        }
        if (context != null) {
            this.mNowWatermarkInCategoryIndex = getNowWatermarkInCategoryIndexFromSP(context, key);
        }
        return this.mNowWatermarkInCategoryIndex;
    }

    private int getNowWatermarkInCategoryIndexFromSP(Context context, String key) {
        if (this.mScanStatus == 3 && context != null) {
            return WMWatermarkListData.getInstance(context).getIntValue(sWMInCategoryIndexKey + key, 0);
        }
        return 0;
    }

    private void setNowWatermarkInCategoryIndexFromSP(Context context, String key, int index) {
        if ((this.mScanStatus == 3 || index == 0) && context != null) {
            WMWatermarkListData.getInstance(context).setIntValue(sWMInCategoryIndexKey + key, index);
        }
    }

    public int getIndexFromPath(String str) {
        if (str == null) {
            return -1;
        }
        int dot = str.lastIndexOf(46);
        if (dot <= -1 || dot >= str.length()) {
            return -1;
        }
        try {
            return Integer.parseInt(str.substring(0, dot));
        } catch (Exception e) {
            return -1;
        }
    }

    private String getZipFilePath(String fileName) {
        synchronized (this) {
            for (int i = this.mWMCfgDirs.size() - 1; i >= 0; i--) {
                if (new File((String) this.mWMCfgDirs.get(i), fileName).exists()) {
                    String str = (String) this.mWMCfgDirs.get(i);
                    return str;
                }
            }
            return null;
        }
    }

    private boolean isTypeLegal(String type) {
        if (type == null || "".equals(type.trim()) || !this.wmTypeNameList.contains(type)) {
            return false;
        }
        return true;
    }

    private String getCustWmStoragePath(Context context) {
        if (context == null) {
            return null;
        }
        String str;
        String path = null;
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService("storage");
            Class StorageVolume = Class.forName("android.os.storage.StorageVolume");
            Object[] storageVolumes = (Object[]) storageManager.getClass().getMethod("getVolumeList", new Class[0]).invoke(storageManager, new Object[0]);
            Method isPrimary = StorageVolume.getMethod("isPrimary", new Class[0]);
            Method getPath = StorageVolume.getMethod("getPath", new Class[0]);
            for (int i = 0; i < storageVolumes.length; i++) {
                if (((Boolean) isPrimary.invoke(storageVolumes[i], new Object[0])).booleanValue()) {
                    path = (String) getPath.invoke(storageVolumes[i], new Object[0]);
                    break;
                }
            }
        } catch (NoSuchMethodException e) {
            WMLog.e(TAG, "getInternalStroagePath NoSuchMethodException" + e.getMessage());
        } catch (InvocationTargetException e2) {
            WMLog.e(TAG, "getInternalStroagePath InvocationTargetException" + e2.getMessage());
        } catch (IllegalAccessException e3) {
            WMLog.e(TAG, "getInternalStroagePath IllegalAccessException" + e3.getMessage());
        } catch (ClassNotFoundException e4) {
            WMLog.e(TAG, "getInternalStroagePath ClassNotFoundException" + e4.getMessage());
        }
        if (path == null || path.trim().equals("")) {
            str = null;
        } else {
            str = path + "/watermark/wm";
        }
        return str;
    }
}
