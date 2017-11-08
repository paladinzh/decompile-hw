package android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import huawei.android.utils.HwEyeProtectionSpline;
import huawei.android.utils.HwEyeProtectionSplineImpl;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwNormalizedSpline extends Spline {
    private static final float BRIGHTNESS_WITHDELTA_MAX = 230.0f;
    private static boolean DEBUG = false;
    private static final String HW_LABC_CONFIG_FILE = "LABCConfig.xml";
    private static final String TAG = "HwNormalizedSpline";
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME_NOEXT = "LABCConfig";
    private static final float maxBrightness = 255.0f;
    private static final float minBrightness = 4.0f;
    private float mAmLux;
    private float mAmLuxSaved;
    private boolean mBrightnessCalibrationEnabled;
    private boolean mCalibrtionModeBeforeEnable;
    private int mCalibrtionTest;
    private String mConfigFilePath;
    private ContentResolver mContentResolver;
    private boolean mCoverModeNoOffsetEnable;
    private int mCurrentUserId;
    List<Point> mDefaultBrighnessLinePointsList;
    List<Point> mDefaultBrighnessLinePointsListCaliBefore;
    private float mDefaultBrightness;
    private float mDelta;
    private float mDeltaNew;
    private float mDeltaSaved;
    private final int mDeviceActualBrightnessLevel;
    private int mDeviceActualBrightnessNit;
    private int mDeviceStandardBrightnessNit;
    private HwEyeProtectionSpline mEyeProtectionSpline;
    private boolean mIsReboot;
    private boolean mIsUserChange;
    private boolean mIsUserChangeSaved;
    private float mLastLuxDefaultBrightness;
    private float mLastLuxDefaultBrightnessSaved;
    private float mOffsetBrightenAlphaLeft;
    private float mOffsetBrightenAlphaRight;
    private float mOffsetBrightenRatioLeft;
    private float mOffsetBrightness_last;
    private float mOffsetBrightness_lastSaved;
    private float mPosBrightness;
    private float mPosBrightnessSaved;
    private float mStartLuxDefaultBrightness;
    private float mStartLuxDefaultBrightnessSaved;

    private static class Point {
        float x;
        float y;

        public Point(float inx, float iny) {
            this.x = inx;
            this.y = iny;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    private HwNormalizedSpline(Context context, int deviceActualBrightnessLevel, int deviceActualBrightnessNit, int deviceStandardBrightnessNit) {
        this.mDelta = 0.0f;
        this.mDeltaNew = 0.0f;
        this.mOffsetBrightenRatioLeft = HwFragmentMenuItemView.ALPHA_NORMAL;
        this.mOffsetBrightenAlphaLeft = HwFragmentMenuItemView.ALPHA_NORMAL;
        this.mOffsetBrightenAlphaRight = HwFragmentMenuItemView.ALPHA_NORMAL;
        this.mPosBrightness = minBrightness;
        this.mIsReboot = false;
        this.mIsUserChange = false;
        this.mOffsetBrightness_last = minBrightness;
        this.mLastLuxDefaultBrightness = minBrightness;
        this.mStartLuxDefaultBrightness = minBrightness;
        this.mConfigFilePath = null;
        this.mCurrentUserId = 0;
        this.mAmLux = -1.0f;
        this.mCoverModeNoOffsetEnable = false;
        this.mDefaultBrighnessLinePointsList = null;
        this.mDefaultBrighnessLinePointsListCaliBefore = null;
        this.mEyeProtectionSpline = null;
        this.mIsReboot = true;
        this.mContentResolver = context.getContentResolver();
        this.mDeviceActualBrightnessLevel = deviceActualBrightnessLevel;
        this.mDeviceActualBrightnessNit = deviceActualBrightnessNit;
        this.mDeviceStandardBrightnessNit = deviceStandardBrightnessNit;
        loadOffsetParas();
        try {
            if (!getConfig()) {
                Slog.e(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (IOException e) {
            Slog.e(TAG, "IOException : loadDefaultConfig");
            loadDefaultConfig();
        }
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mEyeProtectionSpline = new HwEyeProtectionSplineImpl(context);
        }
    }

    private boolean getConfig() throws IOException {
        Throwable th;
        String xmlPath = String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, SystemProperties.get("ro.config.devicecolor"), XML_EXT});
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (DEBUG) {
            Slog.i(TAG, "screenColor=" + screenColor + ",screenColorxmlPath=" + xmlPath);
        }
        if (xmlFile == null) {
            xmlPath = String.format("/xml/lcd/%s", new Object[]{HW_LABC_CONFIG_FILE});
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
            if (xmlFile == null) {
                Slog.e(TAG, "get xmlFile :" + xmlPath + " failed!");
                return false;
            }
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream)) {
                    if (checkConfigLoadedFromXML()) {
                        if (DEBUG) {
                            printConfigFromXML();
                        }
                        initLinePointsList();
                        if (DEBUG) {
                            Slog.i(TAG, "mBrightnessCalibrationEnabled=" + this.mBrightnessCalibrationEnabled + ",mDeviceActualBrightnessNit=" + this.mDeviceActualBrightnessNit + ",mDeviceStandardBrightnessNit=" + this.mDeviceStandardBrightnessNit);
                        }
                        if (this.mBrightnessCalibrationEnabled) {
                            brightnessCalibration(this.mDefaultBrighnessLinePointsList, this.mDeviceActualBrightnessNit, this.mDeviceStandardBrightnessNit);
                        }
                    }
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                    if (DEBUG) {
                        Slog.i(TAG, "get xmlFile :" + this.mConfigFilePath);
                    }
                    inputStream.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return true;
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                fileInputStream = inputStream;
                return false;
            } catch (FileNotFoundException e) {
                fileInputStream = inputStream;
                Slog.e(TAG, "getConfig : FileNotFoundException");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (IOException e2) {
                fileInputStream = inputStream;
                try {
                    Slog.e(TAG, "getConfig : IOException");
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = inputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e3) {
            Slog.e(TAG, "getConfig : FileNotFoundException");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        } catch (IOException e4) {
            Slog.e(TAG, "getConfig : IOException");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        }
    }

    private boolean checkConfigLoadedFromXML() {
        if (this.mDefaultBrightness <= 0.0f) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false for mDefaultBrightness <= 0, LoadDefaultConfig!");
            return false;
        } else if (!checkPointsListIsOK(this.mDefaultBrighnessLinePointsList)) {
            loadDefaultConfig();
            Slog.e(TAG, "checkPointsList mDefaultBrighnessLinePointsList is wrong, LoadDefaultConfig!");
            return false;
        } else if (this.mOffsetBrightenRatioLeft <= 0.0f || this.mOffsetBrightenAlphaLeft < 0.0f || ((double) this.mOffsetBrightenAlphaLeft) > 1.0d) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOffsetBrightenRatioLeft=" + this.mOffsetBrightenRatioLeft + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
            return false;
        } else if (this.mOffsetBrightenAlphaRight < 0.0f || ((double) this.mOffsetBrightenAlphaRight) > 1.0d) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
            return false;
        } else {
            if (DEBUG) {
                Slog.i(TAG, "checkConfigLoadedFromXML success!");
            }
            return true;
        }
    }

    private void initLinePointsList() {
        for (int i = 0; i < this.mDefaultBrighnessLinePointsList.size(); i++) {
            Point tempPoint = new Point();
            tempPoint.x = ((Point) this.mDefaultBrighnessLinePointsList.get(i)).x;
            tempPoint.y = ((Point) this.mDefaultBrighnessLinePointsList.get(i)).y;
            if (this.mDefaultBrighnessLinePointsListCaliBefore == null) {
                this.mDefaultBrighnessLinePointsListCaliBefore = new ArrayList();
            }
            this.mDefaultBrighnessLinePointsListCaliBefore.add(tempPoint);
        }
        System.putIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
        if (DEBUG) {
            Slog.i(TAG, "init list_DefaultBrighnessLinePointsBeforeCali");
        }
    }

    private void brightnessCalibration(List<Point> LinePointsList, int actulBrightnessNit, int standardBrightnessNit) {
        float calibrationRatio;
        List<Point> mLinePointsList = LinePointsList;
        int mActulBrightnessNit = actulBrightnessNit;
        int mStandardBrightnessNit = standardBrightnessNit;
        if (actulBrightnessNit < 400 || standardBrightnessNit > 1000 || standardBrightnessNit <= 0) {
            calibrationRatio = HwFragmentMenuItemView.ALPHA_NORMAL;
            Slog.e(TAG, "error input brightnessNit:mStandardBrightnessNit=" + standardBrightnessNit + ",mActulBrightnessNit=" + actulBrightnessNit);
        } else {
            calibrationRatio = ((float) standardBrightnessNit) / ((float) actulBrightnessNit);
            if (DEBUG) {
                Slog.i(TAG, "calibrationRatio=" + calibrationRatio + ",mStandardBrightnessNit=" + standardBrightnessNit + ",mActulBrightnessNit=" + actulBrightnessNit);
            }
        }
        for (int i = 0; i < LinePointsList.size(); i++) {
            Point pointTemp = (Point) LinePointsList.get(i);
            if (pointTemp.y > minBrightness && pointTemp.y < maxBrightness) {
                pointTemp.y *= calibrationRatio;
                if (pointTemp.y <= minBrightness) {
                    pointTemp.y = minBrightness;
                }
                if (pointTemp.y >= maxBrightness) {
                    pointTemp.y = maxBrightness;
                }
            }
        }
        for (Point temp : LinePointsList) {
            if (DEBUG) {
                Slog.i(TAG, "LoadXMLConfig_NewCalibrationBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
            }
        }
    }

    private boolean checkPointsListIsOK(List<Point> LinePointsList) {
        List<Point> mLinePointsList = LinePointsList;
        if (LinePointsList == null) {
            Slog.e(TAG, "LoadXML false for mLinePointsList == null");
            return false;
        } else if (LinePointsList.size() <= 2 || LinePointsList.size() >= 100) {
            Slog.e(TAG, "LoadXML false for mLinePointsList number is wrong");
            return false;
        } else {
            Point lastPoint = null;
            for (Point tmpPoint : LinePointsList) {
                if (lastPoint == null) {
                    lastPoint = tmpPoint;
                } else if (lastPoint.x >= tmpPoint.x) {
                    loadDefaultConfig();
                    Slog.e(TAG, "LoadXML false for mLinePointsList is wrong");
                    return false;
                } else {
                    lastPoint = tmpPoint;
                }
            }
            return true;
        }
    }

    private void loadDefaultConfig() {
        this.mDefaultBrightness = 100.0f;
        this.mBrightnessCalibrationEnabled = false;
        if (this.mDefaultBrighnessLinePointsList != null) {
            this.mDefaultBrighnessLinePointsList.clear();
        } else {
            this.mDefaultBrighnessLinePointsList = new ArrayList();
        }
        this.mDefaultBrighnessLinePointsList.add(new Point(0.0f, minBrightness));
        this.mDefaultBrighnessLinePointsList.add(new Point(25.0f, 46.5f));
        this.mDefaultBrighnessLinePointsList.add(new Point(1995.0f, 140.7f));
        this.mDefaultBrighnessLinePointsList.add(new Point(4000.0f, maxBrightness));
        this.mDefaultBrighnessLinePointsList.add(new Point(40000.0f, maxBrightness));
        if (DEBUG) {
            printConfigFromXML();
        }
    }

    private void printConfigFromXML() {
        Slog.i(TAG, "LoadXMLConfig_DefaultBrightness=" + this.mDefaultBrightness);
        Slog.i(TAG, "LoadXMLConfig_mBrightnessCalibrationEnabled=" + this.mBrightnessCalibrationEnabled + ",mOffsetBrightenRatioLeft=" + this.mOffsetBrightenRatioLeft + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft + ",mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
        for (Point temp : this.mDefaultBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_DefaultBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        if (DEBUG) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean DefaultBrightnessLoaded = false;
        boolean DefaultBrighnessLinePointsListsLoadStarted = false;
        boolean DefaultBrighnessLinePointsListLoaded = false;
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != 1) {
                String name;
                switch (eventType) {
                    case 2:
                        name = parser.getName();
                        if (!name.equals(XML_NAME_NOEXT)) {
                            if (configGroupLoadStarted) {
                                if (!name.equals("DefaultBrightness")) {
                                    if (!name.equals("BrightnessCalibrationEnabled")) {
                                        if (!name.equals("OffsetBrightenRatioLeft")) {
                                            if (!name.equals("OffsetBrightenAlphaLeft")) {
                                                if (!name.equals("OffsetBrightenAlphaRight")) {
                                                    if (!name.equals("DefaultBrightnessPoints")) {
                                                        if (name.equals("Point") && DefaultBrighnessLinePointsListsLoadStarted) {
                                                            Point currentPoint = new Point();
                                                            String s = parser.nextText();
                                                            currentPoint.x = Float.parseFloat(s.split(",")[0]);
                                                            currentPoint.y = Float.parseFloat(s.split(",")[1]);
                                                            if (this.mDefaultBrighnessLinePointsList == null) {
                                                                this.mDefaultBrighnessLinePointsList = new ArrayList();
                                                            }
                                                            this.mDefaultBrighnessLinePointsList.add(currentPoint);
                                                            break;
                                                        }
                                                    }
                                                    DefaultBrighnessLinePointsListsLoadStarted = true;
                                                    break;
                                                }
                                                this.mOffsetBrightenAlphaRight = Float.parseFloat(parser.nextText());
                                                break;
                                            }
                                            this.mOffsetBrightenAlphaLeft = Float.parseFloat(parser.nextText());
                                            break;
                                        }
                                        this.mOffsetBrightenRatioLeft = Float.parseFloat(parser.nextText());
                                        break;
                                    }
                                    this.mBrightnessCalibrationEnabled = Boolean.parseBoolean(parser.nextText());
                                    break;
                                }
                                this.mDefaultBrightness = Float.parseFloat(parser.nextText());
                                DefaultBrightnessLoaded = true;
                                break;
                            }
                        } else if (this.mDeviceActualBrightnessLevel != 0) {
                            String deviceLevelString = parser.getAttributeValue(null, "level");
                            if (deviceLevelString != null && deviceLevelString.length() != 0) {
                                if (Integer.parseInt(deviceLevelString) == this.mDeviceActualBrightnessLevel) {
                                    if (DEBUG) {
                                        Slog.i(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", find matched level in XML, load start");
                                    }
                                    configGroupLoadStarted = true;
                                    break;
                                }
                            }
                            if (DEBUG) {
                                Slog.i(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", but can't find level in XML, load start");
                            }
                            configGroupLoadStarted = true;
                            break;
                        } else {
                            if (DEBUG) {
                                Slog.i(TAG, "actualDeviceLevel = 0, load started");
                            }
                            configGroupLoadStarted = true;
                            break;
                        }
                        break;
                    case 3:
                        name = parser.getName();
                        if (name.equals(XML_NAME_NOEXT) && configGroupLoadStarted) {
                            loadFinished = true;
                            break;
                        } else if (configGroupLoadStarted && name.equals("DefaultBrightnessPoints")) {
                            DefaultBrighnessLinePointsListsLoadStarted = false;
                            if (this.mDefaultBrighnessLinePointsList != null) {
                                DefaultBrighnessLinePointsListLoaded = true;
                                break;
                            }
                            Slog.e(TAG, "no DefaultBrightnessPoints loaded!");
                            return false;
                        }
                        break;
                }
                if (!loadFinished) {
                    eventType = parser.next();
                } else if (DefaultBrightnessLoaded || !DefaultBrighnessLinePointsListLoaded) {
                    if (!configGroupLoadStarted) {
                        Slog.e(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                        return false;
                    }
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                } else {
                    if (DEBUG) {
                        Slog.i(TAG, "getConfigFromeXML success!");
                    }
                    return true;
                }
            }
            if (DefaultBrightnessLoaded) {
            }
            if (configGroupLoadStarted) {
                Slog.e(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                return false;
            }
        } catch (XmlPullParserException e) {
            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
        } catch (IOException e2) {
            Slog.e(TAG, "getConfigFromXML : IOException");
        } catch (NumberFormatException e3) {
            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
        }
        Slog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    public void updateCurrentUserId(int userId) {
        if (DEBUG) {
            Slog.d(TAG, "save old user's paras and load new user's paras when user change ");
        }
        saveOffsetParas();
        this.mCurrentUserId = userId;
        loadOffsetParas();
    }

    public void loadOffsetParas() {
        boolean z = true;
        this.mPosBrightnessSaved = System.getFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", 0.0f, this.mCurrentUserId) * maxBrightness;
        this.mPosBrightness = this.mPosBrightnessSaved;
        this.mDeltaSaved = System.getFloatForUser(this.mContentResolver, "spline_delta", 0.0f, this.mCurrentUserId);
        this.mDeltaNew = this.mDeltaSaved;
        if (System.getIntForUser(this.mContentResolver, "spline_is_user_change", 0, this.mCurrentUserId) != 1) {
            z = false;
        }
        this.mIsUserChangeSaved = z;
        this.mIsUserChange = this.mIsUserChangeSaved;
        this.mOffsetBrightness_lastSaved = System.getFloatForUser(this.mContentResolver, "spline_offset_brightness_last", minBrightness, this.mCurrentUserId);
        this.mOffsetBrightness_last = this.mOffsetBrightness_lastSaved;
        this.mLastLuxDefaultBrightnessSaved = System.getFloatForUser(this.mContentResolver, "spline_last_lux_default_brightness", minBrightness, this.mCurrentUserId);
        this.mLastLuxDefaultBrightness = this.mLastLuxDefaultBrightnessSaved;
        this.mStartLuxDefaultBrightnessSaved = System.getFloatForUser(this.mContentResolver, "spline_start_lux_default_brightness", minBrightness, this.mCurrentUserId);
        this.mStartLuxDefaultBrightness = this.mStartLuxDefaultBrightnessSaved;
        this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
        if (DEBUG) {
            Slog.d(TAG, "Read:userId=" + this.mCurrentUserId + ",mPosBrightness=" + this.mPosBrightness + ",mOffsetBrightness_last=" + this.mOffsetBrightness_last + ",mIsUserChange=" + this.mIsUserChange + ",mDeltaNew=" + this.mDeltaNew + ",mDelta=" + this.mDelta + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
    }

    private void saveOffsetParas() {
        if (((int) (this.mPosBrightness * 10.0f)) != ((int) (this.mPosBrightnessSaved * 10.0f))) {
            System.putFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", this.mPosBrightness / maxBrightness, this.mCurrentUserId);
            this.mPosBrightnessSaved = this.mPosBrightness;
        }
        if (((int) (this.mDeltaNew * 10.0f)) != ((int) (this.mDeltaSaved * 10.0f))) {
            System.putFloatForUser(this.mContentResolver, "spline_delta", this.mDeltaNew, this.mCurrentUserId);
            this.mDeltaSaved = this.mDeltaNew;
        }
        if (this.mIsUserChange != this.mIsUserChangeSaved) {
            System.putIntForUser(this.mContentResolver, "spline_is_user_change", this.mIsUserChange ? 1 : 0, this.mCurrentUserId);
            this.mIsUserChangeSaved = this.mIsUserChange;
        }
        if (((int) (this.mOffsetBrightness_last * 10.0f)) != ((int) (this.mOffsetBrightness_lastSaved * 10.0f))) {
            System.putFloatForUser(this.mContentResolver, "spline_offset_brightness_last", this.mOffsetBrightness_last, this.mCurrentUserId);
            this.mOffsetBrightness_lastSaved = this.mOffsetBrightness_last;
        }
        if (((int) (this.mLastLuxDefaultBrightness * 10.0f)) != ((int) (this.mLastLuxDefaultBrightnessSaved * 10.0f))) {
            System.putFloatForUser(this.mContentResolver, "spline_last_lux_default_brightness", this.mLastLuxDefaultBrightness, this.mCurrentUserId);
            this.mLastLuxDefaultBrightnessSaved = this.mLastLuxDefaultBrightness;
        }
        if (((int) (this.mStartLuxDefaultBrightness * 10.0f)) != ((int) (this.mStartLuxDefaultBrightnessSaved * 10.0f))) {
            System.putFloatForUser(this.mContentResolver, "spline_start_lux_default_brightness", this.mStartLuxDefaultBrightness, this.mCurrentUserId);
            this.mStartLuxDefaultBrightnessSaved = this.mStartLuxDefaultBrightness;
        }
        if (((int) (this.mAmLux * 10.0f)) != ((int) (this.mAmLuxSaved * 10.0f))) {
            System.putFloatForUser(this.mContentResolver, "spline_ambient_lux", this.mAmLux, this.mCurrentUserId);
            this.mAmLuxSaved = this.mAmLux;
        }
        if (DEBUG) {
            Slog.d(TAG, "write:userId=" + this.mCurrentUserId + ",mPosBrightness =" + this.mPosBrightness + ",mOffsetBrightness_last=" + this.mOffsetBrightness_last + ",mIsUserChange=" + this.mIsUserChange + ",mDeltaNew=" + this.mDeltaNew + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + "mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness + ",mAmLux=" + this.mAmLux);
        }
    }

    public static HwNormalizedSpline createHwNormalizedSpline(Context context, int deviceActualBrightnessLevel, int deviceActualBrightnessNit, int deviceStandardBrightnessNit) {
        return new HwNormalizedSpline(context, deviceActualBrightnessLevel, deviceActualBrightnessNit, deviceStandardBrightnessNit);
    }

    public String toString() {
        return new StringBuilder().toString();
    }

    public float interpolate(float x) {
        this.mAmLux = x;
        if (this.mPosBrightness == 0.0f) {
            this.mIsReboot = true;
        } else {
            this.mIsReboot = false;
        }
        if (DEBUG) {
            Slog.d(TAG, "interpolate:mPosBrightness=" + this.mPosBrightness + "lux=" + x + ",mIsReboot=" + this.mIsReboot + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta);
        }
        float value_interp = getInterpolatedValue(this.mPosBrightness, x) / maxBrightness;
        saveOffsetParas();
        return value_interp;
    }

    public void updateLevelWithLux(float PosBrightness, float lux) {
        if (lux < 0.0f) {
            Slog.e(TAG, "error input lux,lux=" + lux);
            return;
        }
        if (!this.mIsReboot) {
            this.mIsUserChange = true;
        }
        this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
        this.mPosBrightness = PosBrightness;
        this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
        this.mDeltaNew = this.mPosBrightness - this.mStartLuxDefaultBrightness;
        if (this.mPosBrightness == 0.0f) {
            this.mDelta = 0.0f;
            this.mDeltaNew = 0.0f;
            this.mOffsetBrightness_last = 0.0f;
            this.mLastLuxDefaultBrightness = 0.0f;
            this.mStartLuxDefaultBrightness = 0.0f;
        }
        if (DEBUG) {
            Slog.d(TAG, "updateLevel:mDelta=" + this.mDelta + ",mDeltaNew=" + this.mDeltaNew + ",mPosBrightness=" + this.mPosBrightness + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",lux=" + lux);
        }
        saveOffsetParas();
    }

    public float getInterpolatedValue(float PositionBrightness, float lux) {
        float defaultBrightness;
        float offsetBrightness;
        float PosBrightness = PositionBrightness;
        if (this.mEyeProtectionSpline != null && this.mEyeProtectionSpline.isEyeProtectionMode()) {
            defaultBrightness = this.mEyeProtectionSpline.getEyeProtectionBrightnessLevel(lux);
            Slog.i(TAG, "getEyeProtectionBrightnessLevel lux =" + lux + ", defaultBrightness =" + defaultBrightness);
        } else if (this.mCalibrtionModeBeforeEnable) {
            defaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsListCaliBefore, lux);
        } else {
            defaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
        }
        if (this.mIsReboot) {
            this.mLastLuxDefaultBrightness = defaultBrightness;
            this.mStartLuxDefaultBrightness = defaultBrightness;
            this.mOffsetBrightness_last = defaultBrightness;
            this.mIsReboot = false;
            this.mIsUserChange = false;
        }
        if (this.mLastLuxDefaultBrightness <= 0.0f && this.mPosBrightness != 0.0f) {
            this.mPosBrightness = 0.0f;
            PosBrightness = 0.0f;
            this.mDelta = 0.0f;
            this.mOffsetBrightness_last = 0.0f;
            this.mLastLuxDefaultBrightness = 0.0f;
            this.mStartLuxDefaultBrightness = 0.0f;
            this.mIsUserChange = false;
            saveOffsetParas();
            if (DEBUG) {
                Slog.d(TAG, "error state for default state");
            }
        }
        if (Math.abs(defaultBrightness - this.mLastLuxDefaultBrightness) < 1.0E-7f) {
            if (PosBrightness == 0.0f || this.mCoverModeNoOffsetEnable) {
                offsetBrightness = defaultBrightness;
                if (this.mCoverModeNoOffsetEnable) {
                    this.mCoverModeNoOffsetEnable = false;
                    if (DEBUG) {
                        Slog.i(TAG, "set mCoverModeNoOffsetEnable=" + this.mCoverModeNoOffsetEnable);
                    }
                }
            } else {
                offsetBrightness = this.mIsUserChange ? PosBrightness : this.mOffsetBrightness_last;
            }
        } else if (PosBrightness == 0.0f || this.mCoverModeNoOffsetEnable) {
            offsetBrightness = defaultBrightness;
            if (this.mCoverModeNoOffsetEnable) {
                this.mCoverModeNoOffsetEnable = false;
                if (DEBUG) {
                    Slog.i(TAG, "set mCoverModeNoOffsetEnable=" + this.mCoverModeNoOffsetEnable);
                }
            }
        } else {
            offsetBrightness = getOffsetBrightnessLevel_new(this.mStartLuxDefaultBrightness, defaultBrightness, PosBrightness);
        }
        if (DEBUG) {
            Slog.d(TAG, "offsetBrightness=" + offsetBrightness + ",mOffsetBrightness_last" + this.mOffsetBrightness_last + ",lux=" + lux + ",mPosBrightness=" + this.mPosBrightness + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta + ",defaultBrightness=" + defaultBrightness + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + "mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
        this.mLastLuxDefaultBrightness = defaultBrightness;
        this.mOffsetBrightness_last = offsetBrightness;
        return offsetBrightness;
    }

    public float getDefaultBrightnessLevelNew(List<Point> linePointsList, float lux) {
        List<Point> linePointsListIn = linePointsList;
        int count = 0;
        float brightnessLevel = this.mDefaultBrightness;
        Point temp1 = null;
        for (Point temp : linePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (lux < temp.x) {
                Point temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                brightnessLevel = this.mDefaultBrightness;
                if (!DEBUG) {
                    return brightnessLevel;
                }
                Slog.i(TAG, "DefaultBrighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return brightnessLevel;
            }
            temp1 = temp;
            brightnessLevel = temp.y;
            count++;
        }
        return brightnessLevel;
    }

    float getOffsetBrightnessLevel_new(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        if (this.mIsUserChange) {
            this.mIsUserChange = false;
        }
        float ratio = HwFragmentMenuItemView.ALPHA_NORMAL;
        float ratio2 = HwFragmentMenuItemView.ALPHA_NORMAL;
        if (brightnessStartOrig < brightnessEndOrig) {
            if (this.mDelta > 0.0f) {
                ratio2 = (((-this.mDelta) * (Math.abs(brightnessStartOrig - brightnessEndOrig) / (this.mDelta + 1.0E-7f))) / ((maxBrightness - brightnessStartOrig) + 1.0E-7f)) + HwFragmentMenuItemView.ALPHA_NORMAL;
                if (DEBUG) {
                    Slog.i(TAG, "Orig_ratio2=" + ratio2 + ",mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
                }
                ratio2 = ((((HwFragmentMenuItemView.ALPHA_NORMAL - this.mOffsetBrightenAlphaRight) * Math.max(brightnessEndOrig, brightnessStartNew)) + (this.mOffsetBrightenAlphaRight * ((this.mDelta * ratio2) + brightnessEndOrig))) - brightnessEndOrig) / (this.mDelta + 1.0E-7f);
                if (ratio2 < 0.0f) {
                    ratio2 = 0.0f;
                }
            }
            if (this.mDelta < 0.0f) {
                ratio = (((-this.mDelta) * (Math.abs(brightnessStartOrig - brightnessEndOrig) / (this.mDelta - 1.0E-7f))) / ((maxBrightness - brightnessStartOrig) + 1.0E-7f)) + HwFragmentMenuItemView.ALPHA_NORMAL;
                if (ratio < 0.0f) {
                    ratio = 0.0f;
                }
            }
        }
        if (brightnessStartOrig > brightnessEndOrig) {
            if (this.mDelta < 0.0f) {
                ratio2 = ((this.mDelta * (Math.abs(brightnessStartOrig - brightnessEndOrig) / (this.mDelta + 1.0E-7f))) / ((minBrightness - brightnessStartOrig) - 1.0E-7f)) + HwFragmentMenuItemView.ALPHA_NORMAL;
                if (ratio2 < 0.0f) {
                    ratio2 = 0.0f;
                }
            }
            if (this.mDelta > 0.0f) {
                float ratioTmp = (float) Math.pow((double) (brightnessEndOrig / (1.0E-7f + brightnessStartOrig)), (double) this.mOffsetBrightenRatioLeft);
                ratio = ((this.mOffsetBrightenAlphaLeft * brightnessEndOrig) / (1.0E-7f + brightnessStartOrig)) + ((HwFragmentMenuItemView.ALPHA_NORMAL - this.mOffsetBrightenAlphaLeft) * ratioTmp);
                if (DEBUG) {
                    Slog.d(TAG, "ratio=" + ratio + ",ratioTmp=" + ratioTmp + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
                }
            }
        }
        this.mDeltaNew = (this.mDelta * ratio2) * ratio;
        if (DEBUG) {
            Slog.d(TAG, "mDeltaNew=" + this.mDeltaNew + ",mDelta=" + this.mDelta + ",ratio2=" + ratio2 + ",ratio=" + ratio);
        }
        return Math.min(Math.max(brightnessEndOrig + this.mDeltaNew, minBrightness), maxBrightness);
    }

    public float getAmbientValueFromDB() {
        float ambientValue = System.getFloatForUser(this.mContentResolver, "spline_ambient_lux", 100.0f, this.mCurrentUserId);
        if (((int) ambientValue) < 0) {
            Slog.e(TAG, "error inputValue<min,ambientValue=" + ambientValue);
            ambientValue = 0.0f;
        }
        if (((int) ambientValue) <= 40000) {
            return ambientValue;
        }
        Slog.e(TAG, "error inputValue>max,ambientValue=" + ambientValue);
        return 40000.0f;
    }

    public boolean getCalibrationTestEable() {
        int calibrtionTest = System.getIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
        if (calibrtionTest == 0) {
            this.mCalibrtionModeBeforeEnable = false;
            return false;
        }
        int calibrtionTestLow = calibrtionTest & 65535;
        int calibrtionTestHigh = (calibrtionTest >> 16) & 65535;
        if (calibrtionTestLow != calibrtionTestHigh) {
            Slog.e(TAG, "error db, clear DB,,calibrtionTestLow=" + calibrtionTestLow + ",calibrtionTestHigh=" + calibrtionTestHigh);
            System.putIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
            this.mCalibrtionModeBeforeEnable = false;
            return false;
        }
        boolean calibrationTestEable;
        int calibrtionModeBeforeEnableInt = (calibrtionTestLow >> 1) & 1;
        int calibrationTestEnableInt = calibrtionTestLow & 1;
        if (calibrtionModeBeforeEnableInt == 1) {
            this.mCalibrtionModeBeforeEnable = true;
        } else {
            this.mCalibrtionModeBeforeEnable = false;
        }
        if (calibrationTestEnableInt == 1) {
            calibrationTestEable = true;
        } else {
            calibrationTestEable = false;
        }
        if (calibrtionTest != this.mCalibrtionTest) {
            this.mCalibrtionTest = calibrtionTest;
            if (DEBUG) {
                Slog.d(TAG, "mCalibrtionTest=" + this.mCalibrtionTest + ",calibrationTestEnableInt=" + calibrationTestEnableInt + ",calibrtionModeBeforeEnableInt=" + calibrtionModeBeforeEnableInt);
            }
        }
        return calibrationTestEable;
    }

    public void setEyeProtectionControlFlag(boolean inControlTime) {
        if (this.mEyeProtectionSpline != null) {
            this.mEyeProtectionSpline.setEyeProtectionControlFlag(inControlTime);
        }
    }

    public void setNoOffsetEnable(boolean noOffsetEnable) {
        this.mCoverModeNoOffsetEnable = noOffsetEnable;
        if (DEBUG) {
            Slog.i(TAG, "LabcCoverMode CoverModeNoOffsetEnable=" + this.mCoverModeNoOffsetEnable);
        }
    }
}
