package com.android.settingslib.bluetooth;

import android.content.Context;
import com.android.settingslib.R$string;
import java.util.HashMap;
import java.util.Map.Entry;

public class Utils {
    private static ErrorListener sErrorListener;

    public interface ErrorListener {
        void onShowError(Context context, String str, int i);
    }

    public static int getConnectionStateSummary(int connectionState) {
        switch (connectionState) {
            case 0:
                return R$string.bluetooth_disconnected;
            case 1:
                return R$string.bluetooth_connecting;
            case 2:
                return R$string.bluetooth_connected;
            case 3:
                return R$string.bluetooth_disconnecting;
            default:
                return 0;
        }
    }

    static void showError(Context context, String name, int messageResId) {
        if (sErrorListener != null) {
            sErrorListener.onShowError(context, name, messageResId);
        }
    }

    public static void showError(Context context, String name, int messageResId, int knowMoreResId) {
        showError(context, name, messageResId);
    }

    public static void setErrorListener(ErrorListener listener) {
        sErrorListener = listener;
    }

    public static String toHexString(byte[] bytes, String separator) {
        if (bytes == null || separator == null) {
            return null;
        }
        StringBuilder hexString = new StringBuilder(bytes.length * (separator.length() + 2));
        int len = bytes.length;
        for (byte b : bytes) {
            hexString.append(Integer.toHexString(b & 255));
            len--;
            if (len > 0) {
                hexString.append(separator);
            }
        }
        return hexString.toString();
    }

    public static String hashMapToString(HashMap<Integer, byte[]> hashMap) {
        if (hashMap == null) {
            return null;
        }
        StringBuilder hashMapString = new StringBuilder();
        int len = hashMap.size();
        for (Entry<Integer, byte[]> entry : hashMap.entrySet()) {
            hashMapString.append(Integer.toHexString(((Integer) entry.getKey()).intValue())).append("->").append(toHexString((byte[]) entry.getValue(), " "));
            len--;
            if (len > 0) {
                hashMapString.append(",");
            }
        }
        return hashMapString.toString();
    }
}
