package com.huawei.netassistant.ext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.telephony.SmsManager;
import android.text.TextUtils;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.systemmanager.util.HwLog;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class SimCardManagerExt {
    private static final String SIM_KEY_PREFIX_FILE_NAME = "sim_card_name_hsm";
    public static final String SIM_NAME_KEY_PREFIX = "sim_card_name_";
    private static final String TAG = "SimCardManagerExt";

    public static String getOperatorNameFromPlatform(int slotId, String imsi, Context context) {
        String name = getOperatorNameOnQcom(imsi, context.getContentResolver());
        if (name == null) {
            return getOperatorNameOnMTK(slotId, context);
        }
        return name;
    }

    public static String getOperatorNameOnQcom(String imsi, ContentResolver cr) {
        String name = Global.getString(cr, SIM_NAME_KEY_PREFIX + encode(imsi));
        if (TextUtils.isEmpty(name)) {
            return Global.getString(cr, SIM_NAME_KEY_PREFIX + imsi);
        }
        return name;
    }

    public static String getOperatorNameOnPreference(int slotType, Context context) {
        if (context == null) {
            HwLog.w(TAG, "getOperatorNameOnPreference() context is null");
            return "";
        } else if (slotType == 0 || 1 == slotType) {
            SharedPreferences sf = context.getSharedPreferences(SIM_KEY_PREFIX_FILE_NAME, 0);
            return sf != null ? sf.getString(SIM_NAME_KEY_PREFIX + slotType, "") : "";
        } else {
            HwLog.w(TAG, "slotType is " + slotType);
            return "";
        }
    }

    public static boolean setOperatorNameToPreference(int slotType, String name, Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w(TAG, "setOperatorNameToPreference()-- context is null");
            return false;
        } else if (slotType == 0 || 1 == slotType) {
            SharedPreferences sf = context.getSharedPreferences(SIM_KEY_PREFIX_FILE_NAME, 0);
            if (sf != null) {
                z = sf.edit().putString(SIM_NAME_KEY_PREFIX + slotType, name).commit();
            }
            return z;
        } else {
            HwLog.w(TAG, "setOperatorNameToPreference()--slotType is " + slotType);
            return false;
        }
    }

    private static String encode(String val) {
        if (val == null || val.length() == 0) {
            return val;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(val.getBytes("UTF-8"));
            return byte2hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        for (byte b2 : b) {
            stmp = Integer.toHexString(b2 & 255);
            if (stmp.length() == 1) {
                hs = hs.append("0").append(stmp);
            } else {
                hs = hs.append(stmp);
            }
        }
        return hs.toString();
    }

    private static String getOperatorNameOnMTK(int slotId, Context context) {
        String name = null;
        try {
            Class<?> classSimInfoManager = Class.forName("com.mediatek.telephony.SimInfoManager");
            Class<?> classSimInfoRecord = Class.forName("com.mediatek.telephony.SimInfoManager$SimInfoRecord");
            Field filedSimSlotId = classSimInfoRecord.getField("mSimSlotId");
            Field filedmDisplayName = classSimInfoRecord.getField("mDisplayName");
            Object obj = classSimInfoManager.getMethod("getInsertedSimInfoList", new Class[]{Context.class}).invoke(null, new Object[]{context});
            if (obj == null) {
                return null;
            }
            List<?> simInfoList = (List) obj;
            int N = simInfoList.size();
            for (int i = 0; i < N; i++) {
                if (filedSimSlotId.getInt(simInfoList.get(i)) == slotId) {
                    name = (String) filedmDisplayName.get(simInfoList.get(i));
                    break;
                }
            }
            HwLog.d(TAG, "/getOperatorNameOnMTK name is: " + name);
            return name;
        } catch (ClassNotFoundException e) {
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (IllegalArgumentException e5) {
            e5.printStackTrace();
        } catch (InvocationTargetException e6) {
            e6.printStackTrace();
        }
    }

    public static boolean sendMessageFromPlatform(String number, ArrayList<String> sms, int slot, Context context) {
        boolean success = sendMultipartTextMessageOnQcom(number, sms, slot);
        HwLog.d(TAG, "send message on QCOM" + success);
        if (!success) {
            success = sendMultipartTextMessageOnMTK(number, sms, slot, context);
            HwLog.d(TAG, "send message on MTK" + success);
        }
        HwLog.d(TAG, "send message " + success);
        return success;
    }

    private static boolean sendMultipartTextMessageOnMTK(String number, ArrayList<String> sms, int slot, Context context) {
        try {
            Class<?> classSmsManager = Class.forName("android.telephony.SmsManager");
            Class<?> classSmsManagerEx = Class.forName("com.mediatek.telephony.SmsManagerEx");
            Field fieldValidityPeriod = classSmsManager.getField("VALIDITY_PERIOD_NO_DURATION");
            Method methodSmsManagerDefault = classSmsManagerEx.getMethod("getDefault", new Class[0]);
            Method methodSmsManagerSendMessage = classSmsManagerEx.getMethod("sendMultipartTextMessageWithExtraParams", new Class[]{String.class, String.class, ArrayList.class, Bundle.class, ArrayList.class, ArrayList.class, Integer.TYPE});
            int vailidity = PreferenceManager.getDefaultSharedPreferences(context).getInt(Long.toString((long) slot) + "_" + "pref_key_sms_validity_period", fieldValidityPeriod.getInt(null));
            new Bundle().putInt("validity_period", vailidity);
            methodSmsManagerSendMessage.invoke(methodSmsManagerDefault.invoke(null, (Object[]) null), new Object[]{number, null, sms, extra, null, null, Integer.valueOf(slot)});
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return false;
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
            return false;
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
            return false;
        } catch (IllegalArgumentException e5) {
            e5.printStackTrace();
            return false;
        } catch (InvocationTargetException e6) {
            e6.printStackTrace();
            return false;
        }
    }

    private static boolean sendMultipartTextMessageOnQcom(String number, ArrayList<String> sms, int slot) {
        boolean success = false;
        try {
            Class<?> classMSimSmsManager = Class.forName("android.telephony.MSimSmsManager");
            Method methodMSSMGetDefault = classMSimSmsManager.getMethod("getDefault", new Class[0]);
            Method methodMSSMSendMultipartMessage = classMSimSmsManager.getMethod("sendMultipartTextMessage", new Class[]{String.class, String.class, ArrayList.class, ArrayList.class, ArrayList.class, Integer.TYPE});
            if (SimCardManager.getInstance().isPhoneSupportDualCard()) {
                methodMSSMSendMultipartMessage.invoke(methodMSSMGetDefault.invoke(null, (Object[]) null), new Object[]{number, null, sms, null, null, Integer.valueOf(slot)});
                success = true;
            } else {
                SmsManager.getDefault().sendMultipartTextMessage(number, null, sms, null, null);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
        HwLog.d(TAG, "sendMultipartTextMessageOnQcom success:" + success);
        return success;
    }
}
