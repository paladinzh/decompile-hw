package com.amap.api.mapcore.util;

import android.view.animation.AnimationUtils;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: MaskLayer */
public class r {
    public FloatBuffer a;
    public ShortBuffer b;
    float[] c = new float[]{GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1};
    short[] d = new short[]{(short) 0, (short) 1, (short) 3, (short) 0, (short) 3, (short) 2};
    private float e = 0.0f;
    private float f = 0.0f;
    private float g = 0.0f;
    private float h = 0.7f;
    private dh i;

    public r() {
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(this.d.length * 2);
        allocateDirect.order(ByteOrder.nativeOrder());
        this.b = allocateDirect.asShortBuffer();
        this.b.put(this.d);
        this.b.position(0);
        allocateDirect = ByteBuffer.allocateDirect(this.c.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        this.a = allocateDirect.asFloatBuffer();
        this.a.put(this.c);
        this.a.position(0);
    }

    protected void a(GL10 gl10, int i, int i2) {
        gl10.glPushMatrix();
        gl10.glScalef(((float) i) / 2.0f, ((float) i2) / 2.0f, 0.0f);
        gl10.glEnableClientState(32884);
        gl10.glDisable(2929);
        gl10.glDisable(3553);
        gl10.glEnable(3042);
        gl10.glBlendFunc(770, 771);
        a();
        gl10.glColor4f(this.e, this.f, this.g, this.h);
        gl10.glVertexPointer(3, 5126, 0, this.a);
        gl10.glDrawElements(4, this.d.length, 5123, this.b);
        gl10.glDisableClientState(32884);
        gl10.glDisable(3042);
        gl10.glPopMatrix();
        gl10.glFlush();
    }

    private void a() {
        dn dnVar = new dn();
        if (this.i != null && !this.i.l()) {
            this.i.a(AnimationUtils.currentAnimationTimeMillis(), dnVar);
            if (!Double.isNaN(dnVar.c)) {
                this.h = (float) dnVar.c;
            }
        }
    }

    public void a(int i, int i2, int i3, int i4) {
        this.e = ((float) i) / 255.0f;
        this.f = ((float) i2) / 255.0f;
        this.g = ((float) i3) / 255.0f;
        this.h = ((float) i4) / 255.0f;
    }

    public void a(dh dhVar) {
        if (!(this.i == null || this.i.l())) {
            this.i.b();
        }
        if (dhVar != null) {
            this.i = dhVar;
            this.i.c();
        }
    }
}
