package com.huawei.gallery.editor.category;

import com.huawei.gallery.editor.app.EditorState;

public class IconData {
    private int mDrawableId;
    private EditorState mEditorState;
    private String mText;
    private int mTextId;
    private int mViewId;

    public IconData(int textViewId, int drawableId, int textId) {
        this.mViewId = textViewId;
        this.mDrawableId = drawableId;
        this.mTextId = textId;
        this.mText = "";
    }

    public IconData(int textViewId, int drawableId, String text) {
        this(textViewId, drawableId, 0);
        this.mText = text;
    }

    public void setEditorState(EditorState editorState) {
        this.mEditorState = editorState;
    }

    public EditorState getEditorState() {
        return this.mEditorState;
    }

    int getViewId() {
        return this.mViewId;
    }

    int getTextId() {
        return this.mTextId;
    }

    String getText() {
        return this.mText;
    }

    int getDrawableId() {
        return this.mDrawableId;
    }
}
