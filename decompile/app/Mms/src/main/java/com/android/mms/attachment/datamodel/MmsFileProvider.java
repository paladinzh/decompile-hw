package com.android.mms.attachment.datamodel;

import android.content.Context;
import com.android.mms.attachment.Factory;
import java.io.File;

public class MmsFileProvider extends FileProvider {
    File getFile(String path, String extension) {
        return getFile(path);
    }

    private static File getFile(String path) {
        return new File(getDirectory(Factory.get().getApplicationContext()), path + ".dat");
    }

    private static File getDirectory(Context context) {
        return new File(context.getCacheDir(), "rawmms");
    }
}
