package com.android.server.rms.iaware.appmng;

import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.Iterator;

public class AppMngDumpRadar {
    public static final int APPMNG_FEATURE_ID = FeatureType.getFeatureId(FeatureType.FEATURE_APPMNG);
    private static final int DUMPDATA_MAX_SIZE = 5;
    private static final int ONE_MINUTES = 60000;
    private static final int STATISTIC_DATA_TYPE = 1;
    private static AppMngDumpRadar sInstance;
    private ArrayList<DumpData> mAppMngDumpData;
    private ArrayList<StatisticsData> mStatisticsData;

    public static synchronized AppMngDumpRadar getInstance() {
        AppMngDumpRadar appMngDumpRadar;
        synchronized (AppMngDumpRadar.class) {
            if (sInstance == null) {
                sInstance = new AppMngDumpRadar();
            }
            appMngDumpRadar = sInstance;
        }
        return appMngDumpRadar;
    }

    private AppMngDumpRadar() {
        this.mAppMngDumpData = null;
        this.mStatisticsData = null;
        this.mAppMngDumpData = new ArrayList();
        this.mStatisticsData = new ArrayList();
    }

    public ArrayList<DumpData> getDumpData(int time) {
        long currenttime = System.currentTimeMillis();
        synchronized (this.mAppMngDumpData) {
            if (this.mAppMngDumpData.isEmpty()) {
                return null;
            }
            ArrayList<DumpData> tempdumplist = new ArrayList();
            i = this.mAppMngDumpData.size() > 5 ? this.mAppMngDumpData.size() - 5 : 0;
            while (i < this.mAppMngDumpData.size()) {
                DumpData tempDd = (DumpData) this.mAppMngDumpData.get(i);
                if (tempDd != null && currenttime - tempDd.getTime() < ((long) time) * 1000) {
                    tempdumplist.add(tempDd);
                }
                i++;
            }
            if (tempdumplist.isEmpty()) {
                return null;
            }
            return tempdumplist;
        }
    }

    public void insertDumpData(long time, String operation, int exetime, String reason) {
        if (operation != null && reason != null) {
            DumpData Dd = new DumpData(time, APPMNG_FEATURE_ID, operation, exetime, reason);
            synchronized (this.mAppMngDumpData) {
                if (this.mAppMngDumpData.isEmpty()) {
                    this.mAppMngDumpData.add(Dd);
                } else {
                    while (!this.mAppMngDumpData.isEmpty()) {
                        DumpData tempDd = (DumpData) this.mAppMngDumpData.get(0);
                        if (tempDd != null) {
                            if (!(time - tempDd.getTime() > AppHibernateCst.DELAY_ONE_MINS)) {
                                break;
                            }
                            this.mAppMngDumpData.remove(0);
                        } else {
                            this.mAppMngDumpData.remove(0);
                        }
                    }
                    this.mAppMngDumpData.add(Dd);
                }
            }
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        if (!AwareAppMngSort.checkAppMngEnable()) {
            return null;
        }
        ArrayList<StatisticsData> tempList = new ArrayList();
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            ArrayList<StatisticsData> habitSDList = habit.getStatisticsData();
            if (!habitSDList.isEmpty()) {
                tempList.addAll(habitSDList);
            }
        }
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return tempList;
            }
            for (int i = 0; i < this.mStatisticsData.size(); i++) {
                StatisticsData tempSd = (StatisticsData) this.mStatisticsData.get(i);
                if (tempSd != null) {
                    tempList.add(new StatisticsData(tempSd.getFeatureId(), tempSd.getType(), tempSd.getSubType(), tempSd.getOccurCount(), tempSd.getTotalTime(), tempSd.getEffect(), tempSd.getStartTime(), tempSd.getEndTime()));
                }
            }
            clearArrayList();
            return tempList;
        }
    }

    private void clearArrayList() {
        synchronized (this.mStatisticsData) {
            if (this.mStatisticsData.isEmpty()) {
                return;
            }
            Iterator<StatisticsData> it = this.mStatisticsData.iterator();
            long now = System.currentTimeMillis();
            while (it.hasNext()) {
                StatisticsData tempSd = (StatisticsData) it.next();
                if (tempSd != null) {
                    tempSd.setOccurCount(0);
                    tempSd.setTotalTime(0);
                    tempSd.setEffect(0);
                    tempSd.setStartTime(now);
                    tempSd.setEndTime(now);
                }
            }
        }
    }

    public void insertStatisticData(String subtype, int exetime, int effect) {
        if (subtype != null) {
            synchronized (this.mStatisticsData) {
                int size = this.mStatisticsData.size();
                long now = System.currentTimeMillis();
                for (int i = 0; i < size; i++) {
                    StatisticsData data = (StatisticsData) this.mStatisticsData.get(i);
                    if (data != null && data.getSubType().equals(subtype)) {
                        data.setTotalTime(data.getTotalTime() + exetime);
                        data.setOccurCount(data.getOccurCount() + 1);
                        data.setEffect(data.getEffect() + effect);
                        data.setEndTime(now);
                        return;
                    }
                }
                this.mStatisticsData.add(new StatisticsData(APPMNG_FEATURE_ID, 1, subtype, 1, exetime, effect, now, now));
            }
        }
    }
}
