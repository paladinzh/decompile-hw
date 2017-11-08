package com.huawei.gallery.panorama;

import android.graphics.drawable.Drawable;

public class ShareService {
    public Drawable icon;
    public String label;
    public String name;
    public String packageName;

    public ShareService(String name, String packageName, String label, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.label = label;
        this.icon = icon;
    }
}
