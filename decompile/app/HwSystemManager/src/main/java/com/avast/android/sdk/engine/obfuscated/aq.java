package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.ProgressObserver;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.entity.FileEntity;

/* compiled from: Unknown */
public class aq extends FileEntity {
    private ProgressObserver a;

    public aq(File file, String str, ProgressObserver progressObserver) {
        super(file, str);
        this.a = progressObserver;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        long j = -1;
        if (this.file != null) {
            j = this.file.length();
        }
        super.writeTo(new as(outputStream, j, this.a));
    }
}
