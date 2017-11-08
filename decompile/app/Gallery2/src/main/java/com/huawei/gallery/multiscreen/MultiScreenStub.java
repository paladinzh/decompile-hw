package com.huawei.gallery.multiscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.multiscreen.MultiScreen.Listener;
import java.util.ArrayList;
import java.util.List;

public class MultiScreenStub extends MultiScreen implements Runnable {
    private static final /* synthetic */ int[] -com-huawei-gallery-multiscreen-MultiScreenStub$OperationsSwitchesValues = null;
    private List<Listener> mCachedListeners = new ArrayList(2);
    private List<Operations> mCachedOperations = new ArrayList(10);
    private Context mContext;
    private Handler mHandler;
    private MediaItem mPhoto;
    private boolean mPlayForce;
    private MultiScreen mService;

    private enum Operations {
        ADD_LISTENER,
        RM_LISTENER,
        ENTER,
        EXIT,
        REQUEST_REFRESH_INFO,
        PLAY
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-multiscreen-MultiScreenStub$OperationsSwitchesValues() {
        if (-com-huawei-gallery-multiscreen-MultiScreenStub$OperationsSwitchesValues != null) {
            return -com-huawei-gallery-multiscreen-MultiScreenStub$OperationsSwitchesValues;
        }
        int[] iArr = new int[Operations.values().length];
        try {
            iArr[Operations.ADD_LISTENER.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Operations.ENTER.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Operations.EXIT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Operations.PLAY.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Operations.REQUEST_REFRESH_INFO.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Operations.RM_LISTENER.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-huawei-gallery-multiscreen-MultiScreenStub$OperationsSwitchesValues = iArr;
        return iArr;
    }

    public void initialize(Context context, Handler handler) {
        Utils.assertTrue(handler != null);
        this.mContext = context;
        this.mHandler = handler;
        handler.removeCallbacks(this);
        handler.postDelayed(this, 500);
    }

    public void addListener(Listener l) {
        synchronized (this.mCachedOperations) {
            if (this.mService == null) {
                cacheOpSync(Operations.ADD_LISTENER);
                this.mCachedListeners.add(l);
            } else {
                this.mService.addListener(l);
            }
        }
    }

    public void removeListener(Listener l) {
        synchronized (this.mCachedOperations) {
            if (this.mService == null) {
                this.mCachedListeners.remove(l);
            } else {
                this.mService.removeListener(l);
            }
        }
    }

    public void enter() {
        synchronized (this.mCachedOperations) {
            if (this.mService == null) {
                cacheOpSync(Operations.ENTER);
            } else {
                this.mService.enter();
            }
        }
    }

    public void requestRefreshInfo() {
        synchronized (this.mCachedOperations) {
            if (this.mService == null) {
                cacheOpSync(Operations.REQUEST_REFRESH_INFO);
            } else {
                this.mService.requestRefreshInfo();
            }
        }
    }

    public void exit() {
        synchronized (this.mCachedOperations) {
            if (this.mService == null) {
                cacheOpSync(Operations.EXIT);
            } else {
                this.mService.exit();
                this.mCachedListeners.clear();
                this.mPhoto = null;
            }
        }
    }

    public boolean play(MediaItem photo, boolean force) {
        synchronized (this.mCachedOperations) {
            if (this.mService == null) {
                this.mPhoto = photo;
                this.mPlayForce = force;
                boolean cacheOpSync = cacheOpSync(Operations.PLAY);
                return cacheOpSync;
            }
            cacheOpSync = this.mService.play(photo, force);
            return cacheOpSync;
        }
    }

    private boolean cacheOpSync(Operations op) {
        GalleryLog.d("MultiScreenStub", "cache Operations " + op);
        return this.mCachedOperations.add(op);
    }

    public Intent getDeviceSelectorInfo() {
        synchronized (this.mCachedOperations) {
            if (this.mService != null) {
                Intent deviceSelectorInfo = this.mService.getDeviceSelectorInfo();
                return deviceSelectorInfo;
            }
            return null;
        }
    }

    public void run() {
        TraceController.beginSection("MultScreenStub.run.initialize");
        synchronized (this.mCachedOperations) {
            if (checkShouldExitSync()) {
                return;
            }
            MultiScreen.initialize(this.mContext);
            MultiScreen service = MultiScreen.get();
            for (Operations op : this.mCachedOperations) {
                GalleryLog.d("MultiScreenStub", "do Operations " + op);
                switch (-getcom-huawei-gallery-multiscreen-MultiScreenStub$OperationsSwitchesValues()[op.ordinal()]) {
                    case 1:
                        for (Listener l : this.mCachedListeners) {
                            service.addListener(l);
                        }
                        break;
                    case 2:
                        service.enter();
                        break;
                    case 3:
                        service.exit();
                        break;
                    case 4:
                        if (this.mPhoto == null) {
                            break;
                        }
                        service.play(this.mPhoto, this.mPlayForce);
                        this.mPhoto = null;
                        break;
                    case 5:
                        service.requestRefreshInfo();
                        break;
                    default:
                        break;
                }
            }
            this.mCachedOperations.clear();
            this.mCachedListeners.clear();
            this.mService = service;
            TraceController.endSection();
        }
    }

    private boolean checkShouldExitSync() {
        boolean shouldExit = this.mCachedOperations.contains(Operations.EXIT);
        if (shouldExit) {
            GalleryLog.d("MultiScreenStub", "exited should exit and ignore cached operations.");
            this.mContext = null;
            this.mCachedOperations.clear();
            this.mCachedListeners.clear();
            this.mHandler.removeCallbacks(this);
        }
        return shouldExit;
    }

    public boolean hasServiceInited() {
        boolean z;
        synchronized (this.mCachedOperations) {
            z = this.mService != null;
        }
        return z;
    }

    public void resetService() {
        synchronized (this.mCachedOperations) {
            this.mService = null;
        }
    }
}
