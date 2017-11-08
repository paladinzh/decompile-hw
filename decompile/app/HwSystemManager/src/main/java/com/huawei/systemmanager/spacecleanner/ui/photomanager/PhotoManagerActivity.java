package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.app.Fragment;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;

public class PhotoManagerActivity extends ListTrashSetActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.space_clean_trash_photo);
    }

    protected Fragment buildDefaultFragment() {
        return new PhotoManagerFragment();
    }
}
