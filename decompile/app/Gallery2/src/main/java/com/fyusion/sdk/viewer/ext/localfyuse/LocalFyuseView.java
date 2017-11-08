package com.fyusion.sdk.viewer.ext.localfyuse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Pair;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.viewer.ext.localfyuse.a.a.a;
import com.fyusion.sdk.viewer.view.FyuseView;
import com.fyusion.sdk.viewer.view.l;

/* compiled from: Unknown */
public class LocalFyuseView extends FyuseView {
    private MeshRenderer c;
    private a d;
    private MeshView e;

    public LocalFyuseView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected l createMainView(Context context, AttributeSet attributeSet) {
        return new b(context, attributeSet);
    }

    protected void onFyuseShown() {
        if (this.c != null) {
            this.c.complete();
            this.e.close();
        }
    }

    public void setFyuseData(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        super.setFyuseData(aVar);
        if (aVar instanceof a) {
            this.d = (a) aVar;
            ((b) this.a).a(this.d.b());
        }
    }

    public void setProgress(int i, int i2, Object obj) {
        post(new Runnable(this) {
            final /* synthetic */ LocalFyuseView a;

            {
                this.a = r1;
            }

            public void run() {
                if (this.a.c == null && this.a.d != null) {
                    this.a.e = new MeshView(this.a.getContext());
                    this.a.addView(this.a.e);
                    this.a.c = new MeshRenderer(this.a.e, this.a.d.a(), this.a.d.c());
                }
            }
        });
        if (this.c != null && obj != null) {
            try {
                Pair pair = (Pair) obj;
                this.c.renderMeshOntoBitmap(i, (Bitmap) pair.first, (Matrix) pair.second);
            } catch (Throwable e) {
                DLog.e("Mesh", "Cannot properly cast resource", e);
            }
        }
    }
}
