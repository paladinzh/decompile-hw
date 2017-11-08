package com.android.contacts.hap.util;

import android.net.Uri;
import java.util.Set;

public interface SelectedDataCache {
    int getMaxLimit();

    Set<Uri> getSelectedDataUri();

    void removeSelectedUri(Uri uri);

    void setSelectedUri(Uri uri);
}
