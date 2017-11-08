package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: FileLockTable */
class SharedFileLockTable extends FileLockTable {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static ConcurrentHashMap<FileKey, List<FileLockReference>> lockMap = new ConcurrentHashMap();
    private static ReferenceQueue<FileLock> queue = new ReferenceQueue();
    private final Channel channel;
    private final FileKey fileKey;

    /* compiled from: FileLockTable */
    private static class FileLockReference extends WeakReference<FileLock> {
        private FileKey fileKey;

        FileLockReference(FileLock referent, ReferenceQueue<FileLock> queue, FileKey key) {
            super(referent, queue);
            this.fileKey = key;
        }

        FileKey fileKey() {
            return this.fileKey;
        }
    }

    static {
        boolean z;
        if (SharedFileLockTable.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    SharedFileLockTable(Channel channel, FileDescriptor fd) throws IOException {
        this.channel = channel;
        this.fileKey = FileKey.create(fd);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void add(FileLock fl) throws OverlappingFileLockException {
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        while (true) {
            List<FileLockReference> list2;
            if (list == null) {
                List<FileLockReference> prev;
                list = new ArrayList(2);
                synchronized (list) {
                    prev = (List) lockMap.putIfAbsent(this.fileKey, list);
                    if (prev == null) {
                        break;
                    }
                }
                list = prev;
            }
            synchronized (list) {
                List<FileLockReference> current = (List) lockMap.get(this.fileKey);
                if (list == current) {
                    break;
                }
                list2 = current;
            }
            list = list2;
        }
        list.add(new FileLockReference(fl, queue, this.fileKey));
        removeStaleEntries();
    }

    private void removeKeyIfEmpty(FileKey fk, List<FileLockReference> list) {
        if (-assertionsDisabled || Thread.holdsLock(list)) {
            if (!-assertionsDisabled) {
                if ((lockMap.get(fk) == list ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (list.isEmpty()) {
                lockMap.remove(fk);
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    public void remove(FileLock fl) {
        Object obj = 1;
        if (!-assertionsDisabled) {
            if ((fl != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = (FileLockReference) list.get(index);
                    FileLock lock = (FileLock) ref.get();
                    if (lock == fl) {
                        if (!-assertionsDisabled) {
                            if (lock == null || lock.acquiredBy() != this.channel) {
                                obj = null;
                            }
                            if (obj == null) {
                                throw new AssertionError();
                            }
                        }
                        ref.clear();
                        list.remove(index);
                    } else {
                        index++;
                    }
                }
            }
        }
    }

    public List<FileLock> removeAll() {
        List<FileLock> result = new ArrayList();
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = (FileLockReference) list.get(index);
                    FileLock lock = (FileLock) ref.get();
                    if (lock == null || lock.acquiredBy() != this.channel) {
                        index++;
                    } else {
                        ref.clear();
                        list.remove(index);
                        result.add(lock);
                    }
                }
                removeKeyIfEmpty(this.fileKey, list);
            }
        }
        return result;
    }

    public void replace(FileLock fromLock, FileLock toLock) {
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (!-assertionsDisabled) {
            if ((list != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        synchronized (list) {
            for (int index = 0; index < list.size(); index++) {
                FileLockReference ref = (FileLockReference) list.get(index);
                if (((FileLock) ref.get()) == fromLock) {
                    ref.clear();
                    list.set(index, new FileLockReference(toLock, queue, this.fileKey));
                    break;
                }
            }
        }
    }

    private void checkList(List<FileLockReference> list, long position, long size) throws OverlappingFileLockException {
        if (-assertionsDisabled || Thread.holdsLock(list)) {
            for (FileLockReference ref : list) {
                FileLock fl = (FileLock) ref.get();
                if (fl != null && fl.overlaps(position, size)) {
                    throw new OverlappingFileLockException();
                }
            }
            return;
        }
        throw new AssertionError();
    }

    private void removeStaleEntries() {
        while (true) {
            Object ref = (FileLockReference) queue.poll();
            if (ref != null) {
                FileKey fk = ref.fileKey();
                List<FileLockReference> list = (List) lockMap.get(fk);
                if (list != null) {
                    synchronized (list) {
                        list.remove(ref);
                        removeKeyIfEmpty(fk, list);
                    }
                }
            } else {
                return;
            }
        }
    }
}
