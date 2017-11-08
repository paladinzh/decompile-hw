package com.fyusion.sdk.common.ext;

import android.util.Log;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.util.a;
import fyusion.vislib.FrameBlender;
import fyusion.vislib.Fyuse;
import fyusion.vislib.FyuseContainerType;
import fyusion.vislib.FyuseContainerUtils;
import fyusion.vislib.IMUData;
import fyusion.vislib.OnlineImageStabilizerWrapper;
import fyusion.vislib.Platform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Locale;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public class g implements b {
    private static final String a = (File.separator + "upload" + File.separator);
    private static g c = null;
    private final boolean b = false;

    private g() {
        if (c != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static g a() {
        if (c == null) {
            synchronized (g.class) {
                if (c == null) {
                    c = new g();
                }
            }
        }
        return c;
    }

    private ProcessState f(File file) throws FileNotFoundException {
        if (file != null) {
            e b = b(file);
            return b.getNumberOfStabilizedFrames() >= 1 ? b.getNumberOfSlices() <= 0 ? ProcessState.READY_FOR_VIEW : ProcessState.READY_FOR_UPLOAD : ProcessState.INITIAL;
        } else {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            throw new FileNotFoundException();
        }
    }

    public int a(String str, int i) {
        int i2 = 0;
        while (i2 <= i) {
            if (!new File(str + File.separator + String.format(Locale.US, j.aH, new Object[]{Integer.valueOf(i2)})).exists()) {
                break;
            }
            i2++;
        }
        return i2 + 1;
    }

    public ProcessState a(File file) throws IOException {
        try {
            FyuseContainerType c = f.c(file);
            if (c != null) {
                return c != FyuseContainerType.PROCESSED ? ProcessState.INITIAL : ProcessState.READY_FOR_VIEW;
            } else {
                throw new IllegalArgumentException("Invalid fyuse file. " + file.getPath());
            }
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e2) {
            return f(file);
        }
    }

    public File a(String str) {
        if (str != null) {
            return new File(str + File.separator + j.ag);
        }
        Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
        return null;
    }

    public void a(String str, FyuseState fyuseState) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(str + File.separator + "state.dat"));
            objectOutputStream.writeObject(fyuseState);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    void a(List<m> list, String str) {
        ByteBuffer allocate = ByteBuffer.allocate((list.size() * SmsCheckResult.ESCT_200) + 12);
        allocate.order(ByteOrder.LITTLE_ENDIAN);
        allocate.putFloat(2.0f);
        allocate.putLong((long) list.size());
        for (int i = 0; i < list.size(); i++) {
            m mVar = (m) list.get(i);
            double d = mVar.a;
            double d2 = (double) mVar.c.a;
            double d3 = (double) mVar.c.b;
            double d4 = (double) mVar.c.c;
            double d5 = (double) mVar.f.a;
            double d6 = (double) mVar.f.b;
            double d7 = (double) mVar.f.c;
            double d8 = (double) mVar.i.a;
            double d9 = (double) mVar.i.b;
            double d10 = (double) mVar.i.c;
            double d11 = (double) mVar.n[0];
            double d12 = (double) mVar.n[1];
            double d13 = (double) mVar.n[2];
            double d14 = (double) mVar.k.a;
            double d15 = (double) mVar.k.b;
            double d16 = (double) mVar.k.c;
            double d17 = (double) mVar.j[0];
            double d18 = (double) mVar.j[1];
            double d19 = (double) mVar.j[2];
            double d20 = (double) mVar.j[4];
            double d21 = (double) mVar.j[5];
            double d22 = (double) mVar.j[6];
            double d23 = (double) mVar.j[8];
            double d24 = (double) mVar.j[9];
            double d25 = (double) mVar.j[10];
            int i2 = 0;
            String str2 = "";
            for (int i3 = 0; i3 < 3; i3++) {
                for (int i4 = 0; i4 < 4; i4++) {
                    str2 = str2 + " " + mVar.j[i2];
                    i2++;
                }
                str2 = str2 + "\n";
            }
            allocate.putDouble(d);
            allocate.putDouble(d2);
            allocate.putDouble(d3);
            allocate.putDouble(d4);
            allocate.putDouble(d5);
            allocate.putDouble(d6);
            allocate.putDouble(d7);
            allocate.putDouble(d8);
            allocate.putDouble(d9);
            allocate.putDouble(d10);
            allocate.putDouble(d11);
            allocate.putDouble(d12);
            allocate.putDouble(d13);
            allocate.putDouble(d14);
            allocate.putDouble(d15);
            allocate.putDouble(d16);
            allocate.putDouble(d17);
            allocate.putDouble(d18);
            allocate.putDouble(d19);
            allocate.putDouble(d20);
            allocate.putDouble(d21);
            allocate.putDouble(d22);
            allocate.putDouble(d23);
            allocate.putDouble(d24);
            allocate.putDouble(d25);
        }
        FileChannel fileChannel = null;
        try {
            fileChannel = new FileOutputStream(str, false).getChannel();
        } catch (Exception e) {
        }
        if (fileChannel != null) {
            allocate.rewind();
            fileChannel.write(allocate);
        }
        try {
            fileChannel.close();
        } catch (Exception e2) {
        }
        allocate.clear();
    }

    public boolean a(File file, File file2) throws IOException {
        return f.a(file, file2);
    }

    public boolean a(String str, e eVar) {
        if (str != null) {
            return eVar != null ? eVar.a(str, j.ae) : false;
        } else {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            return false;
        }
    }

    public boolean a(String str, OnlineImageStabilizerWrapper onlineImageStabilizerWrapper) {
        if (str != null) {
            return onlineImageStabilizerWrapper != null ? onlineImageStabilizerWrapper.writeToFile(str + File.separator + j.an) : false;
        } else {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            return false;
        }
    }

    public boolean a(String str, List<m> list) {
        if (str == null) {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            return false;
        } else if (list == null) {
            return false;
        } else {
            a((List) list, str + File.separator + j.ai);
            return false;
        }
    }

    public e b(File file) throws FileNotFoundException {
        Fyuse eVar = new e();
        if (file.exists() && !file.isDirectory()) {
            FrameBlender frameBlender = new FrameBlender();
            FyuseContainerUtils.loadMagicDataFromFile(file.getPath(), eVar, frameBlender, Platform.Android);
            frameBlender.delete();
        } else if (!eVar.a(file.getParent())) {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            throw new FileNotFoundException(file + " not found");
        }
        return eVar;
    }

    public IMUData b(String str) throws FileNotFoundException {
        if (str != null) {
            IMUData iMUData = new IMUData();
            iMUData.loadFromFile(str + File.separator + j.ai);
            return iMUData;
        }
        Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
        return null;
    }

    public String c(String str) {
        return str + File.separator + "temp" + File.separator + j.an;
    }

    public void c(File file) throws IOException {
        File file2 = new File(file, "temp");
        a.a(file2, file, j.ao);
        a.a(file2, file, j.ae);
        a.a(file2, file, j.ah);
    }

    public File d(File file) throws IOException {
        File file2 = new File(file, "temp");
        file2.mkdir();
        a.a(file, file2, j.ae);
        a.a(file, file2, j.an);
        a.a(file, file2, j.ai);
        return file2;
    }

    public int e(File file) throws IOException {
        return f.a(file);
    }
}
