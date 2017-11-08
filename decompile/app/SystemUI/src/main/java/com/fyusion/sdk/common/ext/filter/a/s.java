package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.util.Log;
import com.fyusion.sdk.core.util.c;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/* compiled from: Unknown */
public class s {
    boolean a = false;
    boolean b = false;
    public Map<String, Integer> c = new HashMap();
    public Map<String, Integer> d = new HashMap();
    public Map<String, Integer> e = new HashMap();
    public int f = -1;
    public FloatBuffer g = b();
    public int h = -1;
    double i = 2.0d;
    int j = 15;
    List<Vector<Vector<Float>>> k = new ArrayList();

    public s() {
        int i = 0;
        for (int i2 = 2; i2 <= this.j; i2 += 2) {
            Vector vector = new Vector();
            a(i2, vector);
            this.k.add(i, vector);
            i++;
        }
    }

    private void a(int i, Vector<Vector<Float>> vector) {
        int i2;
        double d = ((double) i) / this.i;
        int round = (int) (Math.round(((double) i) / 2.0d) * 2);
        double[] dArr = new double[(round + 1)];
        double d2 = 0.0d;
        for (i2 = 0; i2 <= round; i2++) {
            dArr[i2] = ((1.0d / d) / Math.sqrt(6.28318d)) * Math.exp(Math.pow(((double) i2) / d, 2.0d) * -0.5d);
            d2 += dArr[i2] * 2.0d;
        }
        d2 -= dArr[0];
        for (i2 = 0; i2 <= round; i2++) {
            dArr[i2] = dArr[i2] / d2;
        }
        int i3 = (round / 2) + 1;
        vector.addElement(new Vector(i3));
        vector.addElement(new Vector(i3));
        Vector vector2 = (Vector) vector.get(0);
        Vector vector3 = (Vector) vector.get(1);
        vector2.add(0, Float.valueOf((float) dArr[0]));
        vector3.add(0, Float.valueOf(0.0f));
        for (int i4 = 1; i4 <= i3 - 1; i4++) {
            int i5 = ((i4 - 1) * 2) + 1;
            int i6 = i5 + 1;
            double d3 = dArr[i5] + dArr[i6];
            vector2.add(i4, Float.valueOf((float) d3));
            vector3.add(i4, Float.valueOf((float) (((dArr[i6] * ((double) i6)) + (((double) i5) * dArr[i5])) / d3)));
        }
    }

    private static FloatBuffer b() {
        float[] fArr = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        FloatBuffer asFloatBuffer = allocateDirect.asFloatBuffer();
        asFloatBuffer.put(fArr);
        asFloatBuffer.position(0);
        c.a();
        return asFloatBuffer;
    }

    public void a() {
        GLES20.glDisableVertexAttribArray(this.h);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, 0);
    }

    public void a(o oVar, o oVar2, String str, String str2) {
        a(str, str2);
        if (oVar2 != null) {
            GLES20.glUniform1i(GLES20.glGetUniformLocation(this.f, "texture"), 0);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, oVar.b);
            this.h = GLES20.glGetAttribLocation(this.f, "quad_vertex");
            GLES20.glEnableVertexAttribArray(this.h);
            GLES20.glVertexAttribPointer(this.h, 2, 5126, false, 0, this.g);
            GLES20.glViewport(0, 0, oVar2.c, oVar2.d);
            GLES20.glBindFramebuffer(36160, oVar2.a);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glDisable(3042);
            GLES20.glDrawArrays(6, 0, 4);
            GLES20.glDisableVertexAttribArray(this.h);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, 0);
            c.a();
            return;
        }
        throw new RuntimeException("outputFBO is null");
    }

    public void a(String str, String str2) {
        String str3 = str + str2;
        if (this.c.get(str3) == null) {
            int glCreateShader = GLES20.glCreateShader(35633);
            int glCreateShader2 = GLES20.glCreateShader(35632);
            GLES20.glShaderSource(glCreateShader, str);
            GLES20.glShaderSource(glCreateShader2, str2);
            if (this.b) {
                Log.v("LocalOpenGLUtilsInFilterContainer", "glShaderSource vertex, fragment: " + glCreateShader + ", " + glCreateShader2);
            }
            GLES20.glCompileShader(glCreateShader);
            GLES20.glCompileShader(glCreateShader2);
            int[] iArr = new int[1];
            GLES20.glGetShaderiv(glCreateShader2, 35713, iArr, 0);
            if (iArr[0] == 1) {
                this.f = GLES20.glCreateProgram();
                if (this.b) {
                    Log.v("LocalOpenGLUtilsInFilterContainer", "setShaders, program_: " + this.f);
                }
                GLES20.glAttachShader(this.f, glCreateShader);
                GLES20.glAttachShader(this.f, glCreateShader2);
                GLES20.glLinkProgram(this.f);
                c.a(this.f);
                GLES20.glDetachShader(this.f, glCreateShader2);
                GLES20.glDetachShader(this.f, glCreateShader);
                GLES20.glDeleteShader(glCreateShader);
                GLES20.glDeleteShader(glCreateShader2);
                c.a();
                if (this.f > 0) {
                    this.c.put(str3, Integer.valueOf(this.f));
                } else {
                    throw new RuntimeException("TextureRenderer not initialized.");
                }
            }
            str3 = "Could not compile the shader:\n" + GLES20.glGetShaderInfoLog(glCreateShader2);
            Log.e("FrameBufferObject", str3);
            GLES20.glDeleteShader(glCreateShader2);
            throw new RuntimeException(str3);
        }
        this.f = ((Integer) this.c.get(str3)).intValue();
        GLES20.glUseProgram(this.f);
    }
}
