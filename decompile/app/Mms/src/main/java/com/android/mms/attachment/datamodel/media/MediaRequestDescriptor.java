package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import com.android.mms.attachment.datamodel.binding.BindableMediaRequest;
import com.android.mms.attachment.datamodel.media.MediaResourceManager.MediaResourceLoadListener;

public abstract class MediaRequestDescriptor<T extends RefCountedMediaResource> {
    public abstract MediaRequest<T> buildSyncMediaRequest(Context context);

    public BindableMediaRequest<T> buildAsyncMediaRequest(Context context, MediaResourceLoadListener<T> listener) {
        return AsyncMediaRequestWrapper.createWith(buildSyncMediaRequest(context), listener);
    }
}
