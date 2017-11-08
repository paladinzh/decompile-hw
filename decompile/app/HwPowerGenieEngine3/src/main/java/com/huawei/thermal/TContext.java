package com.huawei.thermal;

import android.content.Context;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public interface TContext {
    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    Context getContext();

    String getThermalInterface(String str);

    boolean isHisiPlatform();

    boolean isQcommPlatform();

    boolean registerPGActions(ArrayList<Integer> arrayList);

    void shutdownPhone(int i);
}
