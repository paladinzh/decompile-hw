package com.huawei.powergenie.integration.adapter.pged;

public interface KStateInterface {
    boolean checkPgedRunning();

    boolean closeKState(int i);

    boolean openKState(int i);

    boolean registerHwPgedListener();

    boolean unregisterHwPgedListener();
}
