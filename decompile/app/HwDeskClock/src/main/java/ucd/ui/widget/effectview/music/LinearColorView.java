package ucd.ui.widget.effectview.music;

import android.content.Context;
import android.graphics.Color;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.Settings.GLObjectSettings;
import ucd.ui.framework.core.BlendManager.BlendType;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.GLObject;
import ucd.ui.framework.core.Group;
import ucd.ui.framework.lib.GL;

public class LinearColorView extends Group {
    protected GLObject colorLayer = null;
    private float[] endColor = new float[4];
    protected int endHandle;
    private String frag;
    private float[] startColor = new float[4];
    protected int startHandle;

    public LinearColorView(GLBase root, int w, int h, String fragFile) {
        super(root, w, h);
        this.frag = fragFile;
        this.colorLayer = initColorLayer(w, h);
        setLinearColor(-16777216, -1);
        setRadius(((float) w) / 2.0f);
        add(this.colorLayer);
    }

    private GLObject initColorLayer(int w, int h) {
        return new GLObject(this.root, w, h) {
            protected void createShader(Context c) {
                this.shader = LinearColorView.this.initColorShader(this.info, LinearColorView.this.frag);
            }

            protected void getHandles(int shader) {
                super.getHandles(shader);
                LinearColorView.this.getColorHandles(shader);
            }

            protected void updateData(GLObjectSettings settings, GLBaseSettings info) {
                super.updateData(settings, info);
                LinearColorView.this.updateColorData();
            }
        };
    }

    protected void updateColorData() {
        GL.glUniform3f(this.startHandle, this.startColor[0], this.startColor[1], this.startColor[2]);
        GL.glUniform3f(this.endHandle, this.endColor[0], this.endColor[1], this.endColor[2]);
    }

    protected void getColorHandles(int shader) {
        this.startHandle = GL.glGetUniformLocation(shader, "startColor");
        this.endHandle = GL.glGetUniformLocation(shader, "endColor");
    }

    protected int initColorShader(GLBaseSettings info, String frag) {
        return info.env.shaderManager.getShader(getClass(), frag);
    }

    protected void _drawChildren(GLBaseSettings info) {
        info.env.blendManager.push(BlendType.Normal);
        this.root.drawGLObject(this.colorLayer, info);
        info.env.blendManager.pop();
    }

    public void setLinearColor(int vs, int ve) {
        this.startColor[0] = ((float) Color.red(vs)) / 255.0f;
        this.startColor[1] = ((float) Color.green(vs)) / 255.0f;
        this.startColor[2] = ((float) Color.blue(vs)) / 255.0f;
        this.endColor[0] = ((float) Color.red(ve)) / 255.0f;
        this.endColor[1] = ((float) Color.green(ve)) / 255.0f;
        this.endColor[2] = ((float) Color.blue(ve)) / 255.0f;
        setDirty();
        requestRender();
    }
}
