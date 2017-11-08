package com.huawei.iaware.userhabit;

import android.content.ContentResolver;
import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.algorithm.utils.ProtectApp;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NRTHabitProtectListTrainAlgorithm {
    private static final String SEPARATOR = ";";
    private static final String TAG = "NRTHabitProtectListTrainAlgorithm";
    private static final int UNUSED_COUNT = 0;
    private static boolean mIsEmailAppsUsed = false;
    private static boolean mIsIMAppsUsed = false;
    private ContentResolver mContentResolver = null;
    private Context mContext = null;
    private final List<ProtectApp> mHabitProtectAppsList = new ArrayList();
    private final List<ProtectApp> mProtectAppsList = new ArrayList();
    private Map<String, Integer> mUsageCount = null;

    public NRTHabitProtectListTrainAlgorithm(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mContentResolver = this.mContext.getContentResolver();
        }
    }

    void deinit() {
        clearHabitProtectList();
    }

    void habitProtectListTrain(Map<String, Integer> map, int i) {
        if (map == null || map.size() == 0) {
            AwareLog.d(TAG, "No need to train!");
            return;
        }
        AwareLog.i(TAG, "Habit protectList train begin.");
        long currentTimeMillis = System.currentTimeMillis();
        this.mUsageCount = map;
        loadHabitProtectList(i);
        if (checkAppsUsedInfo()) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (ProtectApp protectApp : this.mProtectAppsList) {
                int i2;
                float f;
                String appPkgName = protectApp.getAppPkgName();
                String recentUsed = protectApp.getRecentUsed();
                int appType = protectApp.getAppType();
                if (!mIsIMAppsUsed || appType != 0) {
                    if (mIsEmailAppsUsed) {
                        if (appType != 1) {
                        }
                    }
                }
                if (this.mUsageCount.containsKey(appPkgName)) {
                    appPkgName = recentUsed + this.mUsageCount.get(appPkgName) + ";";
                } else {
                    appPkgName = recentUsed + 0 + ";";
                }
                String[] split = appPkgName.split(";");
                int length = split.length;
                appType = 0;
                float avgUsedFrequency = protectApp.getAvgUsedFrequency();
                if (length <= 14) {
                    recentUsed = appPkgName;
                    i2 = 0;
                } else {
                    recentUsed = appPkgName.substring(appPkgName.indexOf(";") + 1);
                    i2 = 1;
                }
                while (i2 < split.length) {
                    appType += getRecuentUsedCount(split[i2]);
                    i2++;
                }
                if (length == 0) {
                    f = avgUsedFrequency;
                } else {
                    try {
                        f = Float.parseFloat(decimalFormat.format((double) (((float) appType) / ((float) length))));
                    } catch (NumberFormatException e) {
                        AwareLog.w(TAG, "parseFloat exception " + e.toString());
                        f = 0.0f;
                    }
                }
                protectApp.setRecentUsed(recentUsed);
                protectApp.setAvgUsedFrequency(f);
            }
            updateHabitProtectListDB(i);
            clearData();
            AwareLog.i(TAG, "habit protectList train end. spend times:" + (System.currentTimeMillis() - currentTimeMillis) + "ms");
            return;
        }
        AwareLog.i(TAG, "Ineffective train end. spend times:" + (System.currentTimeMillis() - currentTimeMillis) + "ms" + mIsEmailAppsUsed + " " + mIsIMAppsUsed);
    }

    private boolean checkAppsUsedInfo() {
        for (ProtectApp protectApp : this.mProtectAppsList) {
            String appPkgName = protectApp.getAppPkgName();
            int appType = protectApp.getAppType();
            if (this.mUsageCount.containsKey(appPkgName)) {
                if (appType == 0) {
                    setIsIMAppUsed(true);
                } else if (appType == 1) {
                    setIsEmailAppUsed(true);
                }
            }
        }
        return mIsEmailAppsUsed || mIsIMAppsUsed;
    }

    private static void setIsIMAppUsed(boolean z) {
        mIsIMAppsUsed = z;
    }

    private static void setIsEmailAppUsed(boolean z) {
        mIsEmailAppsUsed = z;
    }

    private int getRecuentUsedCount(String str) {
        int i = 0;
        try {
            i = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            AwareLog.w(TAG, "parseInt exception " + e.toString());
        }
        return i;
    }

    private void loadHabitProtectList(int i) {
        this.mProtectAppsList.clear();
        Collection arrayList = new ArrayList();
        IAwareHabitUtils.loadUnDeletedHabitProtectList(this.mContentResolver, arrayList, i);
        this.mProtectAppsList.addAll(arrayList);
    }

    private void clearData() {
        this.mUsageCount.clear();
        this.mProtectAppsList.clear();
        setIsIMAppUsed(false);
        setIsEmailAppUsed(false);
    }

    private void updateHabitProtectListDB(int i) {
        if (this.mProtectAppsList != null) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (ProtectApp protectApp : this.mProtectAppsList) {
                if (!mIsIMAppsUsed || protectApp.getAppType() != 0) {
                    if (mIsEmailAppsUsed) {
                        if (protectApp.getAppType() != 1) {
                        }
                    }
                }
                IAwareHabitUtils.updateHabitProtectList(this.mContentResolver, protectApp.getAppPkgName(), protectApp.getRecentUsed(), decimalFormat.format((double) protectApp.getAvgUsedFrequency()), i);
            }
        }
    }

    void initHabitProtectList(int i) {
        synchronized (this.mHabitProtectAppsList) {
            this.mHabitProtectAppsList.clear();
            IAwareHabitUtils.loadHabitProtectList(this.mContentResolver, this.mHabitProtectAppsList, i);
        }
    }

    private void clearHabitProtectList() {
        synchronized (this.mHabitProtectAppsList) {
            this.mHabitProtectAppsList.clear();
        }
    }

    void reportCheckHabitProtectList(String str, int i) {
        synchronized (this.mHabitProtectAppsList) {
            for (ProtectApp protectApp : this.mHabitProtectAppsList) {
                if (protectApp.getAppPkgName().equals(str)) {
                    if (protectApp.getDeletedTag() == 0) {
                        protectApp.setDeletedTag(1);
                    }
                    int i2 = 1;
                }
            }
            Object obj = null;
        }
        if (obj != null) {
            IAwareHabitUtils.deleteHabitProtectList(this.mContentResolver, str, i);
        }
    }

    void foregroundCheckHabitProtectList(String str, int i, int i2) {
        Object obj;
        Object obj2;
        synchronized (this.mHabitProtectAppsList) {
            for (ProtectApp protectApp : this.mHabitProtectAppsList) {
                if (protectApp.getAppPkgName().equals(str)) {
                    if (protectApp.getDeletedTag() != 1) {
                        obj = null;
                        obj2 = null;
                    } else {
                        protectApp.setDeletedTag(0);
                        int i3 = 1;
                        obj2 = null;
                    }
                }
            }
            obj = null;
            obj2 = 1;
        }
        if (obj2 != null) {
            int i4;
            if (i2 == 0 || i2 == 311) {
                i4 = 0;
            } else if (i2 == 1 || i2 == MemoryConstant.MSG_DIRECT_SWAPPINESS) {
                i4 = 1;
            } else {
                return;
            }
            IAwareHabitUtils.insertHabitProtectList(this.mContentResolver, str, i4, i);
            synchronized (this.mHabitProtectAppsList) {
                this.mHabitProtectAppsList.add(new ProtectApp(str, i4, 0, i));
            }
        }
        if (obj != null) {
            IAwareHabitUtils.updateDeletedHabitProtectList(this.mContentResolver, str, i);
        }
    }
}
