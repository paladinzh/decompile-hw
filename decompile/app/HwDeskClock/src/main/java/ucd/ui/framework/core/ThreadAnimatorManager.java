package ucd.ui.framework.core;

import android.os.SystemClock;
import java.util.Vector;

public class ThreadAnimatorManager {
    private Vector<ThreadAnimator> list = new Vector();
    private boolean threadRunning = true;

    protected ThreadAnimatorManager(GLBase root) {
    }

    protected void start(final GLBase root) {
        new Thread() {
            private long deltaTime;

            private long getFPT() {
                return (long) Math.ceil((double) (1000.0f / ((float) root.getFPS())));
            }

            public void run() {
                this.deltaTime = getFPT();
                long lastTime = SystemClock.elapsedRealtime() - this.deltaTime;
                while (ThreadAnimatorManager.this.threadRunning) {
                    long cur;
                    this.deltaTime = getFPT();
                    long t1 = lastTime;
                    if ((lastTime >= root.timeline ? 1 : null) == null) {
                        lastTime = root.timeline;
                        for (int i = 0; i < ThreadAnimatorManager.this.list.size(); i++) {
                            try {
                                ThreadAnimatorManager.this.runTask((ThreadAnimator) ThreadAnimatorManager.this.list.get(i), lastTime);
                            } catch (ArrayIndexOutOfBoundsException e) {
                            }
                        }
                    }
                    long delta = SystemClock.elapsedRealtime() - t1;
                    if (ThreadAnimatorManager.this.list.size() != 0) {
                        if ((delta >= this.deltaTime ? 1 : null) == null) {
                        }
                        cur = SystemClock.elapsedRealtime();
                        if ((cur - root.timeline >= this.deltaTime ? 1 : null) == null) {
                            lastTime = cur - 1;
                            root.timeline = cur;
                        }
                    }
                    try {
                        long sleepTime = this.deltaTime - delta;
                        if (ThreadAnimatorManager.this.list.size() == 0) {
                            sleepTime = 100;
                        }
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e2) {
                    }
                    cur = SystemClock.elapsedRealtime();
                    if (cur - root.timeline >= this.deltaTime) {
                    }
                    if ((cur - root.timeline >= this.deltaTime ? 1 : null) == null) {
                        lastTime = cur - 1;
                        root.timeline = cur;
                    }
                }
            }
        }.start();
    }

    private void runTask(ThreadAnimator anim, long timeline) {
        float per;
        float fraction;
        if (!anim.isRunning()) {
            per = (((float) (timeline - anim.startTime)) * 1.0f) / ((float) anim.duration);
            fraction = anim.pt.getInterpolation(per);
            if (per < 1.0f) {
                anim.onCancel(fraction, per);
            } else {
                anim.onEnd(fraction, per);
            }
            this.list.remove(anim);
        } else if (anim.isRunning == 0.0f) {
            anim.isRunning = Float.MIN_VALUE;
            anim.startTime = timeline;
            fraction = anim.pt.getInterpolation(0.0f);
            anim.onStart(fraction, 0.0f);
            anim.onUpdating(fraction, 0.0f);
        } else {
            per = (((float) (timeline - anim.startTime)) * 1.0f) / ((float) anim.duration);
            if (per >= 1.0f) {
                anim.isRunning = -1.0f;
                per = 1.0f;
            }
            anim.isRunning = per;
            fraction = anim.pt.getInterpolation(per);
            anim.onUpdating(fraction, per);
            if (per == 1.0f) {
                anim.isRunning = -1.0f;
                this.list.remove(anim);
                anim.onEnd(fraction, per);
            }
        }
    }

    protected void add(ThreadAnimator v) {
        if (!this.list.contains(v)) {
            this.list.add(v);
        }
    }

    public void exit() {
        this.threadRunning = false;
        if (this.list != null) {
            this.list.clear();
        }
    }
}
