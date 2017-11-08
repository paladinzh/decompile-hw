package com.android.systemui.volume;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IVolumeController.Stub;
import android.media.VolumePolicy;
import android.media.session.MediaController.PlaybackInfo;
import android.media.session.MediaSession.Token;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.service.notification.Condition;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.R;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.utils.HwLog;
import com.android.systemui.volume.HwVolumeSilentView.HwSilentViewCallback;
import com.huawei.cust.HwCustUtils;
import fyusion.vislib.BuildConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

public class VolumeDialogController {
    private static final int[] STREAMS = new int[]{4, 6, 8, 3, 5, 2, 1, 7, 9, 0};
    private static final String TAG = Util.logTag(VolumeDialogController.class);
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private final AudioManager mAudio;
    private final C mCallbacks = new C();
    private final ComponentName mComponent;
    private final Context mContext;
    private HwCustVolumeDialogController mCustVolumeDialogController;
    private boolean mDestroyed;
    private boolean mEnabled;
    private final boolean mHasVibrator;
    private final MediaSessions mMediaSessions;
    private final MediaSessionsCallbacks mMediaSessionsCallbacksW = new MediaSessionsCallbacks();
    private final NotificationManager mNoMan;
    private final SettingObserver mObserver;
    private final Receiver mReceiver = new Receiver();
    private boolean mShowDndTile = true;
    private HwSilentViewCallback mSilentViewCallback;
    private final State mState = new State();
    private final String[] mStreamTitles;
    private final Vibrator mVibrator;
    private final VC mVolumeController = new VC();
    private VolumePolicy mVolumePolicy;
    private final W mWorker;
    private final HandlerThread mWorkerThread;

    public interface Callbacks {
        void onConfigurationChanged();

        void onDismissRequested(int i);

        void onLayoutDirectionChanged(int i);

        void onScreenOff();

        void onShowRequested(int i);

        void onShowSafetyWarning(int i);

        void onShowSilentHint();

        void onShowVibrateHint();

        void onStateChanged(State state);
    }

    private final class C implements Callbacks {
        private final HashMap<Callbacks, Handler> mCallbackMap;

        private C() {
            this.mCallbackMap = new HashMap();
        }

        public void add(Callbacks callback, Handler handler) {
            if (callback == null || handler == null) {
                throw new IllegalArgumentException();
            }
            this.mCallbackMap.put(callback, handler);
        }

