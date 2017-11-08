package org.apache.commons.jexl2.parser;

public interface ParserVisitor {
    Object visit(ASTAdditiveNode aSTAdditiveNode, Object obj);

    Object visit(ASTAdditiveOperator aSTAdditiveOperator, Object obj);

    Object visit(ASTAmbiguous aSTAmbiguous, Object obj);

    Object visit(ASTAndNode aSTAndNode, Object obj);

    Object visit(ASTArrayAccess aSTArrayAccess, Object obj);

    Object visit(ASTArrayLiteral aSTArrayLiteral, Object obj);

    Object visit(ASTAssignment aSTAssignment, Object obj);

    Object visit(ASTBitwiseAndNode aSTBitwiseAndNode, Object obj);

    Object visit(ASTBitwiseComplNode aSTBitwiseComplNode, Object obj);

    Object visit(ASTBitwiseOrNode aSTBitwiseOrNode, Object obj);

    Object visit(ASTBitwiseXorNode aSTBitwiseXorNode, Object obj);

    Object visit(ASTBlock aSTBlock, Object obj);

    Object visit(ASTConstructorNode aSTConstructorNode, Object obj);

    Object visit(ASTDivNode aSTDivNode, Object obj);

    Object visit(ASTEQNode aSTEQNode, Object obj);

    Object visit(ASTERNode aSTERNode, Object obj);

    Object visit(ASTEmptyFunction aSTEmptyFunction, Object obj);

    Object visit(ASTFalseNode aSTFalseNode, Object obj);

    Object visit(ASTForeachStatement aSTForeachStatement, Object obj);

    Object visit(ASTFunctionNode aSTFunctionNode, Object obj);

    Object visit(ASTGENode aSTGENode, Object obj);

    Object visit(ASTGTNode aSTGTNode, Object obj);

    Object visit(ASTIdentifier aSTIdentifier, Object obj);

    Object visit(ASTIfStatement aSTIfStatement, Object obj);

    Object visit(ASTJexlScript aSTJexlScript, Object obj);

    Object visit(ASTLENode aSTLENode, Object obj);

    Object visit(ASTLTNode aSTLTNode, Object obj);

    Object visit(ASTMapEntry aSTMapEntry, Object obj);

    Object visit(ASTMapLiteral aSTMapLiteral, Object obj);

    Object visit(ASTMethodNode aSTMethodNode, Object obj);

    Object visit(ASTModNode aSTModNode, Object obj);

    Object visit(ASTMulNode aSTMulNode, Object obj);

    Object visit(ASTNENode aSTNENode, Object obj);

    Object visit(ASTNRNode aSTNRNode, Object obj);

    Object visit(ASTNotNode aSTNotNode, Object obj);

    Object visit(ASTNullLiteral aSTNullLiteral, Object obj);

    Object visit(ASTNumberLiteral aSTNumberLiteral, Object obj);

    Object visit(ASTOrNode aSTOrNode, Object obj);

    Object visit(ASTReference aSTReference, Object obj);

    Object visit(ASTReferenceExpression aSTReferenceExpression, Object obj);

    Object visit(ASTReturnStatement aSTReturnStatement, Object obj);

    Object visit(ASTSizeFunction aSTSizeFunction, Object obj);

    Object visit(ASTSizeMethod aSTSizeMethod, Object obj);

    Object visit(ASTStringLiteral aSTStringLiteral, Object obj);

    Object visit(ASTTernaryNode aSTTernaryNode, Object obj);

    Object visit(ASTTrueNode aSTTrueNode, Object obj);

    Object visit(ASTUnaryMinusNode aSTUnaryMinusNode, Object obj);

    Object visit(ASTVar aSTVar, Object obj);

    Object visit(ASTWhileStatement aSTWhileStatement, Object obj);

    Object visit(SimpleNode simpleNode, Object obj);
}
