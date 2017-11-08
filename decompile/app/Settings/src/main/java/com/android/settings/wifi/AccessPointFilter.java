package com.android.settings.wifi;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.util.Xml;
import com.android.settings.MLog;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AccessPointFilter {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void initialFilter(Context context) {
        FileNotFoundException e;
        Editor editor;
        int xmlEventType;
        XmlPullParserException e2;
        Throwable th;
        XmlPullParser xmlPullParser = null;
        InputStream inputStream = null;
        try {
            InputStream in = new FileInputStream("/data/cust/xml/access_point_filter.xml");
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(in, null);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        MLog.e("AccessPointFilter", "close FileInputStram error, error msg: " + e3.getMessage());
                        e3.printStackTrace();
                    }
                }
                inputStream = in;
            } catch (FileNotFoundException e4) {
                e = e4;
                inputStream = in;
                Log.w("AccessPointFilter", "Error while trying to read from access_point_filter", e);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e32) {
                        MLog.e("AccessPointFilter", "close FileInputStram error, error msg: " + e32.getMessage());
                        e32.printStackTrace();
                    }
                }
                if (xmlPullParser != null) {
                    try {
                        editor = context.getSharedPreferences("com.android.settings_accesspoints", 0).edit();
                        while (true) {
                            xmlEventType = xmlPullParser.next();
                            if (xmlEventType == 1) {
                                try {
                                    ((KXmlParser) xmlPullParser).close();
                                    return;
                                } catch (Exception e5) {
                                    MLog.e("AccessPointFilter", "close XmlPullParser error, error msg: " + e5.getMessage());
                                    e5.printStackTrace();
                                    return;
                                }
                            }
                            if (xmlEventType != 2) {
                            }
                            if (xmlEventType != 2) {
                                continue;
                            } else if ("numerics_filter".equals(xmlPullParser.getName())) {
                                editor.putString("numerics", xmlPullParser.getAttributeValue(null, "numerics"));
                                editor.commit();
                            }
                        }
                    } catch (Exception e52) {
                        Log.e("TAG", "Error while trying to read from access_point_filter", e52);
                        return;
                    } catch (Throwable th2) {
                        try {
                            ((KXmlParser) xmlPullParser).close();
                        } catch (Exception e522) {
                            MLog.e("AccessPointFilter", "close XmlPullParser error, error msg: " + e522.getMessage());
                            e522.printStackTrace();
                        }
                    }
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                inputStream = in;
                try {
                    Log.e("AccessPointFilter", "Error while trying to read from access_point_filter", e2);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e322) {
                            MLog.e("AccessPointFilter", "close FileInputStram error, error msg: " + e322.getMessage());
                            e322.printStackTrace();
                        }
                    }
                    if (xmlPullParser != null) {
                        editor = context.getSharedPreferences("com.android.settings_accesspoints", 0).edit();
                        while (true) {
                            xmlEventType = xmlPullParser.next();
                            if (xmlEventType == 1) {
                                if (xmlEventType != 2) {
                                }
                                if (xmlEventType != 2) {
                                    continue;
                                } else if ("numerics_filter".equals(xmlPullParser.getName())) {
                                    editor.putString("numerics", xmlPullParser.getAttributeValue(null, "numerics"));
                                    editor.commit();
                                }
                            } else {
                                ((KXmlParser) xmlPullParser).close();
                                return;
                            }
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3222) {
                            MLog.e("AccessPointFilter", "close FileInputStram error, error msg: " + e3222.getMessage());
                            e3222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            Log.w("AccessPointFilter", "Error while trying to read from access_point_filter", e);
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null) {
                editor = context.getSharedPreferences("com.android.settings_accesspoints", 0).edit();
                while (true) {
                    xmlEventType = xmlPullParser.next();
                    if (xmlEventType == 1) {
                        ((KXmlParser) xmlPullParser).close();
                        return;
                    }
                    if (xmlEventType != 2) {
                    }
                    if (xmlEventType != 2) {
                        continue;
                    } else if ("numerics_filter".equals(xmlPullParser.getName())) {
                        editor.putString("numerics", xmlPullParser.getAttributeValue(null, "numerics"));
                        editor.commit();
                    }
                }
            }
        } catch (XmlPullParserException e8) {
            e2 = e8;
            Log.e("AccessPointFilter", "Error while trying to read from access_point_filter", e2);
            if (inputStream != null) {
                inputStream.close();
            }
            if (xmlPullParser != null) {
                editor = context.getSharedPreferences("com.android.settings_accesspoints", 0).edit();
                while (true) {
                    xmlEventType = xmlPullParser.next();
                    if (xmlEventType == 1) {
                        if (xmlEventType != 2) {
                        }
                        if (xmlEventType != 2) {
                            continue;
                        } else if ("numerics_filter".equals(xmlPullParser.getName())) {
                            editor.putString("numerics", xmlPullParser.getAttributeValue(null, "numerics"));
                            editor.commit();
                        }
                    } else {
                        ((KXmlParser) xmlPullParser).close();
                        return;
                    }
                }
            }
        }
        if (xmlPullParser != null) {
            editor = context.getSharedPreferences("com.android.settings_accesspoints", 0).edit();
            while (true) {
                xmlEventType = xmlPullParser.next();
                if (xmlEventType == 1) {
                    ((KXmlParser) xmlPullParser).close();
                    return;
                } else if (xmlEventType != 2 && "filter".equals(xmlPullParser.getName())) {
                    editor.putString(xmlPullParser.getAttributeValue(null, "ssid"), xmlPullParser.getAttributeValue(null, "value"));
                    editor.commit();
                } else if (xmlEventType != 2) {
                    continue;
                } else if ("numerics_filter".equals(xmlPullParser.getName())) {
                    editor.putString("numerics", xmlPullParser.getAttributeValue(null, "numerics"));
                    editor.commit();
                }
            }
        }
    }
}
