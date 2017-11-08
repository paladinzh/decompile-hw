package com.android.gallery3d.ui;

import android.opengl.GLSurfaceView.EGLConfigChooser;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.util.GalleryLog;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsLatitudeRef;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsStatus;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class GalleryEGLConfigChooser implements EGLConfigChooser {
    private static final int[] ATTR_ID = new int[]{12324, 12323, 12322, 12321, 12325, 12326, 12328, 12327};
    private static final String[] ATTR_NAME = new String[]{"R", "G", "B", GpsStatus.IN_PROGRESS, "D", GpsLatitudeRef.SOUTH, "ID", "CAVEAT"};
    private final int[] mConfigSpec565 = new int[]{12324, 5, 12323, 6, 12322, 5, 12321, 0, 12344};
    private final int[] mConfigSpec888 = new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 0, 12344};

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] numConfig = new int[1];
        int[] mConfigSpec = ApiHelper.USE_888_PIXEL_FORMAT ? this.mConfigSpec888 : this.mConfigSpec565;
        if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, numConfig)) {
            throw new RuntimeException("eglChooseConfig failed");
        } else if (numConfig[0] <= 0) {
            throw new RuntimeException("No configs match configSpec");
        } else {
            EGLConfig[] configs = new EGLConfig[numConfig[0]];
            if (egl.eglChooseConfig(display, mConfigSpec, configs, configs.length, numConfig)) {
                return chooseConfig(egl, display, configs);
            }
            throw new RuntimeException();
        }
    }

    private EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
        EGLConfig result = null;
        int minStencil = Integer.MAX_VALUE;
        int[] value = new int[1];
        int i = 0;
        int n = configs.length;
        while (i < n) {
            if (ApiHelper.USE_888_PIXEL_FORMAT || !egl.eglGetConfigAttrib(display, configs[i], 12324, value) || value[0] != 8) {
                if (!egl.eglGetConfigAttrib(display, configs[i], 12326, value)) {
                    throw new RuntimeException("eglGetConfigAttrib error: " + egl.eglGetError());
                } else if (value[0] != 0 && value[0] < minStencil) {
                    minStencil = value[0];
                    result = configs[i];
                }
            }
            i++;
        }
        if (result == null) {
            result = configs[0];
        }
        egl.eglGetConfigAttrib(display, result, 12326, value);
        logConfig(egl, display, result);
        return result;
    }

    private void logConfig(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] value = new int[1];
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < ATTR_ID.length; j++) {
            egl.eglGetConfigAttrib(display, config, ATTR_ID[j], value);
            sb.append(ATTR_NAME[j]).append(value[0]).append(" ");
        }
        GalleryLog.i("GalleryEGLConfigChooser", "Config chosen: " + sb.toString());
    }
}
