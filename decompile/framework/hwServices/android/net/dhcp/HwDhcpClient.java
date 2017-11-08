package android.net.dhcp;

import android.content.Context;
import android.net.DhcpResults;
import android.util.LruCache;
import com.android.internal.util.StateMachine;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class HwDhcpClient extends DhcpClient {
    private static final String TAG = "HwDhcpClient";
    private static LruCache<String, DhcpResultsInfoRecord> mDhcpResultsInfoCache = new LruCache(50);
    private static Object mLock = new Object();
    private static String mPendingSSID = null;
    private static boolean mReadDBDone = false;
    private DhcpResultsInfoDBManager mDBMgr;
    private DhcpResultsInfoRecord mHwDhcpResultsInfo;
    private String mtoEvictSSID = null;

    static class ReadDBThread extends Thread {
        Context dbcontext;

        public ReadDBThread(Context context) {
            this.dbcontext = context;
        }

        public void run() {
            HwDhcpClient.getAllDhcpResultsInfofromDB(this.dbcontext);
        }
    }

    public HwDhcpClient(Context context, StateMachine controller, String iface) {
        super(context, controller, iface);
        this.mDBMgr = DhcpResultsInfoDBManager.getInstance(context);
    }

    public static DhcpClient makeHwDhcpClient(Context context, StateMachine controller, String intf) {
        DhcpClient client = new HwDhcpClient(context, controller, intf);
        client.start();
        return client;
    }

    public static void initDB(Context context) {
        new ReadDBThread(context).start();
    }

    public static void putPendingSSID(String pendingSSID) {
        mPendingSSID = pendingSSID;
    }

    public static void getAllDhcpResultsInfofromDB(Context context) {
        DhcpResultsInfoDBManager dbMgr = DhcpResultsInfoDBManager.getInstance(context);
        synchronized (mLock) {
            mDhcpResultsInfoCache = dbMgr.getAllDhcpResultsInfo();
        }
        mReadDBDone = true;
    }

    public boolean getReadDBDone() {
        return mReadDBDone;
    }

    public DhcpResultsInfoRecord getDhcpResultsInfoRecord() {
        if (mPendingSSID == null) {
            return null;
        }
        String pendingSSID = mPendingSSID;
        synchronized (mLock) {
            if (mDhcpResultsInfoCache != null) {
                DhcpResultsInfoRecord dhcpResultsInfoRecord = (DhcpResultsInfoRecord) mDhcpResultsInfoCache.get(pendingSSID);
                return dhcpResultsInfoRecord;
            }
            return null;
        }
    }

    public void updateDhcpResultsInfoCache(DhcpResults result) {
        StringBuffer ipstr = new StringBuffer();
        StringBuffer dhcpServerstr = new StringBuffer();
        ipstr.append(result.ipAddress);
        dhcpServerstr.append(result.serverAddress);
        DhcpResultsInfoRecord dhcpResultsInfo = new DhcpResultsInfoRecord(mPendingSSID, ipstr.toString(), dhcpServerstr.toString());
        this.mHwDhcpResultsInfo = dhcpResultsInfo;
        this.mtoEvictSSID = checkReachDBUpperLimit();
        if (mPendingSSID != null) {
            String pendingSSID = mPendingSSID;
            synchronized (mLock) {
                mDhcpResultsInfoCache.put(pendingSSID, dhcpResultsInfo);
                logd("updateDhcpResultsInfoCache add record for " + pendingSSID);
            }
            return;
        }
        logd("updateDhcpResultsInfoCache error PendingSSID is null");
    }

    public void removeDhcpResultsInfoCache() {
        if (mPendingSSID != null) {
            String pendingSSID = mPendingSSID;
            synchronized (mLock) {
                mDhcpResultsInfoCache.remove(pendingSSID);
                logd("removeDhcpResultsInfoCache remove record for " + pendingSSID);
            }
            return;
        }
        logd("removeDhcpResultsInfoCache error PendingSSID is null");
    }

    public void saveDhcpResultsInfotoDB() {
        logd("saveDhcpResultsInfotoDB");
        if (this.mtoEvictSSID != null) {
            this.mDBMgr.deleteDhcpResultsInfoRecord(this.mtoEvictSSID);
            this.mtoEvictSSID = null;
        }
        this.mDBMgr.addOrUpdateDhcpResultsInfoRecord(this.mHwDhcpResultsInfo);
    }

    public String checkReachDBUpperLimit() {
        synchronized (mLock) {
            int size = mDhcpResultsInfoCache.size();
            logd("mDhcpResultsInfoCache size is " + size);
            LinkedHashMap<String, DhcpResultsInfoRecord> map = new LinkedHashMap(mDhcpResultsInfoCache.snapshot());
        }
        if (size < 50) {
            logd("not Reach DB Upper Limit size 50");
            return null;
        }
        Entry<String, DhcpResultsInfoRecord> toEvict = map.eldest();
        if (toEvict == null) {
            logd("not acquire being evicted");
            return null;
        }
        logd("the ssid to be evicted is " + ((String) toEvict.getKey()));
        return (String) toEvict.getKey();
    }
}
