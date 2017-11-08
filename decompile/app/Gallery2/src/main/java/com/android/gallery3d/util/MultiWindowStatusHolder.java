package com.android.gallery3d.util;

import android.app.Activity;
import java.util.HashSet;
import java.util.WeakHashMap;

public class MultiWindowStatusHolder {
    private static boolean sIsInMultiWindowMode = false;
    private static volatile WeakHashMap<IMultiWindowModeChangeListener, Object> sMultiWindowCallbackList = new WeakHashMap();

    public interface IMultiWindowModeChangeListener {
        void multiWindowModeChangeCallback(boolean z);
    }

    public static void updateMultiWindowMode(Activity activity) {
        boolean isMultiWindowMode = activity.isInMultiWindowMode();
        if (isMultiWindowMode != sIsInMultiWindowMode) {
            sIsInMultiWindowMode = isMultiWindowMode;
            notifyWindowFocusChanged(isMultiWindowMode);
        }
    }

    public static boolean isInMultiMaintained() {
        return sIsInMultiWindowMode;
    }

    public static boolean isInMultiWindowMode() {
        return sIsInMultiWindowMode;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void registerMultiWindowModeChangeListener(IMultiWindowModeChangeListener listener, boolean forceCallBackNow) {
        if (listener != null) {
            synchronized (sMultiWindowCallbackList) {
                if (sMultiWindowCallbackList.containsKey(listener)) {
                    return;
                }
                sMultiWindowCallbackList.put(listener, null);
                if (forceCallBackNow) {
                    listener.multiWindowModeChangeCallback(sIsInMultiWindowMode);
                }
            }
        }
    }

    public static void unregisterMultiWindowModeChangeListener(IMultiWindowModeChangeListener listener) {
        synchronized (sMultiWindowCallbackList) {
            if (listener == null) {
                return;
            }
            sMultiWindowCallbackList.remove(listener);
        }
    }

    private static void notifyWindowFocusChanged(boolean isInMultiWindowMode) {
        HashSet<IMultiWindowModeChangeListener> tmpListenerKeySet = new HashSet();
        synchronized (sMultiWindowCallbackList) {
            tmpListenerKeySet.addAll(sMultiWindowCallbackList.keySet());
        }
        for (IMultiWindowModeChangeListener l : tmpListenerKeySet) {
            l.multiWindowModeChangeCallback(isInMultiWindowMode);
        }
    }
}
