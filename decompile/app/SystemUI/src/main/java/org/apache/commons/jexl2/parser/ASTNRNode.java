package org.apache.commons.jexl2.parser;

public class ASTNRNode extends JexlNode {
    public ASTNRNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
