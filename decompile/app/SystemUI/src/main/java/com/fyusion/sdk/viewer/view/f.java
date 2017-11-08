package com.fyusion.sdk.viewer.view;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLDebugHelper;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: Unknown */
public class f extends TextureView implements SurfaceTextureListener, OnLayoutChangeListener {
    private static int a = -1;
    private static final j b = new j();
    private final WeakReference<f> c = new WeakReference(this);
    private i d;
    private Renderer e;
    private boolean f;
    private e g;
    private f h;
    private g i;
    private k j;
    private int k;
    private int l;
    private boolean m;

    /* compiled from: Unknown */
    public interface e {
        EGLConfig a(EGL10 egl10, EGLDisplay eGLDisplay);
    }

    /* compiled from: Unknown */
    private abstract class a implements e {
        protected int[] a;
        final /* synthetic */ f b;

        public a(f fVar, int[] iArr) {
            this.b = fVar;
            this.a = a(iArr);
        }

        private int[] a(int[] iArr) {
            if (this.b.l != 2 && this.b.l != 3) {
                return iArr;
            }
            int length = iArr.length;
            Object obj = new int[(length + 2)];
            System.arraycopy(iArr, 0, obj, 0, length - 1);
            obj[length - 1] = 12352;
            if (this.b.l != 2) {
                obj[length] = 64;
            } else {
                obj[length] = 4;
            }
            obj[length + 1] = 12344;
            return obj;
        }

        public EGLConfig a(EGL10 egl10, EGLDisplay eGLDisplay) {
            int[] iArr = new int[1];
            if (egl10.eglChooseConfig(eGLDisplay, this.a, null, 0, iArr)) {
                int i = iArr[0];
                if (i > 0) {
                    EGLConfig[] eGLConfigArr = new EGLConfig[i];
                    if (egl10.eglChooseConfig(eGLDisplay, this.a, eGLConfigArr, i, iArr)) {
                        EGLConfig a = a(egl10, eGLDisplay, eGLConfigArr);
                        if (a != null) {
                            return a;
                        }
                        throw new IllegalArgumentException("No config chosen");
                    }
                    throw new IllegalArgumentException("eglChooseConfig#2 failed");
                }
                throw new IllegalArgumentException("No configs match configSpec");
            }
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        abstract EGLConfig a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);
    }

    /* compiled from: Unknown */
    private class b extends a {
        protected int c;
        protected int d;
        protected int e;
        protected int f;
        protected int g;
        protected int h;
        final /* synthetic */ f i;
        private int[] j = new int[1];

        public b(f fVar, int i, int i2, int i3, int i4, int i5, int i6) {
            this.i = fVar;
            super(fVar, new int[]{12324, i, 12323, i2, 12322, i3, 12321, i4, 12325, i5, 12326, i6, 12344});
            this.c = i;
            this.d = i2;
            this.e = i3;
            this.f = i4;
            this.g = i5;
            this.h = i6;
        }

        private int a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, int i, int i2) {
            return !egl10.eglGetConfigAttrib(eGLDisplay, eGLConfig, i, this.j) ? i2 : this.j[0];
        }

