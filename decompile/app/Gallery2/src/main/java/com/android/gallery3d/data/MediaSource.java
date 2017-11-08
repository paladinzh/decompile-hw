package com.android.gallery3d.data;

import android.net.Uri;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.GalleryLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public abstract class MediaSource {
    private String mPrefix;

    public static class IdComparator implements Comparator<PathId>, Serializable {
        private static final long serialVersionUID = 1;

        public int compare(PathId p1, PathId p2) {
            String s1 = p1.path.getSuffix();
            String s2 = p2.path.getSuffix();
            int len1 = s1.length();
            int len2 = s2.length();
            if (len1 < len2) {
                return -1;
            }
            if (len1 > len2) {
                return 1;
            }
            return s1.compareTo(s2);
        }
    }

    public static class PathId {
        public int id;
        public Path path;

        public PathId(Path path, int id) {
            this.path = path;
            this.id = id;
        }
    }

    public abstract MediaObject createMediaObject(Path path);

    protected MediaSource(String prefix) {
        this.mPrefix = prefix;
    }

    public String getPrefix() {
        return this.mPrefix;
    }

    public Path findPathByUri(Uri uri, String type) {
        return null;
    }

    public void pause() {
    }

    public void resume() {
    }

    public Path getDefaultSetOf(Path item) {
        return null;
    }

    public void mapMediaItems(ArrayList<PathId> list, ItemConsumer consumer) {
        int n = list.size();
        for (int i = 0; i < n; i++) {
            PathId pid = (PathId) list.get(i);
            synchronized (DataManager.LOCK) {
                MediaObject obj = pid.path.getObject();
                if (obj == null) {
                    try {
                        obj = createMediaObject(pid.path);
                    } catch (Throwable th) {
                        GalleryLog.w("MediaSource", "cannot create media object: " + pid.path + "." + th.getMessage());
                    }
                }
            }
            if (obj != null) {
                consumer.consume(pid.id, (MediaItem) obj);
            }
        }
    }
}
