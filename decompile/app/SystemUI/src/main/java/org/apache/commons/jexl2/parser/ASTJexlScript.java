package org.apache.commons.jexl2.parser;

import org.apache.commons.jexl2.JexlEngine.Frame;
import org.apache.commons.jexl2.JexlEngine.Scope;

public class ASTJexlScript extends JexlNode {
    private Scope scope = null;

    public ASTJexlScript(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public void setScope(Scope theScope) {
        this.scope = theScope;
    }

    public Scope getScope() {
        return this.scope;
    }

    public Frame createFrame(Object... values) {
        return this.scope == null ? null : this.scope.createFrame(values);
    }
}
