package com.huawei.gallery.multiscreen;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import com.android.gallery3d.data.MediaItem;
import com.huawei.android.airsharing.util.PlayerUtil;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiScreen {
    private static final boolean SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiscreen", false);
    private static volatile MultiScreen mInstance;
    static AtomicBoolean serviceReady = new AtomicBoolean(false);

    public interface Listener {
        void onMediaPause();

        void onMediaPlay();

        void onMediaPositionChange();

        void onMediaStop(String str);

        void onUnInitServiceTimeOut();

        void onUpdateActionItem(boolean z, boolean z2);

        void requestMedia();
    }

    public static class MultiScreenListener implements Listener {
        public void onUpdateActionItem(boolean existed, boolean rendering) {
        }

        public void onMediaPositionChange() {
        }

        public void onMediaPause() {
        }

        public void onMediaPlay() {
        }

        public void onMediaStop(String type) {
        }

        public void requestMedia() {
        }

        public void onUnInitServiceTimeOut() {
        }
    }

    public static void initialize(Context context) {
        if (!serviceReady.get() && context != null) {
            if (isSupportMultiScreenFeature(context)) {
                MultiScreenUtils.initialize(context);
                MultiScreenManager.getInstance().init(context);
            } else {
                serviceReady.lazySet(false);
            }
        }
    }

    public static boolean isSupportMultiScreenFeature(Context context) {
        boolean z = false;
        try {
            if (SUPPORTED) {
                z = PlayerUtil.isSupportMultiscreen(context);
            }
            return z;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static MultiScreen get() {
        if (serviceReady.get()) {
            return MultiScreenManager.getInstance();
        }
        if (mInstance == null) {
            mInstance = new MultiScreen();
        }
        return mInstance;
    }

    public boolean play(MediaItem item, boolean force) {
        return false;
    }

    public boolean play(Uri uri, int position) {
        return false;
    }

    public void enter() {
    }

    public void exit() {
    }

    public Intent getDeviceSelectorInfo() {
        return null;
    }

    public void requestRefreshInfo() {
    }

    public void addListener(Listener l) {
    }

    public void removeListener(Listener l) {
    }
}
