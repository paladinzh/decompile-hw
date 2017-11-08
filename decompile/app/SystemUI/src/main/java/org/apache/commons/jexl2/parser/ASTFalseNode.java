package org.apache.commons.jexl2.parser;

public class ASTFalseNode extends JexlNode {
    public ASTFalseNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
