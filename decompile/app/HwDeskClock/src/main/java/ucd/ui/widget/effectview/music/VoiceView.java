package ucd.ui.widget.effectview.music;

import android.content.Context;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.Settings.GLObjectSettings;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.GLObject;
import ucd.ui.framework.lib.GL;

public class VoiceView extends GLObject {
    private float[] balls = new float[16];
    private int circle_handle = -1;
    private float criticleValue = 1.0f;
    private int criticleValueHandle = -1;
    protected float disFactor = 2.5f;
    private float[] lastAmplitude = new float[3];
    protected float maxAmp;
    private float power = 2.0f;
    private int powerHandle = -1;
    protected float r0;
    protected float r0_new;
    protected float r1;
    protected float r2;
    protected float r3;

    public VoiceView(GLBase root, int w, int h) {
        super(root, w, h);
        initCirclesRadius(w, h);
        setPaintColor(-1);
    }

    private void initCirclesRadius(int w, int h) {
        int minW;
        if (w > h) {
            minW = h;
        } else {
            minW = w;
        }
        float f = ((float) minW) * 0.32f;
        this.r0 = f;
        this.r0_new = f;
        f = this.r0 / 4.0f;
        this.r3 = f;
        this.r2 = f;
        this.r1 = f;
        this.maxAmp = this.r1 * 1.5f;
        setCircle(1, new float[]{0.0f, 0.0f, this.r1, 0.0f});
        setCircle(2, new float[]{0.0f, 0.0f, this.r2, 0.0f});
        setCircle(3, new float[]{0.0f, 0.0f, this.r3, 0.0f});
        setCircle(0, new float[]{0.0f, 0.0f, this.r0, 0.0f});
        obtainCriticalValue();
    }

    protected void obtainCriticalValue() {
        this.criticleValue = (((float) Math.pow((double) (this.r1 / this.r0), (double) this.power)) * 3.0f) + 1.0f;
    }

    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        initCirclesRadius(width, height);
        super.onSizeChanged(width, height, oldW, oldH);
    }

    protected void createShader(Context c) {
        this.shader = this.info.env.shaderManager.getShader(VoiceView.class, "shader/widget/voiceview.frag.glsl");
    }

    protected void getHandles(int shader) {
        super.getHandles(shader);
        this.circle_handle = GL.glGetUniformLocation(shader, "circles");
        this.criticleValueHandle = GL.glGetUniformLocation(shader, "criticleValue");
        this.powerHandle = GL.glGetUniformLocation(shader, "power");
    }

    protected void updateData(GLObjectSettings settings, GLBaseSettings info) {
        super.updateData(settings, info);
        GL.glUniformMatrix4fv(this.circle_handle, 1, false, this.balls, 0);
        GL.glUniform1f(this.criticleValueHandle, this.criticleValue);
        GL.glUniform1f(this.powerHandle, this.power);
    }

    public void setCircle(int index, float[] arr) {
        for (int i = 0; i < arr.length; i++) {
            this.balls[(index * 4) + i] = (float) Math.round(arr[i]);
        }
    }

    public float[] getCopyCircle(int index) {
        int s = index * 4;
        float[] t = new float[4];
        for (int i = 0; i < 4; i++) {
            t[i] = this.balls[s + i];
        }
        return t;
    }

    protected float smoothstep(float dst, int index) {
        float last = this.lastAmplitude[index];
        float delta = dst - last;
        if (delta > 0.0f) {
            this.lastAmplitude[index] = (0.5f * delta) + last;
        } else {
            this.lastAmplitude[index] = (0.05f * delta) + last;
        }
        return this.lastAmplitude[index];
    }

    protected int toVisualRange(float v, float factor) {
        return (int) (((60.0f * v) / factor) + 0.0f);
    }

    public void setState(float curAngle, float[] circle1, float[] circle2, float[] circle3, float[] amps, float[] angles) {
        float a1 = amps[0];
        float a2 = amps[1];
        float t = 0.0078125f * this.maxAmp;
        a2 = smoothstep((float) toVisualRange(a2, 50.0f), 1) * t;
        float a3 = smoothstep((float) toVisualRange(amps[2], 10.0f), 2) * t;
        float[] fArr = circle1;
        setPos(1, fArr, this.r1, angles[0], smoothstep((float) toVisualRange(a1, 100.0f), 0) * t);
        setPos(2, circle2, this.r2, angles[1], a2);
        setPos(3, circle3, this.r3, angles[2], a3);
        rotate(0.0f, 0.0f, curAngle);
    }

    protected void setPos(int index, float[] circle, float r, float theta, float amplitude) {
        if (amplitude > this.maxAmp) {
            amplitude = this.maxAmp;
        }
        float d = this.r0_new + amplitude;
        circle[0] = (float) (((double) (d - (this.r1 * this.disFactor))) * Math.sin((double) theta));
        circle[1] = (float) (((double) (d - (this.r1 * this.disFactor))) * Math.cos((double) theta));
        setCircle(index, circle);
    }

    protected void setDisFactor(float factor) {
        this.disFactor = factor;
        setDirty();
        requestRender();
    }

    public void setFactor(float factor) {
        setDisFactor(factor);
    }
}
