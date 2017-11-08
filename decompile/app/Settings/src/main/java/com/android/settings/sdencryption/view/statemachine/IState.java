package com.android.settings.sdencryption.view.statemachine;

import android.os.Message;

public interface IState {
    void enter();

    void exit();

    String getName();

    boolean processMessage(Message message);
}
