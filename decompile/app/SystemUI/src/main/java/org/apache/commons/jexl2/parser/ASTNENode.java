package org.apache.commons.jexl2.parser;

public class ASTNENode extends JexlNode {
    public ASTNENode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
