package android.support.v4.media.session;

public final class MediaControllerCompat {
    private final MediaControllerImpl mImpl;

    interface MediaControllerImpl {
        Object getMediaController();
    }

    public Object getMediaController() {
        return this.mImpl.getMediaController();
    }
}
