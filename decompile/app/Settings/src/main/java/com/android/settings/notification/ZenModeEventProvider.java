package com.android.settings.notification;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.CalendarContract.Calendars;
import android.service.notification.ZenModeConfig.EventInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ZenModeEventProvider {
    public static final Comparator<CalendarInfo> CALENDAR_NAME = new Comparator<CalendarInfo>() {
        public int compare(CalendarInfo lhs, CalendarInfo rhs) {
            return lhs.name.compareTo(rhs.name);
        }
    };
    private static ZenModeEventProvider mZenModeEventProvider;

    public static class CalendarInfo {
        public String accountType;
        public String displayName;
        public String name;
        public int userId;
    }

    public static ZenModeEventProvider getInstance() {
        if (mZenModeEventProvider == null) {
            mZenModeEventProvider = new ZenModeEventProvider();
        }
        return mZenModeEventProvider;
    }

    public static String getEventType(Context context, EventInfo event) {
        CalendarInfo info = findCalendar(context, event);
        if (info != null) {
            return info.displayName;
        }
        return context.getResources().getString(2131626798);
    }

    private static CalendarInfo findCalendar(Context context, EventInfo event) {
        if (context == null || event == null) {
            return null;
        }
        String eventKey = key(event);
        for (CalendarInfo calendar : getCalendars(context)) {
            if (eventKey.equals(key(calendar))) {
                return calendar;
            }
        }
        return null;
    }

    public static List<CalendarInfo> getCalendars(Context context) {
        List<CalendarInfo> calendars = new ArrayList();
        for (UserHandle user : UserManager.get(context).getUserProfiles()) {
            Context userContext = getContextForUser(context, user);
            if (userContext != null) {
                addCalendars(userContext, calendars);
            }
        }
        Collections.sort(calendars, CALENDAR_NAME);
        return calendars;
    }

    private static Context getContextForUser(Context context, UserHandle user) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, user);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private static void addCalendars(Context context, List<CalendarInfo> outCalendars) {
        String primary = "\"primary\"";
        String selection = "\"primary\" = 1";
        Cursor cursor = null;
        cursor = context.getContentResolver().query(Calendars.CONTENT_URI, new String[]{"_id", "calendar_displayName", "account_type", "(account_name=ownerAccount) AS \"primary\""}, "\"primary\" = 1", null, null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        while (cursor.moveToNext()) {
            try {
                String displayName = cursor.getString(1);
                String accountType = cursor.getString(2);
                if (!"com.android.huawei.birthday".equals(accountType)) {
                    if ("com.android.huawei.phone".equals(accountType)) {
                        Resources res = context.getPackageManager().getResourcesForApplication("com.android.calendar");
                        int id = res.getIdentifier("calendar_phone_name", "string", "com.android.calendar");
                        if (id > 0) {
                            displayName = res.getString(id);
                        }
                    }
                    CalendarInfo ci = new CalendarInfo();
                    ci.name = cursor.getString(1);
                    ci.accountType = accountType;
                    ci.displayName = displayName;
                    ci.userId = context.getUserId();
                    outCalendars.add(ci);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public static String key(CalendarInfo calendar) {
        return key(calendar.userId, calendar.name);
    }

    public static String key(EventInfo event) {
        return key(event.userId, event.calendar);
    }

    public static String key(int userId, String calendar) {
        StringBuilder append = new StringBuilder().append(EventInfo.resolveUserId(userId)).append(":");
        if (calendar == null) {
            calendar = "";
        }
        return append.append(calendar).toString();
    }
}
