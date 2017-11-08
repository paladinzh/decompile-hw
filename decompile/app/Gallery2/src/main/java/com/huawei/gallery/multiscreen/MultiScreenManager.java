package com.huawei.gallery.multiscreen;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.airsharing.api.EHwMediaInfoType;
import com.huawei.android.airsharing.api.HwMediaInfo;
import com.huawei.android.airsharing.api.IEventListener;
import com.huawei.android.airsharing.client.PlayerClient;
import com.huawei.gallery.multiscreen.MultiScreen.Listener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiScreenManager extends MultiScreen {
    private static MultiScreenManager mInstance;
    private PlayerClient mClient;
    private Context mContext = null;
    private Runnable mEnterTask = new Runnable() {
        public void run() {
            GalleryLog.d("MultiScreen_Manager", "enter execute");
            HwMediaInfo info = new HwMediaInfo();
            info.setUrl(getClass().getSimpleName());
            info.setName("enter symbol");
            info.setMediaInfoType(EHwMediaInfoType.IMAGE);
            MultiScreenManager.this.mClient.playMedia(info, true, null);
        }
    };
    private MediaEntry mEntry;
    private EventHandler mEventHandler;
    private EventListener mEventListener = new EventListener();
    private Runnable mExitTask = new Runnable() {
        public void run() {
            MultiScreenManager.this.mClient.stop();
        }
    };
    private HandlerThread mHandlerThread;
    private ArrayList<Listener> mListeners = new ArrayList();
    private Handler mOperationHandler;
    private AtomicBoolean mScreenState = new AtomicBoolean(true);
    private Handler mUnInitHandler = new Handler();
    private Runnable mUnInitRunnable = new Runnable() {
        public void run() {
            MultiScreenManager.this.unInit();
            for (Listener l : MultiScreenManager.this.mListeners) {
                l.onUnInitServiceTimeOut();
            }
        }
    };

    private class EventHandler extends Handler {
        private EventHandler() {
        }

        public void handleMessage(Message msg) {
            String result = msg.obj == null ? "" : (String) msg.obj;
            switch (msg.what) {
                case 2000:
                    GalleryLog.d("MultiScreen_Manager", "EVENT_ID_NOTIFY_PLAYER_MEDIA_FOR_PLAY");
                    for (Listener l : MultiScreenManager.this.mListeners) {
                        l.requestMedia();
                    }
                    return;
                case 2001:
                    GalleryLog.d("MultiScreen_Manager", "EVENT_ID_NOTIFY_PLAYER_MEDIA_PLAY");
                    notifyPlayerMediaPlay(result);
                    return;
                case 2002:
                    GalleryLog.d("MultiScreen_Manager", "EVENT_ID_NOTIFY_PLAYER_MEDIA_STOP : " + result);
                    for (Listener l2 : MultiScreenManager.this.mListeners) {
                        l2.onMediaStop(result);
                    }
                    return;
                case 2003:
                    GalleryLog.d("MultiScreen_Manager", "EVENT_ID_NOTIFY_PLAYER_MEDIA_PAUSE");
                    for (Listener l22 : MultiScreenManager.this.mListeners) {
                        l22.onMediaPause();
                    }
                    return;
                case 2004:
                    GalleryLog.d("MultiScreen_Manager", "EVENT_ID_NOTIFY_PLAYER_MEDIA_POSITION_CHANGED");
                    for (Listener l222 : MultiScreenManager.this.mListeners) {
                        l222.onMediaPositionChange();
                    }
                    return;
                case 2006:
                    GalleryLog.d("MultiScreen_Manager", "EVENT_ID_NOTIFY_PLAYER_SERVER_UPDATE " + MultiScreenManager.this.mClient.hasPlayer() + " " + MultiScreenManager.this.mClient.isRendering());
                    for (Listener l2222 : MultiScreenManager.this.mListeners) {
                        l2222.onUpdateActionItem(MultiScreenManager.this.mClient.hasPlayer(), MultiScreenManager.this.mClient.isRendering());
                    }
                    return;
                case 2008:
                    GalleryLog.d("MultiScreen_Manager", "EVENT_ID_NOTIFY_PLAYER_SET_VOLUME");
                    return;
                case 2010:
                    notifyPlayerStartResult(result);
                    return;
                default:
                    return;
            }
        }

        private void notifyPlayerMediaPlay(String result) {
            if ("EVENT_TYPE_PLAYER_MEDIA_PLAY_PUSH_SUCC".equals(result)) {
                GalleryLog.d("MultiScreen_Manager", "EVENT_TYPE_PLAYER_MEDIA_PLAY_PUSH_SUCC");
            } else if ("EVENT_TYPE_PLAYER_MEDIA_PLAY_PLAYSTART".equals(result)) {
                GalleryLog.d("MultiScreen_Manager", "EVENT_TYPE_PLAYER_MEDIA_PLAY_PLAYSTART");
                for (Listener l : MultiScreenManager.this.mListeners) {
                    l.onMediaPlay();
                }
            }
        }

        private void notifyPlayerStartResult(String result) {
            if ("EVENT_TYPE_PLAYER_SUBSCRIBE_SUCCESS".equals(result)) {
                GalleryLog.d("MultiScreen_Manager", "EVENT_TYPE_PLAYER_START_SUCCESS");
                MultiScreenManager.serviceReady.compareAndSet(false, true);
            } else if ("EVENT_TYPE_PLAYER_SUBSCRIBE_FAILED".equals(result)) {
                GalleryLog.d("MultiScreen_Manager", "EVENT_TYPE_PLAYER_START_FAILED");
                MultiScreenManager.serviceReady.compareAndSet(true, false);
            } else {
                throw new IllegalArgumentException("error type : " + result);
            }
            MultiScreenManager.this.mEventHandler.sendEmptyMessageDelayed(2006, 1500);
        }
    }

    private class EventListener implements IEventListener {
        private EventListener() {
        }

        public boolean onEvent(int eventId, String eventType) {
            Message msg = new Message();
            msg.what = eventId;
            msg.obj = eventType;
            MultiScreenManager.this.mEventHandler.sendMessage(msg);
            return true;
        }
    }

    public class MediaEntry implements Cloneable {
        private String name;
        private String position = "";
        private EHwMediaInfoType type;
        private String url;
        private int volume = 5;

        public MediaEntry(MediaItem item) {
            updateEntry(item);
        }

        public boolean updateEntry(MediaItem item) {
            String filePath = item.getFilePath();
            if (filePath == null || filePath.equals(this.url)) {
                return false;
            }
            this.url = item.getFilePath();
            this.name = item.getName();
            if (item.getMediaType() == 2) {
                this.type = EHwMediaInfoType.IMAGE;
            } else if (item.getMediaType() == 4) {
                this.type = EHwMediaInfoType.VIDEO;
            }
            return true;
        }

        public MediaEntry(Uri uri) {
            updateEntry(uri);
        }

        private void updateEntry(Uri uri) {
            String[] info = MultiScreenUtils.fetchInfo(MultiScreenManager.this.mContext, uri);
            this.url = info[0];
            this.name = info[1];
            this.type = EHwMediaInfoType.VIDEO;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public MediaEntry copy() {
            try {
                return (MediaEntry) clone();
            } catch (CloneNotSupportedException e) {
                GalleryLog.i("MultiScreen_Manager", "clone() failed, reason: CloneNotSupportedException.");
                return null;
            }
        }

        protected Object clone() throws CloneNotSupportedException {
            MediaEntry clone = new MediaEntry();
            clone.url = this.url;
            clone.name = this.name;
            clone.type = this.type;
            clone.position = this.position;
            clone.volume = this.volume;
            return clone;
        }
    }

    public class MultiScreenTask implements Runnable {
        private HwMediaInfo mediaInfo = new HwMediaInfo();
        private int type;

        public MultiScreenTask(MediaEntry entry, int type) {
            GalleryLog.d("MultiScreen_Manager", "task created = " + entry.url);
            this.mediaInfo.setUrl(entry.url);
            this.mediaInfo.setName(entry.name);
            this.mediaInfo.setMediaInfoType(entry.type);
            this.mediaInfo.setPosition(entry.position);
            this.mediaInfo.setVolume(entry.volume);
            if (type == 105) {
                int i;
                if (EHwMediaInfoType.VIDEO.equals(entry.type)) {
                    i = OfflineMapStatus.EXCEPTION_SDCARD;
                } else {
                    i = 101;
                }
                this.type = i;
            } else {
                this.type = type;
            }
            GalleryLog.d("MultiScreen_Manager", "pending : " + this.type);
        }

        public void run() {
            boolean result = false;
            switch (this.type) {
                case 101:
                    result = pushImageTask();
                    break;
                case 102:
                    result = pushVideoTask();
                    break;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    result = pushVideoThumbTask();
                    break;
            }
            GalleryLog.v("MultiScreen_Manager", "task result : " + result);
        }

        private boolean pushImageTask() {
            long begin = System.currentTimeMillis();
            GalleryLog.i("MultiScreen_Manager", "start pushImageTask");
            boolean result = MultiScreenManager.this.mClient.playMedia(this.mediaInfo, true, null);
            GalleryLog.i("MultiScreen_Manager", "pushImageTask result : [" + result + "](" + (System.currentTimeMillis() - begin) + ")");
            return result;
        }

        private boolean pushVideoTask() {
            long begin = System.currentTimeMillis();
            GalleryLog.i("MultiScreen_Manager", "start pushVideoTask");
            boolean result = MultiScreenManager.this.mClient.playMedia(this.mediaInfo.getUrl(), this.mediaInfo.getName(), this.mediaInfo.getMediaInfoType(), this.mediaInfo.getPosition(), this.mediaInfo.getVolume());
            GalleryLog.i("MultiScreen_Manager", "pushVideoTask result : [" + result + "](" + (System.currentTimeMillis() - begin) + ")");
            return result;
        }

        private boolean pushVideoThumbTask() {
            long begin = System.currentTimeMillis();
            GalleryLog.i("MultiScreen_Manager", "start pushVideoThumbTask");
            GalleryLog.i("MultiScreen_Manager", "compress cover : " + this.mediaInfo.getUrl());
            String path = MultiScreenUtils.getThumbnailPath(this.mediaInfo.getUrl());
            GalleryLog.i("MultiScreen_Manager", "compress cover finished : " + this.mediaInfo.getUrl() + "[" + path + "](" + (System.currentTimeMillis() - begin) + ")");
            this.mediaInfo.setUrl(path);
            this.mediaInfo.setMediaInfoType(EHwMediaInfoType.IMAGE);
            boolean result = MultiScreenManager.this.mClient.playMedia(this.mediaInfo, true, null);
            GalleryLog.i("MultiScreen_Manager", "pushVideoThumbTask result : [" + result + "](" + (System.currentTimeMillis() - begin) + ")");
            return result;
        }
    }

    private MultiScreenManager() {
    }

    public static synchronized MultiScreenManager getInstance() {
        MultiScreenManager multiScreenManager;
        synchronized (MultiScreenManager.class) {
            if (mInstance == null) {
                mInstance = new MultiScreenManager();
            }
            multiScreenManager = mInstance;
        }
        return multiScreenManager;
    }

    public void init(Context context) {
        this.mContext = context;
        this.mClient = PlayerClient.getInstance();
        if (this.mClient == null) {
            GalleryLog.e("MultiScreen_Manager", "init error : client is null");
            serviceReady.lazySet(false);
            return;
        }
        if (this.mClient.init(this.mContext)) {
            GalleryLog.i("MultiScreen_Manager", "init Client sucesseed!");
            serviceReady.lazySet(true);
            this.mEventHandler = new EventHandler();
            this.mClient.setHwSharingListener(this.mEventListener);
            this.mClient.subscribServers("7");
            startHandlerThread();
        } else {
            GalleryLog.e("MultiScreen_Manager", "init Client failed!");
            serviceReady.lazySet(false);
        }
    }

    private void startHandlerThread() {
        this.mHandlerThread = new HandlerThread(getClass().getName());
        try {
            this.mHandlerThread.start();
        } catch (IllegalThreadStateException e) {
            GalleryLog.e("MultiScreen_Manager", "startHandlerThread." + e.getMessage());
        }
        this.mOperationHandler = new Handler(this.mHandlerThread.getLooper());
    }

    public void unInit() {
        if (serviceReady.get()) {
            this.mHandlerThread.quit();
            this.mClient.unsubscribServers();
            this.mClient.clsHwSharingListener(this.mEventListener);
            this.mEventHandler.removeCallbacksAndMessages(null);
            this.mUnInitHandler.removeCallbacks(this.mUnInitRunnable);
            this.mClient.deInit();
            serviceReady.lazySet(false);
        }
    }

    public boolean play(MediaItem item, boolean force) {
        if (item == null) {
            return false;
        }
        boolean needPush = true;
        if (this.mEntry == null) {
            this.mEntry = new MediaEntry(item);
        } else {
            needPush = this.mEntry.updateEntry(item);
        }
        needPush = this.mClient.isRendering() ? !needPush ? force : true : false;
        if (needPush) {
            scheduleTask();
        }
        return needPush;
    }

    public boolean play(Uri uri, int position) {
        if (uri == null) {
            return false;
        }
        boolean needPush = this.mClient.isRendering();
        this.mEntry = new MediaEntry(uri);
        if (needPush) {
            scheduleTask(position);
        }
        return needPush;
    }

    public void enter() {
        GalleryLog.d("MultiScreen_Manager", "enter");
        if (this.mScreenState.getAndSet(true)) {
            this.mOperationHandler.post(this.mEnterTask);
        }
        this.mUnInitHandler.removeCallbacks(this.mUnInitRunnable);
    }

    public void exit() {
        this.mScreenState.compareAndSet(true, ((PowerManager) this.mContext.getSystemService("power")).isScreenOn());
        if (this.mScreenState.get()) {
            this.mEntry = null;
            this.mOperationHandler.removeCallbacksAndMessages(null);
            this.mOperationHandler.post(this.mExitTask);
        }
        this.mUnInitHandler.removeCallbacks(this.mUnInitRunnable);
        this.mUnInitHandler.postDelayed(this.mUnInitRunnable, 180000);
    }

    public void requestRefreshInfo() {
        this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(2006));
    }

    public Intent getDeviceSelectorInfo() {
        Intent intent = new Intent("com.huawei.android.airsharing.action.ACTION_DEVICE_SELECTOR");
        intent.putExtra("com.huawei.android.airsharing.DEVICE_SELECTOR_CALLER", MultiScreen.class.getName());
        return intent;
    }

    public void addListener(Listener l) {
        this.mListeners.add(l);
    }

    public void removeListener(Listener l) {
        this.mListeners.remove(l);
    }

    public void scheduleTask(int position) {
        if (this.mEntry != null) {
            MediaEntry entry = this.mEntry.copy();
            if (entry != null) {
                entry.setPosition(MultiScreenUtils.timeInt2String(position));
                this.mOperationHandler.post(new MultiScreenTask(entry, 102));
            }
        }
    }

    public void scheduleTask() {
        if (this.mEntry != null) {
            MediaEntry entry = this.mEntry.copy();
            if (entry != null) {
                this.mOperationHandler.post(new MultiScreenTask(entry, 105));
            }
        }
    }
}
