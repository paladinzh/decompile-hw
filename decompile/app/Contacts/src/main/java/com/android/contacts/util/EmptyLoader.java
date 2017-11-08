package com.android.contacts.util;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

public class EmptyLoader extends Loader<Object> {

    public static class Callback implements LoaderCallbacks<Object> {
        private final Context mContext;

        public Callback(Context context) {
            this.mContext = context.getApplicationContext();
        }

        public Loader<Object> onCreateLoader(int id, Bundle args) {
            return new EmptyLoader(this.mContext);
        }

        public void onLoadFinished(Loader<Object> loader, Object data) {
        }

        public void onLoaderReset(Loader<Object> loader) {
        }
    }

    public EmptyLoader(Context context) {
        super(context);
    }
}
