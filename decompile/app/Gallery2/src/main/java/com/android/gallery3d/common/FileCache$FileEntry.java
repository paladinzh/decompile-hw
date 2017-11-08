package com.android.gallery3d.common;

import com.android.gallery3d.common.Entry.Column;
import com.android.gallery3d.common.Entry.Table;

@Table("files")
class FileCache$FileEntry extends Entry {
    public static final EntrySchema SCHEMA = new EntrySchema(FileCache$FileEntry.class);
    @Column("content_url")
    public String contentUrl;
    @Column("filename")
    public String filename;
    @Column(indexed = true, value = "hash_code")
    public long hashCode;
    @Column(indexed = true, value = "last_access")
    public long lastAccess;
    @Column("size")
    public long size;

    private FileCache$FileEntry() {
    }

    public String toString() {
        return "hash_code: " + this.hashCode + ", " + "content_url" + this.contentUrl + ", " + "last_access" + this.lastAccess + ", " + "filename" + this.filename;
    }
}
