package com.android.server.rms.statistic;

import android.os.SystemProperties;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.utils.Utils;
import java.util.ArrayList;
import java.util.Map;

public final class HwStatisticCtl {
    private static final String TAG = "HwStatisticCtl";
    private static final boolean isBetaUser;
    private int mCollectCount;
    private HwConfigReader mConfig;
    private boolean mIsInit = false;
    private ArrayList<Map<String, HwResRecord>> mResRecordMaps = new ArrayList();
    private HwTimeStatistic mTimeStatistic;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        isBetaUser = z;
    }

    public HwStatisticCtl(HwConfigReader config) {
        this.mConfig = config;
        this.mTimeStatistic = new HwTimeStatistic();
    }

    public void init() {
        if (!this.mIsInit) {
            this.mCollectCount = 0;
            initTimeStatistic();
            initResStatistic();
            this.mIsInit = true;
        }
    }

    public void statisticGroups() {
        if (this.mIsInit) {
            this.mCollectCount++;
            int cycle_num = 0;
            Iterable groupIDs = null;
            if (this.mConfig != null) {
                groupIDs = this.mConfig.getCountGroupID();
            }
            if (r1 != null) {
                for (Integer intValue : r1) {
                    int id = intValue.intValue();
                    if (this.mConfig != null) {
                        cycle_num = this.mConfig.getGroupSampleCycleNum(id);
                    }
                    if (cycle_num > 0 && this.mCollectCount % cycle_num == 0) {
                        if ((Utils.RMSVERSION & 1) != 0) {
                            statisticOneGroup(id);
                        }
                        if ((Utils.RMSVERSION & 2) != 0) {
                            trimOneGroup(id);
                        }
                    }
                }
            }
        }
    }

    private void statisticOneGroup(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            resStatistic.statistic(resStatistic.sample(groupID));
        }
    }

    private void trimOneGroup(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            resStatistic.acquire(groupID);
        }
    }

    private void initTimeStatistic() {
        long saveInterval = 0;
        long statisticPeroid = 0;
        if (this.mConfig != null) {
            saveInterval = ((long) this.mConfig.getSaveInterval()) * AppHibernateCst.DELAY_ONE_MINS;
            statisticPeroid = ((long) this.mConfig.getCountInterval(isBetaUser)) * AppHibernateCst.DELAY_ONE_MINS;
        }
        this.mTimeStatistic.init(saveInterval, statisticPeroid, 0);
    }

    private void initResStatistic() {
        ArrayList groupIDs = null;
        if (this.mConfig != null) {
            groupIDs = this.mConfig.getCountGroupID();
        }
        if (groupIDs != null) {
            int size = groupIDs.size();
            for (int index = 0; index < size; index++) {
                int groupID = ((Integer) groupIDs.get(index)).intValue();
                initGroup(groupID);
                Map<String, HwResRecord> recordMap = obtainResRecordMap(groupID);
                if (recordMap != null) {
                    this.mResRecordMaps.add(recordMap);
                }
            }
        }
    }

    private void initGroup(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            resStatistic.init(this.mConfig);
        }
    }

    private Map<String, HwResRecord> obtainResRecordMap(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            return resStatistic.obtainResRecordMap();
        }
        return null;
    }
}
