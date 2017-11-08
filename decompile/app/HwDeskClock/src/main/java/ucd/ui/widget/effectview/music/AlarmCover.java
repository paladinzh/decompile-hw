package ucd.ui.widget.effectview.music;

import android.os.SystemClock;
import android.view.animation.LinearInterpolator;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.core.BlendManager.BlendType;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.ThreadAnimator;

public class AlarmCover extends MusicCover {
    private ThreadAnimator anim;
    private float lastAngle = 0.0f;
    private boolean pause = false;
    private long pauseTime;

    public AlarmCover(GLBase root, int w, int h) {
        super(root, w, h);
        if (this.layer1 instanceof AlarmView) {
            AlarmView layer1 = this.layer1;
            layer1.setRingWidth(0.1f);
            layer1.setExternalGlowWidth(0.05f);
            layer1.setFactor(1.5f);
        }
        if (this.layer2 instanceof AlarmView) {
            AlarmView layer2 = this.layer2;
            layer2.setRingWidth(0.1f);
            layer2.setExternalGlowWidth(0.05f);
            layer2.setFactor(1.5f);
        }
        this.brush.rotate(0.0f, 0.0f, 0.0f);
    }

    protected VoiceView createLayer(int w, int h) {
        return new AlarmView(this.root, w, h);
    }

    protected LinearColorView createBrush(int w, int h) {
        return new ConeColorView(this.root, w, h);
    }

    protected void shake() {
        if (this.anim != null && this.anim.isRunning()) {
            this.anim.cancel();
        }
        this.anim = new ThreadAnimator(this.root) {
            private float[] angles = new float[]{0.0f, (float) Math.toRadians(120.0d), (float) Math.toRadians(240.0d)};
            private float[] data1 = new float[3];
            private float[] p1;
            private float[] p2;
            private float[] p3;

            public void onStart(float fraction, float per) {
                this.p1 = AlarmCover.this.layer1.getCopyCircle(1);
                this.p2 = AlarmCover.this.layer1.getCopyCircle(2);
                this.p3 = AlarmCover.this.layer1.getCopyCircle(3);
            }

            private float[] byVoiceFactor(float[] v) {
                this.data1[0] = v[0] * 0.8f;
                this.data1[1] = v[1] * 0.8f;
                this.data1[2] = v[2] * 0.8f;
                return this.data1;
            }

            public void onUpdating(float fraction, float per) {
                long elapsedTime = SystemClock.elapsedRealtime() - AlarmCover.this.pauseTime;
                float factor;
                if (AlarmCover.this.pause) {
                    if (elapsedTime > AlarmCover.this.anim.getDuration()) {
                        AlarmCover.this.layer1.setFactor(2.5f);
                        AlarmCover.this.layer2.setFactor(2.5f);
                        AlarmCover.this.anim.cancel();
                    } else {
                        factor = 1.5f + (((float) Math.pow((double) (((float) elapsedTime) / ((float) AlarmCover.this.anim.getDuration())), 0.5d)) * 1.0f);
                        AlarmCover.this.layer1.setFactor(factor);
                        AlarmCover.this.layer2.setFactor(factor);
                    }
                } else if (elapsedTime > AlarmCover.this.anim.getDuration()) {
                    AlarmCover.this.layer1.setFactor(1.5f);
                    AlarmCover.this.layer2.setFactor(1.5f);
                } else {
                    factor = 2.5f + (((float) Math.pow((double) (((float) elapsedTime) / ((float) AlarmCover.this.anim.getDuration())), 0.5d)) * -1.0f);
                    AlarmCover.this.layer1.setFactor(factor);
                    AlarmCover.this.layer2.setFactor(factor);
                }
                if (AlarmCover.this.adapter != null) {
                    float[] data = AlarmCover.this.adapter.getLMH();
                    if (data != null) {
                        float degree = AlarmCover.this.lastAngle + (25.0f * fraction);
                        double radian = Math.toRadians((double) degree);
                        float deltaDegree = ((float) Math.sin(radian)) * 10.0f;
                        float deltaDegree1 = ((float) Math.cos(radian)) * 10.0f;
                        float[] newAngles = this.angles;
                        newAngles[0] = (float) Math.toRadians((double) deltaDegree);
                        newAngles[1] = (float) Math.toRadians((((double) deltaDegree) * -0.9d) + 120.0d);
                        newAngles[2] = (float) Math.toRadians((((double) deltaDegree) * -1.5d) + 240.0d);
                        AlarmCover.this.layer1.setState((0.0f + degree) + deltaDegree, this.p1, this.p2, this.p3, data, newAngles);
                        newAngles[0] = (float) Math.toRadians((double) deltaDegree1);
                        newAngles[1] = (float) Math.toRadians((((double) deltaDegree1) * 0.8d) + 120.0d);
                        newAngles[2] = (float) Math.toRadians((((double) deltaDegree1) * -1.3d) + 240.0d);
                        AlarmCover.this.layer2.setState(180.0f + degree, this.p1, this.p2, this.p3, byVoiceFactor(data), newAngles);
                        AlarmCover.this.setDirty();
                        AlarmCover.this.requestRender();
                    }
                }
            }

            public void onEnd(float fraction, float per) {
                AlarmCover alarmCover = AlarmCover.this;
                alarmCover.lastAngle = alarmCover.lastAngle + 25.0f;
                AlarmCover.this.shake();
            }
        };
        this.anim.setInterpolator(new LinearInterpolator());
        this.anim.setDuration(1000);
        this.anim.start();
    }

    protected void _drawChildren(GLBaseSettings info) {
        info.env.blendManager.push(BlendType.Add);
        this.root.drawGLObject(this.layer1, info);
        this.root.drawGLObject(this.layer2, info);
        info.env.blendManager.pop();
        info.env.blendManager.push(BlendType.AlphaGradientStencil);
        this.root.drawGLObject(this.brush, info);
        info.env.blendManager.pop();
    }
}
