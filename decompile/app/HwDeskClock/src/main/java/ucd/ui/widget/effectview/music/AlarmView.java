package ucd.ui.widget.effectview.music;

import android.content.Context;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.Settings.GLObjectSettings;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.lib.GL;

public class AlarmView extends VoiceView {
    private float externalGlowWidth = 0.0f;
    private int externalGlowWidthHandle = -1;
    private float glowPower = 8.0f;
    private int glowPowerHandle = -1;
    private float innerGlowWidth = 0.0f;
    private int innerGlowWidthHandle = -1;
    private float ringWidth = 0.0f;
    private int ringWidthHandle = -1;

    public AlarmView(GLBase root, int w, int h) {
        super(root, w, h);
    }

    protected void createShader(Context c) {
        this.shader = this.info.env.shaderManager.getShader(AlarmView.class, "shader/widget/alarm.frag.glsl");
    }

    protected void updateData(GLObjectSettings settings, GLBaseSettings info) {
        super.updateData(settings, info);
        GL.glUniform1f(this.externalGlowWidthHandle, this.externalGlowWidth);
        GL.glUniform1f(this.innerGlowWidthHandle, this.innerGlowWidth);
        GL.glUniform1f(this.ringWidthHandle, this.ringWidth);
        GL.glUniform1f(this.glowPowerHandle, this.glowPower);
    }

    protected void getHandles(int shader) {
        super.getHandles(shader);
        this.externalGlowWidthHandle = GL.glGetUniformLocation(shader, "externalGlowWidth");
        this.innerGlowWidthHandle = GL.glGetUniformLocation(shader, "innerGlowWidth");
        this.ringWidthHandle = GL.glGetUniformLocation(shader, "ringWidth");
        this.glowPowerHandle = GL.glGetUniformLocation(shader, "glowPower");
    }

    protected void setPos(int index, float[] circle, float r, float theta, float amplitude) {
        float d = this.r0 + amplitude;
        circle[0] = (float) (((double) (d - (this.disFactor * r))) * Math.sin((double) theta));
        circle[1] = (float) (((double) (d - (this.disFactor * r))) * Math.cos((double) theta));
        setCircle(index, circle);
    }

    public void setExternalGlowWidth(float width) {
        if (width < 1.0f) {
            this.externalGlowWidth = width;
            setDirty();
            requestRender();
        }
    }

    public void setRingWidth(float width) {
        this.ringWidth = width;
        setDirty();
        requestRender();
    }

    public void setFactor(float factor) {
        setDisFactor(factor);
    }
}
