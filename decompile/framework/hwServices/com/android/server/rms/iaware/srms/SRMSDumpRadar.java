package com.android.server.rms.iaware.srms;

import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.StatisticsData;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class SRMSDumpRadar {
    private static final int INTERVAL_ELAPSED_TIME = 4;
    private static final int RESOURCE_FEATURE_ID = FeatureType.getFeatureId(FeatureType.FEATURE_RESOURCE);
    private static final String TAG = "SRMSDumpRadar";
    private static volatile SRMSDumpRadar mSRMSDumpRadar;
    private static final String[] mSubTypeList = new String[]{"EnterFgKeyAppBQ", "EnterBgKeyAppBQ"};
    private ArrayList<Integer> mBigDataList = new ArrayList(4);
    private ArrayList<StatisticsData> mStatisticsData = null;

    public static SRMSDumpRadar getInstance() {
        if (mSRMSDumpRadar == null) {
            synchronized (SRMSDumpRadar.class) {
                if (mSRMSDumpRadar == null) {
                    mSRMSDumpRadar = new SRMSDumpRadar();
                }
            }
        }
        return mSRMSDumpRadar;
    }

    private SRMSDumpRadar() {
        int i;
        for (i = 0; i < 4; i++) {
            this.mBigDataList.add(Integer.valueOf(0));
        }
        this.mStatisticsData = new ArrayList();
        for (i = 0; i <= 1; i++) {
            this.mStatisticsData.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, mSubTypeList[i], 0, 0, 0, System.currentTimeMillis(), 0));
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        ArrayList<StatisticsData> dataList = new ArrayList();
        synchronized (this.mStatisticsData) {
            for (int i = 0; i <= 1; i++) {
                dataList.add(new StatisticsData(RESOURCE_FEATURE_ID, 2, mSubTypeList[i], ((StatisticsData) this.mStatisticsData.get(i)).getOccurCount(), 0, 0, ((StatisticsData) this.mStatisticsData.get(i)).getStartTime(), System.currentTimeMillis()));
            }
            resetStatisticsData();
        }
        AwareLog.d(TAG, "SRMS getStatisticsData success");
        return dataList;
    }

    public void updateStatisticsData(int subTypeCode) {
        if (subTypeCode >= 0 && subTypeCode <= 1) {
            synchronized (this.mStatisticsData) {
                StatisticsData data = (StatisticsData) this.mStatisticsData.get(subTypeCode);
                data.setOccurCount(data.getOccurCount() + 1);
            }
        } else if (subTypeCode < 10 || subTypeCode > 13) {
            AwareLog.e(TAG, "error subTypeCode");
        } else {
            updateBigData(subTypeCode - 10);
        }
    }

    private void resetStatisticsData() {
        synchronized (this.mStatisticsData) {
            for (int i = 0; i <= 1; i++) {
                ((StatisticsData) this.mStatisticsData.get(i)).setSubType(mSubTypeList[i]);
                ((StatisticsData) this.mStatisticsData.get(i)).setOccurCount(0);
                ((StatisticsData) this.mStatisticsData.get(i)).setStartTime(System.currentTimeMillis());
                ((StatisticsData) this.mStatisticsData.get(i)).setEndTime(0);
            }
        }
    }

    public String saveSRMSBigData(boolean clear) {
        StringBuilder data;
        synchronized (this.mBigDataList) {
            data = new StringBuilder("[iAwareSRMSStatis_Start]\n").append(makeSRMSJson().toString()).append("\n[iAwareSRMSStatis_End]");
            if (clear) {
                resetBigData();
            }
        }
        AwareLog.d(TAG, "SRMS saveSRMSBigData success:" + data);
        return data.toString();
    }

    private void updateBigData(int interval) {
        synchronized (this.mBigDataList) {
            this.mBigDataList.set(interval, Integer.valueOf(((Integer) this.mBigDataList.get(interval)).intValue() + 1));
        }
    }

    private void resetBigData() {
        for (int i = 0; i < 4; i++) {
            this.mBigDataList.set(i, Integer.valueOf(0));
        }
    }

    private JSONObject makeSRMSJson() {
        int countElapsedTimeLess20 = ((Integer) this.mBigDataList.get(0)).intValue();
        int countElapsedTimeLess60 = ((Integer) this.mBigDataList.get(1)).intValue();
        int countElapsedTimeLess100 = ((Integer) this.mBigDataList.get(2)).intValue();
        int countElapsedTimeMore100 = ((Integer) this.mBigDataList.get(3)).intValue();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("elapsedTime_less20", countElapsedTimeLess20);
            jsonObj.put("elapsedTime_less60", countElapsedTimeLess60);
            jsonObj.put("elapsedTime_less100", countElapsedTimeLess100);
            jsonObj.put("elapsedTime_more100", countElapsedTimeMore100);
        } catch (JSONException e) {
            AwareLog.e(TAG, "make json error");
        }
        return jsonObj;
    }
}
