package com.huawei.notificationmanager.util;

import android.app.Notification;
import android.widget.RemoteViews;
import com.huawei.systemmanager.comm.misc.Utility;
import java.util.ArrayList;

public class NotificationParser {
    private Notification mNotification = new Notification();

    public NotificationParser(Notification notification) {
        this.mNotification = notification;
    }

    public String getAllNotificationText() {
        return getTickerText() + parsetickerView() + parseContentView() + parseBigContentView();
    }

    public String getNotificationContent() {
        return parseContentView() + parseBigContentView();
    }

    public String parsetickerView() {
        return parseRemoteViews(this.mNotification.tickerView);
    }

    public String parseContentView() {
        return parseRemoteViews(this.mNotification.contentView);
    }

    public String parseBigContentView() {
        return parseRemoteViews(this.mNotification.bigContentView);
    }

    public String getTickerText() {
        if (this.mNotification.tickerText == null) {
            return "";
        }
        return addSpaceAfter(this.mNotification.tickerText.toString());
    }

    private String parseRemoteViews(RemoteViews remoteViews) {
        String res = "";
        if (remoteViews == null) {
            return res;
        }
        ArrayList<?> actionList = new RemoteViewsWrapper(remoteViews).mActions;
        if (Utility.isNullOrEmptyList(actionList)) {
            return res;
        }
        StringBuffer strBuffer = new StringBuffer();
        for (Object action : actionList) {
            if (isReflectionAction(action)) {
                strBuffer.append(addSpaceAfter(new ReflectionActionWrapper(action).getValue()));
            }
        }
        return strBuffer.toString();
    }

    private String addSpaceAfter(String thisText) {
        return thisText != null ? thisText + " " : "";
    }

    private boolean isReflectionAction(Object action) {
        return action.getClass() == ReflectionActionReflector.clazz;
    }
}
