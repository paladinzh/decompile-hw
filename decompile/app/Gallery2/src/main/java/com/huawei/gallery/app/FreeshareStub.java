package com.huawei.gallery.app;

import android.app.Activity;
import android.os.Bundle;
import com.android.gallery3d.util.GalleryLog;

public class FreeshareStub extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GalleryLog.d("FreeshareStub", "U called FreeshareStub, it's a wrong, finish!");
        finish();
    }
}
