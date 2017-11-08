package org.apache.commons.jexl2.parser;

public class ASTBitwiseXorNode extends JexlNode {
    public ASTBitwiseXorNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