        public void onShowRequested(final int reason) {
            HwLog.i(VolumeDialogController.TAG, "C onShowRequested reason = " + reason);
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onShowRequested(reason);
                    }
                });
            }
        }

        public void onDismissRequested(final int reason) {
            HwLog.i(VolumeDialogController.TAG, "C onDismissRequested reason = " + reason);
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onDismissRequested(reason);
                    }
                });
            }
        }

        public void onStateChanged(State state) {
            HwLog.i(VolumeDialogController.TAG, "C onStateChanged state = " + state);
            long time = System.currentTimeMillis();
            final State copy = state.copy();
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onStateChanged(copy);
                    }
                });
            }
            Events.writeState(time, copy);
        }

        public void onLayoutDirectionChanged(final int layoutDirection) {
            HwLog.i(VolumeDialogController.TAG, "C onLayoutDirectionChanged layoutDirection = " + layoutDirection);
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onLayoutDirectionChanged(layoutDirection);
                    }
                });
            }
        }

        public void onConfigurationChanged() {
            HwLog.i(VolumeDialogController.TAG, "C onConfigurationChanged ");
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onConfigurationChanged();
                    }
                });
            }
        }

        public void onShowVibrateHint() {
            HwLog.i(VolumeDialogController.TAG, "C onShowVibrateHint ");
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onShowVibrateHint();
                    }
                });
            }
        }

        public void onShowSilentHint() {
            HwLog.i(VolumeDialogController.TAG, "C onShowSilentHint ");
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onShowSilentHint();
                    }
                });
            }
        }

        public void onScreenOff() {
            HwLog.i(VolumeDialogController.TAG, "C onScreenOff ");
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onScreenOff();
                    }
                });
            }
        }

        public void onShowSafetyWarning(final int flags) {
            HwLog.i(VolumeDialogController.TAG, "C onShowSafetyWarning flags = " + flags);
            for (final Entry<Callbacks, Handler> entry : this.mCallbackMap.entrySet()) {
                ((Handler) entry.getValue()).post(new Runnable() {
                    public void run() {
                        ((Callbacks) entry.getKey()).onShowSafetyWarning(flags);
                    }
                });
            }
        }
    }

    private final class MediaSessionsCallbacks implements com.android.systemui.volume.MediaSessions.Callbacks {
        private int mNextStream;
        private final HashMap<Token, Integer> mRemoteStreams;

        private MediaSessionsCallbacks() {
            this.mRemoteStreams = new HashMap();
            this.mNextStream = 100;
        }

        public void onRemoteUpdate(Token token, String name, PlaybackInfo pi) {
            if (!this.mRemoteStreams.containsKey(token)) {
                this.mRemoteStreams.put(token, Integer.valueOf(this.mNextStream));
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onRemoteUpdate: " + name + " is stream " + this.mNextStream);
                }
                this.mNextStream++;
            }
            if (this.mRemoteStreams.get(token) == null) {
                HwLog.e(VolumeDialogController.TAG, "onRemoteUpdate mRemoteStreams.get(token) == null");
                return;
            }
            int stream = ((Integer) this.mRemoteStreams.get(token)).intValue();
            boolean z = VolumeDialogController.this.mState.states.indexOfKey(stream) < 0;
            StreamState ss = VolumeDialogController.this.streamStateW(stream);
            ss.dynamic = true;
            ss.levelMin = 0;
            ss.levelMax = pi.getMaxVolume();
            if (ss.level != pi.getCurrentVolume()) {
                ss.level = pi.getCurrentVolume();
                z = true;
            }
            if (!Objects.equals(ss.name, name)) {
                ss.name = name;
                z = true;
            }
            if (z) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onRemoteUpdate: " + name + ": " + ss.level + " of " + ss.levelMax);
                }
                VolumeDialogController.this.mCallbacks.onStateChanged(VolumeDialogController.this.mState);
            }
        }

        public void onRemoteVolumeChanged(Token token, int flags) {
            if (this.mRemoteStreams.get(token) == null) {
                HwLog.e(VolumeDialogController.TAG, "onRemoteVolumeChanged mRemoteStreams.get(token) == null");
                return;
            }
            int stream = ((Integer) this.mRemoteStreams.get(token)).intValue();
            boolean showUI = (flags & 1) != 0;
            boolean changed = VolumeDialogController.this.updateActiveStreamW(stream);
            if (showUI) {
                changed |= VolumeDialogController.this.checkRoutedToBluetoothW(3);
            }
            if (changed) {
                VolumeDialogController.this.mCallbacks.onStateChanged(VolumeDialogController.this.mState);
            }
            if (showUI) {
                VolumeDialogController.this.mCallbacks.onShowRequested(2);
            }
        }

        public void onRemoteRemoved(Token token) {
            if (this.mRemoteStreams.get(token) == null) {
                HwLog.e(VolumeDialogController.TAG, "onRemoteRemoved mRemoteStreams.get(token) == null");
                return;
            }
            int stream = ((Integer) this.mRemoteStreams.get(token)).intValue();
            VolumeDialogController.this.mState.states.remove(stream);
            if (VolumeDialogController.this.mState.activeStream == stream) {
                VolumeDialogController.this.updateActiveStreamW(-1);
            }
            VolumeDialogController.this.mCallbacks.onStateChanged(VolumeDialogController.this.mState);
        }

        public void setStreamVolume(int stream, int level) {
            Token t = findToken(stream);
            if (t == null) {
                Log.w(VolumeDialogController.TAG, "setStreamVolume: No token found for stream: " + stream);
            } else {
                VolumeDialogController.this.mMediaSessions.setVolume(t, level);
            }
        }

        private Token findToken(int stream) {
            for (Entry<Token, Integer> entry : this.mRemoteStreams.entrySet()) {
                if (((Integer) entry.getValue()).equals(Integer.valueOf(stream))) {
                    return (Token) entry.getKey();
                }
            }
            return null;
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.media.VOLUME_CHANGED_ACTION");
            filter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
            filter.addAction("android.media.RINGER_MODE_CHANGED");
            filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
            filter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
            filter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            VolumeDialogController.this.mContext.registerReceiver(this, filter, null, VolumeDialogController.this.mWorker);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean changed = false;
            int stream;
            if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int level = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                int oldLevel = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive VOLUME_CHANGED_ACTION stream=" + stream + " level=" + level + " oldLevel=" + oldLevel);
                }
                changed = VolumeDialogController.this.updateStreamLevelW(stream, level);
            } else if (action.equals("android.media.STREAM_DEVICES_CHANGED_ACTION")) {
                stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                int devices = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", -1);
                int oldDevices = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive STREAM_DEVICES_CHANGED_ACTION stream=" + stream + " devices=" + devices + " oldDevices=" + oldDevices);
                }
                changed = VolumeDialogController.this.checkRoutedToBluetoothW(stream) | VolumeDialogController.this.onVolumeChangedW(stream, 0);
            } else if (action.equals("android.media.RINGER_MODE_CHANGED")) {
                rm = intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive RINGER_MODE_CHANGED_ACTION rm=" + Util.ringerModeToString(rm));
                }
                changed = VolumeDialogController.this.updateRingerModeExternalW(rm);
            } else if (action.equals("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                rm = intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive INTERNAL_RINGER_MODE_CHANGED_ACTION rm=" + Util.ringerModeToString(rm));
                }
                changed = VolumeDialogController.this.updateRingerModeInternalW(rm);
            } else if (action.equals("android.media.STREAM_MUTE_CHANGED_ACTION")) {
                stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                boolean muted = intent.getBooleanExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", false);
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive STREAM_MUTE_CHANGED_ACTION stream=" + stream + " muted=" + muted);
                }
                changed = VolumeDialogController.this.updateStreamMuteW(stream, muted);
            } else if (action.equals("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                }
                changed = VolumeDialogController.this.updateEffectsSuppressorW(VolumeDialogController.this.mNoMan.getEffectsSuppressor());
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive ACTION_CONFIGURATION_CHANGED");
                }
                VolumeDialogController.this.mCallbacks.onConfigurationChanged();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive ACTION_SCREEN_OFF");
                }
                VolumeDialogController.this.mCallbacks.onScreenOff();
            } else if (action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                if (D.BUG) {
                    Log.d(VolumeDialogController.TAG, "onReceive ACTION_CLOSE_SYSTEM_DIALOGS");
                }
                VolumeDialogController.this.dismiss();
            }
            if (changed) {
                VolumeDialogController.this.mCallbacks.onStateChanged(VolumeDialogController.this.mState);
            }
        }
    }

    private final class SettingObserver extends ContentObserver {
        private final Uri SERVICE_URI = Secure.getUriFor("volume_controller_service_component");
        private final Uri ZEN_MODE_CONFIG_URI = Global.getUriFor("zen_mode_config_etag");
        private final Uri ZEN_MODE_URI = Global.getUriFor("zen_mode");

        public SettingObserver(Handler handler) {
            super(handler);
        }

        public void init() {
            VolumeDialogController.this.mContext.getContentResolver().registerContentObserver(this.SERVICE_URI, false, this);
            VolumeDialogController.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_URI, false, this);
            VolumeDialogController.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_CONFIG_URI, false, this);
            onChange(true, this.SERVICE_URI);
        }

        public void onChange(boolean selfChange, Uri uri) {
            boolean changed = false;
            if (this.SERVICE_URI.equals(uri)) {
                boolean enabled;
                String setting = Secure.getString(VolumeDialogController.this.mContext.getContentResolver(), "volume_controller_service_component");
                if (setting == null || VolumeDialogController.this.mComponent == null) {
                    enabled = false;
                } else {
                    enabled = VolumeDialogController.this.mComponent.equals(ComponentName.unflattenFromString(setting));
                }
                if (enabled != VolumeDialogController.this.mEnabled) {
                    if (enabled) {
                        VolumeDialogController.this.register();
                    }
                    VolumeDialogController.this.mEnabled = enabled;
                } else {
                    return;
                }
            }
            if (this.ZEN_MODE_URI.equals(uri)) {
                changed = VolumeDialogController.this.updateZenModeW();
            }
            if (changed) {
                VolumeDialogController.this.mCallbacks.onStateChanged(VolumeDialogController.this.mState);
            }
        }
    }

    public static final class State {
        public static int NO_ACTIVE_STREAM = -1;
        public int activeStream = NO_ACTIVE_STREAM;
        public ComponentName effectsSuppressor;
        public String effectsSuppressorName;
        public int ringerModeExternal;
        public int ringerModeInternal;
        public final SparseArray<StreamState> states = new SparseArray();
        public int zenMode;

        public State copy() {
            State rt = new State();
            for (int i = 0; i < this.states.size(); i++) {
                rt.states.put(this.states.keyAt(i), ((StreamState) this.states.valueAt(i)).copy());
            }
            rt.ringerModeExternal = this.ringerModeExternal;
            rt.ringerModeInternal = this.ringerModeInternal;
            rt.zenMode = this.zenMode;
            if (this.effectsSuppressor != null) {
                rt.effectsSuppressor = this.effectsSuppressor.clone();
            }
            rt.effectsSuppressorName = this.effectsSuppressorName;
            rt.activeStream = this.activeStream;
            return rt;
        }

        public String toString() {
            return toString(0);
        }

        public String toString(int indent) {
            StringBuilder sb = new StringBuilder("{");
            if (indent > 0) {
                sep(sb, indent);
            }
            for (int i = 0; i < this.states.size(); i++) {
                if (i > 0) {
                    sep(sb, indent);
                }
                StreamState ss = (StreamState) this.states.valueAt(i);
                sb.append(AudioSystem.streamToString(this.states.keyAt(i))).append(":").append(ss.level).append('[').append(ss.levelMin).append("..").append(ss.levelMax).append(']');
                if (ss.muted) {
                    sb.append(" [MUTED]");
                }
            }
            sep(sb, indent);
            sb.append("ringerModeExternal:").append(this.ringerModeExternal);
            sep(sb, indent);
            sb.append("ringerModeInternal:").append(this.ringerModeInternal);
            sep(sb, indent);
            sb.append("zenMode:").append(this.zenMode);
            sep(sb, indent);
            sb.append("effectsSuppressor:").append(this.effectsSuppressor);
            sep(sb, indent);
            sb.append("effectsSuppressorName:").append(this.effectsSuppressorName);
            sep(sb, indent);
            sb.append("activeStream:").append(this.activeStream);
            if (indent > 0) {
                sep(sb, indent);
            }
            return sb.append('}').toString();
        }

        private static void sep(StringBuilder sb, int indent) {
            if (indent > 0) {
                sb.append('\n');
                for (int i = 0; i < indent; i++) {
                    sb.append(' ');
                }
                return;
            }
            sb.append(',');
        }
    }

    public static final class StreamState {
        public boolean dynamic;
        public int level;
        public int levelMax;
        public int levelMin;
        public boolean muteSupported;
        public boolean muted;
        public String name;
        public boolean routedToBluetooth;

        public StreamState copy() {
            StreamState rt = new StreamState();
            rt.dynamic = this.dynamic;
            rt.level = this.level;
            rt.levelMin = this.levelMin;
            rt.levelMax = this.levelMax;
            rt.muted = this.muted;
            rt.muteSupported = this.muteSupported;
            rt.name = this.name;
            rt.routedToBluetooth = this.routedToBluetooth;
            return rt;
        }
    }

    private final class VC extends Stub {
        private final String TAG;

        private VC() {
            this.TAG = VolumeDialogController.TAG + ".VC";
        }

        public void displaySafeVolumeWarning(int flags) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "displaySafeVolumeWarning " + Util.audioManagerFlagsToString(flags));
            }
            if (!VolumeDialogController.this.mDestroyed) {
                VolumeDialogController.this.mWorker.obtainMessage(14, flags, 0).sendToTarget();
            }
        }

        public void volumeChanged(int streamType, int flags) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "volumeChanged " + AudioSystem.streamToString(streamType) + " " + Util.audioManagerFlagsToString(flags));
            }
            if (!VolumeDialogController.this.mDestroyed) {
                VolumeDialogController.this.mWorker.obtainMessage(1, streamType, flags).sendToTarget();
            }
        }

        public void masterMuteChanged(int flags) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "masterMuteChanged");
            }
        }

        public void setLayoutDirection(int layoutDirection) throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "setLayoutDirection");
            }
            if (!VolumeDialogController.this.mDestroyed) {
                VolumeDialogController.this.mWorker.obtainMessage(8, layoutDirection, 0).sendToTarget();
            }
        }

        public void dismiss() throws RemoteException {
            if (D.BUG) {
                Log.d(this.TAG, "dismiss requested");
            }
            if (!VolumeDialogController.this.mDestroyed) {
                VolumeDialogController.this.mWorker.obtainMessage(2, 2, 0).sendToTarget();
                VolumeDialogController.this.mWorker.sendEmptyMessage(2);
            }
        }
    }

    private final class W extends Handler {
        W(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            VolumeDialogController volumeDialogController;
            int i;
            switch (msg.what) {
                case 1:
                    VolumeDialogController.this.onVolumeChangedW(msg.arg1, msg.arg2);
                    return;
                case 2:
                    VolumeDialogController.this.onDismissRequestedW(msg.arg1);
                    return;
                case 3:
                    VolumeDialogController.this.onGetStateW();
                    return;
                case 4:
                    volumeDialogController = VolumeDialogController.this;
                    i = msg.arg1;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    volumeDialogController.onSetRingerModeW(i, z);
                    return;
                case 5:
                    VolumeDialogController.this.onSetZenModeW(msg.arg1);
                    return;
                case 6:
                    VolumeDialogController.this.onSetExitConditionW((Condition) msg.obj);
                    return;
                case 7:
                    volumeDialogController = VolumeDialogController.this;
                    i = msg.arg1;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    volumeDialogController.onSetStreamMuteW(i, z);
                    return;
                case 8:
                    VolumeDialogController.this.mCallbacks.onLayoutDirectionChanged(msg.arg1);
                    return;
                case 9:
                    VolumeDialogController.this.mCallbacks.onConfigurationChanged();
                    return;
                case 10:
                    VolumeDialogController.this.onSetStreamVolumeW(msg.arg1, msg.arg2);
                    return;
                case 11:
                    VolumeDialogController.this.onSetActiveStreamW(msg.arg1);
                    return;
                case 12:
                    volumeDialogController = VolumeDialogController.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    volumeDialogController.onNotifyVisibleW(z);
                    return;
                case 13:
                    VolumeDialogController.this.onUserActivityW();
                    return;
                case 14:
                    VolumeDialogController.this.onShowSafetyWarningW(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    public VolumeDialogController(Context context, ComponentName component) {
        this.mContext = context.getApplicationContext();
        Events.writeEvent(this.mContext, 5, new Object[0]);
        this.mComponent = component;
        this.mWorkerThread = new HandlerThread(VolumeDialogController.class.getSimpleName());
        this.mWorkerThread.start();
        this.mWorker = new W(this.mWorkerThread.getLooper());
        this.mMediaSessions = createMediaSessions(this.mContext, this.mWorkerThread.getLooper(), this.mMediaSessionsCallbacksW);
        this.mAudio = (AudioManager) this.mContext.getSystemService("audio");
        this.mNoMan = (NotificationManager) this.mContext.getSystemService("notification");
        this.mObserver = new SettingObserver(this.mWorker);
        this.mObserver.init();
        this.mReceiver.init();
        this.mStreamTitles = this.mContext.getResources().getStringArray(R.array.volume_stream_titles);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHasVibrator = this.mVibrator != null ? this.mVibrator.hasVibrator() : false;
        this.mCustVolumeDialogController = (HwCustVolumeDialogController) HwCustUtils.createObj(HwCustVolumeDialogController.class, new Object[]{this, this.mContext});
    }

    public AudioManager getAudioManager() {
        return this.mAudio;
    }

    public void dismiss() {
        this.mCallbacks.onDismissRequested(2);
    }

    public void register() {
        try {
            this.mAudio.setVolumeController(this.mVolumeController);
            setVolumePolicy(this.mVolumePolicy);
            showDndTile(this.mShowDndTile);
            try {
                this.mMediaSessions.init();
            } catch (SecurityException e) {
                Log.w(TAG, "No access to media sessions", e);
            }
        } catch (SecurityException e2) {
            Log.w(TAG, "Unable to set the volume controller", e2);
        }
    }

    public void setVolumePolicy(VolumePolicy policy) {
        this.mVolumePolicy = policy;
        if (this.mVolumePolicy != null) {
            try {
                this.mAudio.setVolumePolicy(this.mVolumePolicy);
            } catch (NoSuchMethodError e) {
                Log.w(TAG, "No volume policy api");
            }
        }
    }

    protected MediaSessions createMediaSessions(Context context, Looper looper, com.android.systemui.volume.MediaSessions.Callbacks callbacks) {
        return new MediaSessions(context, looper, callbacks);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(VolumeDialogController.class.getSimpleName() + " state:");
        pw.print("  mEnabled: ");
        pw.println(this.mEnabled);
        pw.print("  mDestroyed: ");
        pw.println(this.mDestroyed);
        pw.print("  mVolumePolicy: ");
        pw.println(this.mVolumePolicy);
        pw.print("  mState: ");
        pw.println(this.mState.toString(4));
        pw.print("  mShowDndTile: ");
        pw.println(this.mShowDndTile);
        pw.print("  mHasVibrator: ");
        pw.println(this.mHasVibrator);
        pw.print("  mRemoteStreams: ");
        pw.println(this.mMediaSessionsCallbacksW.mRemoteStreams.values());
        pw.println();
        this.mMediaSessions.dump(pw);
    }

    public void addCallback(Callbacks callback, Handler handler) {
        this.mCallbacks.add(callback, handler);
    }

    public void getState() {
        if (!this.mDestroyed) {
            this.mWorker.sendEmptyMessage(3);
        }
    }

    public void notifyVisible(boolean visible) {
        if (!this.mDestroyed) {
            int i;
            W w = this.mWorker;
            if (visible) {
                i = 1;
            } else {
                i = 0;
            }
            w.obtainMessage(12, i, 0).sendToTarget();
        }
    }

    public void userActivity() {
        if (!this.mDestroyed) {
            this.mWorker.removeMessages(13);
            this.mWorker.sendEmptyMessage(13);
        }
    }

    public void setRingerMode(int value, boolean external) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(4, value, external ? 1 : 0).sendToTarget();
        }
    }

    public void setStreamVolume(int stream, int level) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(10, stream, level).sendToTarget();
        }
    }

    public void setActiveStream(int stream) {
        if (!this.mDestroyed) {
            this.mWorker.obtainMessage(11, stream, 0).sendToTarget();
        }
    }

    public void vibrate() {
        if (this.mHasVibrator) {
            this.mVibrator.vibrate(300, VIBRATION_ATTRIBUTES);
        }
    }

    public boolean hasVibrator() {
        return this.mHasVibrator;
    }

    private void onNotifyVisibleW(boolean visible) {
        if (!this.mDestroyed) {
            this.mAudio.notifyVolumeControllerVisible(this.mVolumeController, visible);
            if (!visible && updateActiveStreamW(-1)) {
                this.mCallbacks.onStateChanged(this.mState);
            }
        }
    }

    protected void onUserActivityW() {
    }

    private void onShowSafetyWarningW(int flags) {
        this.mCallbacks.onShowSafetyWarning(flags);
    }

    private boolean checkRoutedToBluetoothW(int stream) {
        boolean routedToBluetooth = false;
        if (stream != 3) {
            return false;
        }
        if ((this.mAudio.getDevicesForStream(3) & 896) != 0) {
            routedToBluetooth = true;
        }
        return updateStreamRoutedToBluetoothW(stream, routedToBluetooth);
    }

    private boolean onVolumeChangedW(int stream, int flags) {
        int i;
        HwLog.i(TAG, "onVolumeChangedW stream = " + stream + ", flags = " + flags);
        boolean showUI = (flags & 1) != 0;
        boolean fromKey = (flags & 4096) != 0;
        boolean showVibrateHint = (flags & 2048) != 0;
        boolean showSilentHint = (flags & 128) != 0;
        boolean changed = false;
        if (showUI) {
            if (this.mCustVolumeDialogController != null) {
                this.mCustVolumeDialogController.showToastForShutdown();
            }
            changed = updateActiveStreamW(stream);
        }
        changed |= updateStreamLevelW(stream, this.mAudio.getLastAudibleStreamVolume(stream));
        if (showUI) {
            i = 3;
        } else {
            i = stream;
        }
        changed |= checkRoutedToBluetoothW(i);
        if (changed) {
            this.mCallbacks.onStateChanged(this.mState);
        }
        if (showUI) {
            this.mCallbacks.onShowRequested(1);
        }
        if (showVibrateHint) {
            this.mCallbacks.onShowVibrateHint();
        }
        if (showSilentHint) {
            this.mCallbacks.onShowSilentHint();
        }
        if (changed && fromKey) {
            Events.writeEvent(this.mContext, 4, Integer.valueOf(stream), Integer.valueOf(lastAudibleStreamVolume));
        }
        if (this.mSilentViewCallback != null) {
            this.mSilentViewCallback.updateStreamVolume(stream, -1);
            if (!((flags & 4) == 0 || showVibrateHint || showSilentHint)) {
                this.mSilentViewCallback.playSound(stream);
            }
            if ((flags & 8) != 0) {
                this.mSilentViewCallback.stopSound();
            }
        }
        return changed;
    }

    private boolean updateActiveStreamW(int activeStream) {
        if (activeStream == this.mState.activeStream) {
            return false;
        }
        this.mState.activeStream = activeStream;
        Events.writeEvent(this.mContext, 2, Integer.valueOf(activeStream));
        if (D.BUG) {
            Log.d(TAG, "updateActiveStreamW " + activeStream);
        }
        int s = activeStream < 100 ? activeStream : -1;
        if (D.BUG) {
            Log.d(TAG, "forceVolumeControlStream " + s);
        }
        this.mAudio.forceVolumeControlStream(s);
        return true;
    }

    private StreamState streamStateW(int stream) {
        StreamState ss = (StreamState) this.mState.states.get(stream);
        if (ss != null) {
            return ss;
        }
        ss = new StreamState();
        this.mState.states.put(stream, ss);
        return ss;
    }

    private void onGetStateW() {
        for (int stream : STREAMS) {
            updateStreamLevelW(stream, this.mAudio.getLastAudibleStreamVolume(stream));
            streamStateW(stream).levelMin = this.mAudio.getStreamMinVolume(stream);
            streamStateW(stream).levelMax = this.mAudio.getStreamMaxVolume(stream);
            updateStreamMuteW(stream, this.mAudio.isStreamMute(stream));
            StreamState ss = streamStateW(stream);
            ss.muteSupported = this.mAudio.isStreamAffectedByMute(stream);
            ss.name = this.mStreamTitles[stream];
            checkRoutedToBluetoothW(stream);
        }
        updateRingerModeExternalW(this.mAudio.getRingerMode());
        updateZenModeW();
        updateEffectsSuppressorW(this.mNoMan.getEffectsSuppressor());
        this.mCallbacks.onStateChanged(this.mState);
    }

    private boolean updateStreamRoutedToBluetoothW(int stream, boolean routedToBluetooth) {
        StreamState ss = streamStateW(stream);
        if (ss.routedToBluetooth == routedToBluetooth) {
            return false;
        }
        ss.routedToBluetooth = routedToBluetooth;
        if (D.BUG) {
            Log.d(TAG, "updateStreamRoutedToBluetoothW stream=" + stream + " routedToBluetooth=" + routedToBluetooth);
        }
        return true;
    }

    private boolean updateStreamLevelW(int stream, int level) {
        StreamState ss = streamStateW(stream);
        if (ss.level == level) {
            return false;
        }
        ss.level = level;
        if (isLogWorthy(stream)) {
            Events.writeEvent(this.mContext, 10, Integer.valueOf(stream), Integer.valueOf(level));
        }
        return true;
    }

    private static boolean isLogWorthy(int stream) {
        switch (stream) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
                return true;
            default:
                return false;
        }
    }

    private boolean updateStreamMuteW(int stream, boolean muted) {
        StreamState ss = streamStateW(stream);
        if (ss.muted == muted) {
            return false;
        }
        ss.muted = muted;
        if (isLogWorthy(stream)) {
            Events.writeEvent(this.mContext, 15, Integer.valueOf(stream), Boolean.valueOf(muted));
        }
        if (muted && isRinger(stream)) {
            updateRingerModeInternalW(this.mAudio.getRingerModeInternal());
        }
        return true;
    }

    public void updateStreamMute(int stream, boolean muted) {
        streamStateW(stream).muted = muted;
    }

    private static boolean isRinger(int stream) {
        return stream == 2 || stream == 5;
    }

    private boolean updateEffectsSuppressorW(ComponentName effectsSuppressor) {
        if (Objects.equals(this.mState.effectsSuppressor, effectsSuppressor)) {
            return false;
        }
        this.mState.effectsSuppressor = effectsSuppressor;
        this.mState.effectsSuppressorName = getApplicationName(this.mContext, this.mState.effectsSuppressor);
        Events.writeEvent(this.mContext, 14, this.mState.effectsSuppressor, this.mState.effectsSuppressorName);
        return true;
    }

    private static String getApplicationName(Context context, ComponentName component) {
        if (component == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        String pkg = component.getPackageName();
        try {
            String rt = Objects.toString(pm.getApplicationInfo(pkg, 0).loadLabel(pm), BuildConfig.FLAVOR).trim();
            if (rt.length() > 0) {
                return rt;
            }
            return pkg;
        } catch (NameNotFoundException e) {
        }
    }

    private boolean updateZenModeW() {
        int zen = Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0);
        if (this.mState.zenMode == zen) {
            return false;
        }
        this.mState.zenMode = zen;
        Events.writeEvent(this.mContext, 13, Integer.valueOf(zen));
        return true;
    }

    private boolean updateRingerModeExternalW(int rm) {
        if (rm == this.mState.ringerModeExternal) {
            return false;
        }
        this.mState.ringerModeExternal = rm;
        Events.writeEvent(this.mContext, 12, Integer.valueOf(rm));
        return true;
    }

    private boolean updateRingerModeInternalW(int rm) {
        if (rm == this.mState.ringerModeInternal) {
            return false;
        }
        this.mState.ringerModeInternal = rm;
        Events.writeEvent(this.mContext, 11, Integer.valueOf(rm));
        return true;
    }

    private void onSetRingerModeW(int mode, boolean external) {
        if (external) {
            this.mAudio.setRingerMode(mode);
        } else {
            this.mAudio.setRingerModeInternal(mode);
        }
    }

    private void onSetStreamMuteW(int stream, boolean mute) {
        int i;
        AudioManager audioManager = this.mAudio;
        if (mute) {
            i = -100;
        } else {
            i = 100;
        }
        audioManager.adjustStreamVolume(stream, i, 0);
    }

    private void onSetStreamVolumeW(int stream, int level) {
        if (D.BUG) {
            Log.d(TAG, "onSetStreamVolume " + stream + " level=" + level);
        }
        if (this.mCustVolumeDialogController != null) {
            this.mCustVolumeDialogController.saveAfterChangeVolume(stream, level);
        }
        if (stream >= 100) {
            this.mMediaSessionsCallbacksW.setStreamVolume(stream, level);
            return;
        }
        this.mAudio.setStreamVolume(stream, level, 0);
        if (this.mSilentViewCallback != null) {
            this.mSilentViewCallback.updateStreamVolume(stream, -1);
        }
    }

    private void onSetActiveStreamW(int stream) {
        if (updateActiveStreamW(stream)) {
            this.mCallbacks.onStateChanged(this.mState);
        }
    }

    private void onSetExitConditionW(Condition condition) {
        Uri uri = null;
        NotificationManager notificationManager = this.mNoMan;
        int i = this.mState.zenMode;
        if (condition != null) {
            uri = condition.id;
        }
        notificationManager.setZenMode(i, uri, TAG);
    }

    private void onSetZenModeW(int mode) {
        if (D.BUG) {
            Log.d(TAG, "onSetZenModeW " + mode);
        }
        this.mNoMan.setZenMode(mode, null, TAG);
    }

    private void onDismissRequestedW(int reason) {
        this.mCallbacks.onDismissRequested(reason);
    }

    public void showDndTile(boolean visible) {
        if (D.BUG) {
            Log.d(TAG, "showDndTile");
        }
        DndTile.setVisible(this.mContext, visible);
    }

    public void setSilentViewCallback(HwSilentViewCallback callback) {
        this.mSilentViewCallback = callback;
    }
}
