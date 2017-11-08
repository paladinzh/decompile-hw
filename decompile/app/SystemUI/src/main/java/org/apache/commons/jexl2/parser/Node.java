package org.apache.commons.jexl2.parser;

public interface Node {
    Object jjtAccept(ParserVisitor parserVisitor, Object obj);

    void jjtAddChild(Node node, int i);

    void jjtClose();

    void jjtOpen();

    void jjtSetParent(Node node);
}
