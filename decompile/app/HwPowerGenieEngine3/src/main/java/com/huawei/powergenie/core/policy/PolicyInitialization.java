package com.huawei.powergenie.core.policy;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;
import com.huawei.powergenie.R;
import com.huawei.powergenie.core.XmlHelper;
import com.huawei.powergenie.core.security.DecodeXmlFile;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class PolicyInitialization {
    private static String CUSTDIR_BACKLIGHT_POLICY = "/product/etc/hwpg/backlight_policy.xml";
    private static String CUSTDIR_CORE2_LOAD_POLICY = "/product/etc/hwpg/core2_sys_load_policy.xml";
    private static String CUSTDIR_CORE4_LOAD_POLICY = "/product/etc/hwpg/core4_sys_load_policy.xml";
    private static String CUSTDIR_CORE8_LOAD_POLICY = "/product/etc/hwpg/core8_sys_load_policy.xml";
    private static String CUSTDIR_EXTREME_CPU_POLICY = "/product/etc/hwpg/extreme_cpu_policy.xml";
    private static String CUSTDIR_NORMAL_CPU_POLICY = "/product/etc/hwpg/normal_cpu_policy.xml";
    private static String CUSTDIR_PERFORMANCE_CPU_POLICY = "/product/etc/hwpg/performance_cpu_policy.xml";
    private static String CUSTDIR_SUPER_CPU_POLICY = "/product/etc/hwpg/super_cpu_policy.xml";
    private static String CUSTDIR_VR_CPU_POLICY = "/product/etc/hwpg/vr_cpu_policy.xml";
    private static String CUST_PATH_PG_CONFIG = "/product/etc/hwpg/pg_config.xml";
    private static final ArrayList<String> mCustConfig = new ArrayList<String>() {
        {
            add(PolicyInitialization.CUST_PATH_PG_CONFIG);
            add(PolicyInitialization.CUSTDIR_SUPER_CPU_POLICY);
            add(PolicyInitialization.CUSTDIR_NORMAL_CPU_POLICY);
            add(PolicyInitialization.CUSTDIR_PERFORMANCE_CPU_POLICY);
            add(PolicyInitialization.CUSTDIR_EXTREME_CPU_POLICY);
            add(PolicyInitialization.CUSTDIR_CORE2_LOAD_POLICY);
            add(PolicyInitialization.CUSTDIR_CORE4_LOAD_POLICY);
            add(PolicyInitialization.CUSTDIR_CORE8_LOAD_POLICY);
            add(PolicyInitialization.CUSTDIR_BACKLIGHT_POLICY);
            add(PolicyInitialization.CUSTDIR_VR_CPU_POLICY);
        }
    };
    private static boolean mIsCrypt = true;
    private static final HashMap<String, Integer> mTagNameToTypeId = new HashMap<String, Integer>() {
        {
            put("ipps_policy", Integer.valueOf(3));
            put("cpu_number_max", Integer.valueOf(4));
            put("cpu_number_min", Integer.valueOf(5));
            put("cpu_maxprofile", Integer.valueOf(6));
            put("cpu_minprofile", Integer.valueOf(7));
            put("cpu_number_lock", Integer.valueOf(8));
            put("cpu_profile_block", Integer.valueOf(9));
            put("ddr_maxprofile", Integer.valueOf(10));
            put("ddr_minprofile", Integer.valueOf(11));
            put("ddr_profile_block", Integer.valueOf(12));
            put("gpu_maxprofile", Integer.valueOf(13));
            put("gpu_minprofile", Integer.valueOf(14));
            put("gpu_profile_block", Integer.valueOf(15));
            put("frc_state", Integer.valueOf(16));
            put("check_load_delay", Integer.valueOf(17));
            put("check_load_mode", Integer.valueOf(18));
            put("cpu_a15_maxprofile", Integer.valueOf(19));
            put("cpu_a15_minprofile", Integer.valueOf(20));
            put("threshold_up", Integer.valueOf(23));
            put("threshold_down", Integer.valueOf(24));
            put("delay_time", Integer.valueOf(25));
            put("msg_policy_threshold", Integer.valueOf(26));
            put("cpu0_maxprofile", Integer.valueOf(6));
            put("cpu0_minprofile", Integer.valueOf(7));
            put("cpu1_maxprofile", Integer.valueOf(27));
            put("cpu1_minprofile", Integer.valueOf(28));
            put("cpu2_maxprofile", Integer.valueOf(29));
            put("cpu2_minprofile", Integer.valueOf(30));
            put("cpu3_maxprofile", Integer.valueOf(31));
            put("cpu3_minprofile", Integer.valueOf(32));
            put("cpu4_maxprofile", Integer.valueOf(19));
            put("cpu4_minprofile", Integer.valueOf(20));
            put("cpu5_maxprofile", Integer.valueOf(33));
            put("cpu5_minprofile", Integer.valueOf(34));
            put("cpu6_maxprofile", Integer.valueOf(35));
            put("cpu6_minprofile", Integer.valueOf(36));
            put("cpu7_maxprofile", Integer.valueOf(37));
            put("cpu7_minprofile", Integer.valueOf(38));
            put("ipa_temp", Integer.valueOf(40));
            put("ipa_power", Integer.valueOf(39));
            put("ipa_switch", Integer.valueOf(41));
            put("fork_on_big", Integer.valueOf(42));
            put("boost", Integer.valueOf(43));
        }
    };

    public static void init(Context context) {
        boolean isInit = SharedPref.getInitStatus(context, false);
        boolean initDBOkay = true;
        initCustConfig();
        if (isInit) {
            updateCustConfig(context);
            return;
        }
        Log.i("PolicyInitialization", "Now init start ...");
        initSettings(context);
        if (!initDB(context)) {
            Log.e("PolicyInitialization", "error to init pg db, try to init again");
            initDBOkay = initDB(context);
        }
        if (initDBOkay) {
            Log.i("PolicyInitialization", "update init status");
            SharedPref.updateInitStatus(context, true);
        }
        recordCustConfigInfo(context);
        Log.i("PolicyInitialization", "Now init over!");
    }

    private static void initCustConfig() {
        if (new File("system/etc/").exists()) {
            Log.i("PolicyInitialization", "init cust config !");
            if (!new File(CUST_PATH_PG_CONFIG).exists()) {
                CUST_PATH_PG_CONFIG = "system/etc/pg_config.xml";
            }
            if (!new File(CUSTDIR_SUPER_CPU_POLICY).exists()) {
                CUSTDIR_SUPER_CPU_POLICY = "system/etc/super_cpu_policy.xml";
            }
            if (!new File(CUSTDIR_NORMAL_CPU_POLICY).exists()) {
                CUSTDIR_NORMAL_CPU_POLICY = "system/etc/normal_cpu_policy.xml";
            }
            if (!new File(CUSTDIR_PERFORMANCE_CPU_POLICY).exists()) {
                CUSTDIR_PERFORMANCE_CPU_POLICY = "system/etc/performance_cpu_policy.xml";
            }
            if (!new File(CUSTDIR_EXTREME_CPU_POLICY).exists()) {
                CUSTDIR_EXTREME_CPU_POLICY = "system/etc/extreme_cpu_policy.xml";
            }
            if (!new File(CUSTDIR_VR_CPU_POLICY).exists()) {
                CUSTDIR_VR_CPU_POLICY = "system/etc/vr_cpu_policy.xml";
            }
            if (!new File(CUSTDIR_CORE2_LOAD_POLICY).exists()) {
                CUSTDIR_CORE2_LOAD_POLICY = "system/etc/core2_sys_load_policy.xml";
            }
            if (!new File(CUSTDIR_CORE4_LOAD_POLICY).exists()) {
                CUSTDIR_CORE4_LOAD_POLICY = "system/etc/core4_sys_load_policy.xml";
            }
            if (!new File(CUSTDIR_CORE8_LOAD_POLICY).exists()) {
                CUSTDIR_CORE8_LOAD_POLICY = "system/etc/core8_sys_load_policy.xml";
            }
            if (!new File(CUSTDIR_BACKLIGHT_POLICY).exists()) {
                CUSTDIR_BACKLIGHT_POLICY = "system/etc/backlight_policy.xml";
            }
            mCustConfig.clear();
            mCustConfig.add(CUST_PATH_PG_CONFIG);
            mCustConfig.add(CUSTDIR_SUPER_CPU_POLICY);
            mCustConfig.add(CUSTDIR_NORMAL_CPU_POLICY);
            mCustConfig.add(CUSTDIR_PERFORMANCE_CPU_POLICY);
            mCustConfig.add(CUSTDIR_EXTREME_CPU_POLICY);
            mCustConfig.add(CUSTDIR_VR_CPU_POLICY);
            mCustConfig.add(CUSTDIR_CORE2_LOAD_POLICY);
            mCustConfig.add(CUSTDIR_CORE4_LOAD_POLICY);
            mCustConfig.add(CUSTDIR_CORE8_LOAD_POLICY);
            mCustConfig.add(CUSTDIR_BACKLIGHT_POLICY);
            return;
        }
        Log.e("PolicyInitialization", "not exist config in system/etc/ !");
    }

    private static void initSettings(Context context) {
        HashMap<String, Boolean> custConfig = loadconfigSettings(context, CUST_PATH_PG_CONFIG);
        if (custConfig != null) {
            if (custConfig.containsKey("clean_apps") && custConfig.containsKey("restrict_apps") && !((Boolean) custConfig.get("clean_apps")).booleanValue() && !((Boolean) custConfig.get("restrict_apps")).booleanValue()) {
                SharedPref.updateSettings(context, "apps_mgmt", false);
                Log.i("PolicyInitialization", "apps_mgmt was disabled!");
            }
            for (Entry entry : custConfig.entrySet()) {
                SharedPref.updateSettings(context, (String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
            }
        }
    }

    private static boolean initDB(Context context) {
        boolean z = false;
        DBWrapper dbWrapper = new DBWrapper(context);
        if (delDatabase(context)) {
            if (initSwitch(context, dbWrapper) && initPowerLevel(context, dbWrapper) && initSysLoad(context, dbWrapper) && initCpuPolicy(context, dbWrapper)) {
                z = initBacklightPolicy(context, dbWrapper);
            }
            return z;
        }
        Log.e("PolicyInitialization", "fail to delete database: powergenie.db");
        return false;
    }

    private static boolean delDatabase(Context context) {
        File fDB = context.getDatabasePath("powergenie.db");
        if (fDB == null || !fDB.exists()) {
            Log.i("PolicyInitialization", "not exist database: powergenie.db");
            return true;
        }
        Log.i("PolicyInitialization", "delete database: powergenie.db");
        return context.deleteDatabase("powergenie.db");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean initPowerLevel(Context context, DBWrapper dbWrapper) {
        boolean ret = false;
        XmlResourceParser xmlResourceParser = null;
        PolicyProvider.beginTransaction();
        try {
            String tag;
            xmlResourceParser = context.getResources().getXml(R.xml.power_levels);
            XmlHelper.beginDocument(xmlResourceParser, "power_levels");
            int levelId = 0;
            String itemName = null;
            int userContext = 0;
            int state = 0;
            int powerMode = 0;
            while (true) {
                XmlHelper.nextElement(xmlResourceParser);
                tag = xmlResourceParser.getName();
                if (tag != null) {
                    if (!"level".equals(tag)) {
                        if (!"item".equals(tag)) {
                            if (!"context".equals(tag)) {
                                if (!"state".equals(tag)) {
                                    if (!"mode".equals(tag)) {
                                        break;
                                    }
                                    powerMode = Integer.parseInt(xmlResourceParser.getAttributeValue(0));
                                } else {
                                    state = Integer.parseInt(xmlResourceParser.getAttributeValue(0));
                                }
                            } else {
                                userContext = Integer.parseInt(xmlResourceParser.getAttributeValue(0));
                            }
                        } else {
                            if (itemName != null) {
                                dbWrapper.addPowerLevel(levelId, userContext, state, powerMode);
                            }
                            itemName = xmlResourceParser.getAttributeValue(0);
                        }
                    } else {
                        if (levelId > 0) {
                            dbWrapper.addPowerLevel(levelId, userContext, state, powerMode);
                        }
                        String name = xmlResourceParser.getAttributeValue(0);
                        levelId = Integer.parseInt(xmlResourceParser.getAttributeValue(1));
                        itemName = null;
                        userContext = 0;
                        state = 0;
                        powerMode = 0;
                    }
                } else {
                    break;
                }
                ret = true;
                PolicyProvider.setTransactionSuccessful();
                return ret;
            }
            Log.e("PolicyInitialization", "tag: " + tag + " is unknown. ");
            ret = true;
            PolicyProvider.setTransactionSuccessful();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        } finally {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            PolicyProvider.endTransaction();
        }
        return ret;
    }

    private static boolean initSwitch(Context context, DBWrapper dbWrapper) {
        boolean ret = false;
        XmlResourceParser xmlResourceParser = null;
        PolicyProvider.beginTransaction();
        try {
            String tag;
            xmlResourceParser = context.getResources().getXml(R.xml.switch_policy);
            XmlHelper.beginDocument(xmlResourceParser, "switch_policy");
            int policyId = 0;
            while (true) {
                XmlHelper.nextElement(xmlResourceParser);
                tag = xmlResourceParser.getName();
                if (tag != null) {
                    if (!"policy".equals(tag)) {
                        if (!"switch".equals(tag)) {
                            break;
                        }
                        dbWrapper.addSwitcher(policyId, Integer.parseInt(xmlResourceParser.getAttributeValue(0)), Integer.parseInt(xmlResourceParser.getAttributeValue(1)), Integer.parseInt(xmlResourceParser.getAttributeValue(2)));
                    } else {
                        policyId = Integer.parseInt(xmlResourceParser.getAttributeValue(0));
                    }
                } else {
                    break;
                }
            }
            Log.e("PolicyInitialization", "tag: " + tag + " is unknown. ");
            ret = true;
            PolicyProvider.setTransactionSuccessful();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        } finally {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            PolicyProvider.endTransaction();
        }
        return ret;
    }

    private static void updateCustConfig(Context context) {
        boolean isUpdateDB = false;
        boolean isUpdatePGSettings = false;
        for (int i = 0; i < mCustConfig.size(); i++) {
            File configFile = new File((String) mCustConfig.get(i));
            if (configFile.exists()) {
                long oldConfigTime = SharedPref.getLongSettings(context, (String) mCustConfig.get(i), 0);
                if (0 == oldConfigTime || oldConfigTime != configFile.lastModified()) {
                    if (CUST_PATH_PG_CONFIG.equals(mCustConfig.get(i))) {
                        isUpdatePGSettings = true;
                    } else {
                        isUpdateDB = true;
                    }
                    SharedPref.updateLongSettings(context, (String) mCustConfig.get(i), configFile.lastModified());
                }
            }
        }
        if (isUpdateDB) {
            boolean result = initDB(context);
            if (!result) {
                Log.e("PolicyInitialization", "update cust config, try to init db again");
                result = initDB(context);
            }
            Log.i("PolicyInitialization", "Update powergenie.db result:" + result);
        }
        if (isUpdatePGSettings) {
            initSettings(context);
            Log.i("PolicyInitialization", "Update pg_settings.xml !");
        }
    }

    private static void recordCustConfigInfo(Context context) {
        for (int i = 0; i < mCustConfig.size(); i++) {
            File configFile = new File((String) mCustConfig.get(i));
            if (configFile.exists()) {
                SharedPref.updateLongSettings(context, (String) mCustConfig.get(i), configFile.lastModified());
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static HashMap<String, Boolean> loadconfigSettings(Context context, String custPath) {
        XmlPullParserException e;
        Throwable th;
        HashMap<String, Boolean> pgConfig = new HashMap();
        XmlPullParser xmlPullParser = null;
        InputStream inputStream = null;
        if (custPath != null) {
            try {
                InputStream in = new FileInputStream(custPath);
                try {
                    xmlPullParser = Xml.newPullParser();
                    xmlPullParser.setInput(in, null);
                    try {
                        XmlHelper.beginDocument(xmlPullParser, "pg_config");
                        while (true) {
                            XmlHelper.nextElement(xmlPullParser);
                            String tag = xmlPullParser.getName();
                            if (tag == null) {
                                break;
                            }
                            String name = xmlPullParser.getAttributeName(0);
                            String value = xmlPullParser.getAttributeValue(0);
                            String text = null;
                            if (xmlPullParser.next() == 4) {
                                text = xmlPullParser.getText();
                            }
                            if ("name".equalsIgnoreCase(name) && "bool".equals(tag)) {
                                boolean enable;
                                if ("true".equalsIgnoreCase(text)) {
                                    enable = true;
                                } else if ("false".equalsIgnoreCase(text)) {
                                    enable = false;
                                } else {
                                    Log.e("PolicyInitialization", "bool is not true or false. the value: " + text);
                                }
                                if ("packets_control".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("cpu_governor".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("clean_apps".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("restrict_apps".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("dynamic_compat".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("switcher_mgmt".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("battery_stats".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("default_clean_apps".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("context_feature".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("packets_rrc".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("packets_fastdormancy".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("thermal_com".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("freezer_feature".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                } else if ("scene_freezer_feature".equalsIgnoreCase(value)) {
                                    pgConfig.put(value, Boolean.valueOf(enable));
                                }
                            }
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e2) {
                                Log.w("PolicyInitialization", "Close Input stream error!");
                            }
                        } else if (xmlPullParser != null) {
                            if (xmlPullParser instanceof XmlResourceParser) {
                                ((XmlResourceParser) xmlPullParser).close();
                            }
                        }
                        inputStream = in;
                    } catch (XmlPullParserException e3) {
                        e = e3;
                        inputStream = in;
                    } catch (NumberFormatException e4) {
                        NumberFormatException e5 = e4;
                        inputStream = in;
                    } catch (IOException e6) {
                        IOException e7 = e6;
                        inputStream = in;
                    } catch (Throwable th2) {
                        th = th2;
                        inputStream = in;
                    }
                } catch (FileNotFoundException e8) {
                    inputStream = in;
                    try {
                        Log.w("PolicyInitialization", "FileNotFoundException: " + custPath);
                        if (inputStream == null) {
                            try {
                                inputStream.close();
                            } catch (IOException e9) {
                                Log.w("PolicyInitialization", "Close Input stream error!");
                            }
                        } else if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                            ((XmlResourceParser) xmlPullParser).close();
                        }
                        return null;
                    } catch (XmlPullParserException e10) {
                        e = e10;
                        try {
                            Log.e("PolicyInitialization", "XmlPullParserException ", e);
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e11) {
                                    Log.w("PolicyInitialization", "Close Input stream error!");
                                }
                                return pgConfig;
                            }
                            if (xmlPullParser != null) {
                                if (xmlPullParser instanceof XmlResourceParser) {
                                    ((XmlResourceParser) xmlPullParser).close();
                                }
                            }
                            return pgConfig;
                        } catch (Throwable th3) {
                            th = th3;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e12) {
                                    Log.w("PolicyInitialization", "Close Input stream error!");
                                }
                            } else if (xmlPullParser != null) {
                                if (xmlPullParser instanceof XmlResourceParser) {
                                    ((XmlResourceParser) xmlPullParser).close();
                                }
                            }
                            throw th;
                        }
                    } catch (NumberFormatException e13) {
                        e5 = e13;
                        Log.e("PolicyInitialization", "NumberFormatException ", e5);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e14) {
                                Log.w("PolicyInitialization", "Close Input stream error!");
                            }
                            return pgConfig;
                        }
                        if (xmlPullParser != null) {
                            if (xmlPullParser instanceof XmlResourceParser) {
                                ((XmlResourceParser) xmlPullParser).close();
                            }
                        }
                        return pgConfig;
                    } catch (IOException e15) {
                        e7 = e15;
                        Log.e("PolicyInitialization", "IOException ", e7);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e16) {
                                Log.w("PolicyInitialization", "Close Input stream error!");
                            }
                            return pgConfig;
                        }
                        if (xmlPullParser != null) {
                            if (xmlPullParser instanceof XmlResourceParser) {
                                ((XmlResourceParser) xmlPullParser).close();
                            }
                        }
                        return pgConfig;
                    }
                }
            } catch (FileNotFoundException e17) {
                Log.w("PolicyInitialization", "FileNotFoundException: " + custPath);
                if (inputStream == null) {
                    ((XmlResourceParser) xmlPullParser).close();
                } else {
                    inputStream.close();
                }
                return null;
            }
        }
        Log.w("PolicyInitialization", "The cust path is not exist !");
        return null;
        return pgConfig;
    }

    private static boolean initSysLoad(android.content.Context r24, com.huawei.powergenie.core.policy.DBWrapper r25) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.huawei.powergenie.core.policy.PolicyInitialization.initSysLoad(android.content.Context, com.huawei.powergenie.core.policy.DBWrapper):boolean. bs: [B:7:0x001d, B:28:0x007e]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r19 = 0;
        r18 = 0;
        r16 = 0;
        r21 = com.huawei.powergenie.integration.adapter.NativeAdapter.getCpuCores();
        r2 = -1;
        r0 = r21;
        if (r0 != r2) goto L_0x0042;
    L_0x000f:
        r10 = 8;
    L_0x0011:
        r2 = 2;
        if (r2 != r10) goto L_0x0072;
    L_0x0014:
        r17 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x0045 }
        r2 = CUSTDIR_CORE2_LOAD_POLICY;	 Catch:{ FileNotFoundException -> 0x0045 }
        r0 = r17;	 Catch:{ FileNotFoundException -> 0x0045 }
        r0.<init>(r2);	 Catch:{ FileNotFoundException -> 0x0045 }
        r18 = android.util.Xml.newPullParser();	 Catch:{ FileNotFoundException -> 0x0343, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r2 = 0;	 Catch:{ FileNotFoundException -> 0x0343, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r0 = r18;	 Catch:{ FileNotFoundException -> 0x0343, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r1 = r17;	 Catch:{ FileNotFoundException -> 0x0343, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r0.setInput(r1, r2);	 Catch:{ FileNotFoundException -> 0x0343, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
    L_0x0029:
        r16 = r17;
    L_0x002b:
        if (r18 != 0) goto L_0x0150;
    L_0x002d:
        r2 = "PolicyInitialization";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = "There is no any sys_load_policy file!";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        android.util.Log.w(r2, r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = 0;
        if (r16 == 0) goto L_0x0133;
    L_0x003b:
        r16.close();	 Catch:{ IOException -> 0x0144 }
    L_0x003e:
        com.huawei.powergenie.core.policy.PolicyProvider.endTransaction();
        return r2;
    L_0x0042:
        r10 = r21;
        goto L_0x0011;
    L_0x0045:
        r12 = move-exception;
    L_0x0046:
        r2 = "PolicyInitialization";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = new java.lang.StringBuilder;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22.<init>();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = "FileNotFoundException: ";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = CUSTDIR_CORE2_LOAD_POLICY;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.toString();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        android.util.Log.w(r2, r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r24.getResources();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = 2130903042; // 0x7f030002 float:1.741289E38 double:1.0528059877E-314;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r18 = r2.getXml(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x002b;
    L_0x0072:
        r2 = 4;
        if (r2 != r10) goto L_0x00bb;
    L_0x0075:
        r17 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x0340 }
        r2 = CUSTDIR_CORE4_LOAD_POLICY;	 Catch:{ FileNotFoundException -> 0x0340 }
        r0 = r17;	 Catch:{ FileNotFoundException -> 0x0340 }
        r0.<init>(r2);	 Catch:{ FileNotFoundException -> 0x0340 }
        r18 = android.util.Xml.newPullParser();	 Catch:{ FileNotFoundException -> 0x008b, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r2 = 0;	 Catch:{ FileNotFoundException -> 0x008b, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r0 = r18;	 Catch:{ FileNotFoundException -> 0x008b, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r1 = r17;	 Catch:{ FileNotFoundException -> 0x008b, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r0.setInput(r1, r2);	 Catch:{ FileNotFoundException -> 0x008b, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        goto L_0x0029;
    L_0x008b:
        r12 = move-exception;
        r16 = r17;
    L_0x008e:
        r2 = "PolicyInitialization";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = new java.lang.StringBuilder;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22.<init>();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = "FileNotFoundException: ";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = CUSTDIR_CORE4_LOAD_POLICY;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.toString();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        android.util.Log.w(r2, r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r24.getResources();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = 2130903043; // 0x7f030003 float:1.7412893E38 double:1.052805988E-314;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r18 = r2.getXml(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x002b;
    L_0x00bb:
        r2 = 8;
        if (r2 != r10) goto L_0x0106;
    L_0x00bf:
        r17 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x033d }
        r2 = CUSTDIR_CORE8_LOAD_POLICY;	 Catch:{ FileNotFoundException -> 0x033d }
        r0 = r17;	 Catch:{ FileNotFoundException -> 0x033d }
        r0.<init>(r2);	 Catch:{ FileNotFoundException -> 0x033d }
        r18 = android.util.Xml.newPullParser();	 Catch:{ FileNotFoundException -> 0x00d6, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r2 = 0;	 Catch:{ FileNotFoundException -> 0x00d6, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r0 = r18;	 Catch:{ FileNotFoundException -> 0x00d6, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r1 = r17;	 Catch:{ FileNotFoundException -> 0x00d6, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        r0.setInput(r1, r2);	 Catch:{ FileNotFoundException -> 0x00d6, NotFoundException -> 0x032a, XmlPullParserException -> 0x032f, NumberFormatException -> 0x0333, IOException -> 0x0338, all -> 0x0326 }
        goto L_0x0029;
    L_0x00d6:
        r12 = move-exception;
        r16 = r17;
    L_0x00d9:
        r2 = "PolicyInitialization";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = new java.lang.StringBuilder;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22.<init>();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = "FileNotFoundException: ";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = CUSTDIR_CORE8_LOAD_POLICY;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.toString();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        android.util.Log.w(r2, r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r24.getResources();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = 2130903044; // 0x7f030004 float:1.7412895E38 double:1.0528059887E-314;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r18 = r2.getXml(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x002b;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x0106:
        r2 = "PolicyInitialization";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = new java.lang.StringBuilder;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22.<init>();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = "unknown cpu cores:";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r0.append(r10);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.toString();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        android.util.Log.w(r2, r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r24.getResources();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = 2130903043; // 0x7f030003 float:1.7412893E38 double:1.052805988E-314;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r18 = r2.getXml(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x002b;
    L_0x0133:
        if (r18 == 0) goto L_0x003e;
    L_0x0135:
        r0 = r18;	 Catch:{ IOException -> 0x0144 }
        r0 = r0 instanceof android.content.res.XmlResourceParser;	 Catch:{ IOException -> 0x0144 }
        r22 = r0;	 Catch:{ IOException -> 0x0144 }
        if (r22 == 0) goto L_0x003e;	 Catch:{ IOException -> 0x0144 }
    L_0x013d:
        r18 = (android.content.res.XmlResourceParser) r18;	 Catch:{ IOException -> 0x0144 }
        r18.close();	 Catch:{ IOException -> 0x0144 }
        goto L_0x003e;
    L_0x0144:
        r13 = move-exception;
        r22 = "PolicyInitialization";
        r23 = "Close Input stream error!";
        android.util.Log.w(r22, r23);
        goto L_0x003e;
    L_0x0150:
        com.huawei.powergenie.core.policy.PolicyProvider.beginTransaction();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = "cpu_load";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r18;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        com.huawei.powergenie.core.XmlHelper.beginDocument(r0, r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r3 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r4 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r5 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r6 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r7 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r8 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r9 = 0;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x0162:
        com.huawei.powergenie.core.XmlHelper.nextElement(r18);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r20 = r18.getName();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r20 != 0) goto L_0x0181;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x016b:
        r2 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r3 == r2) goto L_0x0173;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x016e:
        r2 = r25;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2.addSysLoad(r3, r4, r5, r6, r7, r8, r9);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x0173:
        r19 = 1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        com.huawei.powergenie.core.policy.PolicyProvider.setTransactionSuccessful();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r16 == 0) goto L_0x023d;
    L_0x017a:
        r16.close();	 Catch:{ IOException -> 0x024f }
    L_0x017d:
        com.huawei.powergenie.core.policy.PolicyProvider.endTransaction();
    L_0x0180:
        return r19;
    L_0x0181:
        r2 = "para";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r20;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r2.equals(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r2 == 0) goto L_0x01a0;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x018c:
        r2 = -1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r3 == r2) goto L_0x0194;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x018f:
        r2 = r25;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2.addSysLoad(r3, r4, r5, r6, r7, r8, r9);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x0194:
        r2 = 0;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r18;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r0.getAttributeValue(r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r3 = java.lang.Integer.parseInt(r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x0162;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01a0:
        r2 = "upload";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r20;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r2.equals(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r2 == 0) goto L_0x01b4;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01ab:
        r2 = r18.nextText();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r4 = java.lang.Integer.parseInt(r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x0162;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01b4:
        r2 = "upchecktimes";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r20;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r2.equals(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r2 == 0) goto L_0x01c8;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01bf:
        r2 = r18.nextText();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r5 = java.lang.Integer.parseInt(r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x0162;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01c8:
        r2 = "upcheckspace";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r20;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r2.equals(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r2 == 0) goto L_0x01dc;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01d3:
        r2 = r18.nextText();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r6 = java.lang.Integer.parseInt(r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x0162;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01dc:
        r2 = "upoffset";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r20;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r2.equals(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r2 == 0) goto L_0x01f1;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01e7:
        r2 = r18.nextText();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r7 = java.lang.Integer.parseInt(r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x0162;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01f1:
        r2 = "maxchecktimes";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r20;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r2 = r2.equals(r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        if (r2 == 0) goto L_0x0206;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x01fc:
        r2 = r18.nextText();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r8 = java.lang.Integer.parseInt(r2);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x0162;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
    L_0x0206:
        r2 = "PolicyInitialization";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = new java.lang.StringBuilder;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22.<init>();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = "tag: ";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r1 = r20;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r0.append(r1);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r23 = " is unknown. ";	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.append(r23);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r22 = r22.toString();	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        r0 = r22;	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        android.util.Log.e(r2, r0);	 Catch:{ NotFoundException -> 0x022f, XmlPullParserException -> 0x02b5, NumberFormatException -> 0x0289, IOException -> 0x025d }
        goto L_0x0173;
    L_0x022f:
        r11 = move-exception;
    L_0x0230:
        r11.printStackTrace();	 Catch:{ all -> 0x0301 }
        if (r16 == 0) goto L_0x02e1;
    L_0x0235:
        r16.close();	 Catch:{ IOException -> 0x02f3 }
    L_0x0238:
        com.huawei.powergenie.core.policy.PolicyProvider.endTransaction();
        goto L_0x0180;
    L_0x023d:
        if (r18 == 0) goto L_0x017d;
    L_0x023f:
        r0 = r18;	 Catch:{ IOException -> 0x024f }
        r2 = r0 instanceof android.content.res.XmlResourceParser;	 Catch:{ IOException -> 0x024f }
        if (r2 == 0) goto L_0x017d;	 Catch:{ IOException -> 0x024f }
    L_0x0245:
        r0 = r18;	 Catch:{ IOException -> 0x024f }
        r0 = (android.content.res.XmlResourceParser) r0;	 Catch:{ IOException -> 0x024f }
        r2 = r0;	 Catch:{ IOException -> 0x024f }
        r2.close();	 Catch:{ IOException -> 0x024f }
        goto L_0x017d;
    L_0x024f:
        r13 = move-exception;
        r2 = "PolicyInitialization";
        r22 = "Close Input stream error!";
        r0 = r22;
        android.util.Log.w(r2, r0);
        goto L_0x017d;
    L_0x025d:
        r13 = move-exception;
    L_0x025e:
        r13.printStackTrace();	 Catch:{ all -> 0x0301 }
        if (r16 == 0) goto L_0x026b;
    L_0x0263:
        r16.close();	 Catch:{ IOException -> 0x027c }
    L_0x0266:
        com.huawei.powergenie.core.policy.PolicyProvider.endTransaction();
        goto L_0x0180;
    L_0x026b:
        if (r18 == 0) goto L_0x0266;
    L_0x026d:
        r0 = r18;	 Catch:{ IOException -> 0x027c }
        r2 = r0 instanceof android.content.res.XmlResourceParser;	 Catch:{ IOException -> 0x027c }
        if (r2 == 0) goto L_0x0266;	 Catch:{ IOException -> 0x027c }
    L_0x0273:
        r0 = r18;	 Catch:{ IOException -> 0x027c }
        r0 = (android.content.res.XmlResourceParser) r0;	 Catch:{ IOException -> 0x027c }
        r2 = r0;	 Catch:{ IOException -> 0x027c }
        r2.close();	 Catch:{ IOException -> 0x027c }
        goto L_0x0266;
    L_0x027c:
        r13 = move-exception;
        r2 = "PolicyInitialization";
        r22 = "Close Input stream error!";
        r0 = r22;
        android.util.Log.w(r2, r0);
        goto L_0x0266;
    L_0x0289:
        r14 = move-exception;
    L_0x028a:
        r14.printStackTrace();	 Catch:{ all -> 0x0301 }
        if (r16 == 0) goto L_0x0297;
    L_0x028f:
        r16.close();	 Catch:{ IOException -> 0x02a8 }
    L_0x0292:
        com.huawei.powergenie.core.policy.PolicyProvider.endTransaction();
        goto L_0x0180;
    L_0x0297:
        if (r18 == 0) goto L_0x0292;
    L_0x0299:
        r0 = r18;	 Catch:{ IOException -> 0x02a8 }
        r2 = r0 instanceof android.content.res.XmlResourceParser;	 Catch:{ IOException -> 0x02a8 }
        if (r2 == 0) goto L_0x0292;	 Catch:{ IOException -> 0x02a8 }
    L_0x029f:
        r0 = r18;	 Catch:{ IOException -> 0x02a8 }
        r0 = (android.content.res.XmlResourceParser) r0;	 Catch:{ IOException -> 0x02a8 }
        r2 = r0;	 Catch:{ IOException -> 0x02a8 }
        r2.close();	 Catch:{ IOException -> 0x02a8 }
        goto L_0x0292;
    L_0x02a8:
        r13 = move-exception;
        r2 = "PolicyInitialization";
        r22 = "Close Input stream error!";
        r0 = r22;
        android.util.Log.w(r2, r0);
        goto L_0x0292;
    L_0x02b5:
        r15 = move-exception;
    L_0x02b6:
        r15.printStackTrace();	 Catch:{ all -> 0x0301 }
        if (r16 == 0) goto L_0x02c3;
    L_0x02bb:
        r16.close();	 Catch:{ IOException -> 0x02d4 }
    L_0x02be:
        com.huawei.powergenie.core.policy.PolicyProvider.endTransaction();
        goto L_0x0180;
    L_0x02c3:
        if (r18 == 0) goto L_0x02be;
    L_0x02c5:
        r0 = r18;	 Catch:{ IOException -> 0x02d4 }
        r2 = r0 instanceof android.content.res.XmlResourceParser;	 Catch:{ IOException -> 0x02d4 }
        if (r2 == 0) goto L_0x02be;	 Catch:{ IOException -> 0x02d4 }
    L_0x02cb:
        r0 = r18;	 Catch:{ IOException -> 0x02d4 }
        r0 = (android.content.res.XmlResourceParser) r0;	 Catch:{ IOException -> 0x02d4 }
        r2 = r0;	 Catch:{ IOException -> 0x02d4 }
        r2.close();	 Catch:{ IOException -> 0x02d4 }
        goto L_0x02be;
    L_0x02d4:
        r13 = move-exception;
        r2 = "PolicyInitialization";
        r22 = "Close Input stream error!";
        r0 = r22;
        android.util.Log.w(r2, r0);
        goto L_0x02be;
    L_0x02e1:
        if (r18 == 0) goto L_0x0238;
    L_0x02e3:
        r0 = r18;	 Catch:{ IOException -> 0x02f3 }
        r2 = r0 instanceof android.content.res.XmlResourceParser;	 Catch:{ IOException -> 0x02f3 }
        if (r2 == 0) goto L_0x0238;	 Catch:{ IOException -> 0x02f3 }
    L_0x02e9:
        r0 = r18;	 Catch:{ IOException -> 0x02f3 }
        r0 = (android.content.res.XmlResourceParser) r0;	 Catch:{ IOException -> 0x02f3 }
        r2 = r0;	 Catch:{ IOException -> 0x02f3 }
        r2.close();	 Catch:{ IOException -> 0x02f3 }
        goto L_0x0238;
    L_0x02f3:
        r13 = move-exception;
        r2 = "PolicyInitialization";
        r22 = "Close Input stream error!";
        r0 = r22;
        android.util.Log.w(r2, r0);
        goto L_0x0238;
    L_0x0301:
        r2 = move-exception;
    L_0x0302:
        if (r16 == 0) goto L_0x030b;
    L_0x0304:
        r16.close();	 Catch:{ IOException -> 0x031b }
    L_0x0307:
        com.huawei.powergenie.core.policy.PolicyProvider.endTransaction();
        throw r2;
    L_0x030b:
        if (r18 == 0) goto L_0x0307;
    L_0x030d:
        r0 = r18;	 Catch:{ IOException -> 0x031b }
        r0 = r0 instanceof android.content.res.XmlResourceParser;	 Catch:{ IOException -> 0x031b }
        r22 = r0;	 Catch:{ IOException -> 0x031b }
        if (r22 == 0) goto L_0x0307;	 Catch:{ IOException -> 0x031b }
    L_0x0315:
        r18 = (android.content.res.XmlResourceParser) r18;	 Catch:{ IOException -> 0x031b }
        r18.close();	 Catch:{ IOException -> 0x031b }
        goto L_0x0307;
    L_0x031b:
        r13 = move-exception;
        r22 = "PolicyInitialization";
        r23 = "Close Input stream error!";
        android.util.Log.w(r22, r23);
        goto L_0x0307;
    L_0x0326:
        r2 = move-exception;
        r16 = r17;
        goto L_0x0302;
    L_0x032a:
        r11 = move-exception;
        r16 = r17;
        goto L_0x0230;
    L_0x032f:
        r15 = move-exception;
        r16 = r17;
        goto L_0x02b6;
    L_0x0333:
        r14 = move-exception;
        r16 = r17;
        goto L_0x028a;
    L_0x0338:
        r13 = move-exception;
        r16 = r17;
        goto L_0x025e;
    L_0x033d:
        r12 = move-exception;
        goto L_0x00d9;
    L_0x0340:
        r12 = move-exception;
        goto L_0x008e;
    L_0x0343:
        r12 = move-exception;
        r16 = r17;
        goto L_0x0046;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.powergenie.core.policy.PolicyInitialization.initSysLoad(android.content.Context, com.huawei.powergenie.core.policy.DBWrapper):boolean");
    }

    private static InputStream getCpuPolicyStream(String modeStr) {
        try {
            return new FileInputStream(modeStr);
        } catch (FileNotFoundException e) {
            Log.w("PolicyInitialization", "cpu policy :" + modeStr + " not found");
            return null;
        }
    }

    private static InputStream getCpuPolicyStreamCrypt(String modeStr) {
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream inStream = new FileInputStream(modeStr);
            try {
                InputStream inStreamDecode = DecodeXmlFile.getDecodeInputStream(inStream);
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return inStreamDecode;
            } catch (FileNotFoundException e2) {
                inputStream = inStream;
                Log.w("PolicyInitialization", "crypt xml :" + modeStr + " not found");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                return null;
            } catch (Exception e4) {
                inputStream = inStream;
                try {
                    mIsCrypt = false;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e322) {
                            e322.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = inStream;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            Log.w("PolicyInitialization", "crypt xml :" + modeStr + " not found");
            if (inputStream != null) {
                inputStream.close();
            }
            return null;
        } catch (Exception e6) {
            mIsCrypt = false;
            if (inputStream != null) {
                inputStream.close();
            }
            return null;
        }
    }

    private static InputStream setParserInputStream(String modeStr, XmlPullParser parser) {
        InputStream inputStream = null;
        try {
            mIsCrypt = true;
            inputStream = getCpuPolicyStreamCrypt(modeStr);
            if (!mIsCrypt) {
                inputStream = getCpuPolicyStream(modeStr);
            }
            if (inputStream != null) {
                parser.setInput(inputStream, "UTF-8");
            }
            if (true || inputStream == null) {
                return inputStream;
            }
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
            if (null != null || inputStream == null) {
                return inputStream;
            }
            try {
                inputStream.close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
            if (null != null || inputStream == null) {
                return inputStream;
            }
            try {
                inputStream.close();
            } catch (Exception e32) {
                e32.printStackTrace();
            }
        } catch (Throwable th) {
            if (null == null && inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e322) {
                    e322.printStackTrace();
                }
            }
        }
    }

    private static boolean initCpuPolicy(Context context, DBWrapper dbWrapper) {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        XmlPullParser parser = Xml.newPullParser();
        InputStream inStream = setParserInputStream(CUSTDIR_SUPER_CPU_POLICY, parser);
        if (inStream != null) {
            z = loadCpuPolicyFromXML(1, parser, dbWrapper);
            try {
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        inStream = setParserInputStream(CUSTDIR_NORMAL_CPU_POLICY, parser);
        if (inStream != null) {
            z2 = loadCpuPolicyFromXML(2, parser, dbWrapper);
            try {
                inStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        inStream = setParserInputStream(CUSTDIR_PERFORMANCE_CPU_POLICY, parser);
        if (inStream != null) {
            z4 = loadCpuPolicyFromXML(3, parser, dbWrapper);
            try {
                inStream.close();
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
        inStream = setParserInputStream(CUSTDIR_EXTREME_CPU_POLICY, parser);
        if (inStream != null) {
            z3 = loadCpuPolicyFromXML(4, parser, dbWrapper);
            try {
                inStream.close();
            } catch (Exception e222) {
                e222.printStackTrace();
            }
        }
        inStream = setParserInputStream(CUSTDIR_VR_CPU_POLICY, parser);
        if (inStream != null) {
            loadCpuPolicyFromXML(99, parser, dbWrapper);
            try {
                inStream.close();
            } catch (Exception e2222) {
                e2222.printStackTrace();
            }
        }
        if (z && z2 && z4 && z3) {
            return true;
        }
        if (NativeAdapter.getPlatformType() == 2) {
            Log.e("PolicyInitialization", "v9r1 platform cpu freq is not supported!");
            return true;
        }
        if (NativeAdapter.getPlatformType() == 0) {
            inStream = XmlHelper.setParserAssetsInputStream(context, "msm_cpu_policy.xml", parser);
        } else if (NativeAdapter.getPlatformType() == 3) {
            inStream = XmlHelper.setParserAssetsInputStream(context, "k3v3_cpu_policy.xml", parser);
        }
        if (inStream == null && parser == null) {
            Log.e("PolicyInitialization", "There is no any cpu policy!");
            return false;
        }
        if (!z) {
            loadCpuPolicyFromXML(1, parser, dbWrapper);
        }
        if (!z2) {
            if (inStream != null) {
                try {
                    inStream.reset();
                    parser.setInput(inStream, "UTF-8");
                } catch (XmlPullParserException e3) {
                    Log.e("PolicyInitialization", "Fail to get XmlPullParser!");
                } catch (IOException oe) {
                    Log.e("PolicyInitialization", "IOException", oe);
                }
            }
            loadCpuPolicyFromXML(2, parser, dbWrapper);
        }
        if (!z4) {
            if (inStream != null) {
                try {
                    inStream.reset();
                    parser.setInput(inStream, "UTF-8");
                } catch (XmlPullParserException e4) {
                    Log.e("PolicyInitialization", "Fail to get XmlPullParser!");
                } catch (IOException oe2) {
                    Log.e("PolicyInitialization", "IOException", oe2);
                }
            }
            loadCpuPolicyFromXML(3, parser, dbWrapper);
        }
        if (!z3) {
            if (inStream != null) {
                try {
                    inStream.reset();
                    parser.setInput(inStream, "UTF-8");
                } catch (XmlPullParserException oe3) {
                    Log.e("PolicyInitialization", "Fail to get XmlPullParser!", oe3);
                } catch (IOException oe22) {
                    Log.e("PolicyInitialization", "IOException", oe22);
                }
            }
            loadCpuPolicyFromXML(4, parser, dbWrapper);
        }
        if (inStream != null) {
            try {
                inStream.close();
            } catch (Exception e22222) {
                e22222.printStackTrace();
            }
        } else if (parser != null && (parser instanceof XmlResourceParser)) {
            ((XmlResourceParser) parser).close();
        }
        return true;
    }

    private static void addCpuPolicy(DBWrapper dbWrapper, int actionId, int powerMode, int policy, int value, String pkg, String extend) {
        dbWrapper.addCpuPolicy(actionId, powerMode, policy, value, pkg, extend);
    }

    private static boolean loadCpuPolicyFromXML(int powerMode, XmlPullParser parser, DBWrapper dbWrapper) {
        boolean ret = false;
        if (parser == null) {
            Log.w("PolicyInitialization", "There is no any cpu_policy file!");
            return false;
        }
        PolicyProvider.beginTransaction();
        try {
            XmlHelper.beginDocument(parser, "cpu_policy");
            int mode = 0;
            int actionId = 0;
            int policyType = 0;
            int policyValue = -1;
            String pkgName = null;
            String extend = null;
            boolean isSceneModeTag = false;
            while (true) {
                XmlHelper.nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                } else if ("mode".equals(tag)) {
                    if (actionId != 0 && (isSceneModeTag || mode == 0)) {
                        addCpuPolicy(dbWrapper, actionId, powerMode, policyType, policyValue, pkgName, extend);
                        isSceneModeTag = false;
                    }
                    mode = Integer.parseInt(parser.getAttributeValue(0));
                } else if ("scenemode".equals(tag)) {
                    extend = null;
                    if (mode == 0 || mode == powerMode) {
                        if (isSceneModeTag && actionId != 0) {
                            addCpuPolicy(dbWrapper, actionId, powerMode, policyType, policyValue, pkgName, null);
                        }
                        actionId = Integer.parseInt(parser.getAttributeValue(1));
                        pkgName = null;
                        if (actionId >= 10000) {
                            pkgName = parser.getAttributeValue(0);
                        }
                        isSceneModeTag = true;
                    } else {
                        isSceneModeTag = false;
                        actionId = 0;
                    }
                } else if ("comb_scene".equals(tag)) {
                    if (mode == 0 || mode == powerMode) {
                        if (isSceneModeTag && actionId != 0) {
                            addCpuPolicy(dbWrapper, actionId, powerMode, policyType, policyValue, pkgName, extend);
                        }
                        actionId = Integer.parseInt(parser.getAttributeValue(1));
                        int parentActionId = Integer.parseInt(parser.getAttributeValue(2));
                        extend = null;
                        if (parentActionId >= 0) {
                            extend = Integer.toString(parentActionId);
                        }
                        pkgName = null;
                        if (parentActionId == 0) {
                            pkgName = parser.getAttributeValue(0);
                        }
                        isSceneModeTag = true;
                    } else {
                        isSceneModeTag = false;
                        actionId = 0;
                    }
                } else if (mTagNameToTypeId.get(tag) != null) {
                    policyType = ((Integer) mTagNameToTypeId.get(tag)).intValue();
                    if (parser.next() == 4) {
                        policyValue = Integer.parseInt(parser.getText());
                    }
                    if (mode == 0 || mode == powerMode) {
                        addCpuPolicy(dbWrapper, actionId, powerMode, policyType, policyValue, pkgName, extend);
                    }
                    policyType = 0;
                    policyValue = -1;
                    isSceneModeTag = false;
                } else {
                    Log.e("PolicyInitialization", "tag: " + tag + " is unknown. ");
                }
            }
            if (isSceneModeTag && actionId != 0 && (mode == 0 || mode == powerMode)) {
                addCpuPolicy(dbWrapper, actionId, powerMode, policyType, policyValue, pkgName, extend);
            }
            ret = true;
            PolicyProvider.setTransactionSuccessful();
            PolicyProvider.endTransaction();
        } catch (NotFoundException e) {
            e.printStackTrace();
            PolicyProvider.endTransaction();
        } catch (XmlPullParserException e2) {
            Log.e("PolicyInitialization", "decode Policy crypt xml exception:", e2);
            throw new RuntimeException("Decode PowerGenie Policy Xml Exception, PowerMode:" + powerMode);
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
            PolicyProvider.endTransaction();
        } catch (IOException e4) {
            e4.printStackTrace();
            PolicyProvider.endTransaction();
        } catch (Throwable th) {
            PolicyProvider.endTransaction();
        }
        return ret;
    }

    private static boolean initBacklightPolicy(Context context, DBWrapper dbWrapper) {
        InputStream inStream;
        NotFoundException e;
        XmlPullParserException e2;
        NumberFormatException e3;
        IOException e4;
        Throwable th;
        boolean ret = false;
        XmlPullParser parser = null;
        InputStream inputStream = null;
        try {
            if (new File(CUSTDIR_BACKLIGHT_POLICY).exists()) {
                InputStream inputStream2 = null;
                try {
                    InputStream inStreamDecode = new FileInputStream(CUSTDIR_BACKLIGHT_POLICY);
                    try {
                        inputStream = DecodeXmlFile.getDecodeInputStream(inStreamDecode);
                        Log.d("PolicyInitialization", "crypt xml :" + CUSTDIR_BACKLIGHT_POLICY);
                        inputStream2 = inStreamDecode;
                    } catch (Exception e5) {
                        inputStream2 = inStreamDecode;
                        inStream = new FileInputStream(CUSTDIR_BACKLIGHT_POLICY);
                        try {
                            Log.d("PolicyInitialization", "uncrypt xml :" + CUSTDIR_BACKLIGHT_POLICY);
                            inputStream = inStream;
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            parser = Xml.newPullParser();
                            parser.setInput(inputStream, "UTF-8");
                            if (parser == null) {
                                ret = loadBacklightPolicy(parser, dbWrapper);
                                if (inputStream != null) {
                                    ((XmlResourceParser) parser).close();
                                } else {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e6) {
                                        Log.w("PolicyInitialization", "Close Input stream error!");
                                    }
                                }
                                return ret;
                            }
                            Log.e("PolicyInitialization", "There is no any backlight policy file!");
                            if (inputStream == null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e7) {
                                    Log.w("PolicyInitialization", "Close Input stream error!");
                                }
                            } else if (parser != null) {
                                if (parser instanceof XmlResourceParser) {
                                    ((XmlResourceParser) parser).close();
                                }
                            }
                            return false;
                        } catch (NotFoundException e8) {
                            e = e8;
                            inputStream = inStream;
                            e.printStackTrace();
                            if (inputStream == null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e9) {
                                    Log.w("PolicyInitialization", "Close Input stream error!");
                                }
                                return ret;
                            }
                            if (parser != null) {
                                if (parser instanceof XmlResourceParser) {
                                    ((XmlResourceParser) parser).close();
                                }
                            }
                            return ret;
                        } catch (XmlPullParserException e10) {
                            e2 = e10;
                            inputStream = inStream;
                            e2.printStackTrace();
                            if (inputStream == null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e11) {
                                    Log.w("PolicyInitialization", "Close Input stream error!");
                                }
                                return ret;
                            }
                            if (parser != null) {
                                if (parser instanceof XmlResourceParser) {
                                    ((XmlResourceParser) parser).close();
                                }
                            }
                            return ret;
                        } catch (NumberFormatException e12) {
                            e3 = e12;
                            inputStream = inStream;
                            e3.printStackTrace();
                            if (inputStream == null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e13) {
                                    Log.w("PolicyInitialization", "Close Input stream error!");
                                }
                                return ret;
                            }
                            if (parser != null) {
                                if (parser instanceof XmlResourceParser) {
                                    ((XmlResourceParser) parser).close();
                                }
                            }
                            return ret;
                        } catch (IOException e14) {
                            e4 = e14;
                            inputStream = inStream;
                            try {
                                e4.printStackTrace();
                                if (inputStream == null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e15) {
                                        Log.w("PolicyInitialization", "Close Input stream error!");
                                    }
                                    return ret;
                                }
                                if (parser != null) {
                                    if (parser instanceof XmlResourceParser) {
                                        ((XmlResourceParser) parser).close();
                                    }
                                }
                                return ret;
                            } catch (Throwable th2) {
                                th = th2;
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e16) {
                                        Log.w("PolicyInitialization", "Close Input stream error!");
                                    }
                                } else if (parser != null) {
                                    if (parser instanceof XmlResourceParser) {
                                        ((XmlResourceParser) parser).close();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStream = inStream;
                            if (inputStream != null) {
                                inputStream.close();
                            } else if (parser != null) {
                                if (parser instanceof XmlResourceParser) {
                                    ((XmlResourceParser) parser).close();
                                }
                            }
                            throw th;
                        }
                    }
                } catch (Exception e17) {
                    inStream = new FileInputStream(CUSTDIR_BACKLIGHT_POLICY);
                    Log.d("PolicyInitialization", "uncrypt xml :" + CUSTDIR_BACKLIGHT_POLICY);
                    inputStream = inStream;
                    if (inputStream2 != null) {
                        inputStream2.close();
                    }
                    parser = Xml.newPullParser();
                    parser.setInput(inputStream, "UTF-8");
                    if (parser == null) {
                        Log.e("PolicyInitialization", "There is no any backlight policy file!");
                        if (inputStream == null) {
                            inputStream.close();
                        } else if (parser != null) {
                            if (parser instanceof XmlResourceParser) {
                                ((XmlResourceParser) parser).close();
                            }
                        }
                        return false;
                    }
                    ret = loadBacklightPolicy(parser, dbWrapper);
                    if (inputStream != null) {
                        inputStream.close();
                    } else {
                        ((XmlResourceParser) parser).close();
                    }
                    return ret;
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                parser = Xml.newPullParser();
                parser.setInput(inputStream, "UTF-8");
            } else {
                Log.d("PolicyInitialization", "no cust backlight_xml, load xml from pg.");
                parser = context.getResources().getXml(R.xml.backlight_policy);
            }
            if (parser == null) {
                Log.e("PolicyInitialization", "There is no any backlight policy file!");
                if (inputStream == null) {
                    inputStream.close();
                } else if (parser != null) {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                }
                return false;
            }
            ret = loadBacklightPolicy(parser, dbWrapper);
            if (inputStream != null) {
                inputStream.close();
            } else if (parser != null && (parser instanceof XmlResourceParser)) {
                ((XmlResourceParser) parser).close();
            }
            return ret;
        } catch (NotFoundException e18) {
            e = e18;
            e.printStackTrace();
            if (inputStream == null) {
                if (parser != null) {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                }
                return ret;
            }
            inputStream.close();
            return ret;
        } catch (XmlPullParserException e19) {
            e2 = e19;
            e2.printStackTrace();
            if (inputStream == null) {
                if (parser != null) {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                }
                return ret;
            }
            inputStream.close();
            return ret;
        } catch (NumberFormatException e20) {
            e3 = e20;
            e3.printStackTrace();
            if (inputStream == null) {
                if (parser != null) {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                }
                return ret;
            }
            inputStream.close();
            return ret;
        } catch (IOException e21) {
            e4 = e21;
            e4.printStackTrace();
            if (inputStream == null) {
                if (parser != null) {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                }
                return ret;
            }
            inputStream.close();
            return ret;
        }
    }

    private static boolean loadBacklightPolicy(XmlPullParser parser, DBWrapper dbWrapper) {
        boolean ret = false;
        if (parser == null) {
            Log.e("PolicyInitialization", "There is no any backlight policy file!");
            return false;
        }
        PolicyProvider.beginTransaction();
        try {
            XmlHelper.beginDocument(parser, "backlight_policy");
            int mode = 0;
            int actionId = 0;
            int policyRatio = 0;
            int ratioValue = 0;
            int policyAuto = 0;
            int autoValue = 0;
            ArrayList<String> pkgList = new ArrayList();
            while (true) {
                XmlHelper.nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                } else if ("mode".equals(tag)) {
                    if (actionId != 0) {
                        if (pkgList.isEmpty()) {
                            if (policyAuto == 2) {
                                dbWrapper.addBacklightPolicy(actionId, mode, policyAuto, autoValue, null);
                            }
                            if (policyRatio == 1) {
                                dbWrapper.addBacklightPolicy(actionId, mode, policyRatio, ratioValue, null);
                            }
                        } else {
                            for (String pkg : pkgList) {
                                if (policyAuto == 2) {
                                    dbWrapper.addBacklightPolicy(actionId, mode, policyAuto, autoValue, pkg);
                                }
                                if (policyRatio == 1) {
                                    dbWrapper.addBacklightPolicy(actionId, mode, policyRatio, ratioValue, pkg);
                                }
                            }
                        }
                    }
                    mode = Integer.parseInt(parser.getAttributeValue(0));
                    actionId = 0;
                } else if ("action".equals(tag)) {
                    if (actionId != 0) {
                        if (pkgList.isEmpty()) {
                            if (policyAuto == 2) {
                                dbWrapper.addBacklightPolicy(actionId, mode, policyAuto, autoValue, null);
                            }
                            if (policyRatio == 1) {
                                dbWrapper.addBacklightPolicy(actionId, mode, policyRatio, ratioValue, null);
                            }
                        } else {
                            for (String pkg2 : pkgList) {
                                if (policyAuto == 2) {
                                    dbWrapper.addBacklightPolicy(actionId, mode, policyAuto, autoValue, pkg2);
                                }
                                if (policyRatio == 1) {
                                    dbWrapper.addBacklightPolicy(actionId, mode, policyRatio, ratioValue, pkg2);
                                }
                            }
                        }
                    }
                    policyAuto = 0;
                    autoValue = 0;
                    policyRatio = 0;
                    ratioValue = 0;
                    pkgList.clear();
                    actionId = Integer.parseInt(parser.getAttributeValue(1));
                } else if ("item".equals(tag)) {
                    String attrName = parser.getAttributeValue(0);
                    String text = null;
                    if (parser.next() == 4) {
                        text = parser.getText();
                    }
                    if (text == null) {
                        continue;
                    } else if ("policy_auto".equals(attrName)) {
                        policyAuto = 2;
                        autoValue = Integer.parseInt(text);
                    } else if ("policy_ratio".equals(attrName)) {
                        policyRatio = 1;
                        ratioValue = Integer.parseInt(text);
                    } else if ("package_name".equals(attrName)) {
                        pkgList.add(text);
                    } else {
                        Log.e("PolicyInitialization", "atrr: " + attrName + " is unknown. ");
                    }
                } else {
                    Log.e("PolicyInitialization", "tag: " + tag + " is unknown. ");
                }
            }
            if (actionId != 0) {
                if (pkgList.isEmpty()) {
                    if (policyAuto == 2) {
                        dbWrapper.addBacklightPolicy(actionId, mode, policyAuto, autoValue, null);
                    }
                    if (policyRatio == 1) {
                        dbWrapper.addBacklightPolicy(actionId, mode, policyRatio, ratioValue, null);
                    }
                } else {
                    for (String pkg22 : pkgList) {
                        if (policyAuto == 2) {
                            dbWrapper.addBacklightPolicy(actionId, mode, policyAuto, autoValue, pkg22);
                        }
                        if (policyRatio == 1) {
                            dbWrapper.addBacklightPolicy(actionId, mode, policyRatio, ratioValue, pkg22);
                        }
                    }
                }
            }
            ret = true;
            PolicyProvider.setTransactionSuccessful();
            if (parser != null) {
                try {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                } catch (Exception e) {
                    Log.w("PolicyInitialization", "Close Input stream error!");
                }
            }
            PolicyProvider.endTransaction();
        } catch (NotFoundException e2) {
            e2.printStackTrace();
            if (parser != null) {
                try {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                } catch (Exception e3) {
                    Log.w("PolicyInitialization", "Close Input stream error!");
                }
            }
            PolicyProvider.endTransaction();
        } catch (XmlPullParserException e4) {
            e4.printStackTrace();
            if (parser != null) {
                try {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                } catch (Exception e5) {
                    Log.w("PolicyInitialization", "Close Input stream error!");
                }
            }
            PolicyProvider.endTransaction();
        } catch (NumberFormatException e6) {
            e6.printStackTrace();
            if (parser != null) {
                try {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                } catch (Exception e7) {
                    Log.w("PolicyInitialization", "Close Input stream error!");
                }
            }
            PolicyProvider.endTransaction();
        } catch (IOException e8) {
            e8.printStackTrace();
            if (parser != null) {
                try {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                } catch (Exception e9) {
                    Log.w("PolicyInitialization", "Close Input stream error!");
                }
            }
            PolicyProvider.endTransaction();
        } catch (Throwable th) {
            if (parser != null) {
                try {
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                } catch (Exception e10) {
                    Log.w("PolicyInitialization", "Close Input stream error!");
                }
            }
            PolicyProvider.endTransaction();
        }
        return ret;
    }
}
