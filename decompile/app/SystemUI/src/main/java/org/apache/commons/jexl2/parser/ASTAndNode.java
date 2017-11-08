package org.apache.commons.jexl2.parser;

public class ASTAndNode extends JexlNode {
    public ASTAndNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
