package ucd;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.android.deskclock.DeskClockApplication;
import com.android.util.Utils;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import ucd.apps.Demos.BaseEffectView;
import ucd.apps.Demos.TestDemos$AlarmClockEffect;
import ucd.ui.framework.core.GLBase;
import ucd.ui.framework.core.GLBase.FPS;
import ucd.ui.framework.core.Group;
import ucd.ui.framework.core.ThreadAnimator;

public class RythmSurfaceView {
    private BaseEffectView effectView = null;
    protected GLBase glBase = null;
    private boolean isAnimating = false;
    private final BaseEffectView mClock = new TestDemos$AlarmClockEffect();
    private Group presentationGroup = null;

    public RythmSurfaceView(Context context) {
        this.glBase = new GLBase(context) {
            public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
                super.onSurfaceCreated(arg0, arg1);
                int w = RythmSurfaceView.this.glBase.getWidth();
                int h = RythmSurfaceView.this.glBase.getHeight();
                RythmSurfaceView.this.showEffectView(RythmSurfaceView.this.mClock);
                RythmSurfaceView.this.presentationGroup.setLayoutParams(w, h);
                RythmSurfaceView.this.presentationGroup.setLocation(0.0f, 0.0f);
            }

            public void onSurfaceChanged(GL10 arg0, int w, int h) {
                super.onSurfaceChanged(arg0, w, h);
            }
        };
        this.glBase.setFPS(FPS.Low);
        this.glBase.setRenderAuto(false);
        this.presentationGroup = new Group(this.glBase, 0, 0);
        this.glBase.add(this.presentationGroup);
    }

    public void attachView(ViewGroup viewGroup) {
        Context context = DeskClockApplication.getDeskClockApplication();
        if (Utils.isLandScreen(context)) {
            int size = (viewGroup.getResources().getDisplayMetrics().widthPixels / 12) * 5;
            int marginLeft = Utils.dip2px(context, 54);
            int marginRight = Utils.dip2px(context, 14);
            LayoutParams rlp = new LayoutParams(size, size);
            rlp.setMargins(marginLeft, 0, marginRight, 0);
            rlp.addRule(15);
            viewGroup.addView(this.glBase, rlp);
            return;
        }
        size = Math.min(viewGroup.getResources().getDisplayMetrics().heightPixels, viewGroup.getResources().getDisplayMetrics().widthPixels);
        rlp = new LayoutParams(size, size);
        rlp.addRule(13);
        viewGroup.addView(this.glBase, rlp);
    }

    protected void showEffectView(final BaseEffectView view) {
        if (!this.isAnimating) {
            this.effectView = view;
            ThreadAnimator animator = new ThreadAnimator(this.glBase) {
                public void onUpdating(float fraction, float per) {
                    RythmSurfaceView.this.glBase.requestRender();
                }

                public void onStart(float fraction, float per) {
                    RythmSurfaceView.this.isAnimating = true;
                }

                public void onEnd(float fraction, float per) {
                    RythmSurfaceView.this.isAnimating = false;
                    view.createView(RythmSurfaceView.this.glBase, RythmSurfaceView.this.presentationGroup);
                }
            };
            animator.setDuration(150);
            animator.start();
        }
    }

    protected void restore() {
        if (this.effectView != null) {
            ThreadAnimator animator = new ThreadAnimator(this.glBase) {
                public void onUpdating(float fraction, float per) {
                    RythmSurfaceView.this.glBase.requestRender();
                }

                public void onStart(float fraction, float per) {
                    RythmSurfaceView.this.isAnimating = true;
                }

                public void onEnd(float fraction, float per) {
                    RythmSurfaceView.this.isAnimating = false;
                    RythmSurfaceView.this.presentationGroup.delAll();
                    RythmSurfaceView.this.effectView.destroyView(RythmSurfaceView.this.glBase, RythmSurfaceView.this.presentationGroup);
                }
            };
            animator.setDuration(150);
            animator.start();
        }
    }

    public void onResume() {
        this.glBase.onResume();
    }

    public void onPause() {
        this.glBase.onPause();
    }

    public void onDestroy() {
        restore();
        this.glBase.onDestroy();
    }
}
