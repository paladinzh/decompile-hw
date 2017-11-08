package org.apache.commons.jexl2.parser;

import org.apache.commons.jexl2.DebugInfo;
import org.apache.commons.jexl2.JexlEngine.Scope;
import org.apache.commons.jexl2.JexlException.Parsing;

public class JexlParser extends StringParser {
    protected Scope frame;

    public void setFrame(Scope theFrame) {
        this.frame = theFrame;
    }

    public Scope getFrame() {
        return this.frame;
    }

    public String checkVariable(ASTIdentifier identifier, String image) {
        if (this.frame != null) {
            Integer register = this.frame.getRegister(image);
            if (register != null) {
                identifier.setRegister(register.intValue());
            }
        }
        return image;
    }

    public void declareVariable(ASTVar identifier, String image) {
        if (this.frame == null) {
            this.frame = new Scope((String[]) null);
        }
        identifier.setRegister(this.frame.declareVariable(image).intValue());
        identifier.image = image;
    }

    public void Identifier(boolean top) throws ParseException {
    }

    public final void Identifier() throws ParseException {
        Identifier(false);
    }

    public Token getToken(int index) {
        return null;
    }

    void jjtreeOpenNodeScope(JexlNode n) {
    }

    void jjtreeCloseNodeScope(JexlNode n) throws ParseException {
        if ((n instanceof ASTAmbiguous) && n.jjtGetNumChildren() > 0) {
            DebugInfo dbgInfo;
            Token tok = getToken(0);
            if (tok == null) {
                dbgInfo = n.debugInfo();
            } else {
                dbgInfo = new DebugInfo(tok.image, tok.beginLine, tok.beginColumn);
            }
            throw new Parsing(dbgInfo, "Ambiguous statement, missing ';' between expressions", null);
        }
    }
}
