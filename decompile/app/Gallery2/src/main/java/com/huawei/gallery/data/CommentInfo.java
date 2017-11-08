package com.huawei.gallery.data;

import android.text.TextUtils;

public class CommentInfo {
    String appVer = "";
    String content = "";
    String contenturi = "";
    String cpname = "";
    String download = "";
    private String mFilePath;
    String pkgname = "";
    boolean supported = true;
    String title = "";
    String worksdes = "";

    public boolean supportByExif() {
        return this.supported;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    CommentInfo() {
    }

    public String getContent() {
        return this.content.replaceAll("\r|\n", "").trim();
    }

    public void setContent(String content) {
        if (TextUtils.isEmpty(content)) {
            content = "";
        }
        this.content = content;
    }

    public int getContentSizeLimit() {
        return Math.max(this.content.length(), 140);
    }

    public String toString() {
        return CommentHelper.packComment(this);
    }
}
