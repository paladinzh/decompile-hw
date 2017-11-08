package com.huawei.mms.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.Xml;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.MmsSystemEventReceiver;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SmartArchiveSettingUtils {
    private static final Object[][] SMART_ARCHIVE_SETTINGS_CONF;
    private static int sAutoDeleteAtHourOfDay = 3;
    private static int sAutoDeleteRepeatDuration = 1;
    private static int sAutoDeleteTimeDuration = 30;
    private static ArrayList<SmartArchiveSettingItem> sDefaultSmartArchiveSettings = new ArrayList();
    private static final HwCustSmartArchiveSettingUtils sHwCustSmartArchiveSettingUtils = ((HwCustSmartArchiveSettingUtils) HwCustUtils.createObj(HwCustSmartArchiveSettingUtils.class, new Object[0]));
    private static boolean sIsHwNotiReceived = false;
    private static int sSmartArchiveConfigVersion = 8;
    private static boolean sSmartArchiveEnabledDefault = true;
    private static ArrayList<SmartArchiveSettingItem> sSmartArchiveSettingItems = new ArrayList();

    public static class SmartArchiveSettingItem {
        private ArrayList<String> mArchiveNumberOtherPrefixes;
        private ArrayList<String> mArchiveNumberPrefixes;
        private boolean mEnabled;
        private String mKey;
        private String mSummary;
        private int mSummaryId;
        private String mTitle;
        private int mTitleId;
        private int mType;

        public SmartArchiveSettingItem(String key, int type, String[] prefixs, int titleId, int summaryId, boolean enabled) {
            this.mKey = key;
            this.mType = type;
            this.mArchiveNumberPrefixes = new ArrayList();
            this.mArchiveNumberOtherPrefixes = new ArrayList();
            if (prefixs != null) {
                for (String prefix : prefixs) {
                    this.mArchiveNumberPrefixes.add(prefix);
                }
            }
            this.mTitleId = titleId;
            this.mSummaryId = summaryId;
            this.mEnabled = enabled;
        }

        public SmartArchiveSettingItem(SmartArchiveSettingItem item) {
            this.mKey = item.getKey();
            this.mType = item.getType();
            this.mArchiveNumberPrefixes = item.getArchiveNumberPrefixes();
            this.mArchiveNumberOtherPrefixes = item.getArchiveNumberOtherPrefixes();
            this.mTitle = item.mTitle;
            this.mTitleId = item.mTitleId;
            this.mSummary = item.mSummary;
            this.mSummaryId = item.mSummaryId;
            this.mEnabled = item.ismEnabled();
        }

        public void setArchiveNumberOtherPrefixes(String[] prefixs) {
            if (prefixs != null) {
                for (String prefix : prefixs) {
                    this.mArchiveNumberOtherPrefixes.add(prefix);
                }
            }
        }

        public ArrayList<String> getArchiveNumberOtherPrefixes() {
            return this.mArchiveNumberOtherPrefixes;
        }

        public String toString() {
            return new StringBuffer().append("mKey:").append(this.mKey).append(", mType:").append(this.mType).append(", mArchiveNumberPrefixes:").append(this.mArchiveNumberPrefixes).append(", mTitle:").append(this.mTitle).append(", mSummary:").append(this.mSummary).append(", mEnabled:").append(this.mEnabled).toString();
        }

        public String getKey() {
            return this.mKey;
        }

        public void setKey(String key) {
            this.mKey = key;
        }

        public int getType() {
            return this.mType;
        }

        public void setType(int type) {
            this.mType = type;
        }

        public ArrayList<String> getArchiveNumberPrefixes() {
            return this.mArchiveNumberPrefixes;
        }

        public void addArchiveNumberPrefixes(String prefix) {
            this.mArchiveNumberPrefixes.add(prefix);
        }

        public String getTitle() {
            if (TextUtils.isEmpty(this.mTitle)) {
                return MmsApp.getApplication().getString(this.mTitleId);
            }
            return this.mTitle;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public String getSummary() {
            if (TextUtils.isEmpty(this.mSummary)) {
                return MmsApp.getApplication().getString(this.mSummaryId);
            }
            return this.mSummary;
        }

        public void setSummary(String summary) {
            this.mSummary = summary;
        }

        public boolean ismEnabled() {
            return this.mEnabled;
        }

        public void setEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }
    }

    static {
        r0 = new Object[4][];
        r0[0] = new Object[]{"archive_num_huawei", Integer.valueOf(1), "1065796709,1065502043,1065902090,106575550211,10690133830,1069055999,106550200271,1065902002801,106906060012,106900679901,106900679914,106900679916,106903345801,106903345814,106903345816,106903345820,106903345901,106903345914", Integer.valueOf(R.string.archive_num_huawei_title), Integer.valueOf(R.string.archive_num_huawei_summary), Boolean.valueOf(true)};
        r0[1] = new Object[]{"archive_num_106", Integer.valueOf(2), "106,400,12306,111,12329,12345", Integer.valueOf(R.string.archive_num_106_title_content), Integer.valueOf(R.string.archive_num_106_summary_content), Boolean.valueOf(true)};
        r0[2] = new Object[]{"archive_num_bak", Integer.valueOf(2), "95,96", Integer.valueOf(R.string.archive_num_bak_title), Integer.valueOf(R.string.archive_num_bak_summary_content), Boolean.valueOf(true)};
        r0[3] = new Object[]{"archive_num_comm_operator", Integer.valueOf(2), "100,118,116,10198,12580", Integer.valueOf(R.string.archive_num_comm_operator_title), Integer.valueOf(R.string.archive_num_comm_operator_summary), Boolean.valueOf(true)};
        SMART_ARCHIVE_SETTINGS_CONF = r0;
    }

    private static ArrayList<SmartArchiveSettingItem> createDefaultSettingItems() {
        if (sHwCustSmartArchiveSettingUtils != null && sHwCustSmartArchiveSettingUtils.isServiceMessageArchivalEnabled()) {
            return sHwCustSmartArchiveSettingUtils.createDefaultSettingItemsForServiceMessage();
        }
        ArrayList<SmartArchiveSettingItem> items = new ArrayList();
        for (int i = 0; i < SMART_ARCHIVE_SETTINGS_CONF.length; i++) {
            String key = SMART_ARCHIVE_SETTINGS_CONF[i][0];
            SmartArchiveSettingItem item = new SmartArchiveSettingItem(key, ((Integer) SMART_ARCHIVE_SETTINGS_CONF[i][1]).intValue(), ((String) SMART_ARCHIVE_SETTINGS_CONF[i][2]).split(","), ((Integer) SMART_ARCHIVE_SETTINGS_CONF[i][3]).intValue(), ((Integer) SMART_ARCHIVE_SETTINGS_CONF[i][4]).intValue(), ((Boolean) SMART_ARCHIVE_SETTINGS_CONF[i][5]).booleanValue());
            if ("archive_num_106".equals(key)) {
                item.setArchiveNumberOtherPrefixes("1065796709202,106579670915,1065502043202,106550204315,1065902090202,106590209015,1069055999202,106905599915".split(","));
            }
            items.add(item);
        }
        return items;
    }

    private static void loadSmartArchiveConf() {
        ArrayList<SmartArchiveSettingItem> smartArchiveSettingItems;
        IOException e;
        XmlPullParserException e2;
        Exception e3;
        Throwable th;
        XmlPullParser xmlPullParser = null;
        InputStreamReader inputStreamReader = null;
        File confFile = new File("data/cust/xml/mms_smartArchive.xml");
        if (!confFile.exists() || (sHwCustSmartArchiveSettingUtils != null && sHwCustSmartArchiveSettingUtils.isServiceMessageArchivalEnabled())) {
            MLog.d("SmartArchiveSettingUtils", "createDefaultSettingItems from static config");
            smartArchiveSettingItems = createDefaultSettingItems();
        } else {
            smartArchiveSettingItems = new ArrayList();
            try {
                InputStreamReader confreader = new InputStreamReader(new FileInputStream(confFile), Charset.defaultCharset());
                try {
                    xmlPullParser = Xml.newPullParser();
                    xmlPullParser.setInput(confreader);
                    MmsConfig.beginDocument(xmlPullParser, "smartArchiveSettings");
                    sSmartArchiveConfigVersion = Integer.parseInt(xmlPullParser.getAttributeValue(null, "smartArchiveVersion"));
                    sSmartArchiveEnabledDefault = "true".equals(xmlPullParser.getAttributeValue(null, "smartArchiveEnabled"));
                    MLog.d("SmartArchiveSettingUtils", " sSmartArchiveEnabledDefault " + sSmartArchiveEnabledDefault);
                    while (true) {
                        MmsConfig.nextElement(xmlPullParser);
                        if (!"archiveSettingItem".equals(xmlPullParser.getName())) {
                            break;
                        }
                        SmartArchiveSettingItem item = new SmartArchiveSettingItem();
                        item.setKey(xmlPullParser.getAttributeValue(null, "key"));
                        item.setType(Integer.parseInt(xmlPullParser.getAttributeValue(null, NumberInfo.TYPE_KEY)));
                        for (String prefix : xmlPullParser.getAttributeValue(null, "prefixes").split(",")) {
                            item.addArchiveNumberPrefixes(prefix);
                        }
                        item.setTitle(xmlPullParser.getAttributeValue(null, "title"));
                        item.setSummary(xmlPullParser.getAttributeValue(null, "summary"));
                        item.setEnabled("true".equals(xmlPullParser.getAttributeValue(null, "enabled")));
                        smartArchiveSettingItems.add(item);
                    }
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e4) {
                            e = e4;
                            inputStreamReader = confreader;
                            e.printStackTrace();
                            synchronized (sSmartArchiveSettingItems) {
                                sSmartArchiveSettingItems.addAll(smartArchiveSettingItems);
                            }
                            synchronized (sDefaultSmartArchiveSettings) {
                                for (SmartArchiveSettingItem temp : smartArchiveSettingItems) {
                                    sDefaultSmartArchiveSettings.add(new SmartArchiveSettingItem(temp));
                                }
                            }
                        } catch (XmlPullParserException e5) {
                            e2 = e5;
                            inputStreamReader = confreader;
                            e2.printStackTrace();
                            synchronized (sSmartArchiveSettingItems) {
                                sSmartArchiveSettingItems.addAll(smartArchiveSettingItems);
                            }
                            synchronized (sDefaultSmartArchiveSettings) {
                                while (temp$iterator.hasNext()) {
                                    sDefaultSmartArchiveSettings.add(new SmartArchiveSettingItem(temp));
                                }
                            }
                        }
                    }
                    inputStreamReader = confreader;
                    if (xmlPullParser != null) {
                        try {
                            if (!XmlResourceParser.class.isInstance(xmlPullParser)) {
                                xmlPullParser.setInput(null);
                            }
                        } catch (IOException e6) {
                            e = e6;
                            e.printStackTrace();
                            synchronized (sSmartArchiveSettingItems) {
                                sSmartArchiveSettingItems.addAll(smartArchiveSettingItems);
                            }
                            synchronized (sDefaultSmartArchiveSettings) {
                                while (temp$iterator.hasNext()) {
                                    sDefaultSmartArchiveSettings.add(new SmartArchiveSettingItem(temp));
                                }
                            }
                        } catch (XmlPullParserException e7) {
                            e2 = e7;
                            e2.printStackTrace();
                            synchronized (sSmartArchiveSettingItems) {
                                sSmartArchiveSettingItems.addAll(smartArchiveSettingItems);
                            }
                            synchronized (sDefaultSmartArchiveSettings) {
                                while (temp$iterator.hasNext()) {
                                    sDefaultSmartArchiveSettings.add(new SmartArchiveSettingItem(temp));
                                }
                            }
                        }
                    }
                } catch (FileNotFoundException e8) {
                    inputStreamReader = confreader;
                } catch (Exception e9) {
                    e3 = e9;
                    inputStreamReader = confreader;
                } catch (Throwable th2) {
                    th = th2;
                    inputStreamReader = confreader;
                }
            } catch (FileNotFoundException e10) {
                try {
                    MLog.d("SmartArchiveSettingUtils", "mms_smartArchive file not found");
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e11) {
                            e11.printStackTrace();
                        } catch (XmlPullParserException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                        xmlPullParser.setInput(null);
                    }
                    synchronized (sSmartArchiveSettingItems) {
                        sSmartArchiveSettingItems.addAll(smartArchiveSettingItems);
                    }
                    synchronized (sDefaultSmartArchiveSettings) {
                        while (temp$iterator.hasNext()) {
                            sDefaultSmartArchiveSettings.add(new SmartArchiveSettingItem(temp));
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e112) {
                            e112.printStackTrace();
                        } catch (XmlPullParserException e222) {
                            e222.printStackTrace();
                        }
                    }
                    if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                        xmlPullParser.setInput(null);
                    }
                    throw th;
                }
            } catch (Exception e12) {
                e3 = e12;
                MLog.e("SmartArchiveSettingUtils", "Exception while parsing '" + confFile.getAbsolutePath() + "'", (Throwable) e3);
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e1122) {
                        e1122.printStackTrace();
                    } catch (XmlPullParserException e2222) {
                        e2222.printStackTrace();
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    xmlPullParser.setInput(null);
                }
                synchronized (sSmartArchiveSettingItems) {
                    sSmartArchiveSettingItems.addAll(smartArchiveSettingItems);
                }
                synchronized (sDefaultSmartArchiveSettings) {
                    while (temp$iterator.hasNext()) {
                        sDefaultSmartArchiveSettings.add(new SmartArchiveSettingItem(temp));
                    }
                }
            }
        }
        synchronized (sSmartArchiveSettingItems) {
            sSmartArchiveSettingItems.addAll(smartArchiveSettingItems);
        }
        synchronized (sDefaultSmartArchiveSettings) {
            while (temp$iterator.hasNext()) {
                sDefaultSmartArchiveSettings.add(new SmartArchiveSettingItem(temp));
            }
        }
    }

    public static void initSmartArcihivSettings(final Context context) {
        if (!MmsConfig.isSupportSmartFolder()) {
            return;
        }
        if (!OsUtil.isAtLeastL() || !OsUtil.isSecondaryUser()) {
            loadSmartArchiveConf();
            loadSmartArchiveUserData(context);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            sIsHwNotiReceived = sp.getBoolean("is_hw_noti_received", false);
            int oldVersion = sp.getInt("smartArchiveVersion", -1);
            Editor editor = sp.edit();
            editor.putBoolean("pref_key_smart_archive_enable", isSmartArchiveEnabled(context));
            MLog.d("SmartArchiveSettingUtils", "smartArchiveVersion old:" + oldVersion + ", new:" + sSmartArchiveConfigVersion);
            if (sSmartArchiveConfigVersion >= oldVersion) {
                editor.putInt("smartArchiveVersion", sSmartArchiveConfigVersion);
                HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
                    public void run() {
                        SmartArchiveSettingUtils.updateArchiveNumPrefixs(context);
                    }
                }, 1500);
            }
            editor.commit();
            setSmartArchiveEnabled(context, isSmartArchiveEnabled(context));
            setHuaweiArchiveEnabled(context, isHuaweiArchiveEnabled(context));
        }
    }

    private static void loadSmartArchiveUserData(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        synchronized (sSmartArchiveSettingItems) {
            for (SmartArchiveSettingItem item : sSmartArchiveSettingItems) {
                item.setEnabled(sp.getBoolean(item.getKey(), item.ismEnabled()));
            }
        }
    }

    public static ArrayList<SmartArchiveSettingItem> getSmartArchiveSettingItems() {
        ArrayList<SmartArchiveSettingItem> smartArchiveSettingItems = new ArrayList();
        synchronized (sSmartArchiveSettingItems) {
            for (SmartArchiveSettingItem temp : sSmartArchiveSettingItems) {
                smartArchiveSettingItems.add(new SmartArchiveSettingItem(temp));
            }
        }
        return smartArchiveSettingItems;
    }

    private static long getAutoDeleteTimeDuration() {
        return ((long) sAutoDeleteTimeDuration) * 86400000;
    }

    private static long getAutoDeleteRepeatDuration() {
        return ((long) sAutoDeleteRepeatDuration) * 86400000;
    }

    public static boolean isHwNotiReceived() {
        return sIsHwNotiReceived;
    }

    public static void setHasHwNotice(Context context, boolean value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("is_hw_noti_received", value);
        editor.commit();
        sIsHwNotiReceived = value;
    }

    public static void restoreSamrtArchiveSettings() {
        MLog.d("SmartArchiveSettingUtils", "restoreSamrtArchiveSettings");
        final Context context = MmsApp.getApplication();
        setHasHwNotice(context, isHwNotiReceived());
        disableAutoDelete();
        synchronized (sSmartArchiveSettingItems) {
            sSmartArchiveSettingItems.clear();
            synchronized (sDefaultSmartArchiveSettings) {
                for (SmartArchiveSettingItem temp : sDefaultSmartArchiveSettings) {
                    sSmartArchiveSettingItems.add(new SmartArchiveSettingItem(temp));
                }
            }
        }
        ThreadEx.execute(new Runnable() {
            public void run() {
                SmartArchiveSettingUtils.updateArchiveNumPrefixs(context);
            }
        });
    }

    public static void updateSmartArchiveSettingItem(String key, boolean isChecked) {
        if (key != null) {
            synchronized (sSmartArchiveSettingItems) {
                for (SmartArchiveSettingItem item : sSmartArchiveSettingItems) {
                    if (key.equals(item.getKey())) {
                        item.setEnabled(isChecked);
                    }
                }
            }
        }
    }

    public static void autoDeleteNotiMessages(Context context) {
        StringBuffer delWhere = new StringBuffer("thread_id").append(" in (");
        Cursor cursor = null;
        try {
            String queryWhere = new StringBuffer("number_type").append(" in (").append(1).append(",").append(2).append(")").toString();
            cursor = SqliteWrapper.query(context, Conversation.sAllThreadsUri, new String[]{"_id"}, queryWhere, null, null);
            int i = 0;
            while (cursor.moveToNext()) {
                delWhere.append(cursor.getInt(cursor.getColumnIndex("_id"))).append(",");
                i++;
            }
            if (i > 0) {
                delWhere.deleteCharAt(delWhere.length() - 1);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("SmartArchiveSettingUtils", "query notification thread id error " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        delWhere.append(")");
        long currentTimeMillis = System.currentTimeMillis();
        long time = currentTimeMillis - getAutoDeleteTimeDuration();
        delWhere.append(" and ").append("date").append("<?").append(" and locked=0");
        MLog.d("SmartArchiveSettingUtils", "autoDeleteNotiMessages " + delWhere.toString());
        StatisticalHelper.reportEvent(context, 2051, StatisticalHelper.getFormatTime(currentTimeMillis));
        SqliteWrapper.delete(context, Sms.CONTENT_URI, delWhere.toString(), new String[]{String.valueOf(time)});
        SqliteWrapper.delete(context, Mms.CONTENT_URI, delWhere.toString(), new String[]{String.valueOf(time / 1000)});
    }

    public static boolean updateArchiveNumPrefixs(Context context) {
        ArrayList<String> commServNumberOtherPrefexes = new ArrayList();
        ArrayList<String> hwServNumberPrefexes = new ArrayList();
        ArrayList<String> commServNumberPrefexes = new ArrayList();
        Bundle extras = new Bundle();
        for (SmartArchiveSettingItem item : getSmartArchiveSettingItems()) {
            if (1 == item.getType() && !isHwNotiReceived()) {
                item.setEnabled(true);
            }
            if (item.getKey().equals("archive_num_106")) {
                commServNumberOtherPrefexes = item.getArchiveNumberOtherPrefixes();
                extras.putBoolean("type_common_other_serv_num_enable", item.ismEnabled());
            }
            if (item.ismEnabled()) {
                if (item.getType() == 1) {
                    hwServNumberPrefexes.addAll(item.getArchiveNumberPrefixes());
                } else if (item.getType() == 2) {
                    commServNumberPrefexes.addAll(item.getArchiveNumberPrefixes());
                }
            }
        }
        extras.putStringArrayList("type_common_serv_num", commServNumberPrefexes);
        extras.putStringArrayList("type_hw_serv_num", hwServNumberPrefexes);
        extras.putStringArrayList("type_common_other_serv_num", commServNumberOtherPrefexes);
        String callingPkg = context.getPackageName();
        MLog.d("SmartArchiveSettingUtils", "updateArchiveNumPrefixs:" + extras);
        Bundle result = SqliteWrapper.call(context, MmsSms.CONTENT_URI, callingPkg, "method_update_serv_num_prefixs", null, extras);
        if (result == null) {
            return false;
        }
        return result.getBoolean("call_result");
    }

    public static boolean isSmartArchiveEnabled(Context context) {
        boolean z = true;
        if (!MmsConfig.isSupportSmartFolder()) {
            return false;
        }
        if (!OsUtil.isAtLeastL() || !OsUtil.isSecondaryUser()) {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_smart_archive_enable", sSmartArchiveEnabledDefault);
        }
        if (1 != Global.getInt(context.getContentResolver(), "sms_archive_state", sSmartArchiveEnabledDefault ? 1 : 0)) {
            z = false;
        }
        return z;
    }

    public static boolean isHuaweiArchiveEnabled(Context context) {
        boolean z = true;
        boolean z2 = false;
        if (!MmsConfig.isSupportSmartFolder()) {
            return false;
        }
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            if (1 != Global.getInt(context.getContentResolver(), "sms_huawei_archive_state", sSmartArchiveEnabledDefault ? 1 : 0)) {
                z = false;
            }
            return z;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean("pref_key_smart_archive_enable", sSmartArchiveEnabledDefault)) {
            z2 = sp.getBoolean("archive_num_huawei", sSmartArchiveEnabledDefault);
        }
        return z2;
    }

    public static void setSmartArchiveEnabled(Context context, boolean enable) {
        Global.putInt(context.getContentResolver(), "sms_archive_state", enable ? 1 : 0);
    }

    public static void setHuaweiArchiveEnabled(Context context, boolean enable) {
        Global.putInt(context.getContentResolver(), "sms_huawei_archive_state", enable ? 1 : 0);
    }

    public static boolean isSmartArchiveAutoDeleteEnable(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_smart_archive_auto_delete", false);
    }

    public static void disableAutoDelete() {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.w("SmartArchiveSettingUtils", "Not support delete archive sms in secondary user");
            return;
        }
        Context context = MmsApp.getApplication();
        MLog.d("SmartArchiveSettingUtils", "disable auto delete notification messages");
        ((AlarmManager) context.getSystemService("alarm")).cancel(createAutoDeleteIntent());
    }

    public static void enableAutoDelete() {
        if (!OsUtil.isAtLeastL() || !OsUtil.isSecondaryUser()) {
            Context context = MmsApp.getApplication();
            long systemTime = System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(systemTime);
            calendar.set(11, sAutoDeleteAtHourOfDay);
            calendar.set(12, 0);
            calendar.set(13, 0);
            calendar.set(14, 0);
            long timeToStart = calendar.getTimeInMillis();
            if (systemTime > timeToStart) {
                calendar.add(5, 1);
                timeToStart = calendar.getTimeInMillis();
            }
            MLog.d("SmartArchiveSettingUtils", "enable notification messages auto delete timeToStart " + timeToStart);
            ((AlarmManager) context.getSystemService("alarm")).setRepeating(0, timeToStart, getAutoDeleteRepeatDuration(), createAutoDeleteIntent());
        }
    }

    private static PendingIntent createAutoDeleteIntent() {
        Context context = MmsApp.getApplication();
        return PendingIntent.getBroadcast(context, 0, new Intent("com.huawei.mms.AUTO_DELETE_ALARM", null, context, MmsSystemEventReceiver.class), 0);
    }

    public static boolean getsSmartArchiveEnabledDefault() {
        return sSmartArchiveEnabledDefault;
    }
}
