package ucd.ui.framework.core;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public abstract class ThreadAnimator {
    protected long duration = 1000;
    protected float isRunning = -1.0f;
    protected Interpolator pt = new AccelerateDecelerateInterpolator();
    protected GLBase root;
    protected long startTime;

    public abstract void onUpdating(float f, float f2);

    protected ThreadAnimator(GLBase v) {
        this.root = v;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setInterpolator(Interpolator i) {
        if (i != null) {
            this.pt = i;
        }
    }

    public void onStart(float fraction, float per) {
    }

    public void onEnd(float fraction, float per) {
    }

    public void onCancel(float fraction, float per) {
    }

    public boolean isRunning() {
        return this.isRunning != -1.0f;
    }

    public void start() {
        if (!isRunning()) {
            if (this.root != null) {
                this.root.animManager.add(this);
            }
            this.isRunning = 0.0f;
        }
    }

    public void cancel() {
        this.isRunning = -1.0f;
    }

    public void stop() {
        this.isRunning = -1.0f;
    }
}
