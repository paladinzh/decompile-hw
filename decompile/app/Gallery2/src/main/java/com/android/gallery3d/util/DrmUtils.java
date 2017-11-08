package com.android.gallery3d.util;

import android.content.Context;
import android.content.res.Resources;
import android.drm.DrmManagerClient;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import java.lang.reflect.Field;

public class DrmUtils {
    private static final boolean DRM_ENABLED = SystemProperties.getBoolean("ro.huawei.cust.oma_drm", true);
    private static DrmManagerClient sDrmManagerClient;
    private static Field sField;
    private static volatile Field sInDrmField;

    public static void initialize(Context context) {
        sDrmManagerClient = new DrmManagerClient(context);
    }

    public static boolean isDrmFile(String path) {
        if (!TextUtils.isEmpty(path) && DRM_ENABLED && (path.endsWith(".fl") || path.endsWith(".dm") || path.endsWith(".dcf"))) {
            return true;
        }
        return false;
    }

    public static boolean isDrmEnabled() {
        return DRM_ENABLED;
    }

    public static int getRightCount(String path, int action) {
        try {
            if (sDrmManagerClient.canHandle(path, null)) {
                return sDrmManagerClient.getConstraints(path, action).getAsInteger("rights_count").intValue();
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static Options inPreviewMode(Options options) {
        try {
            if (sField == null) {
                sField = Options.class.getField("inThumbnailMode");
            }
            if (!(sField == null || options == null)) {
                sField.set(options, Boolean.valueOf(true));
            }
        } catch (Exception e) {
            GalleryLog.i("DRM", "Fail to set options." + e.getMessage());
        }
        return options;
    }

    public static Options inDrmMode(Options options) {
        try {
            if (sInDrmField == null) {
                sInDrmField = Options.class.getField("inDrmMode");
            }
            if (options != null) {
                sInDrmField.set(options, Boolean.valueOf(true));
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e2) {
        }
        return options;
    }

    public static boolean haveRightsForAction(String path, int action) {
        boolean z = false;
        try {
            if (sDrmManagerClient.canHandle(path, null)) {
                if (sDrmManagerClient.checkRightsStatus(path, action) == 0) {
                    z = true;
                }
                return z;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean haveCountConstraints(String path, int action) {
        boolean z = true;
        try {
            if (sDrmManagerClient.canHandle(path, null)) {
                if (sDrmManagerClient.getConstraints(path, action).getAsBoolean("is_auto_use").booleanValue()) {
                    z = false;
                }
                return z;
            }
        } catch (Exception e) {
        }
        return true;
    }

    public static int getObjectType(String path) {
        try {
            if (sDrmManagerClient.canHandle(path, null)) {
                return sDrmManagerClient.getDrmObjectType(path, null);
            }
        } catch (Exception e) {
        }
        return 4;
    }

    public static boolean canForward(String path) {
        return getObjectType(path) == 7;
    }

    public static Bitmap getPlaceHolder(Resources resources) {
        return BitmapFactory.decodeResource(resources, R.drawable.drm_default_thumb);
    }

    public static boolean canSetAsWallPaper(MediaItem item) {
        boolean z = true;
        if (!item.isDrm()) {
            return true;
        }
        if (!item.hasRight()) {
            return false;
        }
        if (item.getDrmType() != 5 && item.hasCountConstraint()) {
            z = false;
        }
        return z;
    }

    public static boolean canBeGotContent(DataManager dm, Path path) {
        return canBeGotContent((MediaItem) dm.getMediaObject(path));
    }

    public static boolean canBeGotContent(MediaItem item) {
        boolean z = false;
        if (!item.isDrm()) {
            return true;
        }
        if (!item.hasRight()) {
            return false;
        }
        if (item.canForward() && !item.hasCountConstraint()) {
            z = true;
        }
        return z;
    }
}
