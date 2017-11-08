package com.huawei.systemmanager.preventmode;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import com.huawei.systemmanager.preventmode.util.IPreventDataChange.DefaultImpl;
import com.huawei.systemmanager.preventmode.util.PreventDataHelper;
import com.huawei.systemmanager.service.MainService.HsmService;

public class PreventHsmService implements HsmService {
    public static final String TAG = "PreventHsmService";
    private PreventDataHelper mDataHelper = null;
    private Context mcontext = null;

    private class LocalPreventDataChangeImpl extends DefaultImpl {
        private LocalPreventDataChangeImpl() {
        }

        public void onZenModeChange() {
            PreventHsmService.this.startPreventModeService();
            PreventDataHelper.updateVisibility(PreventHsmService.this.mcontext);
        }

        public void onZenModeConfigChange() {
            PreventDataHelper.updateNotification(PreventHsmService.this.mcontext);
        }
    }

    public PreventHsmService(Context ctx) {
        this.mcontext = ctx;
    }

    public void init() {
        startPreventModeService();
        this.mDataHelper = new PreventDataHelper(this.mcontext);
        this.mDataHelper.registDataChangeObserver(new LocalPreventDataChangeImpl());
    }

    public void onDestroy() {
        if (this.mDataHelper != null) {
            this.mDataHelper.unregistDataChangeObserver();
        }
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    private void startPreventModeService() {
        if (PreventDataHelper.getCurrentZenMode(this.mcontext) != 0) {
            this.mcontext.startService(new Intent(this.mcontext, PreventModeService.class));
        }
    }
}
