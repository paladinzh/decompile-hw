package com.android.server.rms.test;

import android.os.Bundle;
import android.os.Process;
import com.android.server.rms.IShrinker;
import com.android.server.rms.shrinker.ProcessShrinker;
import com.android.server.rms.shrinker.SystemShrinker;
import com.android.server.rms.utils.Utils;

public final class TestShrinker {
    public static final void testSystemShrinker() {
        final IShrinker shrinker = new SystemShrinker();
        new Thread("testSystemShrinker") {
            public void run() {
                while (true) {
                    shrinker.reclaim("testSystemShrinker", null);
                    Utils.wait(5000);
                }
            }
        }.start();
    }

    public static final void testProcessShrinker() {
        final ProcessShrinker processShrinker = new ProcessShrinker(1);
        new Thread("testProcessShrinker") {
            public void run() {
                while (true) {
                    for (int pid : Process.getPidsForCommands(new String[]{"system_server", "com.android.systemui", "com.android.keyguard"})) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("pid", pid);
                        processShrinker.reclaim("testMemory", bundle);
                    }
                    Utils.wait(5000);
                }
            }
        }.start();
    }
}
