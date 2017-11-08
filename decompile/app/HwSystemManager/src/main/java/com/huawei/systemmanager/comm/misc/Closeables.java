package com.huawei.systemmanager.comm.misc;

import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.io.IOException;

public class Closeables {
    private Closeables() {
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeAutoCloseable(AutoCloseable auto) {
        if (auto != null) {
            try {
                auto.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Closeable closeable, boolean swallowIOException) throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (swallowIOException) {
                    HwLog.w("Closeable", "IOException thrown while closing Closeable." + e);
                } else {
                    throw e;
                }
            }
        }
    }
}
