package com.fyusion.sdk.processor;

import android.graphics.Rect;
import android.util.Pair;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessDataType;
import com.fyusion.sdk.common.ext.ProcessedImageListener;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.h;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.i.a;
import com.fyusion.sdk.common.i.b;
import fyusion.vislib.Fyuse;
import fyusion.vislib.ImuUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.mtnwrw.pdqimg.ConversionService;
import org.mtnwrw.pdqimg.DecompressionService;
import org.mtnwrw.pdqimg.DecompressionService.ImageInformation;
import org.mtnwrw.pdqimg.PDQImage;

/* compiled from: Unknown */
public class c extends a {
    private ProcessItem b;
    private ProcessedImageListener c;
    private l d;

    public c(ProcessItem processItem, ProcessedImageListener processedImageListener) {
        this.b = processItem;
        this.c = processedImageListener;
        this.d = new l(g.a(), processItem.getFile());
    }

    private int a(String str) {
        return 360;
    }

    private String a(int i, float f) {
        OutputStream fileOutputStream;
        Throwable th;
        Throwable th2;
        File b = this.d.b();
        h hVar = new h(b);
        if (!hVar.d()) {
            DLog.e("InfoProcessorJob", "Image blob does not exist");
            return null;
        } else if (hVar.a(a.READ_ONLY, b.NONE)) {
            File file;
            File file2 = this.b.getFile();
            if (file2.isDirectory()) {
                file = new File(b.getParent(), String.format(Locale.US, j.aJ, new Object[]{Integer.valueOf((int) f)}));
            } else {
                String name = file2.getName();
                if (name.lastIndexOf(".") > 0) {
                    name = name.substring(0, name.lastIndexOf("."));
                }
                file = new File(b.getParent(), String.format(Locale.US, name + "_" + j.aJ, new Object[]{Integer.valueOf((int) f)}));
            }
            FileInputStream b2;
            try {
                b2 = hVar.b(i);
                fileOutputStream = new FileOutputStream(file);
                try {
                    Object obj;
                    if (hVar.o()) {
                        b2.getChannel().transferTo(0, b2.getChannel().size(), fileOutputStream.getChannel());
                        obj = 1;
                    } else {
                        FileChannel channel = b2.getChannel();
                        long position = channel.position();
                        byte[] bArr = new byte[64];
                        if (b2.read(bArr) >= 64) {
                            ImageInformation imageInformation = DecompressionService.getImageInformation(bArr);
                            PDQImage decompressImage = DecompressionService.decompressImage(channel.map(MapMode.READ_ONLY, position, (long) imageInformation.StreamSize));
                            if (decompressImage != null) {
                                decompressImage.swapUVChannels(false);
                                ConversionService.convertPDQImageToYuvImage(decompressImage).compressToJpeg(new Rect(0, 0, imageInformation.Width, imageInformation.Height), 90, fileOutputStream);
                                obj = 1;
                                decompressImage.close();
                            }
                        }
                        obj = null;
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    hVar.a(b2);
                    return obj == null ? null : file.getPath();
                } catch (Throwable th22) {
                    Throwable th3 = th22;
                    th22 = th;
                    th = th3;
                }
            } catch (Exception e) {
                if (e instanceof IndexOutOfBoundsException) {
                    DLog.w("InfoProcessorJob", "Capture does not contain data at requested interval: " + f);
                } else {
                    e.printStackTrace();
                }
                return null;
            } catch (Throwable th4) {
                hVar.a(b2);
            }
        } else {
            DLog.e("InfoProcessorJob", "Cannot open image blob");
            return null;
        }
        if (fileOutputStream != null) {
            if (th22 == null) {
                fileOutputStream.close();
            } else {
                try {
                    fileOutputStream.close();
                } catch (Throwable th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        throw th;
        throw th;
    }

    private List<Pair<String, Integer>> a(int i, int i2) {
        List<Pair<String, Integer>> arrayList = new ArrayList();
        try {
            this.d.h();
            Fyuse d = this.d.d();
            int[] frameIndicesForUniformRotationIntervals = ImuUtils.frameIndicesForUniformRotationIntervals(d, this.d.m(), d.isPortrait(), i2 / i);
            if (frameIndicesForUniformRotationIntervals != null && frameIndicesForUniformRotationIntervals.length > 0) {
                for (int i3 = 0; i3 < frameIndicesForUniformRotationIntervals.length; i3++) {
                    String a = a(frameIndicesForUniformRotationIntervals[i3], (float) (i3 * i));
                    if (a != null) {
                        DLog.d("InfoProcessorJob", a);
                    }
                    arrayList.add(new Pair(a, Float.valueOf(((float) i3) * ((float) i))));
                }
                return arrayList;
            }
            DLog.e("InfoProcessorJob", "Number of frames for this fyuse is " + frameIndicesForUniformRotationIntervals.length + " ignoring.");
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private List<Pair<String, Integer>> a(double[] dArr) {
        int i = 0;
        List<Pair<String, Integer>> arrayList = new ArrayList();
        try {
            double[] dArr2 = new double[dArr.length];
            for (int i2 = 0; i2 < dArr.length; i2++) {
                dArr2[i2] = (dArr[i2] * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude;
            }
            this.d.h();
            Fyuse d = this.d.d();
            int[] frameIndicesAtSpecifiedIntervalsRadians = ImuUtils.frameIndicesAtSpecifiedIntervalsRadians(d, this.d.m(), d.isPortrait(), dArr2);
            if (frameIndicesAtSpecifiedIntervalsRadians != null && frameIndicesAtSpecifiedIntervalsRadians.length > 0) {
                while (i < frameIndicesAtSpecifiedIntervalsRadians.length) {
                    String a = a(frameIndicesAtSpecifiedIntervalsRadians[i], (float) dArr[i]);
                    if (a != null) {
                        DLog.d("InfoProcessorJob", a);
                    }
                    arrayList.add(new Pair(a, Float.valueOf((float) dArr[i])));
                    i++;
                }
                return arrayList;
            }
            DLog.e("InfoProcessorJob", "Number of frames for this fyuse is " + frameIndicesAtSpecifiedIntervalsRadians.length + " ignoring.");
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void a() {
        if (this.c != null) {
            List a = this.b.getDataType() != ProcessDataType.DEGREE_LIST ? a(this.b.getInterval(), a(this.b.getPath())) : a(this.b.getAngleList());
            if (this.c != null) {
                this.c.onImageDataReady(this.b, a);
            }
        }
    }

    public void a(RuntimeException runtimeException) {
    }
}
