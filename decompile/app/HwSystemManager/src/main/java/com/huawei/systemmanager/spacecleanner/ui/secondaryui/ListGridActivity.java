package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;

public class ListGridActivity extends ListTrashSetActivity {
    private static final String TAG = "ListGridActivity";

    public int onGetCustomThemeStyle() {
        return 0;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if (i != null) {
            String title = i.getStringExtra(SecondaryConstant.NAME_ID_EXTRA);
            HwLog.i(TAG, "onCreate  title is:  " + title);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
        }
    }

    protected Fragment buildDefaultFragment() {
        return new MediaListGridFragment();
    }

    public boolean isSupportLandOriention() {
        return false;
    }
}
