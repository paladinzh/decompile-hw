package com.android.systemui.volume;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.IRemoteVolumeController;
import android.media.IRemoteVolumeController.Stub;
import android.media.MediaMetadata;
import android.media.session.ISessionController;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.MediaController.PlaybackInfo;
import android.media.session.MediaSession.QueueItem;
import android.media.session.MediaSession.Token;
import android.media.session.MediaSessionManager;
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import fyusion.vislib.BuildConfig;
import java.io.PrintWriter;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MediaSessions {
    private static final String TAG = Util.logTag(MediaSessions.class);
    private final Callbacks mCallbacks;
    private final Context mContext;
    private final H mHandler;
    private boolean mInit;
    private final MediaSessionManager mMgr;
    private final Map<Token, MediaControllerRecord> mRecords = new HashMap();
    private final IRemoteVolumeController mRvc = new Stub() {
        public void remoteVolumeChanged(ISessionController session, int flags) throws RemoteException {
            MediaSessions.this.mHandler.obtainMessage(2, flags, 0, session).sendToTarget();
        }

        public void updateRemoteController(ISessionController session) throws RemoteException {
            MediaSessions.this.mHandler.obtainMessage(3, session).sendToTarget();
        }
    };
    private final OnActiveSessionsChangedListener mSessionsListener = new OnActiveSessionsChangedListener() {
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            MediaSessions.this.onActiveSessionsUpdatedH(controllers);
        }
    };

    public interface Callbacks {
        void onRemoteRemoved(Token token);

        void onRemoteUpdate(Token token, String str, PlaybackInfo playbackInfo);

        void onRemoteVolumeChanged(Token token, int i);
    }

    private final class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MediaSessions.this.onActiveSessionsUpdatedH(MediaSessions.this.mMgr.getActiveSessions(null));
                    return;
                case 2:
                    MediaSessions.this.onRemoteVolumeChangedH((ISessionController) msg.obj, msg.arg1);
                    return;
                case 3:
                    MediaSessions.this.onUpdateRemoteControllerH((ISessionController) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    private final class MediaControllerRecord extends Callback {
        private final MediaController controller;
        private String name;
        private boolean sentRemote;

        private MediaControllerRecord(MediaController controller) {
            this.controller = controller;
        }

        private String cb(String method) {
            return method + " " + this.controller.getPackageName() + " ";
        }

        public void onAudioInfoChanged(PlaybackInfo info) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onAudioInfoChanged") + Util.playbackInfoToString(info) + " sentRemote=" + this.sentRemote);
            }
            boolean remote = MediaSessions.isRemote(info);
            if (!remote && this.sentRemote) {
                MediaSessions.this.mCallbacks.onRemoteRemoved(this.controller.getSessionToken());
                this.sentRemote = false;
            } else if (remote) {
                MediaSessions.this.updateRemoteH(this.controller.getSessionToken(), this.name, info);
                this.sentRemote = true;
            }
        }

        public void onExtrasChanged(Bundle extras) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onExtrasChanged") + extras);
            }
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onMetadataChanged") + Util.mediaMetadataToString(metadata));
            }
        }

        public void onPlaybackStateChanged(PlaybackState state) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onPlaybackStateChanged") + Util.playbackStateToString(state));
            }
        }

        public void onQueueChanged(List<QueueItem> queue) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onQueueChanged") + queue);
            }
        }

        public void onQueueTitleChanged(CharSequence title) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onQueueTitleChanged") + title);
            }
        }

        public void onSessionDestroyed() {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onSessionDestroyed"));
            }
        }

        public void onSessionEvent(String event, Bundle extras) {
            if (D.BUG) {
                Log.d(MediaSessions.TAG, cb("onSessionEvent") + "event=" + event + " extras=" + extras);
            }
        }
    }

    public MediaSessions(Context context, Looper looper, Callbacks callbacks) {
        this.mContext = context;
        this.mHandler = new H(looper);
        this.mMgr = (MediaSessionManager) context.getSystemService("media_session");
        this.mCallbacks = callbacks;
    }

    public void dump(PrintWriter writer) {
        writer.println(getClass().getSimpleName() + " state:");
        writer.print("  mInit: ");
        writer.println(this.mInit);
        writer.print("  mRecords.size: ");
        writer.println(this.mRecords.size());
        int i = 0;
        try {
            for (MediaControllerRecord r : this.mRecords.values()) {
                i++;
                dump(i, writer, r.controller);
            }
        } catch (ConcurrentModificationException e) {
            if (D.BUG) {
                Log.d(TAG, "MediaSessions dump exception ");
            }
        }
    }

    public void init() {
        if (D.BUG) {
            Log.d(TAG, "init");
        }
        this.mMgr.addOnActiveSessionsChangedListener(this.mSessionsListener, null, this.mHandler);
        this.mInit = true;
        postUpdateSessions();
        this.mMgr.setRemoteVolumeController(this.mRvc);
    }

    protected void postUpdateSessions() {
        if (this.mInit) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void setVolume(Token token, int level) {
        MediaControllerRecord r = (MediaControllerRecord) this.mRecords.get(token);
        if (r == null) {
            Log.w(TAG, "setVolume: No record found for token " + token);
            return;
        }
        if (D.BUG) {
            Log.d(TAG, "Setting level to " + level);
        }
        r.controller.setVolumeTo(level, 0);
    }

    private void onRemoteVolumeChangedH(ISessionController session, int flags) {
        MediaController controller = new MediaController(this.mContext, session);
        if (D.BUG) {
            Log.d(TAG, "remoteVolumeChangedH " + controller.getPackageName() + " " + Util.audioManagerFlagsToString(flags));
        }
        this.mCallbacks.onRemoteVolumeChanged(controller.getSessionToken(), flags);
    }

    private void onUpdateRemoteControllerH(ISessionController session) {
        MediaController controller = null;
        if (session != null) {
            controller = new MediaController(this.mContext, session);
        }
        String packageName = controller != null ? controller.getPackageName() : null;
        if (D.BUG) {
            Log.d(TAG, "updateRemoteControllerH " + packageName);
        }
        postUpdateSessions();
    }

    protected void onActiveSessionsUpdatedH(List<MediaController> controllers) {
        if (D.BUG) {
            Log.d(TAG, "onActiveSessionsUpdatedH n=" + controllers.size());
        }
        Set<Token> toRemove = new HashSet(this.mRecords.keySet());
        for (MediaController controller : controllers) {
            MediaControllerRecord r;
            Token token = controller.getSessionToken();
            PlaybackInfo pi = controller.getPlaybackInfo();
            toRemove.remove(token);
            if (!this.mRecords.containsKey(token)) {
                r = new MediaControllerRecord(controller);
                r.name = getControllerName(controller);
                this.mRecords.put(token, r);
                controller.registerCallback(r, this.mHandler);
            }
            r = (MediaControllerRecord) this.mRecords.get(token);
            if (isRemote(pi)) {
                updateRemoteH(token, r.name, pi);
                r.sentRemote = true;
            }
        }
        for (Token t : toRemove) {
            r = (MediaControllerRecord) this.mRecords.get(t);
            r.controller.unregisterCallback(r);
            this.mRecords.remove(t);
            if (D.BUG) {
                Log.d(TAG, "Removing " + r.name + " sentRemote=" + r.sentRemote);
            }
            if (r.sentRemote) {
                this.mCallbacks.onRemoteRemoved(t);
                r.sentRemote = false;
            }
        }
    }

    private static boolean isRemote(PlaybackInfo pi) {
        return pi != null && pi.getPlaybackType() == 2;
    }

    protected String getControllerName(MediaController controller) {
        PackageManager pm = this.mContext.getPackageManager();
        String pkg = controller.getPackageName();
        try {
            String appLabel = Objects.toString(pm.getApplicationInfo(pkg, 0).loadLabel(pm), BuildConfig.FLAVOR).trim();
            if (appLabel.length() > 0) {
                return appLabel;
            }
            return pkg;
        } catch (NameNotFoundException e) {
        } catch (Exception e2) {
            Log.e(TAG, "system exception", e2);
        }
    }

    private void updateRemoteH(Token token, String name, PlaybackInfo pi) {
        if (this.mCallbacks != null) {
            this.mCallbacks.onRemoteUpdate(token, name, pi);
        }
    }

    private static void dump(int n, PrintWriter writer, MediaController c) {
        writer.println("  Controller " + n + ": " + c.getPackageName());
        Bundle extras = c.getExtras();
        long flags = c.getFlags();
        MediaMetadata mm = c.getMetadata();
        PlaybackInfo pi = c.getPlaybackInfo();
        PlaybackState playbackState = c.getPlaybackState();
        List<QueueItem> queue = c.getQueue();
        CharSequence queueTitle = c.getQueueTitle();
        int ratingType = c.getRatingType();
        PendingIntent sessionActivity = c.getSessionActivity();
        writer.println("    PlaybackState: " + Util.playbackStateToString(playbackState));
        writer.println("    PlaybackInfo: " + Util.playbackInfoToString(pi));
        if (mm != null) {
            writer.println("  MediaMetadata.desc=" + mm.getDescription());
        }
        writer.println("    RatingType: " + ratingType);
        writer.println("    Flags: " + flags);
        if (extras != null) {
            writer.println("    Extras:");
            for (String key : extras.keySet()) {
                writer.println("      " + key + "=" + extras.get(key));
            }
        }
        if (queueTitle != null) {
            writer.println("    QueueTitle: " + queueTitle);
        }
        if (!(queue == null || queue.isEmpty())) {
            writer.println("    Queue:");
            for (QueueItem qi : queue) {
                writer.println("      " + qi);
            }
        }
        if (pi != null) {
            writer.println("    sessionActivity: " + sessionActivity);
        }
    }
}
