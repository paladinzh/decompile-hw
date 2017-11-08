package com.google.android.gms.internal;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.metadata.CollectionMetadataField;
import com.google.android.gms.drive.metadata.MetadataField;
import com.google.android.gms.drive.metadata.internal.a;
import com.google.android.gms.drive.metadata.internal.e;
import com.google.android.gms.drive.metadata.internal.g;
import com.google.android.gms.drive.metadata.internal.i;
import com.google.android.gms.drive.metadata.internal.j;

/* compiled from: Unknown */
public class fs {
    public static final MetadataField<String> EA = new j("webContentLink", 4300000);
    public static final MetadataField<String> EB = new j("webViewLink", 4300000);
    public static final MetadataField<String> EC = new j("indexableText", 4300000);
    public static final MetadataField<Boolean> ED = new a("hasThumbnail", 4300000);
    public static final MetadataField<DriveId> El = fu.EI;
    public static final MetadataField<Boolean> Em = new a("isEditable", 4100000);
    public static final MetadataField<Boolean> En = new a("isAppData", 4300000);
    public static final MetadataField<Boolean> Eo = new a("isShared", 4300000);
    public static final MetadataField<String> Ep = new j("alternateLink", 4300000);
    public static final CollectionMetadataField<String> Eq = new i("ownerNames", 4300000);
    public static final MetadataField<String> Er = new j("description", 4300000);
    public static final MetadataField<Boolean> Es = new a("isCopyable", 4300000);
    public static final MetadataField<String> Et = new j("embedLink", 4300000);
    public static final MetadataField<String> Eu = new j("fileExtension", 4300000);
    public static final MetadataField<Long> Ev = new e("fileSize", 4300000);
    public static final MetadataField<Boolean> Ew = new a("isViewed", 4300000);
    public static final MetadataField<Boolean> Ex = new a("isRestricted", 4300000);
    public static final MetadataField<String> Ey = new j("originalFilename", 4300000);
    public static final MetadataField<Long> Ez = new e("quotaBytesUsed", 4300000);
    public static final MetadataField<Boolean> IS_PINNED = new a("isPinned", 4100000);
    public static final MetadataField<String> MIME_TYPE = new j("mimeType", 4100000);
    public static final CollectionMetadataField<DriveId> PARENTS = new g("parents", 4100000);
    public static final MetadataField<Boolean> STARRED = new a("starred", 4100000);
    public static final MetadataField<String> TITLE = new j("title", 4100000);
    public static final MetadataField<Boolean> TRASHED = new a("trashed", 4100000) {
    };
}
