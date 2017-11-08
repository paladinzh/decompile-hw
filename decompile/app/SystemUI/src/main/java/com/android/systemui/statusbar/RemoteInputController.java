package com.android.systemui.statusbar;

import android.util.ArraySet;
import com.android.internal.util.Preconditions;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RemoteInputController {
    private final ArrayList<Callback> mCallbacks = new ArrayList(3);
    private final HeadsUpManager mHeadsUpManager;
    private final ArrayList<WeakReference<Entry>> mOpen = new ArrayList();
    private final ArraySet<String> mSpinning = new ArraySet();

    public interface Callback {
        void onRemoteInputActive(boolean active) {
        }

        void onRemoteInputSent(Entry entry) {
        }
    }

    public RemoteInputController(StatusBarWindowManager sbwm, HeadsUpManager headsUpManager) {
        addCallback(sbwm);
        this.mHeadsUpManager = headsUpManager;
    }

    public void addRemoteInput(Entry entry) {
        Preconditions.checkNotNull(entry);
        if (!pruneWeakThenRemoveAndContains(entry, null)) {
            this.mOpen.add(new WeakReference(entry));
        }
        apply(entry);
    }

    public void removeRemoteInput(Entry entry) {
        Preconditions.checkNotNull(entry);
        pruneWeakThenRemoveAndContains(null, entry);
        apply(entry);
    }

    public void addSpinning(String key) {
        this.mSpinning.add(key);
    }

    public void removeSpinning(String key) {
        this.mSpinning.remove(key);
    }

    public boolean isSpinning(String key) {
        return this.mSpinning.contains(key);
    }

    private void apply(Entry entry) {
        this.mHeadsUpManager.setRemoteInputActive(entry, isRemoteInputActive(entry));
        boolean remoteInputActive = isRemoteInputActive();
        int N = this.mCallbacks.size();
        for (int i = 0; i < N; i++) {
            ((Callback) this.mCallbacks.get(i)).onRemoteInputActive(remoteInputActive);
        }
    }

    public boolean isRemoteInputActive(Entry entry) {
        return pruneWeakThenRemoveAndContains(entry, null);
    }

    public boolean isRemoteInputActive() {
        pruneWeakThenRemoveAndContains(null, null);
        return !this.mOpen.isEmpty();
    }

    private boolean pruneWeakThenRemoveAndContains(Entry contains, Entry remove) {
        boolean found = false;
        for (int i = this.mOpen.size() - 1; i >= 0; i--) {
            Entry item = (Entry) ((WeakReference) this.mOpen.get(i)).get();
            if (item == null || item == remove) {
                this.mOpen.remove(i);
            } else if (item == contains) {
                found = true;
            }
        }
        return found;
    }

    public void addCallback(Callback callback) {
        Preconditions.checkNotNull(callback);
        this.mCallbacks.add(callback);
    }

    public void remoteInputSent(Entry entry) {
        int N = this.mCallbacks.size();
        for (int i = 0; i < N; i++) {
            ((Callback) this.mCallbacks.get(i)).onRemoteInputSent(entry);
        }
    }

    public void closeRemoteInputs() {
        if (this.mOpen.size() != 0) {
            int i;
            Entry item;
            ArrayList<Entry> list = new ArrayList(this.mOpen.size());
            for (i = this.mOpen.size() - 1; i >= 0; i--) {
                item = (Entry) ((WeakReference) this.mOpen.get(i)).get();
                if (!(item == null || item.row == null)) {
                    list.add(item);
                }
            }
            for (i = list.size() - 1; i >= 0; i--) {
                item = (Entry) list.get(i);
                if (item.row != null) {
                    item.row.closeRemoteInput();
                }
            }
        }
    }
}
