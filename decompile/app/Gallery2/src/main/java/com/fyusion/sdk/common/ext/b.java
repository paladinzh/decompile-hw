package com.fyusion.sdk.common.ext;

import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import fyusion.vislib.IMUData;
import fyusion.vislib.OnlineImageStabilizerWrapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/* compiled from: Unknown */
public interface b {
    int a(String str, int i);

    ProcessState a(File file) throws IOException;

    File a(String str);

    void a(String str, FyuseState fyuseState);

    boolean a(File file, File file2) throws IOException;

    boolean a(String str, e eVar);

    boolean a(String str, OnlineImageStabilizerWrapper onlineImageStabilizerWrapper);

    boolean a(String str, List<m> list);

    e b(File file) throws FileNotFoundException;

    IMUData b(String str) throws FileNotFoundException;

    void c(File file) throws IOException;

    File d(File file) throws IOException;
}
