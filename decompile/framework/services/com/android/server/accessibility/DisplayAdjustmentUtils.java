package com.android.server.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.opengl.Matrix;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.util.Slog;

class DisplayAdjustmentUtils {
    private static final int DEFAULT_DISPLAY_DALTONIZER = 12;
    private static final float[] GRAYSCALE_MATRIX = new float[]{0.2126f, 0.2126f, 0.2126f, 0.0f, 0.7152f, 0.7152f, 0.7152f, 0.0f, 0.0722f, 0.0722f, 0.0722f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] INVERSION_MATRIX_VALUE_ONLY = new float[]{0.402f, -0.598f, -0.599f, 0.0f, -1.174f, -0.174f, -1.175f, 0.0f, -0.228f, -0.228f, 0.772f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    private static final String LOG_TAG = DisplayAdjustmentUtils.class.getSimpleName();

    DisplayAdjustmentUtils() {
    }

    public static boolean hasAdjustments(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        return (Secure.getIntForUser(cr, "accessibility_display_inversion_enabled", 0, userId) == 0 && Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userId) == 0) ? false : true;
    }

    public static void applyAdjustments(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        float[] colorMatrix = null;
        if (Secure.getIntForUser(cr, "accessibility_display_inversion_enabled", 0, userId) != 0) {
            colorMatrix = multiply(null, INVERSION_MATRIX_VALUE_ONLY);
        }
        if (Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userId) != 0) {
            int daltonizerMode = Secure.getIntForUser(cr, "accessibility_display_daltonizer", 12, userId);
            if (daltonizerMode == 0) {
                colorMatrix = multiply(colorMatrix, GRAYSCALE_MATRIX);
                setDaltonizerMode(-1);
            } else {
                setDaltonizerMode(daltonizerMode);
            }
        } else {
            setDaltonizerMode(-1);
        }
        String matrix = Secure.getStringForUser(cr, "accessibility_display_color_matrix", userId);
        if (matrix != null) {
            float[] userMatrix = get4x4Matrix(matrix);
            if (userMatrix != null) {
                colorMatrix = multiply(colorMatrix, userMatrix);
            }
        }
        setColorTransform(colorMatrix);
    }

    private static float[] get4x4Matrix(String matrix) {
        String[] strValues = matrix.split(",");
        if (strValues.length != 16) {
            return null;
        }
        float[] values = new float[strValues.length];
        int i = 0;
        while (i < values.length) {
            try {
                values[i] = Float.parseFloat(strValues[i]);
                i++;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return values;
    }

    private static float[] multiply(float[] matrix, float[] other) {
        if (matrix == null) {
            return other;
        }
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, matrix, 0, other, 0);
        return result;
    }

    private static void setDaltonizerMode(int mode) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(mode);
                flinger.transact(1014, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            Slog.e(LOG_TAG, "Failed to set Daltonizer mode", ex);
        }
    }

    private static void setColorTransform(float[] m) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                if (m != null) {
                    data.writeInt(1);
                    for (int i = 0; i < 16; i++) {
                        data.writeFloat(m[i]);
                    }
                } else {
                    data.writeInt(0);
                }
                flinger.transact(1015, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            Slog.e(LOG_TAG, "Failed to set color transform", ex);
        }
    }
}
