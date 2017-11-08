package com.huawei.harassmentinterception.blackwhitelist;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.harassmentinterception.callback.DataUpdateCallBack;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistInfo;
import com.huawei.harassmentinterception.common.CommonObject.WhitelistInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.strategy.HotlineInterceptionConfigs;
import com.huawei.harassmentinterception.util.HotlineNumberHelper;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class BlackWhiteDBDataUpdater {
    private static final String TAG = "BlackWhiteDBDataUpdater";
    public static final int UPDATE_BY_BACKUP = 0;
    public static final int UPDATE_BY_NORMAL = 1;
    private static DataUpdateCallBack mCallBack = null;
    private static Context mContext = null;
    private static BlackWhiteDBDataUpdater mInstance;
    private static Queue<Integer> mWorkQueue = new ArrayBlockingQueue(10);
    private List<BlacklistInfo> mBlacklistDeletedList = new ArrayList();
    private Map<String, List<BlacklistInfo>> mBlacklistParsedMap = new HashMap();
    private List<BlacklistInfo> mBlacklistUpdatedList = new ArrayList();
    private boolean mDataParseCompleted = false;
    private List<WhitelistInfo> mWhitelistDeletedList = new ArrayList();
    private Map<String, List<WhitelistInfo>> mWhitelistParsedMap = new HashMap();
    private List<WhitelistInfo> mWhitelistUpdatedList = new ArrayList();
    private WorkThread mWorkThread = null;

    class WorkThread extends Thread {
        public void run() {
            try {
                BlackWhiteDBDataUpdater.this.parseDataSync();
                BlackWhiteDBDataUpdater.this.setCompleteFlag();
                tryAgainIfNeeded();
                HwLog.i(BlackWhiteDBDataUpdater.TAG, "parse DB Completed");
                BlackWhiteDBDataUpdater.this.mWorkThread = null;
            } catch (Exception e) {
                HwLog.e(BlackWhiteDBDataUpdater.TAG, "WorkThread-run: Exception", e);
            }
        }

        private void tryAgainIfNeeded() {
            if (BlackWhiteDBDataUpdater.mWorkQueue.contains(Integer.valueOf(0))) {
                HwLog.i(BlackWhiteDBDataUpdater.TAG, "tryAgainIfNeeded,yes");
                BlackWhiteDBDataUpdater.this.parseDataSync();
                return;
            }
            HwLog.i(BlackWhiteDBDataUpdater.TAG, "tryAgainIfNeeded,skip");
        }
    }

    private BlackWhiteDBDataUpdater() {
    }

    public static synchronized BlackWhiteDBDataUpdater getInstance(Context context, DataUpdateCallBack callBack, int source) {
        synchronized (BlackWhiteDBDataUpdater.class) {
            HwLog.i(TAG, "source = " + source);
            mCallBack = callBack;
            mContext = context;
            if (source == 0 || source == 1) {
                if (!mWorkQueue.contains(Integer.valueOf(source))) {
                    mWorkQueue.add(Integer.valueOf(source));
                }
                if (mInstance == null) {
                    mInstance = new BlackWhiteDBDataUpdater();
                }
                BlackWhiteDBDataUpdater blackWhiteDBDataUpdater = mInstance;
                return blackWhiteDBDataUpdater;
            }
            HwLog.w(TAG, "source is not support  " + source);
            return null;
        }
    }

    public void triggleUpdate() {
        if ((this.mWorkThread == null || !this.mWorkThread.isAlive()) && HotlineInterceptionConfigs.isHotlineNumberWithoutAreaCodeFuzzyMatchEnable()) {
            this.mWorkThread = new WorkThread();
            this.mWorkThread.start();
        }
    }

    public boolean isDataParseCompleted() {
        return this.mDataParseCompleted;
    }

    private void setCompleteFlag() {
        this.mDataParseCompleted = true;
    }

    private List<BlacklistInfo> getUpdatedBlackList() {
        return this.mBlacklistUpdatedList;
    }

    private List<BlacklistInfo> getDeletedBlackList() {
        return this.mBlacklistDeletedList;
    }

    private List<WhitelistInfo> getUpdatedWhiteList() {
        return this.mWhitelistUpdatedList;
    }

    private List<WhitelistInfo> getDeletedWhiteList() {
        return this.mWhitelistDeletedList;
    }

    private void mergeBlacklistInfo(List<BlacklistInfo> BlacklistInfo) {
        HwLog.i(TAG, "mergeBlacklistInfo  current blklist count= " + BlacklistInfo.size());
        for (BlacklistInfo blacklistInfoItem : BlacklistInfo) {
            if (maybeHotlineNumberInBlackList(blacklistInfoItem)) {
                String noAreaNumber = HotlineNumberHelper.getNoAreaNumber(mContext, blacklistInfoItem.getPhone());
                if (!TextUtils.isEmpty(noAreaNumber) && noAreaNumber.length() == 5) {
                    if (noAreaNumber.startsWith(HotlineInterceptionConfigs.NUMBER_START)) {
                        String hotlineName = HotlineNumberHelper.getHotlineNumberName(mContext, noAreaNumber);
                        if (TextUtils.isEmpty(hotlineName)) {
                            HwLog.i(TAG, "hotlineName null,skip");
                        } else {
                            if (TextUtils.isEmpty(blacklistInfoItem.getName())) {
                                blacklistInfoItem.setName(hotlineName);
                            }
                            if (this.mBlacklistParsedMap.containsKey(noAreaNumber)) {
                                ((List) this.mBlacklistParsedMap.get(noAreaNumber)).add(blacklistInfoItem);
                            } else {
                                List<BlacklistInfo> blacklistInfos = new ArrayList();
                                blacklistInfos.add(blacklistInfoItem);
                                this.mBlacklistParsedMap.put(noAreaNumber, blacklistInfos);
                            }
                        }
                    }
                }
                HwLog.i(TAG, "not hotline numerber start with 9 and length is 5");
            } else {
                HwLog.i(TAG, "the blacklistInfoItem need not merge!");
            }
        }
        if (this.mBlacklistParsedMap.size() == 0) {
            HwLog.i(TAG, "no hotline number in current blacklist");
            return;
        }
        for (Entry<String, List<BlacklistInfo>> entry : this.mBlacklistParsedMap.entrySet()) {
            List<BlacklistInfo> list = (List) entry.getValue();
            int id = ((BlacklistInfo) list.get(0)).getId();
            String phone = (String) entry.getKey();
            String name = null;
            int option = 0;
            for (int i = 0; i < list.size(); i++) {
                BlacklistInfo blacklistInfo = (BlacklistInfo) list.get(i);
                name = blacklistInfo.getName();
                option |= blacklistInfo.getOption();
                if (i > 0) {
                    this.mBlacklistDeletedList.add(blacklistInfo);
                }
            }
            int[] count = DBAdapter.getInterceptedCallAndMsgCount(mContext, phone, 0);
            this.mBlacklistUpdatedList.add(new BlacklistInfo(id, phone, name, count[0], count[1], option, 0));
        }
    }

    private void mergeWhitelistInfo(List<WhitelistInfo> latestWhiteList) {
        for (WhitelistInfo whitelistInfo : latestWhiteList) {
            if (maybeHotlineNumberInWhiteList(whitelistInfo)) {
                String noAreaNumber = HotlineNumberHelper.getNoAreaNumber(mContext, whitelistInfo.getPhone());
                if (!TextUtils.isEmpty(noAreaNumber) && noAreaNumber.length() == 5 && noAreaNumber.startsWith(HotlineInterceptionConfigs.NUMBER_START)) {
                    String hotlineName = HotlineNumberHelper.getHotlineNumberName(mContext, noAreaNumber);
                    if (!TextUtils.isEmpty(hotlineName)) {
                        if (TextUtils.isEmpty(whitelistInfo.getName())) {
                            whitelistInfo.setName(hotlineName);
                        }
                        if (this.mWhitelistParsedMap.containsKey(noAreaNumber)) {
                            ((List) this.mWhitelistParsedMap.get(noAreaNumber)).add(whitelistInfo);
                        } else {
                            List<WhitelistInfo> whitelistInfos = new ArrayList();
                            whitelistInfos.add(whitelistInfo);
                            this.mWhitelistParsedMap.put(noAreaNumber, whitelistInfos);
                        }
                    }
                }
            }
        }
        if (this.mWhitelistParsedMap.size() != 0) {
            for (Entry<String, List<WhitelistInfo>> entry : this.mWhitelistParsedMap.entrySet()) {
                List<WhitelistInfo> list = (List) entry.getValue();
                int id = ((WhitelistInfo) list.get(0)).getId();
                String phone = (String) entry.getKey();
                String name = null;
                for (int i = 0; i < list.size(); i++) {
                    WhitelistInfo whitelistInfoItem = (WhitelistInfo) list.get(i);
                    name = whitelistInfoItem.getName();
                    if (i > 0 || this.mBlacklistParsedMap.containsKey(phone)) {
                        this.mWhitelistDeletedList.add(whitelistInfoItem);
                    }
                }
                if (!this.mBlacklistParsedMap.containsKey(phone)) {
                    this.mWhitelistUpdatedList.add(new WhitelistInfo(id, phone, name));
                }
            }
        }
    }

    private boolean maybeHotlineNumberInBlackList(BlacklistInfo blacklistInfoItem) {
        if (blacklistInfoItem.getType() != 0 || blacklistInfoItem.getPhone().length() > 9 || blacklistInfoItem.getPhone().length() < 5) {
            return false;
        }
        return true;
    }

    private boolean maybeHotlineNumberInWhiteList(WhitelistInfo whitelistInfoItem) {
        int length = whitelistInfoItem.getPhone().length();
        return length <= 9 && length >= 5;
    }

    private void parseDataSync() {
        HwLog.i(TAG, "call parseDataSync begin ");
        Integer value = (Integer) mWorkQueue.peek();
        if (!mWorkQueue.isEmpty() && value != null && value.intValue() == 0) {
            HwLog.i(TAG, "mWorkQueue is UPDATE_BY_BACKUP");
            mWorkQueue.clear();
        } else if (!mWorkQueue.isEmpty()) {
            mWorkQueue.remove();
        }
        cleanData();
        mergeBlacklistInfo(DBAdapter.getBlacklist(mContext));
        mergeWhitelistInfo(DBAdapter.getWhitelist(mContext));
        List<BlacklistInfo> shouldDelBlackList = getDeletedBlackList();
        List<BlacklistInfo> shouldUpdateBlackList = getUpdatedBlackList();
        List<WhitelistInfo> shouldDelWhiteList = getDeletedWhiteList();
        List<WhitelistInfo> shouldUpdateWhiteList = getUpdatedWhiteList();
        for (BlacklistInfo blacklistInfo : shouldDelBlackList) {
            DBAdapter.deleteBlacklist(mContext, blacklistInfo);
        }
        DBAdapter.updateBlacklistInfo(mContext, shouldUpdateBlackList);
        for (WhitelistInfo whitelistInfo : shouldDelWhiteList) {
            DBAdapter.deleteWhitelist(mContext, whitelistInfo.getId());
        }
        DBAdapter.updateWhitelistInfo(mContext, shouldUpdateWhiteList);
        PreferenceHelper.setBlackWhiteListDBUpdatedStatus(mContext);
        if (mCallBack != null) {
            mCallBack.onCompleteDataUpdate(0);
        }
        HwLog.i(TAG, "call parseDataSync end ");
    }

    private void cleanData() {
        this.mBlacklistDeletedList.clear();
        this.mBlacklistUpdatedList.clear();
        this.mBlacklistParsedMap.clear();
        this.mWhitelistDeletedList.clear();
        this.mWhitelistUpdatedList.clear();
        this.mWhitelistParsedMap.clear();
    }
}
