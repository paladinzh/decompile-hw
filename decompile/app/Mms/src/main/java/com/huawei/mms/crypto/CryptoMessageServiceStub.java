package com.huawei.mms.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import com.huawei.cryptosms.ICryptoMessageClient;

public class CryptoMessageServiceStub {
    private static char ENCRYPT_FLAG_PAD = 'î';
    static ICryptoMessageClient mCallBack;
    private static boolean mSendFakeRegistResponse = SystemProperties.getBoolean("persist.config.hw_cr_s_regsms", false);

    public static int getState(Context context, int subId) {
        SharedPreferences preferences = context.getSharedPreferences("smsEncrypt", 0);
        if (subId == 0) {
            return preferences.getInt("sub0_state", 0);
        }
        return preferences.getInt("sub1_state", 0);
    }

    public static int activate(final Context context, final ICryptoMessageClient client, final int subId, final String accountName, String userId, String serviceToken) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    client.onMessage(1, 0, null);
                    Thread.sleep(3000);
                    client.onMessage(5, 0, null);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                Editor editor = context.getSharedPreferences("smsEncrypt", 0).edit();
                if (subId == 0) {
                    editor.putInt("sub0_state", 1);
                    editor.putString("sub0_name", accountName);
                } else {
                    editor.putInt("sub1_state", 1);
                    editor.putString("sub1_name", accountName);
                }
                editor.commit();
            }
        }).start();
        return 0;
    }

    public static int localDeactivate(Context context, ICryptoMessageClient client, int subId, String userName, String password) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Editor editor = context.getSharedPreferences("smsEncrypt", 0).edit();
        if (subId == 0) {
            editor.putInt("sub0_state", 3);
            editor.remove("sub0_name");
        } else {
            editor.putInt("sub1_state", 3);
            editor.remove("sub1_name");
        }
        editor.commit();
        return 0;
    }

    public static int deactivate(Context context, ICryptoMessageClient client, int subId, String userName, String password) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return 0;
    }

    public static String getCloudAccount(Context context, int subId) {
        SharedPreferences preferences = context.getSharedPreferences("smsEncrypt", 0);
        if (subId == 0) {
            return preferences.getString("sub0_name", "");
        }
        return preferences.getString("sub1_name", "");
    }

    public static void registerCallback(ICryptoMessageClient callback) {
        mCallBack = callback;
    }

    public static void unregisterCallback(ICryptoMessageClient callback) {
        mCallBack = null;
    }
}
