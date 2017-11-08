package com.fyusion.sdk.common.ext.filter;

import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.a.a;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class EditFilterCollection implements Cloneable {
    private boolean a = true;
    private Map<Class, FilterControl> b = new HashMap();
    private List<FilterUpdateListener> c = new ArrayList();
    private volatile boolean d = false;

    /* compiled from: Unknown */
    public interface FilterUpdateListener {
        void onFilterChainUpdated(EditFilterCollection editFilterCollection);
    }

    private void a(@NonNull FilterControl filterControl) {
        synchronized (this) {
            if (filterControl instanceof ToneCurveFilter) {
                this.d = a();
            }
            this.b.put(filterControl.getImplementationClass(), filterControl);
            this.d = true;
        }
        b();
    }

    private boolean a() {
        boolean z = false;
        Iterator it = this.b.values().iterator();
        while (true) {
            boolean z2 = z;
            if (!it.hasNext()) {
                return z2;
            }
            if (((FilterControl) it.next()) instanceof ToneCurveFilter) {
                it.remove();
                z = true;
            } else {
                z = z2;
            }
        }
    }

    private void b() {
        if (this.d) {
            for (FilterUpdateListener onFilterChainUpdated : this.c) {
                onFilterChainUpdated.onFilterChainUpdated(this);
            }
        }
    }

    public void addFilter(@NonNull FilterControl filterControl) {
        a(filterControl);
    }

    public void addListener(@NonNull FilterUpdateListener filterUpdateListener) {
        this.c.add(filterUpdateListener);
    }

    public Object clone() throws CloneNotSupportedException {
        EditFilterCollection editFilterCollection = new EditFilterCollection();
        editFilterCollection.put(this);
        return editFilterCollection;
    }

    public FilterControl getFilterControl(@NonNull Class cls) {
        return (FilterControl) this.b.get(cls);
    }

    public synchronized List<FilterControl> getFilterControls() {
        List<FilterControl> arrayList;
        arrayList = new ArrayList();
        arrayList.addAll(this.b.values());
        return arrayList;
    }

    public void initWith(Collection<ImageFilter> collection) {
        for (ImageFilter imageFilter : collection) {
            this.b.put(imageFilter.getClass(), ((a) imageFilter).c());
        }
    }

    public synchronized boolean isEnabled() {
        return this.a;
    }

    public synchronized boolean isUpdated() {
        return this.d;
    }

    public void put(EditFilterCollection editFilterCollection) {
        try {
            synchronized (this) {
                this.b.clear();
                for (Entry entry : editFilterCollection.b.entrySet()) {
                    this.b.put(entry.getKey(), (FilterControl) ((FilterControl) entry.getValue()).clone());
                }
                this.d = true;
            }
            b();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void removeListener(@NonNull FilterUpdateListener filterUpdateListener) {
        this.c.remove(filterUpdateListener);
    }

    public void removeListeners() {
        this.c.clear();
    }

    public void removeOtherFilters() {
        synchronized (this) {
            Iterator it = this.b.values().iterator();
            while (it.hasNext()) {
                if (!(((FilterControl) it.next()) instanceof ToneCurveFilter)) {
                    it.remove();
                    this.d = true;
                }
            }
        }
        b();
    }

    public void removeToneCurveFilter() {
        synchronized (this) {
            this.d = a();
        }
        b();
    }

    public void reset() {
        Object obj = null;
        synchronized (this) {
            if (this.b.size() > 0) {
                this.b.clear();
                this.d = true;
                int i = 1;
            }
        }
        if (obj != null) {
            b();
        }
    }

    public synchronized void resetUpdate() {
        this.d = false;
    }

    public void setEnabled(boolean z) {
        Object obj = null;
        synchronized (this) {
            if (this.a != z) {
                this.a = z;
                this.d = true;
                int i = 1;
            }
        }
        if (obj != null) {
            b();
        }
    }
}
