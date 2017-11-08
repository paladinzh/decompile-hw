package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.app.EditorState;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;

public abstract class ImageFilter implements Cloneable {
    private FilterEnvironment mEnvironment = null;
    protected String mName = "Original";

    public abstract void useRepresentation(FilterRepresentation filterRepresentation);

    public void freeResources() {
    }

    public Bitmap apply(Bitmap bitmap) {
        return bitmap;
    }

    public void useEditorState(EditorState state) {
    }

    public FilterRepresentation getDefaultRepresentation() {
        return null;
    }

    public void setEnvironment(FilterEnvironment environment) {
        this.mEnvironment = environment;
    }

    public FilterEnvironment getEnvironment() {
        return this.mEnvironment;
    }
}
