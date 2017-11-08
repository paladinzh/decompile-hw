package com.android.mms.drm;

import android.drm.DrmManagerClient;
import android.net.Uri;
import android.util.Log;
import com.android.mms.MmsApp;

public class DrmUtils {
    private DrmUtils() {
    }

    public static boolean isDrmType(String mimeType) {
        DrmManagerClient drmManagerClient = MmsApp.getApplication().getDrmManagerClient();
        if (drmManagerClient == null) {
            return false;
        }
        try {
            if (drmManagerClient.canHandle("", mimeType)) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            Log.w("DrmUtils", "canHandle called with wrong parameters");
            return false;
        } catch (IllegalStateException e2) {
            Log.w("DrmUtils", "DrmManagerClient didn't initialize properly");
            return false;
        }
    }

    public static boolean haveRightsForAction(Uri uri, int action) {
        boolean z = true;
        DrmManagerClient drmManagerClient = MmsApp.getApplication().getDrmManagerClient();
        try {
            if (drmManagerClient.canHandle(uri.toString(), null)) {
                if (drmManagerClient.checkRightsStatus(uri.toString(), action) != 0) {
                    z = false;
                }
                return z;
            }
        } catch (Exception e) {
        }
        return true;
    }
}
