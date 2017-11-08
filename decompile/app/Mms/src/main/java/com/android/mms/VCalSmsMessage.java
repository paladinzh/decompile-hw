package com.android.mms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.android.mms.util.EventInfo;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.ArrayList;

public class VCalSmsMessage {
    public static String getVCalText(ArrayList<Uri> aEventUriList, Context aContext, long birthdayCalendarId) {
        StringBuilder sb = new StringBuilder();
        ArrayList<EventInfo> EventsList = getEventsList(aEventUriList, aContext, birthdayCalendarId);
        int len = EventsList.size();
        if (len < 1) {
            return null;
        }
        for (int i = 0; i < len; i++) {
            sb.append(((EventInfo) EventsList.get(i)).getString(aContext));
            if (i < len - 1) {
                sb.append("----------\r\n");
            }
        }
        return sb.toString();
    }

    private static ArrayList<EventInfo> getEventsList(ArrayList<Uri> aEventUriList, Context aContext, long birthdayCalendarId) {
        ArrayList<EventInfo> EventsList = new ArrayList();
        Cursor cursor = null;
        int index = 0;
        while (index < aEventUriList.size()) {
            try {
                cursor = SqliteWrapper.query(aContext, (Uri) aEventUriList.get(index), null, null, null, null);
                if (cursor == null) {
                    if (cursor == null) {
                        cursor.close();
                    } else {
                        index++;
                    }
                } else if (cursor.moveToFirst()) {
                    do {
                        EventInfo lEvent = new EventInfo();
                        lEvent.set(cursor, aContext, birthdayCalendarId);
                        EventsList.add(lEvent);
                    } while (cursor.moveToNext());
                    if (cursor == null) {
                        index++;
                    } else {
                        cursor.close();
                    }
                } else if (cursor != null) {
                    cursor.close();
                } else {
                    index++;
                }
                cursor = null;
                index++;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return EventsList;
    }
}
