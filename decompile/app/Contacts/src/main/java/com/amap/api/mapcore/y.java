package com.amap.api.mapcore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLDebugHelper;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint({"NewApi"})
/* compiled from: GLTextureView */
public class y extends TextureView implements SurfaceTextureListener {
    private static final j a = new j();
    private final WeakReference<y> b = new WeakReference(this);
    private i c;
    private Renderer d;
    private boolean e;
    private e f;
    private f g;
    private g h;
    private k i;
    private int j;
    private int k;
    private boolean l;

    /* compiled from: GLTextureView */
    public interface e {
        EGLConfig a(EGL10 egl10, EGLDisplay eGLDisplay);
    }

    /* compiled from: GLTextureView */
    private abstract class a implements e {
        protected int[] a;
        final /* synthetic */ y b;

        abstract EGLConfig a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);

        public a(y yVar, int[] iArr) {
            this.b = yVar;
            this.a = a(iArr);
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

        private int[] a(int[] iArr) {
            if (this.b.k != 2 && this.b.k != 3) {
                return iArr;
            }
            int length = iArr.length;
            Object obj = new int[(length + 2)];
            System.arraycopy(iArr, 0, obj, 0, length - 1);
            obj[length - 1] = 12352;
            if (this.b.k != 2) {
                obj[length] = 64;
            } else {
                obj[length] = 4;
            }
            obj[length + 1] = 12344;
            return obj;
        }
    }

    /* compiled from: GLTextureView */
    private class b extends a {
        protected int c;
        protected int d;
        protected int e;
        protected int f;
        protected int g;
        protected int h;
        final /* synthetic */ y i;
        private int[] j = new int[1];

        public b(y yVar, int i, int i2, int i3, int i4, int i5, int i6) {
            this.i = yVar;
            super(yVar, new int[]{12324, i, 12323, i2, 12322, i3, 12321, i4, 12325, i5, 12326, i6, 12344});
            this.c = i;
            this.d = i2;
            this.e = i3;
            this.f = i4;
            this.g = i5;
            this.h = i6;
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

        private int a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, int i, int i2) {
            if (egl10.eglGetConfigAttrib(eGLDisplay, eGLConfig, i, this.j)) {
                return this.j[0];
            }
            return i2;
        }
    }

    /* compiled from: GLTextureView */
    public interface f {
        EGLContext a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig);

