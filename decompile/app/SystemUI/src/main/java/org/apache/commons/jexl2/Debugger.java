package org.apache.commons.jexl2;

import java.util.regex.Pattern;
import org.apache.commons.jexl2.parser.ASTAdditiveNode;
import org.apache.commons.jexl2.parser.ASTAdditiveOperator;
import org.apache.commons.jexl2.parser.ASTAmbiguous;
import org.apache.commons.jexl2.parser.ASTAndNode;
import org.apache.commons.jexl2.parser.ASTArrayAccess;
import org.apache.commons.jexl2.parser.ASTArrayLiteral;
import org.apache.commons.jexl2.parser.ASTAssignment;
import org.apache.commons.jexl2.parser.ASTBitwiseAndNode;
import org.apache.commons.jexl2.parser.ASTBitwiseComplNode;
import org.apache.commons.jexl2.parser.ASTBitwiseOrNode;
import org.apache.commons.jexl2.parser.ASTBitwiseXorNode;
import org.apache.commons.jexl2.parser.ASTBlock;
import org.apache.commons.jexl2.parser.ASTConstructorNode;
import org.apache.commons.jexl2.parser.ASTDivNode;
import org.apache.commons.jexl2.parser.ASTEQNode;
import org.apache.commons.jexl2.parser.ASTERNode;
import org.apache.commons.jexl2.parser.ASTEmptyFunction;
import org.apache.commons.jexl2.parser.ASTFalseNode;
import org.apache.commons.jexl2.parser.ASTForeachStatement;
import org.apache.commons.jexl2.parser.ASTFunctionNode;
import org.apache.commons.jexl2.parser.ASTGENode;
import org.apache.commons.jexl2.parser.ASTGTNode;
import org.apache.commons.jexl2.parser.ASTIdentifier;
import org.apache.commons.jexl2.parser.ASTIfStatement;
import org.apache.commons.jexl2.parser.ASTJexlScript;
import org.apache.commons.jexl2.parser.ASTLENode;
import org.apache.commons.jexl2.parser.ASTLTNode;
import org.apache.commons.jexl2.parser.ASTMapEntry;
import org.apache.commons.jexl2.parser.ASTMapLiteral;
import org.apache.commons.jexl2.parser.ASTMethodNode;
import org.apache.commons.jexl2.parser.ASTModNode;
import org.apache.commons.jexl2.parser.ASTMulNode;
import org.apache.commons.jexl2.parser.ASTNENode;
import org.apache.commons.jexl2.parser.ASTNRNode;
import org.apache.commons.jexl2.parser.ASTNotNode;
import org.apache.commons.jexl2.parser.ASTNullLiteral;
import org.apache.commons.jexl2.parser.ASTNumberLiteral;
import org.apache.commons.jexl2.parser.ASTOrNode;
import org.apache.commons.jexl2.parser.ASTReference;
import org.apache.commons.jexl2.parser.ASTReferenceExpression;
import org.apache.commons.jexl2.parser.ASTReturnStatement;
import org.apache.commons.jexl2.parser.ASTSizeFunction;
import org.apache.commons.jexl2.parser.ASTSizeMethod;
import org.apache.commons.jexl2.parser.ASTStringLiteral;
import org.apache.commons.jexl2.parser.ASTTernaryNode;
import org.apache.commons.jexl2.parser.ASTTrueNode;
import org.apache.commons.jexl2.parser.ASTUnaryMinusNode;
import org.apache.commons.jexl2.parser.ASTVar;
import org.apache.commons.jexl2.parser.ASTWhileStatement;
import org.apache.commons.jexl2.parser.JexlNode;
import org.apache.commons.jexl2.parser.ParserVisitor;
import org.apache.commons.jexl2.parser.SimpleNode;

final class Debugger implements ParserVisitor {
    private static final Pattern QUOTED_IDENTIFIER = Pattern.compile("['\"\\s\\\\]");
    private final StringBuilder builder = new StringBuilder();
    private JexlNode cause = null;
    private int end = 0;
    private int start = 0;

    Debugger() {
    }

    public boolean debug(JexlNode node) {
        this.start = 0;
        this.end = 0;
        if (node != null) {
            this.builder.setLength(0);
            this.cause = node;
            JexlNode root = node;
            while (root.jjtGetParent() != null) {
                root = root.jjtGetParent();
            }
            root.jjtAccept(this, null);
        }
        if (this.end <= 0) {
            return false;
        }
        return true;
    }

