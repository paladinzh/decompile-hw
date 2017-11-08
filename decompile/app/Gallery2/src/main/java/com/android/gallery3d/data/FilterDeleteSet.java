package com.android.gallery3d.data;

import java.util.ArrayList;

public class FilterDeleteSet extends MediaSet implements ContentListener {
    private final MediaSet mBaseSet;
    private ArrayList<Deletion> mCurrent = new ArrayList();
    private ArrayList<Request> mRequests = new ArrayList();

    private static class Deletion {
        int index;
        Path path;

        public Deletion(Path path, int index) {
            this.path = path;
            this.index = index;
        }
    }

    private static class Request {
        int indexHint;
        Path path;
        int type;
    }

    public FilterDeleteSet(Path path, MediaSet baseSet) {
        super(path, -1);
        this.mBaseSet = baseSet;
        this.mBaseSet.addContentListener(this);
    }

    public String getName() {
        return this.mBaseSet.getName();
    }

    public int getMediaItemCount() {
        return this.mBaseSet.getMediaItemCount() - this.mCurrent.size();
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        if (count <= 0) {
            return new ArrayList();
        }
        int end = (start + count) - 1;
        int n = this.mCurrent.size();
        int i = 0;
        while (i < n && ((Deletion) this.mCurrent.get(i)).index - i <= start) {
            i++;
        }
        int j = i;
        while (j < n && ((Deletion) this.mCurrent.get(j)).index - j <= end) {
            j++;
        }
        ArrayList<MediaItem> base = this.mBaseSet.getMediaItem(start + i, (j - i) + count);
        for (int m = j - 1; m >= i; m--) {
            base.remove(((Deletion) this.mCurrent.get(m)).index - (start + i));
        }
        return base;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long reload() {
        boolean newData = this.mBaseSet.reload() > this.mDataVersion;
        synchronized (this.mRequests) {
            if (!newData) {
                if (this.mRequests.isEmpty()) {
                    long j = this.mDataVersion;
                    return j;
                }
            }
            for (int i = 0; i < this.mRequests.size(); i++) {
                Request r = (Request) this.mRequests.get(i);
                int n;
                int j2;
                switch (r.type) {
                    case 1:
                        n = this.mCurrent.size();
                        j2 = 0;
                        while (j2 < n && ((Deletion) this.mCurrent.get(j2)).path != r.path) {
                            j2++;
                        }
                        if (j2 != n) {
                            break;
                        }
                        this.mCurrent.add(new Deletion(r.path, r.indexHint));
                        break;
                        break;
                    case 2:
                        n = this.mCurrent.size();
                        for (j2 = 0; j2 < n; j2++) {
                            if (((Deletion) this.mCurrent.get(j2)).path == r.path) {
                                this.mCurrent.remove(j2);
                                break;
                            }
                        }
                        break;
                    case 3:
                        this.mCurrent.clear();
                        break;
                    default:
                        break;
                }
            }
            this.mRequests.clear();
        }
    }

    public void onContentDirty() {
        notifyContentChanged();
    }
}
