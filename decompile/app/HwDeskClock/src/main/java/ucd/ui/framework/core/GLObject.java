package ucd.ui.framework.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.Settings.GLObjectSettings;
import ucd.ui.framework.Settings.GLObjectSettings.ViewConfig;
import ucd.ui.framework.Settings.GLObjectSettings.ViewConfig.GLObjectType;
import ucd.ui.framework.lib.GL;
import ucd.ui.framework.lib.Model;

public class GLObject {
    private static int[] vertexBuffersTmp = new int[1];
    protected String TAG = getClass().getSimpleName();
    protected ParentImp context;
    private OnFocusChangedListener focusListener;
    protected GLBaseSettings info;
    private boolean isInParent = true;
    private int measuredHeight;
    private int measuredWidth;
    protected boolean needToTransModel = true;
    protected boolean needToUpdateVertices = true;
    private OnClickListener onClickListener;
    protected OnTouchListener onTouchListener;
    private RectF rectParent = new RectF();
    private RectF rectSelf = new RectF();
    protected GLBase root;
    private ArrayList<Runnable> runOnce = new ArrayList();
    protected GLObjectSettings settings = new GLObjectSettings();
    protected int shader = 0;
    protected int texBufferPos = 0;
    private TouchEventManager tm = new TouchEventManager();
    private final float[] tmp_newPos = new float[2];
    protected int vertexBufferPos = 0;

    public interface OnClickListener {
        void onClick(GLObject gLObject);
    }

    public interface OnFocusChangedListener {
        void onFocusChanged(GLObject gLObject, GLObject gLObject2, boolean z);
    }

    public interface OnTouchListener {
        boolean onTouch(MotionEvent motionEvent);
    }

    public GLObject(GLBase root, int w, int h) {
        this.root = root;
        this.settings.viewcfg.radius = 0.0f;
        this.settings.viewcfg.pos.setWidth((float) w);
        this.settings.viewcfg.pos.setHeight((float) h);
        this.measuredWidth = w;
        this.measuredHeight = h;
        this.info = root.settings;
    }

    public final void setParent(ParentImp v) {
        this.context = v;
    }

    public final GLObjectType getType() {
        return this.settings.viewcfg.type;
    }

    protected void createShader(Context c) {
        this.shader = this.root.settings.env.shaderManager.getDefaultShader();
    }

    private void getDefaultHandles(int shader) {
        this.settings.GLObjectCfg.vertexPositionAttribute = GL.glGetAttribLocation(shader, "aVertexPosition");
        this.settings.GLObjectCfg.mMVPMatrix = GL.glGetUniformLocation(shader, "MVPMatrix");
        this.settings.GLObjectCfg.pos4fHandle = GL.glGetUniformLocation(shader, "agRect");
        this.settings.GLObjectCfg.paintColor_handle = GL.glGetUniformLocation(shader, "paintColor");
        this.settings.GLObjectCfg.attrsHandle = GL.glGetUniformLocation(shader, "attrs");
    }

    protected void getHandles(int shader) {
    }

    protected void initShader(Context c) {
        int[] sAttrs = this.root.settings.env.shaderManager.sAttrs;
        createShader(c);
        if (this.shader != this.root.settings.env.shaderManager.getDefaultShader()) {
            getDefaultHandles(this.shader);
        } else {
            this.settings.GLObjectCfg.vertexPositionAttribute = sAttrs[0];
            this.settings.GLObjectCfg.mMVPMatrix = sAttrs[1];
            this.settings.GLObjectCfg.pos4fHandle = sAttrs[2];
            this.settings.GLObjectCfg.paintColor_handle = sAttrs[3];
            this.settings.GLObjectCfg.attrsHandle = sAttrs[4];
        }
        getHandles(this.shader);
    }