    public String data() {
        return this.builder.toString();
    }

    public int start() {
        return this.start;
    }

    public int end() {
        return this.end;
    }

    private Object accept(JexlNode node, Object data) {
        if (node == this.cause) {
            this.start = this.builder.length();
        }
        Object value = node.jjtAccept(this, data);
        if (node == this.cause) {
            this.end = this.builder.length();
        }
        return value;
    }

    private Object acceptStatement(JexlNode child, Object data) {
        Object value = accept(child, data);
        if ((child instanceof ASTBlock) || (child instanceof ASTIfStatement) || (child instanceof ASTForeachStatement) || (child instanceof ASTWhileStatement)) {
            return value;
        }
        this.builder.append(";");
        return value;
    }

    private Object check(JexlNode node, String image, Object data) {
        if (node == this.cause) {
            this.start = this.builder.length();
        }
        if (image == null) {
            this.builder.append(node.toString());
        } else {
            this.builder.append(image);
        }
        if (node == this.cause) {
            this.end = this.builder.length();
        }
        return data;
    }

    private Object infixChildren(JexlNode node, String infix, boolean paren, Object data) {
        int num = node.jjtGetNumChildren();
        if (paren) {
            this.builder.append("(");
        }
        for (int i = 0; i < num; i++) {
            if (i > 0) {
                this.builder.append(infix);
            }
            accept(node.jjtGetChild(i), data);
        }
        if (paren) {
            this.builder.append(")");
        }
        return data;
    }

    private Object prefixChild(JexlNode node, String prefix, Object data) {
        boolean paren = true;
        if (node.jjtGetChild(0).jjtGetNumChildren() <= 1) {
            paren = false;
        }
        this.builder.append(prefix);
        if (paren) {
            this.builder.append("(");
        }
        accept(node.jjtGetChild(0), data);
        if (paren) {
            this.builder.append(")");
        }
        return data;
    }

    public Object visit(ASTAdditiveNode node, Object data) {
        boolean paren = (node.jjtGetParent() instanceof ASTMulNode) || (node.jjtGetParent() instanceof ASTDivNode) || (node.jjtGetParent() instanceof ASTModNode);
        int num = node.jjtGetNumChildren();
        if (paren) {
            this.builder.append("(");
        }
        accept(node.jjtGetChild(0), data);
        for (int i = 1; i < num; i++) {
            accept(node.jjtGetChild(i), data);
        }
        if (paren) {
            this.builder.append(")");
        }
        return data;
    }

    public Object visit(ASTAdditiveOperator node, Object data) {
        this.builder.append(' ');
        this.builder.append(node.image);
        this.builder.append(' ');
        return data;
    }

    public Object visit(ASTAndNode node, Object data) {
        return infixChildren(node, " && ", false, data);
    }

    public Object visit(ASTArrayAccess node, Object data) {
        accept(node.jjtGetChild(0), data);
        int num = node.jjtGetNumChildren();
        for (int i = 1; i < num; i++) {
            this.builder.append("[");
            accept(node.jjtGetChild(i), data);
            this.builder.append("]");
        }
        return data;
    }

    public Object visit(ASTArrayLiteral node, Object data) {
        int num = node.jjtGetNumChildren();
        this.builder.append("[ ");
        if (num > 0) {
            accept(node.jjtGetChild(0), data);
            for (int i = 1; i < num; i++) {
                this.builder.append(", ");
                accept(node.jjtGetChild(i), data);
            }
        }
        this.builder.append(" ]");
        return data;
    }

    public Object visit(ASTAssignment node, Object data) {
        return infixChildren(node, " = ", false, data);
    }

    public Object visit(ASTBitwiseAndNode node, Object data) {
        return infixChildren(node, " & ", false, data);
    }

    public Object visit(ASTBitwiseComplNode node, Object data) {
        return prefixChild(node, "~", data);
    }

    public Object visit(ASTBitwiseOrNode node, Object data) {
        return infixChildren(node, " | ", node.jjtGetParent() instanceof ASTBitwiseAndNode, data);
    }

