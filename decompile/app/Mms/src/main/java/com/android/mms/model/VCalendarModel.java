package com.android.mms.model;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Time;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.Base64;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwSpecialUtils.HwDateUtils;
import com.huawei.mms.util.QuotedPrintable;
import com.huawei.mms.util.VCalParser;
import com.huawei.mms.util.VCalParser.Component;
import com.huawei.mms.util.VCalParser.Parameter;
import com.huawei.mms.util.VCalParser.Property;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.events.Event;

public class VCalendarModel extends MediaModel {
    private byte[] calendarData = null;
    private String mDescribtion;
    private String mEndTime;
    private String mEventTime;
    private String mLocation;
    private String mMultiTitle;
    private String mStartTime;
    private String mTitle;
    private String mVCalendarName;
    private int mVCalendarSize;

    public VCalendarModel(Context context, String tag, String contentType, String src, Uri uri) throws MmsException {
        super(context, tag, contentType, src, uri);
        init(context, uri);
    }

    public VCalendarModel(Context context, String contentType, Uri uri) throws MmsException {
        super(context, "vcalendar", contentType, "Invite.vcs", uri);
        init(context, uri);
    }

    private void init(Context context, Uri uri) throws MmsException {
        if (uri == null) {
            this.mVCalendarName = "Invite.vcs";
            this.mVCalendarSize = 0;
            return;
        }
        try {
            parseCalendarData(context, uri);
        } catch (Exception e) {
            MLog.v("VCalendarModel", "parseCalendarData error ");
        }
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream instanceof FileInputStream) {
                this.calendarData = new byte[inputStream.available()];
                MLog.d("VCalendarModel", "vcalendar read data number=" + inputStream.read(this.calendarData));
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    MLog.e("VCalendarModel", "IOException caught while closing stream", (Throwable) e2);
                }
            }
            this.mVCalendarName = getVCalendarNameFromUri(uri);
            if (this.calendarData != null) {
                this.mVCalendarSize = this.calendarData.length;
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    MLog.e("VCalendarModel", "IOException caught while closing stream", (Throwable) e22);
                }
            }
        }
    }

    private String decodeParameterValue(String aValue, String aEncoding, String aCharset) {
        String value = aValue;
        try {
            if ("QUOTED-PRINTABLE".equalsIgnoreCase(aEncoding)) {
                return QuotedPrintable.decodeQuotedPrintable(aValue.getBytes(aCharset), aCharset);
            }
            if ("BASE64".equalsIgnoreCase(aEncoding)) {
                return new String(Base64.decodeBase64(aValue.getBytes(aCharset)), aCharset);
            }
            return unfoldContentLineFolding(aValue);
        } catch (UnsupportedEncodingException e) {
            MLog.e("VCalendarModel", "UnsupportedEncodingException while aCharset decoding " + e);
            return value;
        }
    }

    private String unfoldContentLineFolding(String aValue) {
        if (aValue != null) {
            return aValue.replaceAll("\r\n ", "").replaceAll("\r\n\t", "");
        }
        return null;
    }

    private void parseCalendarData(Context context, Uri uri) throws MmsException {
        Throwable e;
        Throwable th;
        InputStream inputStream = null;
        StringBuilder iCSstr = new StringBuilder();
        DataInputStream dataInputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream instanceof FileInputStream) {
                DataInputStream dataInputStream2 = new DataInputStream(new BufferedInputStream(inputStream));
                while (true) {
                    try {
                        String line = dataInputStream2.readLine();
                        if (line == null) {
                            break;
                        }
                        iCSstr.append(line).append("\r\n");
                    } catch (Throwable th2) {
                        th = th2;
                        dataInputStream = dataInputStream2;
                    }
                }
                dataInputStream = dataInputStream2;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    MLog.e("VCalendarModel", "IOException caught while closing stream", (Throwable) e2);
                }
            }
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e22) {
                    MLog.e("VCalendarModel", "in IOException caught while closing stream", (Throwable) e22);
                }
            }
            Component component = null;
            try {
                component = VCalParser.parseCalendar(iCSstr.toString());
            } catch (Exception e3) {
                MLog.v("VCalendarModel", "parseCalendarData parseCalendar exception");
            }
            if (component != null) {
                List<Component> componentsList = component.getComponents();
                int size = componentsList.size();
                if (size == 1) {
                    Component subComponent = (Component) componentsList.get(0);
                    this.mTitle = getEncodingValue(subComponent, "SUMMARY");
                    if (this.mTitle == null) {
                        this.mTitle = this.mContext.getString(R.string.no_title);
                    }
                    this.mLocation = getEncodingValue(subComponent, "LOCATION");
                    this.mDescribtion = getEncodingValue(subComponent, "DESCRIPTION");
                    this.mStartTime = getValueWithException(subComponent, "DTSTART");
                    this.mEndTime = getValueWithException(subComponent, "DTEND");
                    String timezone = getValueWithException(subComponent, "TZ");
                    Time time = new Time("UTC");
                    time.parse(this.mStartTime);
                    if (timezone != null) {
                        time.switchTimezone(timezone);
                    }
                    this.mStartTime = HwDateUtils.formatChinaDateRange(this.mContext, time.toMillis(true), time.toMillis(true), 21);
                    this.mEventTime = HwDateUtils.formatChinaDateRange(this.mContext, time.toMillis(true), time.toMillis(true), 20);
                    time.parse(this.mEndTime);
                    if (timezone != null) {
                        time.switchTimezone(timezone);
                    }
                    this.mEndTime = HwDateUtils.formatChinaDateRange(this.mContext, time.toMillis(true), time.toMillis(true), 21);
                } else {
                    this.mMultiTitle = "";
                    for (int i = 0; i < size; i++) {
                        String title = getEncodingValue((Component) componentsList.get(i), "SUMMARY");
                        if (title == null) {
                            title = this.mContext.getString(R.string.no_title);
                        }
                        this.mMultiTitle += "," + title;
                    }
                }
            }
        } catch (Throwable th3) {
            e = th3;
            try {
                MLog.e("VCalendarModel", "IOException caught while opening or reading stream", e);
                throw new MmsException(e.getMessage());
            } catch (Throwable th4) {
                th = th4;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        MLog.e("VCalendarModel", "IOException caught while closing stream", (Throwable) e222);
                    }
                }
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e2222) {
                        MLog.e("VCalendarModel", "in IOException caught while closing stream", (Throwable) e2222);
                    }
                }
                throw th;
            }
        }
    }

    private String getEncodingValue(Component component, String s) {
        String encoding = null;
        try {
            String charSet;
            Property property = (Property) component.getProperties(s).get(0);
            List<Parameter> params = property.getParameters("ENCODING");
            if (params != null && params.size() > 0) {
                encoding = ((Parameter) params.get(0)).value;
            }
            params = property.getParameters("CHARSET");
            if (params != null && params.size() > 0) {
                charSet = ((Parameter) params.get(0)).value;
            } else if (HwMessageUtils.OPERATOR_SOFTBANK) {
                charSet = "SHIFT_JIS";
            } else {
                charSet = "UTF-8";
            }
            String encodingValude = property.getValue();
            if (TextUtils.isEmpty(encoding)) {
                return decodeParameterValue(HwMessageUtils.convertStringCharset(encodingValude, "ISO-8859-1", charSet), encoding, charSet);
            }
            return decodeParameterValue(encodingValude, encoding, charSet);
        } catch (Exception e) {
            MLog.i("VCalendarModel", "getPropertiesWithException");
            return null;
        }
    }

    private String getValueWithException(Component component, String s) {
        String value = null;
        try {
            value = ((Property) component.getProperties(s).get(0)).getValue();
        } catch (NullPointerException e) {
            MLog.i("VCalendarModel", "getPropertiesWithException : component.getProperties(s) = null");
        } catch (IndexOutOfBoundsException e2) {
            MLog.i("VCalendarModel", "getPropertiesWithException : component.getProperties(s).size() = 0");
        }
        return value;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getTime() {
        return this.mEventTime;
    }

    public String[] getVcalendarDetail() {
        if (this.mMultiTitle != null) {
            return new String[]{this.mMultiTitle};
        }
        List<String> detailList = new ArrayList();
        if (this.mTitle != null) {
            detailList.add(this.mTitle);
        }
        if (this.mLocation != null) {
            detailList.add(this.mLocation);
        }
        if (this.mStartTime != null) {
            detailList.add(this.mStartTime);
        }
        if (this.mEndTime != null) {
            detailList.add(this.mEndTime);
        }
        if (this.mDescribtion != null) {
            detailList.add(this.mDescribtion);
        }
        return (String[]) detailList.toArray(new String[detailList.size()]);
    }

    private String getVCalendarNameFromUri(Uri uri) {
        String name = uri.getLastPathSegment();
        int index = name.lastIndexOf(".");
        if (index <= 0) {
            return "Invite.vcs";
        }
        String extendName = name.substring(index + 1);
        MLog.d("VCalendarModel", "VCalendarModel->getVCalendarNameFromUri extendName is vcs? = " + extendName.equalsIgnoreCase("vcs"));
        if (extendName.equalsIgnoreCase("vcs")) {
            return name;
        }
        return "Invite.vcs";
    }

    public void handleEvent(Event evt) {
        notifyModelChanged(false);
    }

    public byte[] getVCalendarData() {
        return this.calendarData != null ? Arrays.copyOf(this.calendarData, this.calendarData.length) : null;
    }

    public String getEventTime() {
        return this.mEventTime;
    }
}
