package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MCVHandler {
    public static final boolean isORANGEMCV = SystemProperties.getBoolean("ro.config.hw_mcvenabled", false);
    private Context mContext;
    private String mGID1 = "";
    private String mMccMnc = "";
    private int mOrangeMVNO = -1;
    Map<String, MNO> mOrange_MNO = new HashMap();
    private String mSPN = "";

    private static class MNO {
        String gid;
        int imsi;
        int isemergency_incalllog;
        int mvnomaskbegin;
        int mvnomaskend;
        String spn;

        private MNO() {
            this.gid = "";
            this.spn = "";
            this.isemergency_incalllog = 1;
            this.imsi = -1;
            this.mvnomaskbegin = -1;
            this.mvnomaskend = -1;
        }
    }

    public MCVHandler() {
        loadOrangeMNO();
    }

    public void handleSIMChangeRequest(Context context, Intent intent) {
        this.mMccMnc = TelephonyManager.getDefault().getSimOperator();
        this.mGID1 = TelephonyManager.getDefault().getGroupIdLevel1();
        this.mSPN = TelephonyManager.getDefault().getSimOperatorName();
        this.mOrangeMVNO = getMVNOfromImsi(context);
        boolean isOrangeMNO = false;
        this.mContext = context;
        if (this.mOrange_MNO.containsKey(this.mMccMnc) && (checkforImsi(this.mMccMnc, this.mOrangeMVNO, context) || checkforGID(this.mMccMnc, this.mGID1) || checkforSPN(this.mMccMnc, this.mSPN))) {
            isOrangeMNO = true;
        }
        if (!isOrangeMNO) {
            resetEmergencyCallLogEntry(context);
        }
    }

    private void resetEmergencyCallLogEntry(Context context) {
        Systemex.putInt(context.getContentResolver(), "emergency_incalllog", -1);
    }

    void loadOrangeMNO() {
        if (isOrangeMNOXMLExist()) {
            loadOrangeMNOfromXML();
        } else {
            loadDefault();
        }
    }

    boolean checkforImsi(String mccmnc, int mvno, Context context) {
        MNO mno = (MNO) this.mOrange_MNO.get(mccmnc);
        if (mno.imsi != 1 || mvno < 0 || (mvno >= mno.mvnomaskbegin && mvno <= mno.mvnomaskend)) {
            return false;
        }
        if (mno.isemergency_incalllog == 0) {
            Systemex.putInt(context.getContentResolver(), "emergency_incalllog", 0);
        }
        Log.i("MCVHandler", "*** imsi check pass");
        return true;
    }

    private boolean checkforGID(String mccmnc, String gid) {
        boolean flag = false;
        MNO mno = (MNO) this.mOrange_MNO.get(mccmnc);
        if (mno.gid.equals("ff")) {
            if (gid == null || gid.length() == 0) {
                Log.i("MCVHandler", "*** nil gid");
                return false;
            }
            char[] array = gid.toCharArray();
            int i = 0;
            while (i < gid.length()) {
                if (array[i] != 'f' && array[i] != 'F') {
                    Log.i("MCVHandler", "*** gid check fail");
                    flag = false;
                    break;
                }
                flag = true;
                i++;
            }
            if (flag) {
                Systemex.putInt(this.mContext.getContentResolver(), "emergency_incalllog", mno.isemergency_incalllog);
            }
        }
        return flag;
    }

    private boolean checkforSPN(String mccmnc, String spn) {
        MNO mno = (MNO) this.mOrange_MNO.get(mccmnc);
        if (!mno.spn.equals(spn)) {
            return false;
        }
        Systemex.putInt(this.mContext.getContentResolver(), "emergency_incalllog", mno.isemergency_incalllog);
        return true;
    }

    private boolean isOrangeMNOXMLExist() {
        if (new File(getPathEx()).exists()) {
            return true;
        }
        return false;
    }

    private int getMVNOfromImsi(Context context) {
        String subscriberId = TelephonyManager.from(context).getSubscriberId();
        if (subscriberId != null) {
            return Integer.parseInt(subscriberId.substring(8, 11));
        }
        return -1;
    }

    private String getPathEx() {
        return "/data/cust/xml/orange_mno.xml";
    }

    public void loadOrangeMNOfromXML() {
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new FileInputStream(new File(getPathEx()).getAbsolutePath());
            try {
                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(in, null);
                this.mOrange_MNO.clear();
                for (int xmlEventType = xml.next(); xmlEventType != 1; xmlEventType = xml.next()) {
                    switch (xmlEventType) {
                        case 2:
                            if (!"orange_identifier".equals(xml.getName())) {
                                break;
                            }
                            MNO mvno = new MNO();
                            String mccmnc = xml.getAttributeValue(null, "mccmnc");
                            mvno.gid = xml.getAttributeValue(null, "gid");
                            mvno.spn = xml.getAttributeValue(null, "spn");
                            mvno.imsi = Integer.parseInt(xml.getAttributeValue(null, "imsi"));
                            if (mvno.imsi == 1) {
                                mvno.mvnomaskbegin = Integer.parseInt(xml.getAttributeValue(null, "mvnomask_begin"));
                                mvno.mvnomaskend = Integer.parseInt(xml.getAttributeValue(null, "mvnomask_end"));
                            }
                            mvno.isemergency_incalllog = Integer.parseInt(xml.getAttributeValue(null, "allow_emergency_numbers_in_call_log"));
                            this.mOrange_MNO.put(mccmnc, mvno);
                            break;
                        case 3:
                            if (xmlEventType == 3) {
                                if (!"orange_mno_list".equals(xml.getName())) {
                                    break;
                                }
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e) {
                                        Log.e("MCVHandler", "An error occurs attempting to close this stream!");
                                    }
                                }
                                return;
                            }
                            continue;
                        default:
                            break;
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e2) {
                        Log.e("MCVHandler", "An error occurs attempting to close this stream!");
                    }
                }
            } catch (XmlPullParserException e3) {
                inputStream = in;
            } catch (FileNotFoundException e4) {
                inputStream = in;
            } catch (IOException e5) {
                inputStream = in;
            } catch (Throwable th2) {
                th = th2;
                inputStream = in;
            }
        } catch (XmlPullParserException e6) {
            try {
                Log.e("MCVHandler", "Parser xml file XmlPullParserException!");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e("MCVHandler", "An error occurs attempting to close this stream!");
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        Log.e("MCVHandler", "An error occurs attempting to close this stream!");
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            Log.e("MCVHandler", "FileNotFoundException : could not find xml file");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e10) {
                    Log.e("MCVHandler", "An error occurs attempting to close this stream!");
                }
            }
        } catch (IOException e11) {
            Log.e("MCVHandler", "Parser xml file IOException!");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e12) {
                    Log.e("MCVHandler", "An error occurs attempting to close this stream!");
                }
            }
        }
    }

    void loadDefault() {
        this.mOrange_MNO.clear();
        MNO mvnogid = new MNO();
        mvnogid.gid = "ff";
        this.mOrange_MNO.put("20801", mvnogid);
        MNO mvnospn = new MNO();
        mvnospn.spn = "Orange";
        this.mOrange_MNO.put("26003", mvnospn);
        this.mOrange_MNO.put("22610", mvnospn);
        this.mOrange_MNO.put("23101", mvnospn);
        this.mOrange_MNO.put("21403", mvnospn);
    }
}