    public Object visit(ASTBitwiseXorNode node, Object data) {
        return infixChildren(node, " ^ ", node.jjtGetParent() instanceof ASTBitwiseAndNode, data);
    }

    public Object visit(ASTBlock node, Object data) {
        this.builder.append("{ ");
        int num = node.jjtGetNumChildren();
        for (int i = 0; i < num; i++) {
            acceptStatement(node.jjtGetChild(i), data);
        }
        this.builder.append(" }");
        return data;
    }

    public Object visit(ASTDivNode node, Object data) {
        return infixChildren(node, " / ", false, data);
    }

    public Object visit(ASTEmptyFunction node, Object data) {
        this.builder.append("empty(");
        accept(node.jjtGetChild(0), data);
        this.builder.append(")");
        return data;
    }

    public Object visit(ASTEQNode node, Object data) {
        return infixChildren(node, " == ", false, data);
    }

    public Object visit(ASTERNode node, Object data) {
        return infixChildren(node, " =~ ", false, data);
    }

    public Object visit(ASTFalseNode node, Object data) {
        return check(node, "false", data);
    }

    public Object visit(ASTForeachStatement node, Object data) {
        this.builder.append("for(");
        accept(node.jjtGetChild(0), data);
        this.builder.append(" : ");
        accept(node.jjtGetChild(1), data);
        this.builder.append(") ");
        if (node.jjtGetNumChildren() <= 2) {
            this.builder.append(';');
        } else {
            acceptStatement(node.jjtGetChild(2), data);
        }
        return data;
    }

    public Object visit(ASTGENode node, Object data) {
        return infixChildren(node, " >= ", false, data);
    }

    public Object visit(ASTGTNode node, Object data) {
        return infixChildren(node, " > ", false, data);
    }

    public Object visit(ASTIdentifier node, Object data) {
        String image = node.image;
        if (QUOTED_IDENTIFIER.matcher(image).find()) {
            image = "'" + node.image.replace("'", "\\'") + "'";
        }
        return check(node, image, data);
    }

    public Object visit(ASTIfStatement node, Object data) {
        this.builder.append("if (");
        accept(node.jjtGetChild(0), data);
        this.builder.append(") ");
        if (node.jjtGetNumChildren() <= 1) {
            this.builder.append(';');
        } else {
            acceptStatement(node.jjtGetChild(1), data);
            if (node.jjtGetNumChildren() <= 2) {
                this.builder.append(';');
            } else {
                this.builder.append(" else ");
                acceptStatement(node.jjtGetChild(2), data);
            }
        }
        return data;
    }

    public Object visit(ASTNumberLiteral node, Object data) {
        return check(node, node.image, data);
    }

    public Object visit(ASTJexlScript node, Object data) {
        int num = node.jjtGetNumChildren();
        for (int i = 0; i < num; i++) {
            acceptStatement(node.jjtGetChild(i), data);
        }
        return data;
    }

    public Object visit(ASTLENode node, Object data) {
        return infixChildren(node, " <= ", false, data);
    }

    public Object visit(ASTLTNode node, Object data) {
        return infixChildren(node, " < ", false, data);
    }

    public Object visit(ASTMapEntry node, Object data) {
        accept(node.jjtGetChild(0), data);
        this.builder.append(" : ");
        accept(node.jjtGetChild(1), data);
        return data;
    }

    public Object visit(ASTMapLiteral node, Object data) {
        int num = node.jjtGetNumChildren();
        this.builder.append("{ ");
        if (num <= 0) {
            this.builder.append(':');
        } else {
            accept(node.jjtGetChild(0), data);
            for (int i = 1; i < num; i++) {
                this.builder.append(", ");
                accept(node.jjtGetChild(i), data);
            }
        }
        this.builder.append(" }");
        return data;
    }

    public Object visit(ASTConstructorNode node, Object data) {
        int num = node.jjtGetNumChildren();
        this.builder.append("new ");
        this.builder.append("(");
        accept(node.jjtGetChild(0), data);
        for (int i = 1; i < num; i++) {
            this.builder.append(", ");
            accept(node.jjtGetChild(i), data);
        }
        this.builder.append(")");
        return data;
    }

