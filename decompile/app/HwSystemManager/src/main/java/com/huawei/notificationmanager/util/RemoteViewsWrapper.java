package com.huawei.notificationmanager.util;

import android.widget.RemoteViews;
import java.util.ArrayList;

public class RemoteViewsWrapper {
    public ArrayList mActions = new ArrayList();
    private RemoteViews mInstance = null;

    public RemoteViewsWrapper(RemoteViews remoteView) {
        this.mInstance = remoteView;
        try {
            this.mActions = (ArrayList) RemoteViewsReflector.mAction.get(this.mInstance);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
    }
}
