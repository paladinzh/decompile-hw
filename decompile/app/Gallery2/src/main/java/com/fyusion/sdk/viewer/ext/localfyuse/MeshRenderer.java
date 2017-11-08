package com.fyusion.sdk.viewer.ext.localfyuse;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.g;
import fyusion.vislib.ISDJavaHelper;
import fyusion.vislib.MeshData;
import fyusion.vislib.MeshRendering;
import java.io.File;

/* compiled from: Unknown */
public class MeshRenderer {
    boolean a = false;
    ISDJavaHelper b = null;
    private final boolean c;
    private Bitmap d = null;
    private Canvas e = null;
    private MeshView f = null;
    private String g;
    private float h;
    private boolean i = false;

    public MeshRenderer(MeshView meshView, e eVar, File file) {
        boolean z = false;
        int cameraOrientation = eVar.getCameraOrientation();
        this.i = eVar.wasRecordedUsingFrontCamera();
        if (this.i && cameraOrientation == 90) {
            z = true;
        }
        this.c = z;
        this.g = g.a().c(file.getPath());
        this.f = meshView;
        this.h = eVar.getGravityX();
    }

    public void complete() {
        this.f.post(new Runnable(this) {
            final /* synthetic */ MeshRenderer a;

            {
                this.a = r1;
            }

            public void run() {
                this.a.a = true;
                this.a.f.setVisibility(8);
            }
        });
    }

    public void renderMeshOntoBitmap(final int i, Bitmap bitmap, Matrix matrix) {
        if (bitmap != null && matrix != null) {
            if (this.b == null) {
                this.b = MeshRendering.load_isd_java(this.g);
            }
            final Matrix matrix2 = new Matrix(matrix);
            if (this.d == null || this.e == null || this.d.getWidth() != bitmap.getWidth() / 8 || this.d.getHeight() != bitmap.getHeight() / 8) {
                this.d = Bitmap.createBitmap(bitmap.getWidth() / 8, bitmap.getHeight() / 8, bitmap.getConfig());
                this.e = new Canvas(this.d);
                this.e.scale(0.125f, 0.125f);
            }
            this.e.drawBitmap(bitmap, 0.0f, 0.0f, null);
            this.f.post(new Runnable(this) {
                final /* synthetic */ MeshRenderer c;

                public void run() {
                    if (!this.c.a) {
                        MeshData computeMeshDataForFrameId = MeshRendering.computeMeshDataForFrameId(i - 1, this.c.b);
                        this.c.f.setImage(this.c.d, computeMeshDataForFrameId.getEdge_pair_indices(), computeMeshDataForFrameId.getMesh_point_coordinates(), this.c.c, this.c.i, this.c.h, matrix2);
                    }
                }
            });
        }
    }
}
