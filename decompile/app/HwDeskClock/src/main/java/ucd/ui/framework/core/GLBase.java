package ucd.ui.framework.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import java.util.ArrayList;
import java.util.Stack;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.coreEx.video.MediaManager;
import ucd.ui.framework.lib.GL;
import ucd.ui.util.CSSManager;
import ucd.ui.util.DensityUtil;

public class GLBase extends GLSurfaceView implements Renderer, ParentImp {
    private static /* synthetic */ int[] $SWITCH_TABLE$ucd$ui$framework$core$GLBase$FPS;
    private static Handler handler = new Handler();
    private int FPS = 60;
    protected ThreadAnimatorManager animManager;
    private float[] clearColor = new float[4];
    private Context context;
    private int counter = 0;
    private int curViewportHeight;
    private int curViewportWidth;
    private float cx;
    private float cy;
    private float cz = 0.0f;
    public int drawCounter = 0;
    private int eventFlags;
    private boolean flag;
    private GLObject focusedChild;
    private FPSListener fpsListener;
    private long lastDrawTime;
    protected ArrayList<GLObject> list = new ArrayList();
    protected MediaManager mediaManager;
    protected OnTouchListener onTouchListener;
    private boolean renderAuto = false;
    private final Runnable requestRenderRunnable = new Runnable() {
        public void run() {
            GLBase.this.requestRender();
        }
    };
    protected GLBaseSettings settings = new GLBaseSettings();
    private Stack<Scene> stack = new Stack();
    private long startTime = System.nanoTime();
    protected long timeline;
    private TouchEventManager tm = new TouchEventManager();
    protected boolean transTexlock;

    public enum FPS {
        Low,
        Medium,
        High
    }

    public interface FPSListener {
        void onFPS(long j);
    }

