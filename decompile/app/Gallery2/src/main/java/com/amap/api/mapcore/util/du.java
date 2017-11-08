package com.amap.api.mapcore.util;

import android.graphics.Color;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: GLESUtility */
public class du {
    private static void c(GL10 gl10, int i, int i2, FloatBuffer floatBuffer, float f, int i3) {
        if (f != 0.0f) {
            gl10.glPushMatrix();
            gl10.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
            gl10.glEnable(3042);
            gl10.glDisable(2929);
            gl10.glBlendFunc(770, 771);
            gl10.glDisable(3553);
            gl10.glEnableClientState(32884);
            float alpha = ((float) Color.alpha(i2)) / 255.0f;
            float red = ((float) Color.red(i2)) / 255.0f;
            float green = ((float) Color.green(i2)) / 255.0f;
            float blue = ((float) Color.blue(i2)) / 255.0f;
            gl10.glEnable(32925);
            gl10.glLineWidth(f);
            gl10.glVertexPointer(3, 5126, 0, floatBuffer);
            gl10.glColor4f(red, green, blue, alpha);
            gl10.glDrawArrays(i, 0, i3);
            gl10.glDisable(32925);
            gl10.glEnable(2832);
            gl10.glHint(3153, 4354);
            if (f >= 10.0f) {
                f = 6.0f;
            } else if (f >= 5.0f) {
                f -= 2.0f;
            } else if (f >= 2.0f) {
                f -= WMElement.CAMERASIZEVALUE1B1;
            }
            gl10.glColor4f(red, green, blue, alpha / 4.0f);
            gl10.glPointSize(f);
            gl10.glDrawArrays(0, 1, i3 - 2);
            gl10.glDisable(2832);
            gl10.glDisableClientState(32884);
            gl10.glDisableClientState(32888);
            gl10.glDisable(3042);
            gl10.glPopMatrix();
        }
    }

    public static void a(GL10 gl10, int i, int i2, FloatBuffer floatBuffer, float f, int i3) {
        c(gl10, 3, i2, floatBuffer, f, i3);
    }

    public static void a(GL10 gl10, int i, int i2, FloatBuffer floatBuffer, float f, FloatBuffer floatBuffer2, int i3, int i4) {
        c(gl10, 4, i, floatBuffer2, WMElement.CAMERASIZEVALUE1B1, i4);
        c(gl10, 2, i2, floatBuffer, f, i3);
    }

    public static void b(GL10 gl10, int i, int i2, FloatBuffer floatBuffer, float f, int i3) {
        c(gl10, 6, i, floatBuffer, WMElement.CAMERASIZEVALUE1B1, i3);
        c(gl10, 2, i2, floatBuffer, f, i3);
    }
}
