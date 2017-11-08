package com.android.mms.util;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.Time;
import cn.com.xy.sms.sdk.db.TrainManager;
import com.android.calendarcommon2.DateException;
import com.android.calendarcommon2.Duration;
import com.android.calendarcommon2.EventRecurrence;
import com.google.android.gms.R;
import com.huawei.mms.util.HwSpecialUtils.HwDateUtils;
import java.util.Formatter;
import java.util.Locale;

public class EventInfo {
    private Context mContext;
    public String mDescription;
    public long mDtend;
    public long mDtstart;
    public String mDuration;
    public String mEventLocation;
    private EventRecurrence mEventRecurrence;
    public String mFrom;
    public boolean mIsAllday;
    public String mOn;
    public String mRule;
    public String mTitle;
    public String mTo;
    public String mTz;

    public void set(Cursor aCursor, Context aContext, long birthdayCalendarId) {
        if (aCursor != null) {
            String currentTimezone;
            this.mContext = aContext;
            this.mEventLocation = aCursor.getString(aCursor.getColumnIndex("eventLocation"));
            this.mTitle = aCursor.getString(aCursor.getColumnIndex("title"));
            this.mIsAllday = 1 == aCursor.getInt(aCursor.getColumnIndex("allDay"));
            this.mTz = aCursor.getString(aCursor.getColumnIndex("eventTimeZone"));
            if (TextUtils.isEmpty(this.mTz)) {
                currentTimezone = Time.getCurrentTimezone();
            } else {
                currentTimezone = this.mTz;
            }
            this.mTz = currentTimezone;
            this.mDtstart = aCursor.getLong(aCursor.getColumnIndex("dtstart"));
            this.mDtend = aCursor.getLong(aCursor.getColumnIndex("dtend"));
            this.mDuration = aCursor.getString(aCursor.getColumnIndex(TrainManager.DURATION));
            if (0 == this.mDtend && this.mDtend < this.mDtstart) {
                if (TextUtils.isEmpty(this.mDuration)) {
                    this.mDtend = this.mDtstart;
                } else {
                    try {
                        Duration d = new Duration();
                        d.parse(this.mDuration);
                        this.mDtend = this.mDtstart + d.getMillis();
                    } catch (DateException e) {
                        this.mDtend = this.mDtstart;
                        e.printStackTrace();
                    }
                    this.mRule = aCursor.getString(aCursor.getColumnIndex("rrule"));
                    if (this.mRule != null) {
                        this.mRule = parserRule(this.mRule);
                        this.mDescription = this.mRule;
                    }
                }
            }
            this.mFrom = HwDateUtils.formatChinaDateRange(aContext, new Formatter(new StringBuilder(50), Locale.getDefault()), this.mDtstart, this.mDtstart, 21, this.mTz);
            this.mTo = HwDateUtils.formatChinaDateRange(aContext, new Formatter(new StringBuilder(50), Locale.getDefault()), this.mDtend, this.mDtend, 21, this.mTz);
            this.mOn = HwDateUtils.formatChinaDateRange(aContext, new Formatter(new StringBuilder(50), Locale.getDefault()), this.mDtstart, this.mDtstart, 20, this.mTz);
            if (aCursor.getLong(aCursor.getColumnIndex("calendar_id")) == birthdayCalendarId) {
                this.mDescription = aContext.getResources().getString(R.string.vcard_birthday);
                return;
            }
            StringBuffer description = new StringBuffer(this.mDescription == null ? "" : this.mDescription);
            if (this.mRule != null) {
                description.append("\r\n");
            }
            String tempStr = aCursor.getString(aCursor.getColumnIndex("description"));
            if (tempStr != null) {
                this.mDescription = description.append(tempStr).toString();
            }
        }
    }

    private String parserRule(String mRule2) {
        this.mEventRecurrence = new EventRecurrence();
        this.mEventRecurrence.parse(mRule2);
        Time date = new Time(Time.getCurrentTimezone());
        date.set(this.mDtstart);
        if (this.mIsAllday) {
            date.timezone = "UTC";
        }
        this.mEventRecurrence.setStartDate(date);
        return EventRecurrenceFormatter.getRepeatString(this.mContext.getResources(), this.mEventRecurrence);
    }

    public String getString(Context aContext) {
        Resources resource = aContext.getResources();
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(this.mTitle)) {
            sb.append(resource.getString(R.string.event_name)).append(": ");
            sb.append(this.mTitle);
            sb.append("\r\n");
        }
        if (!TextUtils.isEmpty(this.mEventLocation)) {
            sb.append(resource.getString(R.string.location)).append(": ");
            sb.append(this.mEventLocation);
            sb.append("\r\n");
        }
        if (!this.mIsAllday) {
            if (!TextUtils.isEmpty(this.mFrom)) {
                sb.append(resource.getString(R.string.event_from)).append(": ").append(this.mFrom);
                sb.append("\r\n");
            }
            if (!TextUtils.isEmpty(this.mTo)) {
                sb.append(resource.getString(R.string.event_to)).append(": ").append(this.mTo).append("");
                sb.append("\r\n");
            }
        } else if (this.mDtstart != 0) {
            sb.append(this.mOn).append(" ").append(resource.getString(R.string.event_all_day));
            sb.append("\r\n");
        }
        if (!TextUtils.isEmpty(this.mDescription)) {
            sb.append(resource.getString(R.string.event_description)).append(": ");
            sb.append(this.mDescription);
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
