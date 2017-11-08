package com.android.systemui.recents;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.recents.IRecentsSystemUserCallbacks.Stub;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.misc.ForegroundThread;

public class RecentsSystemUser extends Stub {
    private Context mContext;
    private RecentsImpl mImpl;
    private final SparseArray<IRecentsNonSystemUserCallbacks> mNonSystemUserRecents = new SparseArray();

    final /* synthetic */ class -void_startScreenPinning_int_taskId_LambdaImpl0 implements Runnable {
        private /* synthetic */ int val$taskId;
        private /* synthetic */ RecentsSystemUser val$this;

        public /* synthetic */ -void_startScreenPinning_int_taskId_LambdaImpl0(RecentsSystemUser recentsSystemUser, int i) {
            this.val$this = recentsSystemUser;
            this.val$taskId = i;
        }

        public void run() {
            this.val$this.-com_android_systemui_recents_RecentsSystemUser_lambda$2(this.val$taskId);
        }
    }

    final /* synthetic */ class -void_updateRecentsVisibility_boolean_visible_LambdaImpl0 implements Runnable {
        private /* synthetic */ RecentsSystemUser val$this;
        private /* synthetic */ boolean val$visible;

        public /* synthetic */ -void_updateRecentsVisibility_boolean_visible_LambdaImpl0(RecentsSystemUser recentsSystemUser, boolean z) {
            this.val$this = recentsSystemUser;
            this.val$visible = z;
        }

        public void run() {
            this.val$this.-com_android_systemui_recents_RecentsSystemUser_lambda$1(this.val$visible);
        }
    }

    public RecentsSystemUser(Context context, RecentsImpl impl) {
        this.mContext = context;
        this.mImpl = impl;
    }

    public void registerNonSystemUserCallbacks(IBinder nonSystemUserCallbacks, final int userId) {
        try {
            final IRecentsNonSystemUserCallbacks callback = IRecentsNonSystemUserCallbacks.Stub.asInterface(nonSystemUserCallbacks);
            nonSystemUserCallbacks.linkToDeath(new DeathRecipient() {
                public void binderDied() {
                    RecentsSystemUser.this.mNonSystemUserRecents.removeAt(RecentsSystemUser.this.mNonSystemUserRecents.indexOfValue(callback));
                    EventLog.writeEvent(36060, new Object[]{Integer.valueOf(5), Integer.valueOf(userId)});
                }
            }, 0);
            this.mNonSystemUserRecents.put(userId, callback);
            EventLog.writeEvent(36060, new Object[]{Integer.valueOf(4), Integer.valueOf(userId)});
        } catch (RemoteException e) {
            Log.e("RecentsSystemUser", "Failed to register NonSystemUserCallbacks", e);
        }
    }

    public IRecentsNonSystemUserCallbacks getNonSystemUserRecentsForUser(int userId) {
        return (IRecentsNonSystemUserCallbacks) this.mNonSystemUserRecents.get(userId);
    }

    public void updateRecentsVisibility(boolean visible) {
        ForegroundThread.getHandler().post(new -void_updateRecentsVisibility_boolean_visible_LambdaImpl0(this, visible));
    }

    /* synthetic */ void -com_android_systemui_recents_RecentsSystemUser_lambda$1(boolean visible) {
        this.mImpl.onVisibilityChanged(this.mContext, visible);
    }

    public void startScreenPinning(int taskId) {
        ForegroundThread.getHandler().post(new -void_startScreenPinning_int_taskId_LambdaImpl0(this, taskId));
    }

    /* synthetic */ void -com_android_systemui_recents_RecentsSystemUser_lambda$2(int taskId) {
        this.mImpl.onStartScreenPinning(this.mContext, taskId);
    }

    public void sendRecentsDrawnEvent() {
        EventBus.getDefault().post(new RecentsDrawnEvent());
    }

    public void sendDockingTopTaskEvent(int dragMode, Rect initialRect) throws RemoteException {
        EventBus.getDefault().post(new DockedTopTaskEvent(dragMode, initialRect));
    }

    public void sendLaunchRecentsEvent() throws RemoteException {
        EventBus.getDefault().post(new RecentsActivityStartingEvent());
    }
}
