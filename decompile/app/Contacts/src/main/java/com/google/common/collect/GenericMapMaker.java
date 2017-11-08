package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.MoreObjects;

@GwtCompatible(emulated = true)
@Deprecated
@Beta
abstract class GenericMapMaker<K0, V0> {
    @GwtIncompatible("To be supported")
    RemovalListener<K0, V0> removalListener;

    @GwtIncompatible("To be supported")
    enum NullListener implements RemovalListener<Object, Object> {
        INSTANCE;

        public void onRemoval(RemovalNotification<Object, Object> removalNotification) {
        }
    }

    GenericMapMaker() {
    }

    @GwtIncompatible("To be supported")
    <K extends K0, V extends V0> RemovalListener<K, V> getRemovalListener() {
        return (RemovalListener) MoreObjects.firstNonNull(this.removalListener, NullListener.INSTANCE);
    }
}
