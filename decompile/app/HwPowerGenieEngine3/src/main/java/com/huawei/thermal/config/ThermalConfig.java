package com.huawei.thermal.config;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.thermal.TContext;
import com.huawei.thermal.event.SceneEvent;
import com.huawei.thermal.security.DecodeXmlFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ThermalConfig {
    private static final boolean DEBUG = true;
    private static final String FEIMA_THERMALCONFIG_PRODUCT = ("hwpg/thermald_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final String FEIMA_THERMAL_POWERSAVE_CONFIG_PRODUCT = ("hwpg/thermald_powersave_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final String FEIMA_THERMAL_VR_CONFIG_PRODUCT = ("hwpg/thermald_vr_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final String THERMALCONFIG_PRODUCT = ("thermald_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final String THERMAL_POWERSAVE_CONFIG_PRODUCT = ("thermald_powersave_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private static final String THERMAL_VR_CONFIG_PRODUCT = ("thermald_vr_" + SystemProperties.get("ro.product.name", "") + ".xml");
    private final HashMap<String, String> mActionInterface = new HashMap();
    private final Context mContext;
    private final HashMap<String, Integer> mCustCombActionId = new HashMap();
    private final HashMap<Integer, String> mCustScenes = new HashMap();
    public int mDisabledLowSensorType = -1;
    public int mDisabledLowTemp = -5;
    public boolean mIsConfigBatteryLevel = false;
    public boolean mMutilSensorBatteryClr = false;
    public String mNotLimitChargeVal = null;
    public int mPostPoneTimeInMSec = -1000;
    public final HashMap<Integer, HashMap<Integer, SensorTempAction>> mSceneSensorActions = new HashMap();
    public String mScreenOnMaxBcurrentVal = null;
    private boolean mScrnStateCtrlChargeEnable = false;
    private final TContext mTContext;
    public int mWarningTemp = 68;
    public int mWarningType = 0;

    static {
        if (Log.isLoggable("HwThermalConfig", 2)) {
        }
    }

    public ThermalConfig(TContext tcontext) {
        this.mContext = tcontext.getContext();
        this.mTContext = tcontext;
    }

    public synchronized ArrayList<Integer> getConfigThermalScene() {
        ArrayList<Integer> scenes;
        scenes = new ArrayList();
        for (Entry entry : this.mSceneSensorActions.entrySet()) {
            int sceneID = ((Integer) entry.getKey()).intValue();
            if (sceneID < 100000) {
                if (this.mCustCombActionId.containsValue(Integer.valueOf(sceneID))) {
                    for (Entry ety : this.mCustCombActionId.entrySet()) {
                        if (sceneID == ((Integer) ety.getValue()).intValue()) {
                            String key = (String) ety.getKey();
                            String[] idArray = key.split("\\|");
                            if (idArray != null && idArray.length >= 2) {
                                try {
                                    int subId = Integer.parseInt(idArray[0]);
                                    if (subId > 0) {
                                        scenes.add(Integer.valueOf(subId));
                                        if (SceneEvent.mSubActionMap.containsKey(Integer.valueOf(subId))) {
                                            scenes.add(SceneEvent.mSubActionMap.get(Integer.valueOf(subId)));
                                        }
                                    }
                                    try {
                                        int parentId = Integer.parseInt(idArray[1]);
                                        if (parentId > 0) {
                                            scenes.add(Integer.valueOf(parentId));
                                        } else {
                                            continue;
                                        }
                                    } catch (Exception e) {
                                    }
                                } catch (Exception e2) {
                                    Log.e("HwThermalConfig", "error for " + key);
                                }
                            }
                        }
                    }
                    continue;
                } else {
                    scenes.add(Integer.valueOf(sceneID));
                    if (SceneEvent.mSubActionMap.containsKey(Integer.valueOf(sceneID))) {
                        scenes.add(SceneEvent.mSubActionMap.get(Integer.valueOf(sceneID)));
                    }
                }
            }
        }
        Log.i("HwThermalConfig", "scenes:" + scenes);
        return scenes;
    }

    public boolean checkPowerSaveThermalConfigFileExist(boolean quickChargeOff) {
        File file = HwCfgFilePolicy.getCfgFile(FEIMA_THERMAL_POWERSAVE_CONFIG_PRODUCT, 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile("hwpg/thermald_powersave.xml", 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile(THERMAL_POWERSAVE_CONFIG_PRODUCT, 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile("thermald_powersave.xml", 0);
        if (file != null && file.exists()) {
            return true;
        }
        if (quickChargeOff) {
            return checkPowerSaveQCThermalConfigFileExist();
        }
        return false;
    }

    public boolean checkQCThermalConfigFileExist() {
        File file = HwCfgFilePolicy.getCfgFile("hwpg/thermald_qcoff.xml", 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile("thermald_qcoff.xml", 0);
        if (file != null && file.exists()) {
            return true;
        }
        return checkPowerSaveQCThermalConfigFileExist();
    }

    private boolean checkPowerSaveQCThermalConfigFileExist() {
        File file = HwCfgFilePolicy.getCfgFile("hwpg/thermald_powersave_qcoff.xml", 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile("thermald_powersave_qcoff.xml", 0);
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    public boolean checkVRThermalConfigFileExist() {
        File file = HwCfgFilePolicy.getCfgFile("hwpg/thermald_vr.xml", 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile(FEIMA_THERMAL_VR_CONFIG_PRODUCT, 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile("thermald_vr.xml", 0);
        if (file != null && file.exists()) {
            return true;
        }
        file = HwCfgFilePolicy.getCfgFile(THERMAL_VR_CONFIG_PRODUCT, 0);
        return file != null && file.exists();
    }

    private File getThermalProductFile(int type, boolean usePowerSaveThermalConf, boolean vrMode, boolean quickChargeOff) {
        String configDir = null;
        switch (type) {
            case NativeAdapter.PLATFORM_QCOM /*0*/:
                if (!vrMode) {
                    configDir = !usePowerSaveThermalConf ? FEIMA_THERMALCONFIG_PRODUCT : FEIMA_THERMAL_POWERSAVE_CONFIG_PRODUCT;
                    break;
                }
                configDir = FEIMA_THERMAL_VR_CONFIG_PRODUCT;
                break;
            case NativeAdapter.PLATFORM_MTK /*1*/:
                if (!vrMode) {
                    configDir = !usePowerSaveThermalConf ? THERMALCONFIG_PRODUCT : THERMAL_POWERSAVE_CONFIG_PRODUCT;
                    break;
                }
                configDir = THERMAL_VR_CONFIG_PRODUCT;
                break;
        }
        File file = HwCfgFilePolicy.getCfgFile(configDir, 0);
        if (file == null || !file.exists()) {
            return null;
        }
        Log.i("HwThermalConfig", "find thermald product config : " + file);
        return file;
    }

    private File getThermalDefaultFile(int type, boolean usePowerSaveThermalConf, boolean vrMode, boolean quickChargeOff) {
        String configDir = null;
        switch (type) {
            case NativeAdapter.PLATFORM_QCOM /*0*/:
                if (!vrMode) {
                    if (!quickChargeOff) {
                        if (usePowerSaveThermalConf) {
                            configDir = "hwpg/thermald_powersave.xml";
                        } else {
                            configDir = "hwpg/thermald.xml";
                        }
                        break;
                    }
                    configDir = !usePowerSaveThermalConf ? "hwpg/thermald_qcoff.xml" : "hwpg/thermald_powersave_qcoff.xml";
                    break;
                }
                configDir = "hwpg/thermald_vr.xml";
                break;
            case NativeAdapter.PLATFORM_MTK /*1*/:
                if (!vrMode) {
                    if (!quickChargeOff) {
                        if (usePowerSaveThermalConf) {
                            configDir = "thermald_powersave.xml";
                        } else {
                            configDir = "thermald.xml";
                        }
                        break;
                    }
                    configDir = !usePowerSaveThermalConf ? "thermald_qcoff.xml" : "thermald_powersave_qcoff.xml";
                    break;
                }
                configDir = "thermald_vr.xml";
                break;
        }
        File file = HwCfgFilePolicy.getCfgFile(configDir, 0);
        if (file == null || !file.exists()) {
            return null;
        }
        Log.i("HwThermalConfig", "find thermald default config : " + file);
        return file;
    }

    private InputStream getThermalStream(int typeDir, boolean usePowerSaveThermalConf, boolean vrMode, boolean quickChargeOff) {
        InputStream inStream = null;
        File fileProduct = getThermalProductFile(typeDir, usePowerSaveThermalConf, vrMode, quickChargeOff);
        File fileDefault = getThermalDefaultFile(typeDir, usePowerSaveThermalConf, vrMode, quickChargeOff);
        if (fileProduct != null) {
            inStream = new FileInputStream(fileProduct);
        } else if (fileDefault == null) {
            try {
                Log.w("HwThermalConfig", "thermald config not found, dir type: " + typeDir);
            } catch (Exception e) {
                Log.w("HwThermalConfig", "get thermald config fail!");
                if (null != null) {
                    try {
                        inStream.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        } else {
            inStream = new FileInputStream(fileDefault);
        }
        return inStream;
    }

    private InputStream getThermalStreamCrypt(int typeDir, boolean usePowerSaveThermalConf, boolean vrMode, boolean quickChargeOff) {
        InputStream inStream;
        InputStream inputStream = null;
        InputStream inStreamDecode = null;
        File fileProduct = getThermalProductFile(typeDir, usePowerSaveThermalConf, vrMode, quickChargeOff);
        File fileDefault = getThermalDefaultFile(typeDir, usePowerSaveThermalConf, vrMode, quickChargeOff);
        if (fileProduct != null) {
            inStream = new FileInputStream(fileProduct);
            try {
                inStreamDecode = DecodeXmlFile.getDecodeInputStream(inStream);
            } catch (Exception e) {
                inputStream = inStream;
                Log.w("HwThermalConfig", "get crypt thermald config fail!");
                if (inputStream != null) {
                    inputStream.close();
                }
                if (null != null) {
                    inStreamDecode.close();
                }
                return null;
            }
        } else if (fileDefault == null) {
            try {
                Log.w("HwThermalConfig", "crypt thermald config not found, dir type: " + typeDir);
            } catch (Exception e2) {
                Log.w("HwThermalConfig", "get crypt thermald config fail!");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (null != null) {
                    inStreamDecode.close();
                }
                return null;
            }
        } else {
            inStream = new FileInputStream(fileDefault);
            inStreamDecode = DecodeXmlFile.getDecodeInputStream(inStream);
        }
        return inStreamDecode;
    }

    public int matchCustPkgName(String packageName) {
        if (packageName == null || packageName.equals("")) {
            return 0;
        }
        int custActionId = 0;
        for (Entry entry : this.mCustScenes.entrySet()) {
            Integer actionid = (Integer) entry.getKey();
            String actionName = (String) entry.getValue();
            if (actionName == null) {
                Log.w("HwThermalConfig", "match cust pkg name, pkg is null!");
            } else if (actionName.equalsIgnoreCase(packageName)) {
                return actionid.intValue();
            } else {
                if (Pattern.compile(actionName, 66).matcher(packageName).find()) {
                    custActionId = actionid.intValue();
                }
            }
        }
        return custActionId;
    }

    public int matchCustComb(int subActionID, int parentActionId, String packageName) {
        int custActionId = 0;
        if (packageName != null && !packageName.equals("")) {
            for (Entry entry : this.mCustCombActionId.entrySet()) {
                String key = (String) entry.getKey();
                if (key != null && key.startsWith(Integer.toString(subActionID)) && Pattern.compile(key, 66).matcher(packageName).find()) {
                    custActionId = ((Integer) entry.getValue()).intValue();
                    break;
                }
            }
        } else {
            custActionId = 0;
        }
        if (custActionId != 0) {
            return custActionId;
        }
        Integer combId = (Integer) this.mCustCombActionId.get(Integer.toString(subActionID) + "|" + Integer.toString(parentActionId));
        if (combId != null && this.mSceneSensorActions.containsKey(combId)) {
            return combId.intValue();
        }
        return subActionID;
    }

    public void resetScreenStateCharge() {
        this.mNotLimitChargeVal = null;
        this.mScreenOnMaxBcurrentVal = null;
        this.mPostPoneTimeInMSec = -1000;
    }

    public synchronized boolean loadThermalConf(boolean usePowerSaveThermalConf, boolean vrMode, boolean quickChargeOff) {
        InputStream inputStream = null;
        XmlPullParser xmlpp = Xml.newPullParser();
        try {
            inputStream = getThermalStreamCrypt(0, usePowerSaveThermalConf, vrMode, quickChargeOff);
            if (inputStream == null) {
                inputStream = getThermalStream(0, usePowerSaveThermalConf, vrMode, quickChargeOff);
                if (inputStream == null) {
                    inputStream = getThermalStreamCrypt(1, usePowerSaveThermalConf, vrMode, quickChargeOff);
                    if (inputStream == null) {
                        inputStream = getThermalStream(1, usePowerSaveThermalConf, vrMode, quickChargeOff);
                    }
                }
            }
            if (inputStream != null) {
                xmlpp.setInput(inputStream, "UTF-8");
                boolean sceneThermalEnable = false;
                String sceneName = "default";
                int sceneId = 10000;
                int sensorType = 0;
                boolean sensorEnable = false;
                boolean isConfigbatteryLevel = false;
                TriggerAction triggerAction = null;
                ActionItem actionItem = null;
                SensorTempAction sensorTempAction = null;
                int batLevelTrigger = -1;
                int batLevelClear = -1;
                int count = 0;
                HashMap<Integer, SensorTempAction> sensorActions = new HashMap();
                this.mActionInterface.clear();
                this.mSceneSensorActions.clear();
                for (int eventType = xmlpp.getEventType(); eventType != 1; eventType = xmlpp.next()) {
                    String nodeName = xmlpp.getName();
                    switch (eventType) {
                        case NativeAdapter.PLATFORM_HI /*2*/:
                            if ("scene".equals(nodeName)) {
                                sceneThermalEnable = true;
                                sceneName = xmlpp.getAttributeValue(0);
                                int thermalSceneID = Integer.parseInt(xmlpp.getAttributeValue(1));
                                sceneId = thermalSceneID;
                                if (thermalSceneID >= 100000) {
                                    this.mCustScenes.put(Integer.valueOf(sceneId), sceneName);
                                }
                            } else if ("comb_scene".equals(nodeName)) {
                                sceneThermalEnable = true;
                                sceneName = xmlpp.getAttributeValue(0);
                                int thermalSubSceneID = Integer.parseInt(xmlpp.getAttributeValue(1));
                                int thermalParentSceneID = Integer.parseInt(xmlpp.getAttributeValue(2));
                                if (thermalParentSceneID >= 0) {
                                    String key;
                                    if (thermalParentSceneID != 0) {
                                        key = Integer.toString(thermalSubSceneID) + "|" + Integer.toString(thermalParentSceneID);
                                    } else {
                                        key = Integer.toString(thermalSubSceneID) + "|" + sceneName;
                                    }
                                    if (this.mCustCombActionId.containsKey(key)) {
                                        sceneId = ((Integer) this.mCustCombActionId.get(key)).intValue();
                                    } else {
                                        count++;
                                        sceneId = 70000 + count;
                                        this.mCustCombActionId.put(key, Integer.valueOf(sceneId));
                                        Log.i("HwThermalConfig", "comb scene, key = (" + key + ") sceneId = (" + sceneId + ")");
                                    }
                                }
                            } else if ("sensor_temp".equals(nodeName)) {
                                String sensorName = xmlpp.getAttributeValue(0);
                                sensorType = Integer.parseInt(xmlpp.getAttributeValue(1));
                                sensorEnable = "true".equals(xmlpp.getAttributeValue(2));
                                if (sensorEnable) {
                                    SensorTempAction sensorTempAction2 = new SensorTempAction(sensorType, sensorName);
                                }
                            } else if ("action_filenode".equals(nodeName)) {
                                this.mActionInterface.put(xmlpp.getAttributeValue(0), xmlpp.nextText());
                            } else if ("warning_temperature".equals(nodeName)) {
                                this.mWarningType = Integer.parseInt(xmlpp.getAttributeValue(0));
                                this.mWarningTemp = Integer.parseInt(xmlpp.nextText());
                            } else if ("flash_disable_by_low_temp".equals(nodeName)) {
                                this.mDisabledLowSensorType = Integer.parseInt(xmlpp.getAttributeValue(0));
                                this.mDisabledLowTemp = Integer.parseInt(xmlpp.nextText());
                            } else if ("screen_on_charge_control".equals(nodeName)) {
                                this.mNotLimitChargeVal = xmlpp.getAttributeValue(0);
                                this.mScreenOnMaxBcurrentVal = xmlpp.getAttributeValue(1);
                                this.mPostPoneTimeInMSec = Integer.parseInt(xmlpp.getAttributeValue(2));
                                this.mScrnStateCtrlChargeEnable = true;
                                Log.i("HwThermalConfig", "mNotLimitChargeVal = " + this.mNotLimitChargeVal + ", mPostPoneTimeInMSec = " + this.mPostPoneTimeInMSec + ", mScreenOnMaxBcurrentVal = " + this.mScreenOnMaxBcurrentVal);
                            }
                            if (sensorEnable) {
                                if (!"item".equals(nodeName)) {
                                    if (!"thresholds".equals(nodeName)) {
                                        if (!"thresholds_clr".equals(nodeName)) {
                                            if (!"thresholds_clr_battery".equals(nodeName)) {
                                                if (!"sensor".equals(nodeName)) {
                                                    if ("action".equals(nodeName)) {
                                                        for (int i = 0; i < xmlpp.getAttributeCount(); i++) {
                                                            if ("battery_level_tri".equals(xmlpp.getAttributeName(i))) {
                                                                batLevelTrigger = Integer.parseInt(xmlpp.getAttributeValue(i));
                                                            } else if ("battery_level_clr".equals(xmlpp.getAttributeName(i))) {
                                                                batLevelClear = Integer.parseInt(xmlpp.getAttributeValue(i));
                                                            }
                                                        }
                                                        if (batLevelTrigger >= 0 && batLevelClear >= 0) {
                                                            this.mIsConfigBatteryLevel = true;
                                                            isConfigbatteryLevel = true;
                                                            actionItem = new ActionItem();
                                                            actionItem.setBatTrigger(batLevelTrigger);
                                                            actionItem.setBatClear(batLevelClear);
                                                            break;
                                                        }
                                                    } else if (!"cpu".equals(nodeName) && !"cpu1".equals(nodeName) && !"cpu2".equals(nodeName) && !"cpu3".equals(nodeName) && !"cpu_a15".equals(nodeName) && !"gpu".equals(nodeName) && !"shutdown".equals(nodeName) && !"lcd".equals(nodeName) && !"battery".equals(nodeName) && !"call_battery".equals(nodeName) && !"wlan".equals(nodeName) && !"paback".equals(nodeName) && !"app".equals(nodeName) && !"wifiap".equals(nodeName) && !"wifioff".equals(nodeName) && !"flash".equals(nodeName) && !"flash_front".equals(nodeName) && !"camera".equals(nodeName) && !"gps".equals(nodeName) && !"modem".equals(nodeName) && !"ucurrent".equals(nodeName) && !"ucurrent_aux".equals(nodeName) && !"bcurrent".equals(nodeName) && !"bcurrent_aux".equals(nodeName) && !"direct_charger".equals(nodeName) && !"uvoltage".equals(nodeName) && !"threshold_up".equals(nodeName) && !"threshold_down".equals(nodeName) && !"pop_up_dialog".equals(nodeName) && !"camera_warning".equals(nodeName) && !"camera_stop".equals(nodeName) && !"ipa_power".equals(nodeName) && !"ipa_temp".equals(nodeName) && !"ipa_switch".equals(nodeName) && !"fork_on_big".equals(nodeName) && !"boost".equals(nodeName) && !"vr_warning_level".equals(nodeName) && !"camera_fps".equals(nodeName) && !"app_action".equals(nodeName) && !"app_ctrl".equals(nodeName) && !"key_thread_sched".equals(nodeName)) {
                                                        break;
                                                    } else {
                                                        String value = xmlpp.nextText();
                                                        if (isConfigbatteryLevel) {
                                                            if (!(actionItem == null || actionItem.addAction(nodeName, value))) {
                                                                Log.e("HwThermalConfig", "error: the thermal config format for action " + nodeName);
                                                                sensorActions.clear();
                                                                this.mSceneSensorActions.clear();
                                                                if (inputStream != null) {
                                                                    try {
                                                                        inputStream.close();
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                                return false;
                                                            }
                                                        } else if (!(triggerAction == null || triggerAction.addAction(nodeName, value))) {
                                                            Log.e("HwThermalConfig", "error: the thermal config format for action " + nodeName);
                                                            sensorActions.clear();
                                                            this.mSceneSensorActions.clear();
                                                            if (inputStream != null) {
                                                                try {
                                                                    inputStream.close();
                                                                } catch (Exception e2) {
                                                                    e2.printStackTrace();
                                                                }
                                                            }
                                                            return false;
                                                        }
                                                    }
                                                } else if (!this.mMutilSensorBatteryClr) {
                                                    break;
                                                } else {
                                                    triggerAction.addBatteryClear(Integer.parseInt(xmlpp.getAttributeValue(0)), Integer.parseInt(xmlpp.nextText()));
                                                    break;
                                                }
                                            }
                                            this.mMutilSensorBatteryClr = true;
                                            break;
                                        }
                                        triggerAction.setClear(Integer.parseInt(xmlpp.nextText()));
                                        break;
                                    }
                                    triggerAction.setTrigger(Integer.parseInt(xmlpp.nextText()));
                                    break;
                                }
                                triggerAction = new TriggerAction();
                                break;
                            }
                            continue;
                            break;
                        case NativeAdapter.PLATFORM_K3V3 /*3*/:
                            if (!sensorEnable || !"action".equals(nodeName)) {
                                if (!sensorEnable || !"item".equals(nodeName)) {
                                    if (sensorEnable) {
                                        if ("sensor_temp".equals(nodeName)) {
                                            sensorActions.put(Integer.valueOf(sensorType), sensorTempAction);
                                            sensorType = 0;
                                            sensorEnable = false;
                                            break;
                                        }
                                    }
                                    if (!"scene".equals(nodeName) && !"comb_scene".equals(nodeName)) {
                                        break;
                                    }
                                    sceneThermalEnable = true;
                                    HashMap<Integer, SensorTempAction> temp = new HashMap();
                                    temp.putAll(sensorActions);
                                    this.mSceneSensorActions.put(Integer.valueOf(sceneId), temp);
                                    sensorActions = new HashMap();
                                    break;
                                } else if (sensorTempAction.addTriggerAction(triggerAction)) {
                                    break;
                                } else {
                                    Log.e("HwThermalConfig", "error: the thermal config format");
                                    sensorActions.clear();
                                    this.mSceneSensorActions.clear();
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Exception e22) {
                                            e22.printStackTrace();
                                        }
                                    }
                                    return false;
                                }
                            }
                            if (isConfigbatteryLevel) {
                                triggerAction.addActionItem(actionItem);
                            }
                            isConfigbatteryLevel = false;
                            batLevelTrigger = -1;
                            batLevelClear = -1;
                            break;
                        default:
                            break;
                    }
                }
                if (!sceneThermalEnable) {
                    this.mSceneSensorActions.put(Integer.valueOf(10000), sensorActions);
                    Log.i("HwThermalConfig", "old thermal xml");
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                }
                return true;
            }
            Log.e("HwThermalConfig", "error: not find the thermald.xml");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                }
            }
            return false;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            this.mSceneSensorActions.clear();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                }
            }
            return false;
        } catch (XmlPullParserException e4) {
            e4.printStackTrace();
            this.mSceneSensorActions.clear();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e222222) {
                    e222222.printStackTrace();
                }
            }
            return false;
        } catch (IOException e5) {
            e5.printStackTrace();
            this.mSceneSensorActions.clear();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2222222) {
                    e2222222.printStackTrace();
                }
            }
            return false;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e22222222) {
                    e22222222.printStackTrace();
                }
            }
        }
    }

    public String getThermalInterface(String action) {
        return (String) this.mActionInterface.get(action);
    }

    public String toString() {
        return new StringBuilder().toString();
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("        mMutilSensorBatteryClr : " + this.mMutilSensorBatteryClr);
        pw.println("        mNotLimitChargeVal: " + this.mNotLimitChargeVal);
        pw.println("        mScreenOnMaxBcurrentVal: " + this.mScreenOnMaxBcurrentVal);
        pw.println("        mPostPoneTimeInMSec: " + this.mPostPoneTimeInMSec);
        pw.println("");
        pw.println("  Cust Comb Scene Config: ");
        for (Entry entry : this.mCustCombActionId.entrySet()) {
            Integer sceneId = (Integer) entry.getValue();
            pw.println("        Comb Scene Key: " + ((String) entry.getKey()) + " Scene Id:" + sceneId);
        }
        pw.println("");
    }
}
