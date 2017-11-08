package org.apache.commons.jexl2.parser;

public class ASTGTNode extends JexlNode {
    public ASTGTNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
