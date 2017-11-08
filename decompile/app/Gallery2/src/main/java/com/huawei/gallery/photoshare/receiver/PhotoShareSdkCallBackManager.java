package com.huawei.gallery.photoshare.receiver;

import java.util.WeakHashMap;

public class PhotoShareSdkCallBackManager {
    public static final Object LOCK = new Object();
    private static PhotoShareSdkCallBackManager instance;
    private WeakHashMap<MyListener, Object> listeners = new WeakHashMap();

    public interface MyListener {
        void onContentChange(int i, String str);

        void onFolderChange(int i);

        void onTagContentChanged(String str, String str2);

        void onTagListChanged(String str);
    }

    public static class SdkListener implements MyListener {
        public void onContentChange(int type, String mPath) {
        }

        public void onFolderChange(int type) {
        }

        public void onTagListChanged(String categoryID) {
        }

        public void onTagContentChanged(String categoryID, String tagID) {
        }
    }

    public static synchronized PhotoShareSdkCallBackManager getInstance() {
        PhotoShareSdkCallBackManager photoShareSdkCallBackManager;
        synchronized (PhotoShareSdkCallBackManager.class) {
            if (instance == null) {
                instance = new PhotoShareSdkCallBackManager();
            }
            photoShareSdkCallBackManager = instance;
        }
        return photoShareSdkCallBackManager;
    }

    public WeakHashMap<MyListener, Object> getListeners() {
        return this.listeners;
    }

    public void addListener(MyListener listener) {
        synchronized (LOCK) {
            this.listeners.put(listener, null);
        }
    }
}
