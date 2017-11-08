package com.fyusion.sdk.common.ext;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.ext.filter.ImageFilterAbstractFactory;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface;
import com.fyusion.sdk.common.ext.util.exif.ExifTag;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.o;
import com.fyusion.sdk.common.util.a;
import fyusion.vislib.FrameBlender;
import fyusion.vislib.FyuseContainerType;
import fyusion.vislib.FyuseContainerUtils;
import fyusion.vislib.Platform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class m {
    private final c a;
    private final File b;
    private final File c;
    private final String d;

    public m(c cVar, File file) {
        this.a = cVar;
        this.d = file.getName();
        if (file.isDirectory()) {
            this.c = new File(file, k.ak);
        } else {
            this.c = file;
        }
        this.b = a.a(this.d);
    }

    public m(File file) {
        this(h.a(), file);
    }

    private static ExifInterface a(List<ExifTag> list) {
        ExifInterface exifInterface = new ExifInterface();
        if (list != null) {
            exifInterface.setTags(list);
        }
        exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_COPYRIGHT, "Fyusion Inc."));
        exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_SOFTWARE, "Fyuse SDK version " + FyuseSDK.getVersion()));
        exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_MAKE, Build.MANUFACTURER));
        exifInterface.setTag(exifInterface.buildTag(ExifInterface.TAG_MODEL, Build.MODEL));
        return exifInterface;
    }

    private void n() {
        if (!this.b.exists()) {
            this.b.mkdirs();
        }
    }

    public int a(int i) {
        return this.a.a(this.b.getPath(), i);
    }

    public String a() {
        return this.d;
    }

    public List<ImageFilter> a(f fVar, ImageFilterAbstractFactory imageFilterAbstractFactory) {
        if (fVar == null) {
            return null;
        }
        List<ImageFilter> arrayList = new ArrayList();
        String selectedFilterID = fVar.getSelectedFilterID();
        if (!(selectedFilterID == null || "raw".equals(selectedFilterID))) {
            ImageFilter createImageFilter = imageFilterAbstractFactory.createImageFilter(selectedFilterID, fVar.getSelectedFilterParameter());
            if (createImageFilter != null) {
                arrayList.add(createImageFilter);
            }
        }
        selectedFilterID = fVar.getListOfAdjustmentFilters();
        if (selectedFilterID != null) {
            String[] split = selectedFilterID.split(",");
            if (split.length > 0) {
                String[] split2 = fVar.getListOfAdjustmentFilterParameters().split("\\],\\[");
                for (int i = 0; i < split2.length; i++) {
                    ImageFilter createImageFilter2 = imageFilterAbstractFactory.createImageFilter(split[i], split2[i]);
                    if (createImageFilter2 != null) {
                        arrayList.add(createImageFilter2);
                    }
                }
            }
        }
        for (ImageFilter imageFilter : arrayList) {
            Log.d("LocalDataManager", "Load filter: " + imageFilter.getName() + ": " + imageFilter.getValue());
        }
        return arrayList;
    }

    public void a(Bitmap bitmap, f fVar) throws IOException {
        OutputStream fileOutputStream;
        Throwable th;
        Object obj = 1;
        if (bitmap != null) {
            File file = new File(this.b, k.ad);
            ExifInterface a = a(g.e(file));
            if (fVar.getCameraOrientation() != 270) {
            }
            a.setTag(a.buildTag(ExifInterface.TAG_ORIENTATION, Integer.valueOf(Math.abs(fVar.getGravityX()) > Math.abs(fVar.getGravityY()) ? 6 : 1)));
            if (fVar.getCurvature() >= 0.0f || fVar.wasRecordedUsingFrontCamera()) {
                obj = null;
            }
            if (obj != null) {
                a.setTag(a.buildTag(ExifInterface.TAG_MAKER_NOTE, "tw=1"));
            }
            fileOutputStream = new FileOutputStream(file);
            try {
                a.writeExif(bitmap, fileOutputStream);
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return;
            } catch (Throwable th2) {
                th = th2;
            }
        } else {
            Log.w("LocalDataManager", "Try to save null bitmap");
            return;
        }
        if (fileOutputStream != null) {
            if (r1 == null) {
                fileOutputStream.close();
            } else {
                try {
                    fileOutputStream.close();
                } catch (Throwable th3) {
                    r1.addSuppressed(th3);
                }
            }
        }
        throw th;
        throw th;
    }

    public void a(FyuseState fyuseState) {
        n();
        this.a.a(this.b.getPath(), fyuseState);
    }

    public void a(boolean z) throws IOException {
        a(z, false);
    }

    public void a(boolean z, boolean z2) throws IOException {
        FyuseContainerType fyuseContainerType = !z ? FyuseContainerType.RECORDED : FyuseContainerType.PROCESSED;
        File file = new File(this.c.getParent(), this.c.getName() + ".tmp");
        g.a(this.b, file, fyuseContainerType, z2);
        if (FyuseUtils.isFyuseFile(file)) {
            file.renameTo(this.c);
        } else {
            file.delete();
            h.c("LocalDataManager", "Saving Fyuse Container file failed " + this.c.getName());
        }
        i();
    }

    public boolean a(f fVar) {
        return a(this.b, fVar);
    }

    public boolean a(f fVar, o oVar) {
        if (this.c.exists()) {
            return FyuseContainerUtils.loadMagicDataFromFile(this.c.getPath(), fVar, (FrameBlender) oVar, Platform.Android);
        }
        String parent = this.c.getParent();
        if (!fVar.a(parent)) {
            return false;
        }
        if (new File(parent, k.ao).exists()) {
            if (oVar.setTweeningFileAndSizes(new File(parent, k.ao).getAbsolutePath(), (int) fVar.getCameraSize().width, (int) fVar.getCameraSize().height, (int) fVar.getProcessedSize().width, (int) fVar.getProcessedSize().height, -1.0f)) {
                oVar.setIndexingOffset(fVar.getStabilizationDataFrameOffset());
                oVar.setLoopClosed(fVar.getLoop_closed_(), 0, fVar.getNumberOfStabilizedFrames());
            }
        }
        return true;
    }

    public boolean a(File file, f fVar) {
        return this.a.a(file.getPath(), fVar);
    }

    public File b() {
        return this.c;
    }

    public File c() {
        return this.b;
    }

    public f d() throws FileNotFoundException {
        return this.a.b(this.c);
    }

    public File e() throws IOException {
        File file = new File(this.b, k.ao);
        if (file.exists()) {
            return file;
        }
        throw new IOException("Tween file does not exist: " + file);
    }

    public void f() throws IOException {
        n();
        g.b(this.c, this.b);
    }

    public ProcessState g() throws IOException {
        return this.a.a(this.c);
    }

    public void h() throws IOException {
        n();
        if (this.c.exists()) {
            this.a.a(this.c, this.b);
            return;
        }
        File parentFile = this.c.getParentFile();
        if (parentFile.exists()) {
            for (File name : this.c.getParentFile().listFiles()) {
                a.a(parentFile, this.b, name.getName());
            }
            return;
        }
        throw new IOException();
    }

    public synchronized void i() {
        a.a(this.b);
    }

    public File j() throws IOException {
        return this.a.d(this.b);
    }

    public void k() throws IOException {
        this.a.c(this.b);
    }

    public File l() {
        return this.a.a(this.b.getPath());
    }
}