        void a(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext);
    }

    /* compiled from: GLTextureView */
    private class c implements f {
        final /* synthetic */ y a;
        private int b;

        private c(y yVar) {
            this.a = yVar;
            this.b = 12440;
        }

        public EGLContext a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig) {
            int[] iArr = new int[]{this.b, this.a.k, 12344};
            EGLContext eGLContext = EGL10.EGL_NO_CONTEXT;
            if (this.a.k == 0) {
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

    /* compiled from: GLTextureView */
    public interface g {
        EGLSurface a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj);

        void a(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface);
    }

    /* compiled from: GLTextureView */
    private static class d implements g {
        private d() {
        }

        public EGLSurface a(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj) {
            EGLSurface eGLSurface = null;
            try {
                eGLSurface = egl10.eglCreateWindowSurface(eGLDisplay, eGLConfig, obj, null);
            } catch (Throwable e) {
                Log.e("GLSurfaceView", "eglCreateWindowSurface", e);
            }
            return eGLSurface;
        }

        public void a(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface) {
            egl10.eglDestroySurface(eGLDisplay, eGLSurface);
        }
    }

    /* compiled from: GLTextureView */
    private static class h {
        EGL10 a;
        EGLDisplay b;
        EGLSurface c;
        EGLConfig d;
        EGLContext e;
        private WeakReference<y> f;

        public h(WeakReference<y> weakReference) {
            this.f = weakReference;
        }

        public void a() {
            this.a = (EGL10) EGLContext.getEGL();
            this.b = this.a.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.b != EGL10.EGL_NO_DISPLAY) {
                if (this.a.eglInitialize(this.b, new int[2])) {
                    y yVar = (y) this.f.get();
                    if (yVar != null) {
                        this.d = yVar.f.a(this.a, this.b);
                        this.e = yVar.g.a(this.a, this.b, this.d);
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
                y yVar = (y) this.f.get();
                if (yVar == null) {
                    this.c = null;
                } else {
                    this.c = yVar.h.a(this.a, this.b, this.d, yVar.getSurfaceTexture());
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
            y yVar = (y) this.f.get();
            if (yVar == null) {
                return gl;
            }
            if (yVar.i != null) {
                gl = yVar.i.a(gl);
            }
            if ((yVar.j & 3) == 0) {
                return gl;
            }
            Writer writer;
            if ((yVar.j & 1) != 0) {
                i = 1;
            }
            if ((yVar.j & 2) == 0) {
                writer = null;
            } else {
                writer = new l();
            }
            return GLDebugHelper.wrap(gl, i, writer);
        }

        public int d() {
            if (this.a.eglSwapBuffers(this.b, this.c)) {
                return 12288;
            }
            return this.a.eglGetError();
        }

        public void e() {
            g();
        }

        private void g() {
            if (this.c != null && this.c != EGL10.EGL_NO_SURFACE) {
                this.a.eglMakeCurrent(this.b, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                y yVar = (y) this.f.get();
                if (yVar != null) {
                    yVar.h.a(this.a, this.b, this.c);
                }
                this.c = null;
            }
        }

        public void f() {
            if (this.e != null) {
                y yVar = (y) this.f.get();
                if (yVar != null) {
                    yVar.g.a(this.a, this.b, this.e);
                }
                this.e = null;
            }
            if (this.b != null) {
                this.a.eglTerminate(this.b);
                this.b = null;
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
            return str + " failed: " + i;
        }
    }

    /* compiled from: GLTextureView */
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
        private h s;
        private WeakReference<y> t;

        i(WeakReference<y> weakReference) {
            this.t = weakReference;
        }

        public void run() {
            setName("GLThread " + getId());
            try {
                l();
            } catch (InterruptedException e) {
            } finally {
                y.a.a(this);
            }
        }

        private void j() {
            if (this.i) {
                this.i = false;
                this.s.e();
            }
        }

        private void k() {
            if (this.h) {
                this.s.f();
                this.h = false;
                y.a.c(this);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void l() throws InterruptedException {
            this.s = new h(this.t);
            this.h = false;
            this.i = false;
            GL10 gl10 = null;
            Object obj = null;
            Object obj2 = null;
            Object obj3 = null;
            Object obj4 = null;
            Object obj5 = null;
            Runnable runnable = null;
            Object obj6 = null;
            int i = 0;
            Object obj7 = null;
            int i2 = 0;
            Object obj8 = null;
            while (true) {
                try {
                    synchronized (y.a) {
                        while (!this.a) {
                            Object obj9;
                            Object obj10;
                            int i3;
                            Object obj11;
                            int i4;
                            Runnable runnable2;
                            Object obj12;
                            if (this.q.isEmpty()) {
                                boolean z;
                                boolean z2;
                                if (this.d == this.c) {
                                    z = false;
                                } else {
                                    z2 = this.c;
                                    this.d = this.c;
                                    y.a.notifyAll();
                                    z = z2;
                                }
                                if (this.k) {
                                    j();
                                    k();
                                    this.k = false;
                                    obj3 = 1;
                                }
                                if (obj8 != null) {
                                    j();
                                    k();
                                    obj8 = null;
                                }
                                if (z && this.i) {
                                    j();
                                }
                                if (z && this.h) {
                                    y yVar = (y) this.t.get();
                                    if (yVar != null) {
                                        z2 = yVar.l;
                                    } else {
                                        z2 = false;
                                    }
                                    if (!z2 || y.a.a()) {
                                        k();
                                    }
                                }
                                if (z && y.a.b()) {
                                    this.s.f();
                                }
                                if (!(this.e || this.g)) {
                                    if (this.i) {
                                        j();
                                    }
                                    this.g = true;
                                    this.f = false;
                                    y.a.notifyAll();
                                }
                                if (this.e && this.g) {
                                    this.g = false;
                                    y.a.notifyAll();
                                }
                                if (obj5 != null) {
                                    obj = null;
                                    obj5 = null;
                                    this.p = true;
                                    y.a.notifyAll();
                                }
                                if (m()) {
                                    int i5;
                                    if (!this.h) {
                                        if (obj3 != null) {
                                            obj3 = null;
                                        } else if (y.a.b(this)) {
                                            this.s.a();
                                            this.h = true;
                                            obj2 = 1;
                                            y.a.notifyAll();
                                        }
                                    }
                                    if (this.h && !this.i) {
                                        this.i = true;
                                        obj4 = 1;
                                        i5 = 1;
                                        int i6 = 1;
                                    } else {
                                        obj9 = obj6;
                                        obj6 = obj7;
                                    }
                                    if (this.i) {
                                        if (this.r) {
                                            obj = 1;
                                            i2 = this.l;
                                            i5 = this.m;
                                            obj10 = 1;
                                            obj7 = 1;
                                            this.r = false;
                                        } else {
                                            obj7 = obj4;
                                            int i7 = i;
                                            obj10 = obj;
                                            obj = obj9;
                                            i5 = i2;
                                            i2 = i7;
                                        }
                                        this.o = false;
                                        y.a.notifyAll();
                                        obj4 = obj3;
                                        obj3 = obj2;
                                        obj2 = obj10;
                                        obj10 = obj;
                                        Object obj13 = obj5;
                                        obj5 = obj7;
                                        i3 = i2;
                                        obj11 = obj8;
                                        i4 = i5;
                                        obj9 = obj6;
                                        runnable2 = runnable;
                                        obj12 = obj13;
                                    } else {
                                        obj7 = obj6;
                                        obj6 = obj9;
                                    }
                                }
                                y.a.wait();
                            } else {
                                obj12 = obj5;
                                obj5 = obj4;
                                obj4 = obj3;
                                obj3 = obj2;
                                obj2 = obj;
                                obj9 = obj7;
                                i3 = i;
                                obj10 = obj6;
                                runnable2 = (Runnable) this.q.remove(0);
                                int i8 = i2;
                                obj11 = obj8;
                                i4 = i8;
                            }
                        }
                        synchronized (y.a) {
                            j();
                            k();
                        }
                        return;
                    }
                } catch (RuntimeException e) {
                    y.a.c(this);
                    throw e;
                } catch (Throwable th) {
                    synchronized (y.a) {
                        j();
                        k();
                    }
                }
            }
        }

        public boolean a() {
            return this.h && this.i && m();
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
                synchronized (y.a) {
                    this.n = i;
                    y.a.notifyAll();
                }
                return;
            }
            throw new IllegalArgumentException("renderMode");
        }

        public int b() {
            int i;
            synchronized (y.a) {
                i = this.n;
            }
            return i;
        }

        public void c() {
            synchronized (y.a) {
                this.o = true;
                y.a.notifyAll();
            }
        }

        public void d() {
            synchronized (y.a) {
                this.e = true;
                this.j = false;
                y.a.notifyAll();
                while (this.g && !this.j && !this.b) {
                    try {
                        y.a.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void e() {
            synchronized (y.a) {
                this.e = false;
                y.a.notifyAll();
                while (!this.g && !this.b) {
                    try {
                        y.a.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void f() {
            synchronized (y.a) {
                this.c = true;
                y.a.notifyAll();
                while (!this.b && !this.d) {
                    try {
                        y.a.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void g() {
            synchronized (y.a) {
                this.c = false;
                this.o = true;
                this.p = false;
                y.a.notifyAll();
                while (!this.b && this.d && !this.p) {
                    try {
                        y.a.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void a(int i, int i2) {
            synchronized (y.a) {
                this.l = i;
                this.m = i2;
                this.r = true;
                this.o = true;
                this.p = false;
                y.a.notifyAll();
                while (!this.b && !this.d && !this.p && a()) {
                    try {
                        y.a.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void h() {
            synchronized (y.a) {
                this.a = true;
                y.a.notifyAll();
                while (!this.b) {
                    try {
                        y.a.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void i() {
            this.k = true;
            y.a.notifyAll();
        }

        public void a(Runnable runnable) {
            if (runnable != null) {
                synchronized (y.a) {
                    this.q.add(runnable);
                    y.a.notifyAll();
                }
                return;
            }
            throw new IllegalArgumentException("r must not be null");
        }
    }

    /* compiled from: GLTextureView */
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

        public synchronized void a(i iVar) {
            iVar.b = true;
            if (this.g == iVar) {
                this.g = null;
            }
            notifyAll();
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

        public synchronized void a(GL10 gl10) {
            boolean z = false;
            synchronized (this) {
                if (!this.d) {
                    c();
                    String glGetString = gl10.glGetString(7937);
                    if (this.c < 131072) {
                        boolean z2;
                        if (glGetString.startsWith("Q3Dimension MSM7500 ")) {
                            z2 = false;
                        } else {
                            z2 = true;
                        }
                        this.e = z2;
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

        private void c() {
            if (!this.b) {
                this.c = 131072;
                if (this.c >= 131072) {
                    this.e = true;
                }
                this.b = true;
            }
        }
    }

    /* compiled from: GLTextureView */
    public interface k {
        GL a(GL gl);
    }

    /* compiled from: GLTextureView */
    static class l extends Writer {
        private StringBuilder a = new StringBuilder();

        l() {
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

        private void a() {
            if (this.a.length() > 0) {
                Log.v("GLSurfaceView", this.a.toString());
                this.a.delete(0, this.a.length());
            }
        }
    }

    /* compiled from: GLTextureView */
    private class m extends b {
        final /* synthetic */ y j;

        public m(y yVar, boolean z) {
            int i;
            this.j = yVar;
            if (z) {
                i = 16;
            } else {
                i = 0;
            }
            super(yVar, 8, 8, 8, 0, i, 0);
        }
    }

    public y(Context context) {
        super(context);
        a();
    }

    public y(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        a();
    }

    protected void finalize() throws Throwable {
        try {
            if (this.c != null) {
                this.c.h();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void a() {
        setSurfaceTextureListener(this);
    }

    public void setRenderer(Renderer renderer) {
        e();
        if (this.f == null) {
            this.f = new m(this, true);
        }
        if (this.g == null) {
            this.g = new c();
        }
        if (this.h == null) {
            this.h = new d();
        }
        this.d = renderer;
        this.c = new i(this.b);
        this.c.start();
    }

    public void setRenderMode(int i) {
        this.c.a(i);
    }

    public void requestRender() {
        this.c.c();
    }

    public void b() {
        this.c.f();
    }

    public void c() {
        this.c.g();
    }

    public void queueEvent(Runnable runnable) {
        this.c.a(runnable);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.e && this.d != null) {
            int i;
            if (this.c == null) {
                i = 1;
            } else {
                i = this.c.b();
            }
            this.c = new i(this.b);
            if (i != 1) {
                this.c.a(i);
            }
            this.c.start();
        }
        this.e = false;
    }

    protected void onDetachedFromWindow() {
        if (this.c != null) {
            this.c.h();
        }
        this.e = true;
        super.onDetachedFromWindow();
    }

    private void e() {
        if (this.c != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        this.c.d();
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        this.c.e();
        return true;
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        this.c.a(i, i2);
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        onSurfaceTextureSizeChanged(getSurfaceTexture(), i3 - i, i4 - i2);
        super.onLayout(z, i, i2, i3, i4);
    }
}
