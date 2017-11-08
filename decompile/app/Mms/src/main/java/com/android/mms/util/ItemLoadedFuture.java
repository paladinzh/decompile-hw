package com.android.mms.util;

import android.net.Uri;

public interface ItemLoadedFuture {
    void cancel(Uri uri);

    boolean isDone();

    void setIsDone(boolean z);
}