    static /* synthetic */ int[] $SWITCH_TABLE$ucd$ui$framework$core$GLBase$FPS() {
        int[] iArr = $SWITCH_TABLE$ucd$ui$framework$core$GLBase$FPS;
        if (iArr != null) {
            return iArr;
        }
        iArr = new int[FPS.values().length];
        try {
            iArr[FPS.High.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[FPS.Low.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[FPS.Medium.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        $SWITCH_TABLE$ucd$ui$framework$core$GLBase$FPS = iArr;
        return iArr;
    }

    public GLBase(Context context) {
        super(context);
        init(context);
        this.animManager = new ThreadAnimatorManager(this);
        this.animManager.start(this);
        this.mediaManager = new MediaManager(context);
    }

    private void init(Context c) {
        this.context = c;
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        setZOrderOnTop(true);
        getHolder().setFormat(1);
        setZs(this.settings);
        setRenderer(this);
        setRenderMode(0);
        this.renderAuto = false;
        setClickable(true);
        CSSManager.read(this.context, "css/text.xml");
    }

    public void setBackgroundColor(int color) {
        this.clearColor[0] = ((float) Color.red(color)) / 255.0f;
        this.clearColor[1] = ((float) Color.green(color)) / 255.0f;
        this.clearColor[2] = ((float) Color.blue(color)) / 255.0f;
        this.clearColor[3] = ((float) Color.alpha(color)) / 255.0f;
        requestRender();
    }

    @Deprecated
    public void setBackground(Drawable background) {
    }

    @Deprecated
    public void setBackgroundResource(int resid) {
    }

    public final synchronized boolean dispatchTouchEvent(MotionEvent event) {
        return this.tm.dispatchTouchEvent(this, event);
    }

    public final void setOnTouchListener(OnTouchListener v) {
        this.onTouchListener = v;
    }

    private void setZs(GLBaseSettings settings) {
        settings.config.nearZ = settings.config.cameraZ - settings.config.near;
        settings.config.farZ = settings.config.cameraZ - settings.config.far;
        settings.config.baseZ = (settings.config.nearZ + settings.config.farZ) * 0.5f;
        settings.config.scale = getScale(settings);
    }

    public static float getScale(GLBaseSettings settings) {
        return getScale(settings.config.near, settings.config.cameraZ, settings.config.baseZ);
    }

    public static float getScale(float near, float cameraZ, float baseZ) {
        if (near == 0.0f) {
            return 1.0f;
        }
        return (cameraZ - baseZ) / near;
    }

    public void add(GLObject glObj) {
        add(glObj, this.list.size());
    }

    public void add(GLObject glObj, int index) {
        ChildrenUtil.add(this, glObj, index);
    }

    public void del(GLObject glObj) {
        ChildrenUtil.del(this, glObj, true);
    }

    protected void onDetachedFromWindow() {
        onDestroy();
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (this.animManager != null) {
            this.animManager.exit();
        }
        for (int i = 0; i < this.list.size(); i++) {
            try {
                GLObject obj = (GLObject) this.list.get(i);
                if (obj != null) {
                    obj.onDestroy();
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }
        this.list.clear();
        requestRender();
        handler.removeCallbacks(this.requestRenderRunnable);
    }

    private void logFPS() {
        if (this.fpsListener != null) {
            int i;
            this.counter++;
            if (System.nanoTime() - this.startTime < 1000000000) {
                i = 1;
            } else {
                i = 0;
            }
            if (i == 0) {
                this.fpsListener.onFPS((long) this.counter);
                this.counter = 0;
                this.startTime = System.nanoTime();
            }
        }
    }

    public void setRenderAuto(boolean flag) {
        this.renderAuto = flag;
        if (flag) {
            setRenderMode(1);
        } else {
            setRenderMode(0);
        }
    }

    public void requestRender() {
        Object obj = null;
        if (!this.renderAuto) {
            if (this.FPS != 60) {
                long delta = (long) Math.ceil((double) (1000.0f / ((float) this.FPS)));
                if (this.timeline - this.lastDrawTime < delta) {
                    obj = 1;
                }
                if (obj == null) {
                    this.lastDrawTime = this.timeline;
                    super.requestRender();
                } else {
                    handler.removeCallbacks(this.requestRenderRunnable);
                    handler.postDelayed(this.requestRenderRunnable, delta);
                }
            } else {
                super.requestRender();
            }
        }
    }

    protected int getFPS() {
        return !this.renderAuto ? this.FPS : 60;
    }

    public void setFPS(FPS v) {
        if (v != null) {
            switch ($SWITCH_TABLE$ucd$ui$framework$core$GLBase$FPS()[v.ordinal()]) {
                case 1:
                    this.FPS = 30;
                    break;
                case 2:
                    this.FPS = 45;
                    break;
                case 3:
                    this.FPS = 60;
                    break;
            }
        }
    }

    public void drawGLObject(GLObject item, GLBaseSettings settings) {
        if (item != null) {
            if (!item.settings.hasInit) {
                item.settings.hasInit = true;
                item.initShader(this.context);
                item.onInit(settings);
            }
            item._onBeforeDraw(settings);
            item._onDraw(settings);
        }
    }

    public void changeScene(boolean willBeEffectedByCameraPos, int w, int h) {
        if (!(w == 0 || h == 0)) {
            if (this.flag) {
                if (!willBeEffectedByCameraPos) {
                }
            }
            this.flag = willBeEffectedByCameraPos;
            this.curViewportWidth = w;
            this.curViewportHeight = h;
            GL.glViewport(0, 0, w, h);
            float scale = getScale(this.settings);
            this.settings.pm.setProjectFrustum(((float) (-w)) / 2.0f, ((float) w) / 2.0f, ((float) h) / 2.0f, ((float) (-h)) / 2.0f, this.settings.config.near, this.settings.config.far);
            if (willBeEffectedByCameraPos) {
                this.settings.pm.setCamera(((((float) w) / 2.0f) + this.cx) * scale, ((((float) h) / 2.0f) + this.cy) * scale, this.settings.config.cameraZ + this.cz, -1.0f, 0.0f, 1.0f, 0.0f);
            } else {
                this.settings.pm.setCamera((((float) w) / 2.0f) * scale, (((float) h) / 2.0f) * scale, this.settings.config.cameraZ, -1.0f, 0.0f, 1.0f, 0.0f);
            }
        }
    }

    public synchronized void onDrawFrame(GL10 arg0) {
        GL.glClearColor(this.clearColor[0], this.clearColor[1], this.clearColor[2], this.clearColor[3]);
        GL.glClear(17664);
        logFPS();
        this.drawCounter = 0;
        this.transTexlock = false;
        this.timeline = SystemClock.elapsedRealtime();
        for (int i = 0; i < this.list.size(); i++) {
            try {
                drawGLObject((GLObject) this.list.get(i), this.settings);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void onSurfaceChanged(GL10 arg0, int w, int h) {
        if (DensityUtil.getScreenSize() == null) {
            this.settings.width = w;
            this.settings.height = h;
        } else {
            this.settings.width = (int) (((double) DensityUtil.getScreenSize()[0]) + 0.5d);
            this.settings.height = (int) (((double) DensityUtil.getScreenSize()[1]) + 0.5d);
        }
        changeScene(false, this.settings.width, this.settings.height);
        this.settings.env.fbo.setDepth(this.settings.width, this.settings.height);
    }

    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        GL.glFrontFace(2305);
        GL.glEnable(2884);
        GL.glEnable(2929);
        GL.glDepthFunc(515);
        GL.glEnable(3042);
        this.settings.initGLEnv(this.context);
    }

    protected final void rememberFocus(GLObject glObj) {
        this.focusedChild = glObj;
    }

    public final GLObject getFocusedChild() {
        return this.focusedChild;
    }

    public int getChildrenCount() {
        return this.list.size();
    }

    public boolean interceptTouchEvent(MotionEvent event) {
        return false;
    }

    public final boolean getDisAllowInterceptTouchEvent() {
        return (this.eventFlags & 524288) != 0;
    }

    public final void resetTouchState() {
        this.eventFlags &= -524289;
    }

    public void onPause() {
        super.onPause();
    }
}