    public Object visit(ASTFunctionNode node, Object data) {
        int num = node.jjtGetNumChildren();
        accept(node.jjtGetChild(0), data);
        this.builder.append(":");
        accept(node.jjtGetChild(1), data);
        this.builder.append("(");
        for (int i = 2; i < num; i++) {
            if (i > 2) {
                this.builder.append(", ");
            }
            accept(node.jjtGetChild(i), data);
        }
        this.builder.append(")");
        return data;
    }

    public Object visit(ASTMethodNode node, Object data) {
        int num = node.jjtGetNumChildren();
        accept(node.jjtGetChild(0), data);
        this.builder.append("(");
        for (int i = 1; i < num; i++) {
            if (i > 1) {
                this.builder.append(", ");
            }
            accept(node.jjtGetChild(i), data);
        }
        this.builder.append(")");
        return data;
    }

    public Object visit(ASTModNode node, Object data) {
        return infixChildren(node, " % ", false, data);
    }

    public Object visit(ASTMulNode node, Object data) {
        return infixChildren(node, " * ", false, data);
    }

    public Object visit(ASTNENode node, Object data) {
        return infixChildren(node, " != ", false, data);
    }

    public Object visit(ASTNRNode node, Object data) {
        return infixChildren(node, " !~ ", false, data);
    }

    public Object visit(ASTNotNode node, Object data) {
        this.builder.append("!");
        accept(node.jjtGetChild(0), data);
        return data;
    }

    public Object visit(ASTNullLiteral node, Object data) {
        check(node, "null", data);
        return data;
    }

    public Object visit(ASTOrNode node, Object data) {
        return infixChildren(node, " || ", node.jjtGetParent() instanceof ASTAndNode, data);
    }

    public Object visit(ASTReference node, Object data) {
        int num = node.jjtGetNumChildren();
        accept(node.jjtGetChild(0), data);
        for (int i = 1; i < num; i++) {
            this.builder.append(".");
            accept(node.jjtGetChild(i), data);
        }
        return data;
    }

    public Object visit(ASTReferenceExpression node, Object data) {
        JexlNode first = node.jjtGetChild(0);
        this.builder.append('(');
        accept(first, data);
        this.builder.append(')');
        int num = node.jjtGetNumChildren();
        for (int i = 1; i < num; i++) {
            this.builder.append("[");
            accept(node.jjtGetChild(i), data);
            this.builder.append("]");
        }
        return data;
    }

    public Object visit(ASTReturnStatement node, Object data) {
        this.builder.append("return ");
        accept(node.jjtGetChild(0), data);
        return data;
    }

    public Object visit(ASTSizeFunction node, Object data) {
        this.builder.append("size(");
        accept(node.jjtGetChild(0), data);
        this.builder.append(")");
        return data;
    }

    public Object visit(ASTSizeMethod node, Object data) {
        check(node, "size()", data);
        return data;
    }

    public Object visit(ASTStringLiteral node, Object data) {
        return check(node, "'" + node.image.replace("'", "\\'") + "'", data);
    }

    public Object visit(ASTTernaryNode node, Object data) {
        accept(node.jjtGetChild(0), data);
        if (node.jjtGetNumChildren() <= 2) {
            this.builder.append("?:");
            accept(node.jjtGetChild(1), data);
        } else {
            this.builder.append("? ");
            accept(node.jjtGetChild(1), data);
            this.builder.append(" : ");
            accept(node.jjtGetChild(2), data);
        }
        return data;
    }

    public Object visit(ASTTrueNode node, Object data) {
        check(node, "true", data);
        return data;
    }

    public Object visit(ASTUnaryMinusNode node, Object data) {
        return prefixChild(node, "-", data);
    }

    public Object visit(ASTVar node, Object data) {
        this.builder.append("var ");
        check(node, node.image, data);
        return data;
    }

    public Object visit(ASTWhileStatement node, Object data) {
        this.builder.append("while (");
        accept(node.jjtGetChild(0), data);
        this.builder.append(") ");
        if (node.jjtGetNumChildren() <= 1) {
            this.builder.append(';');
        } else {
            acceptStatement(node.jjtGetChild(1), data);
        }
        return data;
    }

    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException("unexpected type of node");
    }

    public Object visit(ASTAmbiguous node, Object data) {
        throw new UnsupportedOperationException("unexpected type of node");
    }
}
