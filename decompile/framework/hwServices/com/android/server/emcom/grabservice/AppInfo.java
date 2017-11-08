package com.android.server.emcom.grabservice;

import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppInfo {
    private static final boolean DEBUG = false;
    public Map<String, String> autograbParams = new HashMap();
    public String effectiveAutograbParam = AppHibernateCst.INVALID_PKG;
    public List<EventInfo> effectiveEvents = new ArrayList();
    public List<EventInfo> events = new ArrayList();
    public String packageName = AppHibernateCst.INVALID_PKG;
    int uid = -1;

    public static class EventInfo {
        String activity = AppHibernateCst.INVALID_PKG;
        String description = AppHibernateCst.INVALID_PKG;
        String eventClassName = AppHibernateCst.INVALID_PKG;
        int eventType;
        String packageName = AppHibernateCst.INVALID_PKG;
        String text = AppHibernateCst.INVALID_PKG;
        public int uid;
        public String version = AppHibernateCst.INVALID_PKG;

        public EventInfo(String packageName) {
            this.packageName = packageName;
        }
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public List<EventInfo> getEvents() {
        return this.events;
    }

    public void setEffectiveAutograbParam(String params) {
        this.effectiveAutograbParam = params;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("package name:" + this.packageName + ", ").append("uid:" + this.uid + ", ").append("events size: " + this.events.size()).append("autograb params size: " + this.autograbParams.size());
        buffer.append("event size:" + this.effectiveEvents.size());
        return buffer.toString();
    }
}
