package com.android.mms.util;

import android.net.Uri;

public class NullItemLoadedFuture implements ItemLoadedFuture {
    public void cancel(Uri uri) {
    }

    public boolean isDone() {
        return true;
    }

    public void setIsDone(boolean done) {
    }
}
