package com.huawei.keyguard.amazinglockscreen.data;

import android.content.Intent;
import com.huawei.keyguard.util.HwUnlockUtils;
import java.util.HashMap;

public class UnlockAppManager {
    private static UnlockAppManager sInstance = null;
    private HashMap<String, Intent> mIntents = new HashMap();

    public static UnlockAppManager getInstance() {
        if (sInstance == null) {
            sInstance = new UnlockAppManager();
        }
        return sInstance;
    }

    public static void clear() {
        if (sInstance != null) {
            sInstance.cleanIntents();
            sInstance = null;
        }
    }

    private void cleanIntents() {
        this.mIntents.clear();
    }

    private UnlockAppManager() {
        addIntent("camera", HwUnlockUtils.getCameraIntent());
    }

    public void addIntent(String type, Intent intent) {
        this.mIntents.put(type, intent);
    }

    public Intent getIntent(String type) {
        return (Intent) this.mIntents.get(type);
    }
}
