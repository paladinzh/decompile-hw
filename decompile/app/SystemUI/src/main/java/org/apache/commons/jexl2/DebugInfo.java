package org.apache.commons.jexl2;

import fyusion.vislib.BuildConfig;

public class DebugInfo implements JexlInfo {
    private final int column;
    private final int line;
    private final String name;

    public DebugInfo(String tn, int l, int c) {
        this.name = tn;
        this.line = l;
        this.column = c;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.name == null ? BuildConfig.FLAVOR : this.name);
        if (this.line > 0) {
            sb.append("@");
            sb.append(this.line);
            if (this.column > 0) {
                sb.append(":");
                sb.append(this.column);
            }
        }
        return sb.toString();
    }

    public String debugString() {
        return toString();
    }

    public DebugInfo debugInfo() {
        return this;
    }

    public String getName() {
        return this.name;
    }

    public int getColumn() {
        return this.column;
    }
}
