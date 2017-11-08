package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.anim.StateTransitionAnimation.Transition;
import java.util.Stack;

public class StateManager {
    private GLHost mHost;
    private boolean mIsResumed = false;
    private boolean mIsStarted = false;
    private ResultEntry mResult;
    private Stack<StateEntry> mStack = new Stack();

    private static class StateEntry {
        public ActivityState activityState;
        public Bundle data;

        public StateEntry(Bundle data, ActivityState state) {
            this.data = data;
            this.activityState = state;
        }
    }

    public StateManager(GLHost host) {
        this.mHost = host;
    }

    public void startState(Class<? extends ActivityState> klass, Bundle data) {
        GalleryLog.v("StateManager", "startState " + klass);
        try {
            ActivityState state = (ActivityState) klass.newInstance();
            if (this.mStack.isEmpty()) {
                GalleryLog.d("StateManager", "startState:" + klass + ", old stack is empty");
            } else {
                ActivityState top = getTopState();
                GalleryLog.d("StateManager", "startState:" + klass + ", old top:" + top.getClass());
                top.transitionOnNextPause(top.getClass(), klass, Transition.Incoming);
                if (this.mIsResumed) {
                    top.onPause();
                }
                if (this.mIsStarted) {
                    top.onStop();
                }
            }
            state.initialize(this.mHost, data);
            this.mStack.push(new StateEntry(data, state));
            state.onCreate(data, null);
            if (this.mIsStarted) {
                state.onStart();
            }
            if (this.mIsResumed) {
                state.resume();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void startStateForResult(Class<? extends ActivityState> klass, int requestCode, Bundle data) {
        GalleryLog.v("StateManager", "startStateForResult " + klass + ", " + requestCode);
        TraceController.traceBegin("StateManager startStateForResult newInstance");
        try {
            ActivityState state = (ActivityState) klass.newInstance();
            TraceController.traceEnd();
            state.initialize(this.mHost, data);
            state.mResult = new ResultEntry();
            state.mResult.requestCode = requestCode;
            if (this.mStack.isEmpty()) {
                GalleryLog.d("StateManager", "startStateForResult:" + klass + ", old stack is empty");
                this.mResult = state.mResult;
            } else {
                TraceController.traceBegin("StateManager startStateForResult transitionOnNextPause");
                ActivityState as = getTopState();
                GalleryLog.d("StateManager", "startStateForResult:" + klass + ", old top:" + as.getClass());
                as.transitionOnNextPause(as.getClass(), klass, Transition.Incoming);
                as.mReceivedResults = state.mResult;
                TraceController.traceEnd();
                if (this.mIsResumed) {
                    TraceController.traceBegin("StateManager startStateForResult onPause");
                    as.onPause();
                    TraceController.traceEnd();
                }
                if (this.mIsStarted) {
                    TraceController.traceBegin("StateManager startStateForResult onStop");
                    as.onStop();
                    TraceController.traceEnd();
                }
            }
            this.mStack.push(new StateEntry(data, state));
            TraceController.traceBegin("StateManager startStateForResult onCreate");
            state.onCreate(data, null);
            TraceController.traceEnd();
            if (this.mIsStarted) {
                TraceController.traceBegin("StateManager startStateForResult onStart");
                state.onStart();
                TraceController.traceEnd();
            }
            if (this.mIsResumed) {
                TraceController.traceBegin("StateManager startStateForResult resume");
                state.resume();
                TraceController.traceEnd();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public boolean createOptionsMenu(Menu menu) {
        if (this.mStack.isEmpty()) {
            return false;
        }
        ActivityState as = getTopState();
        GalleryLog.d("StateManager", as.getClass() + " onCreateActionBar");
        return as.onCreateActionBar(menu);
    }

    public void onConfigurationChange(Configuration config) {
        for (StateEntry entry : this.mStack) {
            entry.activityState.onConfigurationChanged(config);
        }
    }

    public void resume() {
        if (!this.mIsResumed) {
            this.mIsResumed = true;
            if (!this.mStack.isEmpty()) {
                ActivityState as = getTopState();
                GalleryLog.d("StateManager", as.getClass() + " resume");
                as.resume();
            }
        }
    }

    public void pause() {
        if (this.mIsResumed) {
            this.mIsResumed = false;
            if (!this.mStack.isEmpty()) {
                boolean z;
                ActivityState as = getTopState();
                GalleryLog.d("StateManager", as.getClass() + " onPause");
                if (GalleryUtils.isKeyguardLocked(this.mHost.getActivity())) {
                    z = true;
                } else {
                    z = GalleryUtils.isScreenOff(this.mHost.getActivity());
                }
                as.pauseByKeyguard(z);
                as.onPause();
                as.pauseByKeyguard(false);
            }
        }
    }

    public void notifyActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityState as = getTopState();
        GalleryLog.d("StateManager", as.getClass() + " onStateResult");
        as.onStateResult(requestCode, resultCode, data);
    }

    public int getStateCount() {
        return this.mStack.size();
    }

    public boolean itemSelected(Action action) {
        if (this.mStack.isEmpty()) {
            return false;
        }
        ActivityState as = getTopState();
        GalleryLog.d("StateManager", as.getClass() + " onItemSelected");
        return as.onItemSelected(action);
    }

    public boolean onBackPressed() {
        if (this.mStack.isEmpty()) {
            return false;
        }
        ActivityState as = getTopState();
        GalleryLog.d("StateManager", as.getClass() + " onBackPressed");
        return as.onBackPressed();
    }

    void finishState(ActivityState state) {
        finishState(state, true);
    }

    void finishState(ActivityState state, boolean fireOnPause) {
        if (this.mStack.size() == 1) {
            Activity activity = this.mHost.getActivity();
            if (this.mResult != null) {
                activity.setResult(this.mResult.resultCode, this.mResult.resultData);
            }
            activity.finish();
            if (activity.isFinishing()) {
                GalleryLog.d("StateManager", "no more state, finish activity");
            } else {
                GalleryLog.w("StateManager", "finish is rejected, keep the last state");
                return;
            }
        }
        GalleryLog.d("StateManager", "finishState " + state);
        if (!printAbnormalState(state)) {
            this.mStack.pop();
            state.mIsFinishing = true;
            ActivityState activityState = !this.mStack.isEmpty() ? ((StateEntry) this.mStack.peek()).activityState : null;
            if (this.mIsStarted && this.mIsResumed && fireOnPause) {
                if (activityState != null) {
                    state.transitionOnNextPause(state.getClass(), activityState.getClass(), Transition.Outgoing);
                }
                state.onPause();
                state.onStop();
            }
            state.onDestroy();
            if (activityState != null) {
                GalleryLog.d("StateManager", "new top is " + activityState.getClass());
                if (this.mIsStarted) {
                    activityState.onStart();
                }
                if (this.mIsResumed) {
                    activityState.resume();
                }
            } else {
                GalleryLog.d("StateManager", "new top is null");
            }
        }
    }

    private boolean printAbnormalState(ActivityState state) {
        if (state == ((StateEntry) this.mStack.peek()).activityState) {
            return false;
        }
        if (state.isDestroyed()) {
            GalleryLog.d("StateManager", "The state is already destroyed");
            return true;
        }
        throw new IllegalArgumentException("The stateview to be finished is not at the top of the stack: " + state + ", " + ((StateEntry) this.mStack.peek()).activityState);
    }

    public void switchState(ActivityState oldState, Class<? extends ActivityState> klass, Bundle data) {
        GalleryLog.d("StateManager", "switchState " + oldState + ", " + klass);
        if (oldState != ((StateEntry) this.mStack.peek()).activityState) {
            throw new IllegalArgumentException("The stateview to be finished is not at the top of the stack: " + oldState + ", " + ((StateEntry) this.mStack.peek()).activityState);
        }
        this.mStack.pop();
        ResultEntry resultEntry = oldState.mResult;
        if (this.mIsResumed) {
            oldState.onPause();
        }
        if (this.mIsStarted) {
            oldState.onStop();
        }
        oldState.onDestroy();
        try {
            ActivityState state = (ActivityState) klass.newInstance();
            state.initialize(this.mHost, data);
            state.mResult = resultEntry;
            this.mStack.push(new StateEntry(data, state));
            state.onCreate(data, null);
            if (this.mIsStarted) {
                state.onStart();
            }
            if (this.mIsResumed) {
                state.resume();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void destroy() {
        GalleryLog.v("StateManager", "destroy");
        while (!this.mStack.isEmpty()) {
            ((StateEntry) this.mStack.pop()).activityState.onDestroy();
        }
        this.mStack.clear();
        this.mHost.getGLRoot().setContentPane(null);
    }

    public void restoreFromState(Bundle inState) {
        GalleryLog.v("StateManager", "restoreFromState");
        for (Parcelable parcelable : inState.getParcelableArray("activity-state")) {
            Bundle bundle = (Bundle) parcelable;
            Class<? extends ActivityState> klass = (Class) bundle.getSerializable("class");
            if (klass != null) {
                Bundle data = bundle.getBundle(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
                Bundle state = bundle.getBundle("bundle");
                try {
                    GalleryLog.d("StateManager", "restoreFromState " + klass);
                    ActivityState activityState = (ActivityState) klass.newInstance();
                    activityState.initialize(this.mHost, data);
                    activityState.onCreate(data, state);
                    this.mStack.push(new StateEntry(data, activityState));
                } catch (InstantiationException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e2) {
                    throw new AssertionError(e2);
                } catch (Exception e3) {
                    throw new AssertionError(e3);
                }
            }
        }
    }

    public void lazyResumeTopState() {
        if (!this.mStack.isEmpty()) {
            ActivityState activityState = getTopState();
            if (this.mIsStarted) {
                activityState.onStart();
            }
            if (this.mIsResumed) {
                activityState.resume();
            }
        }
    }

    public void saveState(Bundle outState) {
        GalleryLog.d("StateManager", "saveState");
        Parcelable[] list = new Parcelable[this.mStack.size()];
        int i = 0;
        for (StateEntry entry : this.mStack) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("class", entry.activityState.getClass());
            bundle.putBundle(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, entry.data);
            Bundle state = new Bundle();
            entry.activityState.onSaveState(state);
            bundle.putBundle("bundle", state);
            GalleryLog.d("StateManager", "saveState " + entry.activityState.getClass());
            int i2 = i + 1;
            list[i] = bundle;
            i = i2;
        }
        outState.putParcelableArray("activity-state", list);
    }

    public ActivityState getTopState() {
        Utils.assertTrue(!this.mStack.isEmpty());
        return ((StateEntry) this.mStack.peek()).activityState;
    }

    public void onNavigationBarChanged(boolean show, int height) {
        if (!this.mStack.isEmpty()) {
            ActivityState as = getTopState();
            GalleryLog.d("StateManager", as.getClass() + " onNavigationBarChanged");
            as.onNavigationBarChanged(show, height);
        }
    }

    public void start() {
        if (!this.mIsStarted) {
            this.mIsStarted = true;
            if (!this.mStack.isEmpty()) {
                ActivityState as = getTopState();
                GalleryLog.d("StateManager", as.getClass() + " onStart");
                as.onStart();
            }
        }
    }

    public void stop() {
        if (this.mIsStarted) {
            this.mIsStarted = false;
            if (!this.mStack.isEmpty()) {
                ActivityState as = getTopState();
                GalleryLog.d("StateManager", as.getClass() + " onStop");
                as.onStop();
            }
        }
    }

    void setActivityResult() {
        if (this.mStack.size() == 1) {
            Activity activity = this.mHost.getActivity();
            if (this.mResult != null) {
                activity.setResult(this.mResult.resultCode, this.mResult.resultData);
            }
        }
    }

    public void onUserSelected(boolean selected) {
        ActivityState activityState = getTopState();
        if (activityState != null) {
            activityState.onUserSelected(selected);
        }
    }
}