        public EGLConfig a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr) {
            for (EGLConfig eGLConfig : eGLConfigArr) {
                int a = a(egl10, eGLDisplay, eGLConfig, 12325, 0);
                int a2 = a(egl10, eGLDisplay, eGLConfig, 12326, 0);
                if (a >= this.g && a2 >= this.h) {
                    a = a(egl10, eGLDisplay, eGLConfig, 12324, 0);
                    int a3 = a(egl10, eGLDisplay, eGLConfig, 12323, 0);
                    int a4 = a(egl10, eGLDisplay, eGLConfig, 12322, 0);
                    a2 = a(egl10, eGLDisplay, eGLConfig, 12321, 0);
                    if (a == this.c && a3 == this.d && a4 == this.e && a2 == this.f) {
                        return eGLConfig;
                    }
                }
            }
            return null;
        }
    }

    /* compiled from: Unknown */
    public interface f {
        EGLContext a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig);

        void a(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext);
    }

    /* compiled from: Unknown */
    private class c implements f {
        final /* synthetic */ f a;
        private int b;

        private c(f fVar) {
            this.a = fVar;
            this.b = 12440;
        }

        public EGLContext a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig) {
            int[] iArr = new int[]{this.b, this.a.l, 12344};
            EGLContext eGLContext = EGL10.EGL_NO_CONTEXT;
            if (this.a.l == 0) {
                iArr = null;
            }
            return egl10.eglCreateContext(eGLDisplay, eGLConfig, eGLContext, iArr);
        }

        public void a(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext) {
            if (!egl10.eglDestroyContext(eGLDisplay, eGLContext)) {
                Log.e("DefaultContextFactory", "display:" + eGLDisplay + " context: " + eGLContext);
                h.a("eglDestroyContex", egl10.eglGetError());
            }
        }
    }

    /* compiled from: Unknown */
    public interface g {
        EGLSurface a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj);

        void a(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface);
    }

    /* compiled from: Unknown */
    private static class d implements g {
        private d() {
        }

        public EGLSurface a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj) {
            EGLSurface eGLSurface = null;
            try {
                eGLSurface = egl10.eglCreateWindowSurface(eGLDisplay, eGLConfig, obj, null);
            } catch (Throwable e) {
                Log.e("GLTextureView", "eglCreateWindowSurface", e);
            }
            return eGLSurface;
        }

        public void a(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface) {
            egl10.eglDestroySurface(eGLDisplay, eGLSurface);
        }
    }

    /* compiled from: Unknown */
    private static class h {
        EGL10 a;
        EGLDisplay b;
        EGLSurface c;
        EGLConfig d;
        EGLContext e;
        private WeakReference<f> f;

        public h(WeakReference<f> weakReference) {
            this.f = weakReference;
        }

        public static String a(int i) {
            switch (i) {
                case 12288:
                    return "EGL_SUCCESS";
                case 12289:
                    return "EGL_NOT_INITIALIZED";
                case 12290:
                    return "EGL_BAD_ACCESS";
                case 12291:
                    return "EGL_BAD_ALLOC";
                case 12292:
                    return "EGL_BAD_ATTRIBUTE";
                case 12293:
                    return "EGL_BAD_CONFIG";
                case 12294:
                    return "EGL_BAD_CONTEXT";
                case 12295:
                    return "EGL_BAD_CURRENT_SURFACE";
                case 12296:
                    return "EGL_BAD_DISPLAY";
                case 12297:
                    return "EGL_BAD_MATCH";
                case 12298:
                    return "EGL_BAD_NATIVE_PIXMAP";
                case 12299:
                    return "EGL_BAD_NATIVE_WINDOW";
                case 12300:
                    return "EGL_BAD_PARAMETER";
                case 12301:
                    return "EGL_BAD_SURFACE";
                case 12302:
                    return "EGL_CONTEXT_LOST";
                default:
                    return String.format(Locale.US, "0x%4s", new Object[]{Integer.toHexString(i)}).replace(' ', '0');
            }
        }

        private void a(String str) {
            a(str, this.a.eglGetError());
        }

        public static void a(String str, int i) {
            throw new RuntimeException(b(str, i));
        }

        public static void a(String str, String str2, int i) {
            Log.w(str, b(str2, i));
        }

        public static String b(String str, int i) {
            return str + " failed: " + a(i);
        }

        private void g() {
            if (this.c != null && this.c != EGL10.EGL_NO_SURFACE) {
                this.a.eglMakeCurrent(this.b, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                f fVar = (f) this.f.get();
                if (fVar != null) {
                    fVar.i.a(this.a, this.b, this.c);
                }
                this.c = null;
            }
        }

        public void a() {
            this.a = (EGL10) EGLContext.getEGL();
            this.b = this.a.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.b != EGL10.EGL_NO_DISPLAY) {
                if (this.a.eglInitialize(this.b, new int[2])) {
                    f fVar = (f) this.f.get();
                    if (fVar != null) {
                        this.d = fVar.g.a(this.a, this.b);
                        this.e = fVar.h.a(this.a, this.b, this.d);
                    } else {
                        this.d = null;
                        this.e = null;
                    }
                    if (this.e == null || this.e == EGL10.EGL_NO_CONTEXT) {
                        this.e = null;
                        a("createContext");
                    }
                    this.c = null;
                    return;
                }
                throw new RuntimeException("eglInitialize failed");
            }
            throw new RuntimeException("eglGetDisplay failed");
        }

        public boolean b() {
            if (this.a == null) {
                throw new RuntimeException("egl not initialized");
            } else if (this.b == null) {
                throw new RuntimeException("eglDisplay not initialized");
            } else if (this.d != null) {
                g();
                f fVar = (f) this.f.get();
                if (fVar == null) {
                    this.c = null;
                } else {
                    this.c = fVar.i.a(this.a, this.b, this.d, fVar.getSurfaceTexture());
                }
                if (this.c == null || this.c == EGL10.EGL_NO_SURFACE) {
                    if (this.a.eglGetError() == 12299) {
                        Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    }
                    return false;
                } else if (this.a.eglMakeCurrent(this.b, this.c, this.c, this.e)) {
                    return true;
                } else {
                    a("EGLHelper", "eglMakeCurrent", this.a.eglGetError());
                    return false;
                }
            } else {
                throw new RuntimeException("mEglConfig not initialized");
            }
        }

        GL c() {
            int i = 0;
            GL gl = this.e.getGL();
            f fVar = (f) this.f.get();
            if (fVar == null) {
                return gl;
            }
            if (fVar.j != null) {
                gl = fVar.j.a(gl);
            }
            if ((fVar.k & 3) == 0) {
                return gl;
            }
            if ((fVar.k & 1) != 0) {
                i = 1;
            }
            return GLDebugHelper.wrap(gl, i, (fVar.k & 2) == 0 ? null : new l());
        }

        public int d() {
            return this.a.eglSwapBuffers(this.b, this.c) ? 12288 : this.a.eglGetError();
        }

        public void e() {
            g();
        }

        public void f() {
            if (this.e != null) {
                f fVar = (f) this.f.get();
                if (fVar != null) {
                    fVar.h.a(this.a, this.b, this.e);
                }
                this.e = null;
            }
            if (this.b != null) {
                this.a.eglTerminate(this.b);
                this.b = null;
            }
        }
    }

    /* compiled from: Unknown */
    static class i extends Thread {
        private boolean a;
        private boolean b;
        private boolean c;
        private boolean d;
        private boolean e;
        private boolean f;
        private boolean g;
        private boolean h;
        private boolean i;
        private boolean j;
        private boolean k;
        private int l = 0;
        private int m = 0;
        private int n = 1;
        private boolean o = true;
        private boolean p;
        private ArrayList<Runnable> q = new ArrayList();
        private boolean r = true;
        private final Object s = new Object();
        private h t;
        private WeakReference<f> u;

        i(WeakReference<f> weakReference) {
            this.u = weakReference;
        }

        private void j() {
            if (this.i) {
                this.i = false;
                this.t.e();
            }
        }

        private void k() {
            if (this.h) {
                this.t.f();
                this.h = false;
                f.b.c(this);
            }
        }

        private void l() throws InterruptedException {
            this.t = new h(this.u);
            this.h = false;
            this.i = false;
            GL10 gl10 = null;
            Object obj = null;
            Runnable runnable = null;
            try {
                synchronized (this.s) {
                    Object obj2 = null;
                    int i = 0;
                    Object obj3 = null;
                    Object obj4 = null;
                    Object obj5 = null;
                    Object obj6 = null;
                    Object obj7 = null;
                    int i2 = 0;
                    Object obj8 = null;
                    while (!this.a) {
                        f fVar;
                        Object obj9;
                        int i3;
                        Object obj10;
                        int i4;
                        Object obj11;
                        int i5;
                        Object obj12;
                        int i6;
                        Runnable runnable2;
                        Object obj13;
                        if (this.q.isEmpty()) {
                            boolean z;
                            if (this.d == this.c) {
                                z = false;
                            } else {
                                boolean z2 = this.c;
                                this.d = this.c;
                                this.s.notifyAll();
                                z = z2;
                            }
                            synchronized (f.b) {
                                if (this.k) {
                                    j();
                                    k();
                                    this.k = false;
                                    obj5 = 1;
                                }
                                if (obj8 != null) {
                                    j();
                                    k();
                                    obj8 = null;
                                }
                                if (z && this.i) {
                                    j();
                                }
                                if (z) {
                                    if (this.h) {
                                        fVar = (f) this.u.get();
                                        if (!(fVar != null ? fVar.m : false) || f.b.a()) {
                                            k();
                                        }
                                    }
                                }
                            }
                            if (z) {
                                if (f.b.b()) {
                                    this.t.f();
                                }
                            }
                            if (!(this.e || this.g)) {
                                if (this.i) {
                                    synchronized (f.b) {
                                        j();
                                    }
                                }
                                this.g = true;
                                this.f = false;
                                this.s.notifyAll();
                            }
                            if (this.e && this.g) {
                                this.g = false;
                                this.s.notifyAll();
                            }
                            if (obj != null) {
                                obj7 = null;
                                obj = null;
                                this.p = true;
                                this.s.notifyAll();
                            }
                            if (m()) {
                                if (!this.h) {
                                    obj9 = null;
                                    synchronized (f.b) {
                                        if (obj5 != null) {
                                            obj5 = null;
                                        } else if (f.b.b(this)) {
                                            try {
                                                this.t.a();
                                                this.h = true;
                                                obj4 = 1;
                                                obj9 = 1;
                                            } catch (RuntimeException e) {
                                                f.b.c(this);
                                                throw e;
                                            }
                                        }
                                    }
                                    if (obj9 != null) {
                                        this.s.notifyAll();
                                    }
                                }
                                if (this.h && !this.i) {
                                    this.i = true;
                                    obj6 = 1;
                                    i3 = 1;
                                    int i7 = 1;
                                } else {
                                    obj9 = obj2;
                                    obj2 = obj3;
                                }
                                if (this.i) {
                                    if (this.r) {
                                        obj7 = 1;
                                        i2 = this.l;
                                        i3 = this.m;
                                        obj10 = 1;
                                        obj3 = 1;
                                        this.r = false;
                                    } else {
                                        obj3 = obj6;
                                        i4 = i;
                                        obj10 = obj7;
                                        obj7 = obj9;
                                        i3 = i2;
                                        i2 = i4;
                                    }
                                    this.o = false;
                                    this.s.notifyAll();
                                    obj6 = obj5;
                                    obj5 = obj4;
                                    obj4 = obj10;
                                    obj10 = obj7;
                                    obj11 = obj;
                                    obj = obj3;
                                    i5 = i2;
                                    obj12 = obj8;
                                    i6 = i3;
                                    obj9 = obj2;
                                    runnable2 = runnable;
                                    obj13 = obj11;
                                } else {
                                    obj3 = obj2;
                                    obj2 = obj9;
                                }
                            }
                            this.s.wait();
                        } else {
                            obj13 = obj;
                            obj = obj6;
                            obj6 = obj5;
                            obj5 = obj4;
                            obj4 = obj7;
                            obj9 = obj3;
                            i5 = i;
                            obj10 = obj2;
                            runnable2 = (Runnable) this.q.remove(0);
                            int i8 = i2;
                            obj12 = obj8;
                            i6 = i8;
                        }
                        if (runnable2 == null) {
                            GL10 gl102;
                            Object obj14;
                            if (obj == null) {
                                obj7 = obj;
                            } else if (this.t.b()) {
                                this.j = true;
                                this.s.notifyAll();
                                obj7 = null;
                            } else {
                                this.j = true;
                                this.f = true;
                                this.s.notifyAll();
                                obj7 = obj4;
                                obj4 = obj5;
                                obj5 = obj6;
                                obj6 = obj;
                                obj = obj13;
                                runnable = runnable2;
                                obj2 = obj10;
                                i = i5;
                                obj3 = obj9;
                                obj11 = obj12;
                                i2 = i6;
                                obj8 = obj11;
                            }
                            if (obj9 == null) {
                                gl102 = gl10;
                                obj14 = obj9;
                            } else {
                                GL10 gl103 = (GL10) this.t.c();
                                f.b.a(gl103);
                                obj14 = null;
                                gl102 = gl103;
                            }
                            if (obj5 != null) {
                                fVar = (f) this.u.get();
                                if (fVar != null) {
                                    fVar.e.onSurfaceCreated(gl102, this.t.d);
                                }
                                obj5 = null;
                            }
                            if (obj10 != null) {
                                fVar = (f) this.u.get();
                                if (fVar != null) {
                                    fVar.e.onSurfaceChanged(gl102, i5, i6);
                                }
                                obj10 = null;
                            }
                            fVar = (f) this.u.get();
                            if (fVar != null) {
                                fVar.e.onDrawFrame(gl102);
                            }
                            i3 = this.t.d();
                            switch (i3) {
                                case 12288:
                                    break;
                                case 12302:
                                    i2 = 1;
                                    break;
                                default:
                                    h.a("GLThread", "eglSwapBuffers", i3);
                                    this.f = true;
                                    this.s.notifyAll();
                                    break;
                            }
                            obj9 = obj4 == null ? obj13 : 1;
                            runnable = runnable2;
                            obj2 = obj10;
                            i = i5;
                            obj3 = obj14;
                            gl10 = gl102;
                            obj = obj9;
                            i4 = i6;
                            obj8 = obj12;
                            i2 = i4;
                            Object obj15 = obj6;
                            obj6 = obj7;
                            obj7 = obj4;
                            obj4 = obj5;
                            obj5 = obj15;
                        } else {
                            runnable2.run();
                            obj7 = obj4;
                            obj4 = obj5;
                            obj5 = obj6;
                            obj6 = obj;
                            obj = obj13;
                            runnable = null;
                            obj2 = obj10;
                            i = i5;
                            obj3 = obj9;
                            obj11 = obj12;
                            i2 = i6;
                            obj8 = obj11;
                        }
                    }
                }
                synchronized (f.b) {
                    j();
                    k();
                }
                synchronized (this.s) {
                    this.s.notifyAll();
                }
            } catch (Throwable th) {
                synchronized (f.b) {
                    j();
                    k();
                    synchronized (this.s) {
                        this.s.notifyAll();
                    }
                }
            }
        }

        private boolean m() {
            if (!this.d && this.e && !this.f && this.l > 0 && this.m > 0) {
                if (this.o || this.n == 1) {
                    return true;
                }
            }
            return false;
        }

        public void a(int i) {
            if (i >= 0 && i <= 1) {
                synchronized (this.s) {
                    this.n = i;
                    this.s.notifyAll();
                }
                return;
            }
            throw new IllegalArgumentException("renderMode");
        }

        public void a(int i, int i2) {
            synchronized (this.s) {
                this.l = i;
                this.m = i2;
                this.r = true;
                this.o = true;
                this.p = false;
                this.s.notifyAll();
                while (!this.b && !this.d && !this.p && a()) {
                    try {
                        this.s.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public boolean a() {
            return this.h && this.i && m();
        }

        public int b() {
            int i;
            synchronized (this.s) {
                i = this.n;
            }
            return i;
        }

        public void c() {
            synchronized (this.s) {
                this.o = true;
                this.s.notifyAll();
            }
        }

        public void d() {
            synchronized (this.s) {
                this.e = true;
                this.j = false;
                this.s.notifyAll();
                while (this.g && !this.j && !this.b) {
                    try {
                        this.s.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void e() {
            synchronized (this.s) {
                this.e = false;
                this.s.notifyAll();
                while (!this.g && !this.b) {
                    try {
                        this.s.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void f() {
            synchronized (this.s) {
                this.c = true;
                this.s.notifyAll();
                while (!this.b && !this.d) {
                    try {
                        this.s.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void g() {
            synchronized (this.s) {
                this.c = false;
                this.o = true;
                this.p = false;
                this.s.notifyAll();
                while (!this.b && this.d && !this.p) {
                    try {
                        this.s.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void h() {
            synchronized (this.s) {
                this.a = true;
                this.s.notifyAll();
            }
            synchronized (f.b) {
                while (!this.b) {
                    try {
                        f.b.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void i() {
            this.k = true;
            f.b.notifyAll();
        }

        public void run() {
            setName("GLThread " + getId());
            try {
                l();
            } catch (InterruptedException e) {
            } finally {
                f.b.a(this);
            }
        }
    }

    /* compiled from: Unknown */
    private static class j {
        private static String a = "GLThreadManager";
        private boolean b;
        private int c;
        private boolean d;
        private boolean e;
        private boolean f;
        private i g;

        private j() {
        }

        private void c() {
            if (!this.b) {
                com.fyusion.sdk.core.util.b.b(f.a, -1);
                this.c = f.a;
                if (this.c >= 131072) {
                    this.e = true;
                }
                this.b = true;
            }
        }

        public synchronized void a(i iVar) {
            iVar.b = true;
            if (this.g == iVar) {
                this.g = null;
            }
            notifyAll();
        }

        public synchronized void a(GL10 gl10) {
            boolean z = false;
            synchronized (this) {
                if (!this.d) {
                    c();
                    String glGetString = gl10.glGetString(7937);
                    if (this.c < 131072) {
                        this.e = !glGetString.startsWith("Q3Dimension MSM7500 ");
                        notifyAll();
                    }
                    if (!this.e) {
                        z = true;
                    }
                    this.f = z;
                    this.d = true;
                }
            }
        }

        public synchronized boolean a() {
            return this.f;
        }

        public synchronized boolean b() {
            boolean z = false;
            synchronized (this) {
                c();
                if (!this.e) {
                    z = true;
                }
            }
            return z;
        }

        public boolean b(i iVar) {
            if (this.g == iVar || this.g == null) {
                this.g = iVar;
                notifyAll();
                return true;
            }
            c();
            if (this.e) {
                return true;
            }
            if (this.g != null) {
                this.g.i();
            }
            return false;
        }

        public void c(i iVar) {
            if (this.g == iVar) {
                this.g = null;
            }
            notifyAll();
        }
    }

    /* compiled from: Unknown */
    public interface k {
        GL a(GL gl);
    }

    /* compiled from: Unknown */
    static class l extends Writer {
        private StringBuilder a = new StringBuilder();

        l() {
        }

        private void a() {
            if (this.a.length() > 0) {
                Log.v("GLTextureView", this.a.toString());
                this.a.delete(0, this.a.length());
            }
        }

        public void close() {
            a();
        }

        public void flush() {
            a();
        }

        public void write(char[] cArr, int i, int i2) {
            for (int i3 = 0; i3 < i2; i3++) {
                char c = cArr[i + i3];
                if (c != '\n') {
                    this.a.append(c);
                } else {
                    a();
                }
            }
        }
    }

    /* compiled from: Unknown */
    private class m extends b {
        final /* synthetic */ f j;

        public m(f fVar, boolean z) {
            this.j = fVar;
            super(fVar, 8, 8, 8, 0, !z ? 0 : 16, 0);
        }
    }

    public f(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        a(context);
    }

    private void a() {
        if (this.d != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }

    private void a(Context context) {
        a = ((ActivityManager) context.getSystemService("activity")).getDeviceConfigurationInfo().reqGlEsVersion;
        setSurfaceTextureListener(this);
    }

    public void a(SurfaceTexture surfaceTexture) {
        com.fyusion.sdk.core.util.b.a(this.d != null);
        this.d.d();
    }

    public void a(SurfaceTexture surfaceTexture, int i, int i2, int i3) {
        com.fyusion.sdk.core.util.b.a(this.d != null);
        this.d.a(i2, i3);
    }

    public void a_() {
        com.fyusion.sdk.core.util.b.a(this.d != null);
        this.d.f();
    }

    public void b(SurfaceTexture surfaceTexture) {
        com.fyusion.sdk.core.util.b.a(this.d != null);
        this.d.e();
    }

    public void d() {
        com.fyusion.sdk.core.util.b.a(this.d != null);
        this.d.c();
    }

    public void f() {
        com.fyusion.sdk.core.util.b.a(this.d != null);
        this.d.g();
    }

    protected void finalize() throws Throwable {
        try {
            if (this.d != null) {
                this.d.h();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.f && this.e != null) {
            int b = this.d == null ? 1 : this.d.b();
            this.d = new i(this.c);
            if (b != 1) {
                this.d.a(b);
            }
            this.d.start();
        }
        this.f = false;
    }

    protected void onDetachedFromWindow() {
        if (this.d != null) {
            this.d.h();
        }
        this.f = true;
        super.onDetachedFromWindow();
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        a(getSurfaceTexture(), 0, i3 - i, i4 - i2);
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        a(surfaceTexture);
        a(surfaceTexture, 0, i, i2);
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        b(surfaceTexture);
        return true;
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        a(surfaceTexture, 0, i, i2);
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    public void setEGLContextClientVersion(int i) {
        a();
        this.l = i;
    }

    public void setRenderMode(int i) {
        com.fyusion.sdk.core.util.b.a(this.d != null);
        this.d.a(i);
    }

    public void setRenderer(Renderer renderer) {
        a();
        if (this.g == null) {
            this.g = new m(this, true);
        }
        if (this.h == null) {
            this.h = new c();
        }
        if (this.i == null) {
            this.i = new d();
        }
        this.e = renderer;
        this.d = new i(this.c);
        this.d.start();
    }
}