    protected void onInit(GLBaseSettings info) {
        GL.glGenBuffers(1, vertexBuffersTmp, 0);
        this.vertexBufferPos = vertexBuffersTmp[0];
    }

    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
    }

    protected void createVertices(GLBaseSettings info) {
        float z = fixZ(this.settings.viewcfg.tz);
        this.settings.GLObjectCfg.vertices = Model.planeModel((float) getWidth(), (float) getHeight(), info.config.baseZ + z, this.settings.GLObjectCfg.vertices);
    }

    protected final void addOnceAction(Runnable r) {
        if (r != null && !this.runOnce.contains(r)) {
            this.runOnce.add(r);
        }
    }

    protected void updateData(GLObjectSettings settings, GLBaseSettings info) {
        if (settings.GLObjectCfg.attrsHandle != -1) {
            GL.glUniform4f(settings.GLObjectCfg.attrsHandle, (float) getType().ordinal(), (float) settings.viewcfg.mixType, settings.viewcfg.alpha, settings.viewcfg.rz);
        }
        if (settings.GLObjectCfg.pos4fHandle != -1) {
            float w = (float) getWidth();
            float h = (float) getHeight();
            float max = w;
            if (h > w) {
                max = h;
            }
            if (max == 0.0f) {
                max = 1.0f;
            }
            GL.glUniform4f(settings.GLObjectCfg.pos4fHandle, max, settings.viewcfg.radius, w, h);
        }
        float[] bgColor = settings.viewcfg.paintColor;
        if (bgColor == null) {
            bgColor = ViewConfig.getTransparent();
        }
        if (bgColor != null) {
            GL.glUniform4fv(settings.GLObjectCfg.paintColor_handle, 1, bgColor, 0);
        }
    }

    protected final void checkSizeChanged() {
        int oldW = getWidth();
        int oldH = getHeight();
        if (oldW != this.measuredWidth || oldH != this.measuredHeight) {
            this.settings.viewcfg.pos.setWidth((float) this.measuredWidth);
            this.settings.viewcfg.pos.setHeight((float) this.measuredHeight);
            onSizeChanged(this.measuredWidth, this.measuredHeight, oldW, oldH);
            this.needToUpdateVertices = true;
            setDirty();
        }
    }

    protected final void _onBeforeDraw(GLBaseSettings info) {
        if (this.runOnce.size() > 0) {
            if (this.root.transTexlock) {
                setDirty();
                requestRender();
                return;
            }
            this.root.transTexlock = true;
            Runnable r = (Runnable) this.runOnce.remove(0);
            if (r != null) {
                r.run();
            }
        }
    }

    protected void onPositionOutOfParent(boolean moveInto) {
    }

    protected final boolean checkPositionOutOfScreenChanged() {
        if (this.context == null) {
            return false;
        }
        if (this.settings.viewcfg.rz == 0.0f) {
            this.rectSelf.set(getX(), getY(), getX() + ((float) getMeasuredWidth()), getY() + ((float) getMeasuredHeight()));
        } else {
            float rotateZ = (float) Math.toRadians((double) this.settings.viewcfg.rz);
            float halfOfWidth = ((float) getMeasuredWidth()) / 2.0f;
            float halfOfHeight = ((float) getMeasuredHeight()) / 2.0f;
            float angle = (float) Math.atan((double) (halfOfHeight / halfOfWidth));
            float centerX = getX() + halfOfWidth;
            float centerY = getY() + halfOfHeight;
            float halfR = (float) Math.sqrt(Math.pow((double) halfOfWidth, 2.0d) + Math.pow((double) halfOfHeight, 2.0d));
            double cosRotateZ = Math.cos((double) rotateZ);
            double sinRotateZ = Math.sin((double) rotateZ);
            double cosAngle = Math.cos((double) angle);
            double sinAngle = Math.sin((double) angle);
            double cos2Angle = 1.0d - (Math.pow(sinAngle, 2.0d) * 2.0d);
            double sin2Angle = (2.0d * sinAngle) * cosAngle;
            float pointLTx = centerX - (((float) ((cosRotateZ * cos2Angle) + (sinRotateZ * sin2Angle))) * halfR);
            float pointLTy = centerY - (((float) ((sinRotateZ * cos2Angle) - (cosRotateZ * sin2Angle))) * halfR);
            float pointRTx = centerX + (((float) Math.cos((double) rotateZ)) * halfR);
            float pointRTy = centerY + (((float) Math.sin((double) rotateZ)) * halfR);
            float pointLBx = centerX - (((float) Math.cos((double) rotateZ)) * halfR);
            float pointLBy = centerY - (((float) Math.sin((double) rotateZ)) * halfR);
            float pointRBx = centerX + (((float) ((cosRotateZ * cos2Angle) - (sinRotateZ * sin2Angle))) * halfR);
            float pointRBy = centerY + (((float) ((sinRotateZ * cos2Angle) - (cosRotateZ * sin2Angle))) * halfR);
            this.rectSelf.set(Math.min(pointLTx, Math.min(pointRTx, Math.min(pointLBx, pointRBx))), Math.min(pointLTy, Math.min(pointRTy, Math.min(pointLBy, pointRBy))), Math.max(pointLTx, Math.max(pointRTx, Math.max(pointLBx, pointRBx))), Math.max(pointLTy, Math.max(pointRTy, Math.max(pointLBy, pointRBy))));
        }
        if (this.context instanceof Group) {
            Group p = (Group) this.context;
            this.rectParent.set(0.0f, 0.0f, (float) p.getMeasuredWidth(), (float) p.getMeasuredHeight());
        } else if (this.context instanceof GLBase) {
            this.rectParent.set(0.0f, 0.0f, (float) this.info.width, (float) this.info.height);
        }
        if (this.rectParent.intersect(this.rectSelf)) {
            if (!this.isInParent) {
                this.isInParent = true;
                onPositionOutOfParent(true);
            }
            return true;
        }
        if (this.isInParent) {
            this.isInParent = false;
            onPositionOutOfParent(false);
        }
        return false;
    }

    protected void _onDraw(GLBaseSettings info) {
        if (isVisible() && this.root != null && this.context != null) {
            checkSizeChanged();
            if (checkPositionOutOfScreenChanged()) {
                if (this.needToUpdateVertices) {
                    createVertices(info);
                    vertexToGL(this.settings, info);
                    this.needToUpdateVertices = false;
                }
                if (this.context instanceof GLBase) {
                    this.root.changeScene(true, info.width, info.height);
                } else {
                    Group tmpGroup = this.context;
                    if (tmpGroup != null) {
                        this.root.changeScene(false, tmpGroup.getWidth(), tmpGroup.getHeight());
                    }
                }
                transModel(info);
                GL.glUseProgram(this.shader);
                updateData(this.settings, info);
                Spirit.draw(this, this.vertexBufferPos, this.texBufferPos);
            }
        }
    }

    public boolean isVisible() {
        return this.settings.viewcfg.alpha != 0.0f;
    }

    protected void transModel(GLBaseSettings info) {
        if (this.needToTransModel) {
            this.needToTransModel = false;
            this.settings.GLObjectCfg.ml.setInitStack();
            float scale = GLBase.getScale(info);
            this.settings.GLObjectCfg.ml.scale(this.settings.viewcfg.scale * scale, this.settings.viewcfg.scale * scale, 1.0f);
            if (this.settings.viewcfg.scale != 0.0f) {
                this.settings.GLObjectCfg.ml.translate((getX() + (((float) getWidth()) / 2.0f)) / this.settings.viewcfg.scale, (getY() + (((float) getHeight()) / 2.0f)) / this.settings.viewcfg.scale, this.settings.viewcfg.tz);
                if (this.settings.viewcfg.rx != 0.0f) {
                    this.settings.GLObjectCfg.ml.rotate(this.settings.viewcfg.rx, 1.0f, 0.0f, 0.0f);
                }
                if (this.settings.viewcfg.ry != 0.0f) {
                    this.settings.GLObjectCfg.ml.rotate(this.settings.viewcfg.ry, 0.0f, 1.0f, 0.0f);
                }
                if (this.settings.viewcfg.rz != 0.0f) {
                    this.settings.GLObjectCfg.ml.rotate(this.settings.viewcfg.rz, 0.0f, 0.0f, 1.0f);
                }
            } else {
                this.settings.GLObjectCfg.ml.translate(0.0f, 0.0f, this.settings.viewcfg.tz);
            }
        }
    }

    protected void setPaintColor(float r, float g, float b, float a) {
        if (this.settings.viewcfg.paintColor == null) {
            this.settings.viewcfg.paintColor = new float[4];
        }
        if (a == 0.0f) {
            b = 0.0f;
            g = 0.0f;
            r = 0.0f;
        }
        this.settings.viewcfg.paintColor[0] = r;
        this.settings.viewcfg.paintColor[1] = g;
        this.settings.viewcfg.paintColor[2] = b;
        this.settings.viewcfg.paintColor[3] = a;
        this.settings.viewcfg.type = GLObjectType.Color;
        setDirty();
    }

    public void setPaintColor(int hex) {
        setPaintColor(((float) Color.red(hex)) / 255.0f, ((float) Color.green(hex)) / 255.0f, ((float) Color.blue(hex)) / 255.0f, ((float) Color.alpha(hex)) / 255.0f);
    }

    protected void setDirty() {
        setParentDirty();
    }

    public void setParentDirty() {
        if (this.context instanceof Group) {
            ((Group) this.context).setDirty();
        }
    }

    public ParentImp getParent() {
        return this.context;
    }

    public void scale(float v) {
        this.settings.viewcfg.scale = v;
        this.needToTransModel = true;
        setParentDirty();
    }

    public void setLocation(float x, float y) {
        this.settings.viewcfg.pos.left = x;
        this.settings.viewcfg.pos.top = y;
        this.needToTransModel = true;
        setParentDirty();
    }

    public float getZ() {
        return this.settings.viewcfg.tz;
    }

    public void rotate(float x, float y, float z) {
        this.settings.viewcfg.rx = x;
        this.settings.viewcfg.ry = y;
        this.settings.viewcfg.rz = z;
        this.needToTransModel = true;
        setParentDirty();
    }

    public void setLayoutParams(int w, int h) {
        if (w < 0) {
            w = 0;
        }
        if (h < 0) {
            h = 0;
        }
        this.measuredWidth = w;
        this.measuredHeight = h;
        setDirty();
    }

    public void setAlpha(float v) {
        this.settings.viewcfg.alpha = Math.max(0.0f, Math.min(1.0f, v));
        setParentDirty();
    }

    public void setRadius(float v) {
        this.settings.viewcfg.radius = v;
        setParentDirty();
    }

    public float getX() {
        return this.settings.viewcfg.pos.left;
    }

    public float getY() {
        return this.settings.viewcfg.pos.top;
    }

    public final int getWidth() {
        return (int) this.settings.viewcfg.pos.getWidth();
    }

    public final int getMeasuredWidth() {
        return this.measuredWidth;
    }

    public final int getHeight() {
        return (int) this.settings.viewcfg.pos.getHeight();
    }

    public int getMeasuredHeight() {
        return this.measuredHeight;
    }

    protected final void vertexToGL(GLObjectSettings settings, GLBaseSettings info) {
        if (settings.GLObjectCfg.vertices == null || settings.GLObjectCfg.vertices.length == 0 || this.vertexBufferPos == 0) {
            show("vertices or vertexBufferPos is invalid.");
            return;
        }
        settings.GLObjectCfg.numItems = settings.GLObjectCfg.vertices.length / settings.GLObjectCfg.itemSize;
        Spirit.setBuffer(this.vertexBufferPos, settings.GLObjectCfg.vertexPositionAttribute, settings.GLObjectCfg.itemSize, settings.GLObjectCfg.vertices);
    }

    public void show(String str) {
        System.out.println(this.TAG + ", " + str);
    }

    public void onDestroy() {
        this.context = null;
    }

    public void del() {
        if (!(this.context == null || this.root == null || !equals(this.root.getFocusedChild()))) {
            handleFocusChanged(null, this, false);
            if (this.focusListener != null) {
                this.focusListener.onFocusChanged(this, null, false);
            }
            this.root.rememberFocus(null);
        }
        ParentImp p = this.context;
        if (this.context != null) {
            onDestroy();
            p.del(this);
        }
    }

    public void requestRender() {
        if (this.root != null) {
            this.root.requestRender();
        }
    }

    public float fixZ(float z) {
        ParentImp parent = this.context;
        while (parent != null && (parent instanceof Group)) {
            z += ((Group) parent).settings.viewcfg.tz;
            parent = ((GLObject) parent).context;
        }
        return z;
    }

    public void getLocationInWindow(float[] newLocation) {
        float x = getX();
        float y = getY();
        ParentImp parent = this.context;
        while (parent != null && (parent instanceof Group)) {
            x += ((Group) parent).getX();
            y += ((Group) parent).getY();
            parent = ((GLObject) parent).context;
        }
        if (newLocation != null && newLocation.length == 2) {
            newLocation[0] = x;
            newLocation[1] = y;
            return;
        }
        show("bad param: newLocation");
    }

    private static void pointRotateBack(float[] pos, float cx, float cy, float rotateAngle) {
        float x = pos[0];
        float y = pos[1];
        if (((double) rotateAngle) != 0.0d) {
            double r = Math.toRadians((double) rotateAngle);
            float y1 = ((float) ((((double) (x - cx)) * Math.sin(r)) + (((double) (y - cy)) * Math.cos(r)))) + cy;
            pos[0] = ((float) ((((double) (x - cx)) * Math.cos(r)) - (((double) (y - cy)) * Math.sin(r)))) + cx;
            pos[1] = y1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean inRect(float[] pos, boolean iterationInParents) {
        if (pos == null || pos.length != 2) {
            return false;
        }
        boolean flag = true;
        getLocationInWindow(this.tmp_newPos);
        if (iterationInParents) {
            LinkedList<Group> path = new LinkedList();
            ParentImp obj = getParent();
            while (obj instanceof Group) {
                Group curPath = (Group) obj;
                path.add(curPath);
                obj = curPath.getParent();
            }
            for (int pathSteps = path.size(); pathSteps > 0 && flag; pathSteps--) {
                flag = ((Group) path.pop()).inRect(pos, false);
            }
            if (flag) {
                flag = inRect(pos, false);
            }
            path.clear();
        } else {
            int i;
            boolean z;
            pointRotateBack(pos, this.tmp_newPos[0], this.tmp_newPos[1], -this.settings.viewcfg.rz);
            if (pos[0] < this.tmp_newPos[0]) {
                i = 1;
            } else {
                z = false;
            }
            if (i == 0) {
                if (pos[0] > this.tmp_newPos[0] + ((float) getWidth())) {
                    i = 1;
                } else {
                    z = false;
                }
                if (i == 0) {
                    if (pos[1] < this.tmp_newPos[1]) {
                        i = 1;
                    } else {
                        z = false;
                    }
                    if (i == 0) {
                    }
                }
            }
            flag = false;
        }
        return flag;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean dispatchTouchEvent;
        synchronized (event) {
            dispatchTouchEvent = this.tm.dispatchTouchEvent(this, event);
        }
        return dispatchTouchEvent;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return isClickable();
        }
        if (!isClickable()) {
            return false;
        }
        if (event.getAction() == 1) {
            performClick();
        }
        return true;
    }

    public boolean performClick() {
        if (this.onClickListener == null) {
            return false;
        }
        this.onClickListener.onClick(this);
        return true;
    }

    public final void queueEvent(Runnable r) {
        if (r != null && this.root != null) {
            this.root.queueEvent(r);
        }
    }

    protected void handleFocusChanged(GLObject current, GLObject old, boolean hasFocus) {
    }

    public final boolean isEnabled() {
        return this.settings.viewcfg.enabled;
    }

    public boolean isClickable() {
        return this.settings.viewcfg.clickable;
    }
}
