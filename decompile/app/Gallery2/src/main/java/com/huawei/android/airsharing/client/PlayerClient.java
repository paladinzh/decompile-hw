package com.huawei.android.airsharing.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import com.huawei.android.airsharing.api.EHwMediaInfoType;
import com.huawei.android.airsharing.api.HwMediaInfo;
import com.huawei.android.airsharing.api.HwMediaPosition;
import com.huawei.android.airsharing.api.HwObject;
import com.huawei.android.airsharing.api.HwServer;
import com.huawei.android.airsharing.api.IEventListener;
import com.huawei.android.airsharing.client.IAidlHwPlayerManager.Stub;
import com.huawei.android.airsharing.util.IICLOG;

public class PlayerClient implements IEventListener {
    private static final String TAG = PlayerClient.class.getSimpleName();
    private static PlayerClient sInstance = null;
    private static final IICLOG sLog = IICLOG.getInstance();
    private boolean hasSubscribe = false;
    private IAidlHwPlayerManager mAidlHwPlayerManager = null;
    private final Object mBinderLock = new Object();
    private Context mContext;
    private IEventListener mEventListener = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (PlayerClient.this.hasSubscribe && PlayerClient.this.mPid != -1) {
                        PlayerClient.this.bindHwPlayerService();
                        break;
                    }
            }
        }
    };
    private final Object mListenerLock = new Object();
    private int mPid = -1;
    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            PlayerClient.sLog.d(PlayerClient.TAG, "bind PlayerService onServiceDisconnected");
            synchronized (PlayerClient.this.mBinderLock) {
                PlayerClient.this.mAidlHwPlayerManager = null;
            }
            PlayerClient.this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
            PlayerClient.this.mHandler.removeMessages(1);
            PlayerClient.this.mHandler.sendEmptyMessageDelayed(1, 5000);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerClient.sLog.d(PlayerClient.TAG, "bind PlayerService onServiceConnected");
            PlayerClient.this.mHandler.removeMessages(1);
            PlayerClient.this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_CONNECTED;
            String serName = PlayerClient.this.mServerName;
            String serType = PlayerClient.this.mServerType;
            int pid = PlayerClient.this.mPid;
            PlayerClient.sLog.d(PlayerClient.TAG, "mSubscribeServerType = " + PlayerClient.this.mSubscribeServerType);
            PlayerClient.this.mAidlHwPlayerManager = Stub.asInterface(service);
            if (serType != null) {
                try {
                    PlayerClient.sLog.d(PlayerClient.TAG, "in onServiceConnected, before invoke startServer mServerType:" + serType);
                    if (PlayerClient.this.mAidlHwPlayerManager != null) {
                        PlayerClient.this.mAidlHwPlayerManager.clsHwSharingListener(pid, new EventListenerAgent(PlayerClient.getInstance()));
                        PlayerClient.this.mAidlHwPlayerManager.setHwSharingListener(pid, new EventListenerAgent(PlayerClient.getInstance()));
                        PlayerClient.this.mAidlHwPlayerManager.startServer(pid, serName, serType);
                    }
                } catch (RemoteException e) {
                    PlayerClient.sLog.w(PlayerClient.TAG, "onServiceConnected throw RemoteException");
                } catch (NullPointerException e2) {
                    PlayerClient.sLog.w(PlayerClient.TAG, "in onServiceConnected, before invoke startServer mServerType:" + serType);
                }
            }
            if (PlayerClient.this.mSubscribeServerType != null) {
                SubServerRunnable subRunnable = new SubServerRunnable();
                subRunnable.serverType = PlayerClient.this.mSubscribeServerType;
                new Thread(subRunnable).start();
            }
        }
    };
    private String mServerName = null;
    private String mServerType = null;
    private EServiceConnectStatus mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
    private String mSubscribeServerType = null;

    private enum EServiceConnectStatus {
        SERVICE_DISCONNECTED,
        SERVICE_DISCONNECTING,
        SERVICE_CONNECTING,
        SERVICE_CONNECTED
    }

    private class EventNotifyRunnable implements Runnable {
        public int eventId;
        public String type;

        private EventNotifyRunnable() {
            this.eventId = -1;
            this.type = null;
        }

        public void run() {
            PlayerClient.this.onEvent(this.eventId, this.type);
        }
    }

    private class SubServerRunnable implements Runnable {
        public String serverType;

        private SubServerRunnable() {
            this.serverType = null;
        }

        public void run() {
            try {
                PlayerClient.sLog.d(PlayerClient.TAG, "SubServerRunnable IN");
                int pid = PlayerClient.this.mPid;
                String serType = this.serverType;
                if (PlayerClient.this.mAidlHwPlayerManager != null) {
                    PlayerClient.this.mAidlHwPlayerManager.clsHwSharingListener(pid, new EventListenerAgent(PlayerClient.getInstance()));
                    PlayerClient.this.mAidlHwPlayerManager.setHwSharingListener(pid, new EventListenerAgent(PlayerClient.getInstance()));
                    PlayerClient.this.mAidlHwPlayerManager.subscribServers(pid, serType);
                    PlayerClient.this.hasSubscribe = true;
                    PlayerClient.this.notifyEventAsync(2010, "EVENT_TYPE_PLAYER_SUBSCRIBE_SUCCESS");
                }
            } catch (RemoteException e) {
                PlayerClient.sLog.w(PlayerClient.TAG, "run RemoteException");
            } catch (NullPointerException e2) {
                PlayerClient.sLog.w(PlayerClient.TAG, "startServer throw NullPointerException");
            }
        }
    }

    public static synchronized PlayerClient getInstance() {
        PlayerClient playerClient;
        synchronized (PlayerClient.class) {
            if (sInstance == null) {
                sInstance = new PlayerClient();
            }
            playerClient = sInstance;
        }
        return playerClient;
    }

    public boolean init(Context context) {
        sLog.d(TAG, "init in with context");
        this.mContext = context;
        this.mPid = Process.myPid();
        this.mHandler.removeMessages(1);
        return bindHwPlayerService();
    }

    public void deInit() {
        this.mHandler.removeMessages(1);
        unbindHwPlayerService();
        this.mPid = -1;
        sLog.d(TAG, "deInit in");
    }

    public void subscribServers(String serverType) {
        sLog.d(TAG, "subscribServers in serverType=" + serverType);
        boolean res = false;
        if (serverType != null) {
            try {
                Integer.parseInt(serverType);
                if (this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTED || this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTING) {
                    sLog.w(TAG, "subscribServers failed, service has not bind");
                } else {
                    if (this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_CONNECTING) {
                        this.mSubscribeServerType = serverType;
                        sLog.d(TAG, "mServiceConnectStatus == SERVICE_CONNECTING");
                    } else if (this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_CONNECTED) {
                        sLog.d(TAG, "mServiceConnectStatus == EServiceConnectStatus.SERVICE_CONNECTED");
                        this.mSubscribeServerType = serverType;
                        SubServerRunnable runnable = new SubServerRunnable();
                        runnable.serverType = serverType;
                        new Thread(runnable).start();
                    }
                    res = true;
                }
            } catch (NumberFormatException e) {
                sLog.w(TAG, "subscribServers serverType is not number =" + serverType);
            }
        } else {
            sLog.w(TAG, "subscribServers failed without service init or type is null");
        }
        if (!res) {
            notifyEventAsync(2010, "EVENT_TYPE_PLAYER_SUBSCRIBE_FAILED");
        }
        sLog.d(TAG, "subscribServers out");
    }

    public void unsubscribServers() {
        sLog.d(TAG, "unsubscribServers in");
        int pid = this.mPid;
        if (this.mAidlHwPlayerManager != null) {
            try {
                sLog.d(TAG, "mAidlHwPlayerManager.unsubscribServers");
                this.mAidlHwPlayerManager.unsubscribServers(pid, "7");
                this.hasSubscribe = false;
            } catch (RemoteException e) {
                sLog.w(TAG, "unsubscribServers aidl throw exception");
            } catch (NullPointerException e2) {
                sLog.w(TAG, "startServer throw NullPointerException");
            }
            sLog.d(TAG, "deInit out");
            return;
        }
        sLog.w(TAG, "unsubscribServers without PlayerService Init");
    }

    public void setHwSharingListener(IEventListener mHwSharingListener) {
        sLog.d(TAG, "setHwSharingListener in mHwSharingListener:" + mHwSharingListener);
        if (this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTED || this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_DISCONNECTING || mHwSharingListener == null) {
            sLog.w(TAG, "mHwSharingListener is null or PlayerService not init");
            return;
        }
        synchronized (this.mListenerLock) {
            this.mEventListener = mHwSharingListener;
            sLog.d(TAG, "setHwSharingListener out mEventListener:" + this.mEventListener);
        }
    }

    public void clsHwSharingListener(IEventListener mHwSharingListener) {
        sLog.d(TAG, "clsHwSharingListener in");
        if (mHwSharingListener != null) {
            int pid = this.mPid;
            synchronized (this.mListenerLock) {
                this.mEventListener = null;
            }
            if (this.mAidlHwPlayerManager != null) {
                try {
                    this.mAidlHwPlayerManager.clsHwSharingListener(pid, new EventListenerAgent(this));
                } catch (RemoteException e) {
                    sLog.w(TAG, "clsHwSharingListener throw RemoteException");
                } catch (NullPointerException e2) {
                    sLog.w(TAG, "clsHwSharingListener throw NullPointerException");
                }
                return;
            }
            return;
        }
        sLog.w(TAG, "mHwSharingListener is null or PlayerService not init");
    }

    public boolean playMedia(String url, String name, EHwMediaInfoType type, String position, int volume) {
        sLog.d(TAG, "PlayMedia in url=" + url + " type=" + type + " position=" + position);
        if (url == null || type == null || name == null) {
            notifyEventAsync(2002, "EVENT_TYPE_PLAYER_MEDIA_STOP_PUSH_FAILED");
            return false;
        }
        HwMediaInfo info = new HwMediaInfo();
        info.setUrl(url);
        info.setName(name);
        info.setMediaInfoType(type);
        info.setPosition(position);
        info.setVolume(volume);
        return playMedia(info, false, null);
    }

    public boolean playMedia(HwMediaInfo mediaInfo, boolean isHwAirsharing, HwObject extendObj) {
        sLog.d(TAG, "PlayMedia in mediaInfo=" + mediaInfo + " isHwAirsharing=" + isHwAirsharing);
        if (mediaInfo == null || mediaInfo.getUrl() == null || mediaInfo.getMediaInfoType() == null || mediaInfo.getName() == null) {
            sLog.w(TAG, "PlayMedia(mediaInfo) without PlayerService Init or subscribe");
            notifyEventAsync(2002, "EVENT_TYPE_PLAYER_MEDIA_STOP_PUSH_FAILED");
            return false;
        }
        int pid = this.mPid;
        try {
            if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
                return this.mAidlHwPlayerManager.PlayMedia(pid, mediaInfo, isHwAirsharing, extendObj);
            }
            sLog.w(TAG, "playMedia without PlayerService Init");
            return false;
        } catch (RemoteException e) {
            sLog.w(TAG, "PlayMedia throw RemoteException");
            return false;
        } catch (NullPointerException e2) {
            sLog.w(TAG, "PlayMedia throw NullPointerException");
            return false;
        }
    }

    public HwMediaPosition getPosition() {
        sLog.d(TAG, "getPosition in");
        int pid = this.mPid;
        if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
            try {
                return this.mAidlHwPlayerManager.getPosition(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch getPosition throw exception");
                return null;
            } catch (NullPointerException e2) {
                sLog.w(TAG, "getPosition throw NullPointerException");
                return null;
            }
        }
        sLog.w(TAG, "getPosition without PlayerService Init");
        return null;
    }

    public boolean seek(String targetPostion) {
        sLog.d(TAG, "Seek in targetPostion=" + targetPostion);
        if (targetPostion != null) {
            int pid = this.mPid;
            if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
                try {
                    return this.mAidlHwPlayerManager.Seek(pid, targetPostion);
                } catch (RemoteException e) {
                    sLog.w(TAG, "catch Seek throw exception");
                    return false;
                } catch (NullPointerException e2) {
                    sLog.w(TAG, "seek throw NullPointerException");
                    return false;
                }
            }
            sLog.w(TAG, "Seek mAidlHwPlayerManager null");
            return false;
        }
        sLog.w(TAG, "Seek without PlayerService Init");
        return false;
    }

    public boolean pause() {
        sLog.d(TAG, "Pause in");
        int pid = this.mPid;
        if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
            try {
                return this.mAidlHwPlayerManager.Pause(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch Pause throw exception");
                return false;
            } catch (NullPointerException e2) {
                sLog.w(TAG, "pause throw NullPointerException");
                return false;
            }
        }
        sLog.w(TAG, "Pause without mAidlHwPlayerManager set");
        return false;
    }

    public boolean resume() {
        sLog.d(TAG, "Resume in");
        int pid = this.mPid;
        if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
            try {
                return this.mAidlHwPlayerManager.Resume(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch Resume throw exception");
                return false;
            } catch (NullPointerException e2) {
                sLog.w(TAG, "resume() throw NullPointerException");
                return false;
            }
        }
        sLog.w(TAG, "Resume without PlayerService Init");
        return false;
    }

    public boolean stop() {
        sLog.d(TAG, "Stop in");
        int pid = this.mPid;
        if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
            try {
                return this.mAidlHwPlayerManager.Stop(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch Resume throw exception");
                return false;
            } catch (NullPointerException e2) {
                sLog.w(TAG, "stop() throw NullPointerException");
                return false;
            }
        }
        sLog.w(TAG, "stop without PlayerService Init");
        return false;
    }

    public boolean isRendering() {
        sLog.d(TAG, "isRendering in");
        int pid = this.mPid;
        if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
            try {
                return this.mAidlHwPlayerManager.isRendering(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch Resume throw exception");
                return false;
            } catch (NullPointerException e2) {
                sLog.w(TAG, "isRendering throw NullPointerException");
                return false;
            }
        }
        sLog.w(TAG, "isRendering without PlayerService Init");
        return false;
    }

    public boolean hasPlayer() {
        sLog.d(TAG, "hasPlayer in");
        int pid = this.mPid;
        if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
            try {
                return this.mAidlHwPlayerManager.hasPlayer(pid);
            } catch (RemoteException e) {
                sLog.w(TAG, "catch Resume throw exception");
                return false;
            } catch (NullPointerException e2) {
                sLog.w(TAG, "hasPlayer throw NullPointerException");
                return false;
            }
        }
        sLog.w(TAG, "hasPlayer without PlayerService Init");
        return false;
    }

    public boolean setVolume(int volume) {
        sLog.d(TAG, "setVolume in volume=" + volume);
        if (volume >= 0 && volume <= 100) {
            int pid = this.mPid;
            if (this.hasSubscribe && this.mAidlHwPlayerManager != null) {
                try {
                    return this.mAidlHwPlayerManager.setVolume(pid, volume);
                } catch (RemoteException e) {
                    sLog.w(TAG, "catch setVolume throw exception");
                    return false;
                } catch (NullPointerException e2) {
                    sLog.w(TAG, "setVolume throw NullPointerException");
                    return false;
                }
            }
            sLog.w(TAG, "setVolume without PlayerService Init");
            return false;
        }
        sLog.w(TAG, "setVolume without PlayerService Init");
        return false;
    }

    public boolean bindHwPlayerService() {
        sLog.d(TAG, "bindHwPlayerService in");
        boolean bindServiceResult = false;
        if (this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_CONNECTED || this.mServiceConnectStatus == EServiceConnectStatus.SERVICE_CONNECTING) {
            sLog.d(TAG, "bindHwPlayerService service has bind");
            return true;
        }
        if (this.mContext != null) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.huawei.android.airsharing", "com.huawei.android.airsharing.service.PlayerService"));
                bindServiceResult = this.mContext.bindService(intent, this.mPlayerServiceConnection, 1);
            } catch (IllegalArgumentException e) {
                sLog.w(TAG, "bindService throw IllegalArgumentException");
            } catch (SecurityException e2) {
                sLog.w(TAG, "bindService throw SecurityException");
            }
        }
        if (!bindServiceResult) {
            this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
        } else if (this.mServiceConnectStatus != EServiceConnectStatus.SERVICE_CONNECTED) {
            this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_CONNECTING;
        }
        sLog.d(TAG, "bindHwPlayerService out bindServiceResult = " + bindServiceResult);
        return bindServiceResult;
    }

    public void unbindHwPlayerService() {
        sLog.d(TAG, "unbindHwPlayerService in");
        try {
            if (this.mContext != null && this.mPlayerServiceConnection != null) {
                this.mContext.unbindService(this.mPlayerServiceConnection);
                this.mServiceConnectStatus = EServiceConnectStatus.SERVICE_DISCONNECTED;
                synchronized (this.mBinderLock) {
                    this.mAidlHwPlayerManager = null;
                }
            }
        } catch (IllegalArgumentException e) {
            sLog.w(TAG, "unbind throw IllegalArgumentException");
        }
    }

    private void notifyEventAsync(int eventId, String type) {
        EventNotifyRunnable eventNotifyRunnable = new EventNotifyRunnable();
        eventNotifyRunnable.eventId = eventId;
        eventNotifyRunnable.type = type;
        new Thread(eventNotifyRunnable).start();
    }

    public boolean onEvent(int eventId, String type) {
        sLog.w(TAG, "onEvent eventId = " + eventId + " || " + type);
        synchronized (this.mListenerLock) {
            if (this.mEventListener != null && eventId > 0) {
                boolean onEvent = this.mEventListener.onEvent(eventId, type);
                return onEvent;
            }
            sLog.w(TAG, "invalid event id or listener has not init");
            return false;
        }
    }

    public HwServer getRenderingServer() {
        sLog.d(TAG, "getRenderingServer");
        if (this.mAidlHwPlayerManager != null && this.hasSubscribe) {
            try {
                sLog.d(TAG, "getRenderingServer 1");
                return this.mAidlHwPlayerManager.getRenderingServer();
            } catch (RemoteException e) {
                sLog.w(TAG, "catch PlayMedia throw exception");
                return null;
            } catch (NullPointerException e2) {
                sLog.w(TAG, "notifyTransportStateChanged throw NullPointerException");
                return null;
            }
        }
        sLog.w(TAG, "getRenderingServer without PlayerService Init");
        return null;
    }
}
