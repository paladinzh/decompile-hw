package org.apache.commons.jexl2.parser;

import fyusion.vislib.BuildConfig;
import org.apache.commons.jexl2.DebugInfo;
import org.apache.commons.jexl2.JexlInfo;

public abstract class JexlNode extends SimpleNode implements JexlInfo {
    public String image;

    public interface Literal<T> {
    }

    public JexlNode(int id) {
        super(id);
    }

    public DebugInfo debugInfo() {
        for (JexlNode node = this; node != null; node = node.jjtGetParent()) {
            if (node.value instanceof DebugInfo) {
                return (DebugInfo) node.value;
            }
        }
        return null;
    }

    public String debugString() {
        DebugInfo info = debugInfo();
        return info == null ? BuildConfig.FLAVOR : info.debugString();
    }

    public final boolean isConstant() {
        return isConstant(this instanceof Literal);
    }

    protected boolean isConstant(boolean literal) {
        if (!literal) {
            return false;
        }
        if (this.children != null) {
            for (JexlNode child : this.children) {
                if (child instanceof ASTReference) {
                    if (!child.isConstant(true)) {
                        return false;
                    }
                } else if (child instanceof ASTMapEntry) {
                    if (!child.isConstant(true)) {
                        return false;
                    }
                } else if (!child.isConstant()) {
                    return false;
                }
            }
        }
        return true;
    }
}
