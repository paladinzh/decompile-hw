package com.android.mms.attachment.datamodel.data;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import com.android.mms.attachment.datamodel.BoundCursorLoader;
import com.android.mms.attachment.datamodel.GalleryBoundCursorLoader;
import com.android.mms.attachment.datamodel.binding.BindableData;
import com.android.mms.attachment.datamodel.binding.BindingBase;
import com.huawei.cspcommon.MLog;

public class MediaPickerData extends BindableData {
    private final Context mContext;
    private final GalleryLoaderCallbacks mGalleryLoaderCallbacks = new GalleryLoaderCallbacks();
    private MediaPickerDataListener mListener;
    private LoaderManager mLoaderManager;

    private class GalleryLoaderCallbacks implements LoaderCallbacks<Cursor> {
        private GalleryLoaderCallbacks() {
        }

        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String bindingId = args.getString("bindingId");
            if (MediaPickerData.this.isBound(bindingId)) {
                switch (id) {
                    case 1:
                        return new GalleryBoundCursorLoader(bindingId, MediaPickerData.this.mContext);
                    default:
                        MLog.w("MediaPickerData", "Unknown loader id for gallery picker!");
                        break;
                }
            }
            MLog.w("MediaPickerData", "Loader created after unbinding the media picker");
            return null;
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (MediaPickerData.this.isBound(((BoundCursorLoader) loader).getBindingId())) {
                switch (loader.getId()) {
                    case 1:
                        MediaPickerData.this.mListener.onMediaPickerDataUpdated(MediaPickerData.this, data, 1);
                        return;
                    default:
                        MLog.w("MediaPickerData", "Unknown loader id for gallery picker!");
                        return;
                }
            }
            MLog.w("MediaPickerData", "Loader finished after unbinding the media picker");
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            if (MediaPickerData.this.isBound(((BoundCursorLoader) loader).getBindingId())) {
                switch (loader.getId()) {
                    case 1:
                        MediaPickerData.this.mListener.onMediaPickerDataUpdated(MediaPickerData.this, null, 1);
                        return;
                    default:
                        MLog.w("MediaPickerData", "Unknown loader id for media picker!");
                        return;
                }
            }
            MLog.w("MediaPickerData", "Loader reset after unbinding the media picker");
        }
    }

    public interface MediaPickerDataListener {
        void onMediaPickerDataUpdated(MediaPickerData mediaPickerData, Object obj, int i);
    }

    public MediaPickerData(Context context) {
        this.mContext = context;
    }

    public void startLoader(int loaderId, BindingBase<MediaPickerData> binding, Bundle args, MediaPickerDataListener listener) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString("bindingId", binding.getBindingId());
        if (loaderId == 1) {
            this.mLoaderManager.initLoader(loaderId, args, this.mGalleryLoaderCallbacks).forceLoad();
        } else {
            MLog.w("MediaPickerData", "Unsupported loader id for media picker!");
        }
        this.mListener = listener;
    }

    public void destroyLoader(int loaderId) {
        this.mLoaderManager.destroyLoader(loaderId);
    }

    public void init(LoaderManager loaderManager) {
        this.mLoaderManager = loaderManager;
    }

    protected void unregisterListeners() {
        if (this.mLoaderManager != null) {
            this.mLoaderManager.destroyLoader(1);
            this.mLoaderManager = null;
        }
    }
}
