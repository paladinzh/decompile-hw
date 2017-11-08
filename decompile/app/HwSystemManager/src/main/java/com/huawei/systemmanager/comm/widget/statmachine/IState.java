package com.huawei.systemmanager.comm.widget.statmachine;

import android.os.Message;

public interface IState {
    public static final boolean HANDLED = true;
    public static final boolean NOT_HANDLED = false;

    void enter();

    void exit();

    int getFlag();

    String getName();

    boolean processMessage(Message message);
}
