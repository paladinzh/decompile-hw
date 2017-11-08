package com.fyusion.sdk.viewer.ext.localfyuse;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.fyusion.sdk.common.ext.BitmapFilterGenerator;
import com.fyusion.sdk.common.ext.BitmapFilterGenerator.Listener;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.filter.AdjustmentFilter;
import com.fyusion.sdk.common.ext.filter.EditFilterCollection;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.ImageFilterFactory;
import com.fyusion.sdk.common.ext.filter.ToneCurveFilter;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.viewer.FyuseException;
import com.fyusion.sdk.viewer.FyuseViewer;
import com.fyusion.sdk.viewer.RequestListener;
import com.fyusion.sdk.viewer.internal.request.target.a;
import java.io.File;
import java.util.List;

/* compiled from: Unknown */
public class FyuseEditor implements RequestListener {
    private EditFilterCollection a;
    private b b;
    private File c;
    private a d;
    private EditListener e;
    private e f;
    private BitmapFilterGenerator g;

    /* compiled from: Unknown */
    public static class Builder {
        private Context context;
        private File file;
        private EditListener listener;
        private LocalFyuseView targetView;

        Builder context(Context context) {
            this.context = context;
            return this;
        }

        public FyuseEditor edit() {
            Object fyuseEditor = new FyuseEditor((b) this.targetView.getView(), this.file, this.listener);
            fyuseEditor.b.a(fyuseEditor.a);
            if (FyuseViewer.with(this.context).load(this.file).listener(fyuseEditor).highRes(true).into(this.targetView) instanceof a) {
                return fyuseEditor;
            }
            throw new IllegalArgumentException("Illegal target supplied to editor");
        }

        public Builder into(LocalFyuseView localFyuseView) {
            this.targetView = localFyuseView;
            return this;
        }

        public Builder listener(EditListener editListener) {
            this.listener = editListener;
            return this;
        }

        public Builder load(File file) {
            this.file = file;
            return this;
        }
    }

    /* compiled from: Unknown */
    public interface EditListener {
        void onFailed(FyuseException fyuseException, File file);

        void onReady(List<FilterControl> list);

        void onSaved();
    }

    private FyuseEditor(b bVar, File file, EditListener editListener) {
        this.a = new EditFilterCollection();
        this.b = null;
        this.b = bVar;
        this.c = file;
        this.e = editListener;
        this.d = new a(new l(g.a(), file));
    }

    private void a(final File file) {
        if (this.g == null) {
            this.g = new BitmapFilterGenerator();
        }
        try {
            Bitmap a = com.fyusion.sdk.common.ext.util.a.a(this.c, this.f);
            this.b.setVisibility(4);
            this.g.apply(a, this.b.getActiveFilters(), new Listener() {
                public void onApplied(Bitmap bitmap) {
                    try {
                        FyuseEditor.this.d.a(file, FyuseEditor.this.f, bitmap);
                    } catch (Throwable e) {
                        Log.e("FyuseEditor", "Save filtered Fyuse failed.", e);
                        if (FyuseEditor.this.e != null) {
                            FyuseEditor.this.e.onFailed(new FyuseException(e.getMessage()), FyuseEditor.this.c);
                        }
                    }
                    if (FyuseEditor.this.e != null) {
                        FyuseEditor.this.e.onSaved();
                    }
                }
            });
        } catch (Throwable e) {
            Log.e("FyuseEditor", "Unable to save thumbnail", e);
            if (this.e != null) {
                this.e.onFailed(new FyuseException(e.getMessage()), this.c);
            }
        }
    }

    public static Builder with(Context context) {
        return new Builder().context(context);
    }

    public void addAdjustment(@NonNull AdjustmentFilter adjustmentFilter) {
        this.a.addFilter(adjustmentFilter);
    }

    public void disableFilters() {
        this.a.setEnabled(false);
    }

    public void enableFilters() {
        this.a.setEnabled(true);
    }

    public List<FilterControl> getFilterConfiguration() {
        return this.a.getFilterControls();
    }

    public boolean onLoadFailed(@Nullable FyuseException fyuseException, Object obj) {
        if (this.e != null) {
            this.e.onFailed(fyuseException, (File) obj);
        }
        return false;
    }

    public void onProgress(int i) {
    }

    public boolean onResourceReady(Object obj) {
        try {
            this.f = this.d.a();
            this.a.initWith(this.d.a(this.f, new ImageFilterFactory()));
            if (this.e != null) {
                this.e.onReady(this.a.getFilterControls());
            }
            return false;
        } catch (Throwable e) {
            Log.e("FyuseEditor", "Unable to load Fyuse metadata", e);
            return false;
        }
    }

    public void reset() {
        this.a.reset();
    }

    public void save() {
        saveTo(this.c);
    }

    public void saveTo(File file) {
        this.d.a(this.b.getActiveFilters(), this.f);
        if (file.isDirectory()) {
            file = new File(file.getAbsoluteFile() + File.separator + j.ak);
        }
        a(file);
    }

    public void setToneFilter(ToneCurveFilter toneCurveFilter) {
        if (toneCurveFilter != null) {
            this.a.addFilter(toneCurveFilter);
        } else {
            this.a.removeToneCurveFilter();
        }
    }

    public void trim(int i, int i2) {
    }
}
