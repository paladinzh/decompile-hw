package org.apache.commons.jexl2.parser;

public class ASTOrNode extends JexlNode {
    public ASTOrNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
