package org.apache.commons.jexl2.parser;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.jexl2.parser.JexlNode.Literal;

public final class ASTMapLiteral extends JexlNode implements Literal<Object> {
    boolean constant = false;
    Map<?, ?> map = null;

    ASTMapLiteral(int id) {
        super(id);
    }

    public void jjtClose() {
        if (this.children == null || this.children.length == 0) {
            this.map = Collections.EMPTY_MAP;
            this.constant = true;
            return;
        }
        this.constant = isConstant();
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
