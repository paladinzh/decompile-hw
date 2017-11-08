package android.rms.iaware;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.ICMSManager.Stub;
import android.rms.iaware.utils.AppTypeRecoUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import java.util.List;
import java.util.Set;

public class AppTypeRecoManager {
    public static final String APP_PKGNAME = "pkgName";
    public static final String APP_STATUS = "appsSatus";
    public static final String APP_TYPE = "appType";
    private static final String TAG = "AppTypeRecoManager";
    private static AppTypeRecoManager mAppTypeRecoManager = null;
    private final ArrayMap<String, Integer> mAppsTypeMap = new ArrayMap();

    public static synchronized AppTypeRecoManager getInstance() {
        AppTypeRecoManager appTypeRecoManager;
        synchronized (AppTypeRecoManager.class) {
            if (mAppTypeRecoManager == null) {
                mAppTypeRecoManager = new AppTypeRecoManager();
            }
            appTypeRecoManager = mAppTypeRecoManager;
        }
        return appTypeRecoManager;
    }

    public void init(Context ctx) {
        if (ctx != null) {
            ContentResolver resolver = ctx.getContentResolver();
            ArrayMap<String, Integer> map = new ArrayMap();
            AppTypeRecoUtils.loadAppType(resolver, map);
            synchronized (this.mAppsTypeMap) {
                this.mAppsTypeMap.putAll(map);
            }
        }
    }

    public boolean loadInstalledAppTypeInfo() {
        List<AppTypeInfo> list = null;
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                ParceledListSlice<AppTypeInfo> slice = awareservice.getAllAppTypeInfo();
                if (slice != null) {
                    list = slice.getList();
                }
            } else {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "loadAppTypeInfo RemoteException");
        }
        if (list == null) {
            return false;
        }
        for (AppTypeInfo info : list) {
            addAppType(info.getPkgName(), info.getType());
        }
        return true;
    }

    public int getAppType(String pkgName) {
        int appType = -1;
        synchronized (this.mAppsTypeMap) {
            if (this.mAppsTypeMap.containsKey(pkgName)) {
                appType = ((Integer) this.mAppsTypeMap.get(pkgName)).intValue();
            }
        }
        return appType;
    }

    public boolean containsAppType(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mAppsTypeMap) {
            if (this.mAppsTypeMap.containsKey(pkgName)) {
                return true;
            }
            return false;
        }
    }

    public Set<String> getAppsByType(int appType) {
        synchronized (this.mAppsTypeMap) {
            ArrayMap<String, Integer> appList = new ArrayMap(this.mAppsTypeMap);
        }
        ArraySet<String> appSet = new ArraySet();
        for (int i = 0; i < appList.size(); i++) {
            if (((Integer) appList.valueAt(i)).intValue() == appType) {
                appSet.add((String) appList.keyAt(i));
            }
        }
        return appSet;
    }

    public Set<String> getAlarmApps() {
        synchronized (this.mAppsTypeMap) {
            ArrayMap<String, Integer> appList = new ArrayMap(this.mAppsTypeMap);
        }
        ArraySet<String> appSet = new ArraySet();
        int i = 0;
        while (i < appList.size()) {
            if (((Integer) appList.valueAt(i)).intValue() == 5 || ((Integer) appList.valueAt(i)).intValue() == AppTypeInfo.PG_APP_TYPE_ALARM) {
                appSet.add((String) appList.keyAt(i));
            }
            i++;
        }
        return appSet;
    }

    public void removeAppType(String pkgName) {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.remove(pkgName);
        }
    }

    public void addAppType(String pkgName, int type) {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.put(pkgName, Integer.valueOf(type));
        }
    }

    public void clearAppsType() {
        synchronized (this.mAppsTypeMap) {
            this.mAppsTypeMap.clear();
        }
    }
}
