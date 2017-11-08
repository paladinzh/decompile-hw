package com.huawei.powergenie.core.server;

import android.content.Context;
import android.util.Log;

@Deprecated
public final class PlugManager {
    private Context mContext;
    private boolean mIsPGApiOkay = false;
    private Object mPGApi = null;

    protected PlugManager(Context context) {
        this.mContext = context;
        this.mIsPGApiOkay = false;
        Log.i("PlugManager", "PG7 do not support the PGAPI feature.");
    }

    protected boolean hasClients() {
        return this.mIsPGApiOkay;
    }

    protected boolean handleActionInner(int exportActionId, String pkgName, String extend1, String extend2) {
        if (this.mIsPGApiOkay) {
            return dispatchAction(exportActionId, pkgName, extend1, extend2);
        }
        return false;
    }

    private boolean dispatchAction(int exportActionId, String pkg, String extend1, String extend2) {
        try {
            if (this.mPGApi != null) {
                try {
                    this.mPGApi.getClass().getMethod("handleAction", new Class[]{Integer.TYPE, String.class, String.class, String.class}).invoke(this.mPGApi, new Object[]{Integer.valueOf(exportActionId), pkg, extend1, extend2});
                    return true;
                } catch (NoSuchMethodException e) {
                    Log.w("PlugManager", "PGApi NoSuchMethod: handleAction [String]");
                } catch (Exception e2) {
                    Log.w("PlugManager", "PGApi handleAction: other error");
                }
            }
        } catch (Exception e3) {
            Log.w("PlugManager", "the class PGApi not exist, do nothing");
        }
        this.mIsPGApiOkay = false;
        return false;
    }
}
