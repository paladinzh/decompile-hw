package com.android.settings;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.format.DateUtils;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TimeZoneUtil {
    private static String TAG = "TimeZoneUtil";

    public static class TimeZoneTips {
        private String zoneID;
        private String zoneName;
        private String zoneTime;

        public String getZoneName() {
            return this.zoneName;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        public String getZoneTime() {
            return this.zoneTime;
        }

        public void setZoneTime(String zoneTime) {
            this.zoneTime = zoneTime;
        }

        public String getZoneID() {
            return this.zoneID;
        }

        public void setZoneID(String zoneID) {
            this.zoneID = zoneID;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<TimeZoneTips> queryTimeZoneByCountry(String countryCode, Context context) {
        XmlResourceParser parser = context.getResources().getXml(2131230912);
        Appendable stringBuilder = new StringBuilder(50);
        Formatter formatter = new Formatter(stringBuilder, Locale.getDefault());
        long currentTime = System.currentTimeMillis();
        List<TimeZoneTips> tips = new ArrayList();
        try {
            beginDocument(parser, "timezones");
            while (parser.getEventType() != 1) {
                nextElement(parser);
                String name = parser.getName();
                if (name == null || !"timezone".equals(name)) {
                    break;
                }
                String code = parser.getAttributeValue(null, "code");
                if (countryCode == null || countryCode.equalsIgnoreCase(code)) {
                    String zoneID = parser.getAttributeValue(null, "id");
                    if (parser.next() == 4) {
                        String displayName = parser.getText();
                        TimeZoneTips tip = new TimeZoneTips();
                        tip.setZoneID(zoneID);
                        tip.setZoneName(displayName);
                        stringBuilder.setLength(0);
                        DateUtils.formatDateRange(context, formatter, currentTime, currentTime, 65557, zoneID);
                        tip.setZoneTime(stringBuilder.toString());
                        tips.add(tip);
                    }
                } else if (tips.size() > 0) {
                    break;
                }
            }
            parser.close();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Got exception while getting preferred time zone.", e);
        } catch (IOException e2) {
            Log.e(TAG, "Got exception while getting preferred time zone.", e2);
        } catch (Throwable th) {
            parser.close();
        }
        return tips;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getZoneNameByID(Context context, String zoneID) {
        if (zoneID == null || zoneID.isEmpty()) {
            return "";
        }
        XmlResourceParser parser = context.getResources().getXml(2131230912);
        String displayName = "";
        try {
            beginDocument(parser, "timezones");
            while (parser.getEventType() != 1) {
                nextElement(parser);
                String name = parser.getName();
                if (name != null && "timezone".equals(name)) {
                    if (zoneID.equals(parser.getAttributeValue(null, "id")) && parser.next() == 4) {
                        displayName = parser.getText();
                        break;
                    }
                } else {
                    break;
                }
            }
            parser.close();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Got exception while getting preferred time zone.", e);
        } catch (IOException e2) {
            Log.e(TAG, "Got exception while getting preferred time zone.", e2);
        } catch (Throwable th) {
            parser.close();
        }
        return displayName;
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new XmlPullParserException("No start tag found");
        } else if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                return;
            }
        } while (type != 1);
    }
}
