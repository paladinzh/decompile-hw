package com.huawei.powergenie.integration.adapter.pged;

import java.util.ArrayList;
import java.util.List;

public interface FreezeInterface {
    boolean checkPgedRunning();

    boolean freezeProcess(List<Integer> list);

    int getProcUTime(int i);

    boolean netPacketListener(int i, ArrayList<Integer> arrayList);

    boolean notifyBastetProxy(List<Integer> list);

    boolean notifyBastetUnProxy(List<Integer> list);

    boolean notifyBastetUnProxyAll();

    boolean unfreezeAllProcess();

    boolean unfreezeProcess(List<Integer> list);
}
