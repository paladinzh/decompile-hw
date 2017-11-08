package com.fyusion.sdk.common.ext;

import android.util.Log;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.util.a;
import fyusion.vislib.FrameBlender;
import fyusion.vislib.Fyuse;
import fyusion.vislib.FyuseContainerType;
import fyusion.vislib.FyuseContainerUtils;
import fyusion.vislib.Platform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Locale;

/* compiled from: Unknown */
public class h implements c {
    private static final String a = (File.separator + "upload" + File.separator);
    private static h c = null;
    private final boolean b = false;

    private h() {
        if (c != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static h a() {
        if (c == null) {
            synchronized (h.class) {
                if (c == null) {
                    c = new h();
                }
            }
        }
        return c;
    }

    private ProcessState f(File file) throws FileNotFoundException {
        if (file != null) {
            f b = b(file);
            return b.getNumberOfStabilizedFrames() >= 1 ? b.getNumberOfSlices() <= 0 ? ProcessState.READY_FOR_VIEW : ProcessState.READY_FOR_UPLOAD : ProcessState.INITIAL;
        } else {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            throw new FileNotFoundException();
        }
    }

    public int a(String str, int i) {
        int i2 = 0;
        while (i2 <= i) {
            if (!new File(str + File.separator + String.format(Locale.US, k.aH, new Object[]{Integer.valueOf(i2)})).exists()) {
                break;
            }
            i2++;
        }
        return i2 + 1;
    }

    public ProcessState a(File file) throws IOException {
        try {
            FyuseContainerType c = g.c(file);
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
            return new File(str + File.separator + k.ag);
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

    public boolean a(File file, File file2) throws IOException {
        return g.a(file, file2);
    }

    public boolean a(String str, f fVar) {
        if (str != null) {
            return fVar != null ? fVar.a(str, k.ae) : false;
        } else {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            return false;
        }
    }

    public f b(File file) throws FileNotFoundException {
        Fyuse fVar = new f();
        if (file.exists() && !file.isDirectory()) {
            FrameBlender frameBlender = new FrameBlender();
            FyuseContainerUtils.loadMagicDataFromFile(file.getPath(), fVar, frameBlender, Platform.Android);
            frameBlender.delete();
        } else if (!fVar.a(file.getParent())) {
            Log.w("FyuseDataManager", "Invalid path. Check that file/directory exists.");
            throw new FileNotFoundException(file + " not found");
        }
        return fVar;
    }

    public String c(String str) {
        return str + File.separator + "temp" + File.separator + k.an;
    }

    public void c(File file) throws IOException {
        File file2 = new File(file, "temp");
        a.a(file2, file, k.ao);
        a.a(file2, file, k.ae);
        a.a(file2, file, k.ah);
    }

    public File d(File file) throws IOException {
        File file2 = new File(file, "temp");
        file2.mkdir();
        a.a(file, file2, k.ae);
        a.a(file, file2, k.an);
        a.a(file, file2, k.ai);
        return file2;
    }

    public int e(File file) throws IOException {
        return g.a(file);
    }
}
