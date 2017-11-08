package com.huawei.powergenie.core.contextaware;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.android.location.provider.ActivityRecognitionProvider;
import com.android.location.provider.ActivityRecognitionProviderWatcher;

public class PGActivityRecognitionService extends Service {
    private static ActivityRecognitionProviderWatcher mRecognitionProviderWatcher = null;

    public void onCreate() {
        Log.d("PGActivityRecognitionService", "onCreate");
        mRecognitionProviderWatcher = ActivityRecognitionProviderWatcher.getInstance();
    }

    public IBinder onBind(Intent arg0) {
        Log.d("PGActivityRecognitionService", "onBind");
        return mRecognitionProviderWatcher.getBinder();
    }

    public boolean onUnbind(Intent intent) {
        Log.d("PGActivityRecognitionService", "onUnbind");
        return false;
    }

    public static ActivityRecognitionProvider getActivityRecognitionProvider() {
        if (mRecognitionProviderWatcher != null) {
            return mRecognitionProviderWatcher.getActivityRecognitionProvider();
        }
        return null;
    }
}
