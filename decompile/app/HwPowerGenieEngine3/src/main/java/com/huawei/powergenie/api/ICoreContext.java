package com.huawei.powergenie.api;

import android.content.Context;
import android.os.Looper;
import java.io.PrintWriter;

public interface ICoreContext {
    void addAction(int i, int i2);

    boolean configBrightnessRange(boolean z);

    void dumpAll(PrintWriter printWriter, String[] strArr, String str);

    Looper getActionsHandlerLooper();

    Context getContext();

    Object getService(String str);

    boolean isHisiPlatform();

    boolean isMultiWinDisplay();

    boolean isQcommPlatform();

    boolean isRestartAfterCrash();

    boolean isScreenOff();

    boolean releaseRRC();

    void removeAction(int i, int i2);

    void removeAllActions(int i);

    boolean setCABC(boolean z);

    boolean setLcdRatio(int i, boolean z);
}
