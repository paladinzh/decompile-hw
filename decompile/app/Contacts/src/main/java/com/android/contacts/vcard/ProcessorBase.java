package com.android.contacts.vcard;

import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public abstract class ProcessorBase implements RunnableFuture<Object> {
    public abstract boolean cancel(boolean z);

    public abstract void cancelAndNotified(boolean z);

    public abstract int getType();

    public abstract boolean isCancelled();

    public abstract boolean isDone();

    public abstract void run();

    public final Object get() {
        throw new UnsupportedOperationException();
    }

    public final Object get(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    public VCardImportExportListener getListener() {
        return null;
    }
}
