package com.huawei.systemmanager.spacecleanner;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.view.View;
import android.view.View.OnClickListener;
import com.common.imageloader.core.ImageLoader;
import com.google.common.collect.Lists;
import com.huawei.android.app.ActionBarEx;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.misc.ImageLoaderUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.ui.SpaceCleanFragment;
import com.huawei.systemmanager.spacecleanner.ui.SpaceSettingActivity;
import com.huawei.systemmanager.util.HwLog;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class SpaceCleanActivity extends SingleFragmentActivity implements MessageHandler {
    private static final long INIT_IMAGELOADER_DELAY_TIME = 1000;
    private static final int MSG_INIT_IMAGELOADER = 1;
    private static final String TAG = "SpaceCleanActivity";
    private static final List<WeakReference<Object>> sAliveRefs = Lists.newArrayList();
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            try {
                SpaceCleanActivity.this.startActivity(new Intent(SpaceCleanActivity.this, SpaceSettingActivity.class));
            } catch (Exception e) {
                HwLog.e(SpaceCleanActivity.TAG, "exception in start activity");
            }
        }
    };
    private Object mAliveTag = new Object();
    private final Handler mHandle = new CommonHandler(this);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utility.isOwnerUser()) {
            putFlag(this.mAliveTag);
            this.mHandle.sendEmptyMessageDelayed(1, 1000);
            initSpaceCleanState();
            initActionBar();
            return;
        }
        finish();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            ActionBarEx.setEndIcon(actionBar, true, getDrawable(R.drawable.settings_menu_btn_selector), this.mActionBarListener);
            ActionBarEx.setEndContentDescription(actionBar, getString(R.string.ActionBar_AddAppSettings_Title));
        }
    }

    private void initSpaceCleanState() {
        SpaceCleannerManager.getInstance().cancelFileAnalysisNotify();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                initImageLoader();
                return;
            default:
                return;
        }
    }

    public static void initImageLoader() {
        ImageLoaderUtils.initImageLoader();
    }

    protected Fragment buildFragment() {
        return new SpaceCleanFragment();
    }

    protected boolean useHsmActivityHelper() {
        return false;
    }

    protected void onDestroy() {
        if (UserHandle.myUserId() == 0) {
            ImageLoader.getInstance().clearMemoryCache();
        }
        super.onDestroy();
        removeFlag(this.mAliveTag);
    }

    private void putFlag(Object aliveFlag) {
        sAliveRefs.add(new WeakReference(aliveFlag));
    }

    private void removeFlag(Object aliveFlag) {
        Iterator<WeakReference<Object>> it = sAliveRefs.iterator();
        while (it.hasNext()) {
            Object flag = ((WeakReference) it.next()).get();
            if (flag == null) {
                it.remove();
            } else if (flag == aliveFlag) {
                it.remove();
            }
        }
    }

    public static boolean checkIfAlive() {
        Iterator<WeakReference<Object>> it = sAliveRefs.iterator();
        while (it.hasNext()) {
            if (((WeakReference) it.next()).get() == null) {
                it.remove();
            }
        }
        return !sAliveRefs.isEmpty();
    }
}
