package com.android.settings.sdencryption.view.statemachine;

import android.app.Activity;
import android.app.Fragment;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.android.settings.sdencryption.SdEncryptionUtils;
import com.android.settings.sdencryption.SdLog;
import com.android.settings.sdencryption.view.MainCircleProgressView;
import com.android.settings.sdencryption.view.MainScreenRollingView;
import java.util.concurrent.atomic.AtomicInteger;

public class MsStateMachine extends SimpleStateMachine {
    private Animation animationCome;
    private Animation animationGo;
    private MainCircleProgressView mCircleImage;
    private IState mCryptEndState = new CryptEndState();
    private IState mCryptingState = new CryptingState();
    private TextView mProgressUnit;
    private MainScreenRollingView mProgressView;
    private AtomicInteger mScore = new AtomicInteger(100);
    private TextView mScoreUnit;
    private MainScreenRollingView mScoreView;

    public class CryptEndState extends SimpleState {
        public void enter() {
            updateStateByScore(true);
        }

        private void updateStateByScore(boolean anima) {
            int score = MsStateMachine.this.mScore.get();
            SdLog.i("MsStateMachine", "updateStateByScore, score is:" + score);
            if (anima) {
                MsStateMachine.this.mScoreView.setNumberQuick(score);
            } else {
                MsStateMachine.this.mScoreView.setNumberImmediately(score);
            }
            MsStateMachine.this.mCircleImage.updateScore(score);
        }
    }

    public class CryptingState extends SimpleState {
        int mProgress;

        public void enter() {
            this.mProgress = SdEncryptionUtils.getCryptPercent();
            SdLog.i("MsStateMachine", "CryptingState Enter Percent = " + this.mProgress);
            if (this.mProgress == 100) {
                this.mProgress = 0;
            }
            MsStateMachine.this.mProgressView.setNumberImmediately(this.mProgress);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 22:
                    this.mProgress = msg.arg1;
                    if (this.mProgress > 99) {
                        MsStateMachine.this.mCircleImage.isShading = true;
                    }
                    if (this.mProgress >= 1) {
                        MsStateMachine.this.mProgressView.setNumberByDuration(this.mProgress, 200);
                        break;
                    }
                    break;
                case 23:
                    final int score = MsStateMachine.this.mScore.get();
                    MsStateMachine.this.mCircleImage.setCompleteStatus();
                    MsStateMachine.this.animationGo.setAnimationListener(new AnimationListener() {
                        public void onAnimationStart(Animation a) {
                            MsStateMachine.this.mCircleImage.updateSocre(score, 600);
                            MsStateMachine.this.mScoreView.setNumberImmediately(score);
                            MsStateMachine.this.mScoreView.setVisibility(0);
                            MsStateMachine.this.mScoreView.startAnimation(MsStateMachine.this.animationCome);
                        }

                        public void onAnimationRepeat(Animation a) {
                        }

                        public void onAnimationEnd(Animation a) {
                            MsStateMachine.this.mProgressView.setVisibility(8);
                            MsStateMachine.this.mProgressUnit.setVisibility(8);
                            MsStateMachine.this.mScoreUnit.setVisibility(0);
                        }
                    });
                    MsStateMachine.this.mProgressView.startAnimation(MsStateMachine.this.animationGo);
                    MsStateMachine.this.transitionTo(MsStateMachine.this.mCryptEndState);
                    break;
                case 25:
                    MsStateMachine.this.mCircleImage.setCompleteStatus();
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }
    }

    public MsStateMachine(Fragment frag, View container, Looper looper) {
        super("MsStateMachine", looper);
        Activity ac = frag.getActivity();
        this.mCircleImage = (MainCircleProgressView) container.findViewById(2131887110);
        this.mScoreView = (MainScreenRollingView) container.findViewById(2131887111);
        this.mProgressView = (MainScreenRollingView) container.findViewById(2131886585);
        this.mProgressUnit = (TextView) container.findViewById(2131886586);
        this.mScoreUnit = (TextView) container.findViewById(2131887112);
        this.animationGo = AnimationUtils.loadAnimation(ac, 2131034135);
        this.animationCome = AnimationUtils.loadAnimation(ac, 2131034136);
        this.mProgressUnit.setVisibility(0);
        this.mProgressView.mIncludePercent = false;
        setInitialState(this.mCryptingState);
    }

    protected void defaultHandlerMessage(Message msg) {
        super.defaultHandlerMessage(msg);
    }

    public void start() {
        super.start();
    }

    public void quit() {
        super.quit();
    }

    public void finishCircleImageShading() {
        this.mCircleImage.setCompleteStatus();
    }
}
