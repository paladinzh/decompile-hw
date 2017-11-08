package com.fyusion.sdk.viewer.ext.localfyuse;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.f;
import com.fyusion.sdk.common.ext.filter.BlockFilter;
import com.fyusion.sdk.common.ext.filter.ExclusiveFilter;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.ext.filter.ImageFilterAbstractFactory;
import com.fyusion.sdk.common.ext.filter.MultiControlsFilter;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.l;
import fyusion.vislib.FyuseContainerType;
import fyusion.vislib.FyuseContainerUtils;
import fyusion.vislib.Platform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class a {
    private l a;
    private l b;
    private File c;

    public a(l lVar) {
        this.a = lVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(e eVar) throws IOException {
        FileChannel channel;
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        File b = this.a.b();
        if (this.b.a(eVar)) {
            File c = this.b.c();
            int a = f.a(b);
            File file = new File(c, j.ag);
            FileChannel channel2 = new FileInputStream(b).getChannel();
            try {
                channel = new FileOutputStream(file).getChannel();
                try {
                    channel2.transferTo((long) a, channel2.size() - ((long) a), channel);
                    if (channel != null) {
                        channel.close();
                    }
                    if (channel2 != null) {
                        channel2.close();
                    }
                    FyuseContainerUtils.composeFromDir(this.b.c().getPath(), this.c.getPath(), Platform.Android, FyuseContainerType.PROCESSED);
                    com.fyusion.sdk.common.util.a.a(this.b.c());
                    return;
                } catch (Throwable th22) {
                    Throwable th4 = th22;
                    th22 = th;
                    th = th4;
                }
            } catch (Throwable th5) {
                th = th5;
                if (channel2 != null) {
                    if (th3 == null) {
                        channel2.close();
                    } else {
                        try {
                            channel2.close();
                        } catch (Throwable th6) {
                            th3.addSuppressed(th6);
                        }
                    }
                }
                throw th;
            }
        }
        return;
        if (channel != null) {
            if (th22 == null) {
                channel.close();
            } else {
                channel.close();
            }
        }
        throw th;
        throw th;
    }

    private void a(File file) {
        l lVar;
        File b = this.a.b();
        this.c = b;
        if (file.equals(b) || file.getPath().equals(b.getParent())) {
            this.c = b;
            lVar = this.a;
        } else {
            if (file.isDirectory()) {
                this.c = new File(file, b.getName());
            } else {
                this.c = file;
            }
            lVar = new l(g.a(), this.c);
        }
        this.b = lVar;
    }

    private static void a(File file, File file2) throws IOException {
        if (!file2.exists()) {
            file2.mkdirs();
        }
        f.b(file, file2);
        File file3 = new File(file2, j.aj);
        if (!file3.exists()) {
            file3.createNewFile();
        }
        file3 = new File(file2, j.ab);
        if (!file3.exists()) {
            file3.createNewFile();
        }
    }

    public e a() throws FileNotFoundException {
        return this.a.d();
    }

    List<ImageFilter> a(e eVar, ImageFilterAbstractFactory imageFilterAbstractFactory) {
        return this.a.a(eVar, imageFilterAbstractFactory);
    }

    public void a(File file, e eVar, Bitmap bitmap) throws IOException {
        a(file);
        a(this.a.b(), this.b.c());
        this.b.a(bitmap);
        a(eVar);
    }

    void a(Collection<ImageFilter> collection, e eVar) {
        if (eVar != null) {
            List arrayList = new ArrayList();
            Iterable arrayList2 = new ArrayList();
            int i = 0;
            for (ImageFilter imageFilter : collection) {
                int i2;
                if (imageFilter instanceof ExclusiveFilter) {
                    eVar.setSelectedFilterID(imageFilter.getName());
                    eVar.setSelectedFilterParameter(Float.toString(imageFilter.getValue()));
                    i2 = 1;
                } else {
                    if (imageFilter instanceof MultiControlsFilter) {
                        for (Entry key : ((MultiControlsFilter) imageFilter).getFilterValues().entrySet()) {
                            arrayList.add(key.getKey());
                            arrayList2.add(Arrays.asList(new String[]{String.valueOf(key.getValue())}));
                        }
                    } else {
                        arrayList.add(imageFilter.getName());
                        List arrayList3 = new ArrayList();
                        arrayList3.add(String.valueOf(imageFilter.getValue()));
                        if (imageFilter instanceof BlockFilter) {
                            BlockFilter blockFilter = (BlockFilter) imageFilter;
                            arrayList3.add(String.valueOf(blockFilter.getWidth()));
                            arrayList3.add(String.valueOf(blockFilter.getHeight()));
                        }
                        arrayList2.add(arrayList3);
                    }
                    i2 = i;
                }
                i = i2;
            }
            if (i == 0) {
                eVar.setSelectedFilterID(ImageFilterAbstractFactory.NO_TONE_CURVE_FILTER);
                eVar.setSelectedFilterParameter("");
            }
            eVar.setListOfAdjustmentFilters(TextUtils.join(",", arrayList.toArray()));
            eVar.setListOfAdjustmentFilterParameters(TextUtils.join(",", arrayList2));
            Log.d("EditorDataManager", "Tone Filter: " + eVar.getSelectedFilterID() + ": " + eVar.getSelectedFilterParameter());
            Log.d("EditorDataManager", "Adjusment Filters: " + eVar.getListOfAdjustmentFilters() + ": " + eVar.getListOfAdjustmentFilterParameters());
        }
    }
}
