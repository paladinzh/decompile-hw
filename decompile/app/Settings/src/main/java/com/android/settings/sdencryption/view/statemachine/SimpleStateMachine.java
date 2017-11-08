package com.android.settings.sdencryption.view.statemachine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import com.android.settings.sdencryption.SdLog;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleStateMachine {
    private IState mCurrentState;
    private IState mDestState;
    private Looper mLooper;
    private String mName;
    private final QuiteState mQuiteState = new QuiteState();
    private SmHandler mSmHandler;
    private AtomicBoolean mStarted = new AtomicBoolean(false);
    private SparseArray<IState> mStates = new SparseArray();

    private static class QuiteState extends SimpleState {
        private QuiteState() {
        }

        public void enter() {
        }

        public boolean processMessage(Message msg) {
            return true;
        }
    }

    private static class SmHandler extends Handler {
        private Message mMsg;
        private SimpleStateMachine mSm;

        private SmHandler(Looper looper, SimpleStateMachine sm) {
            super(looper);
            this.mSm = sm;
        }

        public final void handleMessage(Message msg) {
            this.mMsg = msg;
            IState state = this.mSm.getCurrentState();
            if (!(state == null || state.processMessage(msg))) {
                this.mSm.defaultHandlerMessage(msg);
            }
            this.mSm.performTransitions();
        }
    }

    protected SimpleStateMachine(String name, Looper looper) {
        this.mName = name;
        this.mLooper = looper;
    }

    public void start() {
        if (!this.mStarted.get()) {
            this.mStarted.set(true);
            if (this.mLooper != null) {
                this.mSmHandler = new SmHandler(this.mLooper, this);
            } else {
                SdLog.e("SimpleStateMachine", "state machine looper == null!!");
            }
            if (this.mCurrentState != null) {
                this.mCurrentState.enter();
            }
        }
    }

    public void quit() {
        if (!this.mStarted.compareAndSet(true, false)) {
            SdLog.e("SimpleStateMachine", "Its already quit, do not quite again");
        }
        if (this.mSmHandler != null) {
            this.mSmHandler.sendEmptyMessage(-1);
        }
    }

    protected void setInitialState(IState initialState) {
        if (initialState != null) {
            this.mCurrentState = initialState;
            if (this.mStarted.get()) {
                this.mCurrentState.enter();
            }
        }
    }

    protected final void transitionTo(IState destState) {
        this.mDestState = destState;
    }

    protected void defaultHandlerMessage(Message msg) {
        switch (msg.what) {
            case -1:
                transitionTo(this.mQuiteState);
                return;
            default:
                return;
        }
    }

    public final IState getCurrentState() {
        return this.mCurrentState;
    }

    public final void sendMessage(Message msg) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessage(msg);
        }
    }

    private void performTransitions() {
        while (this.mDestState != null) {
            IState destState = this.mDestState;
            SdLog.i("SimpleStateMachine", "chage state from state:" + this.mCurrentState.getName() + " to state:" + destState.getName());
            this.mDestState = null;
            this.mCurrentState.exit();
            this.mCurrentState = destState;
            destState.enter();
        }
    }
}
