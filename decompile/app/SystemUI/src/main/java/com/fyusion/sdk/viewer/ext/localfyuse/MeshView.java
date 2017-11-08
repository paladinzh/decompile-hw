package com.fyusion.sdk.viewer.ext.localfyuse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix3f;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptGroup;
import android.renderscript.ScriptGroup.Builder;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.fyusion.sdk.common.h;
import fyusion.vislib.FloatVec;
import fyusion.vislib.IntVec;

/* compiled from: Unknown */
public class MeshView extends FrameLayout {
    ImageView a = null;
    g b = null;
    private boolean c;
    private RenderScript d;
    private ScriptIntrinsicBlur e;
    private ScriptIntrinsicColorMatrix f;
    private Builder g = null;
    private ScriptGroup h;
    private Bitmap i = null;
    private Allocation j = null;
    private Context k;

    public MeshView(Context context) {
        super(context);
        a(context);
    }

    public MeshView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        a(context);
    }

    private void a(Context context) {
        this.k = context;
        setOnTouchListener(new OnTouchListener(this) {
            final /* synthetic */ MeshView a;

            {
                this.a = r1;
            }

            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
        this.a = new ImageView(context);
        this.a.setLayoutParams(layoutParams);
        addView(this.a);
        this.b = new g(context);
        this.b.setLayoutParams(layoutParams);
        addView(this.b);
        this.c = false;
        this.d = RenderScript.create(context);
        this.f = ScriptIntrinsicColorMatrix.create(this.d);
        this.f.setColorMatrix(new Matrix3f(new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f}));
        this.e = ScriptIntrinsicBlur.create(this.d, Element.U8_4(this.d));
        this.e.setRadius(1.0f);
    }

    public Bitmap blurBitmap(Bitmap bitmap) {
        if (this.i == null) {
            this.i = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            this.j = Allocation.createFromBitmap(this.d, this.i);
        }
        Allocation createFromBitmap = Allocation.createFromBitmap(this.d, bitmap);
        this.e.setInput(createFromBitmap);
        if (this.g == null) {
            this.g = new Builder(this.d);
            this.g.addKernel(this.e.getKernelID());
            this.g.addKernel(this.f.getKernelID());
            this.g.addConnection(createFromBitmap.getType(), this.e.getKernelID(), this.f.getKernelID());
            this.h = this.g.create();
            this.h.setOutput(this.f.getKernelID(), this.j);
        }
        this.h.execute();
        createFromBitmap.destroy();
        this.j.copyTo(this.i);
        return this.i;
    }

    public void close() {
        try {
            if (this.e != null) {
                this.e.getKernelID().destroy();
                this.e.destroy();
            }
            if (this.f != null) {
                this.f.getKernelID().destroy();
                this.f.destroy();
            }
            if (this.j != null) {
                this.j.destroy();
            }
            if (this.h != null) {
                this.h.destroy();
            }
            this.g = null;
        } catch (RSInvalidStateException e) {
            h.a("MeshView", "Could not clean up mesh resources: " + e.getMessage());
        }
    }

    public void setImage(Bitmap bitmap, IntVec intVec, FloatVec floatVec, boolean z, boolean z2, float f, Matrix matrix) {
        if (bitmap != null && this.a != null && this.k != null && this.b != null) {
            int i;
            int width = getWidth();
            int height = getHeight();
            Object obj = width >= height ? null : 1;
            this.a.setImageBitmap(blurBitmap(bitmap));
            this.b.a();
            float[] fArr = new float[4];
            boolean z3 = !z2;
            if (obj == null) {
                boolean z4 = z3;
                z3 = z2;
                z2 = z4;
            }
            int i2 = 0;
            while (true) {
                if ((((long) i2) >= intVec.size() / 2 ? 1 : null) != null) {
                    break;
                }
                try {
                    float f2;
                    float f3;
                    float f4;
                    float f5;
                    i = intVec.get(i2 * 2);
                    int i3 = intVec.get((i2 * 2) + 1);
                    int i4 = i * 2;
                    i = (i * 2) + 1;
                    int i5 = i3 * 2;
                    i3 = (i3 * 2) + 1;
                    fArr[0] = floatVec.get(i4);
                    fArr[1] = floatVec.get(i);
                    fArr[2] = floatVec.get(i5);
                    fArr[3] = floatVec.get(i3);
                    matrix.mapPoints(fArr);
                    if (obj == null) {
                        f2 = fArr[0];
                        f3 = fArr[1];
                        f4 = fArr[2];
                        f5 = fArr[3];
                    } else {
                        f2 = fArr[1];
                        f3 = fArr[0];
                        f4 = fArr[3];
                        f5 = fArr[2];
                    }
                    if (z3) {
                        f2 = 1.0f - f2;
                        f4 = 1.0f - f4;
                    }
                    if (z2) {
                        f3 = 1.0f - f3;
                        f5 = 1.0f - f5;
                    }
                    this.b.a(f2 * ((float) width), f3 * ((float) height), f4 * ((float) width), f5 * ((float) height));
                    i2++;
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
            }
            if (this.b.getAlpha() != 1.0f) {
                this.b.setAlpha(1.0f);
            }
            this.b.forceLayout();
            if (!this.c) {
                float width2;
                float height2;
                int height3 = bitmap.getHeight();
                i = bitmap.getWidth();
                Matrix matrix2 = new Matrix();
                if (obj == null) {
                    width2 = ((float) getWidth()) / ((float) i);
                    height2 = ((float) getHeight()) / ((float) height3);
                    if (f > 0.0f) {
                        matrix2.setRotate(180.0f);
                        matrix2.postTranslate((float) width, (float) height);
                    }
                } else {
                    width2 = ((float) getWidth()) / ((float) height3);
                    height2 = ((float) getHeight()) / ((float) i);
                    matrix2.setRotate(90.0f);
                    matrix2.postTranslate((float) width, 0.0f);
                }
                height2 = Math.max(width2, height2);
                matrix2.preScale(height2, height2);
                this.a.setScaleType(ScaleType.MATRIX);
                this.a.setImageMatrix(matrix2);
                if (!z) {
                    this.a.setScaleY(1.0f);
                } else if (f <= 0.0f) {
                    if (obj == null) {
                        this.a.setScaleX(-1.0f);
                    } else {
                        this.a.setScaleY(-1.0f);
                    }
                }
                this.c = true;
            }
        }
    }
}
