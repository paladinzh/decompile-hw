package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.tv.pip.PipManager.Listener;

public class PipOnboardingActivity extends Activity implements Listener {
    private AnimatorSet mEnterAnimator;
    private final PipManager mPipManager = PipManager.getInstance();

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tv_pip_onboarding);
        findViewById(R.id.button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PipOnboardingActivity.this.finish();
            }
        });
        this.mPipManager.addListener(this);
    }

    public void onResume() {
        super.onResume();
        this.mEnterAnimator = new AnimatorSet();
        this.mEnterAnimator.playTogether(new Animator[]{loadAnimator(R.id.background, R.anim.tv_pip_onboarding_background_enter_animation), loadAnimator(R.id.remote, R.anim.tv_pip_onboarding_image_enter_animation), loadAnimator(R.id.remote_button, R.anim.tv_pip_onboarding_image_enter_animation), loadAnimator(R.id.title, R.anim.tv_pip_onboarding_title_enter_animation), loadAnimator(R.id.description, R.anim.tv_pip_onboarding_description_enter_animation), loadAnimator(R.id.button, R.anim.tv_pip_onboarding_button_enter_animation)});
        this.mEnterAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                ((AnimationDrawable) ((ImageView) PipOnboardingActivity.this.findViewById(R.id.remote_button)).getDrawable()).start();
            }
        });
        this.mEnterAnimator.setStartDelay((long) getResources().getInteger(R.integer.tv_pip_onboarding_anim_start_delay));
        this.mEnterAnimator.start();
    }

    private Animator loadAnimator(int viewResId, int animResId) {
        Animator animator = AnimatorInflater.loadAnimator(this, animResId);
        animator.setTarget(findViewById(viewResId));
        return animator;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mEnterAnimator.isStarted()) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mEnterAnimator.isStarted()) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onPause() {
        super.onPause();
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this);
    }

    public void onPipEntered() {
    }

    public void onPipActivityClosed() {
        finish();
    }

    public void onShowPipMenu() {
        finish();
    }

    public void onMoveToFullscreen() {
        finish();
    }

    public void onPipResizeAboutToStart() {
    }
}
