package com.android.systemui;

import android.app.Notification.Builder;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;

public abstract class SystemUI {
    public Map<Class<?>, Object> mComponents;
    public Context mContext;

    public abstract void start();

    protected void onConfigurationChanged(Configuration newConfig) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    protected void onBootCompleted() {
    }

    public <T> T getComponent(Class<T> interfaceType) {
        return this.mComponents != null ? this.mComponents.get(interfaceType) : null;
    }

    public <T, C extends T> void putComponent(Class<T> interfaceType, C component) {
        if (this.mComponents != null) {
            this.mComponents.put(interfaceType, component);
        }
    }

    public static void overrideNotificationAppName(Context context, Builder n) {
        Bundle extras = new Bundle();
        extras.putString("android.substName", context.getString(17039681));
        n.addExtras(extras);
    }
}
