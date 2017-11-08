package com.android.gallery3d.ui;

import android.os.SystemProperties;
import com.android.gallery3d.ui.DialogDetailsView.DetailItem;
import java.util.ArrayList;

public class HwCustDialogDetailsViewImpl extends HwCustDialogDetailsView {
    private static final boolean SHOW_MODEL = SystemProperties.getBoolean("ro.config.exif.showModel", true);
    private static final String TAG = "DetailsFilter";

    public boolean setDetails(ArrayList<DetailItem> arrayList, String title, String value, int type) {
        if (2 != type || SHOW_MODEL) {
            return false;
        }
        return true;
    }
}
