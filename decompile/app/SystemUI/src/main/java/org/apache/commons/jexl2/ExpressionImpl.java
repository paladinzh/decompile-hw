package org.apache.commons.jexl2;

import fyusion.vislib.BuildConfig;
import org.apache.commons.jexl2.parser.ASTJexlScript;

public class ExpressionImpl implements Expression, Script {
    protected final String expression;
    protected final JexlEngine jexl;
    protected final ASTJexlScript script;

    protected ExpressionImpl(JexlEngine engine, String expr, ASTJexlScript ref) {
        this.jexl = engine;
        this.expression = expr;
        this.script = ref;
    }

    public Object evaluate(JexlContext context) {
        if (this.script.jjtGetNumChildren() < 1) {
            return null;
        }
        Interpreter interpreter = this.jexl.createInterpreter(context);
        interpreter.setFrame(this.script.createFrame((Object[]) null));
        return interpreter.interpret(this.script.jjtGetChild(0));
    }

    public String getExpression() {
        return this.expression;
    }

    public String toString() {
        String expr = getExpression();
        return expr != null ? expr : BuildConfig.FLAVOR;
    }

    public Object execute(JexlContext context, Object... args) {
        Interpreter interpreter = this.jexl.createInterpreter(context);
        interpreter.setFrame(this.script.createFrame(args));
        return interpreter.interpret(this.script);
    }
}
