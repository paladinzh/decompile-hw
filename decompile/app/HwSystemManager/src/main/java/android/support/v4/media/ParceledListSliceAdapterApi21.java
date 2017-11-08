package android.support.v4.media;

import android.media.browse.MediaBrowser.MediaItem;
import java.lang.reflect.Constructor;
import java.util.List;

class ParceledListSliceAdapterApi21 {
    private static Constructor sConstructor;

    ParceledListSliceAdapterApi21() {
    }

    static {
        try {
            sConstructor = Class.forName("android.content.pm.ParceledListSlice").getConstructor(new Class[]{List.class});
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    static Object newInstance(List<MediaItem> itemList) {
        Object result = null;
        try {
            result = sConstructor.newInstance(new Object[]{itemList});
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return result;
    }
}
