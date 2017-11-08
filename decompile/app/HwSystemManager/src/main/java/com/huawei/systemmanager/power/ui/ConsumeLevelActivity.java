package com.huawei.systemmanager.power.ui;

import android.app.Fragment;
import android.os.UserHandle;
import com.common.imageloader.core.ImageLoader;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;

public class ConsumeLevelActivity extends SingleFragmentActivity {
    private static final String TAG = "ConsumeLevelActivity";

    protected Fragment buildFragment() {
        return new ConsumeLevelFragment();
    }

    protected void onDestroy() {
        if (UserHandle.myUserId() == 0) {
            ImageLoader.getInstance().clearMemoryCache();
        }
        super.onDestroy();
    }
}
