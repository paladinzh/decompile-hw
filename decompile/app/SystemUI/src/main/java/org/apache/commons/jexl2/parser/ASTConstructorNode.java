package org.apache.commons.jexl2.parser;

public class ASTConstructorNode extends JexlNode {
    public ASTConstructorNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
