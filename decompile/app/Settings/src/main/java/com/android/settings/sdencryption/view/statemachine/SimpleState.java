package com.android.settings.sdencryption.view.statemachine;

import android.os.Message;

public abstract class SimpleState implements IState {
    public void enter() {
    }

    public void exit() {
    }

    public boolean processMessage(Message msg) {
        return false;
    }

    public String getName() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(36) + 1);
    }
}
