package android.support.v17.leanback.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import java.util.Map;
import java.util.Map.Entry;

class ViewsStateBundle {
    private LruCache<String, SparseArray<Parcelable>> mChildStates;
    private int mLimitNumber = 100;
    private int mSavePolicy = 0;

    public void clear() {
        if (this.mChildStates != null) {
            this.mChildStates.evictAll();
        }
    }

    public void remove(int id) {
        if (this.mChildStates != null && this.mChildStates.size() != 0) {
            this.mChildStates.remove(getSaveStatesKey(id));
        }
    }

    public final Bundle saveAsBundle() {
        if (this.mChildStates == null || this.mChildStates.size() == 0) {
            return null;
        }
        Map<String, SparseArray<Parcelable>> snapshot = this.mChildStates.snapshot();
        Bundle bundle = new Bundle();
        for (Entry<String, SparseArray<Parcelable>> e : snapshot.entrySet()) {
            bundle.putSparseParcelableArray((String) e.getKey(), (SparseArray) e.getValue());
        }
        return bundle;
    }

    public final void loadFromBundle(Bundle savedBundle) {
        if (this.mChildStates != null && savedBundle != null) {
            this.mChildStates.evictAll();
            for (String key : savedBundle.keySet()) {
                this.mChildStates.put(key, savedBundle.getSparseParcelableArray(key));
            }
        }
    }

    public final void loadView(View view, int id) {
        if (this.mChildStates != null) {
            SparseArray<Parcelable> container = (SparseArray) this.mChildStates.remove(getSaveStatesKey(id));
            if (container != null) {
                view.restoreHierarchyState(container);
            }
        }
    }

    protected final void saveViewUnchecked(View view, int id) {
        if (this.mChildStates != null) {
            String key = getSaveStatesKey(id);
            SparseArray<Parcelable> container = new SparseArray();
            view.saveHierarchyState(container);
            this.mChildStates.put(key, container);
        }
    }

    public final Bundle saveOnScreenView(Bundle bundle, View view, int id) {
        if (this.mSavePolicy != 0) {
            String key = getSaveStatesKey(id);
            SparseArray<Parcelable> container = new SparseArray();
            view.saveHierarchyState(container);
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putSparseParcelableArray(key, container);
        }
        return bundle;
    }

    public final void saveOffscreenView(View view, int id) {
        switch (this.mSavePolicy) {
            case 1:
                remove(id);
                return;
            case 2:
            case 3:
                saveViewUnchecked(view, id);
                return;
            default:
                return;
        }
    }

    static String getSaveStatesKey(int id) {
        return Integer.toString(id);
    }
}
