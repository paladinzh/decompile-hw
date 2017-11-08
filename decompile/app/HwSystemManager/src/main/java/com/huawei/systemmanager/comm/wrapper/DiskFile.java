package com.huawei.systemmanager.comm.wrapper;

import java.io.File;

public class DiskFile {
    public static boolean fileExist(String diskPath) {
        if (diskPath == null) {
            return false;
        }
        return new File(diskPath).exists();
    }
}
