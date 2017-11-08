package com.android.server.media;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IRemoteVolumeController;
import android.media.session.IActiveSessionsListener;
import android.media.session.ISession;
import android.media.session.ISessionCallback;
import android.media.session.ISessionController;
import android.media.session.ISessionManager.Stub;
import android.media.session.MediaSession.Token;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwMediaSessionStack;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MediaSessionService extends SystemService implements Monitor {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "MediaSessionService";
    private static final int WAKELOCK_TIMEOUT = 5000;
    private final ArrayList<MediaSessionRecord> mAllSessions = new ArrayList();
    private AudioManagerInternal mAudioManagerInternal;
    private IAudioService mAudioService;
    private ContentResolver mContentResolver;
    private int mCurrentUserId = -1;
    private final MessageHandler mHandler = new MessageHandler();
    final IBinder mICallback = new Binder();
    private KeyguardManager mKeyguardManager;
    private final Object mLock = new Object();
    private final WakeLock mMediaEventWakeLock;
    private final MediaSessionStack mPriorityStack;
    private IRemoteVolumeController mRvc;
    private final SessionManagerImpl mSessionManagerImpl = new SessionManagerImpl();
    private final ArrayList<SessionsListenerRecord> mSessionsListeners = new ArrayList();
    private SettingsObserver mSettingsObserver;
    private final SparseArray<UserRecord> mUserRecords = new SparseArray();

    final class MessageHandler extends Handler {
        private static final int MSG_SESSIONS_CHANGED = 1;

        MessageHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MediaSessionService.this.pushSessionsChanged(msg.arg1);
                    return;
                default:
                    return;
            }
        }

        public void post(int what, int arg1, int arg2) {
            obtainMessage(what, arg1, arg2).sendToTarget();
        }
    }

    class SessionManagerImpl extends Stub {
        private static final String EXTRA_WAKELOCK_ACQUIRED = "android.media.AudioService.WAKELOCK_ACQUIRED";
        private static final int WAKELOCK_RELEASE_ON_FINISHED = 1980;
        BroadcastReceiver mKeyEventDone = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        synchronized (MediaSessionService.this.mLock) {
                            if (extras.containsKey(SessionManagerImpl.EXTRA_WAKELOCK_ACQUIRED) && MediaSessionService.this.mMediaEventWakeLock.isHeld()) {
                                MediaSessionService.this.mMediaEventWakeLock.release();
                            }
                        }
                    }
                }
            }
        };
        private KeyEventWakeLockReceiver mKeyEventReceiver = new KeyEventWakeLockReceiver(MediaSessionService.this.mHandler);
        private boolean mVoiceButtonDown = false;
        private boolean mVoiceButtonHandled = false;

        class KeyEventWakeLockReceiver extends ResultReceiver implements Runnable, OnFinished {
            private final Handler mHandler;
            private int mLastTimeoutId = 0;
            private int mRefCount = 0;

            public KeyEventWakeLockReceiver(Handler handler) {
                super(handler);
                this.mHandler = handler;
            }

            public void onTimeout() {
                synchronized (MediaSessionService.this.mLock) {
                    if (this.mRefCount == 0) {
                        return;
                    }
                    this.mLastTimeoutId++;
                    this.mRefCount = 0;
                    releaseWakeLockLocked();
                }
            }

            public void aquireWakeLockLocked() {
                if (this.mRefCount == 0) {
                    MediaSessionService.this.mMediaEventWakeLock.acquire();
                }
                this.mRefCount++;
                this.mHandler.removeCallbacks(this);
                this.mHandler.postDelayed(this, 5000);
            }

            public void run() {
                onTimeout();
            }

            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode >= this.mLastTimeoutId) {
                    synchronized (MediaSessionService.this.mLock) {
                        if (this.mRefCount > 0) {
                            this.mRefCount--;
                            if (this.mRefCount == 0) {
                                releaseWakeLockLocked();
                            }
                        }
                    }
                }
            }

            private void releaseWakeLockLocked() {
                MediaSessionService.this.mMediaEventWakeLock.release();
                this.mHandler.removeCallbacks(this);
            }

            public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
                onReceiveResult(resultCode, null);
            }
        }

        private void startVoiceInput(boolean r8) {
            /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r7 = this;
            r4 = 0;
            r3 = 0;
            r5 = com.android.server.media.MediaSessionService.this;
            r5 = r5.getContext();
            r6 = "power";
            r2 = r5.getSystemService(r6);
            r2 = (android.os.PowerManager) r2;
            r5 = com.android.server.media.MediaSessionService.this;
            r5 = r5.mKeyguardManager;
            if (r5 == 0) goto L_0x0065;
        L_0x0019:
            r5 = com.android.server.media.MediaSessionService.this;
            r5 = r5.mKeyguardManager;
            r1 = r5.isKeyguardLocked();
        L_0x0023:
            if (r1 != 0) goto L_0x0067;
        L_0x0025:
            r5 = r2.isScreenOn();
            if (r5 == 0) goto L_0x0067;
        L_0x002b:
            r3 = new android.content.Intent;
            r4 = "android.speech.action.WEB_SEARCH";
            r3.<init>(r4);
            r4 = "MediaSessionService";
            r5 = "voice-based interactions: about to use ACTION_WEB_SEARCH";
            android.util.Log.i(r4, r5);
        L_0x003c:
            if (r8 == 0) goto L_0x0047;
        L_0x003e:
            r4 = com.android.server.media.MediaSessionService.this;
            r4 = r4.mMediaEventWakeLock;
            r4.acquire();
        L_0x0047:
            if (r3 == 0) goto L_0x0059;
        L_0x0049:
            r4 = 276824064; // 0x10800000 float:5.0487098E-29 double:1.3676926E-315;
            r3.setFlags(r4);	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r4 = com.android.server.media.MediaSessionService.this;	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r4 = r4.getContext();	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r5 = android.os.UserHandle.CURRENT;	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r4.startActivityAsUser(r3, r5);	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
        L_0x0059:
            if (r8 == 0) goto L_0x0064;
        L_0x005b:
            r4 = com.android.server.media.MediaSessionService.this;
            r4 = r4.mMediaEventWakeLock;
            r4.release();
        L_0x0064:
            return;
        L_0x0065:
            r1 = r4;
            goto L_0x0023;
        L_0x0067:
            r3 = new android.content.Intent;
            r5 = "android.speech.action.VOICE_SEARCH_HANDS_FREE";
            r3.<init>(r5);
            r5 = "android.speech.extras.EXTRA_SECURE";
            if (r1 == 0) goto L_0x007e;
        L_0x0074:
            r4 = com.android.server.media.MediaSessionService.this;
            r4 = r4.mKeyguardManager;
            r4 = r4.isKeyguardSecure();
        L_0x007e:
            r3.putExtra(r5, r4);
            r4 = "MediaSessionService";
            r5 = "voice-based interactions: about to use ACTION_VOICE_SEARCH_HANDS_FREE";
            android.util.Log.i(r4, r5);
            goto L_0x003c;
        L_0x008b:
            r0 = move-exception;
            r4 = "MediaSessionService";	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r5 = new java.lang.StringBuilder;	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r5.<init>();	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r6 = "No activity for search: ";	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r5 = r5.append(r6);	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r5 = r5.append(r0);	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            r5 = r5.toString();	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            android.util.Log.w(r4, r5);	 Catch:{ ActivityNotFoundException -> 0x008b, all -> 0x00b2 }
            if (r8 == 0) goto L_0x0064;
        L_0x00a8:
            r4 = com.android.server.media.MediaSessionService.this;
            r4 = r4.mMediaEventWakeLock;
            r4.release();
            goto L_0x0064;
        L_0x00b2:
            r4 = move-exception;
            if (r8 == 0) goto L_0x00be;
        L_0x00b5:
            r5 = com.android.server.media.MediaSessionService.this;
            r5 = r5.mMediaEventWakeLock;
            r5.release();
        L_0x00be:
            throw r4;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.media.MediaSessionService.SessionManagerImpl.startVoiceInput(boolean):void");
        }

        SessionManagerImpl() {
        }

        public ISession createSession(String packageName, ISessionCallback cb, String tag, int userId) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionService.this.enforcePackageName(packageName, uid);
                int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, false, true, "createSession", packageName);
                if (cb == null) {
                    throw new IllegalArgumentException("Controller callback cannot be null");
                }
                ISession sessionBinder = MediaSessionService.this.createSessionInternal(pid, uid, resolvedUserId, packageName, cb, tag).getSessionBinder();
                return sessionBinder;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public List<IBinder> getSessions(ComponentName componentName, int userId) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = verifySessionsRequest(componentName, userId, pid, uid);
                ArrayList<IBinder> binders = new ArrayList();
                synchronized (MediaSessionService.this.mLock) {
                    ArrayList<MediaSessionRecord> records = MediaSessionService.this.mPriorityStack.getActiveSessions(resolvedUserId);
                    int size = records.size();
                    for (int i = 0; i < size; i++) {
                        binders.add(((MediaSessionRecord) records.get(i)).getControllerBinder().asBinder());
                    }
                }
                Binder.restoreCallingIdentity(token);
                return binders;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void addSessionsListener(IActiveSessionsListener listener, ComponentName componentName, int userId) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = verifySessionsRequest(componentName, userId, pid, uid);
                synchronized (MediaSessionService.this.mLock) {
                    if (MediaSessionService.this.findIndexOfSessionsListenerLocked(listener) != -1) {
                        Log.w(MediaSessionService.TAG, "ActiveSessionsListener is already added, ignoring");
                    } else {
                        SessionsListenerRecord record = new SessionsListenerRecord(listener, componentName, resolvedUserId, pid, uid);
                        try {
                            listener.asBinder().linkToDeath(record, 0);
                            MediaSessionService.this.mSessionsListeners.add(record);
                            Binder.restoreCallingIdentity(token);
                        } catch (RemoteException e) {
                            Log.e(MediaSessionService.TAG, "ActiveSessionsListener is dead, ignoring it", e);
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void removeSessionsListener(IActiveSessionsListener listener) throws RemoteException {
            synchronized (MediaSessionService.this.mLock) {
                int index = MediaSessionService.this.findIndexOfSessionsListenerLocked(listener);
                if (index != -1) {
                    SessionsListenerRecord record = (SessionsListenerRecord) MediaSessionService.this.mSessionsListeners.remove(index);
                    try {
                        record.mListener.asBinder().unlinkToDeath(record, 0);
                    } catch (Exception e) {
                    }
                }
            }
        }

        public void dispatchMediaKeyEvent(KeyEvent keyEvent, boolean needWakeLock) {
            if (keyEvent == null || !KeyEvent.isMediaKey(keyEvent.getKeyCode())) {
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-media key event.");
                return;
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            Log.d(MediaSessionService.TAG, "dispatchMediaKeyEvent, pid=" + pid + ", uid=" + uid + ", event(action=" + keyEvent.getAction() + ", keyCode=" + keyEvent.getKeyCode() + ", flags=" + keyEvent.getFlags() + ", repeatCount=" + keyEvent.getRepeatCount() + ")");
            if (!isUserSetupComplete()) {
                Slog.i(MediaSessionService.TAG, "Not dispatching media key event because user setup is in progress.");
                Binder.restoreCallingIdentity(token);
            } else if (!isGlobalPriorityActive() || uid == 1000) {
                try {
                    synchronized (MediaSessionService.this.mLock) {
                        UserRecord ur = (UserRecord) MediaSessionService.this.mUserRecords.get(MediaSessionService.this.mCurrentUserId);
                        boolean useNotPlayingSessions = ur != null ? ur.mLastMediaButtonReceiver == null ? ur.mRestoredMediaButtonReceiver == null : false : true;
                        MediaSessionRecord session = MediaSessionService.this.mPriorityStack.getDefaultMediaButtonSession(MediaSessionService.this.mCurrentUserId, useNotPlayingSessions);
                        if (!MediaSessionService.this.mPriorityStack.mIsSupportMediaKey) {
                            Slog.i(MediaSessionService.TAG, "May be not support media key for session " + session);
                        }
                        if (isVoiceKey(keyEvent.getKeyCode())) {
                            handleVoiceKeyEventLocked(keyEvent, needWakeLock, session);
                        } else {
                            dispatchMediaKeyEventLocked(keyEvent, needWakeLock, session);
                        }
                    }
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                Slog.i(MediaSessionService.TAG, "Only the system can dispatch media key event to the global priority session.");
                Binder.restoreCallingIdentity(token);
            }
        }

        public void dispatchAdjustVolume(int suggestedStream, int delta, int flags) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (MediaSessionService.this.mLock) {
                    dispatchAdjustVolumeLocked(suggestedStream, delta, flags, MediaSessionService.this.mPriorityStack.getDefaultVolumeSession(MediaSessionService.this.mCurrentUserId));
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setRemoteVolumeController(IRemoteVolumeController rvc) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionService.this.enforceSystemUiPermission("listen for volume changes", pid, uid);
                MediaSessionService.this.mRvc = rvc;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isGlobalPriorityActive() {
            return MediaSessionService.this.mPriorityStack.isGlobalPriorityActive();
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (MediaSessionService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump MediaSessionService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            pw.println("MEDIA SESSION SERVICE (dumpsys media_session)");
            pw.println();
            synchronized (MediaSessionService.this.mLock) {
                int i;
                pw.println(MediaSessionService.this.mSessionsListeners.size() + " sessions listeners.");
                int count = MediaSessionService.this.mAllSessions.size();
                pw.println(count + " Sessions:");
                for (i = 0; i < count; i++) {
                    ((MediaSessionRecord) MediaSessionService.this.mAllSessions.get(i)).dump(pw, "");
                    pw.println();
                }
                MediaSessionService.this.mPriorityStack.dump(pw, "");
                pw.println("User Records:");
                count = MediaSessionService.this.mUserRecords.size();
                for (i = 0; i < count; i++) {
                    ((UserRecord) MediaSessionService.this.mUserRecords.get(MediaSessionService.this.mUserRecords.keyAt(i))).dumpLocked(pw, "");
                }
            }
        }

        private int verifySessionsRequest(ComponentName componentName, int userId, int pid, int uid) {
            String str = null;
            if (componentName != null) {
                str = componentName.getPackageName();
                MediaSessionService.this.enforcePackageName(str, uid);
            }
            int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, true, true, "getSessions", str);
            MediaSessionService.this.enforceMediaPermissions(componentName, pid, uid, resolvedUserId);
            return resolvedUserId;
        }

        private void dispatchAdjustVolumeLocked(int suggestedStream, int direction, int flags, MediaSessionRecord session) {
            if (MediaSessionService.DEBUG) {
                Log.d(MediaSessionService.TAG, "Adjusting session " + (session == null ? null : session.toString()) + " by " + direction + ". flags=" + flags + ", suggestedStream=" + suggestedStream);
            }
            boolean preferSuggestedStream = false;
            if (isValidLocalStreamType(suggestedStream) && AudioSystem.isStreamActive(suggestedStream, 0)) {
                preferSuggestedStream = true;
            }
            Log.d(MediaSessionService.TAG, "Adjusting session " + (session == null ? null : session.toString()) + " by " + direction + ". flags=" + flags + " suggestedStream=" + suggestedStream + " preferStream=" + preferSuggestedStream);
            if (session == null || preferSuggestedStream) {
                if (!((flags & 512) == 0 || AudioSystem.isStreamActive(3, 0))) {
                    if (suggestedStream == Integer.MIN_VALUE && AudioSystem.isStreamActive(0, 0)) {
                        Log.d(MediaSessionService.TAG, "set suggestedStream to voice call");
                        suggestedStream = 0;
                    } else {
                        Log.d(MediaSessionService.TAG, "No active session to adjust, skipping media only volume event");
                        return;
                    }
                }
                try {
                    MediaSessionService.this.mAudioService.adjustSuggestedStreamVolume(direction, suggestedStream, flags, MediaSessionService.this.getContext().getOpPackageName(), MediaSessionService.TAG);
                } catch (RemoteException e) {
                    Log.e(MediaSessionService.TAG, "Error adjusting default volume.", e);
                }
            } else {
                session.adjustVolume(direction, flags, MediaSessionService.this.getContext().getPackageName(), UserHandle.myUserId(), true);
            }
        }

        private void handleVoiceKeyEventLocked(KeyEvent keyEvent, boolean needWakeLock, MediaSessionRecord session) {
            if (session == null || !session.hasFlag(DumpState.DUMP_INSTALLS)) {
                int action = keyEvent.getAction();
                boolean isLongPress = (keyEvent.getFlags() & 128) != 0;
                if (action == 0) {
                    if (keyEvent.getRepeatCount() == 0) {
                        this.mVoiceButtonDown = true;
                        this.mVoiceButtonHandled = false;
                    } else if (this.mVoiceButtonDown && !this.mVoiceButtonHandled && isLongPress) {
                        this.mVoiceButtonHandled = true;
                        startVoiceInput(needWakeLock);
                    }
                } else if (action == 1 && this.mVoiceButtonDown) {
                    this.mVoiceButtonDown = false;
                    if (!(this.mVoiceButtonHandled || keyEvent.isCanceled())) {
                        dispatchMediaKeyEventLocked(KeyEvent.changeAction(keyEvent, 0), needWakeLock, session);
                        dispatchMediaKeyEventLocked(keyEvent, needWakeLock, session);
                    }
                }
                return;
            }
            dispatchMediaKeyEventLocked(keyEvent, needWakeLock, session);
        }

        private void dispatchMediaKeyEventLocked(KeyEvent keyEvent, boolean needWakeLock, MediaSessionRecord session) {
            if (session != null) {
                Log.d(MediaSessionService.TAG, "Sending media key to " + session.toString());
                if (keyEvent.getAction() == 0) {
                    LogPower.push(148, "mediakey", session.mPackageName, Integer.toString(session.mOwnerPid), new String[]{String.valueOf(keyEvent.getKeyCode())});
                }
                if (needWakeLock) {
                    this.mKeyEventReceiver.aquireWakeLockLocked();
                }
                session.sendMediaButton(keyEvent, needWakeLock ? this.mKeyEventReceiver.mLastTimeoutId : -1, this.mKeyEventReceiver, MediaSessionService.this.getContext().getApplicationInfo().uid, MediaSessionService.this.getContext().getPackageName());
                return;
            }
            UserRecord user = (UserRecord) MediaSessionService.this.mUserRecords.get(MediaSessionService.this.mCurrentUserId);
            if (user == null || (user.mLastMediaButtonReceiver == null && user.mRestoredMediaButtonReceiver == null)) {
                Log.d(MediaSessionService.TAG, "Sending media key ordered broadcast");
                if (needWakeLock) {
                    MediaSessionService.this.mMediaEventWakeLock.acquire();
                }
                Intent keyIntent = new Intent("android.intent.action.MEDIA_BUTTON", null);
                keyIntent.addFlags(268435456);
                keyIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                if (needWakeLock) {
                    keyIntent.putExtra(EXTRA_WAKELOCK_ACQUIRED, WAKELOCK_RELEASE_ON_FINISHED);
                }
                if (checkPackage("com.android.mediacenter")) {
                    Log.d(MediaSessionService.TAG, "Sending media key to mediacenter apk");
                    keyIntent.setPackage("com.android.mediacenter");
                    keyIntent.addFlags(32);
                }
                MediaSessionService.this.getContext().sendOrderedBroadcastAsUser(keyIntent, UserHandle.CURRENT, null, this.mKeyEventDone, MediaSessionService.this.mHandler, -1, null, null);
                return;
            }
            Log.d(MediaSessionService.TAG, "Sending media key to last known PendingIntent " + user.mLastMediaButtonReceiver + " or restored Intent " + user.mRestoredMediaButtonReceiver);
            if (needWakeLock) {
                this.mKeyEventReceiver.aquireWakeLockLocked();
            }
            Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
            mediaButtonIntent.addFlags(268435456);
            mediaButtonIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
            try {
                if (user.mLastMediaButtonReceiver != null) {
                    user.mLastMediaButtonReceiver.send(MediaSessionService.this.getContext(), needWakeLock ? this.mKeyEventReceiver.mLastTimeoutId : -1, mediaButtonIntent, this.mKeyEventReceiver, MediaSessionService.this.mHandler);
                    return;
                }
                mediaButtonIntent.setComponent(user.mRestoredMediaButtonReceiver);
                MediaSessionService.this.getContext().sendBroadcastAsUser(mediaButtonIntent, new UserHandle(MediaSessionService.this.mCurrentUserId));
            } catch (CanceledException e) {
                Log.i(MediaSessionService.TAG, "Error sending key event to media button receiver " + user.mLastMediaButtonReceiver, e);
            }
        }

        private boolean checkPackage(String packageName) {
            if (packageName == null || "".equals(packageName)) {
                return false;
            }
            try {
                ApplicationInfo info = MediaSessionService.this.getContext().getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_PREFERRED_XML);
                return true;
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        private boolean isVoiceKey(int keyCode) {
            return keyCode == 79;
        }

        private boolean isUserSetupComplete() {
            return Secure.getIntForUser(MediaSessionService.this.getContext().getContentResolver(), "user_setup_complete", 0, -2) != 0;
        }

        private boolean isValidLocalStreamType(int streamType) {
            if (streamType < 0 || streamType > 5) {
                return false;
            }
            return true;
        }
    }

    final class SessionsListenerRecord implements DeathRecipient {
        private final ComponentName mComponentName;
        private final IActiveSessionsListener mListener;
        private final int mPid;
        private final int mUid;
        private final int mUserId;

        public SessionsListenerRecord(IActiveSessionsListener listener, ComponentName componentName, int userId, int pid, int uid) {
            this.mListener = listener;
            this.mComponentName = componentName;
            this.mUserId = userId;
            this.mPid = pid;
            this.mUid = uid;
        }

        public void binderDied() {
            synchronized (MediaSessionService.this.mLock) {
                MediaSessionService.this.mSessionsListeners.remove(this);
            }
        }
    }

    final class SettingsObserver extends ContentObserver {
        private final Uri mSecureSettingsUri;

        private SettingsObserver() {
            super(null);
            this.mSecureSettingsUri = Secure.getUriFor("enabled_notification_listeners");
        }

        private void observe() {
            MediaSessionService.this.mContentResolver.registerContentObserver(this.mSecureSettingsUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            MediaSessionService.this.updateActiveSessionListeners();
        }
    }

    final class UserRecord {
        private final Context mContext;
        private PendingIntent mLastMediaButtonReceiver;
        private ComponentName mRestoredMediaButtonReceiver;
        private final ArrayList<MediaSessionRecord> mSessions = new ArrayList();
        private final int mUserId;

        public UserRecord(Context context, int userId) {
            this.mContext = context;
            this.mUserId = userId;
            restoreMediaButtonReceiver();
        }

        public void startLocked() {
        }

        public void stopLocked() {
        }

        public void destroyLocked() {
            for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                MediaSessionService.this.destroySessionLocked((MediaSessionRecord) this.mSessions.get(i));
            }
        }

        public ArrayList<MediaSessionRecord> getSessionsLocked() {
            return this.mSessions;
        }

        public void addSessionLocked(MediaSessionRecord session) {
            this.mSessions.add(session);
        }

        public void removeSessionLocked(MediaSessionRecord session) {
            this.mSessions.remove(session);
        }

        public void dumpLocked(PrintWriter pw, String prefix) {
            pw.println(prefix + "Record for user " + this.mUserId);
            String indent = prefix + "  ";
            pw.println(indent + "MediaButtonReceiver:" + this.mLastMediaButtonReceiver);
            pw.println(indent + "Restored ButtonReceiver:" + this.mRestoredMediaButtonReceiver);
            int size = this.mSessions.size();
            pw.println(indent + size + " Sessions:");
            for (int i = 0; i < size; i++) {
                pw.println(indent + ((MediaSessionRecord) this.mSessions.get(i)).toString());
            }
        }

        private void restoreMediaButtonReceiver() {
            String receiverName = Secure.getStringForUser(MediaSessionService.this.mContentResolver, "media_button_receiver", -2);
            if (!TextUtils.isEmpty(receiverName)) {
                ComponentName eventReceiver = ComponentName.unflattenFromString(receiverName);
                if (eventReceiver != null) {
                    this.mRestoredMediaButtonReceiver = eventReceiver;
                }
            }
        }
    }

    public MediaSessionService(Context context) {
        super(context);
        IHwMediaSessionStack imss = HwServiceFactory.getHuaweiMediaSessionStack();
        if (imss != null) {
            this.mPriorityStack = imss.getInstance(context);
        } else {
            this.mPriorityStack = new MediaSessionStack();
        }
        this.mMediaEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "handleMediaEvent");
    }

    public void onStart() {
        publishBinderService("media_session", this.mSessionManagerImpl);
        Watchdog.getInstance().addMonitor(this);
        this.mKeyguardManager = (KeyguardManager) getContext().getSystemService("keyguard");
        this.mAudioService = getAudioService();
        this.mAudioManagerInternal = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
        this.mContentResolver = getContext().getContentResolver();
        this.mSettingsObserver = new SettingsObserver();
        this.mSettingsObserver.observe();
        updateUser();
    }

    private IAudioService getAudioService() {
        return IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
    }

    public void updateSession(MediaSessionRecord record) {
        synchronized (this.mLock) {
            if (this.mAllSessions.contains(record)) {
                this.mPriorityStack.onSessionStateChange(record);
                this.mHandler.post(1, record.getUserId(), 0);
                return;
            }
            Log.d(TAG, "Unknown session updated. Ignoring.");
        }
    }

    public void notifyRemoteVolumeChanged(int flags, MediaSessionRecord session) {
        if (this.mRvc != null) {
            try {
                this.mRvc.remoteVolumeChanged(session.getControllerBinder(), flags);
            } catch (Exception e) {
                Log.wtf(TAG, "Error sending volume change to system UI.", e);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onSessionPlaystateChange(MediaSessionRecord record, int oldState, int newState) {
        synchronized (this.mLock) {
            if (this.mAllSessions.contains(record)) {
                boolean updateSessions = this.mPriorityStack.onPlaystateChange(record, oldState, newState);
            } else {
                Log.d(TAG, "Unknown session changed playback state. Ignoring.");
            }
        }
    }

    public void onSessionPlaybackTypeChanged(MediaSessionRecord record) {
        synchronized (this.mLock) {
            if (this.mAllSessions.contains(record)) {
                pushRemoteVolumeUpdateLocked(record.getUserId());
                return;
            }
            Log.d(TAG, "Unknown session changed playback type. Ignoring.");
        }
    }

    public void onStartUser(int userHandle) {
        updateUser();
    }

    public void onSwitchUser(int userHandle) {
        updateUser();
    }

    public void onStopUser(int userHandle) {
        synchronized (this.mLock) {
            UserRecord user = (UserRecord) this.mUserRecords.get(userHandle);
            if (user != null) {
                destroyUserLocked(user);
            }
        }
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    protected void enforcePhoneStatePermission(int pid, int uid) {
        if (getContext().checkPermission("android.permission.MODIFY_PHONE_STATE", pid, uid) != 0) {
            throw new SecurityException("Must hold the MODIFY_PHONE_STATE permission.");
        }
    }

    void sessionDied(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    void destroySession(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    private void updateUser() {
        synchronized (this.mLock) {
            int userId = ActivityManager.getCurrentUser();
            if (this.mCurrentUserId != userId) {
                int oldUserId = this.mCurrentUserId;
                this.mCurrentUserId = userId;
                UserRecord oldUser = (UserRecord) this.mUserRecords.get(oldUserId);
                if (oldUser != null) {
                    oldUser.stopLocked();
                }
                getOrCreateUser(userId).startLocked();
            }
        }
    }

    private void updateActiveSessionListeners() {
        synchronized (this.mLock) {
            SessionsListenerRecord listener;
            for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                listener = (SessionsListenerRecord) this.mSessionsListeners.get(i);
                try {
                    enforceMediaPermissions(listener.mComponentName, listener.mPid, listener.mUid, listener.mUserId);
                } catch (SecurityException e) {
                    Log.i(TAG, "ActiveSessionsListener " + listener.mComponentName + " is no longer authorized. Disconnecting.");
                    this.mSessionsListeners.remove(i);
                    try {
                        listener.mListener.onActiveSessionsChanged(new ArrayList());
                    } catch (Exception e2) {
                    }
                }
            }
        }
    }

    private void destroyUserLocked(UserRecord user) {
        user.stopLocked();
        user.destroyLocked();
        this.mUserRecords.remove(user.mUserId);
    }

    private void destroySessionLocked(MediaSessionRecord session) {
        if (DEBUG) {
            Log.d(TAG, "Destroying session : " + session.toString());
        }
        UserRecord user = (UserRecord) this.mUserRecords.get(session.getUserId());
        if (user != null) {
            user.removeSessionLocked(session);
        }
        this.mPriorityStack.removeSession(session);
        this.mAllSessions.remove(session);
        try {
            session.getCallback().asBinder().unlinkToDeath(session, 0);
        } catch (Exception e) {
        }
        session.onDestroy();
        this.mHandler.post(1, session.getUserId(), 0);
    }

    private void enforcePackageName(String packageName, int uid) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName may not be empty");
        }
        String[] packages = getContext().getPackageManager().getPackagesForUid(uid);
        int packageCount = packages.length;
        int i = 0;
        while (i < packageCount) {
            if (!packageName.equals(packages[i])) {
                i++;
            } else {
                return;
            }
        }
        throw new IllegalArgumentException("packageName is not owned by the calling process");
    }

    private void enforceMediaPermissions(ComponentName compName, int pid, int uid, int resolvedUserId) {
        if (!isCurrentVolumeController(uid) && getContext().checkPermission("android.permission.MEDIA_CONTENT_CONTROL", pid, uid) != 0 && !isEnabledNotificationListener(compName, UserHandle.getUserId(uid), resolvedUserId)) {
            throw new SecurityException("Missing permission to control media.");
        }
    }

    private boolean isCurrentVolumeController(int uid) {
        if (this.mAudioManagerInternal != null) {
            int vcuid = this.mAudioManagerInternal.getVolumeControllerUid();
            if (vcuid > 0 && uid == vcuid) {
                return true;
            }
        }
        return false;
    }

    private void enforceSystemUiPermission(String action, int pid, int uid) {
        if (!isCurrentVolumeController(uid) && getContext().checkPermission("android.permission.STATUS_BAR_SERVICE", pid, uid) != 0) {
            throw new SecurityException("Only system ui may " + action);
        }
    }

    private boolean isEnabledNotificationListener(ComponentName compName, int userId, int forUserId) {
        if (userId != forUserId) {
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "Checking if enabled notification listener " + compName);
        }
        if (compName != null) {
            String enabledNotifListeners = Secure.getStringForUser(this.mContentResolver, "enabled_notification_listeners", userId);
            if (enabledNotifListeners != null) {
                String[] components = enabledNotifListeners.split(":");
                int i = 0;
                while (i < components.length) {
                    ComponentName component = ComponentName.unflattenFromString(components[i]);
                    if (component == null || !compName.equals(component)) {
                        i++;
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "ok to get sessions: " + component + " is authorized notification listener");
                        }
                        return true;
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "not ok to get sessions, " + compName + " is not in list of ENABLED_NOTIFICATION_LISTENERS for user " + userId);
            }
        }
        return false;
    }

    private MediaSessionRecord createSessionInternal(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) throws RemoteException {
        MediaSessionRecord createSessionLocked;
        synchronized (this.mLock) {
            createSessionLocked = createSessionLocked(callerPid, callerUid, userId, callerPackageName, cb, tag);
        }
        return createSessionLocked;
    }

    private MediaSessionRecord createSessionLocked(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) {
        MediaSessionRecord session = new MediaSessionRecord(callerPid, callerUid, userId, callerPackageName, cb, tag, this, this.mHandler);
        try {
            cb.asBinder().linkToDeath(session, 0);
            this.mAllSessions.add(session);
            this.mPriorityStack.addSession(session);
            getOrCreateUser(userId).addSessionLocked(session);
            this.mHandler.post(1, userId, 0);
            if (DEBUG) {
                Log.d(TAG, "Created session for package " + callerPackageName + " with tag " + tag);
            }
            return session;
        } catch (RemoteException e) {
            throw new RuntimeException("Media Session owner died prematurely.", e);
        }
    }

    private UserRecord getOrCreateUser(int userId) {
        UserRecord user = (UserRecord) this.mUserRecords.get(userId);
        if (user != null) {
            return user;
        }
        user = new UserRecord(getContext(), userId);
        this.mUserRecords.put(userId, user);
        return user;
    }

    private int findIndexOfSessionsListenerLocked(IActiveSessionsListener listener) {
        for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
            if (((SessionsListenerRecord) this.mSessionsListeners.get(i)).mListener.asBinder() == listener.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    private void pushSessionsChanged(int userId) {
        synchronized (this.mLock) {
            int i;
            List<MediaSessionRecord> records = this.mPriorityStack.getActiveSessions(userId);
            int size = records.size();
            if (size <= 0 || !((MediaSessionRecord) records.get(0)).isPlaybackActive(false)) {
                UserRecord user = (UserRecord) this.mUserRecords.get(userId);
                if (user != null) {
                    user.mLastMediaButtonReceiver = null;
                }
            } else {
                rememberMediaButtonReceiverLocked((MediaSessionRecord) records.get(0));
            }
            ArrayList<Token> tokens = new ArrayList();
            for (i = 0; i < size; i++) {
                tokens.add(new Token(((MediaSessionRecord) records.get(i)).getControllerBinder()));
            }
            pushRemoteVolumeUpdateLocked(userId);
            for (i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                SessionsListenerRecord record = (SessionsListenerRecord) this.mSessionsListeners.get(i);
                if (record.mUserId == -1 || record.mUserId == userId) {
                    try {
                        record.mListener.onActiveSessionsChanged(tokens);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Dead ActiveSessionsListener in pushSessionsChanged, removing", e);
                        this.mSessionsListeners.remove(i);
                    }
                }
            }
        }
    }

    private void pushRemoteVolumeUpdateLocked(int userId) {
        ISessionController iSessionController = null;
        if (this.mRvc != null) {
            try {
                MediaSessionRecord record = this.mPriorityStack.getDefaultRemoteSession(userId);
                IRemoteVolumeController iRemoteVolumeController = this.mRvc;
                if (record != null) {
                    iSessionController = record.getControllerBinder();
                }
                iRemoteVolumeController.updateRemoteController(iSessionController);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error sending default remote volume to sys ui.", e);
            }
        }
    }

    private void rememberMediaButtonReceiverLocked(MediaSessionRecord record) {
        PendingIntent receiver = record.getMediaButtonReceiver();
        UserRecord user = (UserRecord) this.mUserRecords.get(record.getUserId());
        if (receiver != null && user != null) {
            user.mLastMediaButtonReceiver = receiver;
            ComponentName component = receiver.getIntent().getComponent();
            if (component != null && record.getPackageName().equals(component.getPackageName())) {
                Secure.putStringForUser(this.mContentResolver, "media_button_receiver", component.flattenToString(), record.getUserId());
            }
        }
    }
}
