package com.huawei.systemmanager.comm.widget.statmachine;

import android.os.Message;

public abstract class SimpleState implements IState {
    public void enter() {
    }

    public void exit() {
    }

    public boolean processMessage(Message msg) {
        return false;
    }

    public int getFlag() {
        return 0;
    }

    public String getName() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(36) + 1);
    }
}
