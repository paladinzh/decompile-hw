package android.rms.resource;

import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.IMultiTaskProcessObserver.Stub;
import android.app.mtm.MultiTaskManager;
import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.rms.HwSysResImpl;
import android.util.Log;
import com.huawei.hsm.permission.StubController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ProviderResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "ProviderResourceManager";
    private static ProviderResource mInstance = null;
    private int forgroundUid = -1;
    private final ObserverCacheNode mDelayedNode = new ObserverCacheNode("MultiTaskProviderManager");
    private MultiTaskManager mMultiTaskManager = null;
    private IMultiTaskProcessObserver mMultiTaskProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                ProviderResource.this.forgroundUid = uid;
                ProviderResource.this.notifyForegroundChanged(pid, uid);
            }
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }

        public void onProcessDied(int pid, int uid) {
            ProviderResource.this.removeObserverFromCache(pid, uid);
        }
    };

    public static final class ObserverCacheNode {
        private String mName;
        private ArrayList<ObserverCacheEntry> mObservers = new ArrayList();

        private class ObserverCacheEntry implements DeathRecipient {
            public final boolean mSelfChange;
            public final IContentObserver observer;
            private final Object observersLock;
            public final int pid;
            public final int uid;
            public final Uri uri;
            private final int userHandle;

            public ObserverCacheEntry(IContentObserver o, boolean SelfChange, Object observersLock, int _uid, int _pid, int _userHandle, Uri _uri) {
                this.observersLock = observersLock;
                this.observer = o;
                this.uid = _uid;
                this.pid = _pid;
                this.userHandle = _userHandle;
                this.mSelfChange = SelfChange;
                this.uri = _uri;
                try {
                    this.observer.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    binderDied();
                }
            }

            public void binderDied() {
                Log.w(ProviderResource.TAG, "Found dead observer in Caching entry pid is " + this.pid + ", remove it");
                synchronized (this.observersLock) {
                    ObserverCacheNode.this.removeObserverLocked(this.uid, this.pid, this.observer, this.uri);
                }
            }
        }

        private void addObserverLocked(Uri uri, int index, IContentObserver observer, boolean SelfChange, Object observersLock, int uid, int pid, int userHandle) {
            int N = this.mObservers.size();
            IBinder observerBinder = observer.asBinder();
            for (int i = 0; i < N; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid && entry.pid == pid) {
                    if (uri.equals(entry.uri) && entry.observer.asBinder() == observerBinder) {
                        if (Log.HWINFO) {
                            Log.d(ProviderResource.TAG, "Delayed entry is exist:uid=" + entry.uid + "pid=" + entry.pid + "update at " + entry.uri);
                        }
                        return;
                    }
                }
            }
            if (Log.HWINFO) {
                Log.i(ProviderResource.TAG, "add to the cache uid= " + uid + "pid=" + pid + "uri=" + uri);
            }
            this.mObservers.add(new ObserverCacheEntry(observer, SelfChange, observersLock, uid, pid, userHandle, uri));
        }

        public ObserverCacheNode(String name) {
            this.mName = name;
        }

        protected void dumpCache(FileDescriptor fd, PrintWriter pw) {
            int N = this.mObservers.size();
            pw.println("Provider Cached  observer is : ");
            pw.println();
            for (int i = 0; i < N; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null) {
                    pw.println("Cached notify observer:" + entry.observer + " of " + "update at " + entry.uri + " entry.pid=" + entry.pid + " entry.uid=" + entry.uid);
                }
            }
            pw.println();
        }

        public void addObserverLocked(Uri uri, IContentObserver observer, boolean SelfChange, Object observersLock, int uid, int pid, int userHandle) {
            addObserverLocked(uri, 0, observer, SelfChange, observersLock, uid, pid, userHandle);
        }

        public void removeObserverLocked(int uid, int pid, IContentObserver observer, Uri uri) {
            int size = this.mObservers.size();
            IBinder observerBinder = observer.asBinder();
            for (int i = 0; i < size; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid && entry.pid == pid && entry.uri.equals(uri) && entry.observer.asBinder() == observerBinder) {
                    if (Log.HWINFO) {
                        Log.d(ProviderResource.TAG, "move the delay observer: pid=" + pid + "uid=" + uid + "uri=" + uri);
                    }
                    this.mObservers.remove(i);
                    return;
                }
            }
        }

        public void removeObserverLocked(int uid, int pid) {
            int size = this.mObservers.size();
            int i = 0;
            while (i < size) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid && entry.pid == pid) {
                    if (Log.HWINFO) {
                        Log.d(ProviderResource.TAG, "move the delay observer: pid=" + entry.pid + "uid=" + entry.uid + "uri=" + entry.uri);
                    }
                    this.mObservers.remove(i);
                    i--;
                    size--;
                }
                i++;
            }
        }

        public void collectMyDelayedObserversLocked(int uid, int pid, ArrayList<ObserverDelayCall> calls) {
            int N = this.mObservers.size();
            for (int i = 0; i < N; i++) {
                ObserverCacheEntry entry = (ObserverCacheEntry) this.mObservers.get(i);
                if (entry != null && entry.uid == uid) {
                    if (Log.HWINFO) {
                        Log.d(ProviderResource.TAG, "Find the delayed notify observer:" + entry.observer + " of " + "update at " + entry.uri + " pid=" + pid + " uid=" + uid + " entry.pid=" + entry.pid + " entry.uid=" + entry.uid);
                    }
                    calls.add(new ObserverDelayCall(entry.observer, entry.mSelfChange, entry.pid, entry.uid, entry.uri, entry.userHandle));
                }
            }
        }
    }

    public static final class ObserverDelayCall {
        final IContentObserver mObserver;
        final boolean mSelfChange;
        final int pid;
        final int uid;
        final Uri uri;
        final int userHandle;

        ObserverDelayCall(IContentObserver observer, boolean selfChange, int _pid, int _uid, Uri _uri, int _userHandle) {
            this.mObserver = observer;
            this.mSelfChange = selfChange;
            this.pid = _pid;
            this.uid = _uid;
            this.uri = _uri;
            this.userHandle = _userHandle;
        }
    }

    private MultiTaskManager getMultiTaskManager() {
        if (this.mMultiTaskManager == null) {
            this.mMultiTaskManager = MultiTaskManager.getInstance();
        }
        return this.mMultiTaskManager;
    }

    private void notifyForegroundChanged(int pid, int uid) {
        ArrayList<ObserverDelayCall> calls = new ArrayList();
        synchronized (this.mDelayedNode) {
            this.mDelayedNode.collectMyDelayedObserversLocked(uid, pid, calls);
        }
        int numCalls = calls.size();
        int i = 0;
        while (i < numCalls) {
            ObserverDelayCall oc = (ObserverDelayCall) calls.get(i);
            ObserverCacheNode observerCacheNode;
            try {
                oc.mObserver.onChange(oc.mSelfChange, oc.uri, oc.userHandle);
                if (Log.HWINFO) {
                    Log.d(TAG, "Delayed DataChange Notify is finished pid =" + oc.pid + " observer is " + oc.mObserver + " of " + "update at " + oc.uri);
                }
                observerCacheNode = this.mDelayedNode;
                synchronized (observerCacheNode) {
                    this.mDelayedNode.removeObserverLocked(oc.uid, oc.pid, oc.mObserver, oc.uri);
                    i++;
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Found dead observer pid is " + oc.pid + ", remove it");
                observerCacheNode = this.mDelayedNode;
                synchronized (observerCacheNode) {
                    this.mDelayedNode.removeObserverLocked(oc.uid, oc.pid, oc.mObserver, oc.uri);
                }
            } catch (Throwable th) {
                synchronized (this.mDelayedNode) {
                    this.mDelayedNode.removeObserverLocked(oc.uid, oc.pid, oc.mObserver, oc.uri);
                }
            }
        }
    }

    private void removeObserverFromCache(int pid, int uid) {
        synchronized (this.mDelayedNode) {
            this.mDelayedNode.removeObserverLocked(uid, pid);
        }
    }

    private int observerMultiTaskPolicy(Uri uri, int pid, int uid) {
        MultiTaskPolicy policy = null;
        String dataBaseName = uri.getScheme() + "://" + uri.getAuthority();
        if (this.mMultiTaskManager != null) {
            Bundle args = new Bundle();
            args.putInt(FreezeScreenScene.PID_PARAM, pid);
            args.putInt(StubController.TABLE_COLUM_UID, uid);
            policy = this.mMultiTaskManager.getMultiTaskPolicy(15, dataBaseName, 1, args);
        }
        if (policy != null) {
            return policy.getPolicy();
        }
        return 1;
    }

    public ProviderResource() {
        getMultiTaskManager();
        if (this.mMultiTaskManager != null) {
            this.mMultiTaskManager.registerObserver(this.mMultiTaskProcessObserver);
            if (Log.HWINFO) {
                Log.d(TAG, "registered MultiTaskProcess");
            }
        }
    }

    private boolean isObserverResourceManaged(Uri _uri, IContentObserver _observer, int _pid, int _uid, boolean _mSelfChange, int _userHandle) {
        if (this.mMultiTaskManager == null) {
            getMultiTaskManager();
            if (this.mMultiTaskManager != null) {
                this.mMultiTaskManager.registerObserver(this.mMultiTaskProcessObserver);
                if (Log.HWINFO) {
                    Log.d(TAG, "registered MultiTaskProcess");
                }
            }
            return false;
        } else if (_pid < 0 || _uid < 0) {
            return false;
        } else {
            if (_uid < StubController.MIN_APPLICATION_UID || _uid == this.forgroundUid) {
                return false;
            }
            switch (observerMultiTaskPolicy(_uri, _pid, _uid)) {
                case 2:
                    if (Log.HWINFO) {
                        Log.d(TAG, "ForBid policy pid is " + _pid + " uid is " + _uid + " database is " + _uri);
                        break;
                    }
                    break;
                case 4:
                    if (!_mSelfChange) {
                        synchronized (this.mDelayedNode) {
                            this.mDelayedNode.addObserverLocked(_uri, _observer, _mSelfChange, this.mDelayedNode, _uid, _pid, _userHandle);
                        }
                        if (Log.HWINFO) {
                            Log.d(TAG, "Delay policy  pid is " + _pid + " uid is " + _uid + " database is " + _uri);
                        }
                        return true;
                    }
                    break;
            }
            return false;
        }
    }

    public static synchronized ProviderResource getInstance() {
        ProviderResource providerResource;
        synchronized (ProviderResource.class) {
            if (mInstance == null) {
                mInstance = new ProviderResource();
                if (Log.HWINFO) {
                    Log.d(TAG, "getInstance create new provider resource");
                }
            }
            providerResource = mInstance;
        }
        return providerResource;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        if (!(args == null || observer == null)) {
            int _pid = args.getInt("PID");
            int _uid = args.getInt("UID");
            int _userHandle = args.getInt("USERHANDLE");
            if (isObserverResourceManaged(uri, observer, _pid, _uid, args.getBoolean("SELFCHANGE"), _userHandle)) {
                return 3;
            }
        }
        return 1;
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
        synchronized (this.mDelayedNode) {
            this.mDelayedNode.dumpCache(fd, pw);
        }
    }
}
