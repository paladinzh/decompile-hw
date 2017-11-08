package com.huawei.android.quickaction;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.quickaction.IQuickActionService.Stub;
import java.util.List;

public abstract class QuickActionService extends Service {
    private final String TAG = new StringBuilder(String.valueOf(QuickActionService.class.getSimpleName())).append('[').append(getClass().getSimpleName()).append(']').toString();
    private IQuickActionServiceWrapper mWrapper = null;

    class IQuickActionServiceWrapper extends Stub {
        IQuickActionServiceWrapper() {
        }

        public void getQuickActions(ComponentName targetComponentName, IQuickActionResult result) throws RemoteException {
            List<QuickAction> actions = null;
            try {
                actions = QuickActionService.this.onGetQuickActions(targetComponentName);
            } finally {
                result.sendResult(actions);
            }
        }
    }

    public abstract List<QuickAction> onGetQuickActions(ComponentName componentName);

    public IBinder onBind(Intent intent) {
        if (!"com.huawei.android.quickaction.QuickActionService".equals(intent.getAction())) {
            return null;
        }
        if (this.mWrapper == null) {
            this.mWrapper = new IQuickActionServiceWrapper();
        }
        return this.mWrapper;
    }
}
