package com.huawei.systemmanager.spacecleanner.utils.nameconvertor;

import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import java.util.Locale;
import java.util.Map;

public class FolderConvertor {
    public static final String MAGAZINE_UNLOCK = "MagazineUnlock";
    public static final String SCREENSHOTS = "Pictures/Screenshots";
    private static final Map<String, Integer> sResMap = HsmCollections.newArrayMap();

    static {
        sResMap.put(MAGAZINE_UNLOCK.toLowerCase(Locale.ENGLISH), Integer.valueOf(R.string.space_folder_magazine_unlock));
        sResMap.put(SCREENSHOTS.toLowerCase(Locale.ENGLISH), Integer.valueOf(R.string.space_folder_screenshot));
    }

    public static int getConvertorResId(String folderName) {
        if (TextUtils.isEmpty(folderName)) {
            return -1;
        }
        Integer resId = (Integer) sResMap.get(folderName.toLowerCase(Locale.ENGLISH));
        if (resId == null) {
            return -1;
        }
        return resId.intValue();
    }

    public static boolean isMazineUnlock(String albumPath) {
        if (TextUtils.isEmpty(albumPath)) {
            return false;
        }
        return MAGAZINE_UNLOCK.equalsIgnoreCase(albumPath);
    }

    public static boolean isScreenShot(String albumPath) {
        if (TextUtils.isEmpty(albumPath)) {
            return false;
        }
        return SCREENSHOTS.equalsIgnoreCase(albumPath);
    }
}
