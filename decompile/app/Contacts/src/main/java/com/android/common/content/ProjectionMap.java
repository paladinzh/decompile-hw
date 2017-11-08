package com.android.common.content;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ProjectionMap extends HashMap<String, String> {
    private String[] mColumns;

    public static class Builder {
        private ProjectionMap mMap = new ProjectionMap();

        public Builder add(String column) {
            this.mMap.putColumn(column, column);
            return this;
        }

        public Builder add(String alias, String expression) {
            this.mMap.putColumn(alias, expression + " AS " + alias);
            return this;
        }

        public Builder addAll(ProjectionMap map) {
            for (Entry<String, String> entry : map.entrySet()) {
                this.mMap.putColumn((String) entry.getKey(), (String) entry.getValue());
            }
            return this;
        }

        public ProjectionMap build() {
            String[] columns = new String[this.mMap.size()];
            this.mMap.keySet().toArray(columns);
            Arrays.sort(columns);
            this.mMap.mColumns = columns;
            return this.mMap;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private void putColumn(String alias, String column) {
        super.put(alias, column);
    }

    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends String, ? extends String> map) {
        throw new UnsupportedOperationException();
    }
}
