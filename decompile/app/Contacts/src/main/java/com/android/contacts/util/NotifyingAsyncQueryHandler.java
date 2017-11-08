package com.android.contacts.util;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import java.lang.ref.WeakReference;

public class NotifyingAsyncQueryHandler extends AsyncQueryHandler {
    private WeakReference<AsyncQueryListener> mListener;

    public interface AsyncQueryListener {
        void onQueryComplete(int i, Object obj, Cursor cursor);
    }

    public NotifyingAsyncQueryHandler(Context context, AsyncQueryListener listener) {
        super(context.getContentResolver());
        setQueryListener(listener);
    }

    public void setQueryListener(AsyncQueryListener listener) {
        this.mListener = new WeakReference(listener);
    }

    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        AsyncQueryListener listener = (AsyncQueryListener) this.mListener.get();
        if (listener != null) {
            listener.onQueryComplete(token, cookie, cursor);
        } else if (cursor != null) {
            cursor.close();
        }
    }
}
