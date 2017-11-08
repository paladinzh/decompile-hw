package com.android.gallery3d.data;

import android.net.Uri;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

class UriSource extends MediaSource {
    private GalleryApp mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    public UriSource(GalleryApp context) {
        super("uri");
        this.mApplication = context;
        this.mMatcher.add("/uri/all", 0);
    }

    public MediaObject createMediaObject(Path path) {
        if (this.mMatcher.match(path) == 0) {
            return new UriAlbum(path, this.mApplication);
        }
        String[] segment = path.split();
        if (segment.length != 3) {
            throw new RuntimeException("bad path: " + path);
        }
        try {
            String uri = URLDecoder.decode(segment[1], "utf-8");
            return new UriImage(this.mApplication, path, Uri.parse(uri), URLDecoder.decode(segment[2], "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public Path findPathByUri(Uri uri, String type) {
        return GalleryUtils.findPathByUriForUriImage(this.mApplication, uri, type);
    }

    public Path getDefaultSetOf(Path item) {
        if (this.mApplication.getDataManager().getMediaObject(item) instanceof UriImage) {
            return Path.fromString("/uri/all");
        }
        return super.getDefaultSetOf(item);
    }
}
