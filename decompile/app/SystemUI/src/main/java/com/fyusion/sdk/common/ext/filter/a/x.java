package com.fyusion.sdk.common.ext.filter.a;

import android.opengl.GLES20;
import android.util.Log;
import com.fyusion.sdk.common.ext.n;
import com.fyusion.sdk.common.t;
import com.fyusion.sdk.core.util.c;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/* compiled from: Unknown */
public abstract class x {
    private FloatBuffer a = null;
    protected boolean b = false;
    private int c = -1;
    private int d = -1;
    private int e = -1;
    private int f = -1;
    private t g = new t();

    private void b() {
        float[] fArr = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        this.a = allocateDirect.asFloatBuffer();
        this.a.put(fArr);
        this.a.position(0);
        c.a();
    }

    private void b(String str, String str2) {
        int glCreateShader = GLES20.glCreateShader(35633);
        int glCreateShader2 = GLES20.glCreateShader(35632);
        GLES20.glShaderSource(glCreateShader, str);
        GLES20.glShaderSource(glCreateShader2, str2);
        GLES20.glCompileShader(glCreateShader);
        GLES20.glCompileShader(glCreateShader2);
        int[] iArr = new int[1];
        GLES20.glGetShaderiv(glCreateShader2, 35713, iArr, 0);
        if (iArr[0] == 1) {
            this.c = GLES20.glCreateProgram();
            GLES20.glAttachShader(this.c, glCreateShader);
            GLES20.glAttachShader(this.c, glCreateShader2);
            GLES20.glLinkProgram(this.c);
            c.a(this.c);
            GLES20.glDetachShader(this.c, glCreateShader2);
            GLES20.glDetachShader(this.c, glCreateShader);
            GLES20.glDeleteShader(glCreateShader);
            GLES20.glDeleteShader(glCreateShader2);
            this.d = GLES20.glGetAttribLocation(this.c, "quad_vertex");
            this.e = GLES20.glGetUniformLocation(this.c, "texture");
            a(this.c);
            GLES20.glUseProgram(this.c);
            GLES20.glUniform1i(this.e, 0);
            GLES20.glUseProgram(0);
            c.a();
            return;
        }
        String str3 = "Could not compile the shader:\n" + GLES20.glGetShaderInfoLog(glCreateShader2);
        Log.e("TextureRenderer", str3);
        GLES20.glDeleteShader(glCreateShader2);
        throw new RuntimeException(str3);
    }

    protected abstract void a(int i);

    protected void a(String str, String str2) {
        b(str, str2);
        b();
    }

    protected abstract void a(o[] oVarArr, boolean z);

    public void b(int i) {
        this.f = i;
    }

    protected abstract void d();

    protected abstract void e();

    protected abstract void f();

    protected abstract String g();

    protected abstract String h();

    protected abstract n i();

    public void j() {
        n i = i();
        if (i != null) {
            o[] oVarArr = new o[]{new o(i.a, i.b, this.f), null};
            a(oVarArr, false);
            int i2 = oVarArr[1].b;
            r4.a();
            if (this.b) {
                k();
                a(g(), h());
                this.b = false;
            }
            if (this.c <= 0) {
                throw new RuntimeException("TextureRenderer not initialized.");
            } else if (this.f > 0) {
                GLES20.glUseProgram(this.c);
                f();
                d();
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(3553, i2);
                GLES20.glEnableVertexAttribArray(this.d);
                GLES20.glVertexAttribPointer(this.d, 2, 5126, false, 0, this.a);
                GLES20.glBindFramebuffer(36160, 0);
                GLES20.glDrawArrays(6, 0, 4);
                GLES20.glDisableVertexAttribArray(this.d);
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(3553, 0);
                e();
                GLES20.glUseProgram(0);
                c.a();
                oVarArr[1].a(new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(this.f)})), new ArrayList());
            } else {
                throw new RuntimeException("Invalid texture: " + i2);
            }
        }
    }

    protected void k() {
        if (this.c > 0) {
            GLES20.glDeleteProgram(this.c);
            this.c = -1;
        }
    }
}
