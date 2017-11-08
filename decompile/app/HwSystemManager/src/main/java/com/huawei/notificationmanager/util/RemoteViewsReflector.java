package com.huawei.notificationmanager.util;

import android.widget.RemoteViews;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/* compiled from: RemoteViewsWrapper */
class RemoteViewsReflector {
    public static final String FIELDNAME_MACTIONS = "mActions";
    public static final Class<RemoteViews> clazz = RemoteViews.class;
    public static Field mAction;

    RemoteViewsReflector() {
    }

    static {
        mAction = null;
        try {
            mAction = clazz.getDeclaredField(FIELDNAME_MACTIONS);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    RemoteViewsReflector.mAction.setAccessible(true);
                    return null;
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }
}
