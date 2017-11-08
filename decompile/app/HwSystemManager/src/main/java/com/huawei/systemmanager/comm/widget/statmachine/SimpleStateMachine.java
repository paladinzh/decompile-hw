package com.huawei.systemmanager.comm.widget.statmachine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleStateMachine {
    private static final int CMD_QUIT = -1;
    private static final String TAG = "SimpleStateMachine";
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

        public Message getCurrentMessage() {
            return this.mMsg;
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
                HwLog.e(TAG, "state machine looper == null!!");
            }
            if (this.mCurrentState != null) {
                this.mCurrentState.enter();
            }
        }
    }

    public void quit() {
        if (!this.mStarted.compareAndSet(true, false)) {
            HwLog.e(TAG, "Its already quit, do not quite again");
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

    protected void addState(IState state) {
        if (this.mStates.get(state.getFlag()) != null) {
            throw new IllegalStateException("Statemachine has this state already!");
        }
        this.mStates.put(state.getFlag(), state);
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

    protected Handler getHandler() {
        return this.mSmHandler;
    }

    public final IState getCurrentState() {
        return this.mCurrentState;
    }

    public String getName() {
        return this.mName;
    }

    public final void sendEmptyMessage(int what) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendEmptyMessage(what);
        }
    }

    public final void sendMessage(int what) {
        sendEmptyMessage(what);
    }

    public final void sendMessage(int what, Object obj) {
        if (this.mSmHandler != null) {
            this.mSmHandler.obtainMessage(what, obj).sendToTarget();
        }
    }

    public final void sendMessage(Message msg) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessage(msg);
        }
    }

    public final void sendMessage(int what, Object obj, int arg1, int arg2) {
        if (this.mSmHandler != null) {
            this.mSmHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
        }
    }

    public final void sendMessage(int what, int arg1, int arg2) {
        if (this.mSmHandler != null) {
            this.mSmHandler.obtainMessage(what, arg1, arg2).sendToTarget();
        }
    }

    public final void sendMessage(int what, int arg1) {
        if (this.mSmHandler != null) {
            this.mSmHandler.obtainMessage(what, arg1, 0).sendToTarget();
        }
    }

    public final void sendMessageDelay(int what, long delay) {
        if (this.mSmHandler != null) {
            this.mSmHandler.sendMessageDelayed(this.mSmHandler.obtainMessage(what), delay);
        }
    }

    public final void removeMessage(int what) {
        if (this.mSmHandler != null) {
            this.mSmHandler.removeMessages(what);
        }
    }

    public final Message getCurrentMessage() {
        if (this.mSmHandler != null) {
            return this.mSmHandler.getCurrentMessage();
        }
        return null;
    }

    private void performTransitions() {
        while (this.mDestState != null) {
            IState destState = this.mDestState;
            HwLog.i(TAG, "chage state from state:" + this.mCurrentState.getName() + " to state:" + destState.getName());
            this.mDestState = null;
            this.mCurrentState.exit();
            this.mCurrentState = destState;
            destState.enter();
        }
    }
}
