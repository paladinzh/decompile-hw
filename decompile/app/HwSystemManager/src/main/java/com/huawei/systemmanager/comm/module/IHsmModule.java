package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;

public interface IHsmModule {

    public static abstract class AbsHsmModule implements IHsmModule {
        public boolean entryEnabled(Context ctx) {
            return true;
        }
    }

    boolean entryEnabled(Context context);

    Intent getMainEntry(Context context);
}
