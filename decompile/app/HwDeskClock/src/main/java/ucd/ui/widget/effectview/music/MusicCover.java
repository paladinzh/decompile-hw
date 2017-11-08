package ucd.ui.widget.effectview.music;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.animation.LinearInterpolator;
import java.security.SecureRandom;
import ucd.ui.framework.Settings.GLBaseSettings;
import ucd.ui.framework.core.BlendManager.BlendType;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.Group;
import ucd.ui.framework.core.ImageEx;
import ucd.ui.framework.core.ThreadAnimator;
import ucd.ui.util.Downloader.Callback;

public class MusicCover extends Group {
    protected DataAdapter adapter;
    protected float al1 = 0.7f;
    protected float al2 = 0.5f;
    private ThreadAnimator anim;
    protected LinearColorView brush;
    protected ImageEx img;
    private int imgW;
    private float lastAngle;
    private float lastAngle2;
    protected VoiceView layer1;
    protected VoiceView layer2;
    private OnGenerateColorListener onGenerateColorListener;
    private boolean pause = false;
    private long pauseTime;
    private boolean useGenerateColor = true;
    protected float voiceFactor;

    public static abstract class OnGenerateColorListener implements ucd.ui.util.GenerateColor.OnGenerateColorListener {
        public abstract void onGenerateColor(int i, int i2, int i3);

        public void onGenerateColor(int[] colors) {
        }
    }

    @SuppressLint({"TrulyRandom"})
    public MusicCover(GLBase root, int w, int h) {
        super(root, w, h);
        this.layer1 = createLayer(w, h);
        this.layer1.setPaintColor(-16776961);
        this.layer1.setAlpha(this.settings.viewcfg.alpha * this.al1);
        this.layer2 = createLayer(w, h);
        this.layer2.setAlpha(this.settings.viewcfg.alpha * this.al2);
        this.imgW = (int) (this.layer1.r0 * 2.0f);
        this.img = new ImageEx(root, this.imgW, this.imgW);
        this.img.scale(0.98f);
        this.img.setRadius(((float) this.imgW) / 2.0f);
        this.img.setOnGenerateColorListener(new ucd.ui.util.GenerateColor.OnGenerateColorListener() {
            private int defaultColor = -3684409;

            public void onGenerateColor(int[] pColors) {
                if (MusicCover.this.useGenerateColor) {
                    int c1 = pColors[0];
                    if (c1 == -1) {
                        c1 = this.defaultColor;
                    }
                    int c2 = pColors[1];
                    if (c2 == -1) {
                        c2 = this.defaultColor;
                    }
                    int c3 = pColors[3];
                    if (c3 == -16777216) {
                        c3 = pColors[4];
                    }
                    if (c3 == -16777216) {
                        c3 = pColors[5];
                    }
                    if (MusicCover.this.onGenerateColorListener != null) {
                        MusicCover.this.onGenerateColorListener.onGenerateColor(c1, c2, c3);
                    }
                    MusicCover.this.brush.setLinearColor(c1, c2);
                }
            }
        });
        this.brush = createBrush(w, h);
        this.brush.rotate(0.0f, 0.0f, 45.0f);
        add(this.layer1);
        add(this.layer2);
        add(this.brush);
        add(this.img);
        this.voiceFactor = (new SecureRandom().nextFloat() / 2.0f) + 0.1f;
    }

    protected VoiceView createLayer(int w, int h) {
        return new VoiceView(this.root, w, h);
    }

    protected LinearColorView createBrush(int w, int h) {
        return new LinearColorView(this.root, w, h, "shader/widget/linearcolor.frag.glsl");
    }

    public void load(String url, final Callback cb) {
        this.img.load(url, new Callback() {
            public void onload(Bitmap bit, String url) {
                if (cb != null) {
                    cb.onload(bit, url);
                }
                MusicCover.this.setDirty();
                MusicCover.this.requestRender();
            }
        });
    }

    public void setPaintColor(int hex) {
        this.img.setPaintColor(hex);
    }

