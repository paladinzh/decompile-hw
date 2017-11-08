package android.rms;

import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.resource.ActivityResource;
import android.rms.resource.AlarmResource;
import android.rms.resource.AppOpsResource;
import android.rms.resource.AppResource;
import android.rms.resource.AppServiceResource;
import android.rms.resource.BroadcastResource;
import android.rms.resource.ContentObserverResource;
import android.rms.resource.CursorResource;
import android.rms.resource.NotificationResource;
import android.rms.resource.OrderedBroadcastObserveResource;
import android.rms.resource.PidsResource;
import android.rms.resource.ProviderResource;
import android.rms.resource.ReceiverResource;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HwSysResImpl implements HwSysResource {
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.HwSysResImpl";
    private static boolean enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    private static boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);

    public static HwSysResource getResource(int resourceType) {
        if (enableRms) {
            switch (resourceType) {
                case 10:
                    return NotificationResource.getInstance();
                case 12:
                    return ReceiverResource.getInstance();
                case 13:
                    return AlarmResource.getInstance();
                case 14:
                    return AppOpsResource.getInstance();
                case 15:
                    return ProviderResource.getInstance();
                case 16:
                    return PidsResource.getInstance();
                case 17:
                    return CursorResource.getInstance();
                case 18:
                    return AppServiceResource.getInstance();
                case 19:
                    return AppResource.getInstance();
                case 35:
                    return ContentObserverResource.getInstance();
                case 36:
                    return ActivityResource.getInstance();
            }
        }
        if (enableIaware) {
            switch (resourceType) {
                case 11:
                    return BroadcastResource.getInstance();
                case 37:
                    return OrderedBroadcastObserveResource.getInstance();
            }
        }
        return null;
    }

    public int acquire(int callingUid, String pkg, int processTpye) {
        return 1;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        return 1;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        return 1;
    }

    public int queryPkgPolicy(int type, int value, String key) {
        return 0;
    }

    public void release(int callingUid, String pkg, int processTpye) {
    }

    public void clear(int callingUid, String pkg, int processTpye) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
    }

    public Bundle query() {
        return null;
    }

    private static boolean isUidSystem(int uid, String pkg) {
        int appid = UserHandle.getAppId(uid);
        return (appid == 1000 || appid == 1001 || uid == 0) ? true : "android".equals(pkg);
    }

    private int isHuaweiApp(String pkg) {
        return pkg.contains("huawei") ? 1 : 0;
    }

    public int getTypeId(int callingUid, String pkg, int processTpye) {
        int typeID = processTpye;
        if (-1 != processTpye) {
            return typeID;
        }
        if (isUidSystem(callingUid, pkg)) {
            return 2;
        }
        if (pkg != null) {
            return isHuaweiApp(pkg);
        }
        return 0;
    }

    public long getResourceId(int callingUid, String pkg, int processTpye) {
        int uid;
        int typeID = getTypeId(callingUid, pkg, processTpye);
        if (3 == processTpye) {
            uid = -1;
        } else {
            uid = callingUid;
        }
        return (((long) typeID) << 32) + ((long) uid);
    }

    protected boolean registerResourceCallback(IUpdateWhiteListCallback updateCallback) {
        return HwSysResManager.getInstance().registerResourceCallback(updateCallback);
    }

    protected ArrayList<String> getResWhiteList(int resouceTpye, int type) {
        String[] whiteList = null;
        ArrayList<String> mList = new ArrayList();
        String configWhiteList = HwSysResManager.getInstance().getWhiteList(resouceTpye, type);
        if (configWhiteList != null) {
            whiteList = configWhiteList.split(";");
        }
        if (whiteList != null) {
            int i = 0;
            while (i < whiteList.length) {
                if (!(mList.contains(whiteList[i]) || whiteList[i].isEmpty())) {
                    mList.add(whiteList[i]);
                    if (Log.HWLog) {
                        Log.d(TAG, "getResWhiteList put the name into the list  type:" + resouceTpye + ", name:" + whiteList[i] + " , num:" + i);
                    }
                }
                i++;
            }
        }
        return mList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean isInWhiteList(String pkg, ArrayList<String> whiteList) {
        if (pkg == null || whiteList == null || !whiteList.contains(pkg)) {
            return false;
        }
        return true;
    }
}
