package com.android.mms.util;

import android.content.ContentValues;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropertyNode {
    public ContentValues paramMap = new ContentValues();
    public Set<String> paramMap_TYPE = new HashSet();
    public Set<String> propGroupSet = new HashSet();
    public String propName = "";
    public String propValue = "";
    public byte[] propValue_bytes;
    public List<String> propValue_vector = new ArrayList();

    public int hashCode() {
        throw new UnsupportedOperationException("PropertyNode does not provide hashCode() implementation intentionally.");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(Object obj) {
        boolean z = true;
        if (!(obj instanceof PropertyNode)) {
            return false;
        }
        PropertyNode node = (PropertyNode) obj;
        if (this.propName == null || !this.propName.equals(node.propName) || !this.paramMap_TYPE.equals(node.paramMap_TYPE) || !this.paramMap_TYPE.equals(node.paramMap_TYPE) || !this.propGroupSet.equals(node.propGroupSet)) {
            return false;
        }
        if (this.propValue_bytes != null && Arrays.equals(this.propValue_bytes, node.propValue_bytes)) {
            return true;
        }
        if (!this.propValue.equals(node.propValue)) {
            return false;
        }
        if (!(this.propValue_vector.equals(node.propValue_vector) || this.propValue_vector.size() == 1 || node.propValue_vector.size() == 1)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("propName: ");
        builder.append(this.propName);
        builder.append(", paramMap: ");
        builder.append(this.paramMap.toString());
        builder.append(", paramMap_TYPE: [");
        boolean first = true;
        for (String elem : this.paramMap_TYPE) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append('\"');
            builder.append(elem);
            builder.append('\"');
        }
        builder.append("]");
        if (!this.propGroupSet.isEmpty()) {
            builder.append(", propGroupSet: [");
            first = true;
            for (String elem2 : this.propGroupSet) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append('\"');
                builder.append(elem2);
                builder.append('\"');
            }
            builder.append("]");
        }
        if (this.propValue_vector != null && this.propValue_vector.size() > 1) {
            builder.append(", propValue_vector size: ");
            builder.append(this.propValue_vector.size());
        }
        if (this.propValue_bytes != null) {
            builder.append(", propValue_bytes size: ");
            builder.append(this.propValue_bytes.length);
        }
        builder.append(", propValue: \"");
        builder.append(this.propValue);
        builder.append("\"");
        return builder.toString();
    }
}
