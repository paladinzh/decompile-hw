package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class AppCloneUtils {
    private static String CONFIG_FILE_PATH = "xml/hw_clone_app_list.xml";
    private static long LAST_MODIFIED_TIME = 0;
    private static String THEME_ICON_PATH = "/data/skin/";
    private static String THEME_ICON_SYSTEM_PATH = "/system/themes/";

    public static boolean isCloneProfileExisted(Context context) {
        for (UserInfo userInfo : UserManager.get(context).getUsers(true)) {
            if (userInfo.isClonedProfile()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isClonedProfile(Context context, int userId) {
        UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(userId);
        return userInfo != null ? userInfo.isClonedProfile() : false;
    }

    public static int getClonedProfileUserId(Context context) {
        for (UserInfo userInfo : UserManager.get(context).getUsers(true)) {
            if (userInfo.isClonedProfile()) {
                return userInfo.id;
            }
        }
        return -1000;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Map<String, String> getXmlFormCust() {
        Exception e;
        FileNotFoundException e2;
        XmlPullParserException e3;
        Throwable th;
        XmlPullParser xmlPullParser = null;
        Map<String, String> xmlpackages = new HashMap();
        File file = HwCfgFilePolicy.getCfgFile(CONFIG_FILE_PATH, 0);
        if (file == null || !file.exists()) {
            return null;
        }
        InputStream in;
        InputStream inputStream = null;
        try {
            in = new FileInputStream(file);
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(in, "utf-8");
                while (true) {
                    int xmlEventType = xmlPullParser.next();
                    if (xmlEventType == 1) {
                        break;
                    } else if (xmlEventType == 2 && "package".equals(xmlPullParser.getName())) {
                        String mPackage = xmlPullParser.getAttributeValue(null, "name");
                        String type = xmlPullParser.getAttributeValue(null, "type");
                        if (!"".equals(mPackage) && mPackage != null) {
                            if (type != null) {
                                if (!"".equals(type)) {
                                    xmlpackages.put(mPackage, type);
                                }
                            }
                            xmlpackages.put(mPackage, "0");
                        } else if (xmlPullParser != null) {
                            try {
                                ((KXmlParser) xmlPullParser).close();
                            } catch (Exception e4) {
                                Log.e("AppCloneUtils", "close XmlPullParser error, error msg: " + e4.getMessage());
                                e4.printStackTrace();
                            }
                        }
                    }
                }
                if (xmlPullParser != null) {
                    ((KXmlParser) xmlPullParser).close();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        Log.e("AppCloneUtils", "close FileInputStram error, error msg: " + e5.getMessage());
                        e5.printStackTrace();
                    }
                }
                return xmlpackages;
            } catch (FileNotFoundException e6) {
                e2 = e6;
                inputStream = in;
            } catch (XmlPullParserException e7) {
                e3 = e7;
                inputStream = in;
            } catch (Exception e8) {
                e4 = e8;
                inputStream = in;
            } catch (Throwable th2) {
                th = th2;
                inputStream = in;
            }
        } catch (FileNotFoundException e9) {
            e2 = e9;
            try {
                Log.w("AppCloneUtils", "Error FileNotFound while trying to read from hw_clone_app_list", e2);
                if (xmlPullParser != null) {
                    try {
                        ((KXmlParser) xmlPullParser).close();
                    } catch (Exception e42) {
                        Log.e("AppCloneUtils", "close XmlPullParser error, error msg: " + e42.getMessage());
                        e42.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e52) {
                        Log.e("AppCloneUtils", "close FileInputStram error, error msg: " + e52.getMessage());
                        e52.printStackTrace();
                    }
                }
                return xmlpackages;
            } catch (Throwable th3) {
                th = th3;
                if (xmlPullParser != null) {
                    try {
                        ((KXmlParser) xmlPullParser).close();
                    } catch (Exception e422) {
                        Log.e("AppCloneUtils", "close XmlPullParser error, error msg: " + e422.getMessage());
                        e422.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e522) {
                        Log.e("AppCloneUtils", "close FileInputStram error, error msg: " + e522.getMessage());
                        e522.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e10) {
            e3 = e10;
            Log.e("AppCloneUtils", "Error XmlPullParser while trying to read from hw_clone_app_list", e3);
            if (xmlPullParser != null) {
                try {
                    ((KXmlParser) xmlPullParser).close();
                } catch (Exception e4222) {
                    Log.e("AppCloneUtils", "close XmlPullParser error, error msg: " + e4222.getMessage());
                    e4222.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5222) {
                    Log.e("AppCloneUtils", "close FileInputStram error, error msg: " + e5222.getMessage());
                    e5222.printStackTrace();
                }
            }
            return xmlpackages;
        } catch (Exception e11) {
            e4222 = e11;
            Log.e("AppCloneUtils", "Error while trying to read from hw_clone_app_list", e4222);
            if (xmlPullParser != null) {
                try {
                    ((KXmlParser) xmlPullParser).close();
                } catch (Exception e42222) {
                    Log.e("AppCloneUtils", "close XmlPullParser error, error msg: " + e42222.getMessage());
                    e42222.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e52222) {
                    Log.e("AppCloneUtils", "close FileInputStram error, error msg: " + e52222.getMessage());
                    e52222.printStackTrace();
                }
            }
            return xmlpackages;
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e522222) {
                Log.e("AppCloneUtils", "close FileInputStram error, error msg: " + e522222.getMessage());
                e522222.printStackTrace();
            }
        }
        return xmlpackages;
        return xmlpackages;
    }

    public static void initAppCloneXml(Context context) {
        long modifiedTime = context.getSharedPreferences("com.android.settings_appclone_xml_modifiedTime", 0).getLong("time", -1);
        if (modifiedTime == -1 || modifiedTime != LAST_MODIFIED_TIME) {
            saveXmlToSharedPreferences(context, getXmlFormCust());
        }
    }

    public static void saveXmlToSharedPreferences(Context context, Map<String, String> apps) {
        if (apps != null && apps.size() != 0) {
            Editor editor = context.getSharedPreferences("com.android.settings_appclone", 0).edit();
            for (Entry<String, String> entry : apps.entrySet()) {
                if (entry != null) {
                    editor.putString((String) entry.getKey(), (String) entry.getValue());
                }
            }
            editor.commit();
            Editor TimeEditor = context.getSharedPreferences("com.android.settings_appclone_xml_modifiedTime", 0).edit();
            TimeEditor.putLong("time", LAST_MODIFIED_TIME);
            TimeEditor.commit();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean hasAppCloneCust() {
        File file = HwCfgFilePolicy.getCfgFile(CONFIG_FILE_PATH, 0);
        Map<String, String> xmlpackages = getXmlFormCust();
        if (xmlpackages == null || file == null || !file.exists() || xmlpackages.isEmpty()) {
            return false;
        }
        LAST_MODIFIED_TIME = file.lastModified();
        return true;
    }

    public static boolean isSupportAppClone() {
        return SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    }

    public static Drawable getPackageIcon(PackageManager manager, String nPackageName) {
        try {
            return manager.getPackageInfo(nPackageName, 1).applicationInfo.loadIcon(manager);
        } catch (NameNotFoundException e) {
            Log.e("AppCloneUtils", "not found " + nPackageName);
            return null;
        }
    }

    public static String getPackageLabel(PackageManager manager, String nPackageName) {
        try {
            return manager.getPackageInfo(nPackageName, 1).applicationInfo.loadLabel(manager).toString();
        } catch (NameNotFoundException e) {
            Log.e("AppCloneUtils", "not found " + nPackageName);
            return "";
        }
    }

    public static List<AppInfo> getAppListFormUser(Context context, int userId) {
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.LAUNCHER");
        List<AppInfo> userAppList = new ArrayList();
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivitiesAsUser(launcherIntent, 0, userId);
        if (resolveInfos == null) {
            return userAppList;
        }
        Map<String, String> xmlpackages = getXmlFormCust();
        Set<String> containedPackage = new HashSet();
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (!containedPackage.contains(packageName) && xmlpackages.containsKey(packageName)) {
                userAppList.add(new AppInfo(false, resolveInfo.activityInfo.applicationInfo.loadIcon(packageManager), packageName, resolveInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString(), context.getResources().getString(2131628558)));
                containedPackage.add(packageName);
            }
        }
        return userAppList;
    }

    public static List<AppInfo> getAppListWithoutdefault(Context context) {
        UserManager.get(context).getUserProfiles().get(0);
        List<AppInfo> userApps = getAppListFormUser(context, UserHandle.myUserId());
        List<AppInfo> otherApps = new ArrayList();
        Set<String> mOtherApps = getOtherAppListFormXml(context);
        if (mOtherApps.size() == 0 || userApps.size() == 0) {
            return otherApps;
        }
        return getAppListFromUserApps(userApps, mOtherApps, context);
    }

    public static List<AppInfo> getAppListFromUserApps(List<AppInfo> userApps, Set<String> apps, Context context) {
        List<AppInfo> appList = new ArrayList();
        for (int i = 0; i < userApps.size(); i++) {
            String userPackageName = ((AppInfo) userApps.get(i)).getmPackageName();
            for (String packageName : apps) {
                if (userPackageName.equals(packageName)) {
                    appList.add(new AppInfo(false, getPackageIcon(context.getPackageManager(), userPackageName), userPackageName, getPackageLabel(context.getPackageManager(), userPackageName), context.getResources().getString(2131628558)));
                }
            }
        }
        return appList;
    }

    public static List<AppInfo> getDefaultAppList(Context context) {
        UserManager.get(context).getUserProfiles().get(0);
        List<AppInfo> userApps = getAppListFormUser(context, UserHandle.myUserId());
        List<AppInfo> defaultApps = new ArrayList();
        Set<String> mDefaultApps = getDefaultAppListFormXml(context);
        if (mDefaultApps.size() == 0 || userApps.size() == 0) {
            return defaultApps;
        }
        return getAppListFromUserApps(userApps, mDefaultApps, context);
    }

    public static Set<String> getDefaultAppListFormXml(Context context) {
        Map<String, String> xmlpackages = context.getSharedPreferences("com.android.settings_appclone", 0).getAll();
        Set<String> defaultsAppClone = new HashSet();
        if (xmlpackages == null) {
            return defaultsAppClone;
        }
        for (Entry<String, String> entry : xmlpackages.entrySet()) {
            if (entry != null && ((String) entry.getValue()).equals("0")) {
                defaultsAppClone.add((String) entry.getKey());
            }
        }
        return defaultsAppClone;
    }

    public static Set<String> getOtherAppListFormXml(Context context) {
        Map<String, String> xmlpackages = context.getSharedPreferences("com.android.settings_appclone", 0).getAll();
        Set<String> otherAppClone = new HashSet();
        if (xmlpackages == null) {
            return otherAppClone;
        }
        for (Entry<String, String> entry : xmlpackages.entrySet()) {
            if (!"0".equals(entry.getValue())) {
                otherAppClone.add((String) entry.getKey());
            }
        }
        return otherAppClone;
    }
}
