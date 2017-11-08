package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public interface Trash {
    public static final String TAG = "Trash";
    public static final String TAG_CLEAN = "Trash_Clean";
    public static final int TYPE_APK_CUSTOM_DATA = 16384;
    public static final int TYPE_APK_DATA = 262144;
    public static final int TYPE_APK_FILE = 1024;
    public static final int TYPE_APK_INNER_CACHE = 1;
    public static final int TYPE_APK_UNINSTALLED_DATA = 8192;
    public static final int TYPE_APP_PROCESS = 32768;
    public static final int TYPE_AUDIO_FILE = 512;
    public static final int TYPE_BACKUP_FILE = 2097152;
    public static final int TYPE_BLUR_PHOTO = 8388608;
    public static final int TYPE_CUSTOM_AND_TOPVIDEO = 81920;
    public static final int TYPE_EMPTY_FILE = 32;
    public static final int TYPE_JUMP = 131072;
    public static final int TYPE_KIND = 24;
    public static final int TYPE_LARGET_FILE = 4;
    public static final int TYPE_LOG_FILE = 8;
    public static final int TYPE_PHOTO_FILE = 128;
    public static final int TYPE_PREINSTALL_APP = 524288;
    public static final int TYPE_SIMILAR_PHOTO = 4194304;
    public static final int TYPE_TEMP_FILE = 16;
    public static final int TYPE_THUMBNAILS = 2048;
    public static final int TYPE_TOP_VIDEO_APP_FILE = 65536;
    public static final int TYPE_UNKNOW = 0;
    public static final int TYPE_UNUSED_APP = 2;
    public static final int TYPE_VIDEO_FILE = 256;
    public static final int TYPE_WECHAT = 1048576;

    public static abstract class SimpleTrash implements Trash {
        private volatile boolean mCleaned = false;

        public List<String> getFiles() {
            return Collections.emptyList();
        }

        public boolean isValidate() {
            return true;
        }

        public boolean removeFile(String path) {
            return false;
        }

        public boolean cleanFile(String path) {
            return false;
        }

        public boolean isCleaned() {
            return this.mCleaned;
        }

        protected final void setCleaned() {
            this.mCleaned = true;
        }

        public long getTrashSize(int position) {
            if (position == getPosition() || position == 1) {
                return getTrashSize();
            }
            return 0;
        }

        public long getTrashSizeCleaned(boolean cleaned) {
            if ((isCleaned() ^ cleaned) != 0) {
                return 0;
            }
            return getTrashSize();
        }

        public boolean isNormal() {
            return isSuggestClean();
        }

        public int getTrashCount() {
            return 1;
        }

        public void refreshContent() {
        }

        public String getUniqueDes() {
            return null;
        }

        public Trash findTrashByuniqueDes(String uniqueDes) {
            return null;
        }

        public short getYear() {
            return (short) 0;
        }

        public byte getMonth() {
            return (byte) 0;
        }
    }

    boolean clean(Context context);

    boolean cleanFile(String str);

    Trash findTrashByuniqueDes(String str);

    List<String> getFiles();

    byte getMonth();

    String getName();

    int getPosition();

    int getTrashCount();

    long getTrashSize();

    long getTrashSize(int i);

    long getTrashSizeCleaned(boolean z);

    int getType();

    String getUniqueDes();

    short getYear();

    boolean isCleaned();

    boolean isNormal();

    boolean isSuggestClean();

    boolean isValidate();

    void printf(Appendable appendable) throws IOException;

    void refreshContent();

    boolean removeFile(String str);
}
