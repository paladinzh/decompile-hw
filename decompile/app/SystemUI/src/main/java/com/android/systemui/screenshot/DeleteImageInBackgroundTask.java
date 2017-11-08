package com.android.systemui.screenshot;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

/* compiled from: GlobalScreenshot */
class DeleteImageInBackgroundTask extends AsyncTask<Uri, Void, Void> {
    private Context mContext;

    DeleteImageInBackgroundTask(Context context) {
        this.mContext = context;
    }

    protected Void doInBackground(Uri... params) {
        if (params.length != 1) {
            return null;
        }
        this.mContext.getContentResolver().delete(params[0], null, null);
        return null;
    }
}
