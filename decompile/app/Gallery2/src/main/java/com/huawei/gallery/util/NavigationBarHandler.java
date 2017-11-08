package com.huawei.gallery.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import java.util.WeakHashMap;

public class NavigationBarHandler extends BroadcastReceiver {
    public static final boolean IS_SUPPORT_NAVIGATION_BAR = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private final int mBarHeightLand;
    private final int mBarHeightPort;
    private int mBarStatus = 0;
    private Context mContext;
    private WeakHashMap<Listener, Object> mListeners = new WeakHashMap();

    public interface Listener {
        void onNavigationBarChanged(boolean z, int i);
    }

    public void onReceive(Context context, Intent intent) {
        if ("com.huawei.navigationbar.statuschange".equals(intent.getAction())) {
            int status = intent.getBooleanExtra("minNavigationBar", false) ? 1 : 0;
            if (this.mBarStatus != status) {
                this.mBarStatus = status;
                notifyNavigationBarChanged();
            }
        }
    }

    public NavigationBarHandler(Context context) {
        this.mContext = context;
        this.mBarHeightPort = context.getResources().getDimensionPixelSize(context.getResources().getIdentifier("navigation_bar_height", "dimen", "android"));
        this.mBarHeightLand = context.getResources().getDimensionPixelSize(context.getResources().getIdentifier("navigation_bar_width", "dimen", "android"));
        register();
        update();
    }

    public void update() {
        updateStatus();
        notifyNavigationBarChanged();
    }

    private boolean updateStatus() {
        int currentStatus = Global.getInt(this.mContext.getContentResolver(), "navigationbar_is_min", 0);
        if (this.mBarStatus == currentStatus) {
            return false;
        }
        this.mBarStatus = currentStatus;
        return true;
    }

    public final int getHeight() {
        if (this.mBarStatus != 0 || GalleryUtils.isCVAAMode() || !IS_SUPPORT_NAVIGATION_BAR || MultiWindowStatusHolder.isInMultiWindowMode()) {
            return 0;
        }
        return LayoutHelper.isPort() ? this.mBarHeightPort : this.mBarHeightLand;
    }

    private synchronized void notifyNavigationBarChanged() {
        for (Listener l : this.mListeners.keySet()) {
            boolean z;
            if (this.mBarStatus == 0) {
                z = true;
            } else {
                z = false;
            }
            l.onNavigationBarChanged(z, getHeight());
        }
    }

    public synchronized void addListener(Listener l) {
        boolean z = false;
        synchronized (this) {
            Listener listener = (Listener) Utils.checkNotNull(l);
            if (this.mListeners.containsKey(listener)) {
                return;
            }
            this.mListeners.put(listener, null);
            updateStatus();
            if (this.mBarStatus == 0) {
                z = true;
            }
            listener.onNavigationBarChanged(z, getHeight());
        }
    }

    public synchronized void removeListener(Listener l) {
        Listener listener = (Listener) Utils.checkNotNull(l);
        if (this.mListeners.containsKey(listener)) {
            this.mListeners.remove(listener);
        }
    }

    void register() {
        this.mContext.registerReceiver(this, new IntentFilter("com.huawei.navigationbar.statuschange"), "android.permission.REBOOT", null);
    }
}
