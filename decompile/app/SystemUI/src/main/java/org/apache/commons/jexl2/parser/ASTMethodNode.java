package org.apache.commons.jexl2.parser;

public class ASTMethodNode extends JexlNode {
    public ASTMethodNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
