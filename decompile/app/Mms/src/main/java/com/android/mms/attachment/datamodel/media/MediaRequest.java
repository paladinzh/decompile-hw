package com.android.mms.attachment.datamodel.media;

import java.util.List;

public interface MediaRequest<T extends RefCountedMediaResource> {
    MediaRequestDescriptor<T> getDescriptor();

    String getKey();

    MediaCache<T> getMediaCache();

    int getRequestType();

    T loadMediaBlocking(List<MediaRequest<T>> list) throws Exception;
}
