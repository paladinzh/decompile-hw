package com.huawei.gallery.app;

import android.os.Bundle;
import android.util.Log;
import com.android.gallery3d.R;
import com.android.gallery3d.data.Keyguard;
import com.android.gallery3d.util.ReportToBigData;

public class KeyguardManager extends GLActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gl_activity);
        if (savedInstanceState != null) {
            getDataManager().notifyChange(Keyguard.URI);
            getStateManager().restoreFromState(savedInstanceState);
            return;
        }
        initializeByIntent();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void initializeByIntent() {
        ReportToBigData.report(46, "");
        int photoType = getIntent().getIntExtra("photoType", -1);
        boolean updateAvailable = getIntent().getBooleanExtra("updateAvailable", false);
        Bundle data = new Bundle();
        if (photoType == 1) {
            data.putString("media-path", "/keyguard/custom");
            data.putBoolean("updateAvailable", updateAvailable);
            getStateManager().startState(KeyguardPage.class, data);
        } else if (photoType == 0) {
            data.putString("media-path", "/keyguard/download");
            data.putBoolean("updateAvailable", updateAvailable);
            getStateManager().startState(KeyguardPage.class, data);
        } else {
            Log.i("keyguard", "invalid phototype");
            finish();
        }
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
