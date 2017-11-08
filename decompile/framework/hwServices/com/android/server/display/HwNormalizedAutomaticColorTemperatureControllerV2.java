package com.android.server.display;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.os.FileUtils;
import android.os.IPowerManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.input.HwCircleAnimation;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwNormalizedAutomaticColorTemperatureControllerV2 {
    private static boolean DEBUG = false;
    private static final List<Point> HightLuminanceDefaultPoints = null;
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0";
    private static final float[] LowLuminanceDefaultPoints = new float[]{0.03f, 0.0f, 0.0f, 5200.0f};
    public static final int MSG_SET_COLOR_TEMPERATURE = 0;
    private static String TAG = "HwNormalizedAutomaticColorTemperatureControllerV2";
    private static final int VALUE_ANIMATION_MSG_DELAYED = 40;
    private static final int cctMinimumAnimationTime = 300;
    private static final int mDeviceActualBrightnessLevel = 255;
    private float CctCalc_A;
    private float CctCalc_B;
    private float CctCalc_C;
    private float CctCalc_D;
    private int animationCount;
    private int animationInterval;
    private int cctAnimationTime;
    private ArrayList<Integer> cctArray;
    private boolean cctNeedToUpdatedCct;
    private boolean cctUpddateEnable;
    private boolean isColorTemp3DimisionSupport;
    private int lightSensorRate;
    private float luminanceThresh;
    private boolean luxNeedToUpdateCct;
    protected float mAmbientCct;
    protected float mAmbientLux;
    public float mAmbientLux_before;
    private ValueAnimator mAnim_cct1;
    private int mColorTemperatureTarget;
    private String mConfigFilePath;
    protected Context mContext;
    private int mCurrentColorTemperature;
    private float mDefaultBrightness;
    private ArrayList<Point> mHighLuminancePoints;
    private HwAmbientCCTFilterAlgo mHwAmbientCctFilterAlgo;
    private HwAmbientLuxFilterAlgo mHwAmbientLuxFilterAlgo;
    private String mLcdPanelName;
    private ArrayList<Float> mLowLuminancePoints;
    private UpdateCctUtils mUpdateCctUtils;
    private int originCct;

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

    private String getLcdPanelName() {
        String str = null;
        try {
            str = FileUtils.readTextFile(new File(String.format("%s/lcd_model", new Object[]{LCD_PANEL_TYPE_PATH})), 0, null).trim().replace(' ', '_');
            Slog.d(TAG, "panelName is:" + str);
            return str;
        } catch (IOException e) {
            Slog.e(TAG, "Error reading lcd panel name", e);
            return str;
        }
    }

    private boolean getConfig() throws IOException {
        RuntimeException e;
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        Slog.i(TAG, "HwEyeProtectionControllerImpl getConfig");
        if (TextUtils.isEmpty(version)) {
            Slog.w(TAG, "get ro.build.version.emui failed!");
            return false;
        }
        String[] versionSplited = version.split("EmotionUI_");
        if (versionSplited.length < 2) {
            Slog.w(TAG, "split failed! version = " + version);
            return false;
        }
        if (TextUtils.isEmpty(versionSplited[1])) {
            Slog.w(TAG, "get emuiVersion failed!");
            return false;
        }
        String lcdEyeProtectionConfigFile = "EyeProtectionConfig_" + this.mLcdPanelName + ".xml";
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, Utils.HW_EYEPROTECTION_CONFIG_FILE}), 0);
        if (xmlFile == null) {
            xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{Utils.HW_EYEPROTECTION_CONFIG_FILE}), 0);
            if (xmlFile == null) {
                xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, lcdEyeProtectionConfigFile}), 0);
                if (xmlFile == null) {
                    String xmlPath = String.format("/xml/lcd/%s", new Object[]{lcdEyeProtectionConfigFile});
                    xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
                    if (xmlFile == null) {
                        Slog.w(TAG, "get xmlFile :" + xmlPath + " failed!");
                        return false;
                    }
                }
            }
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream)) {
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                    if (DEBUG) {
                        Slog.i(TAG, "get xmlFile :" + this.mConfigFilePath);
                    }
                }
                inputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }
                return true;
            } catch (FileNotFoundException e2) {
                fileInputStream = inputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (IOException e3) {
                fileInputStream = inputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (RuntimeException e4) {
                e = e4;
                fileInputStream = inputStream;
                try {
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e5) {
                fileInputStream = inputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = inputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        } catch (IOException e7) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        } catch (RuntimeException e8) {
            e = e8;
            throw e;
        } catch (Exception e9) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        if (DEBUG) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean configGroupLoadStarted = false;
        boolean LowLuminancePointsLoadStarted = false;
        boolean HighLuminancePointsLoadStarted = false;
        boolean loadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String name;
                switch (eventType) {
                    case 2:
                        name = parser.getName();
                        if (!name.equals(Utils.HW_EYEPROTECTION_CONFIG_FILE_NAME)) {
                            if (!name.equals("ColorTemperatureOrigin")) {
                                if (!name.equals("lightSensorRateMills")) {
                                    if (!name.equals("LuminanceThresh")) {
                                        if (!name.equals("CtAnimationTime")) {
                                            if (!name.equals("EyeProtectionLowLuminancePoints")) {
                                                if (!name.equals("Value") || !LowLuminancePointsLoadStarted) {
                                                    if (!name.equals("EyeProtectionHighLuminancePoints")) {
                                                        if (name.equals("Point") && HighLuminancePointsLoadStarted) {
                                                            Point currentPoint = new Point();
                                                            String s = parser.nextText();
                                                            currentPoint.x = Float.parseFloat(s.split(",")[0]);
                                                            currentPoint.y = Float.parseFloat(s.split(",")[1]);
                                                            if (this.mHighLuminancePoints == null) {
                                                                this.mHighLuminancePoints = new ArrayList();
                                                            }
                                                            this.mHighLuminancePoints.add(currentPoint);
                                                            break;
                                                        }
                                                    }
                                                    HighLuminancePointsLoadStarted = true;
                                                    break;
                                                }
                                                if (this.mLowLuminancePoints == null) {
                                                    this.mLowLuminancePoints = new ArrayList();
                                                }
                                                this.mLowLuminancePoints.add(Float.valueOf(Float.parseFloat(parser.nextText())));
                                                break;
                                            }
                                            LowLuminancePointsLoadStarted = true;
                                            break;
                                        }
                                        this.cctAnimationTime = Integer.parseInt(parser.nextText());
                                        if (this.cctAnimationTime < 300) {
                                            this.cctAnimationTime = 300;
                                        }
                                        Slog.d(TAG, "cctAnimationTime is " + this.cctAnimationTime);
                                        break;
                                    }
                                    this.luminanceThresh = Float.parseFloat(parser.nextText());
                                    Slog.d(TAG, "LuminanceThresh is " + this.luminanceThresh);
                                    break;
                                }
                                this.lightSensorRate = Integer.parseInt(parser.nextText());
                                break;
                            }
                            this.originCct = Integer.parseInt(parser.nextText());
                            break;
                        }
                        configGroupLoadStarted = true;
                        break;
                        break;
                    case 3:
                        name = parser.getName();
                        if (name.equals(Utils.HW_EYEPROTECTION_CONFIG_FILE_NAME) && configGroupLoadStarted) {
                            loadFinished = true;
                            configGroupLoadStarted = false;
                            break;
                        }
                        if (name.equals("EyeProtectionLowLuminancePoints")) {
                            LowLuminancePointsLoadStarted = false;
                            if (this.mLowLuminancePoints == null) {
                                Slog.e(TAG, "no EyeProtectionLowLuminancePoints  loaded!");
                                return false;
                            }
                        }
                        if (name.equals("EyeProtectionHighLuminancePoints")) {
                            HighLuminancePointsLoadStarted = false;
                            if (this.mHighLuminancePoints != null) {
                                break;
                            }
                            Slog.e(TAG, "no EyeProtectionHighLuminancePoints  loaded!");
                            return false;
                        }
                        break;
                }
                if (this.mLowLuminancePoints != null && this.mLowLuminancePoints.size() >= 4) {
                    this.CctCalc_A = ((Float) this.mLowLuminancePoints.get(0)).floatValue();
                    this.CctCalc_B = ((Float) this.mLowLuminancePoints.get(1)).floatValue();
                    this.CctCalc_C = ((Float) this.mLowLuminancePoints.get(2)).floatValue();
                    this.CctCalc_D = ((Float) this.mLowLuminancePoints.get(3)).floatValue();
                }
                if (loadFinished) {
                    if (loadFinished) {
                        Slog.i(TAG, "getConfigFromeXML success!");
                        return true;
                    }
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                }
            }
            if (loadFinished) {
                Slog.i(TAG, "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e) {
        } catch (RuntimeException e2) {
            throw e2;
        } catch (Exception e3) {
        }
        Slog.e(TAG, "getConfigFromeXML false!");
        return false;
    }

    private void setDefaultConfigValue() {
        this.originCct = 7861;
        this.luminanceThresh = 100.0f;
        this.CctCalc_A = 0.03f;
        this.CctCalc_B = 0.0f;
        this.CctCalc_C = 0.0f;
        this.CctCalc_D = 5200.0f;
        this.cctAnimationTime = HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS;
        if (this.mLowLuminancePoints == null) {
            this.mLowLuminancePoints = new ArrayList();
        } else {
            this.mLowLuminancePoints.clear();
        }
        for (float valueOf : LowLuminanceDefaultPoints) {
            this.mLowLuminancePoints.add(Float.valueOf(valueOf));
        }
        if (this.mHighLuminancePoints == null) {
            this.mHighLuminancePoints = new ArrayList();
        } else {
            this.mHighLuminancePoints.clear();
        }
        this.mHighLuminancePoints.add(new Point(0.0f, 5500.0f));
        this.mHighLuminancePoints.add(new Point(3500.0f, 5500.0f));
        this.mHighLuminancePoints.add(new Point(7000.0f, 7005.0f));
        this.mHighLuminancePoints.add(new Point(10000.0f, 7005.0f));
    }

    public HwNormalizedAutomaticColorTemperatureControllerV2(Context mContextEyePro) {
        this.luminanceThresh = 100.0f;
        this.mLowLuminancePoints = null;
        this.mHighLuminancePoints = null;
        this.lightSensorRate = 300;
        this.mUpdateCctUtils = new UpdateCctUtils();
        this.mLcdPanelName = null;
        this.mConfigFilePath = null;
        this.cctUpddateEnable = false;
        this.originCct = -1;
        this.isColorTemp3DimisionSupport = false;
        this.cctArray = new ArrayList();
        this.cctAnimationTime = HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS;
        this.animationCount = -1;
        this.animationInterval = 2;
        this.mDefaultBrightness = 6500.0f;
        this.mHwAmbientCctFilterAlgo = new HwAmbientCCTFilterAlgo(this.lightSensorRate);
        this.mHwAmbientLuxFilterAlgo = new HwAmbientLuxFilterAlgo(this.lightSensorRate, 255);
        this.mAnim_cct1 = new ValueAnimator();
        this.mLcdPanelName = getLcdPanelName();
        try {
            if (!getConfig()) {
                Slog.e(TAG, "getConfig failed!");
                setDefaultConfigValue();
            }
        } catch (Exception e) {
        }
        this.mContext = mContextEyePro;
        this.mCurrentColorTemperature = this.originCct;
        this.cctArray.add(Integer.valueOf(this.mCurrentColorTemperature));
    }

    public void isFirstAmbient(boolean is3DSupport) {
        this.mHwAmbientLuxFilterAlgo.isFirstAmbientLux(true);
        this.mHwAmbientCctFilterAlgo.isFirstAmbientCCT(true);
        this.cctUpddateEnable = true;
        this.isColorTemp3DimisionSupport = is3DSupport;
    }

    public void handleLightSensorEvent(long time, float lux, float cct) {
        this.mHwAmbientLuxFilterAlgo.handleLightSensorEvent(time, lux);
        this.mHwAmbientCctFilterAlgo.handleLightSensorEvent(time, cct);
        this.mAmbientLux = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
        this.mAmbientCct = this.mHwAmbientCctFilterAlgo.getCurrentAmbientCCT();
    }

    public boolean needToUpdatePanelCct() {
        boolean z = false;
        boolean z2 = this.mHwAmbientLuxFilterAlgo.needToUpdateBrightness() && (this.mAmbientLux < this.luminanceThresh || this.mAmbientLux_before < this.luminanceThresh);
        this.luxNeedToUpdateCct = z2;
        if (this.mHwAmbientCctFilterAlgo.needToUpdateCCT() && this.mAmbientLux > this.luminanceThresh) {
            z = true;
        }
        this.cctNeedToUpdatedCct = z;
        if (this.luxNeedToUpdateCct || this.cctNeedToUpdatedCct) {
            Slog.i(TAG, "need to update Cct: LuxNeed = " + this.luxNeedToUpdateCct + " ; CctNeed = " + this.cctNeedToUpdatedCct + " ; mAm = " + this.mAmbientLux + " ; mCt = " + this.mAmbientCct);
        }
        return this.luxNeedToUpdateCct | this.cctNeedToUpdatedCct;
    }

    public void updateColorTemp() {
        updateColorTemp(this.mAmbientLux, this.mAmbientCct);
    }

    public void updateColorTemp(float mAmbientLux, float mAmbientCct) {
        this.mHwAmbientLuxFilterAlgo.brightnessUpdated();
        this.mHwAmbientCctFilterAlgo.cctUpdated();
        this.mColorTemperatureTarget = updateColorTempTarget(mAmbientLux, mAmbientCct);
        if (this.mColorTemperatureTarget > 10000 || this.mColorTemperatureTarget < 5000) {
            Slog.i(TAG, "mColorTemperatureTarget is out of range : " + this.mColorTemperatureTarget);
            return;
        }
        if (mAmbientLux > 0.0f && mAmbientCct > 0.0f) {
            this.mAmbientLux_before = mAmbientLux;
        }
        Slog.i(TAG, "updateColorTemp:  mAmbientLux = " + mAmbientLux + " ; mAmbientCct = " + mAmbientCct + " ; mColorTemperatureTarget = " + this.mColorTemperatureTarget);
        startAnimation(this.mColorTemperatureTarget);
    }

    private void startAnimation(int colortemperature) {
        this.cctArray.add(Integer.valueOf(colortemperature));
        if (this.mAnim_cct1.isRunning()) {
            this.cctArray.set(this.cctArray.size() - 2, (Integer) this.mAnim_cct1.getAnimatedValue());
            this.mAnim_cct1.cancel();
            Slog.i(TAG, "startAnimation cancel");
        }
        Slog.i(TAG, "startAnimation size is " + this.cctArray.size());
        if (this.cctArray.size() > 1) {
            this.mAnim_cct1 = ValueAnimator.ofInt(new int[]{((Integer) this.cctArray.get(this.cctArray.size() - 2)).intValue(), ((Integer) this.cctArray.get(this.cctArray.size() - 1)).intValue()});
            Slog.i(TAG, "startAnimation cct is from  " + this.cctArray.get(this.cctArray.size() - 2) + " to " + this.cctArray.get(this.cctArray.size() - 1));
            if (this.cctArray.get(this.cctArray.size() - 2) == this.cctArray.get(this.cctArray.size() - 1)) {
                Slog.i(TAG, "startAnimation:current cct equals to target cct ,return");
                return;
            }
            this.animationCount = 0;
            this.mAnim_cct1.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    int currentValue = ((Integer) animation.getAnimatedValue()).intValue();
                    Slog.i(HwNormalizedAutomaticColorTemperatureControllerV2.TAG, "startAnimation:current animation cct value is : " + currentValue);
                    if (currentValue < 5000 || currentValue > 10000) {
                        Slog.i(HwNormalizedAutomaticColorTemperatureControllerV2.TAG, "Animation currentvalue is too small or too big :" + currentValue);
                    } else if (currentValue != HwNormalizedAutomaticColorTemperatureControllerV2.this.originCct || HwNormalizedAutomaticColorTemperatureControllerV2.this.cctUpddateEnable) {
                        HwNormalizedAutomaticColorTemperatureControllerV2.this.animationCount = HwNormalizedAutomaticColorTemperatureControllerV2.this.animationCount + 1;
                        if (HwNormalizedAutomaticColorTemperatureControllerV2.this.animationCount % HwNormalizedAutomaticColorTemperatureControllerV2.this.animationInterval == 0) {
                            float[] rgb = HwNormalizedAutomaticColorTemperatureControllerV2.this.mUpdateCctUtils.getRGBM((float) currentValue);
                            HwNormalizedAutomaticColorTemperatureControllerV2.this.updateRgbGamma(rgb[0], rgb[1], rgb[2]);
                            HwNormalizedAutomaticColorTemperatureControllerV2.this.mCurrentColorTemperature = currentValue;
                        }
                    } else {
                        HwNormalizedAutomaticColorTemperatureControllerV2.this.setColorTemperatureAccordingToSetting();
                        Slog.i(HwNormalizedAutomaticColorTemperatureControllerV2.TAG, "animation to Origin CCT: " + HwNormalizedAutomaticColorTemperatureControllerV2.this.originCct);
                    }
                }
            });
            this.mAnim_cct1.setDuration((long) this.cctAnimationTime).start();
        } else {
            Slog.i(TAG, "updateutils first run color temperature =" + this.cctArray.get(this.cctArray.size() - 1));
            float[] rgb = this.mUpdateCctUtils.getRGBM((float) (this.cctArray.size() - 1));
            updateRgbGamma(rgb[0], rgb[1], rgb[2]);
        }
    }

    private int updateRgbGamma(float red, float green, float blue) {
        Slog.i(TAG, "updateRgbGamma:red=" + red + " green=" + green + " blue=" + blue);
        try {
            return Stub.asInterface(ServiceManager.getService("power")).updateRgbGamma(red, green, blue);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public float getDefaultColorTemperatureLevelNew(List<Point> linePointsList, float lux) {
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

    public int updateColorTempTarget(float lux, float cct) {
        if (lux < 0.0f && cct < 0.0f) {
            Slog.i(TAG, "Update Color Temperature to origin");
            return this.originCct;
        } else if (lux > this.luminanceThresh) {
            return (int) getDefaultColorTemperatureLevelNew(this.mHighLuminancePoints, cct);
        } else {
            return (int) (((((this.CctCalc_A * lux) * lux) + (this.CctCalc_B * lux)) + (this.CctCalc_C * cct)) + this.CctCalc_D);
        }
    }

    private void setColorTemperatureAccordingToSetting() {
        Slog.i(TAG, "setColorTemperatureAccordingToSetting");
        int operation;
        if (this.isColorTemp3DimisionSupport) {
            Slog.i(TAG, "setColorTemperatureAccordingToSetting new.");
            try {
                String ctNewRGB = System.getStringForUser(this.mContext.getContentResolver(), Utils.COLOR_TEMPERATURE_RGB, -2);
                if (ctNewRGB != null) {
                    List<String> rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
                    float red = Float.valueOf((String) rgbarryList.get(0)).floatValue();
                    float green = Float.valueOf((String) rgbarryList.get(1)).floatValue();
                    float blue = Float.valueOf((String) rgbarryList.get(2)).floatValue();
                    Slog.i(TAG, "ColorTemperature read from setting:" + ctNewRGB + red + green + blue);
                    updateRgbGamma(red, green, blue);
                } else {
                    operation = System.getIntForUser(this.mContext.getContentResolver(), Utils.COLOR_TEMPERATURE, 128, -2);
                    Slog.i(TAG, "ColorTemperature read from old setting:" + operation);
                    setColorTemperature(operation);
                }
            } catch (UnsatisfiedLinkError e) {
                Slog.w(TAG, "ColorTemperature read from setting exception!");
                updateRgbGamma(HwCircleAnimation.SMALL_ALPHA, HwCircleAnimation.SMALL_ALPHA, HwCircleAnimation.SMALL_ALPHA);
            }
        } else {
            operation = System.getIntForUser(this.mContext.getContentResolver(), Utils.COLOR_TEMPERATURE, 128, -2);
            Slog.i(TAG, "setColorTemperatureAccordingToSetting old:" + operation);
            setColorTemperature(operation);
        }
    }

    private int setColorTemperature(int colorTemper) {
        try {
            return Stub.asInterface(ServiceManager.getService("power")).setColorTemperature(colorTemper);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public void reset() {
        if (this.cctUpddateEnable) {
            this.cctUpddateEnable = false;
            updateColorTemp(-1.0f, -1.0f);
        }
    }

    public void restore() {
        if (!this.cctUpddateEnable) {
            this.cctUpddateEnable = true;
            updateColorTemp();
        }
    }
}
