package ucd.ui.framework.core;

import android.view.MotionEvent;
import java.util.ArrayList;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.Settings.GLObjectSettings.ViewConfig.GLObjectType;
import ucd.ui.framework.lib.GL;

public class Group extends ImageEx implements ParentImp {
    protected int canvasTexObj = 0;
    private int eventFlags;
    private boolean isDirty;
    protected ArrayList<GLObject> list = new ArrayList();

    public Group(GLBase root, int w, int h) {
        super(root, w, h);
    }

    protected void onInit(GLBaseSettings info) {
        this.canvasTexObj = info.env.texManager.createTex();
        info.env.texManager.setTextureParams(this.canvasTexObj, getMeasuredWidth(), getMeasuredHeight());
        super.onInit(info);
    }

    public int getChildrenCount() {
        return this.list != null ? this.list.size() : 0;
    }

    public void setDirty() {
        this.isDirty = true;
        setParentDirty();
    }

    public boolean isDirty() {
        return this.isDirty;
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

    public void delAll() {
        while (this.list.size() > 0) {
            GLObject item = (GLObject) this.list.get(0);
            this.list.remove(0);
            item.del();
        }
        setDirty();
        requestRender();
    }

    protected void drawTextureOnContext(GLBaseSettings info, int texId) {
        transModel(info);
        GLObjectType ttype = this.settings.viewcfg.type;
        this.settings.viewcfg.type = GLObjectType.Image;
        updateData(this.settings, info);
        Spirit.draw(this, this.vertexBufferPos, info.env.fboTexBufferPos, texId);
        this.settings.viewcfg.type = ttype;
    }

    protected void drawTextureOnParent() {
        GLObjectType selfType = this.settings.viewcfg.type;
        this.settings.viewcfg.type = GLObjectType.Image;
        if (this.context instanceof Group) {
            Group parent = this.context;
            this.root.settings.env.fbo.start(parent.canvasTexObj);
            this.root.changeScene(false, parent.getWidth(), parent.getHeight());
            drawTextureOnContext(this.info, this.canvasTexObj);
            this.root.settings.env.fbo.end();
        } else {
            this.root.changeScene(true, this.info.width, this.info.height);
            drawTextureOnContext(this.info, this.canvasTexObj);
        }
        this.settings.viewcfg.type = selfType;
    }

    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        this.root.settings.env.texManager.setTextureParams(this.canvasTexObj, width, height);
    }

    protected final void _drawOnlySelfOnCanvas(GLBaseSettings info) {
        float a = this.settings.viewcfg.alpha;
        setAlpha(1.0f);
        this.settings.GLObjectCfg.ml.pushMatrix();
        this.settings.GLObjectCfg.ml.setInitStack();
        float scale = GLBase.getScale(info);
        this.settings.GLObjectCfg.ml.scale(scale, scale, 1.0f);
        this.settings.GLObjectCfg.ml.translate(((float) getWidth()) / 2.0f, ((float) getHeight()) / 2.0f, 0.0f);
        if (this.needToUpdateVertices) {
            createVertices(info);
            vertexToGL(this.settings, info);
            this.needToUpdateVertices = false;
        }
        updateData(this.settings, info);
        Spirit.draw(this, this.vertexBufferPos, this.texBufferPos);
        this.settings.GLObjectCfg.ml.popMatrix();
        setAlpha(a);
    }

    protected void _onDraw(GLBaseSettings info) {
        if (isVisible()) {
            checkSizeChanged();
            if (checkPositionOutOfScreenChanged()) {
                if (isDirty()) {
                    this.root.settings.env.fbo.start(this.canvasTexObj);
                    this.root.changeScene(false, getWidth(), getHeight());
                    GL.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                    GL.glClear(17664);
                    GL.glUseProgram(this.shader);
                    _drawOnlySelfOnCanvas(info);
                    this.isDirty = false;
                    _drawChildren(info);
                    this.root.settings.env.fbo.end();
                }
                GL.glUseProgram(this.shader);
                drawTextureOnParent();
            }
        }
    }

    protected void _drawChildren(GLBaseSettings info) {
        for (int i = 0; i < this.list.size(); i++) {
            this.root.drawGLObject((GLObject) this.list.get(i), info);
        }
    }

    public void onDestroy() {
        queueEvent(new Runnable() {
            public void run() {
                Group.this.info.env.texManager.recycleTex(Group.this.canvasTexObj);
            }
        });
        for (int i = 0; i < this.list.size(); i++) {
            ((GLObject) this.list.get(i)).onDestroy();
        }
        super.onDestroy();
    }

    public GLObject findByPos(float[] pos, boolean findSmallest, boolean includeDisabledItems) {
        GLObject obj = ChildrenUtil._findByPos(this.list, pos, findSmallest, includeDisabledItems);
        if (obj == null) {
            return this;
        }
        return obj;
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
}
