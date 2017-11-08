package com.android.mms.model;

import android.content.Context;
import android.net.Uri;
import com.google.android.mms.MmsException;

public abstract class RegionMediaModel extends MediaModel {
    protected RegionModel mRegion;
    protected boolean mVisible;

    public RegionMediaModel(Context context, String tag, Uri uri, RegionModel region) throws MmsException {
        this(context, tag, null, null, uri, region);
    }

    public RegionMediaModel(Context context, String tag, String contentType, String src, Uri uri, RegionModel region) throws MmsException {
        super(context, tag, contentType, src, uri);
        this.mVisible = true;
        this.mRegion = region;
    }

    public RegionMediaModel(Context context, String tag, String contentType, String src, byte[] data, RegionModel region) {
        super(context, tag, contentType, src, data);
        this.mVisible = true;
        this.mRegion = region;
    }

    public RegionModel getRegion() {
        return this.mRegion;
    }

    public boolean isVisible() {
        return this.mVisible;
    }
}