    protected void _drawChildren(GLBaseSettings info) {
        this.root.drawGLObject(this.layer1, info);
        this.root.drawGLObject(this.layer2, info);
        info.env.blendManager.push(BlendType.AlphaGradientStencil);
        this.root.drawGLObject(this.brush, info);
        info.env.blendManager.pop();
        this.img.setLocation(((float) (getWidth() - this.imgW)) / 2.0f, ((float) (getHeight() - this.imgW)) / 2.0f);
        this.root.drawGLObject(this.img, info);
    }

    protected void shake() {
        if (this.anim != null && this.anim.isRunning()) {
            this.anim.cancel();
        }
        this.anim = new ThreadAnimator(this.root) {
            private float[] angles = new float[]{0.0f, (float) Math.toRadians(120.0d), (float) Math.toRadians(240.0d)};
            private float[] angles2 = new float[]{(float) Math.toRadians(60.0d), (float) Math.toRadians(180.0d), (float) Math.toRadians(300.0d)};
            private float[] data1 = new float[3];
            private final float l1 = MusicCover.this.lastAngle;
            private final float l2 = MusicCover.this.lastAngle2;
            private float[] p1;
            private float[] p2;
            private float[] p3;

            public void onStart(float fraction, float per) {
                this.p1 = MusicCover.this.layer1.getCopyCircle(1);
                this.p2 = MusicCover.this.layer1.getCopyCircle(2);
                this.p3 = MusicCover.this.layer1.getCopyCircle(3);
            }

            private float[] byVoiceFactor(float[] v) {
                float f = MusicCover.this.voiceFactor;
                this.data1[0] = v[0] * f;
                this.data1[1] = v[1] * f;
                this.data1[2] = v[2] * f;
                return this.data1;
            }

            public void onUpdating(float fraction, float per) {
                long elapsedTime = SystemClock.elapsedRealtime() - MusicCover.this.pauseTime;
                float factor;
                if (MusicCover.this.pause) {
                    if (elapsedTime > MusicCover.this.anim.getDuration()) {
                        MusicCover.this.layer1.setDisFactor(2.5f);
                        MusicCover.this.layer2.setDisFactor(2.5f);
                        MusicCover.this.anim.cancel();
                    } else {
                        factor = 1.5f + (((float) Math.pow((double) (((float) elapsedTime) / ((float) MusicCover.this.anim.getDuration())), 0.5d)) * 1.0f);
                        MusicCover.this.layer1.setDisFactor(factor);
                        MusicCover.this.layer2.setDisFactor(factor);
                    }
                } else if (elapsedTime > MusicCover.this.anim.getDuration()) {
                    MusicCover.this.layer1.setDisFactor(1.5f);
                    MusicCover.this.layer2.setDisFactor(1.5f);
                } else {
                    factor = 2.5f + (((float) Math.pow((double) (((float) elapsedTime) / ((float) MusicCover.this.anim.getDuration())), 0.5d)) * -1.0f);
                    MusicCover.this.layer1.setDisFactor(factor);
                    MusicCover.this.layer2.setDisFactor(factor);
                }
                if (MusicCover.this.adapter != null) {
                    float[] data = MusicCover.this.adapter.getLMH();
                    if (data != null) {
                        float cur = this.l1 + (20.0f * fraction);
                        MusicCover.this.lastAngle = cur;
                        MusicCover.this.layer1.setState(cur, this.p1, this.p2, this.p3, data, this.angles);
                        float cur2 = this.l2 + (35.0f * fraction);
                        MusicCover.this.lastAngle2 = cur2;
                        MusicCover.this.layer2.setState(cur2, this.p1, this.p2, this.p3, byVoiceFactor(data), this.angles2);
                        MusicCover.this.setDirty();
                        MusicCover.this.requestRender();
                    }
                }
            }

            public void onEnd(float fraction, float per) {
                MusicCover.this.shake();
            }
        };
        this.anim.setInterpolator(new LinearInterpolator());
        this.anim.setDuration(1000);
        this.anim.start();
    }

    public void setAdapter(DataAdapter adapter) {
        this.adapter = adapter;
        if (adapter != null) {
            shake();
        } else if (this.anim != null) {
            this.anim.stop();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.anim != null) {
            this.anim.stop();
        }
    }
}
