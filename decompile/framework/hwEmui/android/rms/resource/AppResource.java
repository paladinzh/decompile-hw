package android.rms.resource;

import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class AppResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.AppResource";
    private static final int TYPE_APP_PERMIT = 5;
    private static final int TYPE_CLEAR_DATA = 1;
    private static final int TYPE_DISABLE_APP = 2;
    private static final int TYPE_NOTIFY_CRASHINFO = 3;
    private static final int TYPE_NOTIFY_CRASHINFO_SYSAPP = 4;
    private static AppResource mAppResource;
    private HashSet<String> mAppLaunchedInfo = new HashSet();
    private HashMap<String, Integer> mAppResourceDoPolicyConfigs;
    private long mLifeTime;
    private int mOverloadNumber;
    private int mOverloadPeroid;
    private ResourceConfig[] mResourceConfig;
    private ResourceFlowControl mResourceFlowControl = new ResourceFlowControl();
    private HwSysResManager mResourceManger;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback = new Stub() {
        public void update() throws RemoteException {
            AppResource resource = AppResource.getInstance();
            if (resource == null) {
                Log.e(AppResource.TAG, "Notification Resource update get the instance is null");
                return;
            }
            ArrayList<String> whiteList = resource.getResWhiteList(19, 0);
            if (whiteList.size() > 0) {
                AppResource.this.mAppResourceDoPolicyConfigs.clear();
                AppResource.this.initAppResourceDoPolicyConfigs(whiteList);
            } else {
                Log.e(AppResource.TAG, "APP Resource update nameList failed!!!");
            }
        }
    };

    public AppResource() {
        if (!registerResourceCallback(this.mUpdateWhiteListCallback)) {
            Log.e(TAG, "APP Resource register callback failed");
        }
        getConfig(19);
        this.mAppResourceDoPolicyConfigs = new HashMap();
        ArrayList<String> whiteList = super.getResWhiteList(19, 0);
        if (whiteList.size() > 0) {
            initAppResourceDoPolicyConfigs(whiteList);
        }
    }

    public static synchronized AppResource getInstance() {
        AppResource appResource;
        synchronized (AppResource.class) {
            if (mAppResource == null) {
                mAppResource = new AppResource();
            }
            appResource = mAppResource;
        }
        return appResource;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        int strategy = 1;
        if (this.mResourceConfig == null) {
            return 1;
        }
        String pkg = args.getString("pkg");
        int callingUid = args.getInt("callingUid");
        Long startTime = Long.valueOf(args.getLong("startTime"));
        int typeID = args.getInt("processType");
        boolean launchfromActivity = args.getBoolean("launchfromActivity");
        boolean isTopProcess = args.getBoolean("topProcess");
        int crachTimeInterval = this.mResourceConfig[3].getLoopInterval();
        int shortTime = this.mResourceConfig[3].getResourceStrategy();
        this.mLifeTime = SystemClock.elapsedRealtime() - startTime.longValue();
        Integer doPolicyType = null;
        boolean blaunched = false;
        synchronized (this.mAppLaunchedInfo) {
            if (pkg != null) {
                blaunched = this.mAppLaunchedInfo.contains(pkg);
                if (blaunched && !launchfromActivity) {
                    this.mAppLaunchedInfo.remove(pkg);
                }
            }
        }
        if (typeID == 0 && launchfromActivity && ((this.mLifeTime <= ((long) shortTime) || (this.mLifeTime < ((long) crachTimeInterval) && this.mLifeTime > ((long) shortTime) && isTopProcess)) && blaunched)) {
            doPolicyType = (Integer) this.mAppResourceDoPolicyConfigs.get(pkg);
            if (doPolicyType != null && doPolicyType.intValue() == 5) {
                return 1;
            }
            doPolicyType = Integer.valueOf(3);
        } else if (typeID == 2) {
            doPolicyType = (Integer) this.mAppResourceDoPolicyConfigs.get(pkg);
            if (doPolicyType == null) {
                doPolicyType = Integer.valueOf(4);
            }
        }
        if (doPolicyType != null && isResourceSpeedOverload(callingUid, pkg, typeID)) {
            Bundle data = new Bundle();
            data.putInt("callingUid", callingUid);
            data.putString("pkg", pkg);
            this.mResourceManger.dispatch(19, new MultiTaskPolicy(doPolicyType.intValue(), data));
            strategy = getSpeedOverloadStrategy(typeID);
        }
        return strategy;
    }

    public int acquire(int callingUid, String pkg, int processType) {
        synchronized (this.mAppLaunchedInfo) {
            if (pkg != null) {
                if (processType > 0) {
                    this.mAppLaunchedInfo.add(pkg);
                } else if (this.mAppLaunchedInfo.contains(pkg)) {
                    this.mAppLaunchedInfo.remove(pkg);
                }
            }
        }
        return 1;
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
    }

    private void getConfig(int resourceType) {
        this.mResourceManger = HwSysResManager.getInstance();
        this.mResourceConfig = this.mResourceManger.getResourceConfig(resourceType);
        if (this.mResourceConfig == null) {
        }
    }

    private int getSpeedOverloadStrategy(int typeID) {
        return this.mResourceConfig[typeID].getResourceStrategy();
    }

    private boolean isResourceSpeedOverload(int callingUid, String pkg, int typeID) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int loopInterval = config.getLoopInterval();
        int maxPeroid = config.getResourceMaxPeroid();
        if (this.mResourceFlowControl.checkSpeedOverload(id, threshold, loopInterval)) {
            this.mOverloadPeroid = this.mResourceFlowControl.getOverloadPeroid(id);
            if (this.mOverloadPeroid >= maxPeroid) {
                if (typeID == 0) {
                    this.mOverloadNumber = (int) (this.mLifeTime / 1000);
                } else {
                    this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
                }
                this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 19, this.mOverloadNumber, this.mOverloadPeroid, 0);
                if (Log.HWINFO) {
                    Log.i(TAG, "speedOverload pkg=" + pkg + " id=" + id + " Threshold=" + threshold + " OverloadNum=" + this.mOverloadNumber + " MaxPeroid=" + maxPeroid + " OverloadPeroid=" + this.mOverloadPeroid);
                }
                return true;
            }
        }
        return false;
    }

    private void initAppResourceDoPolicyConfigs(ArrayList<String> whiteList) {
        int whiteListCount = whiteList.size();
        for (int i = 0; i < whiteListCount; i++) {
            String[] list = ((String) whiteList.get(i)).split(":");
            if (list.length == 2) {
                Integer policy = Integer.valueOf(Integer.parseInt(list[1]));
                if (((Integer) this.mAppResourceDoPolicyConfigs.get(list[0])) != null) {
                    this.mAppResourceDoPolicyConfigs.remove(list[0]);
                }
                this.mAppResourceDoPolicyConfigs.put(list[0], policy);
            }
        }
    }

    public int queryPkgPolicy(int type, int value, String key) {
        if (key == null) {
            return 0;
        }
        Integer policy = (Integer) this.mAppResourceDoPolicyConfigs.get(key);
        if (policy == null) {
            return 0;
        }
        return policy.intValue();
    }
}
