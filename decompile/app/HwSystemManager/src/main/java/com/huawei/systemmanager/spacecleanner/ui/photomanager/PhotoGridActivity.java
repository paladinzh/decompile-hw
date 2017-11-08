package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;

public class PhotoGridActivity extends ListTrashSetActivity {
    private static final String TAG = "PhotoGridActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        String title = getTitleStr();
        actionBar.setTitle(title);
        setTitle(title);
        actionBar.show();
    }

    private String getTitleStr() {
        Intent intent = getIntent();
        if (intent == null) {
            HwLog.i(TAG, "intent is null");
            return null;
        }
        OpenSecondaryParam param = (OpenSecondaryParam) intent.getParcelableExtra(SecondaryConstant.OPEN_SECONDARY_PARAM);
        if (param == null) {
            HwLog.i(TAG, "param is null");
            return null;
        }
        String title = param.getTitleStr();
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        HwLog.i(TAG, "title is null");
        return null;
    }

    protected Fragment buildDefaultFragment() {
        return new BasePhotoGridFragment();
    }
}
