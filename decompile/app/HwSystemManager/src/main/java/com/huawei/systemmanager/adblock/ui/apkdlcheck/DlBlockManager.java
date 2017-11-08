package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import android.content.Context;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class DlBlockManager {
    private static final int MAX_TEMP_SIZE = 300;
    private static final String TAG = "DlBlockManager";
    private static DlBlockManager instance;
    private final HashMap<String, Record> hashRecords = new HashMap();

    public static class Record {
        public String mApkName;
        public int mOptPolicy = -1;
        public String mPkgName;
    }

    public static synchronized DlBlockManager getInstance() {
        DlBlockManager dlBlockManager;
        synchronized (DlBlockManager.class) {
            if (instance == null) {
                instance = new DlBlockManager();
            }
            dlBlockManager = instance;
        }
        return dlBlockManager;
    }

    private DlBlockManager() {
    }

    public void record(Context context, int uid, String url, AdCheckUrlResult result) {
        Record record = new Record();
        record.mApkName = result.getApkAppName();
        record.mOptPolicy = result.getOptPolicy();
        record.mPkgName = result.getApkPkgName();
        result.saveOrUpdate(context);
        setTempRecord(uid, url, record);
    }

    public void clearTempRecord(String pkg) {
        Iterator<Entry<String, Record>> it = this.hashRecords.entrySet().iterator();
        while (it.hasNext()) {
            Record record = (Record) this.hashRecords.get((String) ((Entry) it.next()).getKey());
            if (record == null) {
                it.remove();
            } else if (record.mPkgName != null && record.mPkgName.equals(pkg)) {
                it.remove();
            }
        }
    }

    public Record getTempRecord(int uid, String downloadUrl) {
        return (Record) this.hashRecords.get(uid + downloadUrl);
    }

    public void setTempRecord(int uid, String downloadUrl, Record record) {
        String key = uid + downloadUrl;
        if (this.hashRecords.size() > 300) {
            this.hashRecords.clear();
        }
        this.hashRecords.put(key, record);
    }
}
