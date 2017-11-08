package com.android.gallery3d.data;

import com.android.gallery3d.common.Entry;
import com.android.gallery3d.common.Entry.Column;
import com.android.gallery3d.common.Entry.Table;
import com.android.gallery3d.common.EntrySchema;

@Table("download")
public class DownloadEntry extends Entry {
    public static final EntrySchema SCHEMA = new EntrySchema(DownloadEntry.class);
    @Column("_size")
    public long contentSize;
    @Column("content_url")
    public String contentUrl;
    @Column("etag")
    public String eTag;
    @Column(indexed = true, value = "hash_code")
    public long hashCode;
    @Column(indexed = true, value = "last_access")
    public long lastAccessTime;
    @Column("last_updated")
    public long lastUpdatedTime;
    @Column("_data")
    public String path;

    public String toString() {
        return "hash_code: " + this.hashCode + ", " + "content_url" + this.contentUrl + ", " + "_size" + this.contentSize + ", " + "etag" + this.eTag + ", " + "last_access" + this.lastAccessTime + ", " + "last_updated" + this.lastUpdatedTime + "," + "_data" + this.path;
    }
}
