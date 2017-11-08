package org.apache.commons.jexl2.parser;

public class ASTTernaryNode extends JexlNode {
    public ASTTernaryNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
